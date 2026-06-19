package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.github.MichalKC.manylands.component.*;
import com.github.MichalKC.manylands.tiled.ZoneShape;

public class NpcAISystem extends IteratingSystem {
    private static final Vector2 TMP = new Vector2();
    private static final Vector2 TMP_COL = new Vector2();
    private static final float PATROL_MIN_SEC = 2.0f;
    private static final float PATROL_MAX_SEC = 4.0f;
    private static final float IDLE_MIN_SEC = 2.5f;
    private static final float IDLE_MAX_SEC = 4.5f;
    private static final float STUCK_VELOCITY_THRESHOLD = 0.05f;

    public NpcAISystem() {
        super(Family.all(Npc.class, Move.class, Physic.class, Facing.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (Dead.MAPPER.get(entity) != null) return;

        Move move = Move.MAPPER.get(entity);
        if (move.isRooted()) return;

        Npc npc = Npc.MAPPER.get(entity);

        // If in dialogue, stay idle (don't move)
        if (npc.isInDialogue()) {
            move.getDirection().setZero();
            return;
        }

        Body body = Physic.MAPPER.get(entity).getBody();
        ZoneShape roam = npc.getRoamBounds();

        // Enforce roam bounds
        Vector2 colCenter = collisionCenter(body, npc);
        enforceRoamBounds(body, colCenter, roam);
        colCenter = collisionCenter(body, npc);

        // Patrol behavior
        patrol(entity, npc, move, body, colCenter, deltaTime);
    }

    private Vector2 collisionCenter(Body body, Npc npc) {
        return TMP_COL.set(body.getPosition()).add(npc.getCollisionOffset());
    }

    private void enforceRoamBounds(Body body, Vector2 collisionCenter, ZoneShape roam) {
        if (roam == null) return;
        if (roam.contains(collisionCenter)) return;

        Rectangle bounds = roam.getBoundingBox();
        float clampedX = MathUtils.clamp(collisionCenter.x, bounds.x, bounds.x + bounds.width);
        float clampedY = MathUtils.clamp(collisionCenter.y, bounds.y, bounds.y + bounds.height);

        Vector2 bodyPos = body.getPosition();
        float offsetX = collisionCenter.x - bodyPos.x;
        float offsetY = collisionCenter.y - bodyPos.y;
        body.setTransform(clampedX - offsetX, clampedY - offsetY, body.getAngle());
        body.setLinearVelocity(0f, 0f);
    }

    private void patrol(Entity entity, Npc npc, Move move, Body body, Vector2 position, float deltaTime) {
        ZoneShape roam = npc.getRoamBounds();
        if (roam == null) {
            idleTick(npc, move, deltaTime);
            return;
        }

        if (npc.isPatrolling()) {
            if (isStuck(body, move)) {
                pickNewPatrolDirection(npc);
            }

            npc.setPatrolTimer(npc.getPatrolTimer() - deltaTime);
            if (npc.getPatrolTimer() <= 0f) {
                npc.setPatrolling(false);
                npc.setIdleTimer(MathUtils.random(IDLE_MIN_SEC, IDLE_MAX_SEC));
                move.getDirection().setZero();
                return;
            }

            TMP.set(npc.getPatrolDir());
            if (clampDirection(roam, position, TMP)) {
                move.getDirection().set(TMP);
                Facing facing = Facing.MAPPER.get(entity);
                if (facing != null) {
                    if (TMP.x > 0) facing.setDirection(Facing.FacingDirection.RIGHT);
                    else if (TMP.x < 0) facing.setDirection(Facing.FacingDirection.LEFT);
                }
            } else {
                pickNewPatrolDirection(npc);
            }
        } else {
            idleTick(npc, move, deltaTime);
        }
    }

    private boolean isStuck(Body body, Move move) {
        if (move.getDirection().isZero()) return false;
        Vector2 vel = body.getLinearVelocity();
        return vel.len2() < STUCK_VELOCITY_THRESHOLD * STUCK_VELOCITY_THRESHOLD;
    }

    private void pickNewPatrolDirection(Npc npc) {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        npc.getPatrolDir().set(MathUtils.cos(angle), MathUtils.sin(angle)).nor();
        npc.setPatrolTimer(MathUtils.random(PATROL_MIN_SEC, PATROL_MAX_SEC));
    }

    private void idleTick(Npc npc, Move move, float deltaTime) {
        move.getDirection().setZero();
        npc.setIdleTimer(npc.getIdleTimer() - deltaTime);
        if (npc.getIdleTimer() <= 0f) {
            npc.setPatrolling(true);
            pickNewPatrolDirection(npc);
        }
    }

    private boolean clampDirection(ZoneShape roam, Vector2 pos, Vector2 dir) {
        if (roam == null) return !dir.isZero();

        Rectangle bounds = roam.getBoundingBox();
        if (dir.x < 0 && pos.x <= bounds.x) dir.x = 0f;
        if (dir.x > 0 && pos.x >= bounds.x + bounds.width) dir.x = 0f;
        if (dir.y < 0 && pos.y <= bounds.y) dir.y = 0f;
        if (dir.y > 0 && pos.y >= bounds.y + bounds.height) dir.y = 0f;

        return !dir.isZero();
    }
}
