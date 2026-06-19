package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class TextDisplay implements Component {
    public static final ComponentMapper<TextDisplay> MAPPER = ComponentMapper.getFor(TextDisplay.class);

    private final String text;
    private final float displayDuration;

    public TextDisplay(String text, float displayDuration) {
        this.text = text;
        this.displayDuration = displayDuration;
    }

    public String getText() {
        return text;
    }

    public float getDisplayDuration() {
        return displayDuration;
    }
}
