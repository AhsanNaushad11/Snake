package com.ankanalytic.snake;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;

import com.ankanalytic.snake.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SnakeGameView.GameEvents {
    private SnakeGameView gameView;
    private View rootLayout;
    private View topBar;
    private View controlPanel;
    private TextView scoreText;
    private TextView highScoreText;
    private TextView hintText;
    private Button startPauseButton;
    private Button restartButton;
    private Button settingsButton;
    private ImageButton upButton;
    private ImageButton downButton;
    private ImageButton leftButton;
    private ImageButton rightButton;

    private ThemeOption currentTheme;
    private int highScore;
    private boolean isStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        rootLayout = binding.rootLayout;
        topBar = binding.topBar;
        controlPanel = binding.controlPanel;
        gameView = binding.snakeGameView;
        scoreText = binding.scoreText;
        highScoreText = binding.highScoreText;
        hintText = binding.hintText;
        startPauseButton = binding.startPauseButton;
        restartButton = binding.restartButton;
        settingsButton = binding.settingsButton;
        upButton = binding.upButton;
        downButton = binding.downButton;
        leftButton = binding.leftButton;
        rightButton = binding.rightButton;

        gameView.setGameEvents(this);

        highScore = GameSettings.getHighScore(this);
        updateScore(0);
        updateHighScore();

        startPauseButton.setOnClickListener(v -> {
            if (!isStarted) {
                gameView.startGame();
                isStarted = true;
                startPauseButton.setText(R.string.pause);
                return;
            }

            if (gameView.isRunning()) {
                gameView.pauseGame();
                startPauseButton.setText(R.string.resume);
            } else {
                gameView.resumeGame();
                startPauseButton.setText(R.string.pause);
            }
        });

        restartButton.setOnClickListener(v -> {
            gameView.resetGame();
            if (!gameView.isRunning()) {
                gameView.startGame();
            }
            isStarted = true;
            startPauseButton.setText(R.string.pause);
        });

        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        upButton.setOnClickListener(v -> gameView.queueDirection(Direction.UP));
        downButton.setOnClickListener(v -> gameView.queueDirection(Direction.DOWN));
        leftButton.setOnClickListener(v -> gameView.queueDirection(Direction.LEFT));
        rightButton.setOnClickListener(v -> gameView.queueDirection(Direction.RIGHT));

        applyUserSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyUserSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView.isRunning()) {
            gameView.pauseGame();
            startPauseButton.setText(R.string.resume);
        }
    }

    @Override
    public void onScoreChanged(int score, boolean foodEaten) {
        updateScore(score);
        if (foodEaten) {
            animateScorePulse();
        }

        if (score > highScore) {
            highScore = score;
            GameSettings.setHighScore(this, highScore);
            updateHighScore();
        }
    }

    @Override
    public void onGameOver(int finalScore) {
        startPauseButton.setText(R.string.start);
        isStarted = false;

        new AlertDialog.Builder(this)
                .setTitle(R.string.game_over)
                .setMessage(getString(R.string.score_label, finalScore))
                .setPositiveButton(R.string.try_again, (dialog, which) -> {
                    gameView.resetGame();
                    gameView.startGame();
                    isStarted = true;
                    startPauseButton.setText(R.string.pause);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void applyUserSettings() {
        gameView.setSpeedLevel(GameSettings.getSpeed(this));
        ThemeOption selectedTheme = ThemeOption.fromPreference(this, GameSettings.getTheme(this));
        gameView.applyTheme(selectedTheme);

        if (currentTheme == null || currentTheme.id != selectedTheme.id) {
            currentTheme = selectedTheme;
            applyThemeToViews(currentTheme);
        }
    }

    private void applyThemeToViews(ThemeOption theme) {
        rootLayout.setBackgroundColor(theme.backgroundColor);

        topBar.setBackground(createPanelBackground(theme.panelColor, theme.gridColor));
        controlPanel.setBackground(createPanelBackground(theme.panelColor, theme.gridColor));
        gameView.setBackground(createPanelBackground(theme.panelColor, theme.gridColor));

        scoreText.setTextColor(theme.textColor);
        highScoreText.setTextColor(theme.textColor);
        hintText.setTextColor(theme.hintColor);

        ColorStateList accentTint = ColorStateList.valueOf(theme.accentColor);
        startPauseButton.setBackgroundTintList(accentTint);
        restartButton.setBackgroundTintList(accentTint);
        settingsButton.setBackgroundTintList(accentTint);

        startPauseButton.setTextColor(theme.buttonIconColor);
        restartButton.setTextColor(theme.buttonIconColor);
        settingsButton.setTextColor(theme.buttonIconColor);

        upButton.setBackgroundTintList(accentTint);
        downButton.setBackgroundTintList(accentTint);
        leftButton.setBackgroundTintList(accentTint);
        rightButton.setBackgroundTintList(accentTint);

        ColorStateList iconTint = ColorStateList.valueOf(theme.buttonIconColor);
        upButton.setImageTintList(iconTint);
        downButton.setImageTintList(iconTint);
        leftButton.setImageTintList(iconTint);
        rightButton.setImageTintList(iconTint);

        getWindow().setStatusBarColor(theme.statusBarColor);
        getWindow().setNavigationBarColor(theme.statusBarColor);

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        boolean useLightSystemIcons = theme.id != GameSettings.THEME_DARK_RED;
        insetsController.setAppearanceLightStatusBars(useLightSystemIcons);
        insetsController.setAppearanceLightNavigationBars(useLightSystemIcons);
    }

    private GradientDrawable createPanelBackground(int fillColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(24));
        drawable.setColor(fillColor);
        drawable.setStroke((int) dpToPx(2), strokeColor);
        return drawable;
    }

    private float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void animateScorePulse() {
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(scoreText, View.SCALE_X, 1f, 1.2f, 1f);
        pulseX.setDuration(260);
        pulseX.start();

        ObjectAnimator pulseY = ObjectAnimator.ofFloat(scoreText, View.SCALE_Y, 1f, 1.2f, 1f);
        pulseY.setDuration(260);
        pulseY.start();
    }

    private void updateScore(int score) {
        scoreText.setText(getString(R.string.score_label, score));
    }

    private void updateHighScore() {
        highScoreText.setText(getString(R.string.best_score_label, highScore));
    }
}
