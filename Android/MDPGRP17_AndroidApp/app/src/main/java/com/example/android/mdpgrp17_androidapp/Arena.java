package com.example.android.mdpgrp17_androidapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_OBSTACLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOTDIRECTION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOTPOSITION;

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
    private int[][] arenaInfo = new int[20][15];
    private int X;
    private int Y;
    private int _X;
    private int _Y;
    private Paint paint;
    private ArenaThread arenaThread;
    private int gridSize;
    private int[] arenaInfoString = new int[300];
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

        for (int rowIndex = 1; rowIndex <= row; rowIndex++) {
            for (int colIndex = 1; colIndex <= col; colIndex++) {
                // draw grid
                if (Integer.valueOf(arenaInfo[rowIndex - 1][colIndex - 1]) == ARENA_GRID) {
                    drawCell(rowIndex, colIndex, gridSize, Color.parseColor("#295398"), canvas);  // blue
                }

                // draw robot position
                if (Integer.valueOf(arenaInfo[rowIndex - 1][colIndex - 1]) == ARENA_ROBOTPOSITION) {
                    drawCell(rowIndex, colIndex, gridSize, Color.parseColor("#FF8C00"), canvas); // dark orange
                }
                // draw robot direction
                if (Integer.valueOf(arenaInfo[rowIndex - 1][colIndex - 1]) == ARENA_ROBOTDIRECTION) {
                    drawCell(rowIndex, colIndex, gridSize, Color.parseColor("#9932CC"), canvas); // dark orchid
                }
                // draw obstacle
                if (Integer.valueOf(arenaInfo[rowIndex - 1][colIndex - 1]) == ARENA_OBSTACLE) {
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
            arenaInfoString[count] = Integer.valueOf(splitArenaInfoArray[count + 6]);
        }

        for (int rowIndex = 1; rowIndex <= row; rowIndex++) {
            for (int colIndex = 1; colIndex <= col; colIndex++) {
                // set obstacles and the grid
                arenaInfo[rowIndex - 1][colIndex - 1] = arenaInfoString[((colIndex - 1) + (rowIndex - 1) * 15)];
            }
        }
        for (int rowIndex = robotPosition_row; rowIndex <= robotPosition_row + 2; rowIndex++) {
            for (int colIndex = robotPosition_col; colIndex <= robotPosition_col + 2; colIndex++) {
                // set robot position
                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOTPOSITION;
            }
        }

        // set robot direction
        robotsize = 3;

        int robotDirection_head_row;
        int robotDirection_head_col;
        int robotDirection_body = 3;
        boolean isUpDown = false;

        // set robot direction head
        if (robotDirection == 0) { // up
            robotDirection_head_row = robotPosition_row;
            robotDirection_head_col = robotPosition_col + 1;
            robotDirection_body = robotPosition_row + 1;
            arenaInfo[robotDirection_head_row - 1][robotDirection_head_col - 1] = ARENA_ROBOTDIRECTION;
            isUpDown = true;
        }
        if (robotDirection == 90) { // right
            robotDirection_head_row = robotPosition_row + 1;
            robotDirection_head_col = robotPosition_col + 2;
            robotDirection_body = robotPosition_col + 1;
            arenaInfo[robotDirection_head_row - 1][robotDirection_head_col - 1] = ARENA_ROBOTDIRECTION;
        }
        if (robotDirection == 180) { // down
            robotDirection_head_row = robotPosition_row + 2;
            robotDirection_head_col = robotPosition_col + 1;
            robotDirection_body = robotPosition_row + 1;
            arenaInfo[robotDirection_head_row - 1][robotDirection_head_col - 1] = ARENA_ROBOTDIRECTION;
            isUpDown = true;
        }
        if (robotDirection == 270) { // left
            robotDirection_head_row = robotPosition_row + 1;
            robotDirection_head_col = robotPosition_col;
            robotDirection_body = robotPosition_col + 1;
            arenaInfo[robotDirection_head_row - 1][robotDirection_head_col - 1] = ARENA_ROBOTDIRECTION; // head
        }
        // set robot direction body
        if (isUpDown) {
            for (int colIndexCount = robotPosition_col; colIndexCount <= robotPosition_col + 2; colIndexCount++) {
                arenaInfo[robotDirection_body - 1][colIndexCount - 1] = ARENA_ROBOTDIRECTION; // body
            }
        } else {
            for (int rowIndexCount = robotPosition_row; rowIndexCount <= robotPosition_row + 2; rowIndexCount++) {
                arenaInfo[rowIndexCount - 1][robotDirection_body - 1] = ARENA_ROBOTDIRECTION; // body
            }
        }

    }

}
