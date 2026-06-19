package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Inventory implements Component {
    public static final ComponentMapper<Inventory> MAPPER = ComponentMapper.getFor(Inventory.class);
    public static final int EQUIP_SLOTS = 6;

    private final Item[] slots;
    private final Item[] equipSlots;
    private int equipDefenseBonus = 0;

    private final java.util.Set<Integer> unlockedSkills = new java.util.HashSet<>();
    private int persistedActivatedSkillSlot = -1;

    public Inventory(int size) {
        this.slots = new Item[size];
        this.equipSlots = new Item[EQUIP_SLOTS];
    }

    public java.util.Set<Integer> getUnlockedSkills() {
        return unlockedSkills;
    }

    public int getPersistedActivatedSkillSlot() {
        return persistedActivatedSkillSlot;
    }

    public void setPersistedActivatedSkillSlot(int slot) {
        this.persistedActivatedSkillSlot = slot;
    }

    public Item[] getSlots() {
        return slots;
    }

    public Item[] getEquipSlots() {
        return equipSlots;
    }

    public boolean addItem(Item item) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                slots[i] = item;
                return true;
            }
        }
        return false;
    }

    public int getEquipDefenseBonus() { return equipDefenseBonus; }
    public void setEquipDefenseBonus(int bonus) { this.equipDefenseBonus = bonus; }
}
