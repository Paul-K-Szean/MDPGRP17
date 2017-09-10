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
    public static final String CMD_FORWARD = "cmd_forward";
    public static final String CMD_REVERSE = "cmd_reverse";
    public static final String CMD_ROTATELEFT = "cmd_rotateleft";
    public static final String CMD_ROTATERIGHT = "cmd_rotateright";
    public static final String CMD_BEGINEXPLORE = "cmd_beginexplore";
    public static final String CMD_BEGINFASTEST = "cmd_beginfastest";
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

    // Arena State
    public static final int ARENA_GRID = 0;
    public static final int ARENA_OBSTACLE = 1;
    public static final int ARENA_ROBOTPOSITION = 2;
    public static final int ARENA_ROBOTDIRECTION = 3;
    public static final int ARENA_ROBOTTRAVELPATH = 4;

}
