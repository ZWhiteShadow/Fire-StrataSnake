package com.example.stratasnake27;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;

public class SnakeActivity extends Activity {

    // Declare an instance of SnakeEngine
    static SnakeEngine snakeEngine;

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event) {
        boolean handled = false;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                handled = true;
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (SnakeEngine.getDirection() == 'U') {
                    SnakeEngine.fasterSpeed();
                }
                if (SnakeEngine.getDirection() == 'D') {
                    SnakeEngine.slowerSpeed();
                }
                SnakeEngine.setDirection('U');
                handled = true;
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (SnakeEngine.getDirection() == 'D') {
                    SnakeEngine.fasterSpeed();
                }
                if (SnakeEngine.getDirection() == 'U') {
                    SnakeEngine.slowerSpeed();
                }
                SnakeEngine.setDirection('D');
                handled = true;
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (SnakeEngine.getDirection() == 'L') {
                    SnakeEngine.fasterSpeed();
                }
                if (SnakeEngine.getDirection() == 'R') {
                    SnakeEngine.slowerSpeed();
                }
                SnakeEngine.setDirection('L');
                handled = true;
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (SnakeEngine.getDirection() == 'R') {
                    SnakeEngine.fasterSpeed();
                }
                if (SnakeEngine.getDirection() == 'L') {
                    SnakeEngine.slowerSpeed();
                }
                SnakeEngine.setDirection('R');
                handled = true;
                break;

            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (SnakeEngine.getIsPlaying()) {
                    snakeEngine.pause();
                }else {
                    snakeEngine.resume();
                }
                handled = true;
                break;

            case KeyEvent.KEYCODE_MENU:
                SnakeEngine.newGame();
                handled = true;
                break;

        }
        return handled || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);

        // Create a new instance of the SnakeEngine class
        snakeEngine = new SnakeEngine(this, size);

        // Make snakeEngine the view of the Activity
        setContentView(snakeEngine);

    }

    // Start the thread in snakeEngine
    @Override
    protected void onResume() {
        super.onResume();
        snakeEngine.resume();
    }

    // Stop the thread in snakeEngine
    @Override
    protected void onPause() {
        super.onPause();
        snakeEngine.pause();
    }

}