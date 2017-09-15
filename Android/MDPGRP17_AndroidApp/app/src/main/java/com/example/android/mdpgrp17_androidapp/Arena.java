package com.example.android.mdpgrp17_androidapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
<<<<<<< HEAD
import android.widget.Toast;

=======
import android.view.MotionEvent;
>>>>>>> 32c20946a528bdb4d1e034b95a03a1fecda63550
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_END;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_START;
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
<<<<<<< HEAD

=======
    private Canvas canvas;
    private MotionEvent simulationEvent;
>>>>>>> 32c20946a528bdb4d1e034b95a03a1fecda63550
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
        this.arenaThread = new ArenaThread(this);
        arenaThread.startThread();
    }

    @Override
    public void onDraw(Canvas canvas) {
        try {
            drawArenaGrid(canvas);
        } catch (Exception e) {
        }
    }

    private void drawArenaGrid(Canvas canvas) {
        this.gridSize = ((arenaGrid.getMeasuredWidth()) - (arenaGrid.getMeasuredWidth() / col)) / col;
        for (int rowIndex = 1; rowIndex <= row; rowIndex++) {
            for (int colIndex = 1; colIndex <= col; colIndex++) {
                // draw grid
                if (Integer.valueOf(arenaInfo[rowIndex - 1][colIndex - 1]) == ARENA_GRID) {
                    drawCell(rowIndex, colIndex, gridSize, Color.parseColor("#99f8ff"), canvas); // light blue
                }
                // draw start position
                if (Integer.valueOf(arenaInfo[rowIndex - 1][colIndex - 1]) == ARENA_GRID_START) {
                    drawCell(rowIndex, colIndex, gridSize, Color.parseColor("#3AB795"), canvas); // green
                }
                // draw end position
                if (Integer.valueOf(arenaInfo[rowIndex - 1][colIndex - 1]) == ARENA_GRID_END) {
                    drawCell(rowIndex, colIndex, gridSize, Color.parseColor("#FFF201"), canvas); // yellow
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
                    drawCell(rowIndex, colIndex, gridSize, Color.parseColor("#000000"), canvas); // black
                }
            }
        }
    }
    arenaGrid.setOnTouchListner(new View.OnTouchListener(){
        public boolean onTouch(View v, MotionEvent event) {
                if (event == simulationEvent)
                    return false;
                int action = event.getAction();
                int x = (int)event.getX();
                int y = (int)event.getY();
                Log.e("onTouchListener", "User touch at X:" + x + " Y:" + y);
                long length = 0;
                if (action == MotionEvent.ACTION_DOWN) {
                    click(v, x, y);
                }
                return false;
            }
    });
    
    public void drawCell(int row, int col, int gridSize, int c, Canvas canvas) {
        Y = row * gridSize - gridSize / 2;
        _Y = row * gridSize + gridSize / 2;
        X = col * gridSize - gridSize / 2;
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

    private void setupNakedGrid(int startPosition_row, int startPosition_col, int endPosition_row, int endPosition_col) {
        Log.d(TAG, "decodeArenaInfo: setupNakedGrid");
        // naked grid
        row = 20;
        col = 15;
        robotPosition_row = robotPosition_col = robotDirection = 0;
        for (int rowIndex = 1; rowIndex <= row; rowIndex++) {
            for (int colIndex = 1; colIndex <= col; colIndex++) {
                this.arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID;
            }
        }
        // set start position
        for (int rowIndex = startPosition_row; rowIndex <= startPosition_row + 2; rowIndex++) {
            for (int colIndex = startPosition_col; colIndex <= startPosition_col + 2; colIndex++) {
                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_START;
            }
        }
        // set end position
        for (int rowIndex = endPosition_row; rowIndex <= endPosition_row + 2; rowIndex++) {
            for (int colIndex = endPosition_col; colIndex <= endPosition_col + 2; colIndex++) {
                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_END;
            }
        }
    }

    public void decodeArenaInfo(String cmdString) {
        int startPosition_row = 1, startPosition_col = 1;
        int endPosition_row = 18;
        int endPosition_col = 13;
        if (cmdString.equals("NAKEDGRID")) {
            setupNakedGrid(startPosition_row, startPosition_col, endPosition_row, endPosition_col);
        } else {
            Log.d(TAG, "decodeArenaInfo: cmdString");
            String[] splitArenaInfoArray = cmdString.split(" ");
            row = Integer.valueOf(splitArenaInfoArray[1]);
            col = Integer.valueOf(splitArenaInfoArray[2]);
            robotPosition_col = Integer.valueOf(splitArenaInfoArray[3]);
            robotPosition_row = Integer.valueOf(splitArenaInfoArray[4]);
            robotDirection = Integer.valueOf(splitArenaInfoArray[5]);
            try {
                // convert raw data into array
                for (int count = 0; count < splitArenaInfoArray.length - 6; count++) {
                    arenaInfoString[count] = Integer.valueOf(splitArenaInfoArray[count + 6]);
                }
                // set obstacles and the grid
                for (int rowIndex = 1; rowIndex <= row; rowIndex++) {
                    for (int colIndex = 1; colIndex <= col; colIndex++) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = arenaInfoString[((colIndex - 1) + (rowIndex - 1) * 15)];
                    }
                }
                // set start position
                for (int rowIndex = 1; rowIndex <= startPosition_row + 2; rowIndex++) {
                    for (int colIndex = 1; colIndex <= startPosition_col + 2; colIndex++) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_START;
                    }
                }
                // set end position
                for (int rowIndex = endPosition_row; rowIndex <= endPosition_row + 2; rowIndex++) {
                    for (int colIndex = endPosition_col; colIndex <= endPosition_col + 2; colIndex++) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_END;
                    }
                }

                // set robot position
                for (int rowIndex = robotPosition_row; rowIndex <= robotPosition_row + 2; rowIndex++) {
                    for (int colIndex = robotPosition_col; colIndex <= robotPosition_col + 2; colIndex++) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOTPOSITION;
                    }
                }

                // set robot direction
                int robotsize = 3;

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
                        if (colIndexCount != robotPosition_col + 1)
                            arenaInfo[robotDirection_body - 1][colIndexCount - 1] = ARENA_ROBOTDIRECTION; // body

                    }
                } else {
                    for (int rowIndexCount = robotPosition_row; rowIndexCount <= robotPosition_row + 2; rowIndexCount++) {
                        if (rowIndexCount != robotPosition_row + 1)
                            arenaInfo[rowIndexCount - 1][robotDirection_body - 1] = ARENA_ROBOTDIRECTION; // body
                    }
                }
            } catch (Exception ex) {

            }
        }
    }
<<<<<<< HEAD

    public int[][] getArenaInfo() {
        if (arenaInfo == null) {

=======
    
    public void setWayPoint() {
        if (arenaInfo != null) {
>>>>>>> 32c20946a528bdb4d1e034b95a03a1fecda63550
        }
        return arenaInfo;
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int column = (int) (event.getX() / gridSize);
            int row = (int) (event.getY() / gridSize);
            if (row >= 1 && row <= 20 && col >= 1 && col <= 15) {
                // empty on the grid
                if (arenaInfo[row - 1][column - 1] == ARENA_GRID) {
                    arenaInfo[row - 1][column - 1] = ARENA_OBSTACLE;
                } else if (arenaInfo[row - 1][column - 1] == ARENA_OBSTACLE) {
                    arenaInfo[row - 1][column - 1] = ARENA_GRID;
                }
            }
            showToast("Touched on (X,Y): " + row + "," + column);
            invalidate();
        }

        return true;
    }


}
