package com.github.MichalKC.manylands.tiled;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.asset.AssetService;
import com.github.MichalKC.manylands.GameResumeState;
import com.github.MichalKC.manylands.PlayerSessionState;
import com.github.MichalKC.manylands.asset.MapAsset;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TiledService {
    private final AssetService assetService;
    private final World physicWorld;

    private TiledMap currentMap;

    private Float pendingPlayerSpawnPixelX;
    private Float pendingPlayerSpawnPixelY;

    private PlayerSessionState pendingPlayerSession;
    private String currentMapKey;

    private Consumer<TiledMap> mapChangeConsumer;
    private BiConsumer<String, TiledMapTileMapObject> loadObjectConsumer;
    private LoadTileConsumer loadTileConsumer;
    private BiConsumer<String, MapObject> loadTriggerConsumer;
    private Runnable postLoadConsumer;
    private final ObjectMap<String, ZoneShape> zones;
    private final GdxGame game;

    public TiledService(AssetService assetService, World physicWorld, GdxGame game) {
        this.assetService = assetService;
        this.physicWorld = physicWorld;
        this.game = game;
        this.mapChangeConsumer = null;
        this.loadObjectConsumer = null;
        this.currentMap = null;
        this.loadTileConsumer = null;
        this.loadTriggerConsumer = null;
        this.postLoadConsumer = null;
        this.zones = new ObjectMap<>();
    }

    public TiledMap loadMap(MapAsset mapAsset) {
        TiledMap tiledMap = this.assetService.load(mapAsset);
        tiledMap.getProperties().put("mapAsset", mapAsset);
        tiledMap.getProperties().remove("mapFile");
        return tiledMap;
    }

    public TiledMap loadMapFromFile(String mapFileName) {
        assertSafeMapFileName(mapFileName);
        TiledMap tiledMap = this.assetService.loadTiledMapByFileName(mapFileName);
        tiledMap.getProperties().remove("mapAsset");
        tiledMap.getProperties().put("mapFile", mapFileName);
        return tiledMap;
    }

    public static void assertSafeMapFileName(String mapFileName) {
        if (mapFileName == null || mapFileName.isBlank()) {
            throw new GdxRuntimeException("Map file name is empty");
        }
        if (mapFileName.contains("..") || mapFileName.indexOf('/') >= 0 || mapFileName.indexOf('\\') >= 0) {
            throw new GdxRuntimeException("Invalid map file name (use basename only): " + mapFileName);
        }
        if (!mapFileName.toLowerCase(java.util.Locale.ROOT).endsWith(".tmx")) {
            throw new GdxRuntimeException("Map file name must end with .tmx: " + mapFileName);
        }
    }

    public void setMap(TiledMap map) {
        if(this.currentMap != null) {
            MapAsset previousAsset = this.currentMap.getProperties().get("mapAsset", null, MapAsset.class);
            if (previousAsset != null) {
                this.assetService.unload(previousAsset);
            }
            String previousFile = this.currentMap.getProperties().get("mapFile", null, String.class);
            if (previousFile != null && !previousFile.isBlank()) {
                this.assetService.unloadTiledMapByFileName(previousFile);
            }

            //***TO USUWA WSZYSTKIE OBIEKTY Z ENVIRONMENT PRZEZ CO NIE ZAPISUJE
            Array<Body> bodies = new Array<>();
            physicWorld.getBodies(bodies);
            for (Body body : bodies) {
                if ("environment".equals(body.getUserData())) {
                    physicWorld.destroyBody(body);
                }
            }
            //KONIEC
        }

        this.currentMap = map;
        this.currentMapKey = resolveMapKey(map);
        
        // Initialize MapWorldState if it doesn't exist
        if (this.currentMapKey != null && game.getMapWorldState(this.currentMapKey) == null) {
            game.putMapWorldState(this.currentMapKey, new com.github.MichalKC.manylands.world.MapWorldState());
        }
        
        loadMapObjects(map);
        if(this.mapChangeConsumer != null) {
            this.mapChangeConsumer.accept(map);
        }
    }

    private void loadMapObjects(TiledMap tiledMap) {
        zones.clear();
        for (MapLayer layer : tiledMap.getLayers()) {
            if ("zones".equals(layer.getName())) {
                loadZonesLayer(layer);
            }
        }

        for (MapLayer layer : tiledMap.getLayers()) {
            if (layer.getName() != null && layer.getName().startsWith("objects")) {
                loadObjectLayer(layer);
            } else if(layer instanceof TiledMapTileLayer tileLayer) {
                loadTileLayer(tileLayer);
            } else if("trigger".equals(layer.getName())) {
                loadTriggerLayer(layer);
            }
        }

        spawnMapBoundary(tiledMap);
        if (this.postLoadConsumer != null) {
            this.postLoadConsumer.run();
        }
    }

    private void loadZonesLayer(MapLayer layer) {
        for (MapObject mapObject : layer.getObjects()) {
            String name = mapObject.getName();
            if (name == null || name.isBlank()) {
                continue;
            }

            ZoneShape zoneShape = null;
            
            if (mapObject instanceof RectangleMapObject rectObj) {
                Rectangle rect = rectObj.getRectangle();
                Rectangle worldRect = new Rectangle(
                    rect.x * GdxGame.UNIT_SCALE,
                    rect.y * GdxGame.UNIT_SCALE,
                    rect.width * GdxGame.UNIT_SCALE,
                    rect.height * GdxGame.UNIT_SCALE
                );
                zoneShape = new ZoneShape.RectangleZone(worldRect);
            } else if (mapObject instanceof EllipseMapObject ellipseObj) {
                Ellipse ellipse = ellipseObj.getEllipse();
                float x = ellipse.x * GdxGame.UNIT_SCALE;
                float y = ellipse.y * GdxGame.UNIT_SCALE;
                float width = ellipse.width * GdxGame.UNIT_SCALE;
                float height = ellipse.height * GdxGame.UNIT_SCALE;
                
                if (Math.abs(width - height) < 0.01f) {
                    Circle circle = new Circle(x + width / 2, y + height / 2, width / 2);
                    zoneShape = new ZoneShape.CircleZone(circle);
                } else {
                    Ellipse worldEllipse = new Ellipse(x, y, width, height);
                    Polygon polygon = ellipseToPolygon(worldEllipse);
                    zoneShape = new ZoneShape.PolygonZone(polygon);
                }
            } else if (mapObject instanceof PolygonMapObject polyObj) {
                Polygon polygon = polyObj.getPolygon();
                float[] vertices = polygon.getVertices();
                float[] worldVertices = new float[vertices.length];
                float offsetX = polygon.getX() * GdxGame.UNIT_SCALE;
                float offsetY = polygon.getY() * GdxGame.UNIT_SCALE;
                for (int i = 0; i < vertices.length; i += 2) {
                    worldVertices[i] = offsetX + vertices[i] * GdxGame.UNIT_SCALE;
                    worldVertices[i + 1] = offsetY + vertices[i + 1] * GdxGame.UNIT_SCALE;
                }
                Polygon worldPolygon = new Polygon(worldVertices);
                zoneShape = new ZoneShape.PolygonZone(worldPolygon);
            } else if (mapObject instanceof CircleMapObject circleObj) {
                Circle circle = circleObj.getCircle();
                float x = circle.x * GdxGame.UNIT_SCALE;
                float y = circle.y * GdxGame.UNIT_SCALE;
                float radius = circle.radius * GdxGame.UNIT_SCALE;
                Circle worldCircle = new Circle(x, y, radius);
                zoneShape = new ZoneShape.CircleZone(worldCircle);
            }
            
            if (zoneShape != null) {
                zones.put(name.trim(), zoneShape);
            }
        }
    }
    
    private Polygon ellipseToPolygon(Ellipse ellipse) {
        int numVertices = 16;
        float[] vertices = new float[numVertices * 2];
        float centerX = ellipse.x + ellipse.width / 2;
        float centerY = ellipse.y + ellipse.height / 2;
        float radiusX = ellipse.width / 2;
        float radiusY = ellipse.height / 2;
        
        for (int i = 0; i < numVertices; i++) {
            float angle = (float) (2 * Math.PI * i / numVertices);
            vertices[i * 2] = centerX + radiusX * (float) Math.cos(angle);
            vertices[i * 2 + 1] = centerY + radiusY * (float) Math.sin(angle);
        }
        
        return new Polygon(vertices);
    }

    public ZoneShape getZone(String name) {
        if (name == null || name.isBlank()) return null;
        return zones.get(name.trim());
    }

    private void loadTriggerLayer(MapLayer layer) {
        if(loadTriggerConsumer == null) return;

        for (MapObject mapObject : layer.getObjects()) {
            if (mapObject.getName() == null || mapObject.getName().isBlank()) {
                throw new GdxRuntimeException("Trigger must have a name: " + mapObject);
            }

            loadTriggerConsumer.accept(mapObject.getName(), mapObject);
        }
    }

    private void spawnMapBoundary(TiledMap tiledMap) {
        Integer width = tiledMap.getProperties().get("width", 0, Integer.class);
        Integer tileW = tiledMap.getProperties().get("tilewidth", 0, Integer.class);
        Integer height = tiledMap.getProperties().get("height", 0, Integer.class);
        Integer tileH = tiledMap.getProperties().get("tileheight", 0, Integer.class);
        float mapW = width * tileW * GdxGame.UNIT_SCALE;
        float mapH = height * tileH * GdxGame.UNIT_SCALE;
        float halfW = mapW * 0.5f;
        float halfH = mapH * 0.5f;
        float boxThickness = 0.5f;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.setZero();
        bodyDef.fixedRotation = true;
        Body body = physicWorld.createBody(bodyDef);

        //***USUWANIE OBIEKTÓW, DODATKOWA LINIJKA
        body.setUserData("environment");
        //KONIEC

        // left edge
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boxThickness, halfH, new Vector2(-boxThickness, halfH), 0f);
        body.createFixture(shape, 0f).setFriction(0f);
        shape.dispose();

        // right edge
        shape = new PolygonShape();
        shape.setAsBox(boxThickness, halfH, new Vector2(mapW + boxThickness, halfH), 0f);
        body.createFixture(shape, 0f).setFriction(0f);
        shape.dispose();

        // bottom edge
        shape = new PolygonShape();
        shape.setAsBox(halfW, boxThickness, new Vector2(halfW, -boxThickness), 0f);
        body.createFixture(shape, 0f).setFriction(0f);
        shape.dispose();

        // top edge
        shape = new PolygonShape();
        shape.setAsBox(halfW, boxThickness, new Vector2(halfW, mapH + boxThickness), 0f);
        body.createFixture(shape, 0f).setFriction(0f);
        shape.dispose();
    }

    private void loadTileLayer(TiledMapTileLayer tileLayer) {
        if(loadTileConsumer == null) return;

        for (int y = 0; y< tileLayer.getHeight(); y++) {
            for (int x = 0; x < tileLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if(cell == null) continue;

                loadTileConsumer.accept(cell.getTile(), x, y);
            }
        }
    }

    private void loadObjectLayer(MapLayer objectLayer) {
        if(loadObjectConsumer == null) return;

        String layerName = objectLayer.getName();
        float layerOpacity = objectLayer.getOpacity();
        for (MapObject mapObject : objectLayer.getObjects()) {
            if(mapObject instanceof TiledMapTileMapObject tileMapObject) {
                tileMapObject.setOpacity(tileMapObject.getOpacity() * layerOpacity);
                loadObjectConsumer.accept(layerName, tileMapObject);
            } else {
                throw new GdxRuntimeException("Unsupported object: " + mapObject.getClass().getSimpleName());
            }
        }
    }

    public void setMapChangeConsumer(Consumer<TiledMap> mapChangeConsumer) {
        this.mapChangeConsumer = mapChangeConsumer;
    }

    public void setLoadObjectConsumer(BiConsumer<String, TiledMapTileMapObject> loadObjectConsumer) {
        this.loadObjectConsumer = loadObjectConsumer;
    }

    public void setLoadTileConsumer(LoadTileConsumer loadTileConsumer) {
        this.loadTileConsumer = loadTileConsumer;
    }

    public void setLoadTriggerConsumer(BiConsumer<String, MapObject> loadTriggerConsumer) {
        this.loadTriggerConsumer = loadTriggerConsumer;
    }

    public void setPostLoadConsumer(Runnable postLoadConsumer) {
        this.postLoadConsumer = postLoadConsumer;
    }

    public TiledMap getCurrentMap() {
        return currentMap;
    }

    public void setPendingPlayerSpawnPixels(float pixelX, float pixelY) {
        this.pendingPlayerSpawnPixelX = pixelX;
        this.pendingPlayerSpawnPixelY = pixelY;
    }

    public boolean hasPendingPlayerSpawnPixels() {
        return pendingPlayerSpawnPixelX != null && pendingPlayerSpawnPixelY != null;
    }

    public boolean consumePendingPlayerSpawnPixels(Vector2 outPixels) {
        if (!hasPendingPlayerSpawnPixels()) {
            return false;
        }
        outPixels.set(pendingPlayerSpawnPixelX, pendingPlayerSpawnPixelY);
        clearPendingPlayerSpawnPixels();
        return true;
    }

    public void clearPendingPlayerSpawnPixels() {
        this.pendingPlayerSpawnPixelX = null;
        this.pendingPlayerSpawnPixelY = null;
    }

    public void setPendingPlayerSession(PlayerSessionState session) {
        this.pendingPlayerSession = session;
    }

    public PlayerSessionState consumePendingPlayerSession() {
        PlayerSessionState session = this.pendingPlayerSession;
        this.pendingPlayerSession = null;
        return session;
    }

    public String getCurrentMapKey() {
        return currentMapKey;
    }

    public static String resolveMapKey(TiledMap map) {
        if (map == null) {
            return null;
        }
        String mapFile = map.getProperties().get("mapFile", null, String.class);
        if (mapFile != null && !mapFile.isBlank()) {
            return normalizeMapKey(mapFile);
        }
        MapAsset mapAsset = map.getProperties().get("mapAsset", null, MapAsset.class);
        if (mapAsset != null) {
            return normalizeMapKey(mapAsset.getDescriptor().fileName);
        }
        return null;
    }

    public static String normalizeMapKey(String mapKey) {
        if (mapKey == null || mapKey.isBlank()) {
            return mapKey;
        }
        int slash = Math.max(mapKey.lastIndexOf('/'), mapKey.lastIndexOf('\\'));
        return slash >= 0 ? mapKey.substring(slash + 1) : mapKey;
    }

    public GameResumeState buildResumeState(float worldX, float worldY) {
        if (currentMap == null) {
            return null;
        }
        String mapFile = currentMap.getProperties().get("mapFile", null, String.class);
        MapAsset mapAsset = currentMap.getProperties().get("mapAsset", null, MapAsset.class);
        if (mapFile != null && !mapFile.isBlank()) {
            return new GameResumeState(mapFile, null, worldX, worldY);
        }
        if (mapAsset != null) {
            return new GameResumeState(null, mapAsset, worldX, worldY);
        }
        return null;
    }

    @FunctionalInterface
    public interface LoadTileConsumer {
        void accept(TiledMapTile tile, float x, float y);
    }
}
