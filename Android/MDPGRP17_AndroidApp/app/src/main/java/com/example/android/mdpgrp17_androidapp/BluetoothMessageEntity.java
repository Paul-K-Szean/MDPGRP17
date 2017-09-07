package com.example.android.mdpgrp17_androidapp;

/**
 * Created by szean on 7/9/2017.
 */

public class BluetoothMessageEntity {


    String from;
    String to;
    String messageContent;

    public BluetoothMessageEntity(String from, String to, String messageContent) {
        this.from = from;
        this.to = to;
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

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }


//    byte[] read = (byte[]) msg.obj;
//    String incomingMessage = new String(read, 0, msg.arg1);  // byte[]; offset; byteCount
////                    Log.d(TAG, "MESSAGE_READ: " + incomingMessage);
//
//    bytes = mmInStream.read(buffer);
//    String incomingMessage = new String(buffer, 0, bytes);
//                    Log.d(TAG, "ConnectedThread: InputStream: " + incomingMessage);
//                    mBTMsgArrayList.add(mConnectedRemoteDevice.getName() + ": " + incomingMessage);
//                    Log.d(TAG, "mBTMsgArrayList size: " + mBTMsgArrayList.size());
//                    for (String message : mBTMsgArrayList) {
//        Log.d(TAG, "message: " + message);
//    }
//                    mHandler.obtainMessage(GlobalVariables.MESSAGE_READ, bytes, -1, buffer).sendToTarget();

}
