package com.github.MichalKC.manylands;

import com.github.MichalKC.manylands.ai.AnimationState;
import com.github.MichalKC.manylands.component.Facing.FacingDirection;

public final class PlayerSessionState {
    private final float maxLife;
    private final float life;
    private final float lifePerSec;
    private final FacingDirection facing;
    private final AnimationState animationState;
    private final boolean dead;
    private final boolean respawnActive;
    private final float respawnTimerSec;
    private final float damageCooldownRemainingSec;
    private final float spawnPositionX;
    private final float spawnPositionY;
    private final int coins;
    private final com.github.MichalKC.manylands.component.Item[] inventorySlots;
    private final com.github.MichalKC.manylands.component.Item[] equipSlots;
    private final float specialCharge;
    private final boolean specialDraining;
    private final java.util.Set<Integer> unlockedSkills;
    private final int persistedActivatedSkillSlot;
    // Active skills timers (remainings)
    private final float skill1ActiveRemaining;
    private final float skill1CooldownRemaining;
    private final float skill2ActiveRemaining;
    private final float skill2CooldownRemaining;
    private final float skill3ActiveRemaining;
    private final float skill3CooldownRemaining;
    private final float skill4ActiveRemaining;
    private final float skill4CooldownRemaining;
    private final float skill5ActiveRemaining;
    private final float skill5CooldownRemaining;
    private final float skill6ActiveRemaining;
    private final float skill6CooldownRemaining;
    private final float skill7ActiveRemaining;
    private final float skill7CooldownRemaining;
    private final float skill8ActiveRemaining;
    private final float skill8CooldownRemaining;

    public PlayerSessionState(
        float maxLife,
        float life,
        float lifePerSec,
        FacingDirection facing,
        AnimationState animationState,
        boolean dead,
        boolean respawnActive,
        float respawnTimerSec,
        float damageCooldownRemainingSec,
        float spawnPositionX,
        float spawnPositionY
    ) {
        this(maxLife, life, lifePerSec, facing, animationState, dead, respawnActive,
            respawnTimerSec, damageCooldownRemainingSec, spawnPositionX, spawnPositionY, 0, null, null, 0f, false);
    }

    public PlayerSessionState(
        float maxLife,
        float life,
        float lifePerSec,
        FacingDirection facing,
        AnimationState animationState,
        boolean dead,
        boolean respawnActive,
        float respawnTimerSec,
        float damageCooldownRemainingSec,
        float spawnPositionX,
        float spawnPositionY,
        int coins
    ) {
        this(maxLife, life, lifePerSec, facing, animationState, dead, respawnActive,
            respawnTimerSec, damageCooldownRemainingSec, spawnPositionX, spawnPositionY, coins, null, null, 0f, false);
    }

    public PlayerSessionState(
        float maxLife,
        float life,
        float lifePerSec,
        FacingDirection facing,
        AnimationState animationState,
        boolean dead,
        boolean respawnActive,
        float respawnTimerSec,
        float damageCooldownRemainingSec,
        float spawnPositionX,
        float spawnPositionY,
        int coins,
        com.github.MichalKC.manylands.component.Item[] inventorySlots
    ) {
        this(maxLife, life, lifePerSec, facing, animationState, dead, respawnActive,
            respawnTimerSec, damageCooldownRemainingSec, spawnPositionX, spawnPositionY, coins, inventorySlots, null, 0f, false);
    }

    public PlayerSessionState(
        float maxLife,
        float life,
        float lifePerSec,
        FacingDirection facing,
        AnimationState animationState,
        boolean dead,
        boolean respawnActive,
        float respawnTimerSec,
        float damageCooldownRemainingSec,
        float spawnPositionX,
        float spawnPositionY,
        int coins,
        com.github.MichalKC.manylands.component.Item[] inventorySlots,
        com.github.MichalKC.manylands.component.Item[] equipSlots,
        float specialCharge,
        boolean specialDraining
    ) {
        this(maxLife, life, lifePerSec, facing, animationState, dead, respawnActive,
            respawnTimerSec, damageCooldownRemainingSec, spawnPositionX, spawnPositionY,
            coins, inventorySlots, equipSlots, specialCharge, specialDraining, null, -1);
    }

    public PlayerSessionState(
        float maxLife,
        float life,
        float lifePerSec,
        FacingDirection facing,
        AnimationState animationState,
        boolean dead,
        boolean respawnActive,
        float respawnTimerSec,
        float damageCooldownRemainingSec,
        float spawnPositionX,
        float spawnPositionY,
        int coins,
        com.github.MichalKC.manylands.component.Item[] inventorySlots,
        com.github.MichalKC.manylands.component.Item[] equipSlots,
        float specialCharge,
        boolean specialDraining,
        java.util.Set<Integer> unlockedSkills,
        int persistedActivatedSkillSlot
    ) {
        this(
            maxLife, life, lifePerSec, facing, animationState, dead, respawnActive, respawnTimerSec,
            damageCooldownRemainingSec, spawnPositionX, spawnPositionY, coins, inventorySlots, equipSlots,
            specialCharge, specialDraining, unlockedSkills, persistedActivatedSkillSlot,
            0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f,
            0f, 0f
        );
    }

