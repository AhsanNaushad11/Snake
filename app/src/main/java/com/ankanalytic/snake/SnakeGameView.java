package com.ankanalytic.snake;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public class SnakeGameView extends View {
    private static final int GRID_SIZE = 20;
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint panelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint snakeBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint snakeHeadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint foodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Deque<Point> snake = new ArrayDeque<>();    private final Runnable tickRunner = this::tick;
    private final List<Point> previousSnake = new ArrayList<>();
    private final List<Sparkle> sparkles = new ArrayList<>();
    private Point food = new Point();
    private Direction direction = Direction.RIGHT;
    private Direction queuedDirection = Direction.RIGHT;
    private float cellSize;
    private float boardLeft;
    private float boardTop;
    private float interpolator;
    private int score;
    private int tickDurationMs = 190;
    private boolean running;
    private boolean gameOver;
    private float touchStartX;
    private float touchStartY;
    private GameEvents gameEvents;
    private ValueAnimator moveAnimator;
    private ThemeOption themeOption;
    public SnakeGameView(Context context) {
        super(context);
        init();
    }
    public SnakeGameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnakeGameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint.setStrokeWidth(1.5f);

        eyePaint.setColor(Color.WHITE);
        sparklePaint.setStyle(Paint.Style.FILL);

        applyTheme(ThemeOption.fromPreference(getContext(), GameSettings.THEME_STANDARD));
        setFocusable(true);
        resetGame();
    }

    public void setGameEvents(GameEvents events) {
        this.gameEvents = events;
    }

    public boolean isRunning() {
        return running;
    }

    public void setSpeedLevel(int speedLevel) {
        int clamped = Math.max(1, Math.min(10, speedLevel));
        tickDurationMs = 280 - (clamped * 20);
        if (running) {
            handler.removeCallbacks(tickRunner);
            handler.postDelayed(tickRunner, tickDurationMs);
        }
    }

    public void startGame() {
        if (running) {
            return;
        }

        if (gameOver) {
            resetGame();
        }

        running = true;
        handler.postDelayed(tickRunner, tickDurationMs);
    }

    public void pauseGame() {
        running = false;
        handler.removeCallbacks(tickRunner);
    }

    public void resumeGame() {
        if (!gameOver) {
            running = true;
            handler.removeCallbacks(tickRunner);
            handler.postDelayed(tickRunner, tickDurationMs);
        }
    }

    public void resetGame() {
        handler.removeCallbacks(tickRunner);
        running = false;
        gameOver = false;
        score = 0;

        snake.clear();
        snake.add(new Point(6, 10));
        snake.add(new Point(5, 10));
        snake.add(new Point(4, 10));

        direction = Direction.RIGHT;
        queuedDirection = Direction.RIGHT;

        previousSnake.clear();
        syncPreviousSnake();

        spawnFood();
        if (gameEvents != null) {
            gameEvents.onScoreChanged(score, false);
        }
        invalidate();
    }

    public void queueDirection(Direction newDirection) {
        if (newDirection == null || isReverse(direction, newDirection)) {
            return;
        }
        queuedDirection = newDirection;
    }

    public void applyTheme(ThemeOption option) {
        if (option == null) {
            return;
        }

        themeOption = option;
        gridPaint.setColor(themeOption.gridColor);
        panelPaint.setColor(themeOption.panelColor);
        snakeBodyPaint.setColor(themeOption.snakeBodyColor);
        snakeHeadPaint.setColor(themeOption.snakeHeadColor);
        foodPaint.setColor(themeOption.foodColor);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(tickRunner);
        if (moveAnimator != null) {
            moveAnimator.cancel();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float boardSize = Math.min(w, h) * 0.94f;
        cellSize = boardSize / GRID_SIZE;
        boardLeft = (w - boardSize) / 2f;
        boardTop = (h - boardSize) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);
        drawFood(canvas);
        drawSnake(canvas);
        drawSparkles(canvas);
    }

    private void drawBoard(Canvas canvas) {
        float boardSize = cellSize * GRID_SIZE;
        RectF boardRect = new RectF(boardLeft, boardTop, boardLeft + boardSize, boardTop + boardSize);
        canvas.drawRoundRect(boardRect, 26f, 26f, panelPaint);

        for (int i = 0; i <= GRID_SIZE; i++) {
            float x = boardLeft + i * cellSize;
            float y = boardTop + i * cellSize;
            canvas.drawLine(x, boardTop, x, boardTop + boardSize, gridPaint);
            canvas.drawLine(boardLeft, y, boardLeft + boardSize, y, gridPaint);
        }
    }

    private void drawFood(Canvas canvas) {
        float baseCx = boardLeft + (food.x + 0.5f) * cellSize;
        float baseCy = boardTop + (food.y + 0.5f) * cellSize;

        float time = System.currentTimeMillis() % 1000L;
        float pulse = 0.78f + 0.16f * (float) Math.sin(time / 1000f * Math.PI * 2f);

        canvas.drawCircle(baseCx, baseCy, cellSize * 0.28f * pulse, foodPaint);
    }

    private void drawSnake(Canvas canvas) {
        if (snake.isEmpty()) {
            return;
        }

        List<Point> currentSnake = new ArrayList<>(snake);
        for (int i = 0; i < currentSnake.size(); i++) {
            Point current = currentSnake.get(i);
            Point previous = i < previousSnake.size() ? previousSnake.get(i) : current;

            float px = lerp(previous.x, current.x, interpolator);
            float py = lerp(previous.y, current.y, interpolator);

            float left = boardLeft + px * cellSize + cellSize * 0.08f;
            float top = boardTop + py * cellSize + cellSize * 0.08f;
            float right = left + cellSize * 0.84f;
            float bottom = top + cellSize * 0.84f;

            Paint paint = i == 0 ? snakeHeadPaint : snakeBodyPaint;
            float radius = i == 0 ? cellSize * 0.28f : cellSize * 0.22f;
            canvas.drawRoundRect(new RectF(left, top, right, bottom), radius, radius, paint);

            if (i == 0) {
                drawEyes(canvas, left, top, right, bottom);
            }
        }
    }

    private void drawEyes(Canvas canvas, float left, float top, float right, float bottom) {
        float cx = (left + right) / 2f;
        float cy = (top + bottom) / 2f;
        float offset = cellSize * 0.12f;
        float radius = cellSize * 0.06f;

        switch (direction) {
            case UP:
                canvas.drawCircle(cx - offset, cy - offset, radius, eyePaint);
                canvas.drawCircle(cx + offset, cy - offset, radius, eyePaint);
                break;
            case DOWN:
                canvas.drawCircle(cx - offset, cy + offset, radius, eyePaint);
                canvas.drawCircle(cx + offset, cy + offset, radius, eyePaint);
                break;
            case LEFT:
                canvas.drawCircle(cx - offset, cy - offset, radius, eyePaint);
                canvas.drawCircle(cx - offset, cy + offset, radius, eyePaint);
                break;
            case RIGHT:
            default:
                canvas.drawCircle(cx + offset, cy - offset, radius, eyePaint);
                canvas.drawCircle(cx + offset, cy + offset, radius, eyePaint);
                break;
        }
    }

    private void drawSparkles(Canvas canvas) {
        long now = System.currentTimeMillis();
        for (int i = sparkles.size() - 1; i >= 0; i--) {
            Sparkle sparkle = sparkles.get(i);
            float life = (now - sparkle.startMs) / (float) sparkle.durationMs;
            if (life >= 1f) {
                sparkles.remove(i);
                continue;
            }

            float alpha = 1f - life;
            int sparkleColor = themeOption != null ? themeOption.foodColor : Color.parseColor("#FF8F62");
            sparklePaint.setColor(Color.argb(
                    (int) (alpha * 255),
                    Color.red(sparkleColor),
                    Color.green(sparkleColor),
                    Color.blue(sparkleColor)
            ));

            float cx = boardLeft + (sparkle.gridX + 0.5f) * cellSize + sparkle.dx * life;
            float cy = boardTop + (sparkle.gridY + 0.5f) * cellSize + sparkle.dy * life;

            canvas.drawCircle(cx, cy, cellSize * 0.08f * alpha, sparklePaint);
        }
    }

    private void tick() {
        if (!running || gameOver) {
            return;
        }

        direction = queuedDirection;
        syncPreviousSnake();

        Point head = snake.peekFirst();
        int[] next = NativeGameLib.nextHead(head.x, head.y, direction.ordinal());
        int nextX = next[0];
        int nextY = next[1];

        if (nextX < 0 || nextY < 0 || nextX >= GRID_SIZE || nextY >= GRID_SIZE) {
            triggerGameOver();
            return;
        }

        boolean ateFood = nextX == food.x && nextY == food.y;
        int[] flat = flattenSnake(!ateFood);
        if (NativeGameLib.containsPoint(flat, nextX, nextY)) {
            triggerGameOver();
            return;
        }

        snake.addFirst(new Point(nextX, nextY));

        if (ateFood) {
            score += 10;
            spawnFood();
            createSparkles(nextX, nextY);
        } else {
            snake.removeLast();
        }

        animateMove();

        if (gameEvents != null) {
            gameEvents.onScoreChanged(score, ateFood);
        }

        handler.postDelayed(tickRunner, tickDurationMs);
    }

    private void triggerGameOver() {
        running = false;
        gameOver = true;
        handler.removeCallbacks(tickRunner);
        if (gameEvents != null) {
            gameEvents.onGameOver(score);
        }
        invalidate();
    }

    private void animateMove() {
        if (moveAnimator != null) {
            moveAnimator.cancel();
        }

        moveAnimator = ValueAnimator.ofFloat(0f, 1f);
        moveAnimator.setDuration(Math.max(90L, tickDurationMs - 20L));
        moveAnimator.setInterpolator(new LinearInterpolator());
        moveAnimator.addUpdateListener(animation -> {
            interpolator = (float) animation.getAnimatedValue();
            invalidate();
        });
        moveAnimator.start();
    }

    private void spawnFood() {
        boolean valid;
        do {
            food.x = random.nextInt(GRID_SIZE);
            food.y = random.nextInt(GRID_SIZE);
            valid = true;
            for (Point p : snake) {
                if (p.x == food.x && p.y == food.y) {
                    valid = false;
                    break;
                }
            }
        } while (!valid);
    }

    private void createSparkles(int gridX, int gridY) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < 9; i++) {
            float angle = (float) (i * (Math.PI * 2f / 9f));
            float distance = cellSize * (0.2f + random.nextFloat() * 0.6f);
            sparkles.add(new Sparkle(
                    gridX,
                    gridY,
                    (float) Math.cos(angle) * distance,
                    (float) Math.sin(angle) * distance,
                    now,
                    260 + random.nextInt(180)
            ));
        }
    }

    private int[] flattenSnake(boolean includeTail) {
        int count = snake.size();
        if (!includeTail && count > 0) {
            count -= 1;
        }
        int[] flattened = new int[count * 2];
        int idx = 0;
        int i = 0;
        for (Point p : snake) {
            if (!includeTail && i == snake.size() - 1) {
                break;
            }
            flattened[idx++] = p.x;
            flattened[idx++] = p.y;
            i++;
        }
        return flattened;
    }

    private void syncPreviousSnake() {
        previousSnake.clear();
        for (Point segment : snake) {
            previousSnake.add(new Point(segment));
        }
        interpolator = 0f;
    }

    private boolean isReverse(Direction current, Direction next) {
        return (current == Direction.UP && next == Direction.DOWN)
                || (current == Direction.DOWN && next == Direction.UP)
                || (current == Direction.LEFT && next == Direction.RIGHT)
                || (current == Direction.RIGHT && next == Direction.LEFT);
    }

    private float lerp(float from, float to, float progress) {
        return from + (to - from) * progress;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - touchStartX;
                float dy = event.getY() - touchStartY;

                if (Math.abs(dx) < 20 && Math.abs(dy) < 20) {
                    return true;
                }

                if (Math.abs(dx) > Math.abs(dy)) {
                    queueDirection(dx > 0 ? Direction.RIGHT : Direction.LEFT);
                } else {
                    queueDirection(dy > 0 ? Direction.DOWN : Direction.UP);
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public interface GameEvents {
        void onScoreChanged(int score, boolean foodEaten);

        void onGameOver(int finalScore);
    }

    private static final class Sparkle {
        final int gridX;
        final int gridY;
        final float dx;
        final float dy;
        final long startMs;
        final int durationMs;

        Sparkle(int gridX, int gridY, float dx, float dy, long startMs, int durationMs) {
            this.gridX = gridX;
            this.gridY = gridY;
            this.dx = dx;
            this.dy = dy;
            this.startMs = startMs;
            this.durationMs = durationMs;
        }
    }


}
