package com.example.testwidget;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Switch switch1, switch2, switch3;
    TextView textview;
    EditText editText;
    Button button;
    SeekBar bar;

    boolean s1 = false;
    boolean s2 = false;
    boolean s3 = false;
    boolean emailCheck = false;
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

        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        switch3 = findViewById(R.id.switch3);
        textview = findViewById(R.id.textView);
        editText = findViewById(R.id.editTextTextEmailAddress);
        button = findViewById(R.id.button);
        bar = findViewById(R.id.seekBar);

        textview.setText("Color");

        ArrayList<String> emailDatabase = new ArrayList<>();

        emailDatabase.add("alpesh.vets@gmail.com");
        emailDatabase.add("diyanshishah13@gmail.com");
        emailDatabase.add("shahved234@gmail.com");

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                s1 = isChecked;
                checkColors();
            }
        });

        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                s2 = isChecked;
                checkColors();
            }
        });

        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                s3 = isChecked;
                checkColors();
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString();
                emailCheck = email.contains("@") && email.endsWith(".com")
                        && email.indexOf("@") < email.lastIndexOf(".com");

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputEmail = editText.getText().toString();
                if (emailCheck) {
                    Toast.makeText(MainActivity.this, "VALID", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "INVALID", Toast.LENGTH_SHORT).show();
                }

                if (emailDatabase.contains(inputEmail)) {
                    textview.setText("VERIFIED");
                } else {
                    textview.setText("NOT IN DATABASE");
                }
            }
        });

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newwidth = 5 + progress;
                if (newwidth < 30) {
                    textview.setTextSize(newwidth);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void checkColors(){
        if (s1 && s2 && s3) {
            textview.setTextColor(Color.BLUE);
        } else if (s1 && !s2 && s3) {
            textview.setTextColor(Color.RED);
        } else if (!s1 && !s2 && s3) {
            textview.setTextColor(Color.GREEN);
        } else {
            textview.setTextColor(Color.BLACK);
        }
    }
}