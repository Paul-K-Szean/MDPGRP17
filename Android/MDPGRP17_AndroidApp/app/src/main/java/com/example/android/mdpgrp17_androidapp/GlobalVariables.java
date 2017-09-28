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
    public static final String CMD_BEGINEXPLORE = "cam";
    public static final String CMD_BEGINFASTEST = "can";
    public static final String CMD_SENDARENAINFO = "cmd_sendarenainfo";


    // Bluetooth Connection State
    // 0 = disconnected, 1 = connecting, 2 = connected, 3 = disconnecting, 4 = idle, 5 = listening, 6 = state changed
    public static final int BT_CONNECTION_STATE_DISCONNECTED = 0;
    public static final int BT_CONNECTION_STATE_CONNECTING = 1;
    public static final int BT_CONNECTION_STATE_CONNECTED = 2;
    public static final int BT_CONNECTION_STATE_DISCONNECTING = 3;
    public static final int BT_CONNECTION_STATE_IDLE = 4; // ON
    public static final int BT_CONNECTION_STATE_LISTENING = 5;
    public static final int BT_CONNECTION_STATE_CHANGE = 6;
    public static final int BT_CONNECTION_STATE_OFF = 7;

    public static final int BT_CONNECTION_STATE_CONNECTIONLOST = 8;
    public static final int BT_CONNECTION_STATE_CONNECTIONFAILED = 9;

    // Arena State
    public static final int ARENA_GRID = 0;
    public static final int ARENA_GRID_OBSTACLE = 1;
    public static final int ARENA_GRID_POSITION_START = 2;
    public static final int ARENA_GRID_POSITION_END = 3;
    public static final int ARENA_GRID_POSITIN_WAYPOINT = 4;
    public static final int ARENA_ROBOT_POSITION = 5;
    public static final int ARENA_ROBOT_DIRECTION = 6;
    public static final int ARENA_ROBOT_TRAVELPATH = 7;
    public static final int ARENA_ROBOT_TRAVELPATH_WITH_WAYPOINT = 8;   // travel path with way point


    // Set Way Point


}
