package com.example.widget1test;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView textviewUsed, textviewmatch;
    EditText textviewEnter, textviewreenter;
    Switch switch1;
    Button button1;
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
        textviewEnter = findViewById(R.id.editTextText);
        textviewreenter = findViewById(R.id.editTextTextPassword);
        textviewUsed = findViewById(R.id.textView2);
        textviewmatch = findViewById(R.id.textView3);
        switch1 = findViewById(R.id.switch1);
        button1 = findViewById(R.id.button);
        ArrayList<String> passwordDatabase = new ArrayList<>();
        textviewEnter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
        });
        textviewreenter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(textviewEnter.getText().equals(textviewreenter.getText())){
                    switch1.setChecked(true);
                    textviewmatch.setText("MATCH");
                }
                else{
                    switch1.setChecked(false);
                    textviewmatch.setText("DOES NOT MATCH");
                }
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textviewEnter.getText().toString().equals(textviewreenter.getText().toString())){
                    switch1.setChecked(true);
                    textviewmatch.setText("MATCH");
                    if(passwordDatabase.contains(textviewEnter.getText().toString())){
                        textviewUsed.setText("Password Already Used");
                    }
                    else{
                        textviewUsed.setText("Password Not Used");
                        passwordDatabase.add(textviewEnter.getText().toString());
                    }
                }
                else{
                    switch1.setChecked(false);
                    textviewmatch.setText("DOES NOT MATCH");
                }

            }
        });
    }

}