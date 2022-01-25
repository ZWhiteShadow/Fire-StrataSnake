package com.example.stratasnake27;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


public class SnakeEngine extends SurfaceView implements Runnable {

    // Our game thread for the main game loop
    private Thread thread = null;


    @Override
    public void run() {

        while (isPlaying) {

            // Update 10 times a second
            if (updateRequired()) {
                update();
                draw();
            }

        }
    }

    // For tracking movement Heading
    static char direction = 'D';
    public static void setDirection(char newDirection){
        direction = newDirection;
    }

    // Where is Bob hiding?
    private int bobX;
    private int bobY;

    // Control pausing between updates
    private long nextFrameTime;

    // How many points does the player have
    private static int score;

    // The location in the grid of all the segments
    private final int[] x;
    private final int[] y;

    // Everything we need for drawing
// Is the game currently playing?
    private volatile boolean isPlaying;

    // Required to use canvas
    private final SurfaceHolder surfaceHolder;

    // Some paint for our canvas
    public final Paint paint = new Paint();

    Bitmap bitmap;
    Canvas canvas;
    int SCREEN_WIDTH, SCREEN_HEIGHT;
    int SQUARES_ACROSS, SQUARES_DOWN;
    int UNIT_SIZE, GAME_UNITS, TOTAL_SQUARES;
    int sneggyBodyParts;

