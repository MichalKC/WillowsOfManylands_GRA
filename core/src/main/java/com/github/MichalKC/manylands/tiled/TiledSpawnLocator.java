package com.github.MichalKC.manylands.tiled;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public final class TiledSpawnLocator {

    private TiledSpawnLocator() {
    }

    public static boolean findSpawnCenterPixels(TiledMap map, String objectName, Vector2 outPixels) {
        if (objectName == null || objectName.isBlank()) {
            return false;
        }
        if (tryLayerName(map, "spawn", objectName, outPixels)) {
            return true;
        }
        if (tryLayerName(map, "trigger", objectName, outPixels)) {
            return true;
        }
        for (MapLayer layer : map.getLayers()) {
            String n = layer.getName();
            if (n == null || !n.startsWith("objects")) {
                continue;
            }
            if (tryLayer(layer, objectName, outPixels)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryLayerName(TiledMap map, String layerName, String objectName, Vector2 outPixels) {
        MapLayer layer = null;
        for (MapLayer candidate : map.getLayers()) {
            if (layerName.equals(candidate.getName())) {
                layer = candidate;
                break;
            }
        }
        if (layer == null) {
            return false;
        }
        return tryLayer(layer, objectName, outPixels);
    }

    private static boolean tryLayer(MapLayer layer, String objectName, Vector2 outPixels) {
        MapObjects objects = layer.getObjects();
        if (objects == null) {
            return false;
        }
        for (MapObject obj : objects) {
            if (!objectName.equals(obj.getName())) {
                continue;
            }
            if (obj instanceof RectangleMapObject rectObj) {
                Rectangle r = rectObj.getRectangle();
                outPixels.set(r.x + r.width * 0.5f, r.y);
                return true;
            }
            if (obj instanceof TiledMapTileMapObject tileObj) {
                outPixels.set(tileObj.getX(), tileObj.getY());
                return true;
            }
        }
        return false;
    }
}
