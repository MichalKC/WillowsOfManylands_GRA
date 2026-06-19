package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

public class CombatTrigger implements Component {
    public static final ComponentMapper<CombatTrigger> MAPPER = ComponentMapper.getFor(CombatTrigger.class);

    private final String enemyId;
    private Entity overlappingPlayer;
    private boolean playerInside;
    private boolean requireExit;
    private float cooldown;

    public CombatTrigger(String enemyId) {
        this.enemyId = enemyId;
    }

    public String getEnemyId() { return enemyId; }

    public Entity getOverlappingPlayer() { return overlappingPlayer; }
    public void setOverlappingPlayer(Entity player) { this.overlappingPlayer = player; }

    public boolean isPlayerInside() { return playerInside; }
    public void setPlayerInside(boolean inside) { this.playerInside = inside; }

    public boolean isRequireExit() { return requireExit; }
    public void setRequireExit(boolean requireExit) { this.requireExit = requireExit; }

    public float getCooldown() { return cooldown; }
    public void setCooldown(float cooldown) { this.cooldown = cooldown; }
}
