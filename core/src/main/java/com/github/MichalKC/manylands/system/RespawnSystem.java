package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.github.MichalKC.manylands.ai.AnimationState;
import com.github.MichalKC.manylands.component.*;
import com.github.MichalKC.manylands.component.Enemy;
import com.github.MichalKC.manylands.component.Transform;
import com.github.MichalKC.manylands.tiled.TiledPhysics;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

public class RespawnSystem extends IteratingSystem {
    private final World world;
    private final GameViewModel viewModel;
    private boolean paused;

    public RespawnSystem(World world, GameViewModel viewModel) {
        super(Family.all(Dead.class, Respawn.class, Transform.class, Tiled.class).get());
        this.world = world;
        this.viewModel = viewModel;
        this.paused = false;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (paused) {
            return;
        }

        if (Enemy.MAPPER.get(entity) != null) {
            return;
        }

        Respawn respawn = Respawn.MAPPER.get(entity);
        if (!respawn.isActive()) {
            respawn.activate();
        }

        respawn.decTimer(deltaTime);
        if (!respawn.isReady()) {
            return;
        }

        Transform transform = Transform.MAPPER.get(entity);
        transform.getPosition().set(respawn.getSpawnPosition());

        Life life = Life.MAPPER.get(entity);
        if (life != null) {
            life.addLife(life.getMaxLife());
            if (Player.MAPPER.get(entity) != null) {
                viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
            }
        }

        DamageCooldown cooldown = DamageCooldown.MAPPER.get(entity);
        if (cooldown != null) {
            entity.remove(DamageCooldown.class);
        }

        Attack attack = Attack.MAPPER.get(entity);
        if (attack != null) {
            attack.resetAttackTimer();
        }

        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            move.getDirection().setZero();
            move.setRooted(false);
        }

        addEntityPhysic(entity);

        // Remove Dead component BEFORE changing FSM state to IDLE
        // Otherwise IDLE.update() will see Dead component and switch back to DEAD
        entity.remove(Dead.class);

        Fsm fsm = Fsm.MAPPER.get(entity);
        if (fsm != null) {
            DefaultStateMachine<Entity, AnimationState> animationFsm = fsm.getAnimationFsm();
            animationFsm.changeState(AnimationState.IDLE);
        }

        respawn.deactivate();
    }

    private void addEntityPhysic(Entity entity) {
        Tiled tiled = Tiled.MAPPER.get(entity);
        if (!(tiled.getMapObjectRef() instanceof TiledMapTileMapObject tileMapObject)) {
            return;
        }
        TiledMapTile tile = tileMapObject.getTile();
        Transform transform = Transform.MAPPER.get(entity);
        BodyDef.BodyType bodyType = getObjectBodyType(tile);
        Body body = createBody(tile.getObjects(), transform.getPosition(), transform.getScaling(), bodyType, entity);
        entity.add(new Physic(body, new Vector2(body.getPosition())));
    }

    private Body createBody(MapObjects mapObjects,
                            Vector2 position,
                            Vector2 scaling,
                            BodyDef.BodyType bodyType,
                            Entity userData) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);
        body.setUserData(userData);
        for (MapObject object : mapObjects) {
            FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(object, scaling, Vector2.Zero);
            Fixture fixture = body.createFixture(fixtureDef);
            boolean collect = object.getProperties().get("collect", false, Boolean.class);
            fixture.setUserData(collect ? "collect" : object.getName());
            fixtureDef.shape.dispose();
        }
        return body;
    }

    private BodyDef.BodyType getObjectBodyType(TiledMapTile tile) {
        String classType = tile.getProperties().get("type", "", String.class);
        if ("StaticProp".equals(classType)) {
            return BodyDef.BodyType.StaticBody;
        }

        String bodyTypeStr = tile.getProperties().get("bodyType", "DynamicBody", String.class);
        return BodyDef.BodyType.valueOf(bodyTypeStr);
    }
}
