package com.github.MichalKC.manylands.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;

public enum MusicAsset implements Asset<Music> {
    MENU("menuMusic.mp3"),
    COMBAT_DYNAMIC("combatMusicDynamic.mp3"),
    VILLAGE("villageMusic.mp3"),
    HOUSE("houseMusic.mp3"),
    CEMETERY("cemeteryMusic.mp3");

    private final AssetDescriptor<Music> descriptor;

    MusicAsset(String musicFile) {
        this.descriptor = new AssetDescriptor<>("audio/" + musicFile, Music.class);
    }

    @Override
    public AssetDescriptor<Music> getDescriptor() {
        return descriptor;
    }
}
