package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.MichalKC.manylands.component.*;

public class EnemyDeathRemovalSystem extends IteratingSystem {

    public EnemyDeathRemovalSystem() {
        super(Family.all(Enemy.class, Dead.class, Graphic.class, Animation2D.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Enemy enemy = Enemy.MAPPER.get(entity);
        if (enemy.getDeathTimeMillis() < 0) {
            enemy.setDeathTimeMillis(TimeUtils.millis());
        }

        Animation2D anim = Animation2D.MAPPER.get(entity);
        if (!anim.isDirty()
            && anim.getType() == Animation2D.AnimationType.DEAD
            && anim.isFinished()) {
            Graphic graphic = Graphic.MAPPER.get(entity);
            graphic.getColor().a = 0f;
        }
    }
}
