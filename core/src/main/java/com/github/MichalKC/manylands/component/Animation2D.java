package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.MichalKC.manylands.asset.AtlasAsset;
import com.github.MichalKC.manylands.component.Facing.FacingDirection;

public class Animation2D implements Component {
    public static final ComponentMapper<Animation2D> MAPPER = ComponentMapper.getFor(Animation2D.class);

    private final AtlasAsset atlasAsset;
    private final String atlasKey;
    private AnimationType type;
    private int stage;
    private FacingDirection direction;
    private PlayMode playMode;
    private float speed;
    private float stateTime;
    private Animation<TextureRegion> animation;
    private boolean dirty;

    public Animation2D(AtlasAsset atlasAsset,
                       String atlasKey,
                       AnimationType type,
                       PlayMode playMode,
                       float speed) {
        this.atlasAsset = atlasAsset;
        this.atlasKey = atlasKey;
        this.type = type;
        this.stage = 1;
        this.direction = null;
        this.playMode = playMode;
        this.speed = speed;
        this.stateTime = 0f;
        this.animation = null;
    }

    public void setAnimation(Animation<TextureRegion> animation, FacingDirection direction) {
        this.animation = animation;
        this.direction = direction;
        this.stateTime = 0f;
        this.dirty = false;
    }

    public FacingDirection getDirection() {
        return direction;
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public AtlasAsset getAtlasAsset() {
        return atlasAsset;
    }

    public String getAtlasKey() {
        return atlasKey;
    }

    public void setType(AnimationType type) {
        this.type = type;
        this.dirty = true;
    }

    public AnimationType getType() {
        return type;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        if (stage < 1) stage = 1;
        if (this.stage != stage) {
            this.stage = stage;
            this.dirty = true;
        }
    }

    public PlayMode getPlayMode() {
        return playMode;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isFinished() {
        return animation != null && animation.isAnimationFinished(stateTime);
    }

    public float incAndGetStateTime(float deltaTime) {
        this.stateTime += deltaTime * speed;
        return this.stateTime;
    }

    public enum AnimationType {
        IDLE, RUN, ATTACK, ATTACK2, SPECIAL, DAMAGED, DEAD, DROP,
        TO_ACTIVATED("toActivated"), ACTIVATED("activated"), TO_IDLE("toIdle");

        private final String atlasKey;

        AnimationType() {
            this.atlasKey = name().toLowerCase();
        }

        AnimationType(String atlasKey) {
            this.atlasKey = atlasKey;
        }

        public String getAtlasKey() {
            return atlasKey;
        }
    }
}
