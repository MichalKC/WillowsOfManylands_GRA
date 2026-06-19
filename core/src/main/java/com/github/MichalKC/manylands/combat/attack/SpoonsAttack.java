package com.github.MichalKC.manylands.combat.attack;

import com.github.MichalKC.manylands.combat.AttackPattern;
import com.github.MichalKC.manylands.combat.CombatConstants;
import com.github.MichalKC.manylands.combat.CombatProjectile;

import java.util.List;

public class SpoonsAttack implements AttackPattern {
    private float nextSpawn;

    @Override public String getName() { return "spoons"; }
    @Override public float getDuration() { return 7.5f; }

    @Override
    public void reset() {
        nextSpawn = 0.3f;
    }

    @Override
    public void spawn(float t, float dt, List<CombatProjectile> projs) {
        if (t < nextSpawn) return;

        if (Math.random() < 0.7) {
            // Drop spoons from the top
            float x = CombatConstants.BOX_X + 14f + (float)(Math.random() * (CombatConstants.BOX_W - 28f));
            float y = CombatConstants.BOX_Y + CombatConstants.BOX_H + 20f;
            float vy = -(160f + (float)(Math.random() * 140f));
            float wobble = (float)((Math.random() - 0.5) * 30.0);
            float w = 10f + (float)(Math.random() * 4f);
            float h = 18f + (float)(Math.random() * 6f);
            projs.add(new CombatProjectile(x, y, wobble, vy, w, h, CombatProjectile.Type.SPOON));
        } else {
            // Shoot spoons horizontally from sides
            boolean fromLeft = Math.random() < 0.5f;
            float x = fromLeft ? CombatConstants.BOX_X - 18f : CombatConstants.BOX_X + CombatConstants.BOX_W + 18f;
            float y = CombatConstants.BOX_Y + 8f + (float)(Math.random() * (CombatConstants.BOX_H - 16f));
            float vx = (fromLeft ? 170f : -170f) + (float)((Math.random() - 0.5) * 60.0);
            float vy = (float)((Math.random() - 0.5) * 60.0);
            float w = 18f + (float)(Math.random() * 4f);
            float h = 8f + (float)(Math.random() * 3f);
            projs.add(new CombatProjectile(x, y, vx, vy, w, h, CombatProjectile.Type.SPOON));
        }

        nextSpawn = t + 0.18f + (float)(Math.random() * 0.28f);
    }
}
