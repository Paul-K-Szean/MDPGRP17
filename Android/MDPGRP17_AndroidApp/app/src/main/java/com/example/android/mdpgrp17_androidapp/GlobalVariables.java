package com.example.android.mdpgrp17_androidapp;

/**
 * Created by szean on 4/9/2017.
 */

public class GlobalVariables {
    // Bluetooth Message Command
    public static final int MESSAGE_COMMAND = 0;
    public static final int MESSAGE_CONVERSATION = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final String MESSAGE_FROM = "MDPGRP17";
    public static final String CMD_FORWARD = "bai";
    public static final String CMD_REVERSE = "bak";
    public static final String CMD_ROTATELEFT = "baj";
    public static final String CMD_ROTATERIGHT = "bal";
    public static final String CMD_BEGINEXPLORE = "cmd_beginexploration";
    public static final String CMD_BEGINFASTEST = "cmd_beginfastest";
    public static final String CMD_SENDARENAINFO = "cmd_sendarenainfo";

    // Bluetooth Connection State
    // 0 = disconnected, 1 = connecting, 2 = connected, 3 = disconnecting, 4 = idle, 5 = listening, 6 = state changed
    public static final int BT_CONNECTION_STATE_DISCONNECTED = 0;
    public static final int BT_CONNECTION_STATE_CONNECTING = 1;
    public static final int BT_CONNECTION_STATE_CONNECTED = 2;
    public static final int BT_CONNECTION_STATE_DISCONNECTING = 3;
    public static final int BT_CONNECTION_STATE_IDLE = 4; // ON
    public static final int BT_CONNECTION_STATE_CANNOTLISTEN = 5; // Cannot start accept thread
    public static final int BT_CONNECTION_STATE_LISTENING = 6;
    public static final int BT_CONNECTION_STATE_CHANGE = 7;
    public static final int BT_CONNECTION_STATE_OFF = 8;
    public static final int BT_CONNECTION_STATE_CONNECTIONLOST = 9;
    public static final int BT_CONNECTION_STATE_CONNECTIONFAILED = 10;
    public static final int BT_CONNECTION_STATE_DISCOVERABLE_DURATION = 300; // in secs

    // Arena State
    public static final int ARENA_GRID = 0;
    public static final int ARENA_GRID_OBSTACLE = 1;
    public static final int ARENA_GRID_POSITION_START = 2;
    public static final int ARENA_GRID_POSITION_END = 3;
    public static final int ARENA_GRID_POSITION_WAYPOINT = 4;
    public static final int ARENA_ROBOT_POSITION = 5;
    public static final int ARENA_ROBOT_POSITION_WITH_WAYPOINT = 6;
    public static final int ARENA_ROBOT_DIRECTION = 7;
    public static final int ARENA_ROBOT_DIRECTION_WITH_WAYPOINT = 8;
    public static final int ARENA_ROBOT_TRAVELPATH = 9;
    public static final int ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT = 10;   // travel path with way point
    public static final int ARENA_ROBOT_TRAVELPATH_WITH_STARTPOSITION = 11;   // travel path with way point
    public static final int ARENA_ROBOT_TRAVELPATH_WITH_ENDPOSITION = 12;   // travel path with way point

    public static final int ARENA_GAME_MODE_FASTEST = 0;
    public static final int ARENA_GAME_MODE_EXPLORATION = 1;
    public static final int ARENA_GAME_MODE_MANUALCONTROL = 2;

    public static final int ARENA_CONTROL_MODE_BUTTON = 0;
    public static final int ARENA_CONTROL_MODE_SWIPE = 1;
    public static final int ARENA_CONTROL_MODE_TILT = 2;
    public static final int SWIPE_MINIMUM_DISTANCE = 220;
    public static final int TILT_MINIMUM_UP = 3;
    public static final int TILT_MINIMUM_DOWN = -3;
    public static final int TILT_MINIMUM_LEFT = -3;
    public static final int TILT_MINIMUM_RIGHT = 3;
    public static final float TILT_SENSITIVITY = 0.0000008f;

    public static final int REQUESTCODE_YES = 300;
    public static final int REQUESTCODE_NO = 0;
    public static final int REQUESTCODE_BLUETOOTH_CONNECTABLE_DISCOVERABLE = 1;

    public static final int TOUCHMODE_SETWAYPOINT = 0;
    public static final int TOUCHMODE_SETROBOTPOSITION = 1;
    public static final int TOUCHMODE_SWIPE = 2;

    // Services
    public static final String COUNTDOWNTIMER_SERVICE = "countDownTimerService";
    public static final String STOPWATCH_SERVICE = "stopWatchService";

}
