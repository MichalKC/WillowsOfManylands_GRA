package com.github.MichalKC.manylands.combat;

import java.util.List;

public interface  AttackPattern {
    String getName();
    float getDuration();
    void reset();
    void spawn(float time, float dt, List<CombatProjectile> projectiles);
}
