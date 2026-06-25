package com.example.elf;

import android.media.Image;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    ImageView elfMoodImage;
    TextView moodDescription, cookieCountText;
    SeekBar energySeekBar;
    SwitchCompat workshopSwitch;

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
    }
    private void updateElfState() {
        cookieCountText.setText("Cookies: " + cookieCount); [cite: 10]

        // Rule logic for moods [cite: 14]
        if (cookieCount > 15) {
            currentMood = "gifting";
            moodDescription.setText("The elf is in gifting mode!"); [cite: 13, 20]
            elfMoodImage.setImageResource(R.drawable.elf_gifting); [cite: 20]
        } else if (energyLevel > 80) {
            currentMood = "happy";
            moodDescription.setText("The elf is happy!"); [cite: 13, 20]
            elfMoodImage.setImageResource(R.drawable.elf_happy); [cite: 20]
        } else if (energyLevel < 20 && cookieCount < 3) {
            currentMood = "concerned";
            moodDescription.setText("The elf is concerned..."); [cite: 13, 20]
            elfMoodImage.setImageResource(R.drawable.elf_concerned); [cite: 20]
        } else if (energyLevel < 10) {
            currentMood = "sleepy";
            moodDescription.setText("The elf is sleepy..."); [cite: 13, 20]
            elfMoodImage.setImageResource(R.drawable.elf_sleepy); [cite: 20]
        } else {
            currentMood = "idle";
            moodDescription.setText("The elf is idle..."); [cite: 13, 20, 23]
            elfMoodImage.setImageResource(R.drawable.elf_idle); [cite: 20]
        }
    }
}