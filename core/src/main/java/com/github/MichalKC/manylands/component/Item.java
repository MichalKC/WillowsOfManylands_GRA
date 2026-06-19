package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Item implements Component {
    public static final ComponentMapper<Item> MAPPER = ComponentMapper.getFor(Item.class);

    private final String id;
    private final String name;
    private final String texturePath;
    private final int value;
    private final int price;
    private boolean markedForPickup = false;
    private String wear;
    private boolean eat;
    private boolean read;
    private String description;
    private int plusHP;
    private int plusAttack1;
    private int plusAttack2;
    private int plusAttack3;
    private int plusDefense;
    private int plusSpeed;

    public Item(String id, String name, String texturePath) {
        this(id, name, texturePath, 0, 0);
    }

    public Item(String id, String name, String texturePath, int value) {
        this(id, name, texturePath, value, 0);
    }

    public Item(String id, String name, String texturePath, int value, int price) {
        this.id = id;
        this.name = name;
        this.texturePath = texturePath;
        this.value = value;
        this.price = price;
    }

    public int getValue() {
        return value;
    }

    public int getPrice() {
        return price;
    }

    public boolean isSellable() {
        return value > 0;
    }

    public boolean isBuyable() {
        return price > 0;
    }

    public boolean isMarkedForPickup() {
        return markedForPickup;
    }

    public void setMarkedForPickup(boolean markedForPickup) {
        this.markedForPickup = markedForPickup;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public String getWear() {
        return wear;
    }

    public void setWear(String wear) {
        this.wear = wear;
    }

    public int getPlusHP()       { return plusHP; }
    public int getPlusAttack1()  { return plusAttack1; }
    public int getPlusAttack2()  { return plusAttack2; }
    public int getPlusAttack3()  { return plusAttack3; }
    public int getPlusDefense()  { return plusDefense; }
    public int getPlusSpeed()    { return plusSpeed; }

    public boolean isEat()             { return eat; }
    public void setEat(boolean v)       { this.eat = v; }

    public boolean isRead()            { return read; }
    public void setRead(boolean v)      { this.read = v; }

    public String getDescription()      { return description; }
    public void setDescription(String d){ this.description = d; }

    public void setPlusHP(int v)       { this.plusHP = v; }
    public void setPlusAttack1(int v)  { this.plusAttack1 = v; }
    public void setPlusAttack2(int v)  { this.plusAttack2 = v; }
    public void setPlusAttack3(int v)  { this.plusAttack3 = v; }
    public void setPlusDefense(int v)  { this.plusDefense = v; }
    public void setPlusSpeed(int v)    { this.plusSpeed = v; }
}
