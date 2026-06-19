package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;

public class Life implements Component {
    public static final ComponentMapper<Life> MAPPER = ComponentMapper.getFor(Life.class);

    private float maxLife;
    private float life;
    private float lifePerSec;
    private float regenMultiplier;
    private int equipBonusMaxLife = 0;

    public Life(int maxLife, float lifePerSec) {
        this.maxLife = maxLife;
        this.life = maxLife;
        this.lifePerSec = lifePerSec;
        this.regenMultiplier = 1.0f;
    }

    public float getMaxLife() {
        return maxLife + equipBonusMaxLife;
    }

    public float getLife() {
        return life;
    }

    public void addLife(float value) {
        this.life = MathUtils.clamp(life + value, 0f, getMaxLife());
    }

    public void restore(float maxLife, float life) {
        this.maxLife = maxLife;
        this.life = MathUtils.clamp(life, 0f, getMaxLife());
    }

    public void setEquipMaxLifeBonus(int newBonus) {
        this.equipBonusMaxLife = newBonus;
        this.life = MathUtils.clamp(life, 0f, getMaxLife());
    }

    public int getEquipMaxLifeBonus() { return equipBonusMaxLife; }
    public float getBaseMaxLife()      { return maxLife; }

    public float getLifePerSec() {
        return lifePerSec;
    }

    public float getRegenMultiplier() {
        return regenMultiplier;
    }

    public void setRegenMultiplier(float multiplier) {
        this.regenMultiplier = Math.max(0f, multiplier);
    }
}
