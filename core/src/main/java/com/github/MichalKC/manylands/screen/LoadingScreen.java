package com.github.MichalKC.manylands.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.asset.AssetService;
import com.github.MichalKC.manylands.asset.AtlasAsset;
import com.github.MichalKC.manylands.asset.SkinAsset;
import com.github.MichalKC.manylands.asset.SoundAsset;

public class LoadingScreen extends ScreenAdapter {

    private final GdxGame game;
    private final AssetService assetService;

    public LoadingScreen(GdxGame game, AssetService assetService) {
        this.game = game;
        this.assetService = assetService;
    }

    @Override
    public void show() {
        for (AtlasAsset atlas : AtlasAsset.values()) {
            assetService.queue(atlas);
        }
        for (SoundAsset sound : SoundAsset.values()) {
            assetService.queue(sound);
        }
        assetService.queue(SkinAsset.DEFAULT);
    }

    @Override
    public void render(float delta) {
        if(this.assetService.update()) {
            Gdx.app.debug("LoadingScreen", "Finished asset loading");
            createScreens();
            this.game.removeScreen(this);
            this.dispose();
            this.game.setScreen(MainMenuScreen.class);
        }
    }

    private void createScreens() {
        this.game.addScreen(new MainMenuScreen(this.game));
        this.game.addScreen(new GameScreen(this.game));
    }
}
