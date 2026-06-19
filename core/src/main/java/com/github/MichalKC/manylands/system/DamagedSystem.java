package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.MichalKC.manylands.component.DamageCooldown;
import com.github.MichalKC.manylands.component.Damaged;
import com.github.MichalKC.manylands.component.Dead;
import com.github.MichalKC.manylands.component.Inventory;
import com.github.MichalKC.manylands.component.Life;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.component.Transform;
import com.github.MichalKC.manylands.ui.model.GameViewModel;


public class DamagedSystem extends IteratingSystem {
    private static final float DAMAGE_COOLDOWN_SEC = 0.5f;
    private final GameViewModel viewModel;

    public DamagedSystem(GameViewModel viewModel) {
        super(Family.all(Damaged.class).get());
        this.viewModel = viewModel;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        if (Dead.MAPPER.get(entity) != null) {
            entity.remove(Damaged.class);
            return;
        }

        Damaged damaged = Damaged.MAPPER.get(entity);
        entity.remove(Damaged.class);

        DamageCooldown cooldown = DamageCooldown.MAPPER.get(entity);
        if (cooldown != null && cooldown.isActive()) {
            return;
        }
        float cdDuration = damaged.getCooldownOverrideSec() > 0f
            ? damaged.getCooldownOverrideSec()
            : DAMAGE_COOLDOWN_SEC;
        if (cooldown == null) {
            entity.add(new DamageCooldown(cdDuration));
        } else {
            cooldown.reset(cdDuration);
        }

        Life life = Life.MAPPER.get(entity);
        if (life != null) {
            float dmg = damaged.getDamage();
            if (Player.MAPPER.get(entity) != null) {
                Inventory inv = Inventory.MAPPER.get(entity);
                if (inv != null && inv.getEquipDefenseBonus() > 0) {
                    dmg = Math.max(0f, dmg - inv.getEquipDefenseBonus() / 2f);
                }
            }
            life.addLife(-dmg);
            if (Player.MAPPER.get(entity) != null) {
                com.github.MichalKC.manylands.component.ActiveSkills skills = com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(entity);
                if (skills != null && skills.isSkill4Active()) {
                    skills.cancelSkill4Early();
                }
            }
            // If an enemy takes any damage while Skill 7 is active on a player,
            // remove petrification only for that enemy by adding PetrifyOverride
            if (com.github.MichalKC.manylands.component.Enemy.MAPPER.get(entity) != null) {
                com.badlogic.ashley.utils.ImmutableArray<com.badlogic.ashley.core.Entity> players =
                    getEngine().getEntitiesFor(
                        com.badlogic.ashley.core.Family.all(
                            com.github.MichalKC.manylands.component.Player.class,
                            com.github.MichalKC.manylands.component.ActiveSkills.class
                        ).get()
                    );
                boolean skill7Active = false;
                for (int i = 0; i < players.size(); i++) {
                    com.github.MichalKC.manylands.component.ActiveSkills sk =
                        com.github.MichalKC.manylands.component.ActiveSkills.MAPPER.get(players.get(i));
                    if (sk != null && sk.isSkill7Active()) { skill7Active = true; break; }
                }
                if (skill7Active) {
                    if (com.github.MichalKC.manylands.component.PetrifyOverride.MAPPER.get(entity) == null) {
                        entity.add(new com.github.MichalKC.manylands.component.PetrifyOverride());
                    }
                }
            }
            if (Player.MAPPER.get(entity) != null) {
                viewModel.updateLifeInfo(life.getMaxLife(), life.getLife());
            }
            if (life.getLife() <= 0f) {
                entity.add(new Dead());
            }
        }

        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null) {
            // we should check that the damage source is the player, but
            // in this version of game it is in the only possibility so we can skip it
            float x = transform.getPosition().x + transform.getSize().x * 0.5f;
            float y = transform.getPosition().y;
            viewModel.playerDamage((int) damaged.getDamage(), x, y);
            // Add special gauge charge only when target is not the player (player dealt damage)
            if (Player.MAPPER.get(entity) == null) {
                viewModel.addSpecialChargeByDamage((int) damaged.getDamage());
            }
        }
    }
}
