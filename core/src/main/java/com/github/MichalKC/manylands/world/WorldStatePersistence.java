package com.github.MichalKC.manylands.world;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.MichalKC.manylands.component.Animation2D;
import com.github.MichalKC.manylands.component.Animation2D.AnimationType;
import com.github.MichalKC.manylands.component.Coin;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Enemy;
import com.github.MichalKC.manylands.component.Npc;
import com.github.MichalKC.manylands.component.Facing;
import com.github.MichalKC.manylands.component.CoinDropper;
import com.github.MichalKC.manylands.component.Interactable;
import com.github.MichalKC.manylands.component.Item;
import com.github.MichalKC.manylands.component.Life;
import com.github.MichalKC.manylands.component.Physic;
import com.github.MichalKC.manylands.component.Storage;
import com.github.MichalKC.manylands.component.Tiled;
import com.github.MichalKC.manylands.component.Transform;

/**
 * Saves and restores interactable map objects (opened chests, activated levers, etc.) per map file.
 */
public final class WorldStatePersistence {

    private WorldStatePersistence() {
    }

    public static MapWorldState capture(Engine engine, MapWorldState mapWorldState) {
        MapWorldState world = new MapWorldState();
        if (mapWorldState != null) {
            world.getPickedUpItems().addAll(mapWorldState.getPickedUpItems());
        }
        ImmutableArray<Entity> entities = engine.getEntitiesFor(
            Family.all(Tiled.class, Interactable.class, Animation2D.class).exclude(Npc.class).get()
        );
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            Tiled tiled = Tiled.MAPPER.get(entity);
            MapObject mapObject = tiled.getMapObjectRef();
            String layerName = tiled.getLayerName();
            if (layerName == null) {
                continue;
            }
            String key = objectKey(layerName, mapObject);
            CoinDropper coinDropper = CoinDropper.MAPPER.get(entity);
            boolean coinsDropped = coinDropper != null && coinDropper.isDropped();
            InteractableObjectState state = snapshotInteractable(
                Interactable.MAPPER.get(entity),
                Animation2D.MAPPER.get(entity),
                coinsDropped
            );
            world.put(key, state);
        }

