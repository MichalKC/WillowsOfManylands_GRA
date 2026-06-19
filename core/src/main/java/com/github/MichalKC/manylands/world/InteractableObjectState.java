package com.github.MichalKC.manylands.world;

import com.github.MichalKC.manylands.component.Animation2D.AnimationType;

/** Saved state of a chest / switch / other {@link com.github.MichalKC.manylands.component.Interactable} on a map. */
public final class InteractableObjectState {
    private final boolean activated;
    private final AnimationType animationType;
    private final int stage;
    private final boolean coinsDropped;

    public InteractableObjectState(boolean activated, AnimationType animationType) {
        this(activated, animationType, 1, false);
    }

    public InteractableObjectState(boolean activated, AnimationType animationType, int stage) {
        this(activated, animationType, stage, false);
    }

    public InteractableObjectState(boolean activated, AnimationType animationType, int stage, boolean coinsDropped) {
        this.activated = activated;
        this.animationType = animationType;
        this.stage = Math.max(1, stage);
        this.coinsDropped = coinsDropped;
    }

    public boolean isActivated() {
        return activated;
    }

    public AnimationType getAnimationType() {
        return animationType;
    }

    public int getStage() {
        return stage;
    }

    public boolean isCoinsDropped() {
        return coinsDropped;
    }
}
