package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class DamageCooldown implements Component {
    public static final ComponentMapper<DamageCooldown> MAPPER = ComponentMapper.getFor(DamageCooldown.class);

    private float remainingSec;

    public DamageCooldown(float remainingSec) {
        this.remainingSec = remainingSec;
    }

    public void dec(float deltaTime) {
        remainingSec -= deltaTime;
    }

    public boolean isActive() {
        return remainingSec > 0f;
    }

    public void reset(float durationSec) {
        this.remainingSec = durationSec;
    }

    public float getRemainingSec() {
        return remainingSec;
    }
}
