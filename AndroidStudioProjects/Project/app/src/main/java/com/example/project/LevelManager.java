package com.example.project;

import java.util.ArrayList;
import java.util.List;

/**
 * LevelManager - creates Level and Hazard objects for each game level.
 *
 * HOW LEVELS ARE DEFINED:
 *   Each level is a 2D int array using the tile constants from the Tile class.
 *   The grid is 13 columns × 19 rows.
 *   Row 0 and row 18 are all WALL (top/bottom borders).
 *   Column 0 and column 12 are all WALL (left/right borders).
 *
 * TILE LEGEND:
 *   0 = EMPTY (icy floor)
 *   1 = WALL  (solid ice block)
 *   2 = SPIKE (instant death)
 *   3 = CRACKING (floor that disappears — Level 4)
 *   4 = WIND_RIGHT  (Level 4)
 *   5 = WIND_LEFT   (Level 4)
 *   6 = WIND_UP     (Level 4)
 *   7 = WIND_DOWN   (Level 4)
 *   8 = GOAL
 *   9 = START (player spawn, treated as EMPTY)
 *
 * LEVEL PROGRESSION:
 *   Level 1 — Snake maze, no hazards. Challenge = slippery physics.
 *   Level 2 — Same maze + static spike tiles. Challenge = route planning around spikes.
 *   Level 3 — Tighter maze + moving hazards (icicles + sliders). Challenge = timing.
 *   Level 4 — Open arena + cracking ice + wind zones. Challenge = path planning + env.
 */
public class LevelManager {

    public static final int TOTAL_LEVELS = 4;

