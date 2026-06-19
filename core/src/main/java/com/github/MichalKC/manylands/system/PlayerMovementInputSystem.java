package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Move;
import com.github.MichalKC.manylands.component.Player;

public class PlayerMovementInputSystem extends IteratingSystem {

    public PlayerMovementInputSystem() {
        super(Family.all(Player.class, Move.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (Dead.MAPPER.get(entity) != null) {
            Move.MAPPER.get(entity).getDirection().setZero();
            return;
        }

        Move move = Move.MAPPER.get(entity);
        if (move.isRooted()) {
            return;
        }

        float x = 0f;
        float y = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            y += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            y -= 1f;
        }
        move.getDirection().set(x, y);
    }
}
