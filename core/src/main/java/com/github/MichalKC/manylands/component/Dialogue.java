package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Dialogue implements Component {
    public static final ComponentMapper<Dialogue> MAPPER = ComponentMapper.getFor(Dialogue.class);

    private final String[][] variants;

    public Dialogue(String text) {
        String[] rawVariants = text.split("\\|\\|");
        this.variants = new String[rawVariants.length][];
        for (int v = 0; v < rawVariants.length; v++) {
            String[] split = rawVariants[v].split("\\|");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
            }
            this.variants[v] = split;
        }
    }

    public String[][] getVariants() {
        return variants;
    }

    public String[] getLines() {
        return variants.length > 0 ? variants[0] : new String[0];
    }
}
