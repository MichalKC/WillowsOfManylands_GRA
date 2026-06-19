package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.maps.MapObject;

public class Tiled implements Component {
    public static final ComponentMapper<Tiled> MAPPER = ComponentMapper.getFor(Tiled.class);

    private final String layerName;
    private final int id;
    private final MapObject mapObjectRef;

    public Tiled(String layerName, MapObject mapObjectRef) {
        this.layerName = layerName;
        this.id = mapObjectRef.getProperties().get("id", -1, Integer.class);
        this.mapObjectRef = mapObjectRef;
    }

    public String getLayerName() {
        return layerName;
    }

    public int getId() {
        return id;
    }

    public MapObject getMapObjectRef() {
        return mapObjectRef;
    }
}
