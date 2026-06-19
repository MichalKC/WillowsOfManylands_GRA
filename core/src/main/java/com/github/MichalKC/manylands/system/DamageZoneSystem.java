package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.MichalKC.manylands.component.DamageCooldown;
import com.github.MichalKC.manylands.component.DamageZone;
import com.github.MichalKC.manylands.component.Damaged;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Gated;

public class DamageZoneSystem extends IteratingSystem {
    private static final float ZONE_DAMAGE_COOLDOWN_SEC = 1f;

    public DamageZoneSystem() {
        super(Family.all(DamageZone.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        DamageZone zone = DamageZone.MAPPER.get(entity);

        Gated gated = Gated.MAPPER.get(entity);
        if (gated != null && !GateEvaluator.isOpen(getEngine(), gated)) {
            return;
        }

        Entity player = zone.getOverlappingPlayer();
        if (player != null && Dead.MAPPER.get(player) == null) {
            applyDamage(player, zone.getDamage());
        }

        if (zone.isCanDamageEnemies()) {
            for (Entity enemy : zone.getOverlappingEnemies()) {
                if (Dead.MAPPER.get(enemy) == null) {
                    applyDamage(enemy, zone.getDamage());
                }
            }
        }
    }

    private void applyDamage(Entity entity, float damage) {
        DamageCooldown cooldown = DamageCooldown.MAPPER.get(entity);
        if (cooldown != null && cooldown.isActive()) return;
        Damaged damaged = Damaged.MAPPER.get(entity);
        if (damaged == null) {
            entity.add(new Damaged(damage, ZONE_DAMAGE_COOLDOWN_SEC));
        } else {
            damaged.addDamage(damage);
        }
    }
}
