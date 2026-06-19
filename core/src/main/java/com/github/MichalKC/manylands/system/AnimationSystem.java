package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.MichalKC.manylands.asset.AssetService;
import com.github.MichalKC.manylands.asset.AtlasAsset;
import com.github.MichalKC.manylands.component.Animation2D;
import com.github.MichalKC.manylands.component.Animation2D.AnimationType;
import com.github.MichalKC.manylands.component.Facing;
import com.github.MichalKC.manylands.component.Facing.FacingDirection;
import com.github.MichalKC.manylands.component.Graphic;
import com.github.MichalKC.manylands.component.Npc;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.FreezeOnDeadPose;

import java.util.HashMap;
import java.util.Map;

public class AnimationSystem extends IteratingSystem {
    private static final float FRAME_DURATION = 1 / 8f;

    private final AssetService assetService;
    private final Map<CacheKey, Animation<TextureRegion>> animationCache;

    public AnimationSystem(AssetService assetService) {
        super(Family.all(Animation2D.class, Graphic.class, Facing.class).get());
        this.assetService = assetService;
        this.animationCache = new HashMap<>();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Animation2D animation2D = Animation2D.MAPPER.get(entity);
        FacingDirection facingDirection = Facing.MAPPER.get(entity).getDirection();

        boolean freezeDeadNpc =
            Npc.MAPPER.get(entity) != null &&
            Dead.MAPPER.get(entity) != null &&
            FreezeOnDeadPose.MAPPER.get(entity) != null &&
            animation2D.getType() == Animation2D.AnimationType.DEAD;

        final float stateTime;
        if (animation2D.isDirty() || facingDirection != animation2D.getDirection()) {
            updateAnimation(animation2D, facingDirection);
            stateTime = 0f;
        } else if (!freezeDeadNpc) {
            stateTime = animation2D.incAndGetStateTime(deltaTime);
        } else {
            stateTime = 0f;
        }

        Animation<TextureRegion> animation = animation2D.getAnimation();
        animation.setPlayMode(animation2D.getPlayMode());
        TextureRegion keyFrame;
        if (freezeDeadNpc) {
            keyFrame = animation.getKeyFrame(animation.getAnimationDuration());
        } else {
            keyFrame = animation.getKeyFrame(stateTime);
        }
        Graphic.MAPPER.get(entity).setRegion(keyFrame);
    }

    private void updateAnimation(Animation2D animation2D, FacingDirection facingDirection) {
        AtlasAsset atlasAsset = animation2D.getAtlasAsset();
        String atlasKey = animation2D.getAtlasKey();
        AnimationType type = animation2D.getType();
        int stage = stageForType(type, animation2D.getStage());
        CacheKey cacheKey = new CacheKey(atlasAsset, atlasKey, type, stage, facingDirection);

        Animation<TextureRegion> animation = animationCache.computeIfAbsent(cacheKey, key -> {
            TextureAtlas textureAtlas = this.assetService.get(atlasAsset);
            String stageSuffix = stage > 1 ? Integer.toString(stage) : "";
            String combinedKey = atlasKey + "/" + type.getAtlasKey() + stageSuffix + "_" + facingDirection.getAtlasKey();
            Array<TextureAtlas.AtlasRegion> regions = textureAtlas.findRegions(combinedKey);
            if(regions.isEmpty()) {
                throw new GdxRuntimeException("No regions found for key: " + combinedKey);
            }
            return new Animation<>(FRAME_DURATION, regions);
        });
        animation2D.setAnimation(animation, facingDirection);
    }

    private static int stageForType(AnimationType type, int stage) {
        if (type == AnimationType.TO_ACTIVATED || type == AnimationType.ACTIVATED) {
            return Math.max(1, stage);
        }
        return 1;
    }

    public record CacheKey(
        AtlasAsset atlasAsset,
        String atlasKey,
        Animation2D.AnimationType type,
        int stage,
        Facing.FacingDirection direction
    ) {

    }
}
