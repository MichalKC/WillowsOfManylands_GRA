package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Interactable implements Component {
    public static final ComponentMapper<Interactable> MAPPER = ComponentMapper.getFor(Interactable.class);

    private final float radiusSq;
    private final int activatedCount;
    private boolean activated;
    private int activatedStage;

    public Interactable(float interactionRadiusWorld) {
        this(interactionRadiusWorld, 1);
    }

    public Interactable(float interactionRadiusWorld, int activatedCount) {
        this.radiusSq = interactionRadiusWorld * interactionRadiusWorld;
        this.activatedCount = Math.max(1, activatedCount);
        this.activated = false;
        this.activatedStage = 0;
    }

    public float getRadiusSq() {
        return radiusSq;
    }

    public int getActivatedCount() {
        return activatedCount;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public int getActivatedStage() {
        return activatedStage;
    }

    public void setActivatedStage(int activatedStage) {
        if (activatedStage < 0) activatedStage = 0;
        this.activatedStage = activatedStage;
    }
}
