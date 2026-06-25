package com.example.widget1demo;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    Switch toggle;
    Button b1;
    int width = 145;
    TextView toggleText,displayColor;
    SeekBar bar;
    EditText edittext;
    @SuppressLint("MissingInflatedId")
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
        toggle = findViewById(R.id.s1);
        toggleText = findViewById(R.id.textViewSwitch);
        bar = findViewById(R.id.seekBar);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                if(b){
                    bar.setEnabled(true);
                }
                else{
                    bar.setEnabled(false);
                }
            }
        });
        edittext = findViewById(R.id.eTTName);
        displayColor = findViewById(R.id.textViewColor);


        edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().toLowerCase().equals("red")){
                    displayColor.setTextColor(Color.RED);
                }
                else if(charSequence.toString().toLowerCase().equals("blue")){
                    displayColor.setTextColor(Color.BLUE);
                }
                else if(charSequence.toString().toLowerCase().equals("green")){
                    displayColor.setTextColor(Color.GREEN);
                }
                else{
                    displayColor.setTextColor(Color.BLACK);
                }

            }
        });



        b1 = findViewById(R.id.button);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ViewGroup.LayoutParams params = b1.getLayoutParams();
                params.width = 390+i;
                b1.setLayoutParams(params);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}