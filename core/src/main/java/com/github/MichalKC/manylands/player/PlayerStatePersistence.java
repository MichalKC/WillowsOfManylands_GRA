package com.github.MichalKC.manylands.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.MichalKC.manylands.PlayerSessionState;
import com.github.MichalKC.manylands.ai.AnimationState;
import com.github.MichalKC.manylands.component.Attack;
import com.github.MichalKC.manylands.component.Inventory;
import com.github.MichalKC.manylands.component.Item;
import com.github.MichalKC.manylands.component.DamageCooldown;
import com.github.MichalKC.manylands.component.Damaged;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Facing;
import com.github.MichalKC.manylands.component.Fsm;
import com.github.MichalKC.manylands.component.Life;
import com.github.MichalKC.manylands.component.Move;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.component.Respawn;
import com.badlogic.gdx.math.Vector2;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

public final class PlayerStatePersistence {

    private PlayerStatePersistence() {
    }

    public static PlayerSessionState capture(Engine engine) {
        return capture(engine, null);
    }

    public static PlayerSessionState capture(Engine engine, GameViewModel viewModel) {
        Entity player = findPlayer(engine);
        if (player == null) {
            return null;
        }

        Life life = Life.MAPPER.get(player);
        float maxLife = life != null ? life.getBaseMaxLife() : 0f;
        float currentLife = life != null ? life.getLife() : 0f;
        float lifePerSec = life != null ? life.getLifePerSec() : 0f;

        Facing facing = Facing.MAPPER.get(player);
        Facing.FacingDirection facingDirection = facing != null
            ? facing.getDirection()
            : Facing.FacingDirection.RIGHT;

        AnimationState animationState = AnimationState.IDLE;
        Fsm fsm = Fsm.MAPPER.get(player);
        if (fsm != null) {
            animationState = fsm.getAnimationFsm().getCurrentState();
        }

        boolean dead = Dead.MAPPER.get(player) != null;
        Respawn respawn = Respawn.MAPPER.get(player);
        boolean respawnActive = respawn != null && respawn.isActive();
        float respawnTimerSec = respawn != null ? respawn.getTimerSec() : 0f;

        float damageCooldownRemainingSec = 0f;
        DamageCooldown cooldown = DamageCooldown.MAPPER.get(player);
        if (cooldown != null && cooldown.isActive()) {
            damageCooldownRemainingSec = cooldown.getRemainingSec();
        }

        float spawnPositionX = 0f;
        float spawnPositionY = 0f;
        if (respawn != null) {
            Vector2 sp = respawn.getSpawnPosition();
            spawnPositionX = sp.x;
            spawnPositionY = sp.y;
        }

        int coins = viewModel != null ? viewModel.getCoins() : 0;
        float specialCharge = viewModel != null ? viewModel.getSpecialCharge() : 0f;
        boolean specialDraining = viewModel != null && viewModel.isSpecialDraining();

        Inventory playerInv = Inventory.MAPPER.get(player);
        Item[] inventorySlots = playerInv != null ? playerInv.getSlots() : null;
        Item[] equipSlots = playerInv != null ? playerInv.getEquipSlots() : null;
        java.util.Set<Integer> unlockedSkills = playerInv != null ? playerInv.getUnlockedSkills() : null;
        int persistedActivatedSkillSlot = playerInv != null ? playerInv.getPersistedActivatedSkillSlot() : -1;

        // Capture active skills timers if present
        float s1a=0f,s1c=0f,s2a=0f,s2c=0f,s3a=0f,s3c=0f,s4a=0f,s4c=0f,s5a=0f,s5c=0f,s6a=0f,s6c=0f,s7a=0f,s7c=0f,s8a=0f,s8c=0f;
        com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(player);
        if (skills != null) {
            s1a = skills.getSkill1ActiveRemaining(); s1c = skills.getSkill1CooldownRemaining();
            s2a = skills.getSkill2ActiveRemaining(); s2c = skills.getSkill2CooldownRemaining();
            s3a = skills.getSkill3ActiveRemaining(); s3c = skills.getSkill3CooldownRemaining();
            s4a = skills.getSkill4ActiveRemaining(); s4c = skills.getSkill4CooldownRemaining();
            s5a = skills.getSkill5ActiveRemaining(); s5c = skills.getSkill5CooldownRemaining();
            s6a = skills.getSkill6ActiveRemaining(); s6c = skills.getSkill6CooldownRemaining();
            s7a = skills.getSkill7ActiveRemaining(); s7c = skills.getSkill7CooldownRemaining();
            s8a = skills.getSkill8ActiveRemaining(); s8c = skills.getSkill8CooldownRemaining();
        }

        return new PlayerSessionState(
            maxLife,
            currentLife,
            lifePerSec,
            facingDirection,
            animationState,
            dead,
            respawnActive,
            respawnTimerSec,
            damageCooldownRemainingSec,
            spawnPositionX,
            spawnPositionY,
            coins,
            inventorySlots,
            equipSlots,
            specialCharge,
            specialDraining,
            unlockedSkills,
            persistedActivatedSkillSlot,
            s1a,s1c,s2a,s2c,s3a,s3c,s4a,s4c,s5a,s5c,s6a,s6c,s7a,s7c,s8a,s8c
        );
    }

