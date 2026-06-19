package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Timer;
import com.github.MichalKC.manylands.asset.SoundAsset;
import com.github.MichalKC.manylands.audio.AudioService;
import com.github.MichalKC.manylands.component.*;

public class TriggerSystem extends IteratingSystem {
    private final AudioService audioService;

    public TriggerSystem(AudioService audioService) {
        super(Family.all(Trigger.class).get());
        this.audioService = audioService;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (MapPortal.MAPPER.get(entity) != null) {
            return;
        }
        Trigger trigger = Trigger.MAPPER.get(entity);
        if(trigger.getTriggeringEntity() == null) return;

        // Universal gating: trigger does nothing until required interactables are activated.
        Gated gated = Gated.MAPPER.get(entity);
        if (gated != null && !GateEvaluator.isOpen(getEngine(), gated)) {
            trigger.setTriggeringEntity(null);
            return;
        }

        fireTrigger(trigger.getName(), trigger.getTriggeringEntity());
        trigger.setTriggeringEntity(null);
    }

    private void fireTrigger(String triggerName, Entity triggeringEntity) {
        switch (triggerName) {
            case "trap_trigger" -> executeTrapScript(triggeringEntity);
            default -> Gdx.app.debug("TriggerSystem", "No script for trigger: " + triggerName);
        }
    }

    private void executeTrapScript(Entity triggeringEntity) {
        Entity trapEntity = entityByTiledId(65);
        if(trapEntity == null) return;

        Animation2D animation2D = Animation2D.MAPPER.get(trapEntity);
        animation2D.setSpeed(1f);
        animation2D.setPlayMode(Animation.PlayMode.NORMAL);

        final float trapDamage =3f; // albo z konfiguracji
        Damaged damaged = Damaged.MAPPER.get(triggeringEntity);
        if (damaged == null) {
            triggeringEntity.add(new Damaged(trapDamage));
        } else {
            damaged.addDamage(trapDamage);
        }

        audioService.playSound(SoundAsset.TRAP);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                animation2D.setSpeed(0f);
                animation2D.setType(Animation2D.AnimationType.IDLE);
            }
        }, 2.5f);
    }

    private Entity entityByTiledId(int tiledId) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(Family.all(Tiled.class).get());
        for (Entity entity : entities) {
            if (Tiled.MAPPER.get(entity).getId() == tiledId) {
                return entity;
            }
        }
        return null;
    }
}
