package com.github.MichalKC.manylands.world;

import com.github.MichalKC.manylands.component.Facing;

public final class EnemySnapshot {
    private final float x;
    private final float y;
    private final Facing.FacingDirection facing;

    public EnemySnapshot(float x, float y, Facing.FacingDirection facing) {
        this.x = x;
        this.y = y;
        this.facing = facing;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Facing.FacingDirection getFacing() {
        return facing;
    }
}
