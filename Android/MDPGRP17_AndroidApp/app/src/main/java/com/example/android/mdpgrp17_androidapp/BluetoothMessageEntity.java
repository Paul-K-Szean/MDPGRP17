package com.example.android.mdpgrp17_androidapp;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_COMMAND;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_CONVERSATION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_FROM;

/**
 * Created by szean on 7/9/2017.
 */

public class BluetoothMessageEntity {


    String from;
    String to;
    int messageType;
    String messageContent;

    public BluetoothMessageEntity(String from, String to, int messageType, String messageContent) {
        this.from = from;
        this.to = to;
        this.messageType = messageType;
        this.messageContent = messageContent;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;

    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public static BluetoothMessageEntity sendCommand(String commandString) {
        BluetoothConnection mBluetoothConnection = BluetoothConnection.getmBluetoothConnection(null);
        return new BluetoothMessageEntity(MESSAGE_FROM, mBluetoothConnection.getConnectedRemoteDevice().getName(), MESSAGE_COMMAND, commandString);
    }
    public static BluetoothMessageEntity sendConversation(String conversationString) {
        BluetoothConnection mBluetoothConnection = BluetoothConnection.getmBluetoothConnection(null);
        return new BluetoothMessageEntity(MESSAGE_FROM, mBluetoothConnection.getConnectedRemoteDevice().getName(), MESSAGE_CONVERSATION, conversationString);
    }
}
