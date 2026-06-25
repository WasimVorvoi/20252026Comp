package com.example.voidfall.echo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.voidfall.entity.Player;

/**
 * BlockEcho — spawned when a HeavyEnemy is defeated.
 *
 * BEHAVIOUR:
 *   Completely stationary. Acts as an impassable rectangular zone.
 *   The player and other entities are blocked by its bounds.
 *   Slowly rotates to look ominous, but CollisionManager uses the
 *   UNROTATED bounding box — so the collision is simpler/fairer.
 *
 *   Strategically, block echoes accumulate and shrink the passable arena.
 *   Killing many HeavyEnemies near corridors can make the arena very tight.
 *
 * VISUAL:
 *   Large semi-transparent orange-red rectangle.
 *   Inner border lines suggest the armoured shell of the original enemy.
 *   Slowly rotates on screen for visual feedback that it's active.
 */
public class BlockEcho extends EchoShadow {

    private float width, height;
    private float rotation     = 0f;
    private float rotationVel  = 8f; // degrees/second

    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BlockEcho(float spawnX, float spawnY, float width, float height) {
        super(spawnX, spawnY);
        this.width  = Math.max(width,  60f);
        this.height = Math.max(height, 60f);

        fillPaint.setColor(Color.rgb(255, 107, 53));
        fillPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.rgb(200, 60, 20));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
    }

    @Override
    protected void updateBehavior(float dt, Player player) {
        rotation += rotationVel * dt;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!active) return;

        float cx = x + width / 2f;
        float cy = y + height / 2f;

        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(rotation);

        // Fill (semi-transparent)
        fillPaint.setAlpha(drawAlpha);
        canvas.drawRect(-width / 2f, -height / 2f, width / 2f, height / 2f, fillPaint);

        // Border
        borderPaint.setAlpha(drawAlpha);
        canvas.drawRect(-width / 2f, -height / 2f, width / 2f, height / 2f, borderPaint);

        // Inner cross-hatch marks (armour texture)
        borderPaint.setAlpha(drawAlpha / 2);
        float m = 10f;
        canvas.drawRect(-width / 2f + m, -height / 2f + m,
                         width / 2f - m,  height / 2f - m, borderPaint);

        canvas.restore();
    }

    @Override
    public RectF getBounds() {
        // Use unrotated bounds for collision (simpler and fairer)
        return new RectF(x, y, x + width, y + height);
    }

    public float getWidth()  { return width;  }
    public float getHeight() { return height; }
}
