package com.example.sfdsfsfsfsdfdsfsdfds;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    ConstraintLayout constraintLayout;
    TextView textViewInCode;
    ImageView imageViewInCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        constraintLayout = findViewById(R.id.main);
        textViewInCode = new TextView(this);
        textViewInCode.setId(View.generateViewId());
        textViewInCode.setText("Tap Me!");
        textViewInCode.setTextColor(Color.RED);
        ConstraintLayout.LayoutParams textParams = new
                ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewInCode.setLayoutParams(textParams);
        constraintLayout.addView(textViewInCode);
        imageViewInCode = new ImageView(this);
        imageViewInCode.setId(View.generateViewId());
        imageViewInCode.setImageResource(R.drawable.attack);

        ViewGroup.LayoutParams imageParams = new
                ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        imageViewInCode.setLayoutParams(imageParams);
        constraintLayout.addView(imageViewInCode);
        ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayout);
        set.connect(textViewInCode.getId(), ConstraintSet.TOP,
                constraintLayout.getId(), ConstraintSet.TOP);
        set.connect(textViewInCode.getId(), ConstraintSet.BOTTOM,
                constraintLayout.getId(), ConstraintSet.BOTTOM);
        set.connect(textViewInCode.getId(), ConstraintSet.LEFT,
                constraintLayout.getId(), ConstraintSet.LEFT);
        set.connect(textViewInCode.getId(), ConstraintSet.RIGHT,
                constraintLayout.getId(), ConstraintSet.RIGHT);
        set.connect(imageViewInCode.getId(), ConstraintSet.TOP,
                constraintLayout.getId(), ConstraintSet.TOP);
        set.connect(imageViewInCode.getId(), ConstraintSet.BOTTOM,
                constraintLayout.getId(), ConstraintSet.BOTTOM);
        set.connect(imageViewInCode.getId(), ConstraintSet.LEFT,
                constraintLayout.getId(), ConstraintSet.LEFT);
        set.connect(imageViewInCode.getId(), ConstraintSet.RIGHT,
                constraintLayout.getId(), ConstraintSet.RIGHT);
        set.setVerticalBias(imageViewInCode.getId(), 0.2f);
        set.setScaleX(imageViewInCode.getId(), 0.5f);
        set.setScaleY(imageViewInCode.getId(), 0.5f);
        set.applyTo(constraintLayout);
        final RotateAnimation rotate = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(400);
        final ScaleAnimation scale = new ScaleAnimation(
                0.5f, 1.5f,
                0.5f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(400);
        textViewInCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(scale);
            }
        });
        imageViewInCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(rotate);
            }
        });
    }
}