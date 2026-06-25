package com.example.project;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Tile - represents one cell of the level grid.
 *
 * The level is a 2D grid of integer tile IDs.  This class holds the integer
 * constants that name each tile type, and provides a static draw() method
 * that knows how to render each type using only Canvas and Paint — no images.
 *
 * Tile types and their meanings:
 *   EMPTY       plain icy floor — player can walk here
 *   WALL        solid ice block — player cannot pass through
 *   SPIKE       static spike — kills player on contact
 *   CRACKING    floor that disappears after the player stands on it too long
 *   WIND_RIGHT  pushes the player rightward continuously
 *   WIND_LEFT   pushes the player leftward
 *   WIND_UP     pushes the player upward
 *   WIND_DOWN   pushes the player downward
 *   GOAL        the exit tile — reaching it completes the level
 *   START       marks where the player spawns (treated as EMPTY after placement)
 */
public class Tile {

    // ---- tile type constants (used in Level's int[][] map) ----
    public static final int EMPTY       = 0;
    public static final int WALL        = 1;
    public static final int SPIKE       = 2;
    public static final int CRACKING    = 3;
    public static final int WIND_RIGHT  = 4;
    public static final int WIND_LEFT   = 5;
    public static final int WIND_UP     = 6;
    public static final int WIND_DOWN   = 7;
    public static final int GOAL        = 8;
    public static final int START       = 9;

    // ---- colours used for each tile type (frozen theme) ----
    // Wall: layered ice-blue shades
    static final int COL_WALL_MAIN  = Color.rgb(55, 130, 195);
    static final int COL_WALL_LIGHT = Color.rgb(110, 180, 230);
    static final int COL_WALL_DARK  = Color.rgb(30,  80,  140);
    // Floor: very dark blue — icy ground
    static final int COL_FLOOR      = Color.rgb(18, 42, 90);
    static final int COL_FLOOR_LINE = Color.rgb(30, 60, 110);
    // Spike: pale icy white with bluish shadows
    static final int COL_SPIKE      = Color.rgb(210, 235, 255);
    static final int COL_SPIKE_SHD  = Color.rgb(80, 140, 200);
    // Cracking ice: warm orange-red tones — visual contrast against cold palette
    static final int COL_CRACK_OK   = Color.rgb(200, 120, 40);
    static final int COL_CRACK_WARN = Color.rgb(220, 60, 20);
    // Wind zones: light cyan
    static final int COL_WIND       = Color.rgb(100, 200, 235);
    static final int COL_WIND_ARROW = Color.rgb(180, 230, 255);
    // Goal: glowing teal
    static final int COL_GOAL_INNER = Color.rgb(0, 230, 210);
    static final int COL_GOAL_OUTER = Color.rgb(0, 150, 140);

    // Reusable Paint objects (static so they are shared across all draw calls)
    private static final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Draw one tile at pixel position (x, y) with the given width and height.
     *
     * @param canvas    the Canvas to draw onto
     * @param tileType  one of the integer constants above
     * @param x         left edge of the tile in pixels
     * @param y         top  edge of the tile in pixels
     * @param w         tile width  in pixels
     * @param h         tile height in pixels
     * @param crackProgress  [0-1] how cracked this tile is (only used for CRACKING type)
     */
    public static void draw(Canvas canvas, int tileType,
                            float x, float y, float w, float h,
                            float crackProgress) {
        switch (tileType) {
            case WALL:
                drawWall(canvas, x, y, w, h);
                break;
            case SPIKE:
                drawFloor(canvas, x, y, w, h);   // spike sits on floor
                drawSpikes(canvas, x, y, w, h);
                break;
            case CRACKING:
                drawCrackingIce(canvas, x, y, w, h, crackProgress);
                break;
            case WIND_RIGHT:
                drawFloor(canvas, x, y, w, h);
                drawWindArrow(canvas, x, y, w, h, 0);   // 0° = right
                break;
            case WIND_LEFT:
                drawFloor(canvas, x, y, w, h);
                drawWindArrow(canvas, x, y, w, h, 180); // 180° = left
                break;
            case WIND_UP:
                drawFloor(canvas, x, y, w, h);
                drawWindArrow(canvas, x, y, w, h, 270); // 270° = up
                break;
            case WIND_DOWN:
                drawFloor(canvas, x, y, w, h);
                drawWindArrow(canvas, x, y, w, h, 90);  // 90° = down
                break;
            case GOAL:
                drawGoalTile(canvas, x, y, w, h);
                break;
            case EMPTY:
            case START:
            default:
                drawFloor(canvas, x, y, w, h);
                break;
        }
    }

    // ---- private draw helpers ----

    private static void drawFloor(Canvas canvas, float x, float y, float w, float h) {
        // Solid dark-blue base
        fillPaint.setColor(COL_FLOOR);
        canvas.drawRect(x, y, x + w, y + h, fillPaint);
        // Subtle grid line on top/left edges to suggest ice panels
        edgePaint.setColor(COL_FLOOR_LINE);
        edgePaint.setStrokeWidth(1f);
        edgePaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(x, y, x + w, y + h, edgePaint);
    }

