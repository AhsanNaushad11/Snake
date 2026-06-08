package com.ankanalytic.snake;

import android.content.Context;

import androidx.core.content.ContextCompat;

public final class ThemeOption {
    public final int id;
    public final int backgroundColor;
    public final int panelColor;
    public final int gridColor;
    public final int snakeHeadColor;
    public final int snakeBodyColor;
    public final int foodColor;
    public final int textColor;
    public final int hintColor;
    public final int accentColor;
    public final int statusBarColor;
    public final int buttonIconColor;

    private ThemeOption(
            int id,
            int backgroundColor,
            int panelColor,
            int gridColor,
            int snakeHeadColor,
            int snakeBodyColor,
            int foodColor,
            int textColor,
            int hintColor,
            int accentColor,
            int statusBarColor,
            int buttonIconColor
    ) {
        this.id = id;
        this.backgroundColor = backgroundColor;
        this.panelColor = panelColor;
        this.gridColor = gridColor;
        this.snakeHeadColor = snakeHeadColor;
        this.snakeBodyColor = snakeBodyColor;
        this.foodColor = foodColor;
        this.textColor = textColor;
        this.hintColor = hintColor;
        this.accentColor = accentColor;
        this.statusBarColor = statusBarColor;
        this.buttonIconColor = buttonIconColor;
    }

    public static ThemeOption fromPreference(Context context, int preference) {
        return switch (preference) {
            case GameSettings.THEME_DARK_RED -> fromResources(
                    context,
                    GameSettings.THEME_DARK_RED,
                    R.color.theme_dark_bg,
                    R.color.theme_dark_panel,
                    R.color.theme_dark_grid,
                    R.color.theme_dark_snake_head,
                    R.color.theme_dark_snake_body,
                    R.color.theme_dark_food,
                    R.color.theme_dark_text,
                    R.color.theme_dark_hint,
                    R.color.theme_dark_accent,
                    R.color.theme_dark_panel,
                    R.color.theme_dark_icon
            );
            case GameSettings.THEME_GRASS_YELLOW -> fromResources(
                    context,
                    GameSettings.THEME_GRASS_YELLOW,
                    R.color.theme_grass_bg,
                    R.color.theme_grass_panel,
                    R.color.theme_grass_grid,
                    R.color.theme_grass_snake_head,
                    R.color.theme_grass_snake_body,
                    R.color.theme_grass_food,
                    R.color.theme_grass_text,
                    R.color.theme_grass_hint,
                    R.color.theme_grass_accent,
                    R.color.theme_grass_panel,
                    R.color.theme_grass_icon
            );
            default -> fromResources(
                    context,
                    GameSettings.THEME_STANDARD,
                    R.color.theme_standard_bg,
                    R.color.theme_standard_panel,
                    R.color.theme_standard_grid,
                    R.color.theme_standard_snake_head,
                    R.color.theme_standard_snake_body,
                    R.color.theme_standard_food,
                    R.color.theme_standard_text,
                    R.color.theme_standard_hint,
                    R.color.theme_standard_accent,
                    R.color.theme_standard_panel,
                    R.color.theme_standard_icon
            );
        };
    }

    private static ThemeOption fromResources(
            Context context,
            int id,
            int backgroundColorRes,
            int panelColorRes,
            int gridColorRes,
            int snakeHeadColorRes,
            int snakeBodyColorRes,
            int foodColorRes,
            int textColorRes,
            int hintColorRes,
            int accentColorRes,
            int statusBarColorRes,
            int buttonIconColorRes
    ) {
        return new ThemeOption(
                id,
                ContextCompat.getColor(context, backgroundColorRes),
                ContextCompat.getColor(context, panelColorRes),
                ContextCompat.getColor(context, gridColorRes),
                ContextCompat.getColor(context, snakeHeadColorRes),
                ContextCompat.getColor(context, snakeBodyColorRes),
                ContextCompat.getColor(context, foodColorRes),
                ContextCompat.getColor(context, textColorRes),
                ContextCompat.getColor(context, hintColorRes),
                ContextCompat.getColor(context, accentColorRes),
                ContextCompat.getColor(context, statusBarColorRes),
                ContextCompat.getColor(context, buttonIconColorRes)
        );
    }
}
