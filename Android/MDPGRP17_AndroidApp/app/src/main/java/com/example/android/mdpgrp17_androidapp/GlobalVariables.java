package com.example.android.mdpgrp17_androidapp;

/**
 * Created by szean on 4/9/2017.
 */
// Android = a
// Arduino = b
// Algorithm = c
// chat[0] = destination
// chat[1] = source
// chat[2onwards] = content
// Bluetooth Message Command
public class GlobalVariables {

    public static final int MESSAGE_COMMAND = 0;
    public static final int MESSAGE_CONVERSATION = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final String NAME_MDPGR17 = "MDPGRP17";
    public static final String NAME_RASPBERRYPI = "mdpteam17_raspi";
    public static final char MESSAGE_FROM_ANDROID = 'a';
    public static final char MESSAGE_FROM_ARDUINO = 'b';
    public static final char MESSAGE_FROM_ALGORITHM = 'c';
    public static final String CMD_FORWARD = "bai|"; // destination: arduino = b, source: android = a, content
    public static final String CMD_REVERSE = "bak|";
    public static final String CMD_ROTATELEFT = "baj|";
    public static final String CMD_ROTATERIGHT = "bal|";
    public static final String CMD_STRAFELEFT = "bau";
    public static final String CMD_STRAFERIGHT = "bao";
    public static final String CMD_BEGINEXPLORE = "cae"; // destination: algo = c, source: android = a, content
    public static final String CMD_BEGINFASTEST = "cas";
    public static final String CMD_SENDARENAINFO = "can";
    public static final String CMD_KILLME_RASPBERRYPI = "killme";
    public static final String CMD_CALIBRATEROBOT = "bay|"; // calibrate sensor

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
    public static final int BT_CONNECTION_STATE_DISCOVERABLE_DURATION = 120; // in secs, no idea why returns this as result code

    // Arena State
    public static final int ARENA_GRID = 0;
    public static final int ARENA_GRID_OBSTACLE = 1;
    public static final int ARENA_GRID_STARTPOSITION = 2;                   // start position
    public static final int ARENA_GRID_STARTPOSITION_UNDISCOVERED = 3;      // start position un-discover
    public static final int ARENA_GRID_ENDPOSITION = 4;                     // end position
    public static final int ARENA_GRID_ENDPOSITION_UNDISCOVERED = 5;        // end position un-discover
    public static final int ARENA_GRID_WAYPOINT = 6;                        // way point
    public static final int ARENA_ROBOT_POSITION = 7;                       // 1. robot position
    public static final int ARENA_ROBOT_POSITION_WITH_WAYPOINT = 8;         // 2. robot position with way point
    public static final int ARENA_ROBOT_POSITION_WITH_STARTPOSITION = 9;    // 3. robot position with start position
    public static final int ARENA_ROBOT_POSITION_WITH_ENDPOSITION = 10;     // 4. robot position with end position
    public static final int ARENA_ROBOT_DIRECTION = 11;                     // 5. robot direction
    public static final int ARENA_ROBOT_DIRECTION_WITH_WAYPOINT = 12;       // 6. robot direction with way point
    public static final int ARENA_ROBOT_DIRECTION_WITH_STARTPOSITION = 13;  // 7. robot position with start position
    public static final int ARENA_ROBOT_DIRECTION_WITH_ENDPOSITION = 14;    // 8. robot position with end position
    public static final int ARENA_ROBOT_TRAVELPATH = 15;                    // 9. robot travel path
    public static final int ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT = 16;      // 10. robot travel path with way point
    public static final int ARENA_ROBOT_TRAVELPATH_WITH_STARTPOSITION = 17; // 11. robot travel path with start position
    public static final int ARENA_ROBOT_TRAVELPATH_WITH_ENDPOSITION = 18;   // 12. robot travel path with end position
    public static final int ARENA_ROBOT_EXPLOREDPATH = 19;                  // 13. robot travel path with end position


    public static final int ARENA_GAME_MODE_BUTTON = 0;
    public static final int ARENA_GAME_MODE_SWIPE = 1;
    public static final int ARENA_GAME_MODE_TILT = 2;
    public static final int ARENA_GAME_MODE_FASTEST = 3;
    public static final int ARENA_GAME_MODE_EXPLORATION = 4;


    public static final int SWIPE_MINIMUM_DISTANCE = 220;
    public static final int TILT_MINIMUM_UP = 3;
    public static final int TILT_MINIMUM_DOWN = -3;
    public static final int TILT_MINIMUM_LEFT = -3;
    public static final int TILT_MINIMUM_RIGHT = 3;
    public static final float TILT_SENSITIVITY = 0.0000008f;

    public static final int REQUESTCODE_YES = 300;
    public static final int REQUESTCODE_NO = 0;
    public static final int REQUESTCODE_BLUETOOTH_CONNECTABLE_DISCOVERABLE = 1;
    public static final int REQUESTCODE_BLUETOOTH_PAIRING = 2;

    public static final int TOUCHMODE_NONE = 0;
    public static final int TOUCHMODE_SETWAYPOINT = 1;
    public static final int TOUCHMODE_SETROBOTPOSITION = 2;
    public static final int TOUCHMODE_SWIPE = 3;

    // Services
    public static final String COUNTDOWNTIMER_SERVICE = "countDownTimerService";

    // AMD Device Name
    public static final String AMDTOOLNAME = "MONSTER-PC";

}
