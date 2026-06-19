package com.github.MichalKC.manylands.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import java.util.HashSet;
import java.util.Set;

public class DamageZone implements Component {
    public static final ComponentMapper<DamageZone> MAPPER = ComponentMapper.getFor(DamageZone.class);

    private final float damage;
    private final boolean canDamageEnemies;
    private Entity overlappingPlayer;
    private final Set<Entity> overlappingEnemies = new HashSet<>();

    public DamageZone(float damage, boolean canDamageEnemies) {
        this.damage = Math.max(0f, damage);
        this.canDamageEnemies = canDamageEnemies;
    }

    public float getDamage()             { return damage; }
    public boolean isCanDamageEnemies()  { return canDamageEnemies; }

    public void setOverlappingPlayer(Entity player) { this.overlappingPlayer = player; }
    public Entity getOverlappingPlayer()            { return overlappingPlayer; }

    public void addOverlappingEnemy(Entity enemy)    { overlappingEnemies.add(enemy); }
    public void removeOverlappingEnemy(Entity enemy) { overlappingEnemies.remove(enemy); }
    public Set<Entity> getOverlappingEnemies()       { return overlappingEnemies; }
}
