package com.github.MichalKC.manylands.tiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.MichalKC.manylands.GdxGame;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Shape;


public final class TiledPhysics {

    public static FixtureDef fixtureDefOf(MapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        if (mapObject instanceof RectangleMapObject rectMapObj) {
            return rectangleFixtureDef(rectMapObj, scaling, relativeTo);
        } else if (mapObject instanceof EllipseMapObject ellipseMapObj) {
            return ellipseFixtureDef(ellipseMapObj, scaling, relativeTo);
        } else if (mapObject instanceof PolygonMapObject polygonMapObj) {
            Polygon polygon = polygonMapObj.getPolygon();
            float offsetX = polygon.getX() * GdxGame.UNIT_SCALE;
            float offsetY = polygon.getY() * GdxGame.UNIT_SCALE;
            return polygonFixtureDef(polygonMapObj, polygon.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else if (mapObject instanceof PolylineMapObject polylineMapObj) {
            Polyline polyline = polylineMapObj.getPolyline();
            float offsetX = polyline.getX() * GdxGame.UNIT_SCALE;
            float offsetY = polyline.getY() * GdxGame.UNIT_SCALE;
            return polygonFixtureDef(polylineMapObj, polyline.getVertices(), offsetX, offsetY, scaling, relativeTo);
        } else {
            throw new GdxRuntimeException("Unsupported MapObject: " + mapObject);
        }
    }

    private static FixtureDef rectangleFixtureDef(RectangleMapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        Rectangle rectangle = mapObject.getRectangle();
        float rectX = rectangle.x;
        float rectY = rectangle.y;
        float rectW = rectangle.width;
        float rectH = rectangle.height;

        float boxX = rectX * GdxGame.UNIT_SCALE * scaling.x - relativeTo.x;
        float boxY = rectY * GdxGame.UNIT_SCALE * scaling.y - relativeTo.y;
        float boxW = rectW * GdxGame.UNIT_SCALE * scaling.x * 0.5f;
        float boxH = rectH * GdxGame.UNIT_SCALE * scaling.y * 0.5f;

        String objInfo = mapObject.getName() != null ? mapObject.getName() :
            "Rectangle@(" + (int)rectangle.x + "," + (int)rectangle.y + ") " + (int)rectangle.width + "x" + (int)rectangle.height;
        Gdx.app.log("TiledPhysics", "Creating rectangle fixture: " + objInfo + " -> size: " + boxW + "x" + boxH);

        if (boxW < 0.005f || boxH < 0.005f) {
            Gdx.app.error("TiledPhysics", "RECTANGLE TOO SMALL: " + objInfo + "! size=" + boxW + "x" + boxH);
        }

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(boxW, boxH, new Vector2(boxX + boxW, boxY + boxH), 0f);
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    private static FixtureDef ellipseFixtureDef(EllipseMapObject mapObject, Vector2 scaling, Vector2 relativeTo) {
        String objName = mapObject.getName() != null ? mapObject.getName() : "unnamed";

        Ellipse ellipse = mapObject.getEllipse();
        float x = ellipse.x;
        float y = ellipse.y;
        float w = ellipse.width;
        float h = ellipse.height;

        float ellipseX = x * GdxGame.UNIT_SCALE * scaling.x - relativeTo.x;
        float ellipseY = y * GdxGame.UNIT_SCALE * scaling.y - relativeTo.y;
        float ellipseW = w * GdxGame.UNIT_SCALE * scaling.x * 0.5f;
        float ellipseH = h * GdxGame.UNIT_SCALE * scaling.y * 0.5f;

        if (MathUtils.isEqual(ellipseW, ellipseH, 0.1f)) {
            CircleShape shape = new CircleShape();
            shape.setPosition(new Vector2(ellipseX + ellipseW, ellipseY + ellipseH));
            shape.setRadius(ellipseW);
            return fixtureDefOfMapObjectAndShape(mapObject, shape);
        }

        final int numVertices = 8;
        float angleStep = MathUtils.PI2 / numVertices;
        Vector2[] vertices = new Vector2[numVertices];

        for (int vertexIdx = 0; vertexIdx < numVertices; vertexIdx++) {
            float angle = vertexIdx * angleStep;
            float offsetX = ellipseW * MathUtils.cos(angle);
            float offsetY = ellipseH * MathUtils.sin(angle);
            vertices[vertexIdx] = new Vector2(ellipseX + ellipseW + offsetX, ellipseY + ellipseH + offsetY);
        }

        PolygonShape shape = new PolygonShape();
        shape.set(vertices);
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    private static FixtureDef polygonFixtureDef(
        MapObject mapObject,
        float[] polyVertices,
        float offsetX,
        float offsetY,
        Vector2 scaling,
        Vector2 relativeTo
    ) {
        offsetX = (offsetX * scaling.x) - relativeTo.x;
        offsetY = (offsetY * scaling.y) - relativeTo.y;
        float[] vertices = new float[polyVertices.length];
        for (int vertexIdx = 0; vertexIdx < polyVertices.length; vertexIdx += 2) {
            vertices[vertexIdx] = offsetX + polyVertices[vertexIdx] * GdxGame.UNIT_SCALE * scaling.x;
            vertices[vertexIdx + 1] = offsetY + polyVertices[vertexIdx + 1] * GdxGame.UNIT_SCALE * scaling.y;
        }

        // Log object info with position for debugging (even if unnamed)
        String objInfo = mapObject.getName() != null ? mapObject.getName() :
            mapObject.getClass().getSimpleName() + "@(" + (int)offsetX + "," + (int)offsetY + ")";

        // Remove vertices that are too close to each other (Box2D requires min 0.005 distance)
        float[] cleanedVertices = removeDuplicateVertices(vertices, 0.005f, objInfo);

        Gdx.app.log("TiledPhysics", "Creating fixture: " + objInfo + " vertices=" + (cleanedVertices.length/2) + " (was " + (vertices.length/2) + ")");

        ChainShape shape = new ChainShape();
        if (mapObject instanceof PolygonMapObject) {
            shape.createLoop(cleanedVertices);
        } else {
            shape.createChain(cleanedVertices);
        }
        return fixtureDefOfMapObjectAndShape(mapObject, shape);
    }

    private static float[] removeDuplicateVertices(float[] vertices, float minDistance, String objInfo) {
        float minDistSq = minDistance * minDistance;
        java.util.ArrayList<Float> cleaned = new java.util.ArrayList<>();
        int removed = 0;

        for (int i = 0; i < vertices.length; i += 2) {
            float x = vertices[i];
            float y = vertices[i + 1];
            boolean tooClose = false;

            // Check against already kept vertices
            for (int j = 0; j < cleaned.size(); j += 2) {
                float dx = x - cleaned.get(j);
                float dy = y - cleaned.get(j + 1);
                if (dx*dx + dy*dy < minDistSq) {
                    tooClose = true;
                    removed++;
                    break;
                }
            }

            if (!tooClose) {
                cleaned.add(x);
                cleaned.add(y);
            }
        }

        if (removed > 0) {
            Gdx.app.log("TiledPhysics", "Removed " + removed + " duplicate vertices from " + objInfo);
        }

        // Convert back to float array
        float[] result = new float[cleaned.size()];
        for (int i = 0; i < cleaned.size(); i++) {
            result[i] = cleaned.get(i);
        }
        return result;
    }

    private static FixtureDef fixtureDefOfMapObjectAndShape(MapObject mapObject, Shape shape) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = mapObject.getProperties().get("friction", 0f, Float.class);
        fixtureDef.restitution = mapObject.getProperties().get("restitution", 0f, Float.class);
        fixtureDef.density = mapObject.getProperties().get("density", 0f, Float.class);
        fixtureDef.isSensor = mapObject.getProperties().get("sensor", false, Boolean.class);
        return fixtureDef;
    }
}
