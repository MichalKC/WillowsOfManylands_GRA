package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.MichalKC.manylands.tiled.ZoneShape;

public class Npc implements Component {
    public static final ComponentMapper<Npc> MAPPER = ComponentMapper.getFor(Npc.class);

    private final ZoneShape roamBounds;
    private final float respawnDelaySec;
    private final Vector2 spawnPosition;
    private long deathTimeMillis;
    private final Vector2 collisionOffset;

    private float patrolTimer;
    private float idleTimer;
    private final Vector2 patrolDir = new Vector2();
    private boolean patrolling;
    private boolean inDialogue;

    public Npc(ZoneShape roamBounds, float respawnDelaySec, Vector2 spawnPosition, Vector2 collisionOffset) {
        this.roamBounds = roamBounds;
        this.respawnDelaySec = respawnDelaySec;
        this.spawnPosition = new Vector2(spawnPosition);
        this.deathTimeMillis = -1;
        this.collisionOffset = collisionOffset != null ? new Vector2(collisionOffset) : new Vector2();
        this.idleTimer = MathUtils.random(1f, 3f);
        this.patrolling = false;
        this.inDialogue = false;
    }

    public Vector2 getCollisionOffset() {
        return collisionOffset;
    }

    public ZoneShape getRoamBounds() {
        return roamBounds;
    }

    public float getRespawnDelaySec() {
        return respawnDelaySec;
    }

    public Vector2 getSpawnPosition() {
        return spawnPosition;
    }

    public long getDeathTimeMillis() {
        return deathTimeMillis;
    }

    public void setDeathTimeMillis(long deathTimeMillis) {
        this.deathTimeMillis = deathTimeMillis;
    }

    public float getPatrolTimer() {
        return patrolTimer;
    }

    public void setPatrolTimer(float patrolTimer) {
        this.patrolTimer = patrolTimer;
    }

    public float getIdleTimer() {
        return idleTimer;
    }

    public void setIdleTimer(float idleTimer) {
        this.idleTimer = idleTimer;
    }

    public Vector2 getPatrolDir() {
        return patrolDir;
    }

    public boolean isPatrolling() {
        return patrolling;
    }

    public void setPatrolling(boolean patrolling) {
        this.patrolling = patrolling;
    }

    public boolean isInDialogue() {
        return inDialogue;
    }

    public void setInDialogue(boolean inDialogue) {
        this.inDialogue = inDialogue;
    }
}
