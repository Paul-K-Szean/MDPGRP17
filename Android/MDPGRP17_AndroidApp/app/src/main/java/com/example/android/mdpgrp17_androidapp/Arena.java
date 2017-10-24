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

import java.util.ArrayList;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GAME_MODE_SWIPE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_ENDPOSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_ENDPOSITION_UNDISCOVERED;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_OBSTACLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_STARTPOSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_STARTPOSITION_UNDISCOVERED;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GRID_WAYPOINT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_DIRECTION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_DIRECTION_WITH_ENDPOSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_DIRECTION_WITH_STARTPOSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_DIRECTION_WITH_WAYPOINT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_EXPLOREDPATH;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_POSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_POSITION_WITH_ENDPOSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_POSITION_WITH_STARTPOSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_POSITION_WITH_WAYPOINT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_TRAVELPATH;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_TRAVELPATH_WITH_ENDPOSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_TRAVELPATH_WITH_STARTPOSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_FORWARD;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_REVERSE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_ROTATELEFT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_ROTATERIGHT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.SWIPE_MINIMUM_DISTANCE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.TOUCHMODE_SETROBOTPOSITION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.TOUCHMODE_SETWAYPOINT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.TOUCHMODE_SWIPE;
import static com.example.android.mdpgrp17_androidapp.R.color.color_Arena_RobotPosition;
import static com.example.android.mdpgrp17_androidapp.R.color.color_Arena_WayPoint;
import static java.util.Arrays.deepToString;

/**
 * Created by szean on 8/9/2017.
 */

public class Arena extends View {
    private static final String TAG = "Arena";
    // GUI objects
    private RelativeLayout RLO_ArenaGrid;
    private TextView TXTVW_ControlMode;
    private TextView TXTVW_MDFString;
    private TextView TXTVW_RobotStatusValue;
    private TextView TXTVW_WayPointValue;
    private TextView TXTVW_WayPoint;

    // Arena objects/variables
    private int grid_Row, grid_Col, grid_Size;
    private int startPosition_Row_Center = 19, startPosition_Col_Center = 2, endPosition_Row_Center = 2, endPosition_Col_Center = 14;
    private int wayPointPosition_Row_Center, wayPointPosition_Col_Center;   // center of way point
    private int robotPosition_Row;
    private int robotPosition_Col;               // top left reference point from arena info
    private int robotPosition_Row_Center, robotPosition_Col_Center; // center reference point robotPosition_Row, robotPosition_Col
    private int robotDirection;                                     // 0, 90, 180, 270
    private boolean isUpDown, isWayPointReached = false;            // 0 || 180 for isUpDown
    private boolean isRobotAtStartPosition = false, isRobotAtEndPosition = false;
    private int touchMode;
    private int[] arenaInfoString = new int[300];   // to store arena info
    private int[][] arenaInfo = new int[20][15], travelInfo = new int[20][15], displayInfo = new int[20][15], exploredPathInfo, obstacleInfo = new int[20][15];
    private Boolean isMapMode_Auto = true; // always auto update map
    private int saveStateIndex_Pause, saveStateIndex_InArray, saveStateIndex_WayPointWasSetAt;   // negative to ensure default value, use to transverse the array of the saved state
    private ArrayList<ArenaSaveState> saveStateArrayList;

    // Paint objects
    private Paint paint;
    private int X;
    private int Y;
    private int _X;
    private int _Y;
    // Thread object
    private ArenaThread arenaThread;
    private float downX, downY, upX, upY;