    private static void drawWall(Canvas canvas, float x, float y, float w, float h) {
        // Main ice block fill
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(COL_WALL_MAIN);
        canvas.drawRect(x, y, x + w, y + h, fillPaint);

        // Light highlight on top and left (simulates light from upper-left)
        edgePaint.setStyle(Paint.Style.FILL);
        edgePaint.setColor(COL_WALL_LIGHT);
        float bevel = w * 0.08f;
        // top highlight strip
        canvas.drawRect(x, y, x + w, y + bevel, edgePaint);
        // left highlight strip
        canvas.drawRect(x, y, x + bevel, y + h, edgePaint);

        // Dark shadow on bottom and right
        edgePaint.setColor(COL_WALL_DARK);
        // bottom shadow strip
        canvas.drawRect(x, y + h - bevel, x + w, y + h, edgePaint);
        // right shadow strip
        canvas.drawRect(x + w - bevel, y, x + w, y + h, edgePaint);

        // Inner shine — small diagonal glint near top-left
        fillPaint.setColor(Color.argb(80, 200, 230, 255));
        float shine = w * 0.15f;
        canvas.drawRect(x + bevel, y + bevel, x + bevel + shine, y + bevel + shine, fillPaint);
    }

    private static void drawSpikes(Canvas canvas, float x, float y, float w, float h) {
        // Draw 3 triangular ice spikes pointing upward in the tile
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(COL_SPIKE);

        int numSpikes = 3;
        float spikeW = w / numSpikes;
        Path path = new Path();

        for (int i = 0; i < numSpikes; i++) {
            float left  = x + i * spikeW;
            float right = left + spikeW;
            float base  = y + h;
            float tip   = y + h * 0.15f; // tip near top

            path.reset();
            path.moveTo(left + spikeW * 0.1f, base);   // bottom-left
            path.lineTo(right - spikeW * 0.1f, base);  // bottom-right
            path.lineTo(left + spikeW * 0.5f, tip);    // tip
            path.close();
            canvas.drawPath(path, fillPaint);

            // Shadow side of spike
            fillPaint.setColor(COL_SPIKE_SHD);
            path.reset();
            path.moveTo(left + spikeW * 0.5f, tip);
            path.lineTo(right - spikeW * 0.1f, base);
            path.lineTo(left + spikeW * 0.5f + spikeW * 0.1f, base);
            path.close();
            canvas.drawPath(path, fillPaint);
            fillPaint.setColor(COL_SPIKE);
        }
    }

    private static void drawCrackingIce(Canvas canvas, float x, float y,
                                         float w, float h, float crackProgress) {
        // Interpolate colour from orange to red as crack progresses
        int r = (int)(200 + crackProgress * 20);
        int g = (int)(120 - crackProgress * 90);
        int b = (int)(40  - crackProgress * 30);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.rgb(r, g, b));
        canvas.drawRect(x, y, x + w, y + h, fillPaint);

        // Draw crack lines that grow with crackProgress
        if (crackProgress > 0.1f) {
            edgePaint.setStyle(Paint.Style.STROKE);
            edgePaint.setColor(Color.rgb(255, 220, 180));
            edgePaint.setStrokeWidth(2f * crackProgress);
            float cx = x + w / 2f;
            float cy = y + h / 2f;
            // Main cross crack
            canvas.drawLine(cx, cy, x + w * 0.2f, y + h * 0.1f, edgePaint);
            canvas.drawLine(cx, cy, x + w * 0.9f, y + h * 0.3f, edgePaint);
            canvas.drawLine(cx, cy, x + w * 0.1f, y + h * 0.8f, edgePaint);
            canvas.drawLine(cx, cy, x + w * 0.8f, y + h * 0.9f, edgePaint);
        }
    }

    private static void drawWindArrow(Canvas canvas, float x, float y,
                                       float w, float h, float angleDegrees) {
        // Draw a simple arrow indicating wind direction
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(COL_WIND_ARROW);

        canvas.save();
        canvas.rotate(angleDegrees, x + w / 2f, y + h / 2f);

        float cx = x + w / 2f;
        float cy = y + h / 2f;
        float arrowLen  = w * 0.45f;
        float arrowHead = w * 0.2f;

        // Shaft
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(w * 0.08f);
        edgePaint.setColor(COL_WIND_ARROW);
        canvas.drawLine(cx - arrowLen, cy, cx + arrowLen * 0.4f, cy, edgePaint);

        // Arrowhead triangle
        Path arrowPath = new Path();
        arrowPath.moveTo(cx + arrowLen, cy);
        arrowPath.lineTo(cx + arrowLen - arrowHead, cy - arrowHead * 0.6f);
        arrowPath.lineTo(cx + arrowLen - arrowHead, cy + arrowHead * 0.6f);
        arrowPath.close();
        canvas.drawPath(arrowPath, fillPaint);

        canvas.restore();
    }

    private static void drawGoalTile(Canvas canvas, float x, float y, float w, float h) {
        // Outer glow ring
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(COL_GOAL_OUTER);
        canvas.drawRect(x, y, x + w, y + h, fillPaint);
        // Inner glow
        fillPaint.setColor(COL_GOAL_INNER);
        float margin = w * 0.15f;
        canvas.drawRect(x + margin, y + margin, x + w - margin, y + h - margin, fillPaint);
    }

    /** Returns true if the tile type is solid (blocks the player). */
    public static boolean isSolid(int tileType) {
        return tileType == WALL;
    }

    /** Returns true if stepping on this tile kills the player. */
    public static boolean isDeadly(int tileType) {
        return tileType == SPIKE;
    }

    /** Returns true if this tile pushes the player in a direction. */
    public static boolean isWindZone(int tileType) {
        return tileType == WIND_RIGHT || tileType == WIND_LEFT
            || tileType == WIND_UP   || tileType == WIND_DOWN;
    }
}
