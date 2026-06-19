package com.github.MichalKC.manylands.world;

public final class DroppedCoinState {
    private final float x;
    private final float y;
    private final int value;

    public DroppedCoinState(float x, float y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getValue() {
        return value;
    }
}
