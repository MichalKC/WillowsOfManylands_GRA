package com.github.MichalKC.manylands.combat.attack;

import com.github.MichalKC.manylands.combat.AttackPattern;
import com.github.MichalKC.manylands.combat.CombatConstants;
import com.github.MichalKC.manylands.combat.CombatProjectile;

import java.util.List;

public class LasersAttack implements AttackPattern {
    private float nextSpawn;

    @Override public String getName() { return "lasers"; }
    @Override public float getDuration() { return 7f; }

    @Override
    public void reset() {
        nextSpawn = 0.5f;
    }

    @Override
    public void spawn(float t, float dt, List<CombatProjectile> projs) {
        if (t < nextSpawn) return;

        boolean vertical = Math.random() < 0.5f;

        if (vertical) {
            int beams = 4; // vertical: 4 beams
            float left = CombatConstants.BOX_X + 20f;
            float right = CombatConstants.BOX_X + CombatConstants.BOX_W - 20f;
            for (int i = 0; i < beams; i++) {
                float x = left + (float)(Math.random() * Math.max(1f, (right - left)));
                CombatProjectile warning = new CombatProjectile(
                    x - 8f, CombatConstants.BOX_Y, 0f, 0f, 16f, CombatConstants.BOX_H,
                    CombatProjectile.Type.WARNING
                );
                warning.life = 0f;
                warning.maxLife = 0.8f;
                projs.add(warning);
            }
        } else {
            int beams = 2; // horizontal: 2 beams
            float bottom = CombatConstants.BOX_Y + 20f;
            float top = CombatConstants.BOX_Y + CombatConstants.BOX_H - 20f;
            for (int i = 0; i < beams; i++) {
                float y = bottom + (float)(Math.random() * Math.max(1f, (top - bottom)));
                CombatProjectile warning = new CombatProjectile(
                    CombatConstants.BOX_X, y - 8f, 0f, 0f, CombatConstants.BOX_W, 16f,
                    CombatProjectile.Type.WARNING
                );
                warning.life = 0f;
                warning.maxLife = 0.8f;
                projs.add(warning);
            }
        }

        nextSpawn = t + 1.1f + (float)(Math.random() * 0.6f);
    }
}
