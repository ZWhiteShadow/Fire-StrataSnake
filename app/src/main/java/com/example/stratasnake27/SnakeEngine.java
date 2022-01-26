package com.example.stratasnake27;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class SnakeEngine extends SurfaceView implements Runnable {

    ArrayList<SneggyBoard> squares; // list holding board dimensions and what squares are where
    static final int SIDE_FOR_SCORE = 320;
    static final int BOTTOM_PANEL = 125;
    static int SCREEN_WIDTH = 1200;
    static int SCREEN_HEIGHT = 600;
    static int UNIT_SIZE = 25;
    static int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static int SQUARES_ACROSS;
    static int SQUARES_DOWN;
    static double TOTAL_SQUARES;
    static double sneggySpeed; //How fast game is running
    // For tracking movement Heading
    static char direction = 'D';
    // Some paint for our canvas
    public final Paint paint = new Paint();
    // Required to use canvas
    private final SurfaceHolder surfaceHolder;
    int[] x; // x coordinates of sneggy
    int[] y; // y coordinates of sneggy
    float scoreMultiplierFloat; //score bonus
    int score;
    int lastScore;
    float sneggyBodyParts;
    float numbersLeft; // units left before entering next level
    float displayLevel; // level user is on including part level 1.3 ect
    float level; // only whole level user is on level 1, 2, 3 ect
    boolean running; // is the game running
    boolean gameStarted; // has the first level started - first number one is hit
    Random random = new Random();
    DecimalFormat withCommas = new DecimalFormat("#,###");
    DecimalFormat decimal = new DecimalFormat("#,###.##");
    int difficulty; // used for bonus - based on number of negatives on board vs amount normal for level
    boolean waitingForNextLevel; // is the white level number on board - are we waiting to hit it
    HighScoresList[] highScoresArray = new HighScoresList[27];
    int gameHighScore;
    float skip;
    int levelChange;
    int attempt = 0;
    int displayPerUnit; // shown number received for hitting positive units
    double danger; // how much of the board is full
    Bitmap bitmap;
    Canvas canvas;
    // Our game thread for the main game loop
    private Thread thread = null;
    // Control pausing between updates
    private long nextFrameTime;
    // Everything we need for drawing
// Is the game currently playing?
    private volatile boolean isPlaying;
    String text = "AAA";
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

        // Initialize the drawing objects
        surfaceHolder = getHolder();

        // Start the game
        newGame();
    }

    public static char getDirection(){
        return direction;
    }

    public static void setDirection(char newDirection){
        direction = newDirection;
    }

    public static void slowerSpeed() {
        if (sneggySpeed < 250) {
            sneggySpeed *= 1.1;
        }
    }

    public static void fasterSpeed() {
        if (sneggySpeed > 25) {
            sneggySpeed /= 1.05;
        }
    }

    @Override
    public void run() {

        while (isPlaying) {

            // Update 10 times a second
            if (updateRequired()) {
                moveSnake();
                draw();
            }

        }
    }

    public int[] getRandomFilteredXY(String filter) {  //get Random XY of specific type
        int[] xy = new int[2];
        // create new list with only FILTER types
        ArrayList<SneggyBoard> result = new ArrayList<>();
        for (int i = 0; i < squares.size(); i++){
            if (squares.get(i).type.equals(filter)){
                result.add(squares.get(i));
            }
        }
        int randomLocation = random.nextInt(result.size()); //Find item on shorter list
        xy[0] = result.get(randomLocation).getX(); // find x value
        xy[1] = result.get(randomLocation).getY();// Find y value
        return xy;
    }

    public void changeAtXY(int[] xy, String newType, int valueChange) {
        int oldValue = getValueAtXY(xy[0], xy[1]); // find current value
        squares.set((xy[1] * 48) + xy[0], //index instead of x y
                new SneggyBoard(newType, oldValue + valueChange, new int[]{xy[0], xy[1]})); //replace with new object
    }

    public int countType(String countType) { //count number of a type
        //  "E" Empty (0) // "G" Good (positive 1-99)
        // "HR" Hollow Red (300, 600, 900 ect) // "SR" Solid Red (negative 1-99)
        // "HY" Hollow Yellow (101-199) // "SY" Solid Yellow (-100, -200, -300 ect)
        //count number of certain type
        ArrayList<SneggyBoard> result = new ArrayList<>();
        for (int i = 0; i < squares.size(); i++){
            if (squares.get(i).type.equals(countType)){
                result.add (squares.get(i));
            }
        }
        return result.size(); //get number based on size of list
    }

    public int getValueAtXY(int x, int y) { //get value on x y
        return squares.get((y * SQUARES_ACROSS) + x).value;
    }

    public int countNumberAtMax(String type, int changeValue) { //count number of a type
        int squareValue;
        int numberAtMax = 0;
        for (SneggyBoard square : squares) {
            //account for different value system
            squareValue = (type.equals("HY")) ? square.value - 100 : square.value / changeValue;
            if (squareValue == 99) // equals max amount
                numberAtMax++; //increase count by one
        }
        return numberAtMax;
    }

    public void changeSquare(String typeToChange, int baseValue, int changeValue, int numToChange, boolean isNew, boolean add) {
        int squareValue;
        int[] tempValue;
        for (int i = 0; i < numToChange; i++) {
            if (isNew) {  // should it be added to a new square
                changeAtXY(getRandomFilteredXY("E"), typeToChange, baseValue); // add to empty square
            } else {
                if (add) {  // add to existing one
                    if (countType(typeToChange) == countNumberAtMax(typeToChange, changeValue)) { //if only numbers left are 99
                        changeAtXY(getRandomFilteredXY("E"), typeToChange, baseValue); // add to empty square
                    } else {
                        do {
                            tempValue = getRandomFilteredXY(typeToChange); // get xy
                            squareValue = (typeToChange.equals("HY")) ? getValueAtXY(tempValue[0], tempValue[1]) - 100 :
                                    getValueAtXY(tempValue[0], tempValue[1]) / changeValue; //find value of square
                        } while (squareValue >= 99); // find one that is not 99
                        changeAtXY(tempValue, typeToChange, changeValue); // add to it
                    }
                } else {  // remove from existing
                    tempValue = getRandomFilteredXY(typeToChange); // get random xy
                    if (getValueAtXY(tempValue[0], tempValue[1]) == baseValue) {
                        changeAtXY(tempValue, "E", -1 * baseValue); // if value == 1 remove square
                    } else {
                        changeAtXY(tempValue, typeToChange, -1 * changeValue); // if higher reduce
                    }
                }
            }
        }
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
        squares = new ArrayList<>();
        for (int j = 0; j < SQUARES_DOWN; j++) {  //Set up board values as Good Or Safe spaces with 0 values
            for (int i = 0; i < SQUARES_ACROSS; i++) {
                squares.add(new SneggyBoard("E", 0, new int[]{i, j})); //E for empty
            }
        }

        //highScoresArray = HighScoreReader.ReadHighScore();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Arrays.sort(highScoresArray, Comparator.comparingInt(HighScoresList::getScore));
//        }
        skip = 0;
        levelChange = 0;
        attempt += 1;
        difficulty = 0;
        direction = 'D';
        sneggyBodyParts = 11;
        sneggySpeed = 125;
        score = 0;
        lastScore = 0;
        gameHighScore = 0;
        running = true;
        scoreMultiplierFloat = 100.0f;
        x = new int[GAME_UNITS];
        y = new int[GAME_UNITS];
        level = 1;
        gameStarted = false;
        numbersLeft = level;
        changeAtXY(getRandomFilteredXY("E"), "G", (int) numbersLeft); //change first square to be white level number
        waitingForNextLevel = true;
        // Start with a single snake segment
        x[0] = SQUARES_ACROSS / 2;
        y[0] = SQUARES_DOWN / 2;

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis();
    }

    private void moveSnake() {
        // Move the body
        for (int i = (int) sneggyBodyParts; i > 0; i--) {
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

        if (getValueAtXY(x[0], y[0]) != 0) {
            int numbersHit = getValueAtXY(x[0], y[0]);
            changeAtXY(new int[]{x[0], y[0]}, "E", -1 * numbersHit);
            newNumbers(numbersHit);
            slowerSpeed();
        }
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
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.rgb(255 - (colorShift * i), 0, 255 - (colorShift * i)));
                canvas.drawRect(x[i] * UNIT_SIZE,
                        (y[i] * UNIT_SIZE),
                        (x[i] * UNIT_SIZE) + UNIT_SIZE,
                        (y[i] * UNIT_SIZE) + UNIT_SIZE,
                        paint);
            }

            drawGrid(canvas);
            drawNumbers(canvas);

