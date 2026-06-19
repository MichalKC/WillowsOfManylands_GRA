package com.github.MichalKC.manylands.ui.model;

import com.badlogic.gdx.Gdx;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.audio.AudioService;
import com.github.MichalKC.manylands.screen.GameScreen;

public class MenuViewModel extends ViewModel{

    private final AudioService audioService;
    private Runnable startGameCallback;

    public MenuViewModel(GdxGame game) {
        super(game);
        this.audioService = game.getAudioService();
    }

    public float getMusicVolume() {
        return audioService.getMusicVolume();
    }

    public float getSoundVolume() {
        return audioService.getSoundVolume();
    }

    public void setMusicVolume(float volume) {
        this.audioService.setMusicVolume(volume);
    }

    public void setSoundVolume(float volume) {
        this.audioService.setSoundVolume(volume);
    }

    public void startGame() {
        if (startGameCallback != null) {
            startGameCallback.run();
        } else {
            game.setScreen(GameScreen.class);
        }
    }

    public void quitGame() {
        Gdx.app.exit();
    }

    public void setStartGameCallback(Runnable cb) {
        this.startGameCallback = cb;
    }
}



