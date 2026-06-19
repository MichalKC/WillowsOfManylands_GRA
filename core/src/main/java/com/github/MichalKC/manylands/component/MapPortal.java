package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class MapPortal implements Component {
    public static final ComponentMapper<MapPortal> MAPPER = ComponentMapper.getFor(MapPortal.class);

    private final String targetMapFile;
    private final Float spawnXPixels;
    private final Float spawnYPixels;
    private final String spawnAtObjectName;

    public MapPortal(String targetMapFile, Float spawnXPixels, Float spawnYPixels, String spawnAtObjectName) {
        this.targetMapFile = targetMapFile;
        this.spawnXPixels = spawnXPixels;
        this.spawnYPixels = spawnYPixels;
        this.spawnAtObjectName = spawnAtObjectName;
    }

    public String getTargetMapFile() {
        return targetMapFile;
    }

    public boolean hasCustomSpawn() {
        return spawnXPixels != null && spawnYPixels != null;
    }

    public float getSpawnXPixels() {
        return spawnXPixels;
    }

    public float getSpawnYPixels() {
        return spawnYPixels;
    }

    public boolean hasNamedSpawn() {
        return spawnAtObjectName != null && !spawnAtObjectName.isBlank();
    }

    public String getSpawnAtObjectName() {
        return spawnAtObjectName;
    }

    public boolean hasSpawnResolution() {
        return hasCustomSpawn() || hasNamedSpawn();
    }
}
