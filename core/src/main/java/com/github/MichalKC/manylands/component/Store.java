package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Store implements Component {
    public static final ComponentMapper<Store> MAPPER = ComponentMapper.getFor(Store.class);

    private final String storeType;
    private final String storeName;

    public Store(String storeType, String storeName) {
        this.storeType = storeType == null || storeType.isBlank() ? "general" : storeType.trim();
        this.storeName = storeName == null || storeName.isBlank() ? "SHOP" : storeName.trim();
    }

    public String getStoreType() {
        return storeType;
    }

    public String getStoreName() {
        return storeName;
    }
}