    // -----------------------------------------------------------------
    //  LEVEL 1: Basic snake maze, no hazards.
    //  The snake winds left-right across 19 rows.
    //  Challenge: master slippery physics through tight turns.
    // -----------------------------------------------------------------
    private static final int[][] LEVEL_1_MAP = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1},   // row  0 — top border
        {1,9,0,0,0,0,0,0,0,0,0,0,1},   // row  1 — start at (1,1)
        {1,1,1,1,1,1,1,1,1,1,1,0,1},   // row  2 — wall, gap at col 11
        {1,0,0,0,0,0,0,0,0,0,0,0,1},   // row  3
        {1,0,1,1,1,1,1,1,1,1,1,1,1},   // row  4 — gap at col 1 (left)
        {1,0,0,0,0,0,0,0,0,0,0,0,1},   // row  5
        {1,1,1,1,1,1,1,1,1,1,1,0,1},   // row  6 — gap at col 11 (right)
        {1,0,0,0,0,0,0,0,0,0,0,0,1},   // row  7
        {1,0,1,1,1,1,1,1,1,1,1,1,1},   // row  8 — gap at col 1
        {1,0,0,0,0,0,0,0,0,0,0,0,1},   // row  9
        {1,1,1,1,1,1,1,1,1,1,1,0,1},   // row 10 — gap at col 11
        {1,0,0,0,0,0,0,0,0,0,0,0,1},   // row 11
        {1,0,1,1,1,1,1,1,1,1,1,1,1},   // row 12 — gap at col 1
        {1,0,0,0,0,0,0,0,0,0,0,0,1},   // row 13
        {1,1,1,1,1,1,1,1,1,1,1,0,1},   // row 14 — gap at col 11
        {1,0,0,0,0,0,0,0,0,0,0,0,1},   // row 15
        {1,0,1,1,1,1,1,1,1,1,1,1,1},   // row 16 — gap at col 1
        {1,0,0,0,0,0,0,0,0,0,0,8,1},   // row 17 — goal at (17,11)
        {1,1,1,1,1,1,1,1,1,1,1,1,1},   // row 18 — bottom border
    };

    // -----------------------------------------------------------------
    //  LEVEL 2: Same snake maze + static ice spikes.
    //  Spikes are placed mid-corridor — player must navigate around them.
    //  Challenge: slippery physics + spike avoidance.
    // -----------------------------------------------------------------
    private static final int[][] LEVEL_2_MAP = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,9,0,0,0,2,0,0,0,0,0,0,1},   // spike at (1,5)
        {1,1,1,1,1,1,1,1,1,1,1,0,1},
        {1,0,0,0,2,0,0,0,0,2,0,0,1},   // spikes at (3,4) and (3,9)
        {1,0,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,2,0,0,0,0,1},   // spike at (5,7)
        {1,1,1,1,1,1,1,1,1,1,1,0,1},
        {1,0,0,2,0,0,0,0,0,0,0,0,1},   // spike at (7,3)
        {1,0,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,2,0,0,1},   // spike at (9,9)
        {1,1,1,1,1,1,1,1,1,1,1,0,1},
        {1,0,0,2,0,0,0,2,0,0,0,0,1},   // spikes at (11,3) and (11,7)
        {1,0,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,2,0,0,0,0,0,0,1},   // spike at (13,5)
        {1,1,1,1,1,1,1,1,1,1,1,0,1},
        {1,0,2,0,0,0,0,0,0,0,2,0,1},   // spikes at (15,2) and (15,10)
        {1,0,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,2,0,0,0,0,0,0,8,1},   // spike at (17,4), goal at (17,11)
        {1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    // -----------------------------------------------------------------
    //  LEVEL 3: Snake maze + MOVING hazards (icicles + sliders).
    //  The maze is slightly wider corridors to allow dodging.
    //  Hazards are defined separately in createHazards().
    //  Challenge: timing movement around moving obstacles.
    // -----------------------------------------------------------------
    private static final int[][] LEVEL_3_MAP = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,9,0,0,0,0,0,0,0,0,0,0,1},   // start
        {1,1,1,1,1,1,0,1,1,1,1,0,1},   // two gaps: col 5 and col 11
        {1,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,1,1,1,1,1,1,0,1,1},   // gaps at col 1 and col 10
        {1,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,1,1,0,1,1,1,1,1,1,1,0,1},   // gaps at col 3 and col 11
        {1,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,1,1,0,1,1,1,1,1,1},   // gaps at col 1 and col 6
        {1,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,0,1,1,1,1,1,0,1},   // gaps at col 5 and col 11
        {1,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,1,1,1,1,0,1,1,1,1},   // gaps at col 1 and col 8
        {1,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,1,0,1,1,1,1,1,1,1,1,0,1},   // gaps at col 2 and col 11
        {1,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,0,1,1,1,1,0,1,1,1},   // multiple gaps
        {1,0,0,0,0,0,0,0,0,0,0,8,1},   // goal at (17,11)
        {1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    // -----------------------------------------------------------------
    //  LEVEL 4: Open-arena design with cracking ice + wind zones.
    //  Pillars divide the space; cracking tiles litter the main paths;
    //  wind zones push the player off-course on certain rows.
    //  Challenge: path planning, cracking ice, and wind correction.
    // -----------------------------------------------------------------
    //  Tile key for this level:  3=CRACKING, 4=WIND_RIGHT, 5=WIND_LEFT
    private static final int[][] LEVEL_4_MAP = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,9,0,0,1,0,0,0,1,0,0,0,1},   // start; pillars at col 4 and col 8
        {1,0,0,0,1,0,1,0,1,0,3,0,1},   // cracking at (2,10)
        {1,3,3,0,0,0,1,0,0,0,3,0,1},   // cracking tiles on left and right
        {1,1,1,0,1,0,1,0,1,0,1,0,1},   // pillar row
        {1,4,4,0,1,0,0,0,1,0,4,4,1},   // WIND_RIGHT on cols 1,2 and 10,11
        {1,0,0,0,0,0,1,0,0,0,0,0,1},   // open row
        {1,0,3,3,0,0,1,0,0,3,3,0,1},   // cracking sections
        {1,0,0,0,0,0,0,0,0,0,0,0,1},   // fully open — can cross the centre
        {1,1,1,0,1,0,1,0,1,0,1,1,1},   // pillar row
        {1,5,5,0,1,0,0,0,1,0,5,5,1},   // WIND_LEFT on cols 1,2 and 10,11
        {1,0,0,0,1,0,1,0,1,0,0,0,1},
        {1,0,3,0,0,0,1,0,0,0,3,0,1},   // cracking
        {1,0,3,0,1,0,1,0,1,0,3,0,1},   // cracking + pillars
        {1,0,0,0,1,0,0,0,1,0,0,0,1},
        {1,1,1,0,1,0,1,0,1,0,1,1,1},   // pillar row
        {1,0,0,0,0,0,1,0,0,0,0,0,1},   // open row before goal
        {1,0,0,0,0,0,0,0,0,0,0,8,1},   // goal at (17,11)
        {1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    // ----------------------------------------------------------------- public API

    /**
     * Build and return a Level object for the given level number (1-based).
     * The level is fitted to the screen in GameView after the surface is created.
     *
     * @param levelNum  1-based level index (1 through TOTAL_LEVELS)
     */
    public static Level createLevel(int levelNum) {
        int[][] map;
        switch (levelNum) {
            case 2:  map = deepCopy(LEVEL_2_MAP); break;
            case 3:  map = deepCopy(LEVEL_3_MAP); break;
            case 4:  map = deepCopy(LEVEL_4_MAP); break;
            default: map = deepCopy(LEVEL_1_MAP); break;
        }

        // Scan map to find start and goal positions
        int startR = 1, startC = 1, goalR = 17, goalC = 11;
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length; c++) {
                if (map[r][c] == Tile.START) { startR = r; startC = c; }
                if (map[r][c] == Tile.GOAL)  { goalR  = r; goalC  = c; }
            }
        }

        return new Level(map, startR, startC, goalR, goalC);
    }

    /**
     * Build the list of Hazard objects for a given level.
     * Hazard pixel positions depend on the Level's tile size, so call
     * level.fitToScreen() BEFORE calling this method.
     *
     * @param levelNum  1-based level index
     * @param level     the Level returned by createLevel (already fitted to screen)
     */
    public static List<Hazard> createHazards(int levelNum, Level level) {
        List<Hazard> hazards = new ArrayList<>();
        float tW = level.tileW;
        float tH = level.tileH;
        float levelPixelH = level.rows * tH + level.offsetY;

        if (levelNum == 3) {
            // ---- FALLING ICICLES ----
            // Icicle 1: falls through the centre column (col 6) from top to bottom
            hazards.add(Hazard.createIcicle(
                level.tileCentreX(6),           // X: centre of col 6
                level.tilePixelY(0) - tH * 2,  // start above the screen
                tW * 0.6f,                       // size
                tH * 5f,                         // fall speed (pixels/sec)
                levelPixelH
            ));

            // Icicle 2: falls through col 3, offset timing (starts lower)
            hazards.add(Hazard.createIcicle(
                level.tileCentreX(3),
                level.tilePixelY(4),            // starts mid-level for staggered timing
                tW * 0.6f,
                tH * 4f,
                levelPixelH
            ));

            // Icicle 3: falls through col 9
            hazards.add(Hazard.createIcicle(
                level.tileCentreX(9),
                level.tilePixelY(8),
                tW * 0.6f,
                tH * 6f,
                levelPixelH
            ));

            // ---- SLIDING BLOCKERS ----
            // Slider 1: moves left-right along row 5 between col 1 and col 10
            hazards.add(Hazard.createSlider(
                level.tilePixelX(2),             // start X
                level.tilePixelY(5) + tH * 0.15f,// Y (slightly inset vertically)
                tW * 1.5f,                        // width
                tH * 0.7f,                        // height
                tW * 4f,                          // speed (pixels/sec)
                level.tilePixelX(1),              // min X boundary
                level.tilePixelX(11)              // max X boundary
            ));

            // Slider 2: moves left-right along row 11, faster, going the other way
            hazards.add(Hazard.createSlider(
                level.tilePixelX(8),
                level.tilePixelY(11) + tH * 0.15f,
                tW * 1.5f,
                tH * 0.7f,
                -tW * 5f,                         // negative = starts moving left
                level.tilePixelX(1),
                level.tilePixelX(11)
            ));

            // Slider 3: faster slider on row 15
            hazards.add(Hazard.createSlider(
                level.tilePixelX(5),
                level.tilePixelY(15) + tH * 0.15f,
                tW * 1.5f,
                tH * 0.7f,
                tW * 6f,
                level.tilePixelX(2),
                level.tilePixelX(11)
            ));
        }

        // Level 4 has no Hazard objects — its challenges come from CRACKING tiles
        // and WIND_ZONE tiles built into the map.

        return hazards;
    }

    /** Returns a name description for the level (used in HUD). */
    public static String getLevelName(int levelNum) {
        switch (levelNum) {
            case 1: return "Ice Cave";
            case 2: return "Spike Fields";
            case 3: return "Falling Ice";
            case 4: return "Frozen Abyss";
            default: return "Level " + levelNum;
        }
    }

    /** Returns a short description of the new challenge in each level. */
    public static String getLevelHint(int levelNum) {
        switch (levelNum) {
            case 1: return "Mind the slippery ice!";
            case 2: return "Watch out for spikes!";
            case 3: return "Dodge the moving ice!";
            case 4: return "Cracking ice & wind — plan carefully!";
            default: return "";
        }
    }

    /** Creates a deep copy of a 2D int array so each level gets its own data. */
    private static int[][] deepCopy(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
    }
}
