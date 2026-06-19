package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.github.MichalKC.manylands.audio.AudioService;
import com.github.MichalKC.manylands.component.*;
import com.github.MichalKC.manylands.component.Attack.AttackType;
import com.github.MichalKC.manylands.component.Facing.FacingDirection;

public class AttackSystem extends IteratingSystem {
    public static final Rectangle attackAABB = new Rectangle();

    private final AudioService audioService;
    private final World world;
    private final Vector2 tmpVertex;
    private final ObjectSet<Body> hitBodies;
    private Body attackerBody;
    private boolean attackerIsEnemy;
    private float attackDamage;

    public AttackSystem(World world, AudioService audioService) {
        super(Family.all(Attack.class, Facing.class, Physic.class).get());
        this.audioService = audioService;
        this.world = world;
        this.tmpVertex = new Vector2();
        this.hitBodies = new ObjectSet<>();
        this.attackerBody = null;
        this.attackDamage = 0f;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (Dead.MAPPER.get(entity) != null) return;

        Attack attack = Attack.MAPPER.get(entity);

        if (attack.canAttack()) return;

        if (attack.isFreshAttackTick() && attack.getSfx() != null) {
            audioService.playSound(attack.getSfx());
            Move move = Move.MAPPER.get(entity);
            if (move != null) {
                move.setRooted(true);
            }
        }

        // Compute potential speed multiplier for player skill-based haste
        float speedMul = 1f;
        boolean entityIsEnemy = Enemy.MAPPER.get(entity) != null;
        if (!entityIsEnemy) {
            ActiveSkills skills = ActiveSkills.MAPPER.get(entity);
            if (skills != null) {
                AttackType at = attack.getActiveAttackType();
                if (skills.isSkill3Active() && at == AttackType.PRIMARY) {
                    speedMul = 1.4f;
                } else if (skills.isSkill5Active() && at == AttackType.SECONDARY) {
                    speedMul = 1.4f;
                }
            }
        }

        // Advance attack timer with speed multiplier
        attack.advanceElapsed(deltaTime * speedMul);
        // Match animation speed to attack haste while attacking, preserving base speed
        Animation2D anim = Animation2D.MAPPER.get(entity);
        if (anim != null) {
            if (speedMul != 1f) {
                com.github.MichalKC.manylands.component.AttackAnimSpeed base = com.github.MichalKC.manylands.component.AttackAnimSpeed.MAPPER.get(entity);
                if (base == null) {
                    base = new com.github.MichalKC.manylands.component.AttackAnimSpeed(anim.getSpeed());
                    entity.add(base);
                }
                anim.setSpeed(base.getBaseSpeed() * speedMul);
            }
        }

        if (attack.consumeDamageMoment()) {
            FacingDirection facingDirection = Facing.MAPPER.get(entity).getDirection();
            attackerBody = Physic.MAPPER.get(entity).getBody();
            attackerIsEnemy = Enemy.MAPPER.get(entity) != null;
            PolygonShape attackShape = getAttackFixture(attackerBody, facingDirection, attack.getActiveAttackType());
            updateAttachAABB(attackerBody.getPosition(), attackShape);

            this.attackDamage = attack.getDamage();
            // Apply active skill modifiers (player-only)
            if (!attackerIsEnemy) {
                ActiveSkills skills = ActiveSkills.MAPPER.get(entity);
                if (skills != null) {
                    if (skills.isSkill1Active() && attack.getActiveAttackType() == AttackType.PRIMARY) {
                        this.attackDamage *= 1.3f; // +30% for primary while skill #1 is active
                    } else if (skills.isSkill2Active() && attack.getActiveAttackType() == AttackType.SECONDARY) {
                        this.attackDamage *= 1.3f; // +30% for secondary while skill #2 is active
                    }
                    if (skills.isSkill8Active()) {
                        this.attackDamage *= 1.25f; // +25% for all attacks while skill #8 is active
                    }
                }
            }
            hitBodies.clear();
            world.QueryAABB(this::attackCallback, attackAABB.x, attackAABB.y, attackAABB.width, attackAABB.height);
        }

        if (attack.finishAttackIfLockExpired()) {
            Move move = Move.MAPPER.get(entity);
            if (move != null) {
                move.setRooted(false);
            }
            // Restore original animation speed after attack ends
            Animation2D anim2 = Animation2D.MAPPER.get(entity);
            com.github.MichalKC.manylands.component.AttackAnimSpeed base = com.github.MichalKC.manylands.component.AttackAnimSpeed.MAPPER.get(entity);
            if (anim2 != null && base != null) {
                anim2.setSpeed(base.getBaseSpeed());
                entity.remove(com.github.MichalKC.manylands.component.AttackAnimSpeed.class);
            }
        }
    }

