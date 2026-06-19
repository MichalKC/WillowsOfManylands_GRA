package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.MichalKC.manylands.component.DamageCooldown;

public class DamageCooldownSystem extends IteratingSystem {
    public DamageCooldownSystem() {
        super(Family.all(DamageCooldown.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        DamageCooldown cooldown = DamageCooldown.MAPPER.get(entity);
        cooldown.dec(deltaTime);
        if (!cooldown.isActive()) {
            entity.remove(DamageCooldown.class);
        }
    }
}
