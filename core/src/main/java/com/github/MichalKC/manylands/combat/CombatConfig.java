package com.github.MichalKC.manylands.combat;

import com.github.MichalKC.manylands.asset.MusicAsset;

public class CombatConfig {
    private final String id;
    private final String displayName;
    private final float maxHp;
    private final String imagePath;
    private final String damageImagePath;
    private final String[] attackPatterns;
    private final String[] dialogLines;
    private final String[] turnLines;
    private final float projectileDamage;
    private final float playerAttackDamage;
    private final int mercyThreshold;
    private final MusicAsset musicAsset;

    public CombatConfig(
        String id, String displayName, float maxHp,
        String imagePath, String damageImagePath,
        String[] attackPatterns, String[] dialogLines, String[] turnLines,
        float projectileDamage, float playerAttackDamage, int mercyThreshold,
        MusicAsset musicAsset
    ) {
        this.id = id;
        this.displayName = displayName;
        this.maxHp = maxHp;
        this.imagePath = imagePath;
        this.damageImagePath = damageImagePath;
        this.attackPatterns = attackPatterns;
        this.dialogLines = dialogLines;
        this.turnLines = turnLines;
        this.projectileDamage = projectileDamage;
        this.playerAttackDamage = playerAttackDamage;
        this.mercyThreshold = mercyThreshold;
        this.musicAsset = musicAsset;
    }

    public String getId()                 { return id; }
    public String getDisplayName()        { return displayName; }
    public float getMaxHp()               { return maxHp; }
    public String getImagePath()          { return imagePath; }
    public String getDamageImagePath()    { return damageImagePath; }
    public String[] getAttackPatterns()   { return attackPatterns; }
    public String[] getDialogLines()      { return dialogLines; }
    public String[] getTurnLines()        { return turnLines; }
    public float getProjectileDamage()    { return projectileDamage; }
    public float getPlayerAttackDamage()  { return playerAttackDamage; }
    public int getMercyThreshold()        { return mercyThreshold; }
    public MusicAsset getMusicAsset()      { return musicAsset; }
}
