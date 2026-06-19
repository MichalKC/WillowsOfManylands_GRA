package com.github.MichalKC.manylands.combat;

import com.github.MichalKC.manylands.combat.attack.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class AttackPatternFactory {
    private static final Map<String, Supplier<AttackPattern>> REGISTRY = new HashMap<>();

    static {
        register("pellets", PelletsAttack::new);
        register("lasers",  LasersAttack::new);
        register("hearts",  HeartsAttack::new);
        register("mixed",   MixedAttack::new);
        register("bombsLasers", BombsLasersAttack::new);
        register("spoons", SpoonsAttack::new);
        register("fallingPot", FallingPotAttack::new);
        register("knives", KnifeBarrageAttack::new);
        register("forks", SpinningForksAttack::new);
    }

    private AttackPatternFactory() {}

    public static void register(String name, Supplier<AttackPattern> supplier) {
        REGISTRY.put(name.toLowerCase(), supplier);
    }

    public static AttackPattern create(String name) {
        Supplier<AttackPattern> supplier = REGISTRY.get(name.toLowerCase());
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown attack pattern: " + name);
        }
        AttackPattern pattern = supplier.get();
        pattern.reset();
        return pattern;
    }

    public static AttackPattern[] createAll(String[] names) {
        AttackPattern[] result = new AttackPattern[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = create(names[i].trim());
        }
        return result;
    }
}
