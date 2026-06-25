package com.example.project;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Level - holds the tile grid for one game level and handles drawing + collision.
 *
 * The level is stored as a 2D int array (int[][] map).
 * Each int is a Tile type constant (Tile.WALL, Tile.SPIKE, etc.).
 *
 * COORDINATE SYSTEM:
 *   The grid is rows × cols.
 *   Row 0 is at the TOP of the screen; row (rows-1) is at the BOTTOM.
 *   Column 0 is at the LEFT; column (cols-1) is at the RIGHT.
 *   Pixel position of tile (row r, col c):
 *       x = c * tileW + offsetX
 *       y = r * tileH + offsetY
 *
 * TILE SIZE:
 *   Calculated in fitToScreen() to fill the screen while maintaining a constant
 *   grid size. The level is centred on the screen using offsetX and offsetY.
 *
 * CRACKING ICE:
 *   Each tile has a separate crackProgress float (0 = intact, 1 = fully cracked/dead).
 *   When the player stands on a CRACKING tile, its progress increases.
 *   At 1.0 the tile becomes a "hole" (kills the player).
 */
public class Level {

    // The tile grid — set by LevelManager
    public final int[][] map;
    public final int rows;
    public final int cols;

    // Tile dimensions in pixels (computed to fill the screen)
    public float tileW, tileH;

    // Pixel offset so the grid is centred on screen
    public float offsetX, offsetY;

    // Where the player starts (grid coordinates)
    public final int startRow, startCol;

    // Where the goal is (grid coordinates)
    public final int goalRow, goalCol;

    // Per-tile crack progress for CRACKING tiles [0.0, 1.0]
    private final float[][] crackProgress;

    // How fast a cracking tile progresses per second
    private static final float CRACK_SPEED = 0.5f; // fully cracked in 2 seconds

    // Paint for background
    private static final Paint bgPaint = new Paint();

    /**
     * Create a Level from a 2D tile map.
     *
     * @param map       the 2D int array of tile IDs
     * @param startRow  row index of the player start tile (Tile.START)
     * @param startCol  column index of the start tile
     * @param goalRow   row index of the goal tile
     * @param goalCol   column index of the goal tile
     */
    public Level(int[][] map, int startRow, int startCol, int goalRow, int goalCol) {
        this.map      = map;
        this.rows     = map.length;
        this.cols     = map[0].length;
        this.startRow = startRow;
        this.startCol = startCol;
        this.goalRow  = goalRow;
        this.goalCol  = goalCol;
        this.crackProgress = new float[rows][cols];
    }

    /**
     * Calculate tileW, tileH, offsetX, offsetY so the level fills the screen.
     * Call this once after the SurfaceView is ready and its dimensions are known.
     *
     * @param screenW  screen width in pixels
     * @param screenH  screen height in pixels
     */
    public void fitToScreen(int screenW, int screenH) {
        // Choose the smaller scale so the whole grid fits within the screen
        float scaleX = (float) screenW / cols;
        float scaleY = (float) screenH / rows;
        float scale  = Math.min(scaleX, scaleY);

        tileW = scale;
        tileH = scale;

        // Centre the grid if it doesn't fill the whole screen
        offsetX = (screenW - cols * tileW) / 2f;
        offsetY = (screenH - rows * tileH) / 2f;
    }

    // ---- pixel helpers ----

    /** Returns the pixel X of the left edge of the given column. */
    public float tilePixelX(int col) { return offsetX + col * tileW; }

    /** Returns the pixel Y of the top edge of the given row. */
    public float tilePixelY(int row) { return offsetY + row * tileH; }

    /** Returns the pixel X of the centre of the given column. */
    public float tileCentreX(int col) { return tilePixelX(col) + tileW / 2f; }

    /** Returns the pixel Y of the centre of the given row. */
    public float tileCentreY(int row) { return tilePixelY(row) + tileH / 2f; }

    /** Returns the player's starting pixel X (centre of start tile). */
    public float startPixelX() { return tileCentreX(startCol); }

    /** Returns the player's starting pixel Y (centre of start tile). */
    public float startPixelY() { return tileCentreY(startRow); }

    // ---- tile queries ----

    /** Returns the tile type at (row, col), or WALL if out of bounds. */
    public int getTile(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return Tile.WALL;
        return map[row][col];
    }

