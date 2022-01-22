package com.example.stratasnake27;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    LinearLayout linlay;
    ImageView imageview;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;

    int SCREEN_WIDTH, SCREEN_HEIGHT;
    int SQUARES_ACROSS, SQUARES_DOWN;
    int UNIT_SIZE, GAME_UNITS, TOTAL_SQUARES;
    float sneggyBodyParts;
    int[] x, y; // x and y coordinates of sneggy

    public void drawGrid() {

        paint.setColor(Color.rgb(0, 150, 0));
        //draw Vertical Lines on board
        for (int i = 0; i <= SQUARES_ACROSS ; i++) {
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
                    canvas.drawCircle(i * UNIT_SIZE , j * UNIT_SIZE, 6, paint);
                }
            }
        }
    }

//    public void drawSneggy() {
//        if (sneggyBodyParts > 1) {
//            int colorShift = 255 / (int) sneggyBodyParts;
//            for (int i = 0; i < sneggyBodyParts; i++) {
//                paint.setColor(Color.rgb(255 - (colorShift * i), 0, 255 - (colorShift * i)));
//                paint.setStyle(Paint.Style.FILL);
//                canvas.drawRect(x[i] * UNIT_SIZE, y[i] * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE, paint);
//            }
//        }
//    }
//
//    public void startGame() {
//    sneggyBodyParts = 11;
//    imageview.invalidate();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Base Square Model for single square
        // https://www.youtube.com/watch?v=r_P9K945vEk //
        // Android Studio Drawing Squares
        
        SCREEN_WIDTH = metrics.widthPixels;
        UNIT_SIZE = SCREEN_WIDTH / 54;
        SCREEN_HEIGHT = metrics.heightPixels;
        GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
        SQUARES_DOWN = (SCREEN_HEIGHT / UNIT_SIZE) - 3;
        SQUARES_ACROSS =(SCREEN_WIDTH / UNIT_SIZE);
        TOTAL_SQUARES = SQUARES_ACROSS * SQUARES_DOWN;

        linlay = new LinearLayout(this);
        imageview = new ImageView(this);
        imageview.setLayoutParams(new LinearLayout.LayoutParams(SCREEN_WIDTH,SCREEN_HEIGHT));
        linlay.addView(imageview);
        bitmap = Bitmap.createBitmap(SCREEN_WIDTH,SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
        imageview.setImageBitmap(bitmap);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        drawGrid();
//        startGame();
//        drawSneggy();
        linlay.setGravity(Gravity.CENTER_HORIZONTAL);
        setContentView(linlay);
    }
}