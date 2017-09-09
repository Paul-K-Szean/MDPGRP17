package com.example.android.mdpgrp17_androidapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by szean on 8/9/2017.
 */

public class Arena extends View {
    private static final String TAG = "Arena";
    private int row;
    private int col;
    private int robotPosition_row;
    private int robotPosition_col;
    private int robotDirection;
    private int robotsize = 3;
    private int[][] obstacleArray = new int[20][15];
    private int[][] robotArray = new int[3][3];
    private int X;
    private int Y;
    private int _X;
    private int _Y;
    private Paint paint;
    private ArenaThread arenaThread;
    private int gridSize;
    private int[] arenaInfo = new int[300];
    private RelativeLayout arenaGrid;


    public Arena(Context context, RelativeLayout arenaGrid) {
        super(context);
        Log.d(TAG, "Arena");
        // the GUI layout of the arena grid
        this.arenaGrid = arenaGrid;
        // to paint the grid with styles
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setDither(true);
        // a thread to update the grid
        ArenaThread arenaThread = new ArenaThread(this);
        arenaThread.startThread();
    }

    @Override
    public void onDraw(Canvas canvas) {
        try {
            this.gridSize = ((arenaGrid.getMeasuredWidth()) - (arenaGrid.getMeasuredWidth() / col)) / col;
            drawArenaGrid(gridSize, canvas);
        } catch (Exception e) {
        }
    }

    private void drawArenaGrid(int gridSize, Canvas canvas) {
        int robotPosition_row_copy = robotPosition_row;
        int robotPosition_col_copy = robotPosition_col;
        int robotDirection_head_row = robotPosition_row;
        int robotDirection_head_col = robotPosition_col;
        int robotDirection_body = 3;
        boolean isUpDown = false;

        // set robot direction
        if (robotDirection == 0) { // up
            robotDirection_head_row = robotPosition_row;
            robotDirection_head_col = robotPosition_col + 1;
            robotDirection_body = robotPosition_row + 1;
            isUpDown = true;
        }
        if (robotDirection == 90) { // right
            robotDirection_head_row = robotPosition_row + 1;
            robotDirection_head_col = robotPosition_col + 2;
            robotDirection_body = robotPosition_col + 1;
        }
        if (robotDirection == 180) { // down
            robotDirection_head_row = robotPosition_row + 2;
            robotDirection_head_col = robotPosition_col + 1;
            robotDirection_body = robotPosition_row + 1;
            isUpDown = true;
        }
        if (robotDirection == 270) { // left
            robotDirection_head_row = robotPosition_row + 1;
            robotDirection_head_col = robotPosition_col;
            robotDirection_body = robotPosition_col + 1;
        }

        for (int rowIndex = 1; rowIndex <= row; rowIndex++) {
            for (int colIndex = 1; colIndex <= col; colIndex++) {
                // draw grid
                drawCell(rowIndex, colIndex, gridSize, Color.parseColor("#295398"), canvas);  // blue

                // draw robot
                if (rowIndex == robotPosition_row_copy && colIndex == robotPosition_col_copy) {
                    if (robotsize > 0) {
                        for (int colIndexCount = robotPosition_col_copy; colIndexCount <= robotPosition_col_copy + 2; colIndexCount++) {
                            drawCell(robotPosition_row_copy, colIndexCount, gridSize, Color.parseColor("#ffa500"), canvas); // light orange

                            // draw head
                            if (rowIndex == robotDirection_head_row && colIndexCount == robotDirection_head_col) {
                                drawCell(robotDirection_head_row, robotDirection_head_col, gridSize, Color.parseColor("#954bcc"), canvas); // teal
                            }
                            // draw body
                            if (isUpDown) {
                                if (rowIndex == robotDirection_body) {
                                    if (colIndexCount != robotDirection_head_col) // remove center of body
                                        drawCell(robotDirection_body, colIndexCount, gridSize, Color.parseColor("#4b0082"), canvas); // chocolate
                                }
                            } else {
                                if (rowIndex == robotPosition_row_copy && colIndexCount == robotDirection_body) {
                                    if (rowIndex != robotDirection_head_row) // remove center of body
                                        drawCell(robotPosition_row_copy, colIndexCount, gridSize, Color.parseColor("#4b0082"), canvas); // chocolate
                                }
                            }
                        }
                        colIndex += 2; // skip col to draw
                        robotPosition_row_copy++;    // to draw next row for
                        robotsize--;
                    } else {
                        // reset
                        robotPosition_row_copy = robotPosition_row;
                        robotPosition_col_copy = robotPosition_col;
                        robotsize = 3;
                    }
                }


                // draw obstacle
                if (Integer.valueOf(obstacleArray[rowIndex - 1][colIndex - 1]) == 1) {
                    drawCell(rowIndex, colIndex, gridSize, Color.parseColor("#e33054"), canvas); // red
                }
            }
        }

    }

    public void drawCell(int row, int col, int gridSize, int c, Canvas canvas) {
        Y = row * gridSize - gridSize / 2;
        X = col * gridSize - gridSize / 2;
        _Y = row * gridSize + gridSize / 2;
        _X = col * gridSize + gridSize / 2;

        // paint the fill in color
        paint.setColor(c);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
        // paint the stroke border
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
    }

    public void update() {
        // called every period (200milisec) because arena thread is running

    }

    public void decodeArenaInfo(String cmdString) {
        String[] splitArenaInfoArray = cmdString.split(" ");
        row = Integer.valueOf(splitArenaInfoArray[1]);
        col = Integer.valueOf(splitArenaInfoArray[2]);
        robotPosition_col = Integer.valueOf(splitArenaInfoArray[3]);
        robotPosition_row = Integer.valueOf(splitArenaInfoArray[4]);
        robotDirection = Integer.valueOf(splitArenaInfoArray[5]);

        for (int count = 0; count < splitArenaInfoArray.length - 6; count++) {
            arenaInfo[count] = Integer.valueOf(splitArenaInfoArray[count + 6]);
        }

        for (int rowIndex = 1; rowIndex <= row; rowIndex++) {
            for (int colIndex = 1; colIndex <= col; colIndex++) {
                obstacleArray[rowIndex - 1][colIndex - 1] = arenaInfo[((colIndex - 1) + (rowIndex - 1) * 15)];
            }
        }
    }

}
