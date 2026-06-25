package com.example.surfaceviewdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager sensorManager;
    float sensorX, sensorY;
    Sensor accelerometerSensor, gyroscopeSensor;
    GameSurface gameSurface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }
    @Override
    protected void onResume() {
        super.onResume();
        gameSurface.resume();
        sensorManager.registerListener(this, accelerometerSensor,sensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameSurface.pause();
        sensorManager.unregisterListener(this);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0];
            sensorY = event.values[1];
        }
    }

    public class GameSurface extends SurfaceView implements Runnable{

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap ball, background;
        public int ballX;
        public int ballY;
        public int ballZ;
        public int sensx;
        public int sensy;
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
            while (running) {
                if (!holder.getSurface().isValid()) continue;
                Canvas canvas = holder.lockCanvas();
                canvas.drawBitmap(background, null, canvas.getClipBounds(), null);
                ballX -= (sensorX * 5.0f);
                ballY += (sensorY * 5.0f);
                if (ballX < 0) ballX = 0;
                if (ballX > screenWidth - ball.getWidth()) ballX = screenWidth - ball.getWidth();
                if (ballY < 0) ballY = 0;
                if (ballY > screenHeight - ball.getHeight()) ballY = screenHeight - ball.getHeight();
                canvas.drawBitmap(ball, ballX, ballY, null);
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