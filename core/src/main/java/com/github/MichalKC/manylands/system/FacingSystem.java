package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.github.MichalKC.manylands.component.Attack;
import com.github.MichalKC.manylands.component.Facing;
import com.github.MichalKC.manylands.component.Facing.FacingDirection;
import com.github.MichalKC.manylands.component.Move;

public class FacingSystem extends IteratingSystem {
    public FacingSystem() {
        super(Family.all(Facing.class, Move.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Move move = Move.MAPPER.get(entity);
        if (move.isRooted()) {
            return;
        }
        Attack attack = Attack.MAPPER.get(entity);
        if (attack != null && attack.isAttacking()) {
            return;
        }

        Vector2 moveDirection = move.getDirection();
        if(moveDirection.isZero()) {
            return;
        }

        Facing facing = Facing.MAPPER.get(entity);
        if (moveDirection.x > 0) {
            facing.setDirection(FacingDirection.RIGHT);
        } else if (moveDirection.x < 0){
            facing.setDirection(FacingDirection.LEFT);
        }
    }
}
