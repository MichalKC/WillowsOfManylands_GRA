package com.github.MichalKC.manylands.combat;

public class CombatProjectile {
    public float x, y, vx, vy, w, h;
    public Type type;
    public float life;
    public float maxLife;

    public enum Type {
        BONE, PELLET, LASER, HEART, WARNING, SPOON, POT, KNIFE, FORK
    }

    public CombatProjectile(float x, float y, float vx, float vy, float w, float h, Type type) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.w = w;
        this.h = h;
        this.type = type;
    }
}