    public Arena(Context context, RelativeLayout RLO_ArenaGrid, int wayPointPosition_Col, int wayPointPosition_Row) {
        super(context);
        Log.d(TAG, "Arena: Old Way Point" + wayPointPosition_Col + ", " + wayPointPosition_Row);
        // the GUI layout of the arena grid
        this.RLO_ArenaGrid = RLO_ArenaGrid;
        this.saveStateArrayList = new ArrayList<>();
        this.saveStateIndex_Pause = -1;     // negative to ensure default value, use to save at which index the manual map mode is set
        this.saveStateIndex_InArray = -1;   // negative to ensure default value, use to transverse the array of the saved state
        this.saveStateIndex_WayPointWasSetAt = -1;
        // to paint the grid with styles
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setDither(true);
        // a thread to update the grid
        this.arenaThread = new ArenaThread(this);
        this.arenaThread.startThread();
        this.touchMode = TOUCHMODE_SETWAYPOINT;
        if (wayPointPosition_Col > 0 && wayPointPosition_Row > 0) {
            wayPointPosition_Row_Center = wayPointPosition_Row;
            wayPointPosition_Col_Center = wayPointPosition_Col;
        } else {
            this.wayPointPosition_Row_Center = -1;
            this.wayPointPosition_Col_Center = -1;
            removeWayPoint();
        }
        exploredPathInfo = new int[20][15]; // cannot always renew explored array. so initialize once
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int touchAction = motionEvent.getAction();
        final MainActivity mainActivity = (MainActivity) getContext();
        if (touchMode == TOUCHMODE_SETWAYPOINT) {
            if (mainActivity.getIsWayPointLocked()) {
                Log.d(TAG, "onTouchEvent: TOUCHMODE_SETWAYPOINT: way point was locked");
                // way point was locked
                showToast_Short("Unlock way point to reselect");
            } else {
                if (touchAction == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "onTouchEvent: TOUCHMODE_SETWAYPOINT: way point was not locked");
                    int touched_Col = (int) (motionEvent.getX() / grid_Size);
                    int touched_Row = (int) (motionEvent.getY() / grid_Size);

                    if (touched_Row == wayPointPosition_Row_Center && touched_Col == wayPointPosition_Col_Center) {
                        wayPointPosition_Col_Center = touched_Col;
                        wayPointPosition_Row_Center = touched_Row;
                        removeWayPoint();   // remove existing way points
                    } else { // remove existing way points and add new way point
                        removeWayPoint();
                        wayPointPosition_Col_Center = touched_Col;
                        wayPointPosition_Row_Center = touched_Row;
                        drawWayPoint();     // @onTouchEvent set way point
                    }
                    saveArenaState(); // save way point into new state
                    invalidate();
                }
            }

        } else if (touchMode == TOUCHMODE_SETROBOTPOSITION) {
            if (touchAction == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "onTouchEvent: TOUCHMODE_SETROBOTPOSITION grid_Size: " + grid_Size);
                for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
                    for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                        if (arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_POSITION ||
                                arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_POSITION_WITH_WAYPOINT ||
                                arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_POSITION_WITH_STARTPOSITION ||
                                arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_POSITION_WITH_ENDPOSITION ||
                                arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_DIRECTION ||
                                arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_DIRECTION_WITH_WAYPOINT ||
                                arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_DIRECTION_WITH_STARTPOSITION ||
                                arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_DIRECTION_WITH_ENDPOSITION
                                ) {
                            arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID; // remove existing robot position
                        }
                    }
                }
                // getting robot position from touched points
                robotPosition_Col_Center = (int) (motionEvent.getX() / grid_Size);
                robotPosition_Row_Center = (int) (motionEvent.getY() / grid_Size);
                robotPosition_Col = robotPosition_Col_Center - 1;
                robotPosition_Row = robotPosition_Row_Center - 1;
                robotDirection = 0;


                drawWayPoint();         // @onTouchEvent redraw the way point
                drawStartPosition();    // @onTouchEvent redraw the start position
                drawEndPosition();      // @onTouchEvent redraw the end position
                drawRobotPosition();
                drawRobotDirection();
                saveArenaState();       // @onTouchEvent save new robot position
            }
        } else if (touchMode == TOUCHMODE_SWIPE) {
            Log.d(TAG, "onTouchEvent: TOUCHMODE_SWIPE");
            // locked, cannot touch screen, check if in swipe mode
            if (mainActivity.getGameMode() == ARENA_GAME_MODE_SWIPE) {
                switch (touchAction) {
                    case MotionEvent.ACTION_DOWN: {
                        downX = motionEvent.getX();
                        downY = motionEvent.getY();
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        upX = motionEvent.getX();
                        upY = motionEvent.getY();

                        float deltaX = downX - upX;
                        float deltaY = downY - upY;

                        // swipe left/right?
                        if (Math.abs(deltaX) > SWIPE_MINIMUM_DISTANCE) {
                            // left or right
                            if (deltaX < 0) {
                                Log.i(TAG, "Swipe Right");
                                mainActivity.moveRobot(CMD_ROTATERIGHT);
                                return true;
                            }
                            if (deltaX > 0) {
                                Log.i(TAG, "Swipe Left");
                                mainActivity.moveRobot(CMD_ROTATELEFT);
                                return true;
                            }
                        } else {
                            Log.i(TAG, "Swipe was only " + Math.abs(deltaX) + " long horizontally, need at least " + SWIPE_MINIMUM_DISTANCE);
                            // return false; // We don't consume the event
                        }

                        // swipe up/down?
                        if (Math.abs(deltaY) > SWIPE_MINIMUM_DISTANCE) {
                            // top or down
                            if (deltaY < 0) {
                                Log.i(TAG, "Swipe Down");
                                mainActivity.moveRobot(CMD_REVERSE);
                                return true;
                            }
                            if (deltaY > 0) {
                                Log.i(TAG, "Swipe Up");
                                mainActivity.moveRobot(CMD_FORWARD);
                                return true;
                            }
                        } else {
                            Log.i(TAG, "Swipe was only " + Math.abs(deltaY) + " long vertically, need at least " + SWIPE_MINIMUM_DISTANCE);
                            // return false; // We don't consume the event
                        }
                        return false;
                    }
                }
            } else {
                showToast_Short("Way point is locked");
            }
        }

        return true;
    }

    public void update() {
        // called every period (200milisec) because arena thread is running

        if (isMapMode_Auto) {
            if (saveStateArrayList.size() > 0) {
                robotPosition_Row_Center = saveStateArrayList.get(saveStateArrayList.size() - 1).getRobotPosition_Row_Center();
                robotPosition_Col_Center = saveStateArrayList.get(saveStateArrayList.size() - 1).getRobotPosition_Col_Center();
                displayInfo = saveStateArrayList.get(saveStateArrayList.size() - 1).getArenaInfo();
            }
            if (!isWayPointReached) {
                checkWayPointReached();
            }
            checkTravelPath();
        } else {
            robotPosition_Row_Center = saveStateArrayList.get(saveStateIndex_InArray).getRobotPosition_Row_Center();
            robotPosition_Col_Center = saveStateArrayList.get(saveStateIndex_InArray).getRobotPosition_Col_Center();

            displayInfo = saveStateArrayList.get(saveStateIndex_InArray).getArenaInfo();
            if (!saveStateArrayList.get(saveStateIndex_InArray).getWayPointReached()) {
                checkWayPointReached();
            }
        }

    }

    @Override
    public void onDraw(Canvas canvas) {
        try {
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
                if (arenaInfoValue == ARENA_GRID_STARTPOSITION) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_StartPosition, canvas);
                }
                // paint end position
                if (arenaInfoValue == ARENA_GRID_ENDPOSITION) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_EndPosition, canvas);
                }
                // paint start position un-discover
                if (arenaInfoValue == ARENA_GRID_STARTPOSITION_UNDISCOVERED) {
                    drawCellWithBorder_StartPositionUndiscovered(rowIndex, colIndex, grid_Size, R.color.color_Arena_Grid, canvas);
                }
                // paint end position un-discover
                if (arenaInfoValue == ARENA_GRID_ENDPOSITION_UNDISCOVERED) {
                    drawCellWithBorder_EndPositionUndiscovered(rowIndex, colIndex, grid_Size, R.color.color_Arena_Grid, canvas);
                }
                // draw obstacle
                if (arenaInfoValue == ARENA_GRID_OBSTACLE) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_Obstacle, canvas);
                }
                // draw way point
                if (arenaInfoValue == ARENA_GRID_WAYPOINT) {
                    drawCell(rowIndex, colIndex, grid_Size, color_Arena_WayPoint, canvas);
                }
                // draw robot position
                if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                    drawCell(rowIndex, colIndex, grid_Size, color_Arena_RobotPosition, canvas);
                }
                if (arenaInfoValue == ARENA_ROBOT_POSITION_WITH_WAYPOINT) {
                    drawCellWithBorder_WayPoint(rowIndex, colIndex, grid_Size, R.color.color_Arena_WayPointReached, canvas);
                }
                if (arenaInfoValue == ARENA_ROBOT_POSITION_WITH_STARTPOSITION) {
                    drawCellWithBorder_StartPosition(rowIndex, colIndex, grid_Size, color_Arena_RobotPosition, canvas);
                }
                if (arenaInfoValue == ARENA_ROBOT_POSITION_WITH_ENDPOSITION) {
                    drawCellWithBorder_EndPosition(rowIndex, colIndex, grid_Size, color_Arena_RobotPosition, canvas);
                }
                // draw robot direction
                if (arenaInfoValue == ARENA_ROBOT_DIRECTION) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotDirection, canvas);
                }
                if (arenaInfoValue == ARENA_ROBOT_DIRECTION_WITH_WAYPOINT) {
                    drawCellWithBorder_WayPoint(rowIndex, colIndex, grid_Size, R.color.color_Arena_WayPointReached, canvas);
                }
                if (arenaInfoValue == ARENA_ROBOT_DIRECTION_WITH_STARTPOSITION) {
                    drawCellWithBorder_StartPosition(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotDirection, canvas);
                }
                if (arenaInfoValue == ARENA_ROBOT_DIRECTION_WITH_ENDPOSITION) {
                    drawCellWithBorder_EndPosition(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotDirection, canvas);
                }
                // draw travel path
                if (arenaInfoValue == ARENA_ROBOT_TRAVELPATH) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotTravelPath, canvas);
                }
                // draw travel path with start position
                if (arenaInfoValue == ARENA_ROBOT_TRAVELPATH_WITH_STARTPOSITION) {
                    drawCellWithBorder_StartPosition(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotTravelPath, canvas);
                }
                // draw travel path with end position
                if (arenaInfoValue == ARENA_ROBOT_TRAVELPATH_WITH_ENDPOSITION) {
                    drawCellWithBorder_EndPosition(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotTravelPath, canvas);
                }
                // draw way point reached
                if (arenaInfoValue == ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT) {
                    drawCellWithBorder_WayPoint(rowIndex, colIndex, grid_Size, R.color.color_Arena_WayPointReached, canvas);
                }
                // draw explored path
                if (arenaInfoValue == ARENA_ROBOT_EXPLOREDPATH) {
                    drawCell(rowIndex, colIndex, grid_Size, R.color.color_Arena_RobotExploredPath, canvas);
                }

                paint.setColor(Color.BLACK);
                paint.setTextSize(12);
                canvas.drawText((colIndex - 1) + "," + (21 - rowIndex - 1),
                        (colIndex * grid_Size - grid_Size / 2) + 6,
                        ((rowIndex * grid_Size - grid_Size / 2) + 24), paint);
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

