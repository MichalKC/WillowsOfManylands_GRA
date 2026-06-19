package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Graphic implements Component {
    public static final ComponentMapper<Graphic> MAPPER = ComponentMapper.getFor(Graphic.class);

    private TextureRegion region;
    private final Color color;
    private float flipOffsetX;

    public Graphic(Color color, TextureRegion region) {
        this.color = color;
        this.region = region;
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
    }

    public TextureRegion getRegion() {
        return region;
    }

    public Color getColor() {
        return color;
    }

    public float getFlipOffsetX() {
        return flipOffsetX;
    }

    public void setFlipOffsetX(float flipOffsetX) {
        this.flipOffsetX = flipOffsetX;
    }
}