    private void updateAttachAABB(Vector2 bodyPosition, PolygonShape attackShape) {
        attackShape.getVertex(0, tmpVertex);
        tmpVertex.add(bodyPosition);
        attackAABB.setPosition(tmpVertex.x, tmpVertex.y);

        attackShape.getVertex(2, tmpVertex);
        tmpVertex.add(bodyPosition);
        attackAABB.setSize(tmpVertex.x, tmpVertex.y);
    }

    private boolean attackCallback(Fixture fixture) {
        Body body = fixture.getBody();
        if (body.equals(attackerBody)) return true;
        if (hitBodies.contains(body)) return true;
        if (!(body.getUserData() instanceof Entity entity)) return true;

        // Enemies don't damage each other
        if (attackerIsEnemy && Enemy.MAPPER.get(entity) != null) return true;

        // Player cannot damage enemies when outside their roam zone
        if (!attackerIsEnemy) {
            Enemy targetEnemy = Enemy.MAPPER.get(entity);
            if (targetEnemy != null && targetEnemy.getRoamBounds() != null) {
                if (!targetEnemy.getRoamBounds().contains(attackerCollisionCenter())) {
                    return true;
                }
            }
        }

        DamageCooldown cooldown = DamageCooldown.MAPPER.get(entity);
        if (cooldown != null && cooldown.isActive()) {
            return true;
        }

        Life life = Life.MAPPER.get(entity);
        if (Dead.MAPPER.get(entity) != null) {
            return true;
        }
        if (life != null && life.getLife() <= 0f) {
            return true;
        }

        boolean canBeDamaged = life != null;
        if (!canBeDamaged) {
            Tiled tiled = Tiled.MAPPER.get(entity);
            if (tiled != null && tiled.getMapObjectRef() instanceof TiledMapTileMapObject obj) {
                boolean onObject = obj.getProperties().get("canBeDamaged", false, Boolean.class);
                boolean onTile = obj.getTile().getProperties().get("canBeDamaged", false, Boolean.class);
                canBeDamaged = onObject || onTile;
            }
        }
        if (!canBeDamaged) {
            return true;
        }

        if (Enemy.MAPPER.get(entity) == null) {
            Facing facing = Facing.MAPPER.get(entity);
            if (facing != null) {
                if (attackerBody.getPosition().x <= body.getPosition().x) {
                    facing.setDirection(Facing.FacingDirection.LEFT);
                } else {
                    facing.setDirection(Facing.FacingDirection.RIGHT);
                }
            }
        }

        hitBodies.add(body);

        Damaged damaged = Damaged.MAPPER.get(entity);
        if (damaged == null) {
            entity.add(new Damaged(this.attackDamage));
        } else {
            damaged.addDamage(this.attackDamage);
        }
        if (!attackerIsEnemy) {
            Entity attackerEntity = (Entity) attackerBody.getUserData();
            if (attackerEntity != null) {
                com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(attackerEntity);
                if (skills != null) {
                    if (skills.isSkill4Active()) {
                        skills.cancelSkill4Early();
                    }
                    if (Enemy.MAPPER.get(entity) != null && skills.isSkill7Active()) {
                        if (com.github.MichalKC.manylands.component.PetrifyOverride.MAPPER.get(entity) == null) {
                            entity.add(new com.github.MichalKC.manylands.component.PetrifyOverride());
                        }
                    }
                }
            }
        }
        return true;
    }

    private Vector2 attackerCollisionCenter() {
        Vector2 pos = attackerBody.getPosition();
        if (attackerBody.getFixtureList().size > 0) {
            Shape shape = attackerBody.getFixtureList().first().getShape();
            if (shape instanceof PolygonShape poly) {
                float cx = 0, cy = 0;
                int n = poly.getVertexCount();
                Vector2 v = new Vector2();
                for (int i = 0; i < n; i++) {
                    poly.getVertex(i, v);
                    cx += v.x;
                    cy += v.y;
                }
                return new Vector2(pos.x + cx / n, pos.y + cy / n);
            }
        }
        return new Vector2(pos);
    }

    private PolygonShape getAttackFixture(Body body, FacingDirection facingDirection, AttackType attackType) {
        Array<Fixture> fixtureList = body.getFixtureList();
        String fixtureName = attackType.getSensorPrefix() + facingDirection.getAtlasKey();
        for (Fixture fixture : fixtureList) {
            if (fixtureName.equals(fixture.getUserData()) && Shape.Type.Polygon.equals(fixture.getShape().getType())) {
                return (PolygonShape) fixture.getShape();
            }
        }

        throw new GdxRuntimeException("Entity has no attack sensor of name " + fixtureName);
    }
}
