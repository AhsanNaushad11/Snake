# Snake (Android Native)

A native Android Snake game built with Java activities/views and a JNI C++ helper.

## Features
- Splash screen with cartoon snake branding.
- Animated game board with smooth snake movement interpolation.
- Pulse animation for food.
- Sparkle animation when food is eaten.
- Score + best score persistence.
- Speed control (1 to 10).
- Swipe controls and on-screen directional controls.
- C++ (JNI) logic for movement vector and collision lookup helper.

## Project Name
- App name: `Snake`

## Build Notes
- Android Gradle Plugin: `8.4.2`
- Gradle Wrapper: `8.6`
- Expected JDK for AGP 8.4.x: Java 17

If your machine defaults to Java 24+, point Android Studio/Gradle to JDK 17 before building.

## Key Files
- `app/src/main/java/com/example/snake/MainActivity.java`
- `app/src/main/java/com/example/snake/SplashActivity.java`
- `app/src/main/java/com/example/snake/SnakeGameView.java`
- `app/src/main/java/com/example/snake/NativeGameLib.java`
- `app/src/main/cpp/native-lib.cpp`
