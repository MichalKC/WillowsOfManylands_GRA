package com.github.MichalKC.manylands.ui.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.MichalKC.manylands.GdxGame;
import com.github.MichalKC.manylands.asset.SoundAsset;
import com.github.MichalKC.manylands.audio.AudioService;

import java.util.Map;

public class GameViewModel extends ViewModel {
    public static final String LIFE_POINTS = "lifePoints";
    public static final String MAX_LIFE = "maxLife";
    public static final String PLAYER_DAMAGE = "playerDamage";
    public static final String INTERACT_HINT = "interactHint";
    public static final String COINS = "coins";
    public static final String TEXT_DISPLAY = "textDisplay";
    public static final String TALKING_DISPLAY = "talkingDisplay";
    public static final String TALKING_OPTIONS = "talkingOptions";
    public static final String SELECTED_SKILL = "selectedSkill";
    public static final String SPECIAL_CHARGE = "specialCharge";
    public static final String SPECIAL_DRAINING = "specialDraining";
    public static final String ACTIVE_EFFECTS = "activeEffects";

    private final AudioService audioService;
    private int lifePoints;
    private int maxLife;
    private int coins;
    private Map.Entry<Vector2, Integer> playerDamage;
    private final Vector2 tmpVec2;
    private boolean interactHintVisible;
    private String textDisplay;
    private String talkingDisplay;
    private String talkingOptions;
    private int selectedSkillHudIdx = -1;
    private int skillWidgetStateOrdinal = 0;
    private float skillWidgetTimer = 0f;
    private float specialCharge = 0f; // 0..1
    private boolean specialDraining = false;
    private boolean specialAlwaysUsable = false;
    private java.util.Set<String> activeEffects = new java.util.HashSet<>();

    public GameViewModel(GdxGame game) {
        super(game);
        this.audioService = game.getAudioService();
        this.lifePoints = 0;
        this.maxLife = 0;
        this.coins = 0;
        this.playerDamage = null;
        this.tmpVec2 = new Vector2();
        this.interactHintVisible = false;
        this.textDisplay = null;
        this.talkingDisplay = null;
        this.talkingOptions = null;
    }

    public float getSpecialCharge() { return specialCharge; }

    public void setSpecialCharge(float value) {
        float v = MathUtils.clamp(value, 0f, 1f);
        if (this.specialCharge != v) {
            this.propertyChangeSupport.firePropertyChange(SPECIAL_CHARGE, this.specialCharge, v);
            this.specialCharge = v;
        } else {
            this.specialCharge = v;
        }
    }

    public boolean isSpecialDraining() { return specialDraining; }

    public void setSpecialDraining(boolean draining) {
        if (this.specialDraining != draining) {
            this.propertyChangeSupport.firePropertyChange(SPECIAL_DRAINING, this.specialDraining, draining);
            this.specialDraining = draining;
        } else {
            this.specialDraining = draining;
        }
    }

    public boolean canTriggerSpecial() {
        return !specialDraining && (specialAlwaysUsable || specialCharge >= 1f);
    }

    public void addSpecialChargeByDamage(int damage) {
        if (damage <= 0) return;
        if (specialDraining) return;
        if (specialCharge >= 1f) return;
        float inc = (float) damage / 50f;
        setSpecialCharge(specialCharge + inc);
    }

    public boolean isSpecialAlwaysUsable() { return specialAlwaysUsable; }
    public void setSpecialAlwaysUsable(boolean v) { this.specialAlwaysUsable = v; }

    public int getSelectedSkillHudIdx() {
        return selectedSkillHudIdx;
    }

    public void setSelectedSkillHudIdx(int hudIdx) {
        if (this.selectedSkillHudIdx != hudIdx) {
            int old = this.selectedSkillHudIdx;
            this.selectedSkillHudIdx = hudIdx;
            propertyChangeSupport.firePropertyChange(SELECTED_SKILL, old, hudIdx);
        }
    }

    public int getSkillWidgetStateOrdinal() { return skillWidgetStateOrdinal; }
    public float getSkillWidgetTimer()       { return skillWidgetTimer; }
    public void saveSkillWidgetState(int stateOrdinal, float timer) {
        this.skillWidgetStateOrdinal = stateOrdinal;
        this.skillWidgetTimer = timer;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        if (coins < 0) coins = 0;
        if (this.coins != coins) {
            this.propertyChangeSupport.firePropertyChange(COINS, this.coins, coins);
            this.coins = coins;
        }
    }