        captureEnemyDeaths(engine, world);
        captureEnemyLife(engine, world);
        captureEnemySnapshots(engine, world);
        captureNpcSnapshots(engine, world);
        captureDroppedCoins(engine, world);
        captureStorageSlots(engine, world);
        return world;
    }

    public static MapWorldState captureForResume(Engine engine, MapWorldState mapWorldState) {
        MapWorldState world = capture(engine, mapWorldState);
        ImmutableArray<Entity> deadNpcs = engine.getEntitiesFor(
            Family.all(Tiled.class, Npc.class, Dead.class, Transform.class, Facing.class).get()
        );
        for (int i = 0; i < deadNpcs.size(); i++) {
            Entity entity = deadNpcs.get(i);
            Tiled tiled = Tiled.MAPPER.get(entity);
            String layerName = tiled.getLayerName();
            if (layerName == null) continue;
            String key = objectKey(layerName, tiled.getMapObjectRef());
            Transform transform = Transform.MAPPER.get(entity);
            Facing facing = Facing.MAPPER.get(entity);
            world.putNpcDeadSnapshot(key, new NpcDeadSnapshot(
                transform.getPosition().x,
                transform.getPosition().y,
                facing.getDirection()
            ));
        }
        return world;
    }

    private static void captureStorageSlots(Engine engine, MapWorldState world) {
        ImmutableArray<Entity> storages = engine.getEntitiesFor(
            Family.all(Tiled.class, Storage.class).get()
        );
        for (int i = 0; i < storages.size(); i++) {
            Entity entity = storages.get(i);
            Tiled tiled = Tiled.MAPPER.get(entity);
            Storage storage = Storage.MAPPER.get(entity);
            if (tiled.getLayerName() == null) continue;
            String key = objectKey(tiled.getLayerName(), tiled.getMapObjectRef());
            world.putStorageSlots(key, storage.getSlots().clone());
        }
    }

    public static void applyNpcDeadResume(Engine engine, MapWorldState worldState) {
        if (worldState == null) return;
        ImmutableArray<Entity> npcs = engine.getEntitiesFor(
            Family.all(Tiled.class, Npc.class, Transform.class, Facing.class, Animation2D.class).get()
        );
        for (int i = 0; i < npcs.size(); i++) {
            Entity entity = npcs.get(i);
            Tiled tiled = Tiled.MAPPER.get(entity);
            String layerName = tiled.getLayerName();
            if (layerName == null) continue;
            String key = objectKey(layerName, tiled.getMapObjectRef());
            NpcDeadSnapshot snap = worldState.getNpcDeadSnapshot(key);
            if (snap == null) continue;
            Transform transform = Transform.MAPPER.get(entity);
            transform.getPosition().set(snap.getX(), snap.getY());
            Physic physic = Physic.MAPPER.get(entity);
            if (physic != null) {
                physic.getBody().setTransform(snap.getX(), snap.getY(), 0f);
                physic.getPrevPosition().set(snap.getX(), snap.getY());
            }
            Facing facing = Facing.MAPPER.get(entity);
            if (facing != null && snap.getFacing() != null) {
                facing.setDirection(snap.getFacing());
            }
            if (!Dead.MAPPER.has(entity)) entity.add(new Dead());
            Animation2D anim = Animation2D.MAPPER.get(entity);
            if (anim != null) {
                anim.setType(Animation2D.AnimationType.DEAD);
                anim.setPlayMode(PlayMode.NORMAL);
            }
            if (!com.github.MichalKC.manylands.component.FreezeOnDeadPose.MAPPER.has(entity)) {
                entity.add(new com.github.MichalKC.manylands.component.FreezeOnDeadPose());
            }
        }
    }

    private static void captureDroppedCoins(Engine engine, MapWorldState world) {
        ImmutableArray<Entity> coins = engine.getEntitiesFor(
            Family.all(Coin.class, Transform.class).get()
        );
        Array<DroppedCoinState> coinStates = new Array<>();
        for (int i = 0; i < coins.size(); i++) {
            Entity entity = coins.get(i);
            Coin coin = Coin.MAPPER.get(entity);
            Vector2 pos = Transform.MAPPER.get(entity).getPosition();
            coinStates.add(new DroppedCoinState(pos.x, pos.y, coin.getValue()));
        }
        world.setDroppedCoins(coinStates);
    }

    private static void captureEnemySnapshots(Engine engine, MapWorldState world) {
        ImmutableArray<Entity> enemies = engine.getEntitiesFor(
            Family.all(Tiled.class, Enemy.class, Transform.class, Facing.class).exclude(Dead.class).get()
        );
        for (int i = 0; i < enemies.size(); i++) {
            Entity entity = enemies.get(i);
            Tiled tiled = Tiled.MAPPER.get(entity);
            String layerName = tiled.getLayerName();
            if (layerName == null) continue;

            String key = objectKey(layerName, tiled.getMapObjectRef());
            Transform transform = Transform.MAPPER.get(entity);
            Facing facing = Facing.MAPPER.get(entity);
            world.putEnemySnapshot(key, new EnemySnapshot(
                transform.getPosition().x,
                transform.getPosition().y,
                facing.getDirection()
            ));
        }
    }

    public static void restoreEnemySnapshot(Entity entity, String layerName, MapObject mapObject, MapWorldState worldState) {
        if (worldState == null) return;
        String key = objectKey(layerName, mapObject);
        EnemySnapshot snapshot = worldState.getEnemySnapshot(key);
        if (snapshot == null) return;

        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null) {
            transform.getPosition().set(snapshot.getX(), snapshot.getY());
        }
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null) {
            physic.getBody().setTransform(snapshot.getX(), snapshot.getY(), 0f);
            physic.getPrevPosition().set(snapshot.getX(), snapshot.getY());
        }
        Facing facing = Facing.MAPPER.get(entity);
        if (facing != null && snapshot.getFacing() != null) {
            facing.setDirection(snapshot.getFacing());
        }
    }

    private static void captureNpcSnapshots(Engine engine, MapWorldState world) {
        ImmutableArray<Entity> npcs = engine.getEntitiesFor(
            Family.all(Tiled.class, Npc.class, Transform.class, Facing.class).exclude(Dead.class).get()
        );
        for (int i = 0; i < npcs.size(); i++) {
            Entity entity = npcs.get(i);
            Tiled tiled = Tiled.MAPPER.get(entity);
            String layerName = tiled.getLayerName();
            if (layerName == null) continue;

            String key = objectKey(layerName, tiled.getMapObjectRef());
            Transform transform = Transform.MAPPER.get(entity);
            Facing facing = Facing.MAPPER.get(entity);
            world.putNpcSnapshot(key, new NpcSnapshot(
                transform.getPosition().x,
                transform.getPosition().y,
                facing.getDirection()
            ));
        }
    }

    public static void restoreNpcSnapshot(Entity entity, String layerName, MapObject mapObject, MapWorldState worldState) {
        if (worldState == null) return;
        String key = objectKey(layerName, mapObject);
        NpcSnapshot snapshot = worldState.getNpcSnapshot(key);
        if (snapshot == null) return;

        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null) {
            transform.getPosition().set(snapshot.getX(), snapshot.getY());
        }
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null) {
            physic.getBody().setTransform(snapshot.getX(), snapshot.getY(), 0f);
            physic.getPrevPosition().set(snapshot.getX(), snapshot.getY());
        }
        Facing facing = Facing.MAPPER.get(entity);
        if (facing != null && snapshot.getFacing() != null) {
            facing.setDirection(snapshot.getFacing());
        }
    }

    private static void captureEnemyDeaths(Engine engine, MapWorldState world) {
        ImmutableArray<Entity> enemies = engine.getEntitiesFor(
            Family.all(Tiled.class, Enemy.class, Dead.class).get()
        );
        for (int i = 0; i < enemies.size(); i++) {
            Entity entity = enemies.get(i);
            Tiled tiled = Tiled.MAPPER.get(entity);
            Enemy enemy = Enemy.MAPPER.get(entity);
            String layerName = tiled.getLayerName();
            if (layerName == null) continue;

            String key = objectKey(layerName, tiled.getMapObjectRef());
            EnemyDeathRecord record = new EnemyDeathRecord(
                TimeUtils.millis(),
                enemy.getRespawnDelaySec(),
                true
            );
            world.putEnemyDeath(key, record);
        }
    }

    public static void tryRestore(
        Entity entity,
        String layerName,
        MapObject mapObject,
        MapWorldState worldState
    ) {
        if (worldState == null) {
            return;
        }
        if (Npc.MAPPER.get(entity) != null) {
            return;
        }
        InteractableObjectState saved = worldState.get(objectKey(layerName, mapObject));
        if (saved == null) {
            return;
        }
        Interactable interactable = Interactable.MAPPER.get(entity);
        Animation2D anim = Animation2D.MAPPER.get(entity);
        if (interactable == null || anim == null) {
            return;
        }
        interactable.setActivated(saved.isActivated());
        interactable.setActivatedStage(saved.isActivated() ? saved.getStage() : 0);
        anim.setStage(saved.getStage());
        anim.setType(saved.getAnimationType());
        anim.setPlayMode(PlayMode.LOOP);

        CoinDropper coinDropper = CoinDropper.MAPPER.get(entity);
        if (coinDropper != null && saved.isCoinsDropped()) {
            coinDropper.setDropped(true);
        }

        Storage storage = Storage.MAPPER.get(entity);
        if (storage != null) {
            String key = objectKey(layerName, mapObject);
            Item[] savedSlots = worldState.getStorageSlots(key);
            if (savedSlots != null) {
                System.arraycopy(savedSlots, 0, storage.getSlots(), 0, Math.min(storage.getSlots().length, savedSlots.length));
            }
        }
    }

    private static void captureEnemyLife(Engine engine, MapWorldState world) {
        ImmutableArray<Entity> enemies = engine.getEntitiesFor(
            Family.all(Tiled.class, Enemy.class, Life.class).exclude(Dead.class).get()
        );
        for (int i = 0; i < enemies.size(); i++) {
            Entity entity = enemies.get(i);
            Life life = Life.MAPPER.get(entity);
            if (life.getLife() >= life.getMaxLife()) continue;

            Tiled tiled = Tiled.MAPPER.get(entity);
            String layerName = tiled.getLayerName();
            if (layerName == null) continue;

            String key = objectKey(layerName, tiled.getMapObjectRef());
            world.putEnemyLife(key, life.getLife());
        }
    }

    public static void restoreEnemyLife(Entity entity, String layerName, MapObject mapObject, MapWorldState worldState) {
        if (worldState == null) return;
        String key = objectKey(layerName, mapObject);
        Float savedLife = worldState.getEnemyLife(key);
        if (savedLife == null) return;
        Life life = Life.MAPPER.get(entity);
        if (life == null) return;
        life.restore(life.getMaxLife(), savedLife);
    }

    public static boolean shouldSkipEnemy(String layerName, MapObject mapObject, MapWorldState worldState) {
        if (worldState == null) {
            return false;
        }
        String key = objectKey(layerName, mapObject);
        EnemyDeathRecord record = worldState.getEnemyDeath(key);
        if (record == null) {
            return false;
        }
        if (record.canRespawn(TimeUtils.millis())) {
            worldState.removeEnemyDeath(key);
            return false;
        }
        return true;
    }

    public static String objectKey(String layerName, MapObject mapObject) {
        String suffix = objectSuffix(mapObject);
        return layerName + "|" + suffix;
    }

    private static String objectSuffix(MapObject mapObject) {
        String name = mapObject.getName();
        if (name != null && !name.isBlank()) {
            return "n:" + name.trim();
        }
        Integer persistId = mapObject.getProperties().get("id", null, Integer.class);
        if (persistId != null && persistId >= 0) {
            return "id:" + persistId;
        }
        if (mapObject instanceof TiledMapTileMapObject tileObj) {
            return "pos:" + Math.round(tileObj.getX()) + "," + Math.round(tileObj.getY());
        }
        return "obj:" + mapObject.hashCode();
    }

    private static InteractableObjectState snapshotInteractable(Interactable interactable, Animation2D anim, boolean coinsDropped) {
        boolean activated = interactable.isActivated();
        AnimationType type = anim.getType();
        int stage = Math.max(1, anim.getStage());
        if (type == AnimationType.TO_ACTIVATED) {
            activated = true;
            type = AnimationType.ACTIVATED;
        } else if (type == AnimationType.TO_IDLE) {
            activated = false;
            type = AnimationType.IDLE;
            stage = 1;
        }
        if (activated && type == AnimationType.IDLE) {
            type = AnimationType.ACTIVATED;
            stage = Math.max(1, interactable.getActivatedStage());
        } else if (!activated && type == AnimationType.ACTIVATED) {
            type = AnimationType.IDLE;
            stage = 1;
        }
        if (!activated) {
            stage = 1;
        }
        return new InteractableObjectState(activated, type, stage, coinsDropped);
    }
}