    public SnakeEngine(Context context, Point size) {
        super(context);

        SCREEN_WIDTH = size.x;
        SCREEN_HEIGHT = size.y;
        UNIT_SIZE = SCREEN_WIDTH / 54;
        GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
        SQUARES_DOWN = (SCREEN_HEIGHT / UNIT_SIZE) - 3;
        SQUARES_ACROSS = (SCREEN_WIDTH / UNIT_SIZE);
        TOTAL_SQUARES = SQUARES_ACROSS * SQUARES_DOWN;
        sneggyBodyParts = 11;
        x = new int[GAME_UNITS];
        y = new int[GAME_UNITS];
        bitmap = Bitmap.createBitmap(SCREEN_WIDTH, SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        // Initialize the drawing objects
        surfaceHolder = getHolder();

        // Start the game
        newGame();
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void newGame() {
        // Start with a single snake segment
        x[0] = SQUARES_ACROSS / 2;
        y[0] = SQUARES_DOWN / 2;

        // Get Bob ready for dinner
        spawnBob();

        // Reset the score
        score = 0;

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnBob() {
        Random random = new Random();
        bobX = random.nextInt(SQUARES_ACROSS - 1) + 1;
        bobY = random.nextInt(SQUARES_DOWN - 1) + 1;
    }

    private void eatBob() {
        //  Got him!
        // Increase the size of the snake
        sneggyBodyParts++;
        //replace Bob
        // This reminds me of Edge of Tomorrow. Oneday Bob will be ready!
        spawnBob();
        //add to the score
        score = score + 1;
    }

    private void moveSnake() {
        // Move the body
        for (int i = sneggyBodyParts; i > 0; i--) {
            // Start at the back and move it
            // to the position of the segment in front of it
            x[i] = x[i - 1];
            y[i] = y[i - 1];

            // Exclude the head because
            // the head has nothing in front of it
        }

        // Move the head in the appropriate heading
        switch (direction) {
            case 'U':
                y[0]--;
                break;

            case 'R':
                x[0]++;
                break;

            case 'D':
                y[0]++;
                break;

            case 'L':
                x[0]--;
                break;

        }
        //Loop around sides
        if (x[0] < 0) {
            x[0] += SQUARES_ACROSS;
        }
        if (x[0] >= SQUARES_ACROSS) {
            x[0] = 0;
        }
        if (y[0] < 0) {
            y[0] += SQUARES_DOWN;
        }
        if (y[0] >= SQUARES_DOWN) {
            y[0] = 0;
        }
    }

    public void update() {
        // Did the head of the snake eat Bob?
        if (x[0] == bobX && y[0] == bobY) {
            eatBob();
        }

        moveSnake();
    }

    public void draw() {
        // Get a lock on the canvas
        if (surfaceHolder.getSurface().isValid()) {

            // A canvas for our paint
            Canvas canvas = surfaceHolder.lockCanvas();

            // Fill the screen with Game Code School black
            canvas.drawColor(Color.argb(255, 0, 0, 0));
            paint.setColor(Color.rgb(0, 150, 0));

            // Set the color of the paint to draw the snake white
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Draw the snake one block at a time
            int colorShift = 255 / (int) sneggyBodyParts;
            for (int i = 0; i < sneggyBodyParts; i++) {
                paint.setColor(Color.rgb(255 - (colorShift * i), 0, 255 - (colorShift * i)));
                canvas.drawRect(x[i] * UNIT_SIZE,
                        (y[i] * UNIT_SIZE),
                        (x[i] * UNIT_SIZE) + UNIT_SIZE,
                        (y[i] * UNIT_SIZE) + UNIT_SIZE,
                        paint);
            }

            drawGrid(canvas);

            // Scale the HUD text
            paint.setTextSize(90);
            paint.setColor(Color.WHITE);
            canvas.drawText("Score: " + score, 10, 70, paint);

            // Set the color of the paint to draw Bob red
            paint.setColor(Color.argb(255, 255, 0, 0));

            // Draw Bob
            canvas.drawRect(bobX * UNIT_SIZE,
                    (bobY * UNIT_SIZE),
                    (bobX * UNIT_SIZE) + UNIT_SIZE,
                    (bobY * UNIT_SIZE) + UNIT_SIZE,
                    paint);

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {

        // Are we due to update the frame
        if (nextFrameTime <= System.currentTimeMillis()) {
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            // Update the game 10 times per second
            long FPS = 10;
            // There are 1000 milliseconds in a second
            long MILLIS_PER_SECOND = 1000;
            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }



        @Override
        public boolean onTouchEvent (MotionEvent motionEvent){
            float xDiff = (x[0] * UNIT_SIZE) - motionEvent.getX();
            float yDiff = (y[0] * UNIT_SIZE) - motionEvent.getY();
            xDiff = (xDiff <  UNIT_SIZE) ? 0 : xDiff;
            yDiff = (yDiff <  UNIT_SIZE) ? 0 : yDiff;

            if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                switch (direction) {

                    case 'U':
                    case 'D':
                        if (xDiff > 0) {
                            direction = 'L';
                        } else {
                            direction = 'R';
                        }
                        break;

                    case 'L':
                    case 'R':
                        if (yDiff > 0) {
                            direction = 'U';
                        } else {
                            direction = 'D';
                        }
                        break;
                }
            }
            return true;
        }

        public void drawGrid (Canvas canvas){
            //draw Vertical Lines on board
            paint.setColor(Color.rgb(0, 100, 0));
            for (int i = 0; i <= SQUARES_ACROSS; i++) {
                canvas.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, UNIT_SIZE * SQUARES_DOWN, paint);
            }
            //Draw Horizontal
            for (int i = 0; i <= SQUARES_DOWN; i++) {
                canvas.drawLine(0, i * UNIT_SIZE, UNIT_SIZE * SQUARES_ACROSS, i * UNIT_SIZE, paint);
            }
            paint.setStyle(Paint.Style.FILL);
            //Draw Middle circles
            for (int i = 0; i <= SQUARES_ACROSS; i++) {
                for (int j = 0; j <= SQUARES_DOWN; j++) {
                    if (((j + i) / 2) % 2 == 0) {
                        canvas.drawCircle(i * UNIT_SIZE, j * UNIT_SIZE, 3, paint);
                    } else {
                        canvas.drawCircle(i * UNIT_SIZE, j * UNIT_SIZE, 6, paint);
                    }
                }
            }
        }

        //        if (getValueAtXY(x[0], y[0]) != 0) {
//            int numbersHit = getValueAtXY(x[0], y[0]);
//            changeAtXY(new int[]{x[0], y[0]}, "E", -1 * numbersHit);
//            newNumbers(numbersHit);
//            slowerSpeed();
//        }
    }
