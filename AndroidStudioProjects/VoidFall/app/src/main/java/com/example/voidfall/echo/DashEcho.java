package com.example.voidfall.echo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.example.voidfall.entity.Player;

/**
 * DashEcho — spawned when a FastEnemy is defeated.
 *
 * BEHAVIOUR:
 *   Repeatedly dashes horizontally in the direction the FastEnemy was moving
 *   when it died. When it reaches the screen edge, it "teleports" back to
 *   its origin and dashes again — a looping patrol of danger.
 *
 *   The dash speed matches the original FastEnemy's DASH_SPEED so the
 *   player already knows how fast it moves from fighting it.
 *
 * VISUAL:
 *   Slim semi-transparent cyan triangle (same shape as FastEnemy).
 *   Leaves a fading streak trail behind it.
 *   Overall alpha tied to echo lifespan fade.
 */
public class DashEcho extends EchoShadow {

    private static final float DASH_SPEED = 300f;
    private static final float W = 38f;
    private static final float H = 48f;
    private static final float SCREEN_W = 1080f; // will be overridden conceptually

    private final float originX;
    private final float dashDir;    // +1 = right, -1 = left
    private float cx, cy;           // current center position

    private final Paint bodyPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  bodyPath   = new Path();

    // Trail positions
    private static final int TRAIL = 8;
    private final float[] trailX = new float[TRAIL];
    private int trailIdx = 0;

    public DashEcho(float spawnX, float spawnY, float dashDir) {
        super(spawnX, spawnY);
        this.originX = spawnX;
        this.dashDir = dashDir > 0 ? 1f : -1f;
        this.cx      = spawnX;
        this.cy      = spawnY;
        for (int i = 0; i < TRAIL; i++) trailX[i] = spawnX;

        bodyPaint.setColor(Color.rgb(0, 229, 255));
        trailPaint.setColor(Color.rgb(0, 229, 255));
        trailPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void updateBehavior(float dt, Player player) {
        // Record trail
        trailX[trailIdx % TRAIL] = cx;
        trailIdx++;

        // Move
        cx += dashDir * DASH_SPEED * dt;

        // Wrap around when off screen bounds (use a wide screen estimate)
        if (cx > 1200f || cx < -100f) {
            cx = originX; // reset to origin
        }

        // Update base x/y for collision
        x = cx - W / 2f;
        y = cy - H / 2f;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!active) return;

        // Draw trail
        for (int i = 0; i < TRAIL; i++) {
            float t    = (float)i / TRAIL;
            int   alpha = (int)(drawAlpha * t * 0.4f);
            trailPaint.setAlpha(alpha);
            float tx = trailX[(trailIdx - i - 1 + TRAIL) % TRAIL];
            canvas.drawCircle(tx, cy, W * 0.3f * t, trailPaint);
        }

        // Draw ghost triangle
        float tipX  = (dashDir > 0) ? cx + W / 2f : cx - W / 2f;
        float baseX = (dashDir > 0) ? cx - W / 2f : cx + W / 2f;

        bodyPath.reset();
        bodyPath.moveTo(tipX,  cy);
        bodyPath.lineTo(baseX, cy - H / 2f);
        bodyPath.lineTo(baseX, cy + H / 2f);
        bodyPath.close();

        bodyPaint.setAlpha(drawAlpha);
        canvas.drawPath(bodyPath, bodyPaint);
    }

    @Override
    public RectF getBounds() {
        return new RectF(x, y, x + W, y + H);
    }
}
