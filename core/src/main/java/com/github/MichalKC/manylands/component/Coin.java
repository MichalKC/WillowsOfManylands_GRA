package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Coin implements Component {
    public static final ComponentMapper<Coin> MAPPER = ComponentMapper.getFor(Coin.class);

    public static final int DEFAULT_VALUE = 5;

    private final int value;
    private boolean dropAnimationFinished;
    private boolean markedForPickup;

    public Coin(int value) {
        this.value = value;
        this.dropAnimationFinished = false;
        this.markedForPickup = false;
    }

    public int getValue() {
        return value;
    }

    public boolean isDropAnimationFinished() {
        return dropAnimationFinished;
    }

    public void setDropAnimationFinished(boolean finished) {
        this.dropAnimationFinished = finished;
    }

    public boolean isMarkedForPickup() {
        return markedForPickup;
    }

    public void setMarkedForPickup(boolean markedForPickup) {
        this.markedForPickup = markedForPickup;
    }
}
