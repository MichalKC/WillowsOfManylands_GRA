package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.github.MichalKC.manylands.component.*;
import com.github.MichalKC.manylands.component.Attack.AttackType;
import com.github.MichalKC.manylands.tiled.ZoneShape;

public class EnemyAISystem extends IteratingSystem {
    private static final Vector2 TMP = new Vector2();
    private static final Vector2 TMP_COL = new Vector2();
    private static final float PATROL_MIN_SEC = 1f;
    private static final float PATROL_MAX_SEC = 2.5f;
    private static final float IDLE_MIN_SEC = 2.5f;
    private static final float IDLE_MAX_SEC = 6f;
    private static final float STUCK_VELOCITY_THRESHOLD = 0.05f;

    private ImmutableArray<Entity> players;

    public EnemyAISystem() {
        super(Family.all(Enemy.class, Move.class, Physic.class, Facing.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        players = engine.getEntitiesFor(Family.all(Player.class, Physic.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (Dead.MAPPER.get(entity) != null) return;

        Move move = Move.MAPPER.get(entity);
        if (move.isRooted()) return;

        boolean petrify = false;
        for (int i = 0; i < players.size(); i++) {
            Entity p = players.get(i);
            if (Dead.MAPPER.get(p) != null) continue;
            com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(p);
            if (skills != null && skills.isSkill7Active()) { petrify = true; break; }
        }

        com.github.MichalKC.manylands.component.Graphic graphic = com.github.MichalKC.manylands.component.Graphic.MAPPER.get(entity);
        boolean overridden = com.github.MichalKC.manylands.component.PetrifyOverride.MAPPER.get(entity) != null;
        if (petrify && !overridden) {
            if (graphic != null) {
                float t = 0.6f;
                if (Math.abs(graphic.getColor().r - t) > 0.001f || Math.abs(graphic.getColor().g - t) > 0.001f || Math.abs(graphic.getColor().b - t) > 0.001f) {
                    graphic.getColor().r = t;
                    graphic.getColor().g = t;
                    graphic.getColor().b = t;
                }
            }
            com.github.MichalKC.manylands.component.Physic phys = com.github.MichalKC.manylands.component.Physic.MAPPER.get(entity);
            if (phys != null) {
                phys.getBody().setLinearVelocity(0f, 0f);
            }
            move.getDirection().setZero();
            return;
        } else if (petrify && overridden) {
            if (graphic != null) {
                float t = 1f;
                if (Math.abs(graphic.getColor().r - t) > 0.001f || Math.abs(graphic.getColor().g - t) > 0.001f || Math.abs(graphic.getColor().b - t) > 0.001f) {
                    graphic.getColor().r = t;
                    graphic.getColor().g = t;
                    graphic.getColor().b = t;
                }
            }
        } else if (!petrify) {
            if (graphic != null) {
                float t = 1f;
                if (Math.abs(graphic.getColor().r - t) > 0.001f || Math.abs(graphic.getColor().g - t) > 0.001f || Math.abs(graphic.getColor().b - t) > 0.001f) {
                    graphic.getColor().r = t;
                    graphic.getColor().g = t;
                    graphic.getColor().b = t;
                }
            }
            if (overridden) {
                entity.remove(com.github.MichalKC.manylands.component.PetrifyOverride.class);
            }
        }

        Enemy enemy = Enemy.MAPPER.get(entity);
        Body body = Physic.MAPPER.get(entity).getBody();
        ZoneShape roam = enemy.getRoamBounds();
        Vector2 enemyPos = body.getPosition();

        Vector2 colCenter = collisionCenter(body, enemy);
        enforceRoamBounds(body, colCenter, roam);
        colCenter = collisionCenter(body, enemy);
        enemyPos = body.getPosition();

        Entity playerEntity = findNearestPlayer(enemyPos);
        float distToPlayer = Float.MAX_VALUE;
        Vector2 playerPos = null;
        if (playerEntity != null) {
            playerPos = Physic.MAPPER.get(playerEntity).getBody().getPosition();
            distToPlayer = enemyPos.dst(playerPos);
        }

        if (playerPos != null && distToPlayer <= enemy.getAggroRadius()) {
            if (roam != null) {
                Vector2 playerCol = playerCollisionCenter(playerEntity);
                if (!roam.contains(playerCol)) {
                    patrol(entity, enemy, move, body, colCenter, deltaTime);
                    return;
                }
            }

            enemy.setPatrolling(false);
            faceTowards(entity, enemyPos, playerPos);

            if (distToPlayer <= enemy.getAttackRange()) {
                move.getDirection().setZero();
                Attack attack = Attack.MAPPER.get(entity);
                if (attack != null && attack.canAttack()) {
                    AttackType type = MathUtils.random() < 0.3f ? AttackType.SECONDARY : AttackType.PRIMARY;
                    attack.startAttack(type);
                }
                return;
            }

            TMP.set(playerPos).sub(enemyPos).nor();
            if (clampDirection(roam, colCenter, TMP)) {
                move.getDirection().set(TMP);
            } else {
                move.getDirection().setZero();
            }
            return;
        }

        patrol(entity, enemy, move, body, colCenter, deltaTime);
    }

    private Vector2 collisionCenter(Body body, Enemy enemy) {
        return TMP_COL.set(body.getPosition()).add(enemy.getCollisionOffset());
    }

    private Vector2 playerCollisionCenter(Entity playerEntity) {
        Body pBody = Physic.MAPPER.get(playerEntity).getBody();
        Vector2 pos = pBody.getPosition();
        if (pBody.getFixtureList().size > 0) {
            Shape shape = pBody.getFixtureList().first().getShape();
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

    private void enforceRoamBounds(Body body, Vector2 collisionCenter, ZoneShape roam) {
        if (roam == null) return;
        if (roam.contains(collisionCenter)) return;

        Rectangle bounds = roam.getBoundingBox();
        float clampedX = MathUtils.clamp(collisionCenter.x, bounds.x, bounds.x + bounds.width);
        float clampedY = MathUtils.clamp(collisionCenter.y, bounds.y, bounds.y + bounds.height);

        Vector2 bodyPos = body.getPosition();
        float offsetX = collisionCenter.x - bodyPos.x;
        float offsetY = collisionCenter.y - bodyPos.y;
        body.setTransform(clampedX - offsetX, clampedY - offsetY, body.getAngle());
        body.setLinearVelocity(0f, 0f);
    }

    private void patrol(Entity entity, Enemy enemy, Move move, Body body, Vector2 collisionCenter, float deltaTime) {
        ZoneShape roam = enemy.getRoamBounds();
        if (roam == null) {
            idleTick(enemy, move, deltaTime);
            return;
        }

        if (enemy.isPatrolling()) {
            if (isStuck(body, move)) {
                pickNewPatrolDirection(enemy);
            }

            enemy.setPatrolTimer(enemy.getPatrolTimer() - deltaTime);
            if (enemy.getPatrolTimer() <= 0f) {
                enemy.setPatrolling(false);
                enemy.setIdleTimer(MathUtils.random(IDLE_MIN_SEC, IDLE_MAX_SEC));
                move.getDirection().setZero();
                return;
            }

            TMP.set(enemy.getPatrolDir());
            if (clampDirection(roam, collisionCenter, TMP)) {
                move.getDirection().set(TMP);
                Facing facing = Facing.MAPPER.get(entity);
                if (facing != null) {
                    if (TMP.x > 0) facing.setDirection(Facing.FacingDirection.RIGHT);
                    else if (TMP.x < 0) facing.setDirection(Facing.FacingDirection.LEFT);
                }
            } else {
                pickNewPatrolDirection(enemy);
            }
        } else {
            idleTick(enemy, move, deltaTime);
        }
    }

    private boolean isStuck(Body body, Move move) {
        if (move.getDirection().isZero()) return false;
        Vector2 vel = body.getLinearVelocity();
        return vel.len2() < STUCK_VELOCITY_THRESHOLD * STUCK_VELOCITY_THRESHOLD;
    }

    private void pickNewPatrolDirection(Enemy enemy) {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        enemy.getPatrolDir().set(MathUtils.cos(angle), MathUtils.sin(angle)).nor();
        enemy.setPatrolTimer(MathUtils.random(PATROL_MIN_SEC, PATROL_MAX_SEC));
    }

    private void idleTick(Enemy enemy, Move move, float deltaTime) {
        move.getDirection().setZero();
        enemy.setIdleTimer(enemy.getIdleTimer() - deltaTime);
        if (enemy.getIdleTimer() <= 0f) {
            enemy.setPatrolling(true);
            pickNewPatrolDirection(enemy);
        }
    }

    private boolean clampDirection(ZoneShape roam, Vector2 pos, Vector2 dir) {
        if (roam == null) return !dir.isZero();

        Rectangle bounds = roam.getBoundingBox();
        if (dir.x < 0 && pos.x <= bounds.x) dir.x = 0f;
        if (dir.x > 0 && pos.x >= bounds.x + bounds.width) dir.x = 0f;
        if (dir.y < 0 && pos.y <= bounds.y) dir.y = 0f;
        if (dir.y > 0 && pos.y >= bounds.y + bounds.height) dir.y = 0f;

        return !dir.isZero();
    }

    private void faceTowards(Entity entity, Vector2 from, Vector2 to) {
        Facing facing = Facing.MAPPER.get(entity);
        if (facing == null) return;
        if (to.x < from.x) {
            facing.setDirection(Facing.FacingDirection.LEFT);
        } else {
            facing.setDirection(Facing.FacingDirection.RIGHT);
        }
    }

    private Entity findNearestPlayer(Vector2 from) {
        if (players.size() == 0) return null;

        Entity nearest = null;
        float nearestDist = Float.MAX_VALUE;
        for (int i = 0; i < players.size(); i++) {
            Entity p = players.get(i);
            if (Dead.MAPPER.get(p) != null) continue;
            com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(p);
            if (skills != null && skills.isSkill4Active()) {
                continue;
            }
            Vector2 pPos = Physic.MAPPER.get(p).getBody().getPosition();
            float dist = from.dst(pPos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }
}
