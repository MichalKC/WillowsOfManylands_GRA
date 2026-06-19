package com.github.MichalKC.manylands.combat.attack;

import com.github.MichalKC.manylands.combat.AttackPattern;
import com.github.MichalKC.manylands.combat.CombatConstants;
import com.github.MichalKC.manylands.combat.CombatProjectile;

import java.util.List;

public class KnifeBarrageAttack implements AttackPattern {
    private float nextVolley;

    @Override public String getName() { return "knives"; }
    @Override public float getDuration() { return 8.0f; }

    @Override
    public void reset() {
        nextVolley = 0.7f;
    }

    @Override
    public void spawn(float t, float dt, List<CombatProjectile> projs) {
        if (t < nextVolley) return;

        // Aim roughly from top corners towards box center like a bow shooting knives
        int shots = 3 + (int)(Math.random() * 3);
        for (int i = 0; i < shots; i++) {
            boolean fromLeft = Math.random() < 0.5f;
            float sx = fromLeft ? CombatConstants.BOX_X - 16f : CombatConstants.BOX_X + CombatConstants.BOX_W + 16f;
            float sy = CombatConstants.BOX_Y + CombatConstants.BOX_H - 8f - i * 8f + (float)((Math.random() - 0.5) * 10.0);
            float tx = CombatConstants.BOX_X + CombatConstants.BOX_W / 2f + (float)((Math.random() - 0.5) * 80.0);
            float ty = CombatConstants.BOX_Y + 20f + (float)(Math.random() * (CombatConstants.BOX_H - 40f));
            float dx = tx - sx, dy = ty - sy;
            float len = (float)Math.sqrt(dx * dx + dy * dy);
            float speed = 230f + (float)(Math.random() * 70f);
            float vx = dx / len * speed;
            float vy = dy / len * speed;
            projs.add(new CombatProjectile(sx, sy, vx, vy, 24f, 8f, CombatProjectile.Type.KNIFE));
        }

        nextVolley = t + 1.0f + (float)(Math.random() * 0.6f);
    }
}