//        paint.setColor(Color.BLACK);
//        paint.setTextSize(12);
//        canvas.drawText((col - 1) + "," + (21 - row - 1), X + 6, (Y + 24), paint);
    }

    // Draw Cell With Border
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
        paint.setColor(ContextCompat.getColor(getContext(), color_Arena_WayPoint));
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

    public void drawCellWithBorder_StartPositionUndiscovered(int row, int col, int gridSize, int c, Canvas canvas) {
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

    public void drawCellWithBorder_EndPositionUndiscovered(int row, int col, int gridSize, int c, Canvas canvas) {
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
        drawEndPosition();      // @setupNakedGrid
        drawWayPoint();         // @setupNakedGrid
        drawRobotPosition();    // @setupNakedGrid
        drawRobotDirection();   // @setupNakedGrid
        drawTravelPath();       // @setupNakedGrid
        saveArenaState();       // @setupNakedGrid save initial setup
    }

    // Debug - not used
    public void printArenaInfo() {
        Log.d(TAG, "--------------------Start of Print Arena Info--------------------");
        Log.d(TAG, "--------------------printArenaInfo--------------------");
        String printArenaInfoString = deepToString(arenaInfo).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
        String[] printArenaInfoStringArray = printArenaInfoString.split(";");
        for (String value : printArenaInfoStringArray) {
            Log.d(TAG, "printArenaInfo: arenaInfo: " + value);
        }
        Log.d(TAG, "--------------------printTravelInfo--------------------");
        String printTravelInfoString = deepToString(travelInfo).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
        String[] printTravelInfoArray = printTravelInfoString.split(";");
        for (String value : printTravelInfoArray) {
            Log.d(TAG, "printTravelInfo: travelInfo: " + value);
        }
        Log.d(TAG, "--------------------printObstacleInfo--------------------");
        String printObstacleInfoString = deepToString(obstacleInfo).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
        String[] printObstacleInfoArray = printObstacleInfoString.split(";");
        for (String value : printObstacleInfoArray) {
            Log.d(TAG, "printObstacleInfo: obstacleInfo: " + value);
        }
        Log.d(TAG, "--------------------printExploredPathInfo--------------------");
        String printExploredPathInfoString = deepToString(exploredPathInfo).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
        String[] printExploredPathInfoArray = printExploredPathInfoString.split(";");
        for (String value : printExploredPathInfoArray) {
            Log.d(TAG, "printExploredPathInfo: ExploredPathInfo: " + value);
        }
        Log.d(TAG, "--------------------saveArenaState---------------------");
        Log.d(TAG, "saveArenaState: Index_Pause: " + (saveStateIndex_Pause) + ", Index_InArray: " + (saveStateIndex_InArray) +
                ", arrListSize: " + saveStateArrayList.size() + ", MapMode: " +
                isMapMode_Auto + ", WayPointReached: " + saveStateArrayList.get(saveStateArrayList.size() - 1).getWayPointReached());
        for (int index = 0; index < saveStateArrayList.size(); index++) {
            int[][] saveArenaState = saveStateArrayList.get(index).getArenaInfo();
            String saveArenaStateString = deepToString(saveArenaState).replace("[[", "[").replace("]]", "]").replace("], ", "];").trim();
            String[] saveArenaStateArray = saveArenaStateString.split(";");
            for (String value : saveArenaStateArray) {
                Log.d(TAG, "saveArenaState: arenaInfo: " + value);
            }
            Log.d(TAG, "------------------------------------------------------- Count: " + index);
        }
        Log.d(TAG, "--------------------End of Print Arena Info--------------------");

    }

    public void saveArenaState() {
        Log.d(TAG, "********************************SAVE ARENA STATE********************************");
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
                isWayPointReached, robotPosition_Row_Center, robotPosition_Col_Center, robotDirection);
        saveStateArrayList.add(arenaSaveState);
//        printArenaInfo();
    }

    private void showToast_Short(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Check Methods
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
        int checkWayPointArray[][];
        robotPosition_Row_Center = robotPosition_Col_Center = 0;
        if (saveStateArrayList.size() != 0) {
            if (isMapMode_Auto) {
                checkWayPointArray = saveStateArrayList.get(saveStateArrayList.size() - 1).getArenaInfo();
            } else {
                checkWayPointArray = saveStateArrayList.get(saveStateIndex_InArray).getArenaInfo();
            }
            robotPosition_Row_Center = saveStateArrayList.get(saveStateArrayList.size() - 1).getRobotPosition_Row_Center();
            robotPosition_Col_Center = saveStateArrayList.get(saveStateArrayList.size() - 1).getRobotPosition_Col_Center();
            for (int rowIndex = robotPosition_Row; rowIndex <= robotPosition_Row + 2; rowIndex++) {
                for (int colIndex = robotPosition_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                    if (rowIndex <= grid_Row && colIndex <= grid_Col) {
                        int checkWayPointValue = checkWayPointArray[rowIndex - 1][colIndex - 1];
                        // Log.d(TAG, "checkWayPointReached: checkWayPointValue: " + checkWayPointValue);
                        if (checkWayPointValue == ARENA_GRID_WAYPOINT ||
                                checkWayPointValue == ARENA_ROBOT_DIRECTION_WITH_WAYPOINT ||
                                checkWayPointValue == ARENA_ROBOT_POSITION_WITH_WAYPOINT) {
                            Log.d(TAG, "checkWayPointReached: Way Point Reached");
                            isWayPointReached = true;
                            // showToast_Short("Way Point Reach!");
                            break;
                        }
                    }
                }
            }
        }
    }

    private void checkTravelPath() {
        // start position
        int startPosition_Row_Top = startPosition_Row_Center - 1;
        int startPosition_Row_Bottom = startPosition_Row_Center + 1;
        int startPosition_Col_Left = startPosition_Col_Center - 1;
        int startPosition_Col_Right = startPosition_Col_Center + 1;
        for (int index = 0; index < saveStateArrayList.size(); index++) {
            int[][] arenaInfor = saveStateArrayList.get(index).getArenaInfo();
            for (int rowIndex = startPosition_Row_Top; rowIndex <= startPosition_Row_Bottom; rowIndex++) {
                for (int colIndex = startPosition_Col_Left; colIndex <= startPosition_Col_Right; colIndex++) {
                    if (arenaInfor[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_TRAVELPATH) {
                        arenaInfor[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH_WITH_STARTPOSITION;
                    }
                }
            }
        }

        // end position
        int endPosition_Row_Top = endPosition_Row_Center - 1;
        int endPosition_Row_Bottom = endPosition_Row_Center + 1;
        int endPosition_Col_Left = endPosition_Col_Center - 1;
        int endPosition_Col_Right = endPosition_Col_Center + 1;
        for (int index = 0; index < saveStateArrayList.size(); index++) {
            int[][] arenaInfor = saveStateArrayList.get(index).getArenaInfo();
            for (int rowIndex = endPosition_Row_Top; rowIndex <= endPosition_Row_Bottom; rowIndex++) {
                for (int colIndex = endPosition_Col_Left; colIndex <= endPosition_Col_Right; colIndex++) {
                    if (arenaInfor[rowIndex - 1][colIndex - 1] == ARENA_ROBOT_TRAVELPATH) {
                        arenaInfor[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH_WITH_ENDPOSITION;
                    }
                }
            }
        }
    }

    private void checkRobotDirectionOnAnyThing(int robotDirection_Row, int robotDirection_Col) {
        robotDirection_Row -= 1; // 0 based
        robotDirection_Col -= 1; // 0 based
        if (robotDirection_Row >= 0 &&
                robotDirection_Row < grid_Row &&
                robotDirection_Col >= 0 &&
                robotDirection_Col < grid_Col) {
            int arenaInfoValue = arenaInfo[robotDirection_Row][robotDirection_Col];
            // to draw border on start/end position, redraw way point
            if (arenaInfoValue == ARENA_ROBOT_POSITION_WITH_STARTPOSITION) {
//            Log.d(TAG, "drawRobotDirection: checkRobotDirectionOnAnyThing: " + robotPosition_Row + "," + robotPosition_Col + ": at ARENA_GRID_STARTPOSITION");
                arenaInfo[robotDirection_Row][robotDirection_Col] = ARENA_ROBOT_DIRECTION_WITH_STARTPOSITION;
            } else if (arenaInfoValue == ARENA_ROBOT_POSITION_WITH_ENDPOSITION) {
//            Log.d(TAG, "drawRobotDirection: checkRobotDirectionOnAnyThing: " + robotPosition_Row + "," + robotPosition_Col + ": at ARENA_GRID_ENDPOSITION");
                arenaInfo[robotDirection_Row][robotDirection_Col] = ARENA_ROBOT_DIRECTION_WITH_ENDPOSITION;
            } else if (arenaInfoValue == ARENA_ROBOT_POSITION_WITH_WAYPOINT) {
//            Log.d(TAG, "drawRobotDirection: checkRobotDirectionOnAnyThing: " + robotPosition_Row + "," + robotPosition_Col + ": at ARENA_ROBOT_POSITION_WITH_WAYPOINT");
                arenaInfo[robotDirection_Row][robotDirection_Col] = ARENA_ROBOT_DIRECTION_WITH_WAYPOINT;
            } else if (arenaInfoValue == ARENA_ROBOT_POSITION) {
//            Log.d(TAG, "drawRobotDirection: checkRobotDirectionOnAnyThing: " + robotPosition_Row + "," + robotPosition_Col + ": at ARENA_ROBOT_POSITION");
                arenaInfo[robotDirection_Row][robotDirection_Col] = ARENA_ROBOT_DIRECTION;
            }
        }
    }

    // Getters and Setters
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

    public void setSaveStateArrayList(ArrayList<ArenaSaveState> saveStateArrayList) {
        this.saveStateArrayList = saveStateArrayList;
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

    public int getTouchMode() {
        Log.d(TAG, "getTouchMode");
        return touchMode;
    }

    public void setTouchMode(int touchMode) {
        Log.d(TAG, "setTouchMode");
        this.touchMode = touchMode;
    }

    public int getRobotPosition_Row() {
        return robotPosition_Row;
    }

    public void setRobotPosition_Row(int robotPosition_Row) {
        this.robotPosition_Row += robotPosition_Row;
    }

    public int getRobotPosition_Col() {
        return robotPosition_Col;
    }

    public void setRobotPosition_Col(int robotPosition_Col) {
        this.robotPosition_Col += robotPosition_Col;
    }

    public void setRobotDirection(int robotDirection) {
        this.robotDirection = robotDirection;
    }

    private void setObstacle(String content) {
        Log.d(TAG, "setObstacle: " + content);
        String[] contentArray = content.replace("EXPLORED,", "").split(",");     // raw string
        int[][] contentArrayAlgo = new int[15][20];     // convert into algo array
        int[][] contentArrayAndroid = new int[20][15];  // convert into android array
        obstacleInfo = new int[20][15];
        int rowIndex, colIndex;
        int index = 0;
        Log.d(TAG, "setObstacle: Converting into algo array");
        for (rowIndex = 0; rowIndex < grid_Col; rowIndex++) {
            for (colIndex = 0; colIndex < grid_Row; colIndex++, index++) {
                contentArrayAlgo[rowIndex][colIndex] = Integer.parseInt(contentArray[index]);
                contentArrayAndroid[colIndex][rowIndex] = contentArrayAlgo[rowIndex][colIndex];
            }
        }
        Log.d(TAG, "setObstacle: Converting into android array");
        int temp[];
        for (int rowIndex1 = 0; rowIndex1 < 10; rowIndex1++) {  // swap rows
            temp = contentArrayAndroid[contentArrayAndroid.length - rowIndex1 - 1];    // first item row value
            contentArrayAndroid[contentArrayAndroid.length - rowIndex1 - 1] = contentArrayAndroid[rowIndex1];
            contentArrayAndroid[rowIndex1] = temp;
        }
        Log.d(TAG, "setObstacle: Setting obstacle");
        for (int rowIndex2 = 1; rowIndex2 <= grid_Row; rowIndex2++) {
            for (int colIndex2 = 1; colIndex2 <= grid_Col; colIndex2++) {
                int resultInfoValue = Integer.valueOf(contentArrayAndroid[rowIndex2 - 1][colIndex2 - 1]);
                if (resultInfoValue == 2) {
                    Log.d(TAG, "setObstacle: obstacle found at " + colIndex2 + ", " + rowIndex2);
                    obstacleInfo[rowIndex2 - 1][colIndex2 - 1] = ARENA_GRID_OBSTACLE;
                }
            }
        }
    }

    // Draw Methods
    private void drawStartPosition() {
        Log.d(TAG, "drawStartPosition");
        int startPosition_row = 18, startPosition_col = 1;
        // set start position
        for (int rowIndex = startPosition_row; rowIndex <= 20; rowIndex++) {
            for (int colIndex = startPosition_col; colIndex <= 3; colIndex++) {
                int arenaInfoValue = arenaInfo[rowIndex - 1][colIndex - 1];
                if (arenaInfoValue == ARENA_ROBOT_EXPLOREDPATH) {
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_STARTPOSITION;
                } else {
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_STARTPOSITION_UNDISCOVERED;
                }
            }
        }
    }

    private void drawEndPosition() {
        Log.d(TAG, "drawEndPosition");
        int endPosition_row = 1, endPosition_col = 13;
        // set end position
        for (int rowIndex = endPosition_row; rowIndex <= 3; rowIndex++) {
            for (int colIndex = endPosition_col; colIndex <= 15; colIndex++) {
                int arenaInfoValue = arenaInfo[rowIndex - 1][colIndex - 1];
                if (arenaInfoValue == ARENA_ROBOT_EXPLOREDPATH) {
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_ENDPOSITION;
                } else {
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_ENDPOSITION_UNDISCOVERED;
                }
            }
        }
    }

    public void drawWayPoint() {
        Log.d(TAG, "drawWayPoint: " + wayPointPosition_Col_Center + "," + wayPointPosition_Row_Center);
        TXTVW_WayPointValue = (TextView) getRootView().findViewById(R.id.TXTVW_WayPointValue);
        TXTVW_WayPoint = (TextView) getRootView().findViewById(R.id.TXTVW_WayPoint);
        try {
            if (wayPointPosition_Row_Center > 0 && wayPointPosition_Col_Center > 0) {
                int arenaInfoValue = arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1];
                if (arenaInfoValue == ARENA_GRID ||
                        arenaInfoValue == ARENA_ROBOT_EXPLOREDPATH) {
                    arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] = ARENA_GRID_WAYPOINT;
                    saveStateIndex_WayPointWasSetAt = saveStateArrayList.size() - 1;
                    Log.d(TAG, "drawWayPoint: saveStateIndex_WayPointWasSetAt: " + saveStateIndex_WayPointWasSetAt);
                }

                int displayRow = 21 - wayPointPosition_Row_Center;
                TXTVW_WayPointValue.setText((wayPointPosition_Col_Center - 1) + "," + (displayRow - 1));
            }


            if (isMapMode_Auto) {
                // set the GUI text color for the latest state
                if (isWayPointReached) {
                    TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                } else {
                    TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_WayPoint));
                    TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), color_Arena_WayPoint));
                }
            } else {
                // set the GUI text color for each arena info in the saveState_ArenaInfo
                if (saveStateArrayList.get(saveStateIndex_InArray).getWayPointReached()) {
                    TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                } else {
                    TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_WayPoint));
                    TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), color_Arena_WayPoint));
                }
            }
        } catch (IndexOutOfBoundsException ex_IndexOutOfBound) {
            Log.d(TAG, "drawWayPoint: error: " + ex_IndexOutOfBound.getMessage());
            TXTVW_WayPointValue.setText("0,0");
            TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), R.color.color_System_Default));
            TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_System_Default));
            showToast_Short("Unable to set way point on (" + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ")");

        }


    }

    private void removeWayPoint() {
        Log.d(TAG, "removeWayPoint: " + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ", center reference point");
        try {
            if (wayPointPosition_Row_Center != 0 && wayPointPosition_Col_Center != 0) {
                int oldValue = arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1];
                if (oldValue != ARENA_GRID && oldValue != ARENA_GRID_WAYPOINT) {
                    arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] = oldValue;
                } else {
                    arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] = ARENA_GRID;
                }
            }
            wayPointPosition_Row_Center = wayPointPosition_Col_Center = -1;
            TXTVW_WayPointValue = (TextView) getRootView().findViewById(R.id.TXTVW_WayPointValue);
            TXTVW_WayPointValue.setText(wayPointPosition_Col_Center + "," + wayPointPosition_Row_Center);
            TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_WayPoint));
            TXTVW_WayPoint = (TextView) getRootView().findViewById(R.id.TXTVW_WayPoint);
            TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), R.color.color_Arena_WayPoint));
        } catch (Exception ex) {
            Log.d(TAG, ex == null ? "removeWayPoint: Exception was null" : ex.getMessage());
        }
    }

    private void drawWayPointReached() {
        Log.d(TAG, "drawWayPointReached: " + wayPointPosition_Row_Center + "," + wayPointPosition_Col_Center + ", center reference point");
        if (isWayPointReached) {
            if (arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] == ARENA_ROBOT_POSITION) {
                arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] = ARENA_ROBOT_POSITION_WITH_WAYPOINT;
            } else if (arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] == ARENA_ROBOT_DIRECTION) {
                arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] = ARENA_ROBOT_DIRECTION_WITH_WAYPOINT;
            } else if (arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] == ARENA_ROBOT_TRAVELPATH) {
                arenaInfo[wayPointPosition_Row_Center - 1][wayPointPosition_Col_Center - 1] = ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT;
            }

            TXTVW_WayPointValue = (TextView) getRootView().findViewById(R.id.TXTVW_WayPointValue);
            TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
            TXTVW_WayPoint = (TextView) getRootView().findViewById(R.id.TXTVW_WayPoint);
            TXTVW_WayPoint.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
        }
    }

    private void drawRobotPosition() {
        Log.d(TAG, "drawRobotPosition: Y,X: " + robotPosition_Row + "," + robotPosition_Col + ": top left reference point.");
        // set robot position, check if robot position is within grid
        if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
            for (int rowIndex = robotPosition_Row; rowIndex <= robotPosition_Row + 2; rowIndex++) {
                if (rowIndex <= grid_Row)
                    for (int colIndex = robotPosition_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                        if (colIndex <= grid_Col) {
                            int arenaInfoValue = arenaInfo[rowIndex - 1][colIndex - 1];
                            if (arenaInfoValue == ARENA_GRID_STARTPOSITION) {
                                //Log.d(TAG, "drawRobotPosition: Y,X: " + (rowIndex) + "," + (colIndex) + ": " + arenaInfoValue + " , top left reference point. ARENA_GRID_STARTPOSITION");
                                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_POSITION_WITH_STARTPOSITION;
                            } else if (arenaInfoValue == ARENA_GRID_ENDPOSITION) {
                                //Log.d(TAG, "drawRobotPosition: Y,X: " + (rowIndex) + "," + (colIndex) + ": " + arenaInfoValue + " , top left reference point. ARENA_GRID_ENDPOSITION");
                                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_POSITION_WITH_ENDPOSITION;
                            } else if (arenaInfoValue == ARENA_GRID_WAYPOINT) {
                                //Log.d(TAG, "drawRobotPosition: Y,X: " + (rowIndex) + "," + (colIndex) + ": " + arenaInfoValue + " , top left reference point. ARENA_GRID_WAYPOINT");
                                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_POSITION_WITH_WAYPOINT;
                            } else if (arenaInfoValue == ARENA_ROBOT_POSITION) {
                                if (robotPosition_Row_Center == wayPointPosition_Row_Center && robotPosition_Col_Center == wayPointPosition_Col_Center) {
                                    //Log.d(TAG, "drawRobotPosition: Y,X: " + (rowIndex) + "," + (colIndex) + ": " + arenaInfoValue + " , top left reference point. ARENA_ROBOT_POSITION Way Point");
                                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_POSITION_WITH_WAYPOINT;
                                } else {
                                    //Log.d(TAG, "drawRobotPosition: Y,X: " + (rowIndex) + "," + (colIndex) + ": " + arenaInfoValue + " , top left reference point. ARENA_ROBOT_POSITION Only");
                                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_POSITION;
                                }
                            } else {
                                //Log.d(TAG, "drawRobotPosition: Y,X: " + (rowIndex) + "," + (colIndex) + ": " + arenaInfoValue + " , top left reference point. ELSE CLAUSE");
                                arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_POSITION;
                            }
                        }
                    }
            }
        }
    }

    private void drawRobotDirection() {
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
                Log.d(TAG, "drawRobotDirection: up");
                checkRobotDirectionOnAnyThing(robotDirection_head_row, robotDirection_head_col); // 1 based array
            }
            isUpDown = true;
        }
        if (robotDirection == 90) { // right
            Log.d(TAG, "drawRobotDirection: right");
            robotDirection_head_row = robotPosition_Row + 1;
            robotDirection_head_col = robotPosition_Col + 2;
            robotDirection_body = robotPosition_Col + 1;
            if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
                checkRobotDirectionOnAnyThing(robotDirection_head_row, robotDirection_head_col); // 1 based array
            }
        }
        if (robotDirection == 180) { // down
            Log.d(TAG, "drawRobotDirection: down");
            robotDirection_head_row = robotPosition_Row + 2;
            robotDirection_head_col = robotPosition_Col + 1;
            robotDirection_body = robotPosition_Row + 1;
            if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
                checkRobotDirectionOnAnyThing(robotDirection_head_row, robotDirection_head_col); // 1 based array
            }
            isUpDown = true;
        }
        if (robotDirection == 270) { // left
            Log.d(TAG, "drawRobotDirection: left");
            robotDirection_head_row = robotPosition_Row + 1;
            robotDirection_head_col = robotPosition_Col;
            robotDirection_body = robotPosition_Col + 1;
            if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
                checkRobotDirectionOnAnyThing(robotDirection_head_row, robotDirection_head_col); // 1 based array
            }
        }

        // set robot direction body
        if (robotPosition_Row >= 1 && robotPosition_Row <= grid_Row && robotPosition_Col >= 1 && robotPosition_Col <= grid_Col) {
            if (isUpDown) {
                for (int colIndexCount = robotPosition_Col; colIndexCount <= robotPosition_Col + 2; colIndexCount++) {
                    if (colIndexCount != robotPosition_Col + 1) {
                        checkRobotDirectionOnAnyThing(robotDirection_body, colIndexCount); // 1 based
                    }
                }
            } else {
                for (int rowIndexCount = robotPosition_Row; rowIndexCount <= robotPosition_Row + 2; rowIndexCount++) {
                    if (rowIndexCount != robotPosition_Row + 1) {
                        checkRobotDirectionOnAnyThing(rowIndexCount, robotDirection_body); // 1 based
                    }
                }
            }
        }
    }

    private void drawTravelPath() {
        Log.d(TAG, "drawTravelPath");

        // save previous position of robot into travel info array
        for (int paintRow = robotPosition_Row; paintRow <= robotPosition_Row + 2; paintRow++) {
            for (int paintCol = robotPosition_Col; paintCol <= robotPosition_Col + 2; paintCol++) {
                if (paintRow <= grid_Row && paintCol <= grid_Col)
                    travelInfo[paintRow - 1][paintCol - 1] = ARENA_ROBOT_TRAVELPATH;
            }
        }
        // set the travel info into arena info via checking previous robot position
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                int arenaInfoValue = arenaInfo[rowIndex - 1][colIndex - 1];
                int travelInfoValue = travelInfo[rowIndex - 1][colIndex - 1];
                if (travelInfoValue == ARENA_ROBOT_TRAVELPATH) {
                    if (arenaInfoValue == ARENA_GRID) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH;
                    } else if (arenaInfoValue == ARENA_GRID_WAYPOINT) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT;
                    } else if (arenaInfoValue == ARENA_GRID_STARTPOSITION) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH_WITH_STARTPOSITION;
                    } else if (arenaInfoValue == ARENA_GRID_ENDPOSITION) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH_WITH_ENDPOSITION;
                    } else if (arenaInfoValue == ARENA_ROBOT_EXPLOREDPATH) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH;
                    } else if (arenaInfoValue == ARENA_GRID_STARTPOSITION_UNDISCOVERED) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH_WITH_STARTPOSITION;
                    } else if (arenaInfoValue == ARENA_GRID_ENDPOSITION_UNDISCOVERED) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_TRAVELPATH_WITH_ENDPOSITION;
                    }
                }
            }
        }
    }

    private void drawObstacle() {
        Log.d(TAG, "drawObstacle");
        // save obstacle into obstacle info array
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                int arenaInfoValue = arenaInfo[rowIndex - 1][colIndex - 1];
                if (arenaInfoValue == ARENA_GRID_OBSTACLE) {
                    obstacleInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_OBSTACLE;
                }
            }
        }
        // redraw the obstacle into the next arena info
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                int arenaInfoValue = arenaInfo[rowIndex - 1][colIndex - 1];
                int obstacleInfoValue = obstacleInfo[rowIndex - 1][colIndex - 1];
                if (obstacleInfoValue == ARENA_GRID_OBSTACLE) {
                    if (arenaInfoValue == ARENA_ROBOT_EXPLOREDPATH) {
                        arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_OBSTACLE;
                    }
                }
            }
        }
    }

    private void drawExploredPath() {
        Log.d(TAG, "drawExploredPath");
        int exploredPath_Row = -1, exploredPath_Col = -1;

        // facing up
        if (robotDirection == 0) {
            Log.d(TAG, "drawExploredPath: facing up");
            // draw front sensors
            exploredPath_Row = robotPosition_Row - 3;
            for (int rowIndex = exploredPath_Row; rowIndex <= robotPosition_Row - 1; rowIndex++) {
                // prevent index out of bound
                if (rowIndex > grid_Row) rowIndex = grid_Row;
                if (rowIndex < 1) rowIndex = 1;
                for (int colIndex = robotPosition_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                    // prevent index out of bound
                    if (colIndex > grid_Col) colIndex = grid_Col;
                    if (colIndex < 1) colIndex = 1;
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
                }
            }
            // draw left sensor (long range)
            exploredPath_Col = robotPosition_Col - 5;
            for (int colIndex = exploredPath_Col; colIndex <= robotPosition_Col - 1; colIndex++) {
                // prevent index out of bound
                if (colIndex > grid_Col) colIndex = grid_Col;
                if (colIndex < 1) colIndex = 1;
                arenaInfo[robotPosition_Row - 1][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
            }
            // draw right sensor (short range)
            exploredPath_Col = robotPosition_Col + 5;
            for (int colIndex = exploredPath_Col; colIndex >= robotPosition_Col + 3; colIndex--) {
                // prevent index out of bound
                if (colIndex > grid_Col) colIndex = grid_Col;
                if (colIndex < 1) colIndex = 1;
                arenaInfo[robotPosition_Row - 1][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
            }

        }
        // facing down
        if (robotDirection == 180) {
            Log.d(TAG, "drawExploredPath: facing down");
            exploredPath_Row = robotPosition_Row + 5;
            for (int rowIndex = exploredPath_Row; rowIndex >= robotPosition_Row + 3; rowIndex--) {
                // prevent index out of bound
                if (rowIndex > grid_Row) rowIndex = grid_Row;
                if (rowIndex < 1) rowIndex = 1;
                for (int colIndex = robotPosition_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                    // prevent index out of bound
                    if (colIndex > grid_Col) colIndex = grid_Col;
                    if (colIndex < 1) colIndex = 1;
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
                }
            }
            // draw left sensor (long range)
            exploredPath_Col = robotPosition_Col + 7;
            for (int colIndex = exploredPath_Col; colIndex >= robotPosition_Col + 3; colIndex--) {
                // prevent index out of bound
                if (colIndex > grid_Col) colIndex = grid_Col;
                if (colIndex < 1) colIndex = 1;
                int rowIndex = robotPosition_Row + 1;
                if (rowIndex >= grid_Row) rowIndex = grid_Row - 1;
                arenaInfo[rowIndex][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
            }
            // draw right sensor (short range)
            exploredPath_Col = robotPosition_Col - 3;
            for (int colIndex = exploredPath_Col; colIndex <= robotPosition_Col - 1; colIndex++) {
                // prevent index out of bound
                if (colIndex > grid_Col) colIndex = grid_Col;
                if (colIndex < 1) colIndex = 1;
                int rowIndex = robotPosition_Row + 1;
                if (rowIndex >= grid_Row) rowIndex = grid_Row - 1;
                arenaInfo[rowIndex][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
            }
        }

        // facing left
        if (robotDirection == 270) {
            Log.d(TAG, "drawExploredPath: facing left");
            // draw front sensors
            exploredPath_Col = robotPosition_Col - 3;
            for (int rowIndex = robotPosition_Row; rowIndex <= robotPosition_Row + 2; rowIndex++) {
                for (int colIndex = exploredPath_Col; colIndex <= robotPosition_Col + 2; colIndex++) {
                    // prevent index out of bound
                    if (colIndex > grid_Col) colIndex = grid_Col;
                    if (colIndex < 1) colIndex = 1;
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
                }
            }
            // draw left sensor (long range)
            exploredPath_Row = robotPosition_Row + 7;
            for (int rowIndex = exploredPath_Row; rowIndex >= robotPosition_Row + 3; rowIndex--) {
                // prevent index out of bound
                if (rowIndex > grid_Row) rowIndex = grid_Row;
                arenaInfo[rowIndex - 1][robotPosition_Col - 1] = ARENA_ROBOT_EXPLOREDPATH;
            }
            // draw right sensor (short range)
            exploredPath_Row = robotPosition_Row - 3;
            for (int rowIndex = exploredPath_Row; rowIndex <= robotPosition_Row - 1; rowIndex++) {
                // prevent index out of bound
                if (rowIndex < 1) rowIndex = 1;
                arenaInfo[rowIndex - 1][robotPosition_Col - 1] = ARENA_ROBOT_EXPLOREDPATH;
            }
        }
        // facing right
        if (robotDirection == 90) {
            Log.d(TAG, "drawExploredPath: facing right");
            exploredPath_Col = robotPosition_Col + 5;
            for (int rowIndex = robotPosition_Row; rowIndex <= robotPosition_Row + 2; rowIndex++) {
                for (int colIndex = exploredPath_Col; colIndex >= robotPosition_Col + 3; colIndex--) {
                    // prevent index out of bound
                    if (colIndex > grid_Col) colIndex = grid_Col;
                    if (colIndex < 1) colIndex = 1;
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
                }
            }
            // draw left sensor (long range)
            exploredPath_Row = robotPosition_Row - 5;
            for (int rowIndex = exploredPath_Row; rowIndex <= robotPosition_Row - 1; rowIndex++) {
                // prevent index out of bound
                if (rowIndex < 1) rowIndex = 1;
                arenaInfo[rowIndex - 1][robotPosition_Col + 1] = ARENA_ROBOT_EXPLOREDPATH;
            }
            // draw right sensor (short range)
            exploredPath_Row = robotPosition_Row + 5;
            for (int rowIndex = exploredPath_Row; rowIndex >= robotPosition_Row + 3; rowIndex--) {
                // prevent index out of bound
                if (rowIndex > grid_Row) rowIndex = grid_Row;
                arenaInfo[rowIndex - 1][robotPosition_Col + 1] = ARENA_ROBOT_EXPLOREDPATH;
            }
        }


        // save explored path into exploredPathInfo array
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                int arenaInfoValue = arenaInfo[rowIndex - 1][colIndex - 1];
                if (arenaInfoValue == ARENA_ROBOT_EXPLOREDPATH) {
                    exploredPathInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
                }
            }
        }
        // redraw the explored path into the next arena info
        for (int rowIndex = 1; rowIndex <= grid_Row; rowIndex++) {
            for (int colIndex = 1; colIndex <= grid_Col; colIndex++) {
                int arenaInfoValue = arenaInfo[rowIndex - 1][colIndex - 1];
                int exploredPathValue = exploredPathInfo[rowIndex - 1][colIndex - 1];
                if (exploredPathValue == ARENA_ROBOT_EXPLOREDPATH) {
                    arenaInfo[rowIndex - 1][colIndex - 1] = ARENA_ROBOT_EXPLOREDPATH;
                }
            }
        }
    }

    // Decode method - connected to robot
    public void decodeAlgorithm(String content) {
        Log.d(TAG, "decodeAlgorithm: content: " + content);
        TXTVW_ControlMode = (TextView) getRootView().findViewById(R.id.TXTVW_ControlMode);
        TXTVW_MDFString = (TextView) getRootView().findViewById(R.id.TXTVW_MDFString);
        TXTVW_RobotStatusValue = (TextView) getRootView().findViewById(R.id.TXTVW_RobotStatusValue);
        arenaInfo = new int[grid_Row][grid_Col];
        if (content.charAt(0) == 'm') {
            // display mdf string
            TXTVW_MDFString.setText(content.substring(1));
            TXTVW_MDFString.setVisibility(VISIBLE);
            TXTVW_ControlMode.setText("Eploration Complete");
        }
        switch (content) {
            case "q|":
            case "q":
                // exploration stopped
                TXTVW_ControlMode.setText("Eploration Complete");
                break;
            case "i|":
            case "i":
            case CMD_FORWARD:
                if (robotDirection == 0) { // face up, move forward
                    Log.d(TAG, "decodeAlgorithm: cmd_forward: " + robotPosition_Row + "," + robotPosition_Col + ": facing up");
                    if ((robotPosition_Row - 1) >= 1) {
                        robotPosition_Row -= 1;
                        TXTVW_RobotStatusValue.setText("Moving forward");
                        TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    } else {
                        showToast_Short("Unable to move forward");
                    }
                } else if (robotDirection == 90) { // facing right, move forward
                    Log.d(TAG, "decodeAlgorithm: cmd_forward: " + robotPosition_Row + "," + robotPosition_Col + ": facing right");
                    if ((robotPosition_Col + 1) <= grid_Col - 2) {
                        robotPosition_Col += 1;
                        TXTVW_RobotStatusValue.setText("Moving forward");
                        TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    } else {
                        showToast_Short("Unable to move forward");
                    }
                } else if (robotDirection == 180) { // facing down, move forward
                    Log.d(TAG, "decodeAlgorithm: cmd_forward: " + robotPosition_Row + "," + robotPosition_Col + ": facing down");
                    if ((robotPosition_Row + 1) <= grid_Row - 2) {
                        robotPosition_Row += 1;
                        TXTVW_RobotStatusValue.setText("Moving forward");
                        TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    } else {
                        showToast_Short("Unable to move forward");
                    }
                } else if (robotDirection == 270) { // facing left, move forward
                    Log.d(TAG, "decodeAlgorithm: cmd_forward: " + robotPosition_Row + "," + robotPosition_Col + ": facing left");
                    if ((robotPosition_Col - 1) >= 1) {
                        robotPosition_Col -= 1;
                        TXTVW_RobotStatusValue.setText("Moving forward");
                        TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    } else {
                        showToast_Short("Unable to move forward");
                    }
                }
                // set robot position
                arenaInfo[robotPosition_Row][robotPosition_Col] = ARENA_ROBOT_POSITION;
                break;
            case "k|":
            case "k":
            case CMD_REVERSE:
                if (robotDirection == 0) { // face up, reverse
                    Log.d(TAG, "decodeAlgorithm: cmd_reverse: " + robotPosition_Row + "," + robotPosition_Col + ": facing up");
                    if ((robotPosition_Row + 1) <= 18) {
                        robotPosition_Row += 1;
                        TXTVW_RobotStatusValue.setText("Reversing");
                        TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    } else {
                        showToast_Short("Unable to reverse");
                    }
                } else if (robotDirection == 90) { // facing right, reverse
                    Log.d(TAG, "decodeAlgorithm: cmd_reverse: " + robotPosition_Row + "," + robotPosition_Col + ": facing right");
                    if ((robotPosition_Col - 1) >= 1) {
                        robotPosition_Col -= 1;
                        TXTVW_RobotStatusValue.setText("Reversing");
                        TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    } else {
                        showToast_Short("Unable to reverse");
                    }
                } else if (robotDirection == 180) { // facing down, reverse
                    Log.d(TAG, "decodeAlgorithm: cmd_reverse: " + robotPosition_Row + "," + robotPosition_Col + ": facing down");
                    if ((robotPosition_Row - 1) >= 1) {
                        robotPosition_Row -= 1;
                        TXTVW_RobotStatusValue.setText("Reversing");
                        TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    } else {
                        showToast_Short("Unable to reverse");
                    }
                } else if (robotDirection == 270) { // facing left, reverse
                    Log.d(TAG, "decodeAlgorithm: cmd_reverse: " + robotPosition_Row + "," + robotPosition_Col + ": facing left");
                    if ((robotPosition_Col + 1) <= 13) {
                        robotPosition_Col += 1;
                        TXTVW_RobotStatusValue.setText("Reversing");
                        TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    } else {
                        showToast_Short("Unable to reverse");
                    }
                }
                // set robot position
                arenaInfo[robotPosition_Row][robotPosition_Col] = ARENA_ROBOT_POSITION;
                break;
            case "j|":
            case "j":
            case CMD_ROTATELEFT:// rotate left
                Log.d(TAG, "decodeAlgorithm: cmd_rotate left: " + robotPosition_Row + "," + robotPosition_Col);
                arenaInfo[robotPosition_Row][robotPosition_Col] = ARENA_ROBOT_POSITION;
                // set robot direction
                if (saveStateArrayList.size() > 0) {
                    TXTVW_RobotStatusValue.setText("Rotating left");
                    TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    int previousDirection = saveStateArrayList.get(saveStateArrayList.size() - 1).getRobotDirection();
                    if (previousDirection == 0) {
                        robotDirection = 270;
                    } else if (previousDirection == 90) {
                        robotDirection = 0;
                    } else if (previousDirection == 180) {
                        robotDirection = 90;
                    } else if (previousDirection == 270) {
                        robotDirection = 180;
                    }
                } else {
                    Log.d(TAG, "decodeAlgorithm: cmd_rotate left: saveStateArrayList.size() = 0 ");
                }
                break;
            case "l|":
            case "l":
            case CMD_ROTATERIGHT:// rotate right
                Log.d(TAG, "decodeAlgorithm: cmd_rotate right: " + robotPosition_Row + "," + robotPosition_Col);
                arenaInfo[robotPosition_Row][robotPosition_Col] = ARENA_ROBOT_POSITION;
                // set robot direction
                if (saveStateArrayList.size() > 0) {
                    TXTVW_RobotStatusValue.setText("Rotating right");
                    TXTVW_RobotStatusValue.setTextColor(ContextCompat.getColor(getContext(), color_Arena_RobotPosition));
                    int previousDirection = saveStateArrayList.get(saveStateArrayList.size() - 1).getRobotDirection();
                    if (previousDirection == 0) {
                        robotDirection = 90;
                    } else if (previousDirection == 90) {
                        robotDirection = 180;
                    } else if (previousDirection == 180) {
                        robotDirection = 270;
                    } else if (previousDirection == 270) {
                        robotDirection = 0;
                    }
                } else {
                    Log.d(TAG, "decodeAlgorithm: cmd_rotate right: saveStateArrayList.size() = 0 ");
                }
                break;
        }
        // set the robot center position
        robotPosition_Row_Center = robotPosition_Row + 1;
        robotPosition_Col_Center = robotPosition_Col + 1;

        drawExploredPath();
        if (content.contains("EXPLORED")) {
            setObstacle(content);
        }
        drawObstacle();
        drawStartPosition();
        drawEndPosition();
        drawWayPoint();
        drawRobotPosition();
        drawRobotDirection();
        drawTravelPath();
        drawWayPointReached();

        saveArenaState();   // save state for algorithm

    }

    // Connected to AMDTool
    public void decodeArenaInfo(String cmdString) {
        // decode the cmd string
        if (cmdString.contains("NakedArena")) {
            setupNakedGrid();   // @decodeArenaInfo
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
                        if (arenaInfo[rowIndex - 1][colIndex - 1] == ARENA_GRID_OBSTACLE) {
                            obstacleInfo[rowIndex - 1][colIndex - 1] = ARENA_GRID_OBSTACLE;
                        }
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
                // set way point reached
                drawWayPointReached();
            } catch (Exception ex) {

            }
        }
        saveArenaState(); // save get arena info
    }


}
