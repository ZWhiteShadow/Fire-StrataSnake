package com.example.stratasnake27;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;

public class SnakeActivity extends Activity {

    // Declare an instance of SnakeEngine
    SnakeEngine snakeEngine;

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event) {
        boolean handled = false;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                handled = true;
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                SnakeEngine.setDirection('U');
                handled = true;
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                SnakeEngine.setDirection('D');
                handled = true;
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                SnakeEngine.setDirection('L');
                handled = true;
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                SnakeEngine.setDirection('R');
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