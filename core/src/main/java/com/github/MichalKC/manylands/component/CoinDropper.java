package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class CoinDropper implements Component {
    public static final ComponentMapper<CoinDropper> MAPPER = ComponentMapper.getFor(CoinDropper.class);

    private final int amount;
    private boolean dropped;

    public CoinDropper(int amount) {
        this.amount = Math.max(0, amount);
        this.dropped = false;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isDropped() {
        return dropped;
    }

    public void setDropped(boolean dropped) {
        this.dropped = dropped;
    }
}
