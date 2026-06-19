package com.github.MichalKC.manylands.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;

public enum SoundAsset implements Asset<Sound> {
    SWING("swing.wav"),
    LIFE_REG("life_reg.wav"),
    TRAP("trap.wav"),
    COIN("coin_collect.mp3"),
    SELL("sell.mp3"),
    COMBAT_TRIGGER("combatTrigger.mp3"),
    COMBAT_HIT("combatHit.mp3");

    private final AssetDescriptor<Sound> descriptor;

    SoundAsset(String musicFile) {
        this.descriptor = new AssetDescriptor<>("audio/" + musicFile, Sound.class);
    }

    @Override
    public AssetDescriptor<Sound> getDescriptor() {
        return descriptor;
    }
}
