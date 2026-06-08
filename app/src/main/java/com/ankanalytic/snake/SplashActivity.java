package com.ankanalytic.snake;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DURATION_MS = 1350L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView splashIcon = findViewById(R.id.splashIcon);
        ObjectAnimator pulse = ObjectAnimator.ofFloat(splashIcon, "scaleX", 0.92f, 1.05f, 1f);
        pulse.setDuration(SPLASH_DURATION_MS);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());
        pulse.start();

        ObjectAnimator pulseY = ObjectAnimator.ofFloat(splashIcon, "scaleY", 0.92f, 1.05f, 1f);
        pulseY.setDuration(SPLASH_DURATION_MS);
        pulseY.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseY.start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DURATION_MS);
    }
}
