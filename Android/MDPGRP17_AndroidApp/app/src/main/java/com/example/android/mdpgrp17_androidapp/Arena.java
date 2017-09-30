package com.example.android.mdpgrp17_androidapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_OBSTACLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_POSITIN_WAYPOINT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_POSITION_END;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_POSITION_START;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_DIRECTION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_POSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_TRAVELPATH;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_FORWARD;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_REVERSE;

/**
 * Created by szean on 8/9/2017.
 */

public class Arena extends View implements SensorEventListener {
    private static final String TAG = "Arena";
    // GUI objects
    private RelativeLayout RLO_ArenaGrid;
    private TextView TXTVW_WayPointValue;
    private TextView TXTVW_WayPoint;
    private TextView TXTVW_MapModeIndexValue;

    // Arena objects/variables
    private int grid_Row, grid_Col, grid_Size;
    private int startPosition_Row_Center = 19, startPosition_Col_Center = 2, endPosition_Row_Center = 2, endPosition_Col_Center = 14;
    private int wayPointPosition_Row_Center, wayPointPosition_Col_Center;   // center of way point
    private int robotPosition_Row, robotPosition_Col;               // top left reference point from arena info
    private int robotPosition_Row_Center, robotPosition_Col_Center; // center reference point robotPosition_Row, robotPosition_Col
    private int robotDirection;                                     // 0, 90, 180, 270
    private boolean isUpDown, isWayPointReached = false;            // 0 || 180 for isUpDown
    private int[] arenaInfoString = new int[300];   // to store arena info
    private int[][] arenaInfo = new int[20][15], travelInfo = new int[20][15], displayInfo = new int[20][15];
    private Boolean isMapMode_Auto = true; // always auto update map
    private int saveStateIndex_Pause, saveStateIndex_InArray;   // negative to ensure default value, use to transverse the array of the saved state
    private ArrayList<ArenaSaveState> saveStateArrayList;
    private SensorManager sensorManager;


    public Sensor sensor;
    // Paint objects
    private Paint paint;
    private int X;
    private int Y;
    private int _X;
    private int _Y;
    private Canvas canvas;
    // Thread object
    private ArenaThread arenaThread;