    public PlayerSessionState(
        float maxLife,
        float life,
        float lifePerSec,
        FacingDirection facing,
        AnimationState animationState,
        boolean dead,
        boolean respawnActive,
        float respawnTimerSec,
        float damageCooldownRemainingSec,
        float spawnPositionX,
        float spawnPositionY,
        int coins,
        com.github.MichalKC.manylands.component.Item[] inventorySlots,
        com.github.MichalKC.manylands.component.Item[] equipSlots,
        float specialCharge,
        boolean specialDraining,
        java.util.Set<Integer> unlockedSkills,
        int persistedActivatedSkillSlot,
        float skill1ActiveRemaining,
        float skill1CooldownRemaining,
        float skill2ActiveRemaining,
        float skill2CooldownRemaining,
        float skill3ActiveRemaining,
        float skill3CooldownRemaining,
        float skill4ActiveRemaining,
        float skill4CooldownRemaining,
        float skill5ActiveRemaining,
        float skill5CooldownRemaining,
        float skill6ActiveRemaining,
        float skill6CooldownRemaining,
        float skill7ActiveRemaining,
        float skill7CooldownRemaining,
        float skill8ActiveRemaining,
        float skill8CooldownRemaining
    ) {
        this.maxLife = maxLife;
        this.life = life;
        this.lifePerSec = lifePerSec;
        this.facing = facing;
        this.animationState = animationState;
        this.dead = dead;
        this.respawnActive = respawnActive;
        this.respawnTimerSec = respawnTimerSec;
        this.damageCooldownRemainingSec = damageCooldownRemainingSec;
        this.spawnPositionX = spawnPositionX;
        this.spawnPositionY = spawnPositionY;
        this.coins = Math.max(0, coins);
        this.inventorySlots = inventorySlots != null ? inventorySlots : new com.github.MichalKC.manylands.component.Item[16];
        this.equipSlots = equipSlots;
        this.specialCharge = Math.max(0f, Math.min(1f, specialCharge));
        this.specialDraining = specialDraining;
        this.unlockedSkills = unlockedSkills != null ? unlockedSkills : new java.util.HashSet<>();
        this.persistedActivatedSkillSlot = persistedActivatedSkillSlot;
        this.skill1ActiveRemaining = Math.max(0f, skill1ActiveRemaining);
        this.skill1CooldownRemaining = Math.max(0f, skill1CooldownRemaining);
        this.skill2ActiveRemaining = Math.max(0f, skill2ActiveRemaining);
        this.skill2CooldownRemaining = Math.max(0f, skill2CooldownRemaining);
        this.skill3ActiveRemaining = Math.max(0f, skill3ActiveRemaining);
        this.skill3CooldownRemaining = Math.max(0f, skill3CooldownRemaining);
        this.skill4ActiveRemaining = Math.max(0f, skill4ActiveRemaining);
        this.skill4CooldownRemaining = Math.max(0f, skill4CooldownRemaining);
        this.skill5ActiveRemaining = Math.max(0f, skill5ActiveRemaining);
        this.skill5CooldownRemaining = Math.max(0f, skill5CooldownRemaining);
        this.skill6ActiveRemaining = Math.max(0f, skill6ActiveRemaining);
        this.skill6CooldownRemaining = Math.max(0f, skill6CooldownRemaining);
        this.skill7ActiveRemaining = Math.max(0f, skill7ActiveRemaining);
        this.skill7CooldownRemaining = Math.max(0f, skill7CooldownRemaining);
        this.skill8ActiveRemaining = Math.max(0f, skill8ActiveRemaining);
        this.skill8CooldownRemaining = Math.max(0f, skill8CooldownRemaining);
    }

    public com.github.MichalKC.manylands.component.Item[] getInventorySlots() {
        return inventorySlots;
    }

    public com.github.MichalKC.manylands.component.Item[] getEquipSlots() {
        return equipSlots;
    }

    public int getCoins() {
        return coins;
    }

    public float getMaxLife() {
        return maxLife;
    }

    public float getLife() {
        return life;
    }

    public float getLifePerSec() {
        return lifePerSec;
    }

    public FacingDirection getFacing() {
        return facing;
    }

