package com.github.MichalKC.manylands.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.PlayerSessionState;
import com.github.MichalKC.manylands.asset.AssetService;
import com.github.MichalKC.manylands.player.PlayerStatePersistence;
import com.github.MichalKC.manylands.world.MapWorldState;
import com.github.MichalKC.manylands.world.WorldStatePersistence;
import com.github.MichalKC.manylands.asset.AtlasAsset;
import com.github.MichalKC.manylands.asset.SoundAsset;
import com.github.MichalKC.manylands.component.*;
import com.github.MichalKC.manylands.component.Animation2D.AnimationType;
import com.github.MichalKC.manylands.component.Transform;

public class TiledAshleyConfigurator {
    private static final Vector2 DEFAULT_PHYSIC_SCALING = new Vector2(1f, 1f);

    private final Engine engine;
    private final AssetService assetService;
    private final TiledService tiledService;
    private final GdxGame game;
    private final World physicWorld;
    private final Vector2 tmpVec2;
    private final MapObjects tmpMapObjects;
    private final List<AbstractMap.SimpleEntry<String, Item>> pendingStorageItems = new ArrayList<>();
    private final List<Entity> currentLoadStorageEntities = new ArrayList<>();
    private final java.util.Map<String, java.util.List<Item>> shopCatalog = new java.util.HashMap<>();

    public TiledAshleyConfigurator(
        Engine engine,
        AssetService assetService,
        TiledService tiledService,
        GdxGame game,
        World physicWorld
    ) {
        this.engine = engine;
        this.assetService = assetService;
        this.tiledService = tiledService;
        this.game = game;
        this.physicWorld = physicWorld;
        this.tmpVec2 = new Vector2();
        this.tmpMapObjects = new MapObjects();
    }

    public void onLoadTile(TiledMapTile tiledMapTile, float x, float y) {
        createBody(
            tiledMapTile.getObjects(),
            new Vector2(x, y),
            DEFAULT_PHYSIC_SCALING,
            BodyDef.BodyType.StaticBody,
            Vector2.Zero,
            "environment"
        );
    }