    public Arena(Context context, RelativeLayout RLO_ArenaGrid) {
        super(context);
        Log.d(TAG, "Arena");
        // the GUI layout of the arena grid
        this.RLO_ArenaGrid = RLO_ArenaGrid;
        this.saveStateArrayList = new ArrayList<>();
        this.saveStateIndex_Pause = -1;     // negative to ensure default value, use to save at which index the manual map mode is set
        this.saveStateIndex_InArray = -1;   // negative to ensure default value, use to transverse the array of the saved state
        // to paint the grid with styles
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setDither(true);
        // a thread to update the grid
        this.arenaThread = new ArenaThread(this);
        arenaThread.startThread();
    }
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public void onSensorChanged(SensorEvent event) {


        float x = event.values[0];
        float y = event.values[1];
        if (Math.abs(x) > Math.abs(y)) {

                int touched_Col = (int) (x / grid_Size);
                int touched_Row = (int) (y / grid_Size);
                if (touched_Col == wayPointPosition_Col_Center && touched_Row == wayPointPosition_Row_Center) {
                    // remove existing way points
                    removeWayPoint();
                } else {
                    // remove existing way points
                    removeWayPoint();
                    //
                    wayPointPosition_Col_Center = touched_Col;
                    wayPointPosition_Row_Center = touched_Row;
                    // check if touched point is within the grid
                    if (wayPointPosition_Row_Center >= 1 && wayPointPosition_Row_Center <= 20 && grid_Col >= 1 && grid_Col <= 15) {
                        // check if touched point is a valid way point
                        if (arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] == ARENA_GRID) {
                            drawWayPoint();
                        } else {
                            // touched point is not a valid way point, reset
                            showToast("Unable to set way point on (" + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ")");
                            if (wayPointPosition_Row_Center != 0 && wayPointPosition_Col_Center != 0) {
                                wayPointPosition_Row_Center = wayPointPosition_Col_Center = 0;  // reset value for way point
                            }
                            TXTVW_WayPointValue.setText("0,0");
                            TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_Default));
                            TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_Default));
                        }
                    } else {
                        // check if existing way point is set
                        String[] existingWayPoint = TXTVW_WayPointValue.getText().toString().split(",");
                        wayPointPosition_Col_Center = Integer.parseInt(existingWayPoint[0]);
                        wayPointPosition_Row_Center = Integer.parseInt(existingWayPoint[1]);
                        if (wayPointPosition_Col_Center != 0 && wayPointPosition_Row_Center != 0) {
                            drawWayPoint();
                        }
                    }
                }
                invalidate();
            }

        }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        MainActivity mainActivity = (MainActivity) getContext();
        if (mainActivity.getIsWayPointLocked()) {
            // locked, cannot touch screen
            showToast("Way point is locked");
        } else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int touched_Col = (int) (event.getX() / grid_Size);
                int touched_Row = (int) (event.getY() / grid_Size);
                if (touched_Col == wayPointPosition_Col_Center && touched_Row == wayPointPosition_Row_Center) {
                    // remove existing way points
                    removeWayPoint();
                } else {
                    // remove existing way points
                    removeWayPoint();
                    //
                    wayPointPosition_Col_Center = touched_Col;
                    wayPointPosition_Row_Center = touched_Row;
                    // check if touched point is within the grid
                    if (wayPointPosition_Row_Center >= 1 && wayPointPosition_Row_Center <= 20 && grid_Col >= 1 && grid_Col <= 15) {
                        // check if touched point is a valid way point
                        if (arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] == ARENA_GRID) {
                            drawWayPoint();
                        } else {
                            // touched point is not a valid way point, reset
                            showToast("Unable to set way point on (" + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ")");
                            if (wayPointPosition_Row_Center != 0 && wayPointPosition_Col_Center != 0) {
                                wayPointPosition_Row_Center = wayPointPosition_Col_Center = 0;  // reset value for way point
                            }
                            TXTVW_WayPointValue.setText("0,0");
                            TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_Default));
                            TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_Default));
                        }
                    } else {
                        // check if existing way point is set
                        String[] existingWayPoint = TXTVW_WayPointValue.getText().toString().split(",");
                        wayPointPosition_Col_Center = Integer.parseInt(existingWayPoint[0]);
                        wayPointPosition_Row_Center = Integer.parseInt(existingWayPoint[1]);
                        if (wayPointPosition_Col_Center != 0 && wayPointPosition_Row_Center != 0) {
                            drawWayPoint();
                        }
                    }
                }
                invalidate();
            }
        }

        return true;
    }

    public void update() {
        // called every period (200milisec) because arena thread is running

        if (isMapMode_Auto) {
            if (saveStateArrayList.size() > 0) {
                robotPosition_Row_Center = saveStateArrayList.get(saveStateArrayList.size() - 1).getWayPointPosition_Row_Center();
                robotPosition_Col_Center = saveStateArrayList.get(saveStateArrayList.size() - 1).getWayPointPosition_Col_Center();
                displayInfo = saveStateArrayList.get(saveStateArrayList.size() - 1).getArenaInfo();
            }
            if (isWayPointReached) {

            } else {
                checkWayPointReached();
            }
        } else {
            robotPosition_Row_Center = saveStateArrayList.get(saveStateIndex_InArray).getWayPointPosition_Row_Center();
            robotPosition_Col_Center = saveStateArrayList.get(saveStateIndex_InArray).getWayPointPosition_Col_Center();

            displayInfo = saveStateArrayList.get(saveStateIndex_InArray).getArenaInfo();
            if (!saveStateArrayList.get(saveStateIndex_InArray).getWayPointReached()) {
                checkWayPointReached();
            }
        }

    }

    @Override
    public void onDraw(Canvas canvas) {
        try {
            this.canvas = canvas;
            drawArenaGrid(canvas);
        } catch (Exception e) {
        }

    }

    private void drawArenaGrid(Canvas canvas) {
        this.grid_Size = ((RLO_ArenaGrid.getMeasuredWidth()) - (RLO_ArenaGrid.getMeasuredWidth() / grid_Col)) / grid_Col;

        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                // order does not matters, it just paints the grid
                int arenaInfoValue = Integer.valueOf(displayInfo[rowIndex - 1][colIndex - 1]);
                // paint grid
                if (arenaInfoValue == ARENA_GRID) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_Grid, canvas);
                }
                // paint start position
                if (arenaInfoValue == ARENA_GRID_POSITION_START) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_StartPosition, canvas);
                }
                // paint end position
                if (arenaInfoValue == ARENA_GRID_POSITION_END) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_EndPosition, canvas);
                }
                // paint robot position
                if (robotPosition_Row_Center != 0 && robotPosition_Col_Center != 0) {
                    // check if robot on way point
                    if (robotPosition_Row_Center == wayPointPosition_Row_Center && robotPosition_Col_Center == wayPointPosition_Col_Center) {
                        // draw robot position with border color
                        if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                            drawCellWithBorder_WayPoint(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotPosition, canvas); //  dark orange with way point border color
                        }
                        // draw robot direction with border color
                        if (arenaInfoValue == ARENA_ROBOT_DIRECTION) {
                            drawCellWithBorder_WayPoint(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotDirection, canvas); //  dark orchid with way point border color
                        }
                        // check if robot on start position
                    } else if (robotPosition_Row_Center == startPosition_Row_Center && robotPosition_Col_Center == startPosition_Col_Center) {
                        // draw robot position with border color
                        if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                            drawCellWithBorder_StartPosition(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotPosition, canvas); //  dark orange with way point border color
                        }
                        // draw robot direction with border color
                        if (arenaInfoValue == ARENA_ROBOT_DIRECTION) {
                            drawCellWithBorder_StartPosition(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotDirection, canvas); //  dark orchid with way point border color
                        }
                        // check if robot on end position
                    } else if (robotPosition_Row_Center == endPosition_Row_Center && robotPosition_Col_Center == endPosition_Col_Center) {
                        // draw robot position with border color
                        if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                            drawCellWithBorder_EndPosition(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotPosition, canvas); //  dark orange with way point border color
                        }
                        // draw robot direction with border color
                        if (arenaInfoValue == ARENA_ROBOT_DIRECTION) {
                            drawCellWithBorder_EndPosition(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotDirection, canvas); //  dark orchid with way point border color
                        }
                    } else {
                        // draw robot position as robot not on way point center, start or end position
                        if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                            drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotPosition, canvas);
                        }
                        // draw robot direction
                        if (arenaInfoValue == ARENA_ROBOT_DIRECTION) {
                            drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotDirection, canvas);
                        }
                    }
                } else {
                    // draw robot position as robot not on way point, on grid
                    if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                        drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotPosition, canvas);
                    }
                    // draw robot direction
                    if (arenaInfoValue == ARENA_ROBOT_DIRECTION) {
                        drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotDirection, canvas);
                    }
                }
                // draw obstacle
                if (arenaInfoValue == ARENA_GRID_OBSTACLE) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_Obstacle, canvas);
                }
                // draw way point
                if (arenaInfoValue == ARENA_GRID_POSITIN_WAYPOINT) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_WayPoint, canvas);
                }
                // draw travel path
                if (arenaInfoValue == ARENA_ROBOT_TRAVELPATH) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotTravelPath, canvas);
                }
                // draw way point reached
                if (arenaInfoValue == ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT) {
                    drawCellWithBorder_WayPoint(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotTravelPath, canvas);
                }
            }
        }
    }

    public void drawCell(int row, int col, int gridSize, int c, Canvas canvas) {
        Y = row * gridSize - gridSize / 2;
        _Y = row * gridSize + gridSize / 2;
        X = col * gridSize - gridSize / 2;
        _X = col * gridSize + gridSize / 2;
        // paint the fill in color
        paint.setColor(ContextCompat.getColor(getContext(), c));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
        // paint the stroke border
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);

    }

    // WITH WAY POINT BORDER
    public void drawCellWithBorder_WayPoint(int row, int col, int gridSize, int c, Canvas canvas) {
        Y = row * gridSize - gridSize / 2;
        _Y = row * gridSize + gridSize / 2;
        X = col * gridSize - gridSize / 2;
        _X = col * gridSize + gridSize / 2;
        // paint the fill in color
        paint.setColor(ContextCompat.getColor(getContext(), c));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
        // paint the stroke border
        paint.setColor(ContextCompat.getColor(getContext(), R.color.color_Arena_WayPoint));    // light violet
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
    }

    public void drawCellWithBorder_StartPosition(int row, int col, int gridSize, int c, Canvas canvas) {
        Y = row * gridSize - gridSize / 2;
        _Y = row * gridSize + gridSize / 2;
        X = col * gridSize - gridSize / 2;
        _X = col * gridSize + gridSize / 2;
        // paint the fill in color
        paint.setColor(ContextCompat.getColor(getContext(), c));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
        // paint the stroke border
        paint.setColor(ContextCompat.getColor(getContext(), R.color.color_Arena_StartPosition));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
    }

    public void drawCellWithBorder_EndPosition(int row, int col, int gridSize, int c, Canvas canvas) {
        Y = row * gridSize - gridSize / 2;
        _Y = row * gridSize + gridSize / 2;
        X = col * gridSize - gridSize / 2;
        _X = col * gridSize + gridSize / 2;
        // paint the fill in color
        paint.setColor(ContextCompat.getColor(getContext(), c));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
        // paint the stroke border
        paint.setColor(ContextCompat.getColor(getContext(), R.color.color_Arena_EndPosition));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
    }

    public void setupNakedGrid() {
        Log.d(TAG, "setupNakedGrid");
        // naked grid
        grid_Row = 20;
        grid_Col = 15;
        robotDirection = 0;
        robotPosition_Row = 18; // start position
        robotPosition_Col = 1;  // sart position
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                this.arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID;
            }
        }
        // set start position
        drawStartPosition();
        // set end position
        drawEndPosition();

    }

    public void decodeArenaInfo(String cmdString) {

        // decode the cmd string
        if (cmdString.contains("NakedArena")) {
            setupNakedGrid();
        } else {
            Log.d(TAG, "decodeArenaInfo: cmdString: " + cmdString);
            String[] splitArenaInfoArray = cmdString.split(" ");
            grid_Row = Integer.valueOf(splitArenaInfoArray[1]);
            grid_Col = Integer.valueOf(splitArenaInfoArray[2]);
            robotPosition_Col = Integer.valueOf(splitArenaInfoArray[3]);    // top col
            robotPosition_Row = Integer.valueOf(splitArenaInfoArray[4]);    // top row
            robotDirection = Integer.valueOf(splitArenaInfoArray[5]);
            robotPosition_Col_Center = robotPosition_Col + 1;   // center col
            robotPosition_Row_Center = robotPosition_Row + 1;   // center row
            try {
                // convert raw data into array
                for (int count = 0; count < splitArenaInfoArray.length - 6; count++) {
                    arenaInfoString[count] = Integer.valueOf(splitArenaInfoArray[count + 6]);
                }
                // set obstacles and the grid
                for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
                    for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = arenaInfoString[((colIndex - 1) + (rowIndex - 1) * 15)];
                    }
                }
                // set start position
                drawStartPosition();
                // set end position
                drawEndPosition();
                // set way point
                drawWayPoint();
                // check way point reached
                checkWayPointReached();
                // set robot position
                drawRobotPosition();
                // set robot direction
                drawRobotDirection();
                // set robot travel path
                drawTravelPath();
                if (isWayPointReached) drawWayPointReached();
            } catch (Exception ex) {

            }
        }
        Log.d(TAG, "********************************SAVE********************************");
        int[][] saveState_Temp_ArenaInfo = new int[grid_Row][grid_Col];  // temp variable to save the state
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                saveState_Temp_ArenaInfo[rowIndex - 1][colIndex - 1] = arenaInfo[rowIndex - 1][colIndex - 1];
            }
        }
        int[][] saveState_Temp_TravelInfo = new int[grid_Row][grid_Col];  // temp variable to save the state
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                saveState_Temp_TravelInfo[rowIndex - 1][colIndex - 1] = travelInfo[rowIndex - 1][colIndex - 1];
            }
        }
        ArenaSaveState arenaSaveState = new ArenaSaveState(saveState_Temp_ArenaInfo, saveState_Temp_TravelInfo,
                isWayPointReached, robotPosition_Row_Center, robotPosition_Col_Center);
        saveStateArrayList.add(arenaSaveState);
        drawWayPoint();
        printArenaInfo();
    }

    public void printArenaInfo() {
        Log.d(TAG, "--------------------Start of Print Arena Info--------------------");
        Log.d(TAG, "--------------------printArenaInfo--------------------");
        String printArenaInfoString = Arrays.deepToString(arenaInfo).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
        String[] printArenaInfoStringArray = printArenaInfoString.split(";");
        for (String value : printArenaInfoStringArray) {
            Log.d(TAG, "printArenaInfo: arenaInfo: " + value);
        }
        Log.d(TAG, "--------------------printTravelInfo--------------------");
        String printTravelInfoString = Arrays.deepToString(travelInfo).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
        String[] printTravelInfoArray = printTravelInfoString.split(";");
        for (String value : printTravelInfoArray) {
            Log.d(TAG, "printTravelInfo: travelInfo: " + value);
        }
        Log.d(TAG, "--------------------saveArenaState---------------------");
        Log.d(TAG, "saveArenaState: Index_Pause: " + (saveStateIndex_Pause) + ", Index_InArray: " + (saveStateIndex_InArray) +
                ", arrListSize: " + saveStateArrayList.size() + ", MapMode: " +
                isMapMode_Auto + ", WayPointReached: " + saveStateArrayList.get(saveStateArrayList.size() - 1).getWayPointReached());
        for (int index = 0; index < saveStateArrayList.size(); index++) {
            int[][] saveArenaState = saveStateArrayList.get(index).getArenaInfo();
            String saveArenaStateString = Arrays.deepToString(saveArenaState).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
            String[] saveArenaStateArray = saveArenaStateString.split(";");
            for (String value : saveArenaStateArray) {
                Log.d(TAG, "saveArenaState: arenaInfo: " + value);
            }
            Log.d(TAG, "------------------------------------------------------- Count: " + index);
        }
        Log.d(TAG, "--------------------End of Print Arena Info--------------------");

    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public boolean checkMovement(String moveCmd) {
        Log.d(TAG, "checkMovement");
        boolean isValidMove = true;
        int checkDirection;
        if (moveCmd.equals(CMD_FORWARD)) {
            if (robotDirection == 0) { // up
                checkDirection = robotPosition_Row - 1;
                if (checkDirection < 1) {
                    isValidMove = false;
                } else {
                    for (int colIndex = robotPosition_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                        if (arenaInfo[checkDirection - 1][colIndex - 1] == ARENA_GRID_OBSTACLE) {
                            isValidMove = false;
                            break;
                        }
                    }
                }
            }
            if (robotDirection == 90) { // right
                checkDirection = robotPosition_Col + 3;
                if (checkDirection > grid_Col) {
                    isValidMove = false;
                } else {
                    for (int rowIndex = robotPosition_Row; rowIndex <= robotPosition_Row + 2; rowIndex++) {
                        if (arenaInfo[rowIndex - 1][checkDirection - 1] == ARENA_GRID_OBSTACLE) {
                            isValidMove = false;
                            break;
                        }
                    }
                }
            }
            if (robotDirection == 180) { // bottom
                checkDirection = robotPosition_Row + 3;
                if (checkDirection > grid_Row) {
                    isValidMove = false;
                } else {
                    for (int colIndex = robotPosition_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                        if (arenaInfo[checkDirection - 1][colIndex - 1] == ARENA_GRID_OBSTACLE) {
                            isValidMove = false;
                            break;
                        }
                    }
                }
            }
            if (robotDirection == 270) { // left
                checkDirection = robotPosition_Col - 1;
                if (checkDirection < 1) {
                    isValidMove = false;
                } else {
                    for (int rowIndex = robotPosition_Row; rowIndex <= robotPosition_Row + 2; rowIndex++) {
                        if (arenaInfo[rowIndex - 1][checkDirection - 1] == ARENA_GRID_OBSTACLE) {
                            isValidMove = false;
                            break;
                        }
                    }
                }
            }
        }
        if (moveCmd.equals(CMD_REVERSE)) {
            if (robotDirection == 0) { // up
                checkDirection = robotPosition_Row + 3;
                if (checkDirection > grid_Row) {
                    isValidMove = false;
                } else {
                    for (int colIndex = robotPosition_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                        if (arenaInfo[checkDirection - 1][colIndex - 1] == ARENA_GRID_OBSTACLE) {
                            isValidMove = false;
                            break;
                        }
                    }
                }
            }
            if (robotDirection == 90) { // right
                checkDirection = robotPosition_Col - 1;
                if (checkDirection < 1) {
                    isValidMove = false;
                } else {
                    for (int rowIndex = robotPosition_Row; rowIndex <= robotPosition_Row + 2; rowIndex++) {
                        if (arenaInfo[rowIndex - 1][checkDirection - 1] == ARENA_GRID_OBSTACLE) {
                            isValidMove = false;
                            break;
                        }
                    }
                }
            }
            if (robotDirection == 180) { // bottom
                checkDirection = robotPosition_Row - 1;
                if (checkDirection < 1) {
                    isValidMove = false;
                } else {
                    for (int colIndex = robotPosition_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                        if (arenaInfo[checkDirection - 1][colIndex - 1] == ARENA_GRID_OBSTACLE) {
                            isValidMove = false;
                            break;
                        }
                    }
                }
            }
            if (robotDirection == 270) { // left
                checkDirection = robotPosition_Col + 3;
                if (checkDirection > grid_Col) {
                    isValidMove = false;
                } else {
                    for (int rowIndex = robotPosition_Row; rowIndex <= robotPosition_Row + 2; rowIndex++) {
                        if (arenaInfo[rowIndex - 1][checkDirection - 1] == ARENA_GRID_OBSTACLE) {
                            isValidMove = false;
                            break;
                        }
                    }
                }
            }
        }

        return isValidMove;
    }

    private void checkWayPointReached() {
        if (robotPosition_Row_Center != 0 && robotPosition_Col_Center != 0) {
            TXTVW_WayPointValue = (TextView) getRootView().findViewById(R.id.TXTVW_WayPointValue);
            TXTVW_WayPoint = (TextView) getRootView().findViewById(R.id.TXTVW_WayPoint);
            if (robotPosition_Row_Center == wayPointPosition_Row_Center && robotPosition_Col_Center == wayPointPosition_Col_Center) {
                Log.d(TAG, "checkWayPointReached: Way Point Reached");
                isWayPointReached = true;
            }
        }
    }

    // Getters and Setters
    public void setSaveStateArrayList(ArrayList<ArenaSaveState> saveStateArrayList) {
        this.saveStateArrayList = saveStateArrayList;
    }

    public Boolean getMapMode() {
        return isMapMode_Auto;
    }

    public void setMapMode(Boolean isMapMode_Auto) {
        this.isMapMode_Auto = isMapMode_Auto;
    }

    public int getSaveStateIndex_Pause() {
        return saveStateIndex_Pause;
    }

    public void setSaveStateIndex_Pause(int saveStateIndex_Pause) {
        this.saveStateIndex_Pause = saveStateIndex_Pause;
    }

    public int getSaveStateIndex_InArray() {
        return saveStateIndex_InArray;
    }

    public void setSaveStateIndex_InArray(int saveStateIndex_InArray) {
        this.saveStateIndex_InArray = saveStateIndex_InArray;
    }

    public int getArenaSaveStateArrayListSize() {
        return saveStateArrayList.size();
    }

    public ArrayList<ArenaSaveState> getSaveStateArrayList() {
        return saveStateArrayList;
    }

    public int getWayPointPosition_Row_Center() {
        return wayPointPosition_Row_Center;
    }

    public void setWayPointPosition_Row_Center(int wayPointPosition_Row_Center) {
        this.wayPointPosition_Row_Center = wayPointPosition_Row_Center;
    }

    public int getWayPointPosition_Col_Center() {
        return wayPointPosition_Col_Center;
    }

    public void setWayPointPosition_Col_Center(int wayPointPosition_Col_Center) {
        this.wayPointPosition_Col_Center = wayPointPosition_Col_Center;
    }

    // Arena Methods
    private void drawStartPosition() {
        Log.d(TAG, "drawStartPosition");
        int startPosition_row = 18, startPosition_col = 1;
        // set start position
        for (int rowIndex = startPosition_row; rowIndex <= 20; rowIndex++) {
            for (int colIndex = startPosition_col; colIndex <= 3; colIndex++) {
                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_POSITION_START;
            }
        }
    }

    private void drawEndPosition() {
        Log.d(TAG, "drawEndPosition");
        int endPosition_row = 1, endPosition_col = 13;
        // set end position
        for (int rowIndex = endPosition_row; rowIndex <= 3; rowIndex++) {
            for (int colIndex = endPosition_col; colIndex <= 15; colIndex++) {
                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_POSITION_END;
            }
        }
    }

    public void drawWayPoint() {
        Log.d(TAG, "drawWayPoint: " + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ", center reference point");
        // set new way point
        int waypoint_Row_Top = wayPointPosition_Row_Center - 1;
        int waypoint_Row_Bottom = wayPointPosition_Row_Center + 1;
        int waypoint_Col_Left = wayPointPosition_Col_Center - 1;
        int waypoint_Col_Right = wayPointPosition_Col_Center + 1;
        TXTVW_WayPointValue = (TextView) getRootView().findViewById(R.id.TXTVW_WayPointValue);
        TXTVW_WayPoint = (TextView) getRootView().findViewById(R.id.TXTVW_WayPoint);
        boolean isPaintable = true;

        // check if paint points within the grid
        if (waypoint_Row_Top < 1 || waypoint_Row_Bottom > grid_Row || waypoint_Col_Left < 1 || waypoint_Col_Right > grid_Col) {
            Log.d(TAG, "drawWayPoint: paint points outside the grid");
            // paint points outside the grid
            if (wayPointPosition_Row_Center == 0 && wayPointPosition_Col_Center == 0) {
                showToast("No way point set");
            } else {
                showToast("Unable to set way point on (" + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ")");
                if (wayPointPosition_Row_Center != 0 && wayPointPosition_Col_Center != 0) {
                    wayPointPosition_Row_Center = wayPointPosition_Col_Center = 0;  // reset value for way point
                }
            }
            TXTVW_WayPointValue.setText("0,0");
            TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_Default));
            TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_Default));
        } else {
            // paint points within the grid,
            Log.d(TAG, "drawWayPoint: paint points within the grid");
            // check if the selected way point contains any obstacles
            for (int index = 0; index < saveStateArrayList.size(); index++) {
                int[][] arenaInfor = saveStateArrayList.get(index).getArenaInfo();
                if (isPaintable) {
                    for (int rowIndex = waypoint_Row_Top; rowIndex <= waypoint_Row_Bottom; rowIndex++) {
                        for (int colIndex = waypoint_Col_Left; colIndex <= waypoint_Col_Right; colIndex++) {
                            if (arenaInfor[rowIndex - 1][colIndex - 1] == ARENA_GRID_OBSTACLE) {
                                isPaintable = false;
                                break;
                            }
                        }
                    }
                }
            }
            if (isPaintable) {
                // update each arena info in the saveState_ArenaInfo
                for (int index = 0; index < saveStateArrayList.size(); index++) {
                    int[][] arenaInfor = saveStateArrayList.get(index).getArenaInfo();
                    for (int rowIndex = waypoint_Row_Top; rowIndex <= waypoint_Row_Bottom; rowIndex++) {
                        for (int colIndex = waypoint_Col_Left; colIndex <= waypoint_Col_Right; colIndex++) {
                            if (arenaInfor[rowIndex - 1][colIndex - 1] == ARENA_GRID) {
                                arenaInfor[rowIndex - 1][colIndex - 1] = ARENA_GRID_POSITIN_WAYPOINT;
                            }
                        }
                    }
                }
                if (isMapMode_Auto) {
                    if (isWayPointReached) {
                        TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_StartPosition));
                        TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_StartPosition));
                    } else {
                        TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_WayPoint));
                        TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_WayPoint));
                    }
                } else {
                    if (saveStateArrayList.get(saveStateIndex_InArray).getWayPointReached()) {
                        TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_StartPosition));
                        TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_StartPosition));
                    } else {
                        TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_WayPoint));
                        TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_WayPoint));
                    }
                }
            } else {
                showToast("Unable to set way point on (" + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ")");
                TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_error));
                TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_error));
            }
        }


        TXTVW_WayPointValue.setText(wayPointPosition_Col_Center + "," + wayPointPosition_Row_Center);
    }

    private void removeWayPoint() {
        Log.d(TAG, "removeWayPoint: " + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ", center reference point");
        //  remove existing way points
        // remove each arena info in the saveState_ArenaInfo
        for (int index = 0; index < saveStateArrayList.size(); index++) {
            int[][] arenaInfor = saveStateArrayList.get(index).getArenaInfo();
            for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
                for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                    if (arenaInfor[rowIndex - 1][colIndex - 1] == ARENA_GRID_POSITIN_WAYPOINT) {
                        arenaInfor[rowIndex - 1][colIndex - 1] = ARENA_GRID;
                    }
                }
            }
        }

        wayPointPosition_Row_Center = wayPointPosition_Col_Center = 0;
        TXTVW_WayPointValue = (TextView) getRootView().findViewById(R.id.TXTVW_WayPointValue);
        TXTVW_WayPointValue.setText("0,0");
        TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_Default));
        TXTVW_WayPoint = (TextView) getRootView().findViewById(R.id.TXTVW_WayPoint);
        TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_Default));
    }

    private void drawWayPointReached() {
        Log.d(TAG, "drawWayPointReached: " + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ", center reference point");
        // set new way point
        int waypoint_Row_Top = wayPointPosition_Row_Center - 1;
        int waypoint_Row_Bottom = wayPointPosition_Row_Center + 1;
        int waypoint_Col_Left = wayPointPosition_Col_Center - 1;
        int waypoint_Col_Right = wayPointPosition_Col_Center + 1;
        for (int rowIndex = waypoint_Row_Top; rowIndex <= waypoint_Row_Bottom; rowIndex++) {
            for (int colIndex = waypoint_Col_Left; colIndex <= waypoint_Col_Right; colIndex++) {
                if (robotPosition_Row_Center == wayPointPosition_Row_Center &&
                        robotPosition_Col_Center == wayPointPosition_Col_Center) {
                    drawRobotPosition();
                    drawRobotDirection();
                } else {
                    Log.d(TAG, "arenaInfo[" + rowIndex + "][" + colIndex + "]:" + arenaInfo[rowIndex - 1][colIndex - 1]);
                    if (arenaInfo[rowIndex - 1][colIndex - 1] != ARENA_ROBOT_POSITION &&
                            arenaInfo[rowIndex - 1][colIndex - 1] != ARENA_ROBOT_DIRECTION)
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT; // set way point reached
                }

            }
        }


    }

    private void drawRobotPosition() {
        Log.d(TAG, "drawRobotPosition: " + robotPosition_Row + "," + robotPosition_Col + ", top left reference point");
        // set robot position
        // check if robot position is within grid
        if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
            for (int rowIndex = robotPosition_Row; rowIndex <= robotPosition_Row + 2; rowIndex++) {
                for (int colIndex = robotPosition_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_POSITION;
                }
            }
        }
    }

    private void drawRobotDirection() {
        Log.d(TAG, "drawRobotDirection: " + robotDirection);
        int robotDirection_head_row;
        int robotDirection_head_col;
        int robotDirection_body = 3;
        isUpDown = false;

        // set robot direction head
        if (robotDirection == 0) { // up
            robotDirection_head_row = robotPosition_Row;
            robotDirection_head_col = robotPosition_Col + 1;
            robotDirection_body = robotPosition_Row + 1;
            if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
                arenaInfo[robotDirection_head_row - 1][robotDirection_head_col - 1] = ARENA_ROBOT_DIRECTION;
            }
            isUpDown = true;
        }
        if (robotDirection == 90) { // right
            robotDirection_head_row = robotPosition_Row + 1;
            robotDirection_head_col = robotPosition_Col + 2;
            robotDirection_body = robotPosition_Col + 1;
            if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
                arenaInfo[robotDirection_head_row - 1][robotDirection_head_col - 1] = ARENA_ROBOT_DIRECTION;
            }
        }
        if (robotDirection == 180) { // down
            robotDirection_head_row = robotPosition_Row + 2;
            robotDirection_head_col = robotPosition_Col + 1;
            robotDirection_body = robotPosition_Row + 1;
            if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
                arenaInfo[robotDirection_head_row - 1][robotDirection_head_col - 1] = ARENA_ROBOT_DIRECTION;
            }
            isUpDown = true;
        }
        if (robotDirection == 270) { // left
            robotDirection_head_row = robotPosition_Row + 1;
            robotDirection_head_col = robotPosition_Col;
            robotDirection_body = robotPosition_Col + 1;
            if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
                arenaInfo[robotDirection_head_row - 1][robotDirection_head_col - 1] = ARENA_ROBOT_DIRECTION; // head
            }
        }
        // set robot direction body
        if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
            if (isUpDown) {
                for (int colIndexCount = robotPosition_Col; colIndexCount <= robotPosition_Col + 2; colIndexCount++) {
                    if (colIndexCount != robotPosition_Col + 1)
                        arenaInfo[robotDirection_body - 1][colIndexCount - 1] = ARENA_ROBOT_DIRECTION; // body
                }
            } else {
                for (int rowIndexCount = robotPosition_Row; rowIndexCount <= robotPosition_Row + 2; rowIndexCount++) {
                    if (rowIndexCount != robotPosition_Row + 1)
                        arenaInfo[rowIndexCount - 1][robotDirection_body - 1] = ARENA_ROBOT_DIRECTION; // body
                }
            }
        }
    }

    private void drawTravelPath() {
        Log.d(TAG, "drawTravelPath");
        // save previous position of robot into travel info array
        for (int paintRow = robotPosition_Row; paintRow <= robotPosition_Row + 2; paintRow++) {
            for (int paintCol = robotPosition_Col; paintCol <= robotPosition_Col + 2; paintCol++) {
                travelInfo[paintRow - 1][paintCol - 1] = ARENA_ROBOT_TRAVELPATH;
            }
        }
        // set the travel info into arena info via checking previous path
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                if (arenaInfo[rowIndex - 1][colIndex - 1] != ARENA_ROBOT_POSITION &&
                        arenaInfo[rowIndex - 1][colIndex - 1] != ARENA_ROBOT_DIRECTION &&
                        travelInfo[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_TRAVELPATH) {
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH;
                }
            }
        }
    }


}
