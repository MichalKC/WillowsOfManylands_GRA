package com.github.MichalKC.manylands.world;

public class EnemyDeathRecord {
    private final long deathTimeMillis;
    private final float respawnDelaySec;
    private boolean playerLeftSinceDeath;

    public EnemyDeathRecord(long deathTimeMillis, float respawnDelaySec, boolean playerLeftSinceDeath) {
        this.deathTimeMillis = deathTimeMillis;
        this.respawnDelaySec = respawnDelaySec;
        this.playerLeftSinceDeath = playerLeftSinceDeath;
    }

    public long getDeathTimeMillis() {
        return deathTimeMillis;
    }

    public float getRespawnDelaySec() {
        return respawnDelaySec;
    }

    public boolean isPlayerLeftSinceDeath() {
        return playerLeftSinceDeath;
    }

    public void setPlayerLeftSinceDeath(boolean playerLeftSinceDeath) {
        this.playerLeftSinceDeath = playerLeftSinceDeath;
    }

    public boolean canRespawn(long currentTimeMillis) {
        if (!playerLeftSinceDeath) {
            return false;
        }
        long elapsedMillis = currentTimeMillis - deathTimeMillis;
        return elapsedMillis >= (long) (respawnDelaySec * 1000f);
    }
}
