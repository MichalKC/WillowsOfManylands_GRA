package com.github.MichalKC.manylands.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public enum MapAsset implements Asset<TiledMap>{
    MAIN("maingrass.tmx");

    private final AssetDescriptor<TiledMap> descriptor;

    MapAsset(String mapName){
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.projectFilePath = "maps/grassland.tiled-project";
        this.descriptor = new AssetDescriptor<>("maps/"+mapName, TiledMap.class, parameters);
    }

    @Override
    public AssetDescriptor<TiledMap> getDescriptor(){
        return this.descriptor;
    }
}
