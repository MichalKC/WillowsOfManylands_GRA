package com.github.MichalKC.manylands.combat.attack;

import com.github.MichalKC.manylands.combat.AttackPattern;
import com.github.MichalKC.manylands.combat.CombatConstants;
import com.github.MichalKC.manylands.combat.CombatProjectile;

import java.util.List;

public class BombsLasersAttack implements AttackPattern {
    private float nextBomb;
    private float nextLaser;
    private boolean spawnedLaser;

    @Override public String getName() { return "bombsLasers"; }
    @Override public float getDuration() { return 8f; }

    @Override
    public void reset() {
        nextBomb = 0.6f;
        nextLaser = 2.0f;
        spawnedLaser = false;
    }

    @Override
    public void spawn(float t, float dt, List<CombatProjectile> projs) {
        if (t >= nextBomb && t < 6f) {
            float x = CombatConstants.BOX_X + 20f + (float)(Math.random() * (CombatConstants.BOX_W - 40f));
            CombatProjectile bomb = new CombatProjectile(
                x, CombatConstants.BOX_Y + CombatConstants.BOX_H - 20f,
                0f, -80f, 20f, 20f,
                CombatProjectile.Type.HEART
            );
            bomb.life = 0f;
            bomb.maxLife = 5f;
            projs.add(bomb);
            nextBomb = t + 0.8f + (float)(Math.random() * 0.4f);
        }

        if (t >= nextLaser && !spawnedLaser) {
            int laserCount = 2 + (int)(Math.random() * 2);
            for (int i = 0; i < laserCount; i++) {
                boolean vertical = Math.random() < 0.5f;

                if (vertical) {
                    float x = CombatConstants.BOX_X + 30f + (float)(Math.random() * (CombatConstants.BOX_W - 60f));
                    CombatProjectile warning = new CombatProjectile(
                        x - 8f, CombatConstants.BOX_Y, 0f, 0f, 16f, CombatConstants.BOX_H,
                        CombatProjectile.Type.WARNING
                    );
                    warning.life = 0f;
                    warning.maxLife = 0.8f;
                    projs.add(warning);
                } else {
                    float y = CombatConstants.BOX_Y + 30f + (float)(Math.random() * (CombatConstants.BOX_H - 60f));
                    CombatProjectile warning = new CombatProjectile(
                        CombatConstants.BOX_X, y - 8f, 0f, 0f, CombatConstants.BOX_W, 16f,
                        CombatProjectile.Type.WARNING
                    );
                    warning.life = 0f;
                    warning.maxLife = 0.8f;
                    projs.add(warning);
                }
            }
            spawnedLaser = true;
        }
    }
}
