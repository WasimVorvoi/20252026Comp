package com.example.voidfall.level;

import com.example.voidfall.entity.Boss;
import com.example.voidfall.entity.Enemy;
import com.example.voidfall.entity.EchoTitanBoss;
import com.example.voidfall.entity.FastEnemy;
import com.example.voidfall.entity.HeavyEnemy;
import com.example.voidfall.entity.MirrorBeastBoss;
import com.example.voidfall.entity.RangedEnemy;
import com.example.voidfall.entity.ShatterKingBoss;

import java.util.List;

/**
 * LevelManager — owns all LevelConfig objects and manages progression.
 *
 * RESPONSIBILITIES:
 *   1. Build and store configs for all 9 levels.
 *   2. Track the current level number.
 *   3. Provide getConfig(levelNumber) for GameView / ObjectiveManager.
 *   4. Provide spawnEnemies() — places enemies at valid positions on screen.
 *   5. Provide createBoss() — instantiates the correct Boss subclass.
 *   6. advance() — increments level, returns false when game is complete.
 *
 * LEVEL OVERVIEW (each introduces a new mechanic):
 *   1  First Fracture    — basic tilt + fracture intro        DEFEAT_ALL 5
 *   2  Shardfall         — shards on drifting pieces          COLLECT_SHARDS 3
 *   3  BOSS: Shatter King— boss forces mass fractures         BOSS
 *   4  Echo Grounds      — echo shadows accumulate            DEFEAT_ALL 6
 *   5  Core Defense      — protect central core               PROTECT_CORE
 *   6  Seal Sequence     — activate seals in order on pieces  ACTIVATE_SEALS 4
 *   7  BOSS: Mirror Beast— tilt-reversal zones                BOSS
 *   8  Echo Storm        — echo generator enemies             SURVIVE_STORM 30s
 *   9  BOSS: Echo Titan  — all systems, final boss            BOSS
 */
public class LevelManager {

    private static final int TOTAL_LEVELS = 9;

    private LevelConfig[] configs;
    private int currentLevel = 1;

    public LevelManager() {
        buildConfigs();
    }

    public void reset() {
        currentLevel = 1;
    }

    // =========================================================================
    // Config construction — one entry per level
    // =========================================================================

