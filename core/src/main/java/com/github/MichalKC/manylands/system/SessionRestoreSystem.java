package com.github.MichalKC.manylands.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.github.MichalKC.manylands.PlayerSessionState;
import com.github.MichalKC.manylands.player.PlayerStatePersistence;
import com.github.MichalKC.manylands.ui.model.GameViewModel;

public class SessionRestoreSystem extends EntitySystem {
    private final Engine engine;
    private final GameViewModel viewModel;
    private PlayerSessionState scheduled;
    private Runnable postRestoreCallback;

    public SessionRestoreSystem(Engine engine, GameViewModel viewModel) {
        this.engine = engine;
        this.viewModel = viewModel;
        this.scheduled = null;
    }

    public void schedule(PlayerSessionState state) {
        this.scheduled = state;
    }

    public void setPostRestoreCallback(Runnable callback) {
        this.postRestoreCallback = callback;
    }

    @Override
    public void update(float deltaTime) {
        if (scheduled == null) {
            return;
        }
        PlayerSessionState state = scheduled;
        scheduled = null;
        PlayerStatePersistence.apply(engine, state, viewModel);
        if (postRestoreCallback != null) postRestoreCallback.run();
    }
}
