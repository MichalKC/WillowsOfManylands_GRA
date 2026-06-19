package com.github.MichalKC.manylands.combat;

import com.github.MichalKC.manylands.asset.MusicAsset;

import java.util.HashMap;
import java.util.Map;

public final class CombatEnemyRegistry {
    private static final Map<String, CombatConfig> ENEMIES = new HashMap<>();

    static {
        register(new CombatConfig(
            "catWarrior",
            "CAT WARRIOR",
            180f,
            "combat/catWarrior.png",
            null,
            new String[]{"pellets", "lasers", "hearts", "mixed", "bombsLasers"},
            new String[]{
                "Meow! I'm the Cat Warrior!",
                "Prepare yourself, human!",
                "My claws are sharp..."
            },
            new String[]{
                "Hiss...",
                "You're pretty tough!",
                "I won't go easy on you!",
                "Feel my fury!",
                "MEOW!"
            },
            3f,
            30f,
            3,
            MusicAsset.COMBAT_DYNAMIC
        ));

        register(new CombatConfig(
            "madPot",
            "MAD POT",
            200f,
            "combat/madPot.png",
            null,
            new String[]{"spoons", "fallingPot", "knives", "forks"},
            new String[]{
                "I bubble with rage! I'm the Mad Pot!",
                "Get ready for a cutlery storm!",
                "Soup of the day: defeat."
            },
            new String[]{
                "Spoons incoming!",
                "Mind the kni.. I mean knives!",
                "A boiling pot from the sky!",
                "Forks are swirling!",
                "Stir, stir, stir..."
            },
            3f,
            30f,
            5,
            MusicAsset.COMBAT_DYNAMIC
        ));
    }

    private CombatEnemyRegistry() {}

    public static void register(CombatConfig config) {
        ENEMIES.put(config.getId().toLowerCase(), config);
    }

    public static CombatConfig get(String id) {
        CombatConfig config = ENEMIES.get(id.toLowerCase());
        if (config == null) {
            throw new IllegalArgumentException("Unknown combat enemy: " + id);
        }
        return config;
    }

    public static boolean has(String id) {
        return ENEMIES.containsKey(id.toLowerCase());
    }
}