    private void buildConfigs() {
        configs = new LevelConfig[TOTAL_LEVELS + 1]; // 1-indexed

        // ----- Level 1: First Fracture -----
        LevelConfig l1            = new LevelConfig();
        l1.levelNumber            = 1;
        l1.name                   = "First Fracture";
        l1.objectiveDescription   = "Defeat 5 enemies before the arena fractures 3 times. Learn to move with the shifting platforms.";
        l1.objectiveType          = LevelConfig.OBJ_DEFEAT_ALL;
        l1.targetCount            = 5;
        l1.enemyTypes             = new int[]{LevelConfig.ENEMY_FAST};
        l1.enemyCounts            = new int[]{5};
        l1.initialFractureCount   = 0;
        l1.echoShadowsActive      = false;
        configs[1]                = l1;

        // ----- Level 2: Shardfall -----
        LevelConfig l2            = new LevelConfig();
        l2.levelNumber            = 2;
        l2.name                   = "Shardfall";
        l2.objectiveDescription   = "Collect 3 Void Shards scattered across the drifting arena pieces. Enemies will try to knock you into the gaps.";
        l2.objectiveType          = LevelConfig.OBJ_COLLECT_SHARDS;
        l2.targetCount            = 3;
        l2.enemyTypes             = new int[]{LevelConfig.ENEMY_FAST, LevelConfig.ENEMY_RANGED};
        l2.enemyCounts            = new int[]{3, 2};
        l2.initialFractureCount   = 1;
        l2.echoShadowsActive      = false;
        configs[2]                = l2;

        // ----- Level 3: BOSS — Shatter King -----
        LevelConfig l3            = new LevelConfig();
        l3.levelNumber            = 3;
        l3.name                   = "Shatter King";
        l3.objectiveDescription   = "Defeat the Shatter King. He will fracture the arena violently — navigate the breaking tiles to survive.";
        l3.objectiveType          = LevelConfig.OBJ_DEFEAT_ALL;
        l3.targetCount            = 1;
        l3.enemyTypes             = new int[]{};
        l3.enemyCounts            = new int[]{};
        l3.initialFractureCount   = 0;
        l3.isBossLevel            = true;
        l3.bossType               = LevelConfig.BOSS_SHATTER_KING;
        configs[3]                = l3;

        // ----- Level 4: Echo Grounds -----
        LevelConfig l4            = new LevelConfig();
        l4.levelNumber            = 4;
        l4.name                   = "Echo Grounds";
        l4.objectiveDescription   = "Defeat 6 enemies. Each one leaves an echo shadow. Chain kills away from corridors to avoid trapping yourself.";
        l4.objectiveType          = LevelConfig.OBJ_DEFEAT_ALL;
        l4.targetCount            = 6;
        l4.enemyTypes             = new int[]{LevelConfig.ENEMY_FAST, LevelConfig.ENEMY_HEAVY, LevelConfig.ENEMY_RANGED};
        l4.enemyCounts            = new int[]{2, 2, 2};
        l4.initialFractureCount   = 1;
        l4.echoShadowsActive      = true;
        configs[4]                = l4;

        // ----- Level 5: Core Defense -----
        LevelConfig l5            = new LevelConfig();
        l5.levelNumber            = 5;
        l5.name                   = "Core Defense";
        l5.objectiveDescription   = "Protect the Arena Core (3 HP) from enemy attacks. Kill all enemies before they destroy it.";
        l5.objectiveType          = LevelConfig.OBJ_PROTECT_CORE;
        l5.targetCount            = 7;
        l5.enemyTypes             = new int[]{LevelConfig.ENEMY_FAST, LevelConfig.ENEMY_HEAVY, LevelConfig.ENEMY_RANGED};
        l5.enemyCounts            = new int[]{3, 2, 2};
        l5.initialFractureCount   = 1;
        l5.echoShadowsActive      = true;
        configs[5]                = l5;

        // ----- Level 6: Seal Sequence -----
        LevelConfig l6            = new LevelConfig();
        l6.levelNumber            = 6;
        l6.name                   = "Seal Sequence";
        l6.objectiveDescription   = "Activate Seals 1, 2, 3, 4 in order. Seals are placed on different arena pieces. Wrong order resets the sequence.";
        l6.objectiveType          = LevelConfig.OBJ_ACTIVATE_SEALS;
        l6.targetCount            = 4;
        l6.enemyTypes             = new int[]{LevelConfig.ENEMY_FAST, LevelConfig.ENEMY_RANGED};
        l6.enemyCounts            = new int[]{3, 3};
        l6.initialFractureCount   = 2;
        l6.echoShadowsActive      = true;
        configs[6]                = l6;

        // ----- Level 7: BOSS — Mirror Beast -----
        LevelConfig l7            = new LevelConfig();
        l7.levelNumber            = 7;
        l7.name                   = "Mirror Beast";
        l7.objectiveDescription   = "Defeat the Mirror Beast. Certain arena zones invert your tilt controls. Navigate the mirror zones to attack safely.";
        l7.objectiveType          = LevelConfig.OBJ_DEFEAT_ALL;
        l7.targetCount            = 1;
        l7.enemyTypes             = new int[]{};
        l7.enemyCounts            = new int[]{};
        l7.initialFractureCount   = 1;
        l7.isBossLevel            = true;
        l7.bossType               = LevelConfig.BOSS_MIRROR_BEAST;
        configs[7]                = l7;

        // ----- Level 8: Echo Storm -----
        LevelConfig l8            = new LevelConfig();
        l8.levelNumber            = 8;
        l8.name                   = "Echo Storm";
        l8.objectiveDescription   = "Destroy 3 Echo Generators before the echo density overwhelms you. Echoes from every previous level are active.";
        l8.objectiveType          = LevelConfig.OBJ_SURVIVE_STORM;
        l8.targetCount            = 3;
        l8.surviveSeconds         = 0f; // no pure time requirement — kill 3 generators
        l8.enemyTypes             = new int[]{LevelConfig.ENEMY_HEAVY};
        l8.enemyCounts            = new int[]{3};
        l8.initialFractureCount   = 2;
        l8.echoShadowsActive      = true;
        l8.hasEchoGenerators      = true;
        configs[8]                = l8;

        // ----- Level 9: BOSS — Echo Titan -----
        LevelConfig l9            = new LevelConfig();
        l9.levelNumber            = 9;
        l9.name                   = "Echo Titan";
        l9.objectiveDescription   = "Defeat the Echo Titan — the final boss. All echo systems are active. The arena is nearly fully fractured.";
        l9.objectiveType          = LevelConfig.OBJ_DEFEAT_ALL;
        l9.targetCount            = 1;
        l9.enemyTypes             = new int[]{};
        l9.enemyCounts            = new int[]{};
        l9.initialFractureCount   = 3;
        l9.echoShadowsActive      = true;
        l9.isBossLevel            = true;
        l9.bossType               = LevelConfig.BOSS_ECHO_TITAN;
        configs[9]                = l9;
    }