    public void addCoins(int delta) {
        setCoins(this.coins + delta);
    }

    public void setInteractHintVisible(boolean visible) {
        if (this.interactHintVisible != visible) {
            this.propertyChangeSupport.firePropertyChange(INTERACT_HINT, this.interactHintVisible, visible);
            this.interactHintVisible = visible;
        }
    }

    public boolean isInteractHintVisible() {
        return interactHintVisible;
    }

    public void setMaxLife(int maxLife) {
        if (this.maxLife != maxLife) {
            this.propertyChangeSupport.firePropertyChange(MAX_LIFE, this.maxLife, maxLife);
        }
        this.maxLife = maxLife;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public void setLifePoints(int lifePoints) {
        if (this.lifePoints != lifePoints) {
            this.propertyChangeSupport.firePropertyChange(LIFE_POINTS, this.lifePoints, lifePoints);
            if (this.lifePoints != 0 && this.lifePoints < lifePoints) {
                audioService.playSound(SoundAsset.LIFE_REG);
            }
        }
        this.lifePoints = lifePoints;
    }

    public int getLifePoints() {
        return lifePoints;
    }

    public void updateLifeInfo(float maxLife, float life) {
        int uiMaxLife = MathUtils.floor(maxLife);
        int uiLifePoints = life <= 0f ? 0 : MathUtils.ceil(life);
        uiLifePoints = Math.min(uiLifePoints, uiMaxLife);
        setMaxLife(uiMaxLife);
        setLifePoints(uiLifePoints);
    }

    public void playerDamage(int amount, float x, float y) {
        Vector2 position = new Vector2(x, y);
        this.playerDamage = Map.entry(position, amount);
        this.propertyChangeSupport.firePropertyChange(PLAYER_DAMAGE, null, this.playerDamage);
    }

    public void showText(String text) {
        if (!text.equals(this.textDisplay)) {
            this.propertyChangeSupport.firePropertyChange(TEXT_DISPLAY, this.textDisplay, text);
            this.textDisplay = text;
        }
    }

    public void hideText() {
        if (this.textDisplay != null) {
            this.propertyChangeSupport.firePropertyChange(TEXT_DISPLAY, this.textDisplay, null);
            this.textDisplay = null;
        }
    }

    public String getTextDisplay() {
        return textDisplay;
    }

    public void showTalkingText(String text) {
        if (!text.equals(this.talkingDisplay)) {
            this.propertyChangeSupport.firePropertyChange(TALKING_DISPLAY, this.talkingDisplay, text);
            this.talkingDisplay = text;
        }
    }

    public void hideTalkingText() {
        if (this.talkingDisplay != null) {
            this.propertyChangeSupport.firePropertyChange(TALKING_DISPLAY, this.talkingDisplay, null);
            this.talkingDisplay = null;
        }
        hideTalkingOptions();
    }

    public String getTalkingDisplay() {
        return talkingDisplay;
    }

    public void showTalkingOptions(String options) {
        if (options != null && !options.equals(this.talkingOptions)) {
            this.propertyChangeSupport.firePropertyChange(TALKING_OPTIONS, this.talkingOptions, options);
            this.talkingOptions = options;
        }
    }

    public void hideTalkingOptions() {
        if (this.talkingOptions != null) {
            this.propertyChangeSupport.firePropertyChange(TALKING_OPTIONS, this.talkingOptions, null);
            this.talkingOptions = null;
        }
    }

    public Vector2 toScreenCoords(Vector2 position) {
        tmpVec2.set(position);
        game.getViewport().project(tmpVec2);
        return tmpVec2;

    }

    public java.util.Set<String> getActiveEffects() {
        return activeEffects;
    }

    public void addEffect(String effect) {
        if (activeEffects.add(effect)) {
            propertyChangeSupport.firePropertyChange(ACTIVE_EFFECTS, null, activeEffects);
        }
    }

    public void removeEffect(String effect) {
        if (activeEffects.remove(effect)) {
            propertyChangeSupport.firePropertyChange(ACTIVE_EFFECTS, null, activeEffects);
        }
    }

    public void clearEffects() {
        if (!activeEffects.isEmpty()) {
            activeEffects.clear();
            propertyChangeSupport.firePropertyChange(ACTIVE_EFFECTS, null, activeEffects);
        }
    }
}
