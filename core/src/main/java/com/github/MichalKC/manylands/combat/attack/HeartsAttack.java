package com.github.MichalKC.manylands.combat.attack;

import com.github.MichalKC.manylands.combat.AttackPattern;
import com.github.MichalKC.manylands.combat.CombatConstants;
import com.github.MichalKC.manylands.combat.CombatProjectile;

import java.util.List;

public class HeartsAttack implements AttackPattern {
    private float nextSpawn;

    private static final float[][] CORNERS = {
        {CombatConstants.BOX_X, CombatConstants.BOX_Y},
        {CombatConstants.BOX_X + CombatConstants.BOX_W, CombatConstants.BOX_Y},
        {CombatConstants.BOX_X, CombatConstants.BOX_Y + CombatConstants.BOX_H},
        {CombatConstants.BOX_X + CombatConstants.BOX_W, CombatConstants.BOX_Y + CombatConstants.BOX_H},
    };

    @Override public String getName() { return "hearts"; }
    @Override public float getDuration() { return 7f; }

    @Override
    public void reset() {
        nextSpawn = 0.3f;
    }

    @Override
    public void spawn(float t, float dt, List<CombatProjectile> projs) {
        if (t < nextSpawn) return;
        float[] corner = CORNERS[(int)(Math.random() * 4)];
        float sx = corner[0], sy = corner[1];
        float tx = CombatConstants.BOX_X + 40f + (float)(Math.random() * (CombatConstants.BOX_W - 80f));
        float ty = CombatConstants.BOX_Y + 20f + (float)(Math.random() * (CombatConstants.BOX_H - 40f));
        float dx = tx - sx, dy = ty - sy;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len < 1f) len = 1f;
        float speed = 150f + (float)(Math.random() * 100f);
        projs.add(new CombatProjectile(sx, sy, (dx / len) * speed, (dy / len) * speed, 14f, 14f, CombatProjectile.Type.HEART));
        nextSpawn = t + 0.25f + (float)(Math.random() * 0.3f);
    }
}
