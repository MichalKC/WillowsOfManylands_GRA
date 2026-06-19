package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.MichalKC.manylands.component.ActiveSkills;
import com.github.MichalKC.manylands.component.Player;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

public class ActiveSkillsSystem extends IteratingSystem {
    private final GameViewModel viewModel;

    public ActiveSkillsSystem(GameViewModel viewModel) {
        super(Family.all(ActiveSkills.class).get());
        this.viewModel = viewModel;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ActiveSkills skills = ActiveSkills.MAPPER.get(entity);
        if (skills != null) {
            skills.update(deltaTime);
            com.github.MichalKC.manylands.component.Graphic graphic = com.github.MichalKC.manylands.component.Graphic.MAPPER.get(entity);
            if (graphic != null) {
                float target = skills.isSkill4Active() ? 0.6f : 1f;
                if (Math.abs(graphic.getColor().a - target) > 0.001f) {
                    graphic.getColor().a = target;
                }
            }
            if (viewModel != null && Player.MAPPER.get(entity) != null) {
                viewModel.setSpecialAlwaysUsable(skills.isSkill6Active());
            }
        }
    }
}
