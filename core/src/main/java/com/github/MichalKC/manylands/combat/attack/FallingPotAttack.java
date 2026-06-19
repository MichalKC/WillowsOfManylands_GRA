package com.github.MichalKC.manylands.combat.attack;

import com.github.MichalKC.manylands.combat.AttackPattern;
import com.github.MichalKC.manylands.combat.CombatConstants;
import com.github.MichalKC.manylands.combat.CombatProjectile;

import java.util.List;

public class FallingPotAttack implements AttackPattern {
    private float nextSpawn;

    @Override public String getName() { return "fallingPot"; }
    @Override public float getDuration() { return 8.0f; }

    @Override
    public void reset() {
        nextSpawn = 0.8f;
    }

    @Override
    public void spawn(float t, float dt, List<CombatProjectile> projs) {
        if (t < nextSpawn) return;

        float x = CombatConstants.BOX_X + 20f + (float)(Math.random() * (CombatConstants.BOX_W - 40f));
        float w = 54f + (float)(Math.random() * 8f);
        float h = 42f + (float)(Math.random() * 8f);
        CombatProjectile pot = new CombatProjectile(
            x, CombatConstants.BOX_Y + CombatConstants.BOX_H + 40f,
            0f, -(180f + (float)(Math.random() * 90f)),
            w, h,
            CombatProjectile.Type.POT
        );
        pot.life = 0f;
        pot.maxLife = 3.5f;
        projs.add(pot);

        nextSpawn = t + 1.2f + (float)(Math.random() * 1.1f);
    }
}
