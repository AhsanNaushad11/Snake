package com.example.snake;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SnakeGameView.GameEvents {
    private static final String PREFS = "snake_prefs";
    private static final String HIGH_SCORE_KEY = "high_score";

    private SnakeGameView gameView;
    private TextView scoreText;
    private TextView highScoreText;
    private TextView speedLabel;
    private Button startPauseButton;

    private int highScore;
    private boolean isStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.snakeGameView);
        scoreText = findViewById(R.id.scoreText);
        highScoreText = findViewById(R.id.highScoreText);
        speedLabel = findViewById(R.id.speedLabel);
        startPauseButton = findViewById(R.id.startPauseButton);
        Button restartButton = findViewById(R.id.restartButton);
        SeekBar speedSeekBar = findViewById(R.id.speedSeekBar);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        highScore = prefs.getInt(HIGH_SCORE_KEY, 0);

        gameView.setGameEvents(this);
        gameView.setSpeedLevel(speedSeekBar.getProgress() + 1);

        updateScore(0);
        updateHighScore();
        updateSpeedLabel(speedSeekBar.getProgress() + 1);

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

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int speed = progress + 1;
                updateSpeedLabel(speed);
                gameView.setSpeedLevel(speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ImageButton upButton = findViewById(R.id.upButton);
        ImageButton downButton = findViewById(R.id.downButton);
        ImageButton leftButton = findViewById(R.id.leftButton);
        ImageButton rightButton = findViewById(R.id.rightButton);

        upButton.setOnClickListener(v -> gameView.queueDirection(Direction.UP));
        downButton.setOnClickListener(v -> gameView.queueDirection(Direction.DOWN));
        leftButton.setOnClickListener(v -> gameView.queueDirection(Direction.LEFT));
        rightButton.setOnClickListener(v -> gameView.queueDirection(Direction.RIGHT));
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
            getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putInt(HIGH_SCORE_KEY, highScore)
                .apply();
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

    private void updateSpeedLabel(int speed) {
        speedLabel.setText(getString(R.string.speed_label, speed));
    }
}
