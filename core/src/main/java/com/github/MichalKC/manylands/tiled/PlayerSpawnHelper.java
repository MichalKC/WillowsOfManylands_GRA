package com.github.MichalKC.manylands.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.physics.box2d.Body;
import com.github.MichalKC.manylands.component.Physic;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.component.Respawn;
import com.github.MichalKC.manylands.component.Transform;

public final class PlayerSpawnHelper {

    private PlayerSpawnHelper() {
    }

    public static void applyPlayerWorldPosition(Engine engine, float worldX, float worldY) {
        applyPlayerWorldPosition(engine, worldX, worldY, true);
    }

    public static void applyPlayerWorldPosition(Engine engine, float worldX, float worldY, boolean updateRespawnPosition) {
        for (Entity e : engine.getEntitiesFor(Family.all(Player.class, Transform.class).get())) {
            Transform transform = Transform.MAPPER.get(e);
            transform.getPosition().set(worldX, worldY);
            Physic physic = Physic.MAPPER.get(e);
            if (physic != null) {
                Body body = physic.getBody();
                body.setTransform(worldX, worldY, body.getAngle());
                physic.getPrevPosition().set(worldX, worldY);
            }
            if (updateRespawnPosition) {
                Respawn respawn = Respawn.MAPPER.get(e);
                if (respawn != null) {
                    respawn.getSpawnPosition().set(worldX, worldY);
                }
            }
            break;
        }
    }
}
