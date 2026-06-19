package com.github.MichalKC.manylands.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.github.MichalKC.manylands.tiled.OpacityAwareTmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.freetypist.FreeTypistSkinLoader;

public class AssetService implements Disposable {
    private final AssetManager assetManager;

    public AssetService(FileHandleResolver fileHandleResolver){
        this.assetManager = new AssetManager(fileHandleResolver);
        this.assetManager.setLoader(TiledMap.class, new OpacityAwareTmxMapLoader());
        this.assetManager.setLoader(Skin.class, new FreeTypistSkinLoader(fileHandleResolver));
    }

    public <T> T load(Asset<T> asset) {
        this.assetManager.load(asset.getDescriptor());
        this.assetManager.finishLoading();
        return this.assetManager.get(asset.getDescriptor());
    }

    public TiledMap loadTiledMapByFileName(String mapFileName) {
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.projectFilePath = "maps/grassland.tiled-project";
        AssetDescriptor<TiledMap> desc = new AssetDescriptor<>("maps/" + mapFileName, TiledMap.class, parameters);
        this.assetManager.load(desc);
        this.assetManager.finishLoading();
        return this.assetManager.get(desc);
    }

    public void unloadTiledMapByFileName(String mapFileName) {
        this.assetManager.unload("maps/" + mapFileName);
    }

    public <T> void queue(Asset<T> asset) {
        this.assetManager.load(asset.getDescriptor());
    }

    public <T> T get(Asset<T> asset) {
        return this.assetManager.get(asset.getDescriptor());
    }

    public <T> void unload(Asset<T> asset) {
        this.assetManager.unload(asset.getDescriptor().fileName);
    }

    public boolean update() {
        return this.assetManager.update();
    }

    public void debugDiagnostics() {
        Gdx.app.debug("AssetService", this.assetManager.getDiagnostics());
    }

    @Override
    public void dispose() {
        this.assetManager.dispose();
    }
}
