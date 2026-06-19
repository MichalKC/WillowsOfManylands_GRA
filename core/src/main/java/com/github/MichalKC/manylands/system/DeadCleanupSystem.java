package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.World;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Move;
import com.github.MichalKC.manylands.component.Physic;

public class DeadCleanupSystem extends IteratingSystem {
    private final World world;

    public DeadCleanupSystem(World world) {
        super(Family.all(Dead.class, Physic.class).get());
        this.world = world;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            move.getDirection().setZero();
            move.setRooted(true);
        }

        Physic physic = Physic.MAPPER.get(entity);
        world.destroyBody(physic.getBody());

        // Keep entity on map, but detach it from Box2D while dead.
        entity.remove(Physic.class);
    }
}