    /** Returns the tile type at a pixel position. */
    public int getTileAt(float px, float py) {
        int col = (int)((px - offsetX) / tileW);
        int row = (int)((py - offsetY) / tileH);
        return getTile(row, col);
    }

    // ---- collision detection ----

    /**
     * Returns true if a circle at (px, py) with the given radius overlaps any WALL tile.
     *
     * We test four "corner points" of the circle's bounding box.
     * This is simple and fast, though slightly conservative.
     *
     * @param px     circle centre X in pixels
     * @param py     circle centre Y in pixels
     * @param radius circle radius in pixels
     */
    public boolean collidesWithWalls(float px, float py, float radius) {
        // Shrink radius slightly for a forgiving hitbox
        float r = radius * 0.85f;

        // Check 4 corners of the bounding box
        return isSolidAt(px - r, py - r)
            || isSolidAt(px + r, py - r)
            || isSolidAt(px - r, py + r)
            || isSolidAt(px + r, py + r);
    }

    private boolean isSolidAt(float px, float py) {
        return Tile.isSolid(getTileAt(px, py));
    }

    /**
     * Returns true if the tile directly under the player centre is deadly (spike or hole).
     * Also returns true if a cracking tile has progress = 1.0 (fully cracked = hole).
     */
    public boolean isDeadlyAt(float px, float py) {
        int col = (int)((px - offsetX) / tileW);
        int row = (int)((py - offsetY) / tileH);
        int tile = getTile(row, col);
        if (Tile.isDeadly(tile)) return true;
        // Cracking tile fully cracked = instant death (fell through)
        if (tile == Tile.CRACKING && getCrackProgress(row, col) >= 1f) return true;
        return false;
    }

    /**
     * Returns the wind force vector for the tile under the player.
     * Returned as a float[2]: {forceX, forceY} in pixels/s² units.
     * Returns {0,0} if not a wind tile.
     *
     * @param windStrength  how strongly wind pushes (pixels/s²)
     */
    public float[] getWindForceAt(float px, float py, float windStrength) {
        int tile = getTileAt(px, py);
        switch (tile) {
            case Tile.WIND_RIGHT: return new float[]{  windStrength, 0f };
            case Tile.WIND_LEFT:  return new float[]{ -windStrength, 0f };
            case Tile.WIND_UP:    return new float[]{ 0f, -windStrength };
            case Tile.WIND_DOWN:  return new float[]{ 0f,  windStrength };
            default:              return new float[]{ 0f, 0f };
        }
    }

    // ---- cracking ice management ----

    /** Returns the crack progress [0.0 - 1.0] for the tile at (row, col). */
    public float getCrackProgress(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return 0f;
        return crackProgress[row][col];
    }

    /**
     * Update cracking ice tiles. If the player is standing on a CRACKING tile,
     * advance its crack progress.
     *
     * @param playerX  player X in pixels
     * @param playerY  player Y in pixels
     * @param dt       delta time in seconds
     */
    public void updateCrackingTiles(float playerX, float playerY, float dt) {
        int col = (int)((playerX - offsetX) / tileW);
        int row = (int)((playerY - offsetY) / tileH);
        if (row < 0 || row >= rows || col < 0 || col >= cols) return;

        if (map[row][col] == Tile.CRACKING) {
            crackProgress[row][col] = Math.min(1f, crackProgress[row][col] + CRACK_SPEED * dt);
        }
    }

    /** Reset all crack progress (called on level restart). */
    public void resetCracks() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                crackProgress[r][c] = 0f;
            }
        }
    }

    // ---- drawing ----

    /**
     * Draw the entire level grid.
     * Called each frame from GameView.draw().
     */
    public void draw(Canvas canvas) {
        // Dark navy background fill
        bgPaint.setColor(Color.rgb(5, 14, 40));
        canvas.drawColor(Color.rgb(5, 14, 40));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int tileType = map[r][c];
                float px = tilePixelX(c);
                float py = tilePixelY(r);
                float prog = (tileType == Tile.CRACKING) ? crackProgress[r][c] : 0f;

                // Skip drawing GOAL tile here — Goal object draws itself
                // Skip START tile — draw as EMPTY
                int drawType = (tileType == Tile.START) ? Tile.EMPTY : tileType;
                if (drawType != Tile.GOAL) {
                    Tile.draw(canvas, drawType, px, py, tileW, tileH, prog);
                }
            }
        }
    }
}