    private Body createBody(MapObjects mapObjects,
                            Vector2 position,
                            Vector2 scaling,
                            BodyDef.BodyType bodyType,
                            Vector2 relativeTo,
                            Object userData) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        Body body = physicWorld.createBody(bodyDef);
        body.setUserData(userData);
        for (MapObject object : mapObjects) {
            FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(object, scaling, relativeTo);
            Fixture fixture = body.createFixture(fixtureDef);
            boolean collect = object.getProperties().get("collect", false, Boolean.class);
            fixture.setUserData(collect ? "collect" : object.getName());
            fixtureDef.shape.dispose();
        }
        return body;
    }

    public void onLoadObject(String layerName, TiledMapTileMapObject tileMapObject) {
        TiledMapTile tile = tileMapObject.getTile();

        boolean isEnemy = tile.getProperties().get("enemy", false, Boolean.class)
            || tileMapObject.getProperties().get("enemy", false, Boolean.class);

        if (isEnemy) {
            MapWorldState worldState = game.getMapWorldState(tiledService.getCurrentMapKey());
            if (WorldStatePersistence.shouldSkipEnemy(layerName, tileMapObject, worldState)) {
                return;
            }
        }

        String type = tileMapObject.getProperties().get("type", String.class);
        if (type == null) {
            type = tile.getProperties().get("type", String.class);
        }
        boolean isItem = "item".equalsIgnoreCase(type)
            || "item".equalsIgnoreCase(tile.getProperties().get("class", String.class))
            || "item".equalsIgnoreCase(tileMapObject.getProperties().get("class", String.class))
            || tile.getProperties().get("item", false, Boolean.class)
            || tileMapObject.getProperties().get("item", false, Boolean.class);

        if (isItem) {
            // Generate itemId for both pickup check and storage placement
            String itemId = tileMapObject.getProperties().get("itemId", tile.getProperties().get("itemId", tileMapObject.getName(), String.class), String.class);
            if (itemId == null || itemId.isBlank()) {
                Object idProp = tileMapObject.getProperties().get("id");
                if (idProp != null) {
                    itemId = "item_" + idProp.toString();
                } else {
                    itemId = "item_pos_" + Math.round(tileMapObject.getX()) + "_" + Math.round(tileMapObject.getY());
                }
            }

            MapWorldState worldState = game.getMapWorldState(tiledService.getCurrentMapKey());
            if (worldState != null && worldState.isItemPickedUp(itemId)) {
                return;
            }

            // Check if item should be placed inside a storage object
            String insideStorageName = tileMapObject.getProperties().get("inside", tile.getProperties().get("inside", String.class), String.class);
            if (insideStorageName != null) {
                insideStorageName = insideStorageName.trim();
            }
            if (insideStorageName != null && !insideStorageName.isBlank()) {
                // Create item and add to storage instead of creating entity on map
                String itemName = tileMapObject.getProperties().get("itemName", tile.getProperties().get("itemName", tileMapObject.getName(), String.class), String.class);
                if (itemName == null || itemName.isBlank()) {
                    itemName = "Unknown Item";
                }

                String texturePath = tileMapObject.getProperties().get("itemIcon", String.class);
                if (texturePath == null) texturePath = tile.getProperties().get("itemIcon", String.class);
                if (texturePath == null) texturePath = tileMapObject.getProperties().get("icon", String.class);
                if (texturePath == null) texturePath = tile.getProperties().get("icon", String.class);
                if (texturePath == null) texturePath = tileMapObject.getProperties().get("texture", String.class);
                if (texturePath == null) texturePath = tile.getProperties().get("texture", String.class);
                if (texturePath == null || texturePath.isBlank()) {
                    texturePath = "ui/inventory/iconsitemsbag.png";
                }

                if (!texturePath.startsWith("ui/")) {
                    texturePath = "ui/inventory/" + texturePath;
                }

                int itemValue = tileMapObject.getProperties().get("value", tile.getProperties().get("value", 0, Integer.class), Integer.class);
                String itemWear = tileMapObject.getProperties().get("wear", tile.getProperties().get("wear", null, String.class), String.class);
                Item storageItem = new Item(itemId, itemName, texturePath, itemValue);
                if (itemWear != null && !itemWear.isBlank()) storageItem.setWear(itemWear.trim());
                storageItem.setEat(tileMapObject.getProperties().get("eat", tile.getProperties().get("eat", false, Boolean.class), Boolean.class));
                storageItem.setRead(tileMapObject.getProperties().get("read", tile.getProperties().get("read", false, Boolean.class), Boolean.class));
                String descS = tileMapObject.getProperties().get(
                    "description",
                    tile.getProperties().get("description", "", String.class),
                    String.class
                );
                if (descS != null && !descS.isBlank()) storageItem.setDescription(descS);
                storageItem.setPlusHP(tileMapObject.getProperties().get("plusHP", tile.getProperties().get("plusHP", 0, Integer.class), Integer.class));
                storageItem.setPlusAttack1(tileMapObject.getProperties().get("plusAttack1", tile.getProperties().get("plusAttack1", 0, Integer.class), Integer.class));
                storageItem.setPlusAttack2(tileMapObject.getProperties().get("plusAttack2", tile.getProperties().get("plusAttack2", 0, Integer.class), Integer.class));
                storageItem.setPlusAttack3(tileMapObject.getProperties().get("plusAttack3", tile.getProperties().get("plusAttack3", 0, Integer.class), Integer.class));
                storageItem.setPlusDefense(tileMapObject.getProperties().get("plusDefense", tile.getProperties().get("plusDefense", 0, Integer.class), Integer.class));
                storageItem.setPlusSpeed(tileMapObject.getProperties().get("plusSpeed", tile.getProperties().get("plusSpeed", 0, Integer.class), Integer.class));
                pendingStorageItems.add(new AbstractMap.SimpleEntry<>(insideStorageName, storageItem));
                return; // Don't create entity
            }

        }

        Entity entity = this.engine.createEntity();
        TextureRegion textureRegion = getTextureRegion(tile);
        float sortOffsetYPixels = tileMapObject.getProperties().get(
            "sortOffsetY",
            tile.getProperties().get("sortOffsetY", 0f, Float.class),
            Float.class);
        float sortOffsetY = sortOffsetYPixels * GdxGame.UNIT_SCALE;
        int baseZ = tile.getProperties().get("z", 1, Integer.class);
        int layerOffset = layerName.equals("objects") ? 0 : Integer.parseInt(layerName.replace("objects", ""));
        int z = baseZ + layerOffset;

        float spawnXPixels = tileMapObject.getX();
        float spawnYPixels = tileMapObject.getY();
        if ("player".equals(tileMapObject.getName()) && tiledService.consumePendingPlayerSpawnPixels(tmpVec2)) {
            spawnXPixels = tmpVec2.x;
            spawnYPixels = tmpVec2.y;
        }

        addEntityTransform(
            spawnXPixels, spawnYPixels, z,
            textureRegion.getRegionWidth(), textureRegion.getRegionHeight(),
            tileMapObject.getScaleX(), tileMapObject.getScaleY(),
            sortOffsetY,
            entity);
        BodyDef.BodyType bodyType = getObjectBodyType(tile);
        addEntityPhysic(tile.getObjects(), bodyType, Vector2.Zero, entity);
        addEntityAnimation(tile, tileMapObject, entity);
        addEntityInteractable(tile, tileMapObject, entity);
        addEntityStorage(tile, tileMapObject, entity);
        addEntityItem(tile, tileMapObject, entity);
        addEntityEnemy(tile, tileMapObject, entity);
        addEntityNpc(tile, tileMapObject, entity);
        addEntityDialogue(tile, tileMapObject, entity);
        addEntityStore(tile, tileMapObject, entity);
        addEntitySkillsStore(tile, tileMapObject, entity);
        addEntityTextDisplay(tile, tileMapObject, entity);
        addEntityCoinDropper(tile, tileMapObject, entity);
        addEntityMove(tile, entity);
        addEntityController(tileMapObject, entity);
        addEntityCameraFollow(tileMapObject, entity);
        addEntityLife(tile, entity);
        addEntityPlayer(tileMapObject, entity);
        addEntityRespawn(tileMapObject, entity);
        addEntityAttack(tile, entity);
        entity.add(new Facing(Facing.FacingDirection.RIGHT));
        entity.add(new Fsm(entity));
        float tileOpacity = tile.getProperties().get("opacity", 1f, Float.class);
        float objectOpacityProp = tileMapObject.getProperties().get("opacity", 1f, Float.class);
        float alpha = tileMapObject.getOpacity() * tileOpacity * objectOpacityProp;
        Color tint = Color.WHITE.cpy();
        tint.a = Math.max(0f, Math.min(1f, alpha));
        Graphic graphic = new Graphic(tint, textureRegion);
        float flipOffsetXPixels = tile.getProperties().get("graphicFlipOffsetX", 0f, Float.class);
        graphic.setFlipOffsetX(flipOffsetXPixels * GdxGame.UNIT_SCALE);
        entity.add(graphic);
        entity.add(new Tiled(layerName, tileMapObject));

        MapWorldState worldState = game.getMapWorldState(tiledService.getCurrentMapKey());
        WorldStatePersistence.tryRestore(entity, layerName, tileMapObject, worldState);
        if (isEnemy) {
            WorldStatePersistence.restoreEnemyLife(entity, layerName, tileMapObject, worldState);
            WorldStatePersistence.restoreEnemySnapshot(entity, layerName, tileMapObject, worldState);
        } else {
            boolean isNpc = tile.getProperties().get("npc", false, Boolean.class)
                || tileMapObject.getProperties().get("npc", false, Boolean.class)
                || "Character".equalsIgnoreCase(tile.getProperties().get("class", "", String.class))
                || "Character".equalsIgnoreCase(tileMapObject.getProperties().get("class", "", String.class));
            if (isNpc) {
                WorldStatePersistence.restoreNpcSnapshot(entity, layerName, tileMapObject, worldState);
                Animation2D npcAnim = Animation2D.MAPPER.get(entity);
                if (npcAnim != null) {
                    npcAnim.setType(Animation2D.AnimationType.IDLE);
                    npcAnim.setStage(1);
                    npcAnim.setPlayMode(PlayMode.LOOP);
                }
            }
        }

        if ("player".equals(tileMapObject.getName())) {
            PlayerSessionState session = tiledService.consumePendingPlayerSession();
            if (session != null) {
                PlayerStatePersistence.applyToEntity(entity, session, null);
            }
        }

        this.engine.addEntity(entity);
    }

    private void addEntityPlayer(TiledMapTileMapObject tileMapObject, Entity entity) {
        if("player".equals(tileMapObject.getName())) {
            entity.add(new Player());
            entity.add(new Inventory(16));
            entity.add(new ActiveSkills());
        }
    }

    private void addEntityRespawn(TiledMapTileMapObject tileMapObject, Entity entity) {
        if (Player.MAPPER.get(entity) == null) {
            return;
        }

        float respawnDelaySec = tileMapObject.getProperties().get("respawnDelaySec", 2.5f, Float.class);
        Transform transform = Transform.MAPPER.get(entity);
        entity.add(new Respawn(respawnDelaySec, transform.getPosition()));
    }

    private void addEntityAttack(TiledMapTile tile, Entity entity) {
        float damage = tile.getProperties().get("damage", 0f, Float.class);
        if (damage == 0f) return;

        float damageDelay = tile.getProperties().get("damageDelay", 0f, Float.class);
        float attackDuration = tile.getProperties().get("attackDuration", damageDelay, Float.class);
        float secondaryDamage = tile.getProperties().get("attack2Damage", damage * 1.5f, Float.class);
        float secondaryDamageDelay = tile.getProperties().get("attack2DamageDelay", damageDelay + 0.15f, Float.class);
        float secondaryAttackDuration = tile.getProperties().get("attack2Duration", secondaryDamageDelay, Float.class);
        float specialDamage = tile.getProperties().get("specialDamage", damage * 2f, Float.class);
        float specialDamageDelay = tile.getProperties().get("specialDamageDelay", damageDelay + 0.2f, Float.class);
        float specialAttackDuration = tile.getProperties().get("specialDuration", specialDamageDelay, Float.class);

        String soundAssetStr = tile.getProperties().get("attackSound", "", String.class);
        SoundAsset soundAsset = null;
        if(!soundAssetStr.isBlank()) {
            soundAsset = SoundAsset.valueOf(soundAssetStr);
        }

        entity.add(new Attack(
            damage, damageDelay, attackDuration,
            secondaryDamage, secondaryDamageDelay, secondaryAttackDuration,
            specialDamage, specialDamageDelay, specialAttackDuration,
            soundAsset
        ));
    }

    private void ensureDynamicPhysicBody(Entity entity) {
        Physic physic = Physic.MAPPER.get(entity);
        if (physic == null) {
            Transform transform = Transform.MAPPER.get(entity);
            if (transform != null) {
                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyDef.BodyType.DynamicBody;
                
                float w = transform.getSize().x;
                float h = transform.getSize().y;
                bodyDef.position.set(transform.getPosition());
                bodyDef.fixedRotation = true;

                Body body = physicWorld.createBody(bodyDef);
                body.setUserData(entity);

                CircleShape shape = new CircleShape();
                shape.setPosition(new Vector2(w * 0.5f, h * 0.25f));
                shape.setRadius(Math.max(w * 0.25f, 0.25f));

                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = shape;
                fixtureDef.friction = 0f;

                body.createFixture(fixtureDef);
                shape.dispose();

                entity.add(new Physic(body, new Vector2(body.getPosition())));
            }
        } else if (physic.getBody() != null) {
            physic.getBody().setType(BodyDef.BodyType.DynamicBody);
        }
    }

    private void addEntityEnemy(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean isEnemy = tile.getProperties().get("enemy", false, Boolean.class)
            || tileMapObject.getProperties().get("enemy", false, Boolean.class);
        if (!isEnemy) return;

        ensureDynamicPhysicBody(entity);

        float aggroRadiusPixels = tile.getProperties().get("aggroRadius", 80f, Float.class);
        float attackRangePixels = tile.getProperties().get("attackRange", 24f, Float.class);
        float respawnDelaySec = tileMapObject.getProperties().get("respawnDelaySec",
            tile.getProperties().get("respawnDelaySec", 30f, Float.class), Float.class);
        String roamZoneName = tileMapObject.getProperties().get("roamZone", null, String.class);

        float aggroRadius = aggroRadiusPixels * GdxGame.UNIT_SCALE;
        float attackRange = attackRangePixels * GdxGame.UNIT_SCALE;
        ZoneShape roamBounds = tiledService.getZone(roamZoneName);

        Transform transform = Transform.MAPPER.get(entity);
        Vector2 spawnPos = new Vector2(transform.getPosition());

        Vector2 collisionOffset = computeFixtureOffset(Physic.MAPPER.get(entity));
        entity.add(new Enemy(aggroRadius, attackRange, roamBounds, respawnDelaySec, spawnPos, collisionOffset));
    }

    private void addEntityNpc(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        // Check if class is "Character" for walking NPCs
        String classType = tile.getProperties().get("class", "", String.class);
        if (classType == null || classType.isBlank()) {
            classType = tileMapObject.getProperties().get("class", "", String.class);
        }
        boolean isCharacter = "Character".equalsIgnoreCase(classType);

        // Also check for explicit npc=true property as alternative
        boolean isNpc = tile.getProperties().get("npc", false, Boolean.class)
            || tileMapObject.getProperties().get("npc", false, Boolean.class);

        if (!isCharacter && !isNpc) return;

        // Skip if already has Enemy component (enemies have their own AI)
        if (Enemy.MAPPER.get(entity) != null) return;

        ensureDynamicPhysicBody(entity);

        float respawnDelaySec = tileMapObject.getProperties().get("respawnDelaySec",
            tile.getProperties().get("respawnDelaySec", 30f, Float.class), Float.class);
        String roamZoneName = tileMapObject.getProperties().get("roamZone",
            tile.getProperties().get("roamZone", null, String.class), String.class);

        ZoneShape roamBounds = tiledService.getZone(roamZoneName);

        Transform transform = Transform.MAPPER.get(entity);
        Vector2 spawnPos = new Vector2(transform.getPosition());

        Vector2 collisionOffset = computeFixtureOffset(Physic.MAPPER.get(entity));
        entity.add(new Npc(roamBounds, respawnDelaySec, spawnPos, collisionOffset));

        // Automatically setup Dialogue & Interactable if npc has displayText
        String text = tileMapObject.getProperties().get(
            "displayText",
            tile.getProperties().get("displayText", "", String.class),
            String.class
        );
        if (text != null && !text.isBlank()) {
            if (Interactable.MAPPER.get(entity) == null) {
                float radiusPixels = tileMapObject.getProperties().get(
                    "interactionRadius",
                    tile.getProperties().get("interactionRadius", 56f, Float.class),
                    Float.class
                );
                if (radiusPixels <= 0f) {
                    radiusPixels = 56f;
                }
                float radiusWorld = radiusPixels * GdxGame.UNIT_SCALE;
                entity.add(new Interactable(radiusWorld, 1));
            }
            entity.add(new Dialogue(text));
        }
    }

    private Vector2 computeFixtureOffset(Physic physic) {
        if (physic == null) return new Vector2();
        Body body = physic.getBody();
        if (body.getFixtureList().size == 0) return new Vector2();
        Shape shape = body.getFixtureList().first().getShape();
        if (shape instanceof PolygonShape poly) {
            float cx = 0, cy = 0;
            int n = poly.getVertexCount();
            Vector2 v = new Vector2();
            for (int i = 0; i < n; i++) {
                poly.getVertex(i, v);
                cx += v.x;
                cy += v.y;
            }
            return new Vector2(cx / n, cy / n);
        } else if (shape instanceof CircleShape circle) {
            return new Vector2(circle.getPosition());
        }
        return new Vector2();
    }

    private void addEntityLife(TiledMapTile tile, Entity entity) {
        int life = tile.getProperties().get("life", 0, Integer.class);
        if (life == 0) return;

        float lifeReg = tile.getProperties().get("lifeReg", 0f, Float.class);
        entity.add(new Life(life, lifeReg));
    }

    private void addEntityCameraFollow(TiledMapTileMapObject mapObject, Entity entity) {
        boolean camFollow = mapObject.getProperties().get("camFollow", false, Boolean.class);
        if(!camFollow) return;

        entity.add(new CameraFollow());
    }

    private BodyDef.BodyType getObjectBodyType(TiledMapTile tile) {
        String classType = tile.getProperties().get("type","", String.class);
        if("StaticProp".equals(classType)) {
            return BodyDef.BodyType.StaticBody;
        }

        String bodyTypeStr = tile.getProperties().get("bodyType", "DynamicBody", String.class);
        return BodyDef.BodyType.valueOf(bodyTypeStr);
    }

    private void addEntityPhysic(MapObjects objects, BodyDef.BodyType bodyType, Vector2 relativeTo, Entity entity) {
        if(objects.getCount() == 0) return;

        Transform transform = Transform.MAPPER.get(entity);
        Body body = createBody(objects, transform.getPosition(), transform.getScaling(), bodyType, relativeTo, entity);
        entity.add(new Physic(body, new Vector2(body.getPosition())));
    }

    private void addEntityAnimation(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        String tileAnimation = tile.getProperties().get("animation", "", String.class);
        String objectAnimation = tileMapObject.getProperties().get("animation", "", String.class);
        String animationStr = !objectAnimation.isBlank() ? objectAnimation : tileAnimation;
        if (animationStr.isBlank()) return;

        AnimationType animationType = AnimationType.valueOf(animationStr);
        String atlasAssetStr = tile.getProperties().get("atlasAsset", "OBJECTS", String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        float speed = tile.getProperties().get("animationSpeed", 0f, Float.class);
        Animation2D animation = new Animation2D(atlasAsset, atlasKey, animationType, PlayMode.LOOP, speed);
        int stage = tileMapObject.getProperties().get("stage",
            tile.getProperties().get("stage", 1, Integer.class), Integer.class);
        if (stage > 1) {
            animation.setStage(stage);
        }
        entity.add(animation);
    }

    private void addEntityInteractable(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean interactable = tile.getProperties().get("interactable", false, Boolean.class);
        if (!interactable) {
            return;
        }
        Animation2D animation = Animation2D.MAPPER.get(entity);
        if (animation == null) {
            return;
        }

        float radiusPixels = tile.getProperties().get("interactionRadius", 56f, Float.class);
        if (radiusPixels <= 0f) {
            radiusPixels = 56f;
        }
        float radiusWorld = radiusPixels * GdxGame.UNIT_SCALE;
        int activatedCount = tile.getProperties().get("activatedAnimations", 1, Integer.class);
        if (activatedCount < 1) {
            activatedCount = 1;
        }
        Interactable interactableComp = new Interactable(radiusWorld, activatedCount);

        // If the per-object animation was set to ACTIVATED, mark interactable as already activated.
        AnimationType animType = animation.getType();
        if (animType == AnimationType.ACTIVATED || animType == AnimationType.TO_ACTIVATED) {
            interactableComp.setActivated(true);
            int stage = Math.max(1, animation.getStage());
            interactableComp.setActivatedStage(stage);
        }
        entity.add(interactableComp);
    }

    private void addEntityStorage(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean storage = tile.getProperties().get("storage", false, Boolean.class)
            || tileMapObject.getProperties().get("storage", false, Boolean.class)
            || tile.getProperties().get("openInventory", false, Boolean.class)
            || tileMapObject.getProperties().get("openInventory", false, Boolean.class);
        if (!storage) {
            return;
        }
        if (Interactable.MAPPER.get(entity) == null) {
            Gdx.app.log("Storage", "  storage object has no Interactable; adding Storage component anyway");
        }
        String title = tileMapObject.getProperties().get(
            "storageTitle",
            tile.getProperties().get("storageTitle", "CHEST", String.class),
            String.class
        );
        int rows = tileMapObject.getProperties().get(
            "storageRows",
            tile.getProperties().get("storageRows", 4, Integer.class),
            Integer.class
        );
        int cols = tileMapObject.getProperties().get(
            "storageCols",
            tile.getProperties().get("storageCols", 4, Integer.class),
            Integer.class
        );
        entity.add(new Storage(title, rows, cols));
        currentLoadStorageEntities.add(entity);
    }

    public void flushPendingStorageItems() {
        Gdx.app.log("Storage", "flushPendingStorageItems: " + pendingStorageItems.size() + " pending items");
        for (AbstractMap.SimpleEntry<String, Item> entry : pendingStorageItems) {
            Gdx.app.log("Storage", "  -> placing item '" + entry.getValue().getId() + "' into storage '" + entry.getKey() + "'");
            addToStorage(entry.getKey(), entry.getValue());
        }
        pendingStorageItems.clear();
        currentLoadStorageEntities.clear();
        // Build shop catalog from tileset tile properties (not map objects)
        shopCatalog.clear();
        com.badlogic.gdx.maps.tiled.TiledMap currentMap = tiledService.getCurrentMap();
        if (currentMap != null) {
            for (com.badlogic.gdx.maps.tiled.TiledMapTileSet tileSet : currentMap.getTileSets()) {
                for (com.badlogic.gdx.maps.tiled.TiledMapTile tileEntry : tileSet) {
                    com.badlogic.gdx.maps.MapProperties tp = tileEntry.getProperties();
                    String stType = tp.get("storeType", null, String.class);
                    int prc = tp.get("price", 0, Integer.class);
                    if (stType == null || stType.isBlank() || prc <= 0) continue;
                    String iId = tp.get("itemId", null, String.class);
                    if (iId == null || iId.isBlank()) iId = "tile_" + tileEntry.getId();
                    String iName = tp.get("itemName", null, String.class);
                    if (iName == null || iName.isBlank()) iName = "Unknown Item";
                    String iTex = tp.get("itemIcon", String.class);
                    if (iTex == null) iTex = tp.get("icon", String.class);
                    if (iTex == null) iTex = tp.get("texture", String.class);
                    if (iTex == null || iTex.isBlank()) iTex = "ui/inventory/iconsitemsbag.png";
                    if (!iTex.startsWith("ui/")) iTex = "ui/inventory/" + iTex;
                    int iVal = tp.get("value", 0, Integer.class);
                    String iWear = tp.get("wear", null, String.class);
                    Item shopItem = new Item(iId, iName, iTex, iVal, prc);
                    if (iWear != null && !iWear.isBlank()) shopItem.setWear(iWear.trim());
                    shopItem.setEat(tp.get("eat", false, Boolean.class));
                    shopItem.setRead(tp.get("read", false, Boolean.class));
                    String sDesc = tp.get("description", "", String.class);
                    if (sDesc != null && !sDesc.isBlank()) shopItem.setDescription(sDesc);
                    shopItem.setPlusHP(tp.get("plusHP", 0, Integer.class));
                    shopItem.setPlusAttack1(tp.get("plusAttack1", 0, Integer.class));
                    shopItem.setPlusAttack2(tp.get("plusAttack2", 0, Integer.class));
                    shopItem.setPlusAttack3(tp.get("plusAttack3", 0, Integer.class));
                    shopItem.setPlusDefense(tp.get("plusDefense", 0, Integer.class));
                    shopItem.setPlusSpeed(tp.get("plusSpeed", 0, Integer.class));
                    shopCatalog.computeIfAbsent(stType.trim(), k -> new java.util.ArrayList<>()).add(shopItem);
                }
            }
        }
    }

    public java.util.List<Item> getShopCatalog(String storeType) {
        if (storeType == null) return java.util.Collections.emptyList();
        return shopCatalog.getOrDefault(storeType.trim(), java.util.Collections.emptyList());
    }

    private void addToStorage(String storageName, Item item) {
        if (storageName == null) {
            return;
        }
        storageName = storageName.trim();
        if (storageName.isBlank()) {
            return;
        }
        // Search only entities created during the current map load to avoid matching
        // stale entities from the previous map that are pending removal in the engine.
        Gdx.app.log("Storage", "  addToStorage('" + storageName + "'): found " + currentLoadStorageEntities.size() + " storage entities from current load");
        for (Entity entity : currentLoadStorageEntities) {
            Tiled tiled = Tiled.MAPPER.get(entity);
            if (tiled != null && tiled.getMapObjectRef() != null) {
                String objectName = tiled.getMapObjectRef().getName();
                String objectNameNormalized = objectName == null ? "" : objectName.trim();
                String objectIdString = String.valueOf(tiled.getId());
                Gdx.app.log("Storage", "    entity name='" + objectName + "' id='" + objectIdString + "'");
                if ((!objectNameNormalized.isBlank() && storageName.equalsIgnoreCase(objectNameNormalized))
                    || storageName.equals(objectIdString)) {
                    Storage storage = Storage.MAPPER.get(entity);
                    if (storage != null) {
                        // Find first empty slot
                        Item[] slots = storage.getSlots();
                        for (int i = 0; i < slots.length; i++) {
                            if (slots[i] == null) {
                                slots[i] = item;
                                return;
                            }
                        }
                        // No empty slot found, ignore
                    }
                }
            }
        }
    }

    private void addEntityItem(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        String type = tileMapObject.getProperties().get("type", String.class);
        if (type == null) {
            type = tile.getProperties().get("type", String.class);
        }
        boolean isItem = "item".equalsIgnoreCase(type)
            || "item".equalsIgnoreCase(tile.getProperties().get("class", String.class))
            || "item".equalsIgnoreCase(tileMapObject.getProperties().get("class", String.class))
            || tile.getProperties().get("item", false, Boolean.class)
            || tileMapObject.getProperties().get("item", false, Boolean.class);

        if (!isItem) {
            return;
        }

        String itemId = tileMapObject.getProperties().get("itemId", tile.getProperties().get("itemId", tileMapObject.getName(), String.class), String.class);
        if (itemId == null || itemId.isBlank()) {
            Object idProp = tileMapObject.getProperties().get("id");
            if (idProp != null) {
                itemId = "item_" + idProp.toString();
            } else {
                itemId = "item_pos_" + Math.round(tileMapObject.getX()) + "_" + Math.round(tileMapObject.getY());
            }
        }

        String itemName = tileMapObject.getProperties().get("itemName", tile.getProperties().get("itemName", tileMapObject.getName(), String.class), String.class);
        if (itemName == null || itemName.isBlank()) {
            itemName = "Unknown Item";
        }

        String texturePath = tileMapObject.getProperties().get("itemIcon", String.class);
        if (texturePath == null) texturePath = tile.getProperties().get("itemIcon", String.class);
        if (texturePath == null) texturePath = tileMapObject.getProperties().get("icon", String.class);
        if (texturePath == null) texturePath = tile.getProperties().get("icon", String.class);
        if (texturePath == null) texturePath = tileMapObject.getProperties().get("texture", String.class);
        if (texturePath == null) texturePath = tile.getProperties().get("texture", String.class);
        if (texturePath == null || texturePath.isBlank()) {
            texturePath = "ui/inventory/iconsitemsbag.png";
        }

        if (!texturePath.startsWith("ui/")) {
            texturePath = "ui/inventory/" + texturePath;
        }

        int itemValue = tileMapObject.getProperties().get("value", tile.getProperties().get("value", 0, Integer.class), Integer.class);
        String itemWear = tileMapObject.getProperties().get("wear", tile.getProperties().get("wear", null, String.class), String.class);
        Item mapItem = new Item(itemId, itemName, texturePath, itemValue);
        if (itemWear != null && !itemWear.isBlank()) mapItem.setWear(itemWear.trim());
        mapItem.setEat(tileMapObject.getProperties().get("eat", tile.getProperties().get("eat", false, Boolean.class), Boolean.class));
        mapItem.setRead(tileMapObject.getProperties().get("read", tile.getProperties().get("read", false, Boolean.class), Boolean.class));
        String desc = tileMapObject.getProperties().get(
            "description",
            tile.getProperties().get("description", "", String.class),
            String.class
        );
        if (desc != null && !desc.isBlank()) mapItem.setDescription(desc);
        mapItem.setPlusHP(tileMapObject.getProperties().get("plusHP", tile.getProperties().get("plusHP", 0, Integer.class), Integer.class));
        mapItem.setPlusAttack1(tileMapObject.getProperties().get("plusAttack1", tile.getProperties().get("plusAttack1", 0, Integer.class), Integer.class));
        mapItem.setPlusAttack2(tileMapObject.getProperties().get("plusAttack2", tile.getProperties().get("plusAttack2", 0, Integer.class), Integer.class));
        mapItem.setPlusAttack3(tileMapObject.getProperties().get("plusAttack3", tile.getProperties().get("plusAttack3", 0, Integer.class), Integer.class));
        mapItem.setPlusDefense(tileMapObject.getProperties().get("plusDefense", tile.getProperties().get("plusDefense", 0, Integer.class), Integer.class));
        mapItem.setPlusSpeed(tileMapObject.getProperties().get("plusSpeed", tile.getProperties().get("plusSpeed", 0, Integer.class), Integer.class));
        entity.add(mapItem);

        Physic physic = Physic.MAPPER.get(entity);
        if (physic == null) {
            Transform transform = Transform.MAPPER.get(entity);
            if (transform != null) {
                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyDef.BodyType.StaticBody;
                float itemWidth = transform.getSize().x;
                float itemHeight = transform.getSize().y;
                bodyDef.position.set(transform.getPosition().x + itemWidth * 0.5f, transform.getPosition().y + itemHeight * 0.5f);
                bodyDef.fixedRotation = true;

                Body body = physicWorld.createBody(bodyDef);
                body.setUserData(entity);

                CircleShape shape = new CircleShape();
                shape.setRadius(Math.max(itemWidth, itemHeight) * 0.5f);

                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;

                body.createFixture(fixtureDef);
                shape.dispose();

                entity.add(new Physic(body, new Vector2(body.getPosition())));
            }
        } else if (physic.getBody() != null) {
            for (Fixture fixture : physic.getBody().getFixtureList()) {
                fixture.setSensor(true);
            }
        }
    }

    private void addEntityDialogue(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        if (Interactable.MAPPER.get(entity) == null) {
            return;
        }
        boolean hasDialogue = tile.getProperties().get("dialogue", false, Boolean.class)
            || tileMapObject.getProperties().get("dialogue", false, Boolean.class);
        if (!hasDialogue) {
            return;
        }
        String text = tileMapObject.getProperties().get(
            "displayText",
            tile.getProperties().get("displayText", "", String.class),
            String.class
        );
        if (text == null || text.isBlank()) {
            return;
        }
        entity.add(new Dialogue(text));
    }

    private void addEntityStore(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean isStore = tile.getProperties().get("store", false, Boolean.class)
            || tileMapObject.getProperties().get("store", false, Boolean.class);
        if (!isStore) return;
        String storeType = tileMapObject.getProperties().get(
            "storeType",
            tile.getProperties().get("storeType", "general", String.class),
            String.class
        );
        String storeName = tileMapObject.getProperties().get(
            "storeName",
            tile.getProperties().get("storeName", "SHOP", String.class),
            String.class
        );
        entity.add(new com.github.MichalKC.manylands.component.Store(storeType, storeName));
    }

    private void addEntitySkillsStore(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean isSkillsStore = tile.getProperties().get("skillsStore", false, Boolean.class)
            || tileMapObject.getProperties().get("skillsStore", false, Boolean.class);
        if (!isSkillsStore) return;
        entity.add(new com.github.MichalKC.manylands.component.SkillsStore());
    }

    private void addEntityTextDisplay(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        if (Dialogue.MAPPER.get(entity) != null) {
            return;
        }
        String text = tileMapObject.getProperties().get(
            "displayText",
            tile.getProperties().get("displayText", "", String.class),
            String.class
        );
        if (text == null || text.isBlank()) {
            return;
        }
        float displayDuration = tileMapObject.getProperties().get(
            "textDisplayDuration",
            tile.getProperties().get("textDisplayDuration", 3.0f, Float.class),
            Float.class
        );
        entity.add(new TextDisplay(text, displayDuration));
    }

    private void addEntityCoinDropper(TiledMapTile tile, TiledMapTileMapObject tileMapObject, Entity entity) {
        if (Interactable.MAPPER.get(entity) == null) {
            return;
        }
        int amount = tileMapObject.getProperties().get(
            "coinAmount",
            tile.getProperties().get("coinAmount", 0, Integer.class),
            Integer.class
        );
        if (amount <= 0) {
            return;
        }
        CoinDropper dropper = new CoinDropper(amount);
        // If the interactable was already activated (e.g. restored from world state), assume coins were dropped already.
        Interactable interactable = Interactable.MAPPER.get(entity);
        if (interactable.isActivated()) {
            dropper.setDropped(true);
        }
        entity.add(dropper);
    }

    private void addEntityMove(TiledMapTile tile, Entity entity) {
        float speed = tile.getProperties().get("speed", 0f, Float.class);
        if (speed == 0f) return;

        entity.add(new Move(speed));
    }

    private void addEntityController(TiledMapTileMapObject tileMapObject, Entity entity) {
        boolean controller = tileMapObject.getProperties().get("controller", false, Boolean.class);
        if(!controller) return;

        entity.add(new Controller());
    }

    private void addEntityTransform(
        float x, float y, int z,
        float w, float h,
        float scaleX, float scaleY,
        float sortOffsetY,
        Entity entity
    ) {
        Vector2 position = new Vector2(x, y);
        Vector2 size = new Vector2(w, h);
        Vector2 scaling = new Vector2(scaleX, scaleY);

        position.scl(GdxGame.UNIT_SCALE);
        size.scl(GdxGame.UNIT_SCALE);

        entity.add(new Transform(position, z, size, scaling, 0f, sortOffsetY));
    }

    private TextureRegion getTextureRegion(TiledMapTile tile) {
        String atlasAssetStr = tile.getProperties().get("atlasAsset", AtlasAsset.OBJECTS.name(), String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        TextureAtlas textureAtlas = this.assetService.get(atlasAsset);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();

        // Try new PROPS/<world>/ structure first (via propsWorld property)
        String propsWorld = tile.getProperties().get("propsWorld", null, String.class);
        if (propsWorld != null && !propsWorld.isBlank()) {
            String regionName = "PROPS/" + propsWorld.trim() + "/" + atlasKey;
            TextureAtlas.AtlasRegion region = textureAtlas.findRegion(regionName);
            if (region != null) {
                return region;
            }
        }

        // Fallback to legacy grass1-props/ structure for backward compatibility
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion("grass1-props/" + atlasKey);
        if (region != null) {
            return region;
        }

        // Try new structure with grass1 as default world (for migrated content)
        region = textureAtlas.findRegion("PROPS/grass1/" + atlasKey);
        if (region != null) {
            return region;
        }

        return tile.getTextureRegion();
    }

    private void addEntityPhysic(MapObject mapObject, BodyDef.BodyType bodyType, Vector2 relativeTo, Entity entity) {
        if (tmpMapObjects.getCount() > 0) tmpMapObjects.remove(0);

        tmpMapObjects.add(mapObject);
        addEntityPhysic(tmpMapObjects, bodyType, relativeTo, entity);
    }

    public void onLoadTrigger(String triggerName, MapObject triggerMapObject) {
        Entity entity = this.engine.createEntity();
        Rectangle bounds = getObjectBounds(triggerMapObject);

        addEntityTransform(
            bounds.getX(), bounds.getY(), 0,
            bounds.getWidth(), bounds.getHeight(),
            1f, 1f,
            0,
            entity
        );
        addEntityPhysic(
            triggerMapObject,
            BodyDef.BodyType.StaticBody,
            tmpVec2.set(bounds.getX(), bounds.getY()).scl(GdxGame.UNIT_SCALE),
            entity
        );
        entity.add(new Trigger(triggerName));
        String targetMap = triggerMapObject.getProperties().get("targetMap", null, String.class);
        if (targetMap != null && !targetMap.isBlank()) {
            String trimmed = targetMap.trim();
            TiledService.assertSafeMapFileName(trimmed);
            Float spawnX = triggerMapObject.getProperties().get("spawnX", null, Float.class);
            Float spawnY = triggerMapObject.getProperties().get("spawnY", null, Float.class);
            if ((spawnX != null) != (spawnY != null)) {
                throw new GdxRuntimeException(
                    "Trigger " + triggerName + ": set both spawnX and spawnY or neither (targetMap=" + trimmed + ")"
                );
            }
            String spawnAt = triggerMapObject.getProperties().get("spawnAt", null, String.class);
            String spawnAtTrimmed = spawnAt != null && !spawnAt.isBlank() ? spawnAt.trim() : null;
            boolean hasPixels = spawnX != null && spawnY != null;
            if (!hasPixels && spawnAtTrimmed == null) {
                throw new GdxRuntimeException(
                    "Trigger " + triggerName + ": map portal needs spawnAt (object name on target map) and/or spawnX+spawnY (targetMap="
                        + trimmed + ")"
                );
            }
            entity.add(new MapPortal(trimmed, spawnX, spawnY, spawnAtTrimmed));
        }
        addEntityGated(triggerMapObject, entity);

        // Parse heal property for heal zones
        Float healMultiplier = triggerMapObject.getProperties().get("heal", null, Float.class);
        if (healMultiplier != null && healMultiplier > 0f) {
            entity.add(new HealZone(healMultiplier));
            // Make the fixture a sensor for heal zones so they don't block movement
            Physic physic = Physic.MAPPER.get(entity);
            if (physic != null && physic.getBody() != null) {
                for (Fixture fixture : physic.getBody().getFixtureList()) {
                    fixture.setSensor(true);
                }
            }
        }

        // Parse canDamage property for damage zones
        Float canDamage = triggerMapObject.getProperties().get("canDamage", null, Float.class);
        if (canDamage != null && canDamage > 0f) {
            boolean canDamageEnemies = triggerMapObject.getProperties().get("canDamageEnemies", false, Boolean.class);
            entity.add(new com.github.MichalKC.manylands.component.DamageZone(canDamage, canDamageEnemies));
            Physic physic = Physic.MAPPER.get(entity);
            if (physic != null && physic.getBody() != null) {
                for (Fixture fixture : physic.getBody().getFixtureList()) {
                    fixture.setSensor(true);
                }
            }
        }

        // Parse combat trigger
        String combatEnemyId = triggerMapObject.getProperties().get("combat", null, String.class);
        if (combatEnemyId != null && !combatEnemyId.isBlank()) {
            entity.add(new com.github.MichalKC.manylands.component.CombatTrigger(combatEnemyId.trim()));
            Physic physic = Physic.MAPPER.get(entity);
            if (physic != null && physic.getBody() != null) {
                for (Fixture fixture : physic.getBody().getFixtureList()) {
                    fixture.setSensor(true);
                }
            }
        }

        entity.add(new Tiled("trigger", triggerMapObject));

        this.engine.addEntity(entity);
    }

    private void addEntityGated(MapObject triggerMapObject, Entity entity) {
        String csv = triggerMapObject.getProperties().get("requiresActivated", null, String.class);
        if (csv == null || csv.isBlank()) return;

        java.util.List<String> names = new java.util.ArrayList<>();
        for (String token : csv.split("[,;]")) {
            String t = token.trim();
            if (!t.isEmpty()) names.add(t);
        }
        if (names.isEmpty()) return;
        entity.add(new Gated(names));
    }

    private Rectangle getObjectBounds(MapObject mapObject) {
        if (mapObject instanceof RectangleMapObject rectMapObj) {
            return rectMapObj.getRectangle();
        } else if (mapObject instanceof EllipseMapObject ellipseMapObj) {
            Ellipse ellipse = ellipseMapObj.getEllipse();
            return new Rectangle(ellipse.x, ellipse.y, ellipse.width, ellipse.height);
        } else if (mapObject instanceof CircleMapObject circleMapObj) {
            Circle circle = circleMapObj.getCircle();
            return new Rectangle(circle.x - circle.radius, circle.y - circle.radius, circle.radius * 2, circle.radius * 2);
        } else if (mapObject instanceof PolygonMapObject polygonMapObj) {
            Polygon polygon = polygonMapObj.getPolygon();
            float[] vertices = polygon.getVertices();
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
            for (int i = 0; i < vertices.length; i += 2) {
                minX = Math.min(minX, vertices[i]);
                maxX = Math.max(maxX, vertices[i]);
                minY = Math.min(minY, vertices[i + 1]);
                maxY = Math.max(maxY, vertices[i + 1]);
            }
            return new Rectangle(minX, minY, maxX - minX, maxY - minY);
        } else {
            throw new GdxRuntimeException("Unsupported trigger map object: " + mapObject.getClass().getSimpleName());
        }
    }
}
