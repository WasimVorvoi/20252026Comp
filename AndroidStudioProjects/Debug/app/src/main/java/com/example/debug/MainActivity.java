package com.example.debug;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{

    private Switch switchTop, switchMiddle, switchBottom;
    private EditText emailInput;
    private TextView resultTextView;
    private SeekBar sizeSeekBar;
    private ArrayList<String> emailDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchTop = findViewById(R.id.switchTop);
        switchMiddle = findViewById(R.id.switchMiddle);
        switchBottom = findViewById(R.id.switchBottom);
        emailInput = findViewById(R.id.emailInput);
        resultTextView = findViewById(R.id.resultTextView);
        sizeSeekBar = findViewById(R.id.sizeSeekBar);

        emailDatabase = new ArrayList<>();
        // Hardcoded email database
        emailDatabase.add("example1@example.com");
        emailDatabase.add("example2@example.com");
        emailDatabase.add("example3@example.com");

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgessChanged(SeekBar seekBar, int progress, boolean fromUser) {
                resultTextView.setTextSize(resultTextView.getMinEms()+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this example
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this example
            }
        });

        switchTop.setOnCheckedChangeListener(this);
        switchMiddle.setOnCheckedChangeListener(this);
        switchBottom.setOnCheckedChangeListener(this);
    }

    private void updateResultText() {
        boolean topSwitch = switchTop.isChecked();
        boolean middleSwitch = switchMiddle.isChecked();
        boolean bottomSwitch = switchBottom.isChecked();

        if (topSwitch && middleSwitch && bottomSwitch) {
            resultTextView.setTextColor(Color.BLUE);
        } else if (topSwitch && !middleSwitch && bottomSwitch) {
            resultTextView.setTextColor(Color.RED);
        } else if (!topSwitch && !middleSwitch && bottomSwitch) {
            resultTextView.setTextColor(Color.GREEN);
        } else {
            resultTextView.setTextColor(Color.BLACK);
        }
    }

    public void onVerifyClick(View view) {
        String emailToVerify = emailInput.getText().toString().trim();
        if (isValidEmail(emailToVerify)) {
            if (emailDatabase.contains(emailToVerify)) {
                resultTextView.setText("VERIFIED");
            } else {
                resultTextView.setText("NOT IN DATABASE");
            }
            Toast.makeText(this, "Valid email address", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.endsWith(".com") && email.indexOf("@") > email.indexOf(".com");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        updateResultText();
    }
}