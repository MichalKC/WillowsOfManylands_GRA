package com.github.MichalKC.manylands.ui.model;

import java.util.HashSet;
import java.util.Set;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.component.Inventory;
import com.github.MichalKC.manylands.component.Storage;
import com.github.MichalKC.manylands.component.Item;

public class InventoryViewModel extends ViewModel {

    public static final String PROP_STATS = "stats";

    public static final String PROP_TAB = "tab";
    public static final String PROP_SLOT = "slot";
    public static final String PROP_PAGE = "page";

    public static final int TAB_INVENTORY = 0;
    public static final int TAB_QUESTS = 1;
    public static final int TAB_SKILLS = 2;

    public static final String[] EQUIP_SLOT_NAMES = {"head", "chest", "gloves", "weapon", "specialObject", "boots"};

    public static final int GRID_COLS = 4;
    public static final int GRID_ROWS = 4;
    public static final int TOTAL_PAGES = 5;

    private int currentTab = TAB_INVENTORY;
    private int selectedSlot = 0;
    private int currentPage = 0;

    private int statLife = 0;
    private int statAttack = 0;
    private int statDefense = 2;
    private int statSpeed = 0;
    private int coins = 0;

    private Inventory playerInventory;
    private Storage storage;
    private boolean storageMode = false;

    private int heldSlot = -1;
    private boolean heldInStorage = false;
    private boolean heldInEquip = false;
    private Runnable equipChangedCallback;

    public void setEquipChangedCallback(Runnable callback) { this.equipChangedCallback = callback; }
    private void fireEquipChanged() { if (equipChangedCallback != null) equipChangedCallback.run(); }

    public int getPersistedActivatedSkillSlot() {
        return playerInventory != null ? playerInventory.getPersistedActivatedSkillSlot() : -1;
    }

    public void setPersistedActivatedSkillSlot(int slot) {
        if (playerInventory != null) {
            playerInventory.setPersistedActivatedSkillSlot(slot);
        }
    }

    public void unlockSkill(int slotIdx) {
        if (playerInventory != null) {
            playerInventory.getUnlockedSkills().add(slotIdx);
        }
    }

    public boolean isSkillUnlocked(int slotIdx) {
        return playerInventory != null && playerInventory.getUnlockedSkills().contains(slotIdx);
    }

    public Set<Integer> getUnlockedSkills() {
        return playerInventory != null ? playerInventory.getUnlockedSkills() : new HashSet<>();
    }

    public InventoryViewModel(GdxGame game) {
        super(game);
    }

    public int getStatLife()    { return statLife; }
    public int getStatAttack()  { return statAttack; }
    public int getStatDefense() { return statDefense; }
    public int getStatSpeed()   { return statSpeed; }
    public int getCoins()       { return coins; }

    public void setPlayerInventory(Inventory inv) {
        this.playerInventory = inv;
        propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
        this.storageMode = storage != null;
        propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
    }

    public Inventory getPlayerInventory() {
        return playerInventory;
    }

    public Storage getStorage() {
        return storage;
    }

    public int getHeldSlot() {
        return heldSlot;
    }

    public boolean isHeldInStorage() {
        return heldInStorage;
    }

    public boolean isHeldInEquip() {
        return heldInEquip;
    }

    public Item[] getEquipSlots() {
        return playerInventory != null ? playerInventory.getEquipSlots() : null;
    }

    public void resetHeld() {
        heldSlot = -1;
        heldInStorage = false;
        heldInEquip = false;
        propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
    }

    public void selectOrMoveEquip(int equipIdx) {
        if (playerInventory == null) return;
        Item[] playerSlots = playerInventory.getSlots();
        Item[] equipSlots = playerInventory.getEquipSlots();
        if (equipIdx < 0 || equipIdx >= equipSlots.length) return;

        if (heldSlot == -1) {
            if (equipSlots[equipIdx] != null) {
                heldSlot = equipIdx;
                heldInEquip = true;
                heldInStorage = false;
                propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
            }
        } else {
            Item heldItem;
            if (heldInEquip) {
                heldItem = equipSlots[heldSlot];
            } else if (heldInStorage) {
                Item[] storageSlots = storage != null ? storage.getSlots() : null;
                heldItem = storageSlots != null ? storageSlots[heldSlot] : null;
            } else {
                heldItem = playerSlots[heldSlot];
            }
            String requiredWear = equipIdx < EQUIP_SLOT_NAMES.length ? EQUIP_SLOT_NAMES[equipIdx] : null;
            if (heldItem == null || requiredWear == null || !requiredWear.equals(heldItem.getWear())) {
                heldSlot = -1;
                heldInEquip = false;
                heldInStorage = false;
                propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
                return;
            }
            if (heldInEquip) {
                equipSlots[heldSlot] = equipSlots[equipIdx];
            } else if (heldInStorage) {
                Item[] storageSlots = storage != null ? storage.getSlots() : null;
                if (storageSlots != null) storageSlots[heldSlot] = equipSlots[equipIdx];
            } else {
                playerSlots[heldSlot] = equipSlots[equipIdx];
            }
            equipSlots[equipIdx] = heldItem;
            heldSlot = -1;
            heldInEquip = false;
            heldInStorage = false;
            propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
            fireEquipChanged();
        }
    }