//            // Scale the HUD text
//            paint.setTextSize(90);
//            paint.setColor(Color.WHITE);
//            canvas.drawText("Score: " + score, 10, 70, paint);

            // Set the color of the paint to draw Bob red
            paint.setColor(Color.argb(255, 255, 0, 0));

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
            long FPS = 100;
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

//    public void paintComponent(g) {
//        super.paintComponent(g);
//        drawNumbers(g); //colored squares and symbols
//        drawSneggy(g); //snake
//        if (level == 99) {
//            running = false;
//            gameOver(g, "Sneggy Won!", Color.red.brighter()); //display win message
//        } else if (!running || sneggyBodyParts <= 1 || score < 0 ||
//                (countType("E") == SQUARES_DOWN * SQUARES_ACROSS)) {
//            running = false; // stop program
//            gameOver(g, "Sneggy Died!", Color.red.brighter()); //display game message
//        }
//        drawBottomPanel(g);
//        drawHighScore(g);
//        nameField(g);
//    }

//    public void nameField(Graphics g) {
//        g.setFont(new Font("Terminal", Font.PLAIN, 20));
//        g.setColor(Color.white);
//        g.drawRect(SCREEN_WIDTH + 134, 6, 111, 26);
//        g.setColor(new Color(0, 50, 0));
//        g.fillRect(r.x, r.y, r.width, r.height);
//        g.setColor(Color.white);
//        // You can play with this code to center the text
//        g.drawString(text, r.x + 5, r.y - 7 + r.height);
//    }

    public void drawRectangle(int left, int top, int right, int bottom, Canvas canvas, Paint paint) {
        right = left + right; // width is the distance from left to right
        bottom = top + bottom; // height is the distance from top to bottom
        canvas.drawRect(left, top, right, bottom, paint);
    }

    public void drawRec(Canvas canvas, int x, int y, int value, boolean fill) {
        //Create Squared
        if (fill) { //Solid
             paint.setStyle(Paint.Style.FILL);
             drawRectangle(x * UNIT_SIZE + 2,y * UNIT_SIZE + 2,UNIT_SIZE - 2, UNIT_SIZE - 2, canvas, paint);
             paint.setColor(Color.BLACK);
        } else { // Hollow
            paint.setStyle(Paint.Style.STROKE);
            drawRectangle(x * UNIT_SIZE + 2,y * UNIT_SIZE + 2,UNIT_SIZE - 2, UNIT_SIZE - 2, canvas, paint);
        }
        if (value > 9) { //Larger than 9
            canvas.drawText(String.valueOf(value), (x * UNIT_SIZE) + 4, (y * UNIT_SIZE) + UNIT_SIZE - 6, paint);
        } else if ((value > 1) || (value == 1 && !gameStarted)) { // 2-9
            paint.setTextSize(20);
            canvas.drawText(String.valueOf(value), (x * UNIT_SIZE) + 7, (y * UNIT_SIZE) + UNIT_SIZE - 6, paint);
            paint.setTextSize(16);
        }
    }

    //set numbers
    public void drawNumbers(Canvas canvas) {
        for (int i = 0; i < squares.size(); i++) {
                paint.setTextSize(16);
                int squareValue = squares.get(i).value;
                int x = squares.get(i).getX();
                int y = squares.get(i).getY();
                //next level white square with number levels 1-99
                paint.setColor(Color.WHITE);
                if ((((squareValue == level) && (gameStarted) && level != 1) || ((squareValue == level) && (level == 1) && (!gameStarted)))) {
                    paint.setColor(Color.WHITE);
                    drawRec(canvas, x, y, squareValue, false);
                }

                //Yellow Squares
                // hollow square
                else if (squareValue > 100 && squareValue < 200) { //hollow yellow 101-199
                    paint.setColor(Color.YELLOW);
                    drawRec(canvas, x, y, squareValue - 100, false);
                }
                // crossbones
                else if (((int) sneggyBodyParts - ((squareValue / -100f) * ((displayPerUnit * level)) / 1000f) <= 1) && (squareValue <= -100)) { //will kill user
                    paint.setColor(Color.YELLOW);
                    paint.setTextSize(UNIT_SIZE);
                    canvas.drawText("☠", (x * UNIT_SIZE), (y * UNIT_SIZE) + UNIT_SIZE - 2, paint); // skull
                    paint.setTextSize(16);
                }
                // solid
                else if (squareValue <= -100) { // more than 1 yellow
                    paint.setColor(Color.YELLOW);
                    drawRec(canvas, x, y, squareValue / -100, true);
                }

                // Red Squares
                // Hollow
                else if (squareValue >= 300) { // more than one hollow red
                    paint.setColor(Color.RED);
                    drawRec(canvas, x, y, squareValue / 300, false);
                }
                // Crossbones
                else if ((squareValue <= -1) && (score + (displayPerUnit * level * squareValue) < 0)) {
                    paint.setTextSize(UNIT_SIZE);
                    paint.setColor(Color.RED);
                    canvas.drawText("☠", (x * UNIT_SIZE), (y * UNIT_SIZE) + UNIT_SIZE - 2, paint); // skull
                    paint.setTextSize(16);
                }
                // Solid
                else if (squareValue <= -1) {  // solid red
                    paint.setColor(Color.RED);
                    drawRec(canvas, x, y, squareValue / -1, true);
                }

                //Green Squares
                //Smiley face circle
                else if (squareValue == 1) { //positive one1
                    paint.setColor(Color.GREEN);
                    paint.setTextSize(UNIT_SIZE);
                    canvas.drawText("\u263A", (x * UNIT_SIZE), (y * UNIT_SIZE) + UNIT_SIZE - 3, paint);
                    paint.setTextSize(16);
                }
                // Hollow
                else if (squareValue > 0) { //more than one green
                    paint.setColor(Color.GREEN);
                    drawRec(canvas, x, y, squareValue, false);
                }
        }

        if (level == 1) {
            difficulty = 100;
        } else {
            if (waitingForNextLevel) {
                difficulty = (int) (((countType("HR") + countType("SR") +
                        countType("HY") + countType("SY")) / (((level - 1) * 4))) * 100);
            } else {
                difficulty = (int) (((countType("HY") + countType("SY") +
                        countType("HR") + countType("SR")) / (level * 4)) * 100);
            }
        }
    }


