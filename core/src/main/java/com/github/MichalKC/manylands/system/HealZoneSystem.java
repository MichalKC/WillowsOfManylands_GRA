package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.MichalKC.manylands.component.Gated;
import com.github.MichalKC.manylands.component.HealZone;
import com.github.MichalKC.manylands.component.Life;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

public class HealZoneSystem extends IteratingSystem {

    private final GameViewModel viewModel;

    public HealZoneSystem(GameViewModel viewModel) {
        super(Family.all(HealZone.class).get());
        this.viewModel = viewModel;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HealZone healZone = HealZone.MAPPER.get(entity);
        Entity overlappingPlayer = healZone.getOverlappingPlayer();

        if (overlappingPlayer == null) {
            return;
        }

        if (Player.MAPPER.get(overlappingPlayer) == null) {
            return;
        }

        Gated gated = Gated.MAPPER.get(entity);
        if (gated != null && !GateEvaluator.isOpen(getEngine(), gated)) {
            return;
        }

        Life life = Life.MAPPER.get(overlappingPlayer);
        if (life != null) {
            float currentMultiplier = life.getRegenMultiplier();
            float zoneMultiplier = healZone.getHealMultiplier();

            if (zoneMultiplier > currentMultiplier) {
                life.setRegenMultiplier(zoneMultiplier);
            }
            viewModel.addEffect("heal");
        }
    }

    @Override
    public void update(float deltaTime) {
        viewModel.removeEffect("heal");

        ImmutableArray<Entity> lifeEntities = getEngine().getEntitiesFor(Family.all(Life.class).get());
        for (int i = 0; i < lifeEntities.size(); i++) {
            Entity lifeEntity = lifeEntities.get(i);
            Life life = Life.MAPPER.get(lifeEntity);
            life.setRegenMultiplier(1.0f);
        }

        super.update(deltaTime);
    }
}