    public void selectOrMoveItem(int targetSlot, boolean targetInStorage) {
        if (playerInventory == null) return;

        Item[] playerSlots = playerInventory.getSlots();
        Item[] storageSlots = storage != null ? storage.getSlots() : null;
        Item[] equipSlots = playerInventory.getEquipSlots();

        if (heldSlot == -1) {
            Item targetItem = targetInStorage
                ? (storageSlots != null ? storageSlots[targetSlot] : null)
                : playerSlots[targetSlot];
            if (targetItem != null) {
                heldSlot = targetSlot;
                heldInStorage = targetInStorage;
                heldInEquip = false;
                propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
            }
        } else {
            Item heldItem;
            boolean wasHeldInEquip = heldInEquip;
            if (heldInEquip) {
                heldItem = equipSlots[heldSlot];
                Item targetItem = targetInStorage
                    ? (storageSlots != null ? storageSlots[targetSlot] : null)
                    : playerSlots[targetSlot];
                String heldEquipSlotName = heldSlot < EQUIP_SLOT_NAMES.length ? EQUIP_SLOT_NAMES[heldSlot] : null;
                if (targetItem != null && (heldEquipSlotName == null || !heldEquipSlotName.equals(targetItem.getWear()))) {
                    heldSlot = -1;
                    heldInEquip = false;
                    heldInStorage = false;
                    propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
                    return;
                }
                equipSlots[heldSlot] = targetItem;
            } else if (heldInStorage) {
                heldItem = storageSlots != null ? storageSlots[heldSlot] : null;
                Item targetItem = targetInStorage
                    ? (storageSlots != null ? storageSlots[targetSlot] : null)
                    : playerSlots[targetSlot];
                if (storageSlots != null) storageSlots[heldSlot] = targetItem;
            } else {
                heldItem = playerSlots[heldSlot];
                playerSlots[heldSlot] = targetInStorage
                    ? (storageSlots != null ? storageSlots[targetSlot] : null)
                    : playerSlots[targetSlot];
            }

            if (targetInStorage) {
                if (storageSlots != null) storageSlots[targetSlot] = heldItem;
            } else {
                playerSlots[targetSlot] = heldItem;
            }

            heldSlot = -1;
            heldInStorage = false;
            heldInEquip = false;
            propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
            if (wasHeldInEquip) fireEquipChanged();
        }
    }

    public void updateStats(int life, int attack, int defense, int speed, int coins) {
        this.statLife    = life;
        this.statAttack  = attack;
        this.statDefense = defense;
        this.statSpeed   = speed;
        this.coins       = coins;
        propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setTab(int tab) {
        if (tab < 0 || tab > 2 || tab == currentTab) return;
        int old = currentTab;
        currentTab = tab;
        propertyChangeSupport.firePropertyChange(PROP_TAB, old, tab);
    }

    public void selectSlot(int slot) {
        int total = GRID_COLS * GRID_ROWS;
        if (slot < 0 || slot >= total) return;
        int old = selectedSlot;
        selectedSlot = slot;
        propertyChangeSupport.firePropertyChange(PROP_SLOT, old, slot);
    }

    public void nextPage() {
        if (currentPage < TOTAL_PAGES - 1) {
            int old = currentPage;
            currentPage++;
            propertyChangeSupport.firePropertyChange(PROP_PAGE, old, currentPage);
        }
    }

    public void prevPage() {
        if (currentPage > 0) {
            int old = currentPage;
            currentPage--;
            propertyChangeSupport.firePropertyChange(PROP_PAGE, old, currentPage);
        }
    }

    public void onLeft() {
        if (currentTab != TAB_INVENTORY) return;
        int col = selectedSlot % GRID_COLS;
        if (col > 0) {
            selectSlot(selectedSlot - 1);
        }
    }

    public void onRight() {
        if (currentTab != TAB_INVENTORY) return;
        int col = selectedSlot % GRID_COLS;
        if (col < GRID_COLS - 1) {
            selectSlot(selectedSlot + 1);
        }
    }

    public void onUp() {
        if (currentTab != TAB_INVENTORY) return;
        if (selectedSlot >= GRID_COLS) {
            selectSlot(selectedSlot - GRID_COLS);
        }
    }

    public void onDown() {
        if (currentTab != TAB_INVENTORY) return;
        int total = GRID_COLS * GRID_ROWS;
        if (selectedSlot + GRID_COLS < total) {
            selectSlot(selectedSlot + GRID_COLS);
        }
    }

    public Item getItemAtSlot(int slot, boolean inStorage) {
        if (playerInventory == null) return null;
        if (inStorage) {
            Item[] storageSlots = storage != null ? storage.getSlots() : null;
            return (storageSlots != null && slot >= 0 && slot < storageSlots.length) ? storageSlots[slot] : null;
        }
        Item[] playerSlots = playerInventory.getSlots();
        return (slot >= 0 && slot < playerSlots.length) ? playerSlots[slot] : null;
    }

    public void removeItemFromSlot(int slot, boolean inStorage) {
        if (playerInventory == null) return;
        if (inStorage) {
            Item[] storageSlots = storage != null ? storage.getSlots() : null;
            if (storageSlots != null && slot >= 0 && slot < storageSlots.length) {
                storageSlots[slot] = null;
            }
        } else {
            Item[] playerSlots = playerInventory.getSlots();
            if (slot >= 0 && slot < playerSlots.length) {
                playerSlots[slot] = null;
            }
        }
        propertyChangeSupport.firePropertyChange(PROP_STATS, null, this);
    }
}
