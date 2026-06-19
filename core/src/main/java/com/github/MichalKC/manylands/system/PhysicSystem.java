package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.github.MichalKC.manylands.component.Coin;
import com.github.MichalKC.manylands.component.DamageZone;
import com.github.MichalKC.manylands.component.Enemy;
import com.github.MichalKC.manylands.component.Item;
import com.github.MichalKC.manylands.component.HealZone;
import com.github.MichalKC.manylands.component.Physic;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.component.Transform;
import com.github.MichalKC.manylands.component.CombatTrigger;
import com.github.MichalKC.manylands.component.Trigger;

public class PhysicSystem extends IteratingSystem implements EntityListener, ContactListener {

    private final World world;
    private final float interval;
    private float accumulator;

    public PhysicSystem(World world, float interval) {
        super(Family.all(Physic.class, Transform.class).get());
        this.world = world;
        this.interval = interval;
        this.accumulator = 0f;
        world.setContactListener(this);
    }

    private void breakInvisibilityOnPlayerEnemyContact(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        boolean aIsPlayerSolid = Player.MAPPER.get(entityA) != null && !fixtureA.isSensor();
        boolean bIsPlayerSolid = Player.MAPPER.get(entityB) != null && !fixtureB.isSensor();
        if (aIsPlayerSolid && Enemy.MAPPER.get(entityB) != null) {
            com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(entityA);
            if (skills != null && skills.isSkill4Active()) skills.cancelSkill4Early();
        } else if (bIsPlayerSolid && Enemy.MAPPER.get(entityA) != null) {
            com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(entityB);
            if (skills != null && skills.isSkill4Active()) skills.cancelSkill4Early();
        }
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        engine.removeEntityListener(this);
    }

    @Override
    public void entityAdded(Entity entity) {
    }

