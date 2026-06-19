package com.github.MichalKC.manylands.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** Per-map snapshots of interactable object states, keyed by {@link WorldStatePersistence#objectKey}. */
public final class MapWorldState {
    private final ObjectMap<String, InteractableObjectState> interactables;
    private final ObjectMap<String, EnemyDeathRecord> enemyDeaths;
    private final ObjectMap<String, Float> enemyLifeStates;
    private final ObjectMap<String, EnemySnapshot> enemySnapshots;
    private final ObjectMap<String, NpcSnapshot> npcSnapshots;
    private final ObjectMap<String, NpcDeadSnapshot> npcDeadSnapshots;
    private final Array<DroppedCoinState> droppedCoins;
    private final ObjectMap<String, com.github.MichalKC.manylands.component.Item[]> storageSlots;
    private final Array<String> pickedUpItems;

    public MapWorldState() {
        this.interactables = new ObjectMap<>();
        this.enemyDeaths = new ObjectMap<>();
        this.enemyLifeStates = new ObjectMap<>();
        this.enemySnapshots = new ObjectMap<>();
        this.npcSnapshots = new ObjectMap<>();
        this.npcDeadSnapshots = new ObjectMap<>();
        this.droppedCoins = new Array<>();
        this.storageSlots = new ObjectMap<>();
        this.pickedUpItems = new Array<>();
    }

    public MapWorldState(MapWorldState other) {
        this.interactables = new ObjectMap<>(other.interactables);
        this.enemyDeaths = new ObjectMap<>(other.enemyDeaths);
        this.enemyLifeStates = new ObjectMap<>(other.enemyLifeStates);
        this.enemySnapshots = new ObjectMap<>(other.enemySnapshots);
        this.npcSnapshots = new ObjectMap<>(other.npcSnapshots);
        this.npcDeadSnapshots = new ObjectMap<>(other.npcDeadSnapshots);
        this.droppedCoins = new Array<>(other.droppedCoins);
        this.storageSlots = new ObjectMap<>();
        for (ObjectMap.Entry<String, com.github.MichalKC.manylands.component.Item[]> entry : other.storageSlots.entries()) {
            this.storageSlots.put(entry.key, entry.value != null ? entry.value.clone() : null);
        }
        this.pickedUpItems = new Array<>(other.pickedUpItems);
    }

    public ObjectMap<String, InteractableObjectState> getInteractables() {
        return interactables;
    }

    public InteractableObjectState get(String objectKey) {
        return interactables.get(objectKey);
    }

    public void put(String objectKey, InteractableObjectState state) {
        interactables.put(objectKey, state);
    }

    public ObjectMap<String, EnemyDeathRecord> getEnemyDeaths() {
        return enemyDeaths;
    }

    public EnemyDeathRecord getEnemyDeath(String objectKey) {
        return enemyDeaths.get(objectKey);
    }

    public void putEnemyDeath(String objectKey, EnemyDeathRecord record) {
        enemyDeaths.put(objectKey, record);
    }

    public void removeEnemyDeath(String objectKey) {
        enemyDeaths.remove(objectKey);
    }

    public void putEnemyLife(String objectKey, float life) {
        enemyLifeStates.put(objectKey, life);
    }

    public Float getEnemyLife(String objectKey) {
        return enemyLifeStates.get(objectKey);
    }

    public void putEnemySnapshot(String objectKey, EnemySnapshot snapshot) {
        enemySnapshots.put(objectKey, snapshot);
    }

    public EnemySnapshot getEnemySnapshot(String objectKey) {
        return enemySnapshots.get(objectKey);
    }

    public void putNpcSnapshot(String objectKey, NpcSnapshot snapshot) {
        npcSnapshots.put(objectKey, snapshot);
    }

    public NpcSnapshot getNpcSnapshot(String objectKey) {
        return npcSnapshots.get(objectKey);
    }

    public void putNpcDeadSnapshot(String objectKey, NpcDeadSnapshot snapshot) {
        npcDeadSnapshots.put(objectKey, snapshot);
    }

    public NpcDeadSnapshot getNpcDeadSnapshot(String objectKey) {
        return npcDeadSnapshots.get(objectKey);
    }

    public Array<DroppedCoinState> getDroppedCoins() {
        return droppedCoins;
    }

    public void setDroppedCoins(Array<DroppedCoinState> coins) {
        droppedCoins.clear();
        droppedCoins.addAll(coins);
    }

    public ObjectMap<String, com.github.MichalKC.manylands.component.Item[]> getStorageSlots() {
        return storageSlots;
    }

    public void putStorageSlots(String objectKey, com.github.MichalKC.manylands.component.Item[] slots) {
        storageSlots.put(objectKey, slots);
    }

    public com.github.MichalKC.manylands.component.Item[] getStorageSlots(String objectKey) {
        return storageSlots.get(objectKey);
    }

    public Array<String> getPickedUpItems() {
        return pickedUpItems;
    }

    public void addPickedUpItem(String itemId) {
        pickedUpItems.add(itemId);
    }

    public boolean isItemPickedUp(String itemId) {
        return pickedUpItems.contains(itemId, false);
    }
}
