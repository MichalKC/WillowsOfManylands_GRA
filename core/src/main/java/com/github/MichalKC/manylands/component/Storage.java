package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Storage implements Component {
    public static final ComponentMapper<Storage> MAPPER = ComponentMapper.getFor(Storage.class);

    private final String title;
    private final int rows;
    private final int cols;
    private final Item[] slots;

    public Storage(String title, int rows, int cols) {
        this.title = title == null || title.isBlank() ? "CHEST" : title.trim();
        this.rows = Math.max(1, rows);
        this.cols = Math.max(1, cols);
        this.slots = new Item[this.rows * this.cols];
    }

    public Item[] getSlots() {
        return slots;
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
