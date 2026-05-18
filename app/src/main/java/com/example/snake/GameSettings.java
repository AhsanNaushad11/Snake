package com.example.snake;

import android.content.Context;
import android.content.SharedPreferences;

public final class GameSettings {
    public static final int THEME_STANDARD = 0;
    public static final int THEME_DARK_RED = 1;
    public static final int THEME_GRASS_YELLOW = 2;

    private static final String PREFS = "snake_prefs";
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_THEME = "theme";

    private static final int DEFAULT_SPEED = 5;

    private GameSettings() {
    }

    public static int getHighScore(Context context) {
        return prefs(context).getInt(KEY_HIGH_SCORE, 0);
    }

    public static void setHighScore(Context context, int highScore) {
        prefs(context).edit().putInt(KEY_HIGH_SCORE, Math.max(0, highScore)).apply();
    }

    public static int getSpeed(Context context) {
        return clampSpeed(prefs(context).getInt(KEY_SPEED, DEFAULT_SPEED));
    }

    public static void setSpeed(Context context, int speed) {
        prefs(context).edit().putInt(KEY_SPEED, clampSpeed(speed)).apply();
    }

    public static int getTheme(Context context) {
        int stored = prefs(context).getInt(KEY_THEME, THEME_STANDARD);
        if (stored < THEME_STANDARD || stored > THEME_GRASS_YELLOW) {
            return THEME_STANDARD;
        }
        return stored;
    }

    public static void setTheme(Context context, int theme) {
        int normalized = theme;
        if (theme < THEME_STANDARD || theme > THEME_GRASS_YELLOW) {
            normalized = THEME_STANDARD;
        }
        prefs(context).edit().putInt(KEY_THEME, normalized).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static int clampSpeed(int speed) {
        return Math.max(1, Math.min(10, speed));
    }
}
