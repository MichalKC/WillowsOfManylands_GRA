package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

/**
 * Heal zone component for trigger layer objects.
 * When a player (not enemies!) enters this zone, their health regeneration is multiplied.
 * The heal multiplier is read from the 'heal' property in Tiled (float).
 * Supports gating via requiresActivated property.
 */
public class HealZone implements Component {
    public static final ComponentMapper<HealZone> MAPPER = ComponentMapper.getFor(HealZone.class);

    private final float healMultiplier;
    private Entity overlappingPlayer;

    public HealZone(float healMultiplier) {
        this.healMultiplier = Math.max(0f, healMultiplier);
        this.overlappingPlayer = null;
    }

    public float getHealMultiplier() {
        return healMultiplier;
    }

    public void setOverlappingPlayer(Entity player) {
        this.overlappingPlayer = player;
    }

    public Entity getOverlappingPlayer() {
        return overlappingPlayer;
    }
}
