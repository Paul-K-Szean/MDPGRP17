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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_END;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_OBSTACLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_START;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_WAYPOINT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_DIRECTION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_POSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_TRAVELPATH;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT;

/**
 * Created by szean on 8/9/2017.
 */

public class Arena extends View {
    private static final String TAG = "Arena";
    // GUI objects
    private RelativeLayout RLO_ArenaGrid;
    private TextView TV_WayPointValue;

    // Arena objects/variables
    private int grid_Row, grid_Col, grid_Size;
    private int wayPointPosition_Row_Center = 0, wayPointPosition_Col_Center = 0;   // center of way point
    private int robotPosition_Row, robotPosition_Col;               // top left reference point from arena info
    private int robotPosition_Row_Center, robotPosition_Col_Center; // center reference point robotPosition_Row, robotPosition_Col
    private int robotDirection;                                     // 0, 90, 180, 270
    private boolean isUpDown, isWayPointReached = false;            // 0 || 180 for isUpDown
    private int[] arenaInfoString = new int[300];   // to store arena info
    private int[][] arenaInfo = new int[20][15];
    private int[][] travelInfo = new int[20][15];
    private Boolean isMapMode_Auto = true; // always auto update map
    private int arenaStateIndex_Count, arenaStateIndex_Pause, arenaStateIndex_InArray;   // negative to ensure default value, use to transverse the array of the saved state
    private ArrayList<int[][]> saveState_ArenaInfo;
    private ArrayList<int[][]> saveState_TravelInfo;

    // Paint objects
    private Paint paint;
    private int X;
    private int Y;
    private int _X;
    private int _Y;
    // Thread object
    private ArenaThread arenaThread;

    public Arena(Context context, RelativeLayout RLO_ArenaGrid) {
        super(context);
        Log.d(TAG, "Arena");
        // the GUI layout of the arena grid
        this.RLO_ArenaGrid = RLO_ArenaGrid;
        this.saveState_ArenaInfo = new ArrayList<>();
        this.saveState_TravelInfo = new ArrayList<>();
        this.arenaStateIndex_Count = -1;     // negative to ensure default value,
        this.arenaStateIndex_Pause = -1;     // negative to ensure default value, use to save at which index the manual map mode is set
        this.arenaStateIndex_InArray = -1;   // negative to ensure default value, use to transverse the array of the saved state
        // to paint the grid with styles
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setDither(true);
        // a thread to update the grid
        this.arenaThread = new ArenaThread(this);
        arenaThread.startThread();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int touched_Col = (int) (event.getX() / grid_Size);
            int touched_Row = (int) (event.getY() / grid_Size);

            if (touched_Col == wayPointPosition_Col_Center && touched_Row == wayPointPosition_Row_Center) {
                // remove existing way points
                removeWayPoint();
            } else {
                wayPointPosition_Col_Center = touched_Col;
                wayPointPosition_Row_Center = touched_Row;
                // remove existing way points
                removeWayPoint();
                if (touched_Row > 0 && touched_Row < 20 && touched_Col > 0 && touched_Col < 15) {
                    // set way point
                    setWayPoint();
                } else {
                    showToast("Unable to set way point");
                }
            }
            invalidate();
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        try {
            drawArenaGrid(canvas);
        } catch (Exception e) {
        }

    }

    public void update() {
        // called every period (200milisec) because arena thread is running
        if (!isWayPointReached) checkWayPointReached();

//        if (!isMapMode_Auto) {
//            // arenaStateIndex_Count already incremented by 1
//            Log.d(TAG, "decodeArenaInfo: Index_Pause:" + (arenaStateIndex_Pause) + ","
//                    + " Index_InArray:" + (arenaStateIndex_InArray) + ","
//                    + " saveState_ArenaInfo.size(): " + (saveState_ArenaInfo.size()));
//
//            arenaInfo = saveState_ArenaInfo.get(arenaStateIndex_InArray);
//            travelInfo = saveState_TravelInfo.get(arenaStateIndex_InArray);
//        }
    }

