package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.MichalKC.manylands.asset.SoundAsset;

public class Attack implements Component {
    public static final ComponentMapper<Attack> MAPPER = ComponentMapper.getFor(Attack.class);

    public enum AttackType {
        PRIMARY("attack_sensor_"),
        SECONDARY("attack2_sensor_"),
        SPECIAL("special_sensor_");

        private final String sensorPrefix;

        AttackType(String sensorPrefix) {
            this.sensorPrefix = sensorPrefix;
        }

        public String getSensorPrefix() {
            return sensorPrefix;
        }
    }

    private record AttackProfile(float damage, float damageDelay, float lockDuration) {
    }

    private final AttackProfile primaryProfile;
    private final AttackProfile secondaryProfile;
    private final AttackProfile specialProfile;
    private SoundAsset sfx;
    private boolean attacking;
    private float equipBonusPrimary = 0f;
    private float equipBonusSecondary = 0f;
    private float equipBonusSpecial = 0f;
    private AttackType activeAttackType;

    private float elapsedInAttack;
    private boolean damageDealt;

    public Attack(float damage,
                  float damageDelay,
                  float lockDuration,
                  float secondaryDamage,
                  float secondaryDamageDelay,
                  float secondaryLockDuration,
                  float specialDamage,
                  float specialDamageDelay,
                  float specialLockDuration,
                  SoundAsset sfx) {
        this.primaryProfile = new AttackProfile(damage, damageDelay, Math.max(lockDuration, damageDelay));
        this.secondaryProfile = new AttackProfile(secondaryDamage, secondaryDamageDelay, Math.max(secondaryLockDuration, secondaryDamageDelay));
        this.specialProfile = new AttackProfile(specialDamage, specialDamageDelay, Math.max(specialLockDuration, specialDamageDelay));
        this.sfx = sfx;
        this.attacking = false;
        this.activeAttackType = AttackType.PRIMARY;
        this.elapsedInAttack = 0f;
        this.damageDealt = false;
    }

    public boolean canAttack() {
        return !attacking;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void startAttack(AttackType attackType) {
        this.attacking = true;
        this.activeAttackType = attackType;
        this.elapsedInAttack = 0f;
        this.damageDealt = false;
    }

    public void advanceElapsed(float deltaTime) {
        if (!attacking) {
            return;
        }
        elapsedInAttack += deltaTime;
    }

    public boolean consumeDamageMoment() {
        if (!attacking || damageDealt) {
            return false;
        }
        if (elapsedInAttack >= activeProfile().damageDelay()) {
            damageDealt = true;
            return true;
        }
        return false;
    }

    public boolean finishAttackIfLockExpired() {
        if (!attacking) {
            return false;
        }
        if (elapsedInAttack >= activeProfile().lockDuration()) {
            attacking = false;
            activeAttackType = AttackType.PRIMARY;
            elapsedInAttack = 0f;
            damageDealt = false;
            return true;
        }
        return false;
    }

    public boolean isFreshAttackTick() {
        return attacking && elapsedInAttack == 0f;
    }

    public void resetAttackTimer() {
        attacking = false;
        activeAttackType = AttackType.PRIMARY;
        elapsedInAttack = 0f;
        damageDealt = false;
    }

    public float getDamage() {
        float bonus = switch (activeAttackType) {
            case SECONDARY -> equipBonusSecondary;
            case SPECIAL -> equipBonusSpecial;
            default -> equipBonusPrimary;
        };
        return activeProfile().damage() + bonus;
    }

    public float getPrimaryDamage() {
        return primaryProfile.damage() + equipBonusPrimary;
    }

    public float getSecondaryDamage() {
        return secondaryProfile.damage() + equipBonusSecondary;
    }

    public float getSpecialDamage() {
        return specialProfile.damage() + equipBonusSpecial;
    }

    public void setEquipBonusPrimary(float bonus)   { this.equipBonusPrimary = bonus; }
    public void setEquipBonusSecondary(float bonus) { this.equipBonusSecondary = bonus; }
    public void setEquipBonusSpecial(float bonus)  { this.equipBonusSpecial = bonus; }

    public SoundAsset getSfx() {
        return sfx;
    }

    public AttackType getActiveAttackType() {
        return activeAttackType;
    }

    private AttackProfile activeProfile() {
        return switch (activeAttackType) {
            case SECONDARY -> secondaryProfile;
            case SPECIAL -> specialProfile;
            default -> primaryProfile;
        };
    }
}
