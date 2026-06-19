package com.github.MichalKC.manylands.tiled;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.XmlReader;

public class OpacityAwareTmxMapLoader extends TmxMapLoader {

    @Override
    protected void loadObject(TiledMap map, MapLayer layer, XmlReader.Element element) {
        int countBefore = layer.getObjects().getCount();
        super.loadObject(map, layer, element);

        String opacityAttr = element.getAttribute("opacity", null);
        if (opacityAttr == null) return;

        float opacity;
        try {
            opacity = Float.parseFloat(opacityAttr);
        } catch (NumberFormatException ignored) {
            return;
        }

        MapObjects objects = layer.getObjects();
        int countAfter = objects.getCount();
        for (int i = countBefore; i < countAfter; i++) {
            MapObject obj = objects.get(i);
            obj.setOpacity(obj.getOpacity() * opacity);
        }
    }
}
