package com.github.MichalKC.manylands;

import com.github.MichalKC.manylands.asset.MapAsset;

public final class GameResumeState {
    private final String mapFile;
    private final MapAsset mapAsset;
    private final float worldX;
    private final float worldY;

    public GameResumeState(String mapFile, MapAsset mapAsset, float worldX, float worldY) {
        this.mapFile = mapFile;
        this.mapAsset = mapAsset;
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public String getMapFile() {
        return mapFile;
    }

    public MapAsset getMapAsset() {
        return mapAsset;
    }

    public boolean isFromMapFile() {
        return mapFile != null && !mapFile.isBlank();
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }
}
