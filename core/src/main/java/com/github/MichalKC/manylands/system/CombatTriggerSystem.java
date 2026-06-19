package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.MichalKC.manylands.asset.SoundAsset;
import com.github.MichalKC.manylands.audio.AudioService;
import com.github.MichalKC.manylands.component.CombatTrigger;
import com.github.MichalKC.manylands.component.Damaged;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Gated;
import com.github.MichalKC.manylands.component.Player;

import java.util.Set;
import java.util.function.BiConsumer;

public class CombatTriggerSystem extends IteratingSystem {
    private final Set<String> wonCombats;
    private final AudioService audioService;
    private BiConsumer<Entity, String> combatCallback;

    public CombatTriggerSystem(Set<String> wonCombats, AudioService audioService) {
        super(Family.all(CombatTrigger.class).get());
        this.wonCombats = wonCombats;
        this.audioService = audioService;
    }

    public void setCombatCallback(BiConsumer<Entity, String> callback) {
        this.combatCallback = callback;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        CombatTrigger ct = CombatTrigger.MAPPER.get(entity);

        if (ct.isRequireExit()) {
            if (!ct.isPlayerInside()) {
                ct.setRequireExit(false);
            }
            return;
        }

        // Need player inside the trigger to fire
        if (!ct.isPlayerInside()) return;

        Entity player = ct.getOverlappingPlayer();
        if (player == null) return;

        if (Dead.MAPPER.has(player) || Damaged.MAPPER.has(player)) return;

        Gated gated = Gated.MAPPER.get(entity);
        if (gated != null && !GateEvaluator.isOpen(getEngine(), gated)) return;

        String enemyId = ct.getEnemyId();

        if (wonCombats.contains(enemyId)) return;

        ct.setRequireExit(true);
        audioService.playSound(SoundAsset.COMBAT_TRIGGER);
        if (combatCallback != null) {
            combatCallback.accept(entity, enemyId);
        }
    }
}
