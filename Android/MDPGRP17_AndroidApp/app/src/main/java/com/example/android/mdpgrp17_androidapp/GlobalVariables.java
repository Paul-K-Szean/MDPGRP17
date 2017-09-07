package com.example.android.mdpgrp17_androidapp;

/**
 * Created by szean on 4/9/2017.
 */

public class GlobalVariables {
    // Bluetooth Message
    public static final int MESSAGE_COMMAND = 0;
    public static final int MESSAGE_CONVERSATION = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

//    public static final int MESSAGE_DEVICE_NAME = 3;
//    public static final int MESSAGE_TOAST = 4;


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
    // Bluetooth
}
