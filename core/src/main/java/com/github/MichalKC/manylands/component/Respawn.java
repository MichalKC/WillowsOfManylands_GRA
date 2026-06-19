package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class Respawn implements Component {
    public static final ComponentMapper<Respawn> MAPPER = ComponentMapper.getFor(Respawn.class);

    private final float delaySec;
    private final Vector2 spawnPosition;
    private float timerSec;
    private boolean active;

    public Respawn(float delaySec, Vector2 spawnPosition) {
        this.delaySec = delaySec;
        this.spawnPosition = new Vector2(spawnPosition);
        this.timerSec = delaySec;
        this.active = false;
    }

    public float getDelaySec() {
        return delaySec;
    }

    public Vector2 getSpawnPosition() {
        return spawnPosition;
    }

    public void activate() {
        this.active = true;
        this.timerSec = delaySec;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void decTimer(float deltaSec) {
        this.timerSec = Math.max(0f, this.timerSec - deltaSec);
    }

    public boolean isReady() {
        return this.timerSec <= 0f;
    }

    public float getTimerSec() {
        return timerSec;
    }

    public void restoreCountdown(float timerSec) {
        this.active = true;
        this.timerSec = Math.max(0f, timerSec);
    }
}

