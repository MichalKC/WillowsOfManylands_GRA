package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Damaged implements Component {
    public static final ComponentMapper<Damaged> MAPPER = ComponentMapper.getFor(Damaged.class);

    private float damage;
    private float cooldownOverrideSec;

    public Damaged(float damage) {
        this.damage = damage;
    }

    public Damaged(float damage, float cooldownOverrideSec) {
        this.damage = damage;
        this.cooldownOverrideSec = cooldownOverrideSec;
    }

    public void addDamage(float amount) {
        this.damage += amount;
    }

    public float getDamage() {
        return damage;
    }

    public float getCooldownOverrideSec() {
        return cooldownOverrideSec;
    }
}