    private void drawArenaGrid(Canvas canvas) {
        this.grid_Size = ((RLO_ArenaGrid.getMeasuredWidth()) - (RLO_ArenaGrid.getMeasuredWidth() / grid_Col)) / grid_Col;
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                int arenaInfoValue = Integer.valueOf(arenaInfo[rowIndex - 1][colIndex - 1]);

                // order does not matters, it just paints the grid
                // paint grid
                if (arenaInfoValue == ARENA_GRID) {
                    drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#99f8ff"), canvas); // light blue
                }
                // paint start position
                if (arenaInfoValue == ARENA_GRID_START) {
                    drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#3AB795"), canvas); // green
                }
                // paint end position
                if (arenaInfoValue == ARENA_GRID_END) {
                    drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#FFF201"), canvas); // yellow
                }
                // paint robot position
                if (robotPosition_Row_Center != 0 && robotPosition_Col_Center != 0) {
                    if (robotPosition_Row_Center == wayPointPosition_Row_Center && robotPosition_Col_Center == wayPointPosition_Col_Center) {
                        // draw robot position with border color
                        if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                            drawCellWithBorder_WayPoint(rowIndex, colIndex, grid_Size, Color.parseColor("#FF8C00"), canvas); //  dark orange with way point border color
                        }
                        // draw robot direction with border color
                        if (arenaInfoValue == ARENA_ROBOT_DIRECTION) {
                            drawCellWithBorder_WayPoint(rowIndex, colIndex, grid_Size, Color.parseColor("#9932CC"), canvas); //  dark orchid with way point border color
                        }
                    } else { // robot not on way point
                        // draw robot position
                        if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                            drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#FF8C00"), canvas); // dark orange
                        }
                        // draw robot direction
                        if (arenaInfoValue == ARENA_ROBOT_DIRECTION) {
                            drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#9932CC"), canvas); // dark orchid
                        }
                    }
                } else {   // robot not on way point
                    // draw robot position
                    if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                        drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#FF8C00"), canvas); // dark orange
                    }
                    // draw robot direction
                    if (arenaInfoValue == ARENA_ROBOT_DIRECTION) {
                        drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#9932CC"), canvas); // dark orchid
                    }
                }
                // draw obstacle
                if (arenaInfoValue == ARENA_GRID_OBSTACLE) {
                    drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#000000"), canvas); // black
                }
                // draw way point
                if (arenaInfoValue == ARENA_GRID_WAYPOINT) {
                    drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#a76dff"), canvas); // light violet
                }
                // draw way point reached
                if (arenaInfoValue == ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT) {
                    drawCellWithBorder_WayPoint(rowIndex, colIndex, grid_Size, Color.parseColor("#3AB795"), canvas); // green
                }
                // draw travel path
                if (arenaInfoValue == ARENA_ROBOT_TRAVELPATH) {
                    drawCell(rowIndex, colIndex, grid_Size, Color.parseColor("#3AB795"), canvas); //  green
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
        paint.setColor(c);
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
        paint.setColor(c);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
        // paint the stroke border
        paint.setColor(Color.parseColor("#a76dff"));    // light violet
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(new RectF(X, Y, _X, _Y), paint);
    }

    public void setupNakedGrid() {
        Log.d(TAG, "decodeArenaInfo: setupNakedGrid");
        // naked grid
        grid_Row = 20;
        grid_Col = 15;
        robotDirection = 0;
        robotPosition_Row = 18;
        robotPosition_Col = 1;
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                this.arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID;
            }
        }
        // set start position
        setStartPosition();
        // set end position
        setEndPosition();

    }

    public void decodeArenaInfo(String cmdString) {

        if (cmdString.equals("NAKEDGRID")) {
            setupNakedGrid();
        } else {
            Log.d(TAG, "decodeArenaInfo: cmdString");
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
                // set custom arena info
                // set start position
                setStartPosition();
                // set end position
                setEndPosition();
                // set way point
                setWayPoint();
                // check way point reached
                checkWayPointReached();

                // set robot position
                setRobotPosition();
                // set robot direction
                setRobotDirection();
                // set robot travel path
                setTravelPath();
                if (isWayPointReached) setWayPointReached();
                // TODO: Check if robot reached the way point

            } catch (Exception ex) {

            }
        }
        Log.d(TAG, "*******************************BEFORE*******************************");
        printArenaInfo();
        Log.d(TAG, "decodeArenaInfo: Saving state");
        int[][] saveState_2DArray_ArenaInfo = new int[20][15];  // temp variable to save the state
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                saveState_2DArray_ArenaInfo[rowIndex - 1][colIndex - 1] = arenaInfo[rowIndex - 1][colIndex - 1];
            }
        }
        saveState_ArenaInfo.add(saveState_2DArray_ArenaInfo);
        // saveState_TravelInfo.add(travelInfo);
        Log.d(TAG, "*******************************AFTER*******************************");
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
        Log.d(TAG, "--------------------saveArenaState--------------------");
        Log.d(TAG, "saveArenaState: Index_Pause: " + (arenaStateIndex_Pause) + ", Index_InArray: " + (arenaStateIndex_InArray) +
                ", saveState_ArenaInfo.size: " + (saveState_ArenaInfo.size()));
        for (int index = 0; index < saveState_ArenaInfo.size(); index++) {
            int[][] saveArenaState = saveState_ArenaInfo.get(index);
            String saveArenaStateString = Arrays.deepToString(saveArenaState).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
            String[] saveArenaStateArray = saveArenaStateString.split(";");
            for (String value : saveArenaStateArray) {
                Log.d(TAG, "saveArenaState: arenaInfo: " + value);
            }
            Log.d(TAG, "------------------------------------------------------- Count: " + index);
        }
        Log.d(TAG, "--------------------printTravelInfo--------------------");
        String printTravelInfoString = Arrays.deepToString(travelInfo).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
        String[] printTravelInfoArray = printTravelInfoString.split(";");
        for (String value : printTravelInfoArray) {
            Log.d(TAG, "printTravelInfo: travelInfo: " + value);
        }

        Log.d(TAG, "--------------------End of Print Arena Info--------------------");

    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public boolean checkMovement() {
        Log.d(TAG, "checkMovement");
        boolean isValidMove = true;
        int checkDirection;
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
            if (checkDirection > 15) {
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
            if (checkDirection > 20) {
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
        return isValidMove;
    }

    public Boolean getMapMode() {
        return isMapMode_Auto;
    }

    public void setMapMode(Boolean isMapMode_Auto) {
        this.isMapMode_Auto = isMapMode_Auto;
    }

    public int getArenaStateIndex_Pause() {
        return arenaStateIndex_Pause;
    }

    public void setArenaStateIndex_Pause(int arenaStateIndex_Pause) {
        this.arenaStateIndex_Pause = arenaStateIndex_Pause;
    }

    public int getArenaStateIndex_InArray() {
        return arenaStateIndex_InArray;
    }

    public void setArenaStateIndex_InArray(int arenaStateIndex_InArray) {
        this.arenaStateIndex_InArray = arenaStateIndex_InArray;
    }

    public int getArenaInfoSize() {
        return saveState_ArenaInfo.size();
    }

    private void setStartPosition() {
        Log.d(TAG, "setStartPosition");
        int startPosition_row = 18, startPosition_col = 1;
        // set start position
        for (int rowIndex = startPosition_row; rowIndex <= 20; rowIndex++) {
            for (int colIndex = startPosition_col; colIndex <= 3; colIndex++) {
                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_START;
            }
        }
    }

    private void setEndPosition() {
        Log.d(TAG, "setEndPosition");
        int endPosition_row = 1, endPosition_col = 13;
        // set end position
        for (int rowIndex = endPosition_row; rowIndex <= 3; rowIndex++) {
            for (int colIndex = endPosition_col; colIndex <= 15; colIndex++) {
                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_END;
            }
        }
    }

    private void setWayPoint() {
        Log.d(TAG, "setWayPoint: " + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ", center reference point");
        // set new way point
        int waypoint_Row_Top = wayPointPosition_Row_Center - 1;
        int waypoint_Row_Bottom = wayPointPosition_Row_Center + 1;
        int waypoint_Col_Left = wayPointPosition_Col_Center - 1;
        int waypoint_Col_Right = wayPointPosition_Col_Center + 1;
        // check if touched point is within the grid
        TV_WayPointValue = (TextView) getRootView().findViewById(R.id.TXTVW_WayPointValue);
        if (wayPointPosition_Row_Center >= 1 && wayPointPosition_Row_Center <= 20 && grid_Col >= 1 && grid_Col <= 15) {
            // check if touched point is a valid way point
            if (arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] == ARENA_GRID) {
                // touched point is valid, check if paint points within the grid
                if (waypoint_Row_Top < 1 || waypoint_Row_Bottom > grid_Row || waypoint_Col_Left < 1 || waypoint_Col_Right > grid_Col) {
                    // paint points outside the grid
                    showToast("Unable to set way point on (" + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ")");
                    if (wayPointPosition_Row_Center != 0 && wayPointPosition_Col_Center != 0) {
                        wayPointPosition_Row_Center = wayPointPosition_Col_Center = 0;  // reset value for way point
                    }
                    TV_WayPointValue.setText("0,0");
                    TV_WayPointValue.setTextColor(Color.parseColor("#808080"));
                } else {
                    // paint points within the grid
                    for (int rowIndex = waypoint_Row_Top; rowIndex <= waypoint_Row_Bottom; rowIndex++) {
                        for (int colIndex = waypoint_Col_Left; colIndex <= waypoint_Col_Right; colIndex++) {
                            if (arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_GRID) {
                                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_WAYPOINT; // set way point
                            }
                        }
                    }
                    TV_WayPointValue.setText(wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center);
                    TV_WayPointValue.setTextColor(Color.parseColor("#a76dff"));
                }
            } else {
                // touched point is not valid, reset
                showToast("Unable to set way point on (" + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ")");
                if (wayPointPosition_Row_Center != 0 && wayPointPosition_Col_Center != 0) {
                    wayPointPosition_Row_Center = wayPointPosition_Col_Center = 0;  // reset value for way point
                }
                TV_WayPointValue.setText("0,0");
                TV_WayPointValue.setTextColor(Color.parseColor("#808080"));

            }
        }
    }

    private void removeWayPoint() {
        Log.d(TAG, "removeWayPoint: " + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ", center reference point");
        //  remove existing way points
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                if (arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_GRID_WAYPOINT) {
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID;
                }
            }
        }
        TV_WayPointValue = (TextView) getRootView().findViewById(R.id.TXTVW_WayPointValue);
        TV_WayPointValue.setText("0,0");
        TV_WayPointValue.setTextColor(Color.parseColor("#808080"));
    }

    private void setWayPointReached() {
        Log.d(TAG, "setWayPointReached: " + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ", center reference point");
        // set new way point
        int waypoint_Row_Top = wayPointPosition_Row_Center - 1;
        int waypoint_Row_Bottom = wayPointPosition_Row_Center + 1;
        int waypoint_Col_Left = wayPointPosition_Col_Center - 1;
        int waypoint_Col_Right = wayPointPosition_Col_Center + 1;
        for (int rowIndex = waypoint_Row_Top; rowIndex <= waypoint_Row_Bottom; rowIndex++) {
            for (int colIndex = waypoint_Col_Left; colIndex <= waypoint_Col_Right; colIndex++) {
                if (robotPosition_Row_Center == wayPointPosition_Row_Center &&
                        robotPosition_Col_Center == wayPointPosition_Col_Center) {
                    setRobotPosition();
                    setRobotDirection();
                } else {
                    Log.d(TAG, "arenaInfo[" + rowIndex + "][" + colIndex + "]:" + arenaInfo[rowIndex - 1][colIndex - 1]);
                    if (arenaInfo[rowIndex - 1][colIndex - 1] != ARENA_ROBOT_POSITION &&
                            arenaInfo[rowIndex - 1][colIndex - 1] != ARENA_ROBOT_DIRECTION)
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT; // set way point reached
                }

            }
        }
    }

    private void setRobotPosition() {
        Log.d(TAG, "setRobotPosition: " + robotPosition_Row + "," + robotPosition_Col + ", top left reference point");
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

    private void setRobotDirection() {
        Log.d(TAG, "setRobotDirection");
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

    private void setTravelPath() {
        Log.d(TAG, "setTravelPath");
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

    public void setSaveState_ArenaInfo(ArrayList<int[][]> saveState_ArenaInfo) {
        this.saveState_ArenaInfo = saveState_ArenaInfo;
    }

    public void setSaveState_TravelInfo(ArrayList<int[][]> saveState_TravelInfo) {
        this.saveState_TravelInfo = saveState_TravelInfo;
    }

    private void checkWayPointReached() {
        if (robotPosition_Row_Center != 0 && robotPosition_Col_Center != 0) {
            if (robotPosition_Row_Center == wayPointPosition_Row_Center && robotPosition_Col_Center == wayPointPosition_Col_Center) {
                Log.d(TAG, "checkWayPointReached: Way Point Reached");
                isWayPointReached = true;
                TV_WayPointValue = (TextView) getRootView().findViewById(R.id.TXTVW_WayPointValue);
                TV_WayPointValue.setTextColor(Color.GREEN);
            }
        }
    }
}
