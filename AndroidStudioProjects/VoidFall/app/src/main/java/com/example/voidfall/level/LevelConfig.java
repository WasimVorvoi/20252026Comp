package com.example.voidfall.level;

/**
 * LevelConfig — pure data container describing one level's settings.
 *
 * WHY A CONFIG CLASS:
 *   Separating data from logic makes it easy to add new levels:
 *   just add a new LevelConfig in LevelManager.buildConfigs() —
 *   no changes needed to GameView, ArenaManager, or ObjectiveManager.
 *
 * OBJECTIVE TYPES:
 *   DEFEAT_ALL     — kill every spawned enemy
 *   COLLECT_SHARDS — pick up N shards scattered on fracture pieces
 *   PROTECT_CORE   — keep the arena core's HP above 0 while killing enemies
 *   ACTIVATE_SEALS — step on seals in numeric order (1→2→3→4) before time runs out
 *   SURVIVE_STORM  — stay alive for surviveSeconds while destroying echo generators
 *
 * BOSS TYPES:
 *   BOSS_SHATTER_KING (3) — ShatterKingBoss
 *   BOSS_MIRROR_BEAST (7) — MirrorBeastBoss
 *   BOSS_ECHO_TITAN   (9) — EchoTitanBoss
 */
public class LevelConfig {

    // Objective type constants
    public static final int OBJ_DEFEAT_ALL     = 0;
    public static final int OBJ_COLLECT_SHARDS = 1;
    public static final int OBJ_PROTECT_CORE   = 2;
    public static final int OBJ_ACTIVATE_SEALS = 3;
    public static final int OBJ_SURVIVE_STORM  = 4;

    // Boss type constants
    public static final int BOSS_SHATTER_KING  = 0;
    public static final int BOSS_MIRROR_BEAST  = 1;
    public static final int BOSS_ECHO_TITAN    = 2;

    // Enemy type constants
    public static final int ENEMY_FAST   = 0;
    public static final int ENEMY_HEAVY  = 1;
    public static final int ENEMY_RANGED = 2;

    // Level identity
    public int    levelNumber;
    public String name;
    public String objectiveDescription;

    // Objective
    public int   objectiveType;
    public int   targetCount;        // enemies / shards / seals to complete objective
    public float surviveSeconds;     // used by OBJ_SURVIVE_STORM

    // Enemy composition
    public int[] enemyTypes;         // parallel arrays: type of each enemy group
    public int[] enemyCounts;        // how many of each type to spawn

    // Arena settings
    public int     initialFractureCount; // how many fractures at level start
    public boolean arenaStartsFractured; // if true, run setFractureLevel on load

    // Special mechanics flags
    public boolean echoShadowsActive;  // do echoes from previous level persist?
    public boolean hasEchoGenerators;  // level 8: generator enemies spawn echoes

    // Boss settings
    public boolean isBossLevel;
    public int     bossType;
}
