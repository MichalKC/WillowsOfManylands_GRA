package com.github.MichalKC.manylands.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.MichalKC.manylands.component.Controller;

public class GameControllerState implements ControllerState {

    private final ImmutableArray<Entity> controllerEntities;
    private Runnable inventoryToggleCallback;
    private Runnable skillActivateCallback;
    private Runnable specialActivateCallback;
    private com.github.MichalKC.manylands.screen.GameScreen gameScreen;

    public GameControllerState(Engine engine) {
        this.controllerEntities = engine.getEntitiesFor(Family.all(Controller.class).get());
    }

    public void setInventoryToggleCallback(Runnable callback) {
        this.inventoryToggleCallback = callback;
    }

    public void setSkillActivateCallback(Runnable callback) {
        this.skillActivateCallback = callback;
    }

    public void setSpecialActivateCallback(Runnable callback) {
        this.specialActivateCallback = callback;
    }

    public void setGameScreen(com.github.MichalKC.manylands.screen.GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @Override
    public void keyDown(Command command) {
        if (command == Command.INVENTORY) {
            if (inventoryToggleCallback != null && (gameScreen == null || !gameScreen.isCombatTransitionActive())) {
                inventoryToggleCallback.run();
            }
            return;
        }
        if (command == Command.SKILL) {
            if (skillActivateCallback != null) skillActivateCallback.run();
            return;
        }
        if (command == Command.SPECIAL) {
            if (specialActivateCallback != null && (gameScreen == null || !gameScreen.isCombatTransitionActive())) {
                specialActivateCallback.run();
            }
            return;
        }
        for (Entity entity : controllerEntities) {
            Controller.MAPPER.get(entity).getPressedCommands().add(command);
        }
    }

    @Override
    public void keyUp(Command command) {
        for (Entity entity : controllerEntities) {
            Controller.MAPPER.get(entity).getReleasedCommands().add(command);
        }
    }
}
