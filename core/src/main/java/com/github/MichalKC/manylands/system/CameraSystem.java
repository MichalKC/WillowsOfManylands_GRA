package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.component.CameraFollow;
import com.github.MichalKC.manylands.component.Transform;

public class CameraSystem extends IteratingSystem {
    public static final float CAM_OFFSET_Y = 1f;
    public static final float CAM_OFFSET_X = 2f;

    private final Camera camera;
    private final Vector2 targetPosition;
    private final float smoothingFactor;
    private float mapW;
    private float mapH;
    private boolean snapNextFrame;

    public CameraSystem(Camera camera) {
        super(Family.all(CameraFollow.class, Transform.class).get());
        this.camera = camera;
        this.targetPosition = new Vector2();
        this.smoothingFactor = 4f;
    }

    public void requestSnap() {
        this.snapNextFrame = true;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Transform transform = Transform.MAPPER.get(entity);
        calcTargetPosition(transform.getPosition());

        if (snapNextFrame) {
            camera.position.set(this.targetPosition.x, this.targetPosition.y, camera.position.z);
            snapNextFrame = false;
        } else {
            float progress = smoothingFactor * deltaTime;
            float smoothedX = MathUtils.lerp(camera.position.x, this.targetPosition.x, progress);
            float smoothedY = MathUtils.lerp(camera.position.y, this.targetPosition.y, progress);
            camera.position.set(smoothedX, smoothedY, camera.position.z);
        }
    }

    private void calcTargetPosition(Vector2 entityPosition) {
        float targetX = entityPosition.x + CAM_OFFSET_X;
        float camHalfW = camera.viewportWidth * 0.5f;
        if (mapW > camHalfW) {
            float min = Math.min(camHalfW, mapW - camHalfW);
            float max = Math.max(camHalfW, mapW - camHalfW);
            targetX = MathUtils.clamp(targetX, min, max);
        }

        float targetY = entityPosition.y + CAM_OFFSET_Y;
        float camHalfH = camera.viewportHeight * 0.5f;
        if (mapH > camHalfH) {
            float min = Math.min(camHalfH, mapH - camHalfH);
            float max = Math.max(camHalfH, mapH - camHalfH);
            targetY = MathUtils.clamp(targetY, min, max);
        }
        this.targetPosition.set(targetX, targetY);
    }

    public void setMap(TiledMap tiledMap){
        Integer width = tiledMap.getProperties().get("width", 0, Integer.class);
        Integer tileW = tiledMap.getProperties().get("tilewidth", 0, Integer.class);
        Integer height = tiledMap.getProperties().get("height", 0, Integer.class);
        Integer tileH = tiledMap.getProperties().get("tileheight", 0, Integer.class);

        this.mapW = width * tileW * GdxGame.UNIT_SCALE;
        this.mapH = height * tileH * GdxGame.UNIT_SCALE;

        ImmutableArray<Entity> entities = getEntities();
        if (entities.size() == 0) {
            return;
        }

        Entity camEntity = entities.first();
        Transform transform = Transform.MAPPER.get(camEntity);
        calcTargetPosition(transform.getPosition());
        camera.position.set(this.targetPosition.x, this.targetPosition.y, camera.position.z);
    }
}
