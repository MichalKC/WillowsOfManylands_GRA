package com.github.MichalKC.manylands.input;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.HashMap;
import java.util.Map;

public class KeyboardController extends InputAdapter {
    private static final Map<Integer, Command> KEY_MAPPING = Map.ofEntries(
        Map.entry(Input.Keys.W, Command.UP),
        Map.entry(Input.Keys.UP, Command.UP),
        Map.entry(Input.Keys.S, Command.DOWN),
        Map.entry(Input.Keys.DOWN, Command.DOWN),
        Map.entry(Input.Keys.A, Command.LEFT),
        Map.entry(Input.Keys.LEFT, Command.LEFT),
        Map.entry(Input.Keys.D, Command.RIGHT),
        Map.entry(Input.Keys.RIGHT, Command.RIGHT),
        Map.entry(Input.Keys.SPACE, Command.SELECT),
        Map.entry(Input.Keys.J, Command.ATTACK2),
        Map.entry(Input.Keys.K, Command.SPECIAL),
        Map.entry(Input.Keys.E, Command.INTERACT),
        Map.entry(Input.Keys.ESCAPE, Command.CANCEL),
        Map.entry(Input.Keys.I, Command.INVENTORY),
        Map.entry(Input.Keys.L, Command.SKILL)
    );

    private final boolean[] commandState;
    private final Map<Class<? extends ControllerState>, ControllerState> stateCache;
    private ControllerState activeState;

    public KeyboardController(Class<? extends ControllerState>  initialState, Engine engine, Stage stage) {
        this.stateCache = new HashMap<>();
        this.activeState = null;
        this.commandState = new boolean[Command.values().length];

        this.stateCache.put(IdleControllerState.class, new IdleControllerState());
        this.stateCache.put(DialogueControllerState.class, new DialogueControllerState());
        this.stateCache.put(ShopChoiceControllerState.class, new ShopChoiceControllerState());
        if (engine != null) {
            this.stateCache.put(GameControllerState.class, new GameControllerState(engine));
        }
        if (stage != null) {
            this.stateCache.put(UiControllerState.class, new UiControllerState(stage));
        }
        setActiveState(initialState);
    }

    public <T extends ControllerState> T getState(Class<T> stateClass) {
        ControllerState controllerState = stateCache.get(stateClass);
        if (controllerState == null) return null;
        return stateClass.cast(controllerState);
    }

    public void setActiveState(Class<? extends ControllerState> stateClass) {
        ControllerState controllerState = stateCache.get(stateClass);
        if (controllerState == null) {
            throw new GdxRuntimeException("No state with class " + stateClass + " found in the state cache");
        }

        for (Command command : Command.values()) {
            if (this.activeState != null && this.commandState[command.ordinal()]) {
                this.activeState.keyUp(command);
            }
            this.commandState[command.ordinal()] = false;
        }

        this.activeState = controllerState;
    }

    @Override
    public boolean keyDown(int keycode) {
        Command command = KEY_MAPPING.get(keycode);
        if (command == null) return false;

        this.commandState[command.ordinal()] = true;
        this.activeState.keyDown(command);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        Command command = KEY_MAPPING.get(keycode);
        if (command == null) return false;
        if(!this.commandState[command.ordinal()]) return false;

        this.commandState[command.ordinal()] = false;
        this.activeState.keyUp(command);
        return true;
    }
}
