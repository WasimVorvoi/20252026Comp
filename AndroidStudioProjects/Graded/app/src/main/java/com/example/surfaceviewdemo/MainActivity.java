package com.example.surfaceviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    GameSurface gameSurface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurface.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameSurface.pause();
    }

    public class GameSurface extends SurfaceView implements Runnable{

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap ball, background;
        int ballX;
        Paint paintProperty;
        int screenWidth, screenHeight;

        public GameSurface(Context context) {
            super(context);
            holder = getHolder();

            background = BitmapFactory.decodeResource(getResources(), R.drawable.pokemon);
            ball = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.pokeball),200,200, false);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;

            paintProperty = new Paint();


        }

        @Override
        public void run() {
            Canvas canvas = null;
            Drawable d = getResources().getDrawable(R.drawable.pokemon, null);
            int step = 5;
            while(running){
                canvas = holder.lockCanvas(null);
                d.setBounds(getLeft(), getTop(), getRight(), getBottom());
                d.draw(canvas);
                float ballImageHorizontalSpacing = (screenWidth/2.0f)-(ball.getWidth()/2.0f);
                float ballImageVerticalSpacing = (screenHeight/2.0f)-(ball.getHeight()/2.0f);
                canvas.drawBitmap(ball, ballImageHorizontalSpacing + ballX,
                        ballImageVerticalSpacing, null);
                if(ballX == (int)ballImageHorizontalSpacing || ballX == -1 * (int)ballImageHorizontalSpacing)
                    step *= -1;
                ballX += step;
                holder.unlockCanvasAndPost(canvas);
            }
        }
        public void resume(){
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
        public void pause(){
            running = false;
            while(true){
                try{
                    gameThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}