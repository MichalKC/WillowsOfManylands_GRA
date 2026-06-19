package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.PlayerSessionState;
import com.github.MichalKC.manylands.component.Gated;
import com.github.MichalKC.manylands.component.MapPortal;
import com.github.MichalKC.manylands.player.PlayerStatePersistence;
import com.github.MichalKC.manylands.ui.model.GameViewModel;
import com.github.MichalKC.manylands.world.MapWorldState;
import com.github.MichalKC.manylands.world.WorldStatePersistence;
import com.github.MichalKC.manylands.component.Move;
import com.github.MichalKC.manylands.component.Physic;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.component.Trigger;
import com.github.MichalKC.manylands.tiled.PlayerSpawnHelper;
import com.github.MichalKC.manylands.tiled.TiledService;
import com.github.MichalKC.manylands.tiled.TiledSpawnLocator;

/**
 * Loads another map when the player overlaps a {@link MapPortal} trigger.
 * Uses a short cooldown so the player does not instantly bounce back through a paired portal.
 */
public class MapTransitionSystem extends IteratingSystem {
    private static final float DEFAULT_COOLDOWN_SEC = 0.65f;

    private final Engine engine;
    private final TiledService tiledService;
    private final CameraSystem cameraSystem;
    private final GameViewModel viewModel;
    private final GdxGame game;
    private final SessionRestoreSystem sessionRestoreSystem;
    private final Vector2 tmp = new Vector2();
    private final Vector2 spawnPixels = new Vector2();
    private float portalCooldownRemaining;

    public MapTransitionSystem(
        Engine engine,
        TiledService tiledService,
        CameraSystem cameraSystem,
        GameViewModel viewModel,
        GdxGame game,
        SessionRestoreSystem sessionRestoreSystem
    ) {
        super(Family.all(Trigger.class, MapPortal.class).get());
        this.engine = engine;
        this.tiledService = tiledService;
        this.cameraSystem = cameraSystem;
        this.viewModel = viewModel;
        this.game = game;
        this.sessionRestoreSystem = sessionRestoreSystem;
        this.portalCooldownRemaining = 0f;
    }

    @Override
    public void update(float deltaTime) {
        if (portalCooldownRemaining > 0f) {
            portalCooldownRemaining -= deltaTime;
            clearSpuriousPortalTriggers();
            return;
        }
        super.update(deltaTime);
    }

    /** While cooling down, drop trigger overlap flags so they do not queue stale transitions. */
    private void clearSpuriousPortalTriggers() {
        for (Entity entity : getEntities()) {
            Trigger t = Trigger.MAPPER.get(entity);
            if (t != null) {
                t.setTriggeringEntity(null);
            }
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Trigger trigger = Trigger.MAPPER.get(entity);
        if (trigger.getTriggeringEntity() == null) {
            return;
        }

        MapPortal portal = MapPortal.MAPPER.get(entity);
        Entity player = trigger.getTriggeringEntity();
        if (Player.MAPPER.get(player) == null) {
            trigger.setTriggeringEntity(null);
            return;
        }

        // Universal gating: portal does nothing until required interactables are activated.
        Gated gated = Gated.MAPPER.get(entity);
        if (gated != null && !GateEvaluator.isOpen(engine, gated)) {
            trigger.setTriggeringEntity(null);
            return;
        }

        trigger.setTriggeringEntity(null);

        String file = portal.getTargetMapFile();
        TiledService.assertSafeMapFileName(file);

        if (!portal.hasSpawnResolution()) {
            throw new GdxRuntimeException(
                "Map portal to " + file + " must define spawnAt (named object on target map) and/or spawnX + spawnY"
            );
        }

        String leavingMapKey = tiledService.getCurrentMapKey();
        if (leavingMapKey != null) {
            MapWorldState prev = game.getMapWorldState(leavingMapKey);
            game.putMapWorldState(leavingMapKey, WorldStatePersistence.capture(engine, prev));
        }

        PlayerSessionState session = PlayerStatePersistence.capture(engine, viewModel);
        if (session != null) {
            game.setPlayerSessionState(session);
        }

        TiledMap next = tiledService.loadMapFromFile(file);
        resolveSpawnPixels(next, portal);
        tiledService.setPendingPlayerSpawnPixels(spawnPixels.x, spawnPixels.y);
        tiledService.setPendingPlayerSession(session);

        engine.removeAllEntities();
        tiledService.setMap(next);
        tiledService.clearPendingPlayerSpawnPixels();

        restoreDroppedCoins();

        applySpawnFromPixels(spawnPixels.x, spawnPixels.y);
        if (session != null) {
            sessionRestoreSystem.schedule(session.withSpawnPosition(tmp.x, tmp.y));
        }
        cameraSystem.setMap(next);

        resetPlayerMotion();

        String nextMapKey = tiledService.getCurrentMapKey();
        if (nextMapKey != null) {
            game.saveCheckpoint(nextMapKey, session != null ? session.withSpawnPosition(tmp.x, tmp.y) : null);
        }

        portalCooldownRemaining = DEFAULT_COOLDOWN_SEC;
    }

    private void resolveSpawnPixels(TiledMap targetMap, MapPortal portal) {
        if (portal.hasCustomSpawn()) {
            spawnPixels.set(portal.getSpawnXPixels(), portal.getSpawnYPixels());
            return;
        }
        if (portal.hasNamedSpawn()) {
            if (!TiledSpawnLocator.findSpawnCenterPixels(targetMap, portal.getSpawnAtObjectName(), spawnPixels)) {
                throw new GdxRuntimeException(
                    "spawnAt \"" + portal.getSpawnAtObjectName() + "\" not found on target map " + portal.getTargetMapFile()
                );
            }
            return;
        }
        throw new GdxRuntimeException("Map portal has no spawn resolution for " + portal.getTargetMapFile());
    }

    private void applySpawnFromPixels(float spawnXPixels, float spawnYPixels) {
        tmp.set(spawnXPixels, spawnYPixels).scl(GdxGame.UNIT_SCALE);
        PlayerSpawnHelper.applyPlayerWorldPosition(engine, tmp.x, tmp.y);
    }

    private void restoreDroppedCoins() {
        MapWorldState worldState = game.getMapWorldState(tiledService.getCurrentMapKey());
        if (worldState != null) {
            CoinSystem coinSystem = engine.getSystem(CoinSystem.class);
            if (coinSystem != null) {
                coinSystem.restoreCoins(worldState.getDroppedCoins());
            }
        }
    }

    private void resetPlayerMotion() {
        for (Entity e : engine.getEntitiesFor(Family.all(Player.class, Physic.class).get())) {
            Physic physic = Physic.MAPPER.get(e);
            Body body = physic.getBody();
            body.setLinearVelocity(0f, 0f);
            body.setAngularVelocity(0f);
            Move move = Move.MAPPER.get(e);
            if (move != null) {
                move.getDirection().setZero();
            }
            break;
        }
    }
}
