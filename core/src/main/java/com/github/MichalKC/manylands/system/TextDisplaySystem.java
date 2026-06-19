package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.MichalKC.manylands.component.TextDisplay;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

public class TextDisplaySystem extends EntitySystem {

    private final Engine engine;
    private final GameViewModel viewModel;
    private final Family family = Family.all(TextDisplay.class).exclude(com.github.MichalKC.manylands.component.Transform.class).get();

    private String currentText = null;
    private float currentDisplayTime = 0f;

    public TextDisplaySystem(Engine engine, GameViewModel viewModel) {
        this.engine = engine;
        this.viewModel = viewModel;
    }

    @Override
    public void update(float deltaTime) {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(family);
        if (entities.size() > 0) {
            Entity entity = entities.first();
            TextDisplay td = TextDisplay.MAPPER.get(entity);
            if (!td.getText().equals(currentText)) {
                currentText = td.getText();
                currentDisplayTime = 0f;
                viewModel.showText(currentText);
            }
            currentDisplayTime += deltaTime;
            if (currentDisplayTime >= td.getDisplayDuration()) {
                viewModel.hideText();
                engine.removeEntity(entity);
                currentText = null;
                currentDisplayTime = 0f;
            }
        } else if (currentText != null) {
            viewModel.hideText();
            currentText = null;
            currentDisplayTime = 0f;
        }
    }
}