    public static void apply(Engine engine, PlayerSessionState state, GameViewModel viewModel) {
        if (state == null) {
            return;
        }
        Entity player = findPlayer(engine);
        if (player == null) {
            return;
        }
        applyToEntity(player, state, viewModel);
    }

    public static void applyToEntity(Entity player, PlayerSessionState state, GameViewModel viewModel) {
        if (state == null || player == null) {
            return;
        }

        Life life = Life.MAPPER.get(player);
        if (life != null) {
            life.restore(state.getMaxLife(), state.getLife());
            if (viewModel != null) {
                viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
            }
        }

        Facing facing = Facing.MAPPER.get(player);
        if (facing != null) {
            facing.setDirection(state.getFacing());
        }

        player.remove(Damaged.class);

        Attack attack = Attack.MAPPER.get(player);
        if (attack != null) {
            attack.resetAttackTimer();
        }

        player.remove(DamageCooldown.class);
        if (state.getDamageCooldownRemainingSec() > 0f) {
            player.add(new DamageCooldown(state.getDamageCooldownRemainingSec()));
        }

        Move move = Move.MAPPER.get(player);
        if (move != null) {
            move.getDirection().setZero();
            move.setRooted(false);
        }

        player.remove(Dead.class);
        Respawn respawn = Respawn.MAPPER.get(player);
        if (respawn != null) {
            respawn.getSpawnPosition().set(state.getSpawnPositionX(), state.getSpawnPositionY());
        }
        if (state.isDead()) {
            player.add(new Dead());
            if (respawn != null) {
                if (state.isRespawnActive()) {
                    respawn.restoreCountdown(state.getRespawnTimerSec());
                } else {
                    respawn.activate();
                }
            }
        } else if (respawn != null) {
            respawn.deactivate();
        }

        Fsm fsm = Fsm.MAPPER.get(player);
        if (fsm != null) {
            fsm.getAnimationFsm().changeState(state.getAnimationState());
        }

        if (viewModel != null) {
            viewModel.setCoins(state.getCoins());
            viewModel.setSpecialCharge(state.getSpecialCharge());
            viewModel.setSpecialDraining(state.isSpecialDraining());

            int slot = state.getPersistedActivatedSkillSlot();
            int hudIdx = -1;
            if (slot >= 0) {
                final int[] HUD_MAP = {3, 4, 0, 5, 7, 1, 2, 6};
                if (slot < HUD_MAP.length) hudIdx = HUD_MAP[slot];
            }
            viewModel.setSelectedSkillHudIdx(hudIdx);
        }

        Inventory playerInv = Inventory.MAPPER.get(player);
        if (playerInv != null && state.getInventorySlots() != null) {
            System.arraycopy(state.getInventorySlots(), 0, playerInv.getSlots(), 0, Math.min(playerInv.getSlots().length, state.getInventorySlots().length));
        }
        if (playerInv != null && state.getEquipSlots() != null) {
            Item[] dest = playerInv.getEquipSlots();
            Item[] src  = state.getEquipSlots();
            for (int i = 0; i < dest.length && i < src.length; i++) {
                dest[i] = src[i];
            }
        }
        if (playerInv != null) {
            playerInv.getUnlockedSkills().clear();
            if (state.getUnlockedSkills() != null) {
                playerInv.getUnlockedSkills().addAll(state.getUnlockedSkills());
            }
            playerInv.setPersistedActivatedSkillSlot(state.getPersistedActivatedSkillSlot());
        }

        // Restore active skills timers after entity is ready
        com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(player);
        if (skills != null) {
            skills.setSkill1State(state.getSkill1ActiveRemaining(), state.getSkill1CooldownRemaining());
            skills.setSkill2State(state.getSkill2ActiveRemaining(), state.getSkill2CooldownRemaining());
            skills.setSkill3State(state.getSkill3ActiveRemaining(), state.getSkill3CooldownRemaining());
            skills.setSkill4State(state.getSkill4ActiveRemaining(), state.getSkill4CooldownRemaining());
            skills.setSkill5State(state.getSkill5ActiveRemaining(), state.getSkill5CooldownRemaining());
            skills.setSkill6State(state.getSkill6ActiveRemaining(), state.getSkill6CooldownRemaining());
            skills.setSkill7State(state.getSkill7ActiveRemaining(), state.getSkill7CooldownRemaining());
            skills.setSkill8State(state.getSkill8ActiveRemaining(), state.getSkill8CooldownRemaining());
        }
    }

    private static Entity findPlayer(Engine engine) {
        ImmutableArray<Entity> players = engine.getEntitiesFor(Family.all(Player.class).get());
        if (players.size() == 0) {
            return null;
        }
        return players.first();
    }
}
