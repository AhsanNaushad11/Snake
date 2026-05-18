package com.example.snake;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.snake.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    private TextView speedLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        speedLabel = binding.settingsSpeedLabel;
        SeekBar speedSeekBar = binding.settingsSpeedSeekBar;
        RadioGroup themeGroup = binding.themeGroup;
        Button doneButton = binding.doneButton;
        Button backButton = binding.backButton;

        int savedSpeed = GameSettings.getSpeed(this);
        int savedTheme = GameSettings.getTheme(this);

        speedSeekBar.setProgress(savedSpeed - 1);
        updateSpeedLabel(savedSpeed);

        if (savedTheme == GameSettings.THEME_DARK_RED) {
            binding.themeDarkRed.setChecked(true);
        } else if (savedTheme == GameSettings.THEME_GRASS_YELLOW) {
            binding.themeGrassYellow.setChecked(true);
        } else {
            binding.themeStandardWhite.setChecked(true);
        }

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int speed = progress + 1;
                GameSettings.setSpeed(SettingsActivity.this, speed);
                updateSpeedLabel(speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.themeDarkRed.getId()) {
                GameSettings.setTheme(this, GameSettings.THEME_DARK_RED);
            } else if (checkedId == binding.themeGrassYellow.getId()) {
                GameSettings.setTheme(this, GameSettings.THEME_GRASS_YELLOW);
            } else {
                GameSettings.setTheme(this, GameSettings.THEME_STANDARD);
            }
        });

        doneButton.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
    }

    private void updateSpeedLabel(int speed) {
        speedLabel.setText(getString(R.string.speed_label, speed));
    }
}
