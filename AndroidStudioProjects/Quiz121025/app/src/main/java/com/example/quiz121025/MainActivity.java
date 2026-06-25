package com.example.quiz121025;

import static java.lang.Math.*;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    RadioGroup rg;
    Button play;
    ImageView image;
    TextView Res, Total, hi;
    RadioButton rd1, rd2;
    int ai;
    int user;
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
        play = findViewById(R.id.button);
        rd1 = findViewById(R.id.radioButton);
        rd2 = findViewById(R.id.radioButton2);
        Total = findViewById(R.id.textView);
        Res = findViewById(R.id.textView2);
        image = findViewById(R.id.imageView);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ai = (int) ((Math.random() * 2) + 1);
                if (ai == 1) {
                    image.setImageResource(R.drawable.one);
                } else {
                    image.setImageResource(R.drawable.two);
                }
                if (rd1.isChecked()) {
                    user = 1;
                } else if (rd2.isChecked()) {
                    user = 2;
                } else {
                    Toast.makeText(MainActivity.this, "Pick 1 or 2", Toast.LENGTH_SHORT).show();
                    return;
                }
                int total = ai + user;
                Total.setText("Total is " + total);

                if (total % 2 == 0) {
                    Res.setText("You Win!");
                } else {
                    Res.setText("You Lose");
                }
            }
        });
        }

    }