//    public void gameOver(Graphics g, String message, Color color) {
//        //show GAME OVER
//        g.setColor(Color.black);
//        g.fillRect(15 * UNIT_SIZE, 10 * UNIT_SIZE, 18 * UNIT_SIZE, 3 * UNIT_SIZE);
//        g.setColor(color);
//        g.setFont(new Font("Terminal", Font.PLAIN, UNIT_SIZE * 3));
//        FontMetrics metrics = getFontMetrics(g.getFont());
//        g.drawString(message, (SCREEN_WIDTH - metrics.stringWidth(message)) / 2,
//                SCREEN_HEIGHT / 2);
//    }


//    //set side for high score table
//    public void drawHighScore(Graphics g) {
//        g.setColor(Color.darkGray.darker().darker());
//        g.fillRect(SCREEN_WIDTH, 0, SCREEN_WIDTH + SIDE_FOR_SCORE, SCREEN_HEIGHT);
//        for (int i = 0; i <= SQUARES_ACROSS; i++) {
//            if (i % 2 == 0) {
//                g.setColor(new Color(0, 50, 0));
//            } else {
//                g.setColor(new Color(50, 0, 50));
//            }
//            g.fillRect(SCREEN_WIDTH, (24 * i) + 61, SIDE_FOR_SCORE, 20);
//        }
//        g.setColor(Color.white);
//        g.setFont(new Font("Terminal", Font.PLAIN, 18));
//        int y = UNIT_SIZE;
//        g.drawString("High Score:", SCREEN_WIDTH + 25, y);
//
//        g.drawString("Initials:", SCREEN_WIDTH + 15 + UNIT_SIZE, y + 30);
//        g.drawString("Score:", SCREEN_WIDTH + (UNIT_SIZE * 5), y + 30);
//        g.drawString("Level:", SCREEN_WIDTH + (UNIT_SIZE * 10), y + 30);
//        int tempRank = 0;
//        for (int i = highScoresArray.length - 1; i >= 0; i--) {
//            tempRank++;
//            y += g.getFontMetrics().getHeight(); //go down one row with each new score
//            g.drawString(String.valueOf(tempRank), SCREEN_WIDTH + 5, y + 30);
//            if (highScoresArray[i].getScore() != 0) {
//                g.drawString(highScoresArray[i].getInitials(), SCREEN_WIDTH + 15 + UNIT_SIZE, y + 30);
//                g.drawString(withCommas.format(highScoresArray[i].getScore()), SCREEN_WIDTH + (UNIT_SIZE * 5), y + 30);
//                g.drawString(decimal.format(highScoresArray[i].getLevel()), SCREEN_WIDTH + (UNIT_SIZE * 10), y + 30);
//            }
//        }
//    }