    @Override
    public void entityRemoved(Entity entity) {
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null) {
            this.world.destroyBody(physic.getBody());
        }
    }

    @Override
    public void update(float deltaTime) {
        this.accumulator += deltaTime;

        while(this.accumulator >= this.interval) {
            this.accumulator -= this.interval;
            super.update(deltaTime);
            this.world.step(interval, 6, 2);
        }
        world.clearForces();

        float alpha = this.accumulator / this.interval;
        for (int i= 0; i < getEntities().size(); ++i) {
            this.interpolateEntity(getEntities().get(i), alpha);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Physic physic = Physic.MAPPER.get(entity);
        physic.getPrevPosition().set(physic.getBody().getPosition());
    }

    private void interpolateEntity(Entity entity, float alpha) {
        Transform transform = Transform.MAPPER.get(entity);
        Physic physic = Physic.MAPPER.get(entity);

        transform.getPosition().set(
            MathUtils.lerp(physic.getPrevPosition().x, physic.getBody().getPosition().x, alpha),
            MathUtils.lerp(physic.getPrevPosition().y, physic.getBody().getPosition().y, alpha)
        );
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Body bodyA = fixtureA.getBody();
        Object userDataA = bodyA.getUserData();
        Fixture fixtureB = contact.getFixtureB();
        Body bodyB = fixtureB.getBody();
        Object userDataB = bodyB.getUserData();

        if (!(userDataA instanceof Entity entityA) || !(userDataB instanceof Entity entityB)) {
            return;
        }

        playerTriggerContact(entityA, fixtureA, entityB,fixtureB);
        playerCoinContact(entityA, fixtureA, entityB, fixtureB);
        playerItemContact(entityA, fixtureA, entityB, fixtureB);
        healZoneContactBegin(entityA, fixtureA, entityB, fixtureB);
        damageZoneContactBegin(entityA, fixtureA, entityB, fixtureB);
        combatTriggerContactBegin(entityA, fixtureA, entityB, fixtureB);
        breakInvisibilityOnPlayerEnemyContact(entityA, fixtureA, entityB, fixtureB);
    }

    private void playerCoinContact(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        if ("collect".equals(fixtureA.getUserData())) {
            Coin coin = Coin.MAPPER.get(entityB);
            if (coin != null) {
                coin.setMarkedForPickup(true);
                return;
            }
        }
        if ("collect".equals(fixtureB.getUserData())) {
            Coin coin = Coin.MAPPER.get(entityA);
            if (coin != null) {
                coin.setMarkedForPickup(true);
            }
        }
    }

    private void playerItemContact(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        if ("collect".equals(fixtureA.getUserData())) {
            Item item = Item.MAPPER.get(entityB);
            if (item != null) {
                item.setMarkedForPickup(true);
                return;
            }
        }
        if ("collect".equals(fixtureB.getUserData())) {
            Item item = Item.MAPPER.get(entityA);
            if (item != null) {
                item.setMarkedForPickup(true);
            }
        }
    }

    private void playerTriggerContact(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        Trigger trigger = Trigger.MAPPER.get(entityA);
        boolean isPlayer = Player.MAPPER.get(entityB) != null && !fixtureB.isSensor();
        if (trigger != null && isPlayer) {
            trigger.setTriggeringEntity(entityB);
            return;
        }

        trigger = Trigger.MAPPER.get(entityB);
        isPlayer = Player.MAPPER.get(entityA) != null && !fixtureA.isSensor();
        if (trigger != null && isPlayer) {
            trigger.setTriggeringEntity(entityA);
        }
    }

    private void healZoneContactBegin(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        HealZone healZone = HealZone.MAPPER.get(entityA);
        boolean isPlayer = Player.MAPPER.get(entityB) != null && !fixtureB.isSensor();
        if (healZone != null && isPlayer) {
            healZone.setOverlappingPlayer(entityB);
            return;
        }

        healZone = HealZone.MAPPER.get(entityB);
        isPlayer = Player.MAPPER.get(entityA) != null && !fixtureA.isSensor();
        if (healZone != null && isPlayer) {
            healZone.setOverlappingPlayer(entityA);
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Body bodyA = fixtureA.getBody();
        Object userDataA = bodyA.getUserData();
        Fixture fixtureB = contact.getFixtureB();
        Body bodyB = fixtureB.getBody();
        Object userDataB = bodyB.getUserData();

        if (!(userDataA instanceof Entity entityA) || !(userDataB instanceof Entity entityB)) {
            return;
        }

        healZoneContactEnd(entityA, fixtureA, entityB, fixtureB);
        damageZoneContactEnd(entityA, fixtureA, entityB, fixtureB);
        combatTriggerContactEnd(entityA, fixtureA, entityB, fixtureB);
    }

    private void healZoneContactEnd(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        HealZone healZone = HealZone.MAPPER.get(entityA);
        boolean isPlayer = Player.MAPPER.get(entityB) != null && !fixtureB.isSensor();
        if (healZone != null && isPlayer) {
            Entity currentPlayer = healZone.getOverlappingPlayer();
            if (currentPlayer == entityB) {
                healZone.setOverlappingPlayer(null);
            }
            return;
        }

        healZone = HealZone.MAPPER.get(entityB);
        isPlayer = Player.MAPPER.get(entityA) != null && !fixtureA.isSensor();
        if (healZone != null && isPlayer) {
            Entity currentPlayer = healZone.getOverlappingPlayer();
            if (currentPlayer == entityA) {
                healZone.setOverlappingPlayer(null);
            }
        }
    }

    private void damageZoneContactBegin(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        DamageZone zone = DamageZone.MAPPER.get(entityA);
        if (zone != null) {
            if (Player.MAPPER.get(entityB) != null && !fixtureB.isSensor()) {
                zone.setOverlappingPlayer(entityB);
                return;
            }
            if (zone.isCanDamageEnemies() && Enemy.MAPPER.get(entityB) != null && !fixtureB.isSensor()) {
                zone.addOverlappingEnemy(entityB);
                return;
            }
        }
        zone = DamageZone.MAPPER.get(entityB);
        if (zone != null) {
            if (Player.MAPPER.get(entityA) != null && !fixtureA.isSensor()) {
                zone.setOverlappingPlayer(entityA);
                return;
            }
            if (zone.isCanDamageEnemies() && Enemy.MAPPER.get(entityA) != null && !fixtureA.isSensor()) {
                zone.addOverlappingEnemy(entityA);
            }
        }
    }

    private void damageZoneContactEnd(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        DamageZone zone = DamageZone.MAPPER.get(entityA);
        if (zone != null) {
            if (Player.MAPPER.get(entityB) != null && entityB == zone.getOverlappingPlayer()) {
                zone.setOverlappingPlayer(null);
                return;
            }
            if (zone.isCanDamageEnemies() && Enemy.MAPPER.get(entityB) != null) {
                zone.removeOverlappingEnemy(entityB);
                return;
            }
        }
        zone = DamageZone.MAPPER.get(entityB);
        if (zone != null) {
            if (Player.MAPPER.get(entityA) != null && entityA == zone.getOverlappingPlayer()) {
                zone.setOverlappingPlayer(null);
                return;
            }
            if (zone.isCanDamageEnemies() && Enemy.MAPPER.get(entityA) != null) {
                zone.removeOverlappingEnemy(entityA);
            }
        }
    }

    private void combatTriggerContactBegin(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        CombatTrigger ct = CombatTrigger.MAPPER.get(entityA);
        boolean isPlayer = Player.MAPPER.get(entityB) != null && !fixtureB.isSensor();
        if (ct != null && isPlayer) {
            ct.setPlayerInside(true);
            ct.setOverlappingPlayer(entityB);
            return;
        }
        ct = CombatTrigger.MAPPER.get(entityB);
        isPlayer = Player.MAPPER.get(entityA) != null && !fixtureA.isSensor();
        if (ct != null && isPlayer) {
            ct.setPlayerInside(true);
            ct.setOverlappingPlayer(entityA);
        }
    }

    private void combatTriggerContactEnd(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        CombatTrigger ct = CombatTrigger.MAPPER.get(entityA);
        boolean isPlayer = Player.MAPPER.get(entityB) != null && !fixtureB.isSensor();
        if (ct != null && isPlayer) {
            ct.setPlayerInside(false);
            ct.setOverlappingPlayer(null);
            return;
        }
        ct = CombatTrigger.MAPPER.get(entityB);
        isPlayer = Player.MAPPER.get(entityA) != null && !fixtureA.isSensor();
        if (ct != null && isPlayer) {
            ct.setPlayerInside(false);
            ct.setOverlappingPlayer(null);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
