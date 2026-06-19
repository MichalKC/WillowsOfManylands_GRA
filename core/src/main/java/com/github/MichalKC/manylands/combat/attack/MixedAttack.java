package com.github.MichalKC.manylands.combat.attack;

import com.github.MichalKC.manylands.combat.AttackPattern;
import com.github.MichalKC.manylands.combat.CombatConstants;
import com.github.MichalKC.manylands.combat.CombatProjectile;

import java.util.List;

public class MixedAttack implements AttackPattern {
    private float nextBone;
    private float nextPellet;

    @Override public String getName() { return "mixed"; }
    @Override public float getDuration() { return 9f; }

    @Override
    public void reset() {
        nextBone = 0.5f;
        nextPellet = 0.2f;
    }

    @Override
    public void spawn(float t, float dt, List<CombatProjectile> projs) {
        if (t >= nextBone) {
            float y = CombatConstants.BOX_Y + 10f + (float)(Math.random() * (CombatConstants.BOX_H - 30f));
            projs.add(new CombatProjectile(CombatConstants.BOX_X - 20f, y, 220f, 0f, 14f, 26f, CombatProjectile.Type.BONE));
            nextBone = t + 0.55f;
        }
        if (t >= nextPellet) {
            float x = CombatConstants.BOX_X + 10f + (float)(Math.random() * (CombatConstants.BOX_W - 20f));
            projs.add(new CombatProjectile(x, CombatConstants.BOX_Y - 10f, 0f, 180f, 10f, 10f, CombatProjectile.Type.PELLET));
            nextPellet = t + 0.3f;
        }
    }
}