//    //fill in bottom
//    public void drawBottomPanel(Graphics g) {
//
//        //draw panel
//        g.setColor(Color.darkGray.darker().darker());
//        g.fillRect(0, SCREEN_HEIGHT + 8, SCREEN_WIDTH + SIDE_FOR_SCORE, BOTTOM_PANEL);
//
//        // formatting
//
//        displayPerUnit = (int) ((difficulty > 100) ? (int) (scoreMultiplierFloat * (difficulty / 100f)) * (float) (125 / sneggySpeed) : (int) scoreMultiplierFloat * (float) (125 / sneggySpeed));
//        displayLevel = level + ((level - numbersLeft) / level);
//        int notYetNextLevel = 0;
//        if (waitingForNextLevel) {
//            notYetNextLevel = -1;
//        }
//        danger = (((TOTAL_SQUARES - (countType("E"))) / TOTAL_SQUARES) / 2d); // update how many squares on board
//
//        // Game Name
//        g.setColor(Color.magenta.brighter());
//        g.setFont(new Font("Terminal", Font.PLAIN, 22));
//        g.drawString("Introducing \"Sneggy\" In:", 60, SCREEN_HEIGHT + (BOTTOM_PANEL / 2) - 15);
//        g.setFont(new Font("Terminal", Font.PLAIN, (int) (UNIT_SIZE * 1.5)));
//        g.drawString("StrataSnake \uD83D\uDC0D", 50, SCREEN_HEIGHT + (BOTTOM_PANEL / 2) + 25);
//
//        //Game Instructions and stats:
//
//        g.setFont(new Font("Terminal", Font.PLAIN, 20));
//        g.setColor(Color.white);
//        g.drawString("Score: " + withCommas.format(score), 450, SCREEN_HEIGHT + 35);
//        g.setColor(Color.green);
//        g.drawString("\u263A \u25A1 +" + withCommas.format(displayPerUnit), 450, SCREEN_HEIGHT + 60);
//        g.setColor(Color.red);
//        g.drawString("\u25A0 " + withCommas.format(displayPerUnit * level * -1), 450, SCREEN_HEIGHT + 85);
//        g.drawString("\u2620 Death", 450, SCREEN_HEIGHT + 110);
//
//        g.setColor(Color.white);
//        g.drawString("Length: " + decimal.format(sneggyBodyParts - 1) , 700, SCREEN_HEIGHT + 35);
//        g.setColor(Color.yellow);
//        g.drawString("\u25A1  + "+ decimal.format(displayPerUnit / 1000f), 700, SCREEN_HEIGHT + 60);
//        g.drawString("\u25A0  - "+ decimal.format(((displayPerUnit * level) / 1000f)), 700, SCREEN_HEIGHT + 85);
//        g.drawString("\u2620 Death", 700, SCREEN_HEIGHT + 110);
//
//        g.setColor(Color.white);
//        g.drawString("Level: " + decimal.format(level ) , 950, SCREEN_HEIGHT + 35);
//        g.setColor(Color.green);
//        if (waitingForNextLevel) {
//            g.drawString("\u263A + 0%", 950, SCREEN_HEIGHT + 60);
//        } else {
//            g.drawString("\u263A + " + decimal.format((1 / (level + notYetNextLevel)) * 100) + "%", 950, SCREEN_HEIGHT + 60);
//        }
//        g.setColor(Color.red);
//        g.drawString("\u25A1 + " + decimal.format(danger * 100) + "%", 950, SCREEN_HEIGHT + 85);
//
//    }

    public void newNumbers(int numberHit) {
        if ((numberHit == level && level > 1) || (level == 1 && !gameStarted)) { //level hit
            waitingForNextLevel = false;
            for (int i = 0; i < level; i++) { // THREE extra of each per level
                changeSquare("HY", 101, 1, 1, true, true);
                changeSquare("SY", -100, -100, 1, true, true);
                changeSquare("HR", 300, 300, 1, true, true);
                changeSquare("SR", -1, -1, 1, true, true);
            }
        }

        if (numberHit > 100 && numberHit < 200) { // hollow yellow is hit HY 101-199
            sneggyBodyParts += displayPerUnit / 1000f;
            if ((countType("HY") == 0) || (numberHit - 100 == 99)) {
                changeSquare("SY", -100, -100, numberHit - 100, false, false);
            } else {
                changeSquare("HY", 101, 1, (numberHit - 100) + 1, false, true);
                changeSquare("SY", -100, -100, 1, false, true);
            }
            return;
        }

        if (numberHit / -100 > 0) { //solid yellow is hit -100 multiples SY
            sneggyBodyParts -= ((displayPerUnit * level) / 1000f) * (numberHit / -100f); //loss of body parts based on speed.
            if ((countType("SY") == 0) || (numberHit / -100 == 99)) {
                changeSquare("HY", 101, 1, numberHit / -100, false, false);
            } else {
                changeSquare("HY", 101, 1, 1, false, true);
                changeSquare("SY", -100, -100, 2, true, true);
                changeSquare("SY", -100, -100, (numberHit / -100), false, true);
            }
            return;
        }

        if (numberHit >= 300) { // hollow red is hit 300 multiples HR
            skip += (danger * (numberHit / 300f));
            if (skip >= 1) {
                levelChange += (int) skip;
                skip -= (int) skip;
            }
            if ((countType("HR") == 0) || (numberHit / 300 == 99)) {
                changeSquare("SR", -1, -1, numberHit / 300, false, false);
            } else {
                changeSquare("HR", 300, 300, (numberHit / 300) + 1, false, true);
                changeSquare("SR", -1, -1, 1, false, true);
            }
            return;
        }

        if (numberHit < 0) {  //solid red is hit -1 to -99 SR
            if ((countType("SR") == 0) || (numberHit * -1 == 99)) {
                changeSquare("HR", 300, 300, numberHit * -1, false, false);
            } else {
                changeSquare("HR", 300, 300, 1, false, true);
                changeSquare("SR", -1, -1, 2, true, true);
                changeSquare("SR", -1, -1, (numberHit * -1), false, true);
            }
        }

        if (numberHit == 1) {
            if (level == 1 && !gameStarted) {
                changeSquare("G", 1, 1, 1, true, true);
                gameStarted = true;
                waitingForNextLevel = false;
                numbersLeft++;
            }
            numbersLeft--;
            // all greens removed on current level
            if (numbersLeft == 0) {
                level++;
                if (levelChange > 0) {
                    level = level + levelChange;
                    levelChange = 0;
                }
                if (level <= 0) {
                    running = false;
                }

                numbersLeft = level;
                changeAtXY(getRandomFilteredXY("E"), "G", (int) numbersLeft);
                waitingForNextLevel = true;
            }
        }

        if (numberHit > 1) {

            int tempNumberToDisplay = numberHit;
            int numberToSplit = tempNumberToDisplay / 2;
            for (int i = 0; i < 2; i++) {
                changeSquare("G", numberToSplit, numberToSplit, 1, true, true);
                tempNumberToDisplay = tempNumberToDisplay - numberToSplit;
            }
            changeSquare("G", tempNumberToDisplay, tempNumberToDisplay, 1, true, true);
        }

        if ((numberHit != level) || (level == 1 && !gameStarted)) {
            if (numberHit < 0) {
                score += (displayPerUnit * level * -1);
            } else if ((numberHit != level) || (level == 1 && gameStarted)) {
                score += displayPerUnit;
            }
        }

        if (score > gameHighScore) {
            gameHighScore = score;
        }

        if (score !=  lastScore) {
            if (highScoresArray[0].getScore() < score) {
                highScoresArray[0].setInitials(text);
                highScoresArray[0].setScore(score);
                highScoresArray[0].setLevel(displayLevel);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Arrays.sort(highScoresArray, Comparator.comparingInt(HighScoresList::getScore));
            }
            HighScoreSaver.SaveHighScore(highScoresArray);
            lastScore = score;
        }
        displayLevel = level + ((level - numbersLeft) / level);
    }
}