    // =========================================================================
    // Progression
    // =========================================================================

    public LevelConfig getConfig(int level) {
        if (level < 1 || level > TOTAL_LEVELS) return configs[TOTAL_LEVELS];
        return configs[level];
    }

    public int getCurrentLevel() { return currentLevel; }

    /**
     * Move to the next level.
     * Returns true if there is a next level, false if the game is complete.
     */
    public boolean advance() {
        if (currentLevel >= TOTAL_LEVELS) return false;
        currentLevel++;
        return true;
    }

    // =========================================================================
    // Enemy spawning
    // =========================================================================

    /**
     * Spawn enemies for this level at staggered horizontal positions.
     * All enemies start above the arena and fall onto the first platform.
     */
    public void spawnEnemies(LevelConfig cfg, List<Enemy> enemies,
                              float screenW, float screenH) {
        float spawnY = screenH * 0.15f; // spawn above arena top
        int totalEnemies = 0;
        for (int count : cfg.enemyCounts) totalEnemies += count;
        if (totalEnemies == 0) return;

        float spacing = screenW / (totalEnemies + 1f);
        int   placed  = 0;

        for (int g = 0; g < cfg.enemyTypes.length; g++) {
            for (int i = 0; i < cfg.enemyCounts[g]; i++) {
                float spawnX = spacing * (placed + 1);
                Enemy e;
                switch (cfg.enemyTypes[g]) {
                    case LevelConfig.ENEMY_HEAVY:
                        e = new HeavyEnemy(spawnX, spawnY, screenW, screenH);
                        break;
                    case LevelConfig.ENEMY_RANGED:
                        e = new RangedEnemy(spawnX, spawnY, screenW, screenH);
                        break;
                    default: // ENEMY_FAST
                        e = new FastEnemy(spawnX, spawnY, screenW, screenH);
                        break;
                }
                enemies.add(e);
                placed++;
            }
        }
    }

    // =========================================================================
    // Boss creation
    // =========================================================================

    public Boss createBoss(int bossType, float screenW, float screenH) {
        float cx = screenW / 2f;
        float spawnY = screenH * 0.15f;
        switch (bossType) {
            case LevelConfig.BOSS_MIRROR_BEAST:
                return new MirrorBeastBoss(cx - 47f, spawnY, screenW, screenH);
            case LevelConfig.BOSS_ECHO_TITAN:
                return new EchoTitanBoss(cx - 57f, spawnY, screenW, screenH);
            default: // BOSS_SHATTER_KING
                return new ShatterKingBoss(cx - 50f, spawnY, screenW, screenH);
        }
    }
}
