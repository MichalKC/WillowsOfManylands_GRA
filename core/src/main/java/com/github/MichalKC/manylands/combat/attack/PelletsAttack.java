package com.github.MichalKC.manylands.combat.attack;

import com.github.MichalKC.manylands.combat.AttackPattern;
import com.github.MichalKC.manylands.combat.CombatConstants;
import com.github.MichalKC.manylands.combat.CombatProjectile;

import java.util.List;

public class PelletsAttack implements AttackPattern {
    private float nextSpawn;

    @Override public String getName() { return "pellets"; }
    @Override public float getDuration() { return 6.5f; }

    @Override
    public void reset() {
        nextSpawn = 0.2f;
    }

    @Override
    public void spawn(float t, float dt, List<CombatProjectile> projs) {
        if (t < nextSpawn) return;
        float x = CombatConstants.BOX_X + 10f + (float)(Math.random() * (CombatConstants.BOX_W - 20f));
        float vy = 140f + (float)(Math.random() * 80f);
        float vx = (float)((Math.random() - 0.5) * 40.0);
        projs.add(new CombatProjectile(x, CombatConstants.BOX_Y - 10f, vx, vy, 10f, 10f, CombatProjectile.Type.PELLET));
        nextSpawn = t + 0.18f + (float)(Math.random() * 0.22f);
    }
}
