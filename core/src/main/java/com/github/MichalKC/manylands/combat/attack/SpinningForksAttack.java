package com.github.MichalKC.manylands.combat.attack;

import com.github.MichalKC.manylands.combat.AttackPattern;
import com.github.MichalKC.manylands.combat.CombatConstants;
import com.github.MichalKC.manylands.combat.CombatProjectile;

import java.util.List;

public class SpinningForksAttack implements AttackPattern {
    private float nextSpawn;

    @Override public String getName() { return "forks"; }
    @Override public float getDuration() { return 9.0f; }

    @Override
    public void reset() {
        nextSpawn = 0.6f;
    }

    @Override
    public void spawn(float t, float dt, List<CombatProjectile> projs) {
        if (t < nextSpawn) return;

        // Fork rain: spawn groups that move together vertically up or down (about 8 forks per group)
        boolean down = Math.random() < 0.5;
        int groupSize = 5;
        float spacing = 12f; // horizontal spacing inside group
        float forkW = 8f;    // narrow width so fork is vertical
        float forkH = 28f;   // tall height so it looks big
        float speed = 150f + (float)(Math.random() * 60f);

        float totalGroupW = (groupSize - 1) * spacing + forkW;
        float minX = CombatConstants.BOX_X + 6f;
        float maxX = CombatConstants.BOX_X + CombatConstants.BOX_W - totalGroupW - 6f;
        float startX = minX + (float)(Math.random() * Math.max(1f, (maxX - minX)));
        float startY = down ? (CombatConstants.BOX_Y + CombatConstants.BOX_H + 14f) : (CombatConstants.BOX_Y - forkH - 14f);
        float vy = down ? -speed : speed;

        for (int i = 0; i < groupSize; i++) {
            float sx = startX + i * spacing + (float)(Math.random() * 2f - 1f); // tiny jitter
            float sy = startY + (float)(Math.random() * 2f - 1f);
            CombatProjectile fork = new CombatProjectile(sx, sy, 0f, vy, forkW, forkH, CombatProjectile.Type.FORK);
            fork.life = 0f; // immediate movement; renderer flips by vy sign
            fork.maxLife = 0f;
            projs.add(fork);
        }

        nextSpawn = t + 1.1f + (float)(Math.random() * 0.5f);
    }
}