    public AnimationState getAnimationState() {
        return animationState;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isRespawnActive() {
        return respawnActive;
    }

    public float getRespawnTimerSec() {
        return respawnTimerSec;
    }

    public float getDamageCooldownRemainingSec() {
        return damageCooldownRemainingSec;
    }

    public float getSpawnPositionX() {
        return spawnPositionX;
    }

    public float getSpawnPositionY() {
        return spawnPositionY;
    }

    public PlayerSessionState withSpawnPosition(float x, float y) {
        return new PlayerSessionState(
            maxLife, life, lifePerSec, facing, animationState,
            dead, respawnActive, respawnTimerSec, damageCooldownRemainingSec,
            x, y, coins, inventorySlots, equipSlots, specialCharge, specialDraining,
            unlockedSkills, persistedActivatedSkillSlot,
            skill1ActiveRemaining, skill1CooldownRemaining,
            skill2ActiveRemaining, skill2CooldownRemaining,
            skill3ActiveRemaining, skill3CooldownRemaining,
            skill4ActiveRemaining, skill4CooldownRemaining,
            skill5ActiveRemaining, skill5CooldownRemaining,
            skill6ActiveRemaining, skill6CooldownRemaining,
            skill7ActiveRemaining, skill7CooldownRemaining,
            skill8ActiveRemaining, skill8CooldownRemaining
        );
    }

    public PlayerSessionState withLife(float newLife) {
        return new PlayerSessionState(
            maxLife, newLife, lifePerSec, facing, animationState,
            dead, respawnActive, respawnTimerSec, damageCooldownRemainingSec,
            spawnPositionX, spawnPositionY, coins, inventorySlots, equipSlots, specialCharge, specialDraining,
            unlockedSkills, persistedActivatedSkillSlot,
            skill1ActiveRemaining, skill1CooldownRemaining,
            skill2ActiveRemaining, skill2CooldownRemaining,
            skill3ActiveRemaining, skill3CooldownRemaining,
            skill4ActiveRemaining, skill4CooldownRemaining,
            skill5ActiveRemaining, skill5CooldownRemaining,
            skill6ActiveRemaining, skill6CooldownRemaining,
            skill7ActiveRemaining, skill7CooldownRemaining,
            skill8ActiveRemaining, skill8CooldownRemaining
        );
    }

    public float getSpecialCharge() { return specialCharge; }
    public boolean isSpecialDraining() { return specialDraining; }

    public java.util.Set<Integer> getUnlockedSkills() {
        return unlockedSkills;
    }

    public int getPersistedActivatedSkillSlot() {
        return persistedActivatedSkillSlot;
    }

    public static PlayerSessionState clone(PlayerSessionState state) {
        if (state == null) return null;
        com.github.MichalKC.manylands.component.Item[] invCopy = state.getInventorySlots() != null ? state.getInventorySlots().clone() : null;
        com.github.MichalKC.manylands.component.Item[] equipCopy = state.getEquipSlots() != null ? state.getEquipSlots().clone() : null;
        java.util.Set<Integer> skillsCopy = state.getUnlockedSkills() != null ? new java.util.HashSet<>(state.getUnlockedSkills()) : new java.util.HashSet<>();
        return new PlayerSessionState(
            state.getMaxLife(),
            state.getLife(),
            state.getLifePerSec(),
            state.getFacing(),
            state.getAnimationState(),
            state.isDead(),
            state.isRespawnActive(),
            state.getRespawnTimerSec(),
            state.getDamageCooldownRemainingSec(),
            state.getSpawnPositionX(),
            state.getSpawnPositionY(),
            state.getCoins(),
            invCopy,
            equipCopy,
            state.getSpecialCharge(),
            state.isSpecialDraining(),
            skillsCopy,
            state.getPersistedActivatedSkillSlot(),
            state.getSkill1ActiveRemaining(), state.getSkill1CooldownRemaining(),
            state.getSkill2ActiveRemaining(), state.getSkill2CooldownRemaining(),
            state.getSkill3ActiveRemaining(), state.getSkill3CooldownRemaining(),
            state.getSkill4ActiveRemaining(), state.getSkill4CooldownRemaining(),
            state.getSkill5ActiveRemaining(), state.getSkill5CooldownRemaining(),
            state.getSkill6ActiveRemaining(), state.getSkill6CooldownRemaining(),
            state.getSkill7ActiveRemaining(), state.getSkill7CooldownRemaining(),
            state.getSkill8ActiveRemaining(), state.getSkill8CooldownRemaining()
        );
    }

    public float getSkill1ActiveRemaining() { return skill1ActiveRemaining; }
    public float getSkill1CooldownRemaining() { return skill1CooldownRemaining; }
    public float getSkill2ActiveRemaining() { return skill2ActiveRemaining; }
    public float getSkill2CooldownRemaining() { return skill2CooldownRemaining; }
    public float getSkill3ActiveRemaining() { return skill3ActiveRemaining; }
    public float getSkill3CooldownRemaining() { return skill3CooldownRemaining; }
    public float getSkill4ActiveRemaining() { return skill4ActiveRemaining; }
    public float getSkill4CooldownRemaining() { return skill4CooldownRemaining; }
    public float getSkill5ActiveRemaining() { return skill5ActiveRemaining; }
    public float getSkill5CooldownRemaining() { return skill5CooldownRemaining; }
    public float getSkill6ActiveRemaining() { return skill6ActiveRemaining; }
    public float getSkill6CooldownRemaining() { return skill6CooldownRemaining; }
    public float getSkill7ActiveRemaining() { return skill7ActiveRemaining; }
    public float getSkill7CooldownRemaining() { return skill7CooldownRemaining; }
    public float getSkill8ActiveRemaining() { return skill8ActiveRemaining; }
    public float getSkill8CooldownRemaining() { return skill8CooldownRemaining; }
}
