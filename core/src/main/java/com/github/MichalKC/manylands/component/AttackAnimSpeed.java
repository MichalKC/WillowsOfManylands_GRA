package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class AttackAnimSpeed implements Component {
    public static final ComponentMapper<AttackAnimSpeed> MAPPER = ComponentMapper.getFor(AttackAnimSpeed.class);

    private float baseSpeed;

    public AttackAnimSpeed() {}
    public AttackAnimSpeed(float baseSpeed) { this.baseSpeed = baseSpeed; }

    public float getBaseSpeed() { return baseSpeed; }
    public void setBaseSpeed(float baseSpeed) { this.baseSpeed = baseSpeed; }
}
