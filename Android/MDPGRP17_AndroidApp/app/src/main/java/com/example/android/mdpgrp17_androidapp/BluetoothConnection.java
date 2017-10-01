package com.example.android.mdpgrp17_androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTED;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTING;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_IDLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_LISTENING;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_FORWARD;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_REVERSE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_ROTATELEFT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_ROTATERIGHT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_SENDARENAINFO;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_COMMAND;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_CONVERSATION;


/**
 * Created by szean on 1/9/2017.
 */

public class BluetoothConnection {
    private static final String TAG = "BluetoothConnection";
    private static final String SERVERNAME_SECURE = "MDP_AndroidApp_Secure";
    private static final String SERVERNAME_INSECURE = "MDP_AndroidApp_Insecure";

    // 00001101-0000-1000-8000-00805f9b34fb
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final UUID MY_UUID_INSECURE = MY_UUID_SECURE;
    private static final UUID REMOTE_UUID_SECURE = MY_UUID_SECURE;
    private static final UUID REMOTE_UUID_INSECURE = MY_UUID_SECURE;
    private static BluetoothConnection mBluetoothConnection;
    private final BluetoothAdapter mBluetoothAdapter;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BluetoothDevice mConnectedRemoteDevice;   // connected remote device
    private int mBTCurrentState;
    private Handler mHandler;
    private ArrayList<BluetoothMessageEntity> mBTConversationArrayList;
    private ArrayList<BluetoothMessageEntity> mBTCommandArrayList;
    private ConfigFileHandler configFileHandler;
    private ConfigFile configFile;
    private boolean isUserDisconnect = false;

    // constructor
    public BluetoothConnection(Handler mHandler) {
        Log.d(TAG, "BluetoothConnection");
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = mHandler;
        this.mBTCurrentState = GlobalVariables.BT_CONNECTION_STATE_IDLE;
        this.configFileHandler = new ConfigFileHandler();
        this.configFile = configFileHandler.getConfigFile();
    }

    public static BluetoothConnection getmBluetoothConnection(Handler mHandler) {
        if (mBluetoothConnection == null) {
            Log.d(TAG, "getmBluetoothConnection: BluetoothConnection was null");
            mBluetoothConnection = new BluetoothConnection(mHandler);
        }
        return mBluetoothConnection;
    }

    public void setHandler(Handler handler) {
        Log.d(TAG, "setHandler");
        mHandler = handler;
    }

    public ArrayList<BluetoothMessageEntity> getmBTConversationArrayList() {
        if (mBTConversationArrayList == null) {
            Log.d(TAG, "getmBTConversationArrayList: mBTConversationArrayList was null, created new mBTConversationArrayList");
            mBTConversationArrayList = new ArrayList<BluetoothMessageEntity>();
        }
        Log.d(TAG, "getmBTConversationArrayList: mBTConversationArrayList was not null, size: " + mBTConversationArrayList.size() + ", returned mBTConversationArrayList");
        return mBTConversationArrayList;
    }

    public ArrayList<BluetoothMessageEntity> getmBTCommandArrayList() {
        if (mBTCommandArrayList == null) {
            Log.d(TAG, "getmBTCommandArrayList: mBTCommandArrayList was null, created new mBTCommandArrayList");
            mBTCommandArrayList = new ArrayList<BluetoothMessageEntity>();
        }
        Log.d(TAG, "getmBTCommandArrayList: mBTCommandArrayList was not null, size: " + mBTCommandArrayList.size() + ", returned mBTCommandArrayList");
        return mBTCommandArrayList;
    }

    public void setmBTCommandArrayList(ArrayList<BluetoothMessageEntity> mBTCommandArrayList) {
        this.mBTCommandArrayList = mBTCommandArrayList;
    }

    /**
     * Start the chat service. Specifically startAcceptThread AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void startAcceptThread(boolean isSecureConnection) {
        if (isSecureConnection) {
            mSecureAcceptThread= null;
            mSecureAcceptThread = new AcceptThread(isSecureConnection);
            mSecureAcceptThread.start();
        } else {
            mInSecureAcceptThread= null;
            mInSecureAcceptThread = new AcceptThread(isSecureConnection);
            mInSecureAcceptThread.start();
        }
    }

    /**
     * Explicitly start the ConnectThread as the remote device is paired
     */
    public synchronized void startConnectThread(BluetoothDevice mPairedDevice, boolean secure) {
        Log.d(TAG, "startConnectThread");
        Log.d(TAG, "startConnectThread: mPairedDevice: " + mPairedDevice.getName());
        mConnectThread = new ConnectThread(mPairedDevice, secure);
        mConnectThread.start();
    }

    /**
     * */
    public synchronized void startConnectedThread(BluetoothSocket mmSocket, boolean isConnectionSecure) {
        Log.d(TAG, "startConnectedThread");
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public synchronized void disconnect() {
        Log.d(TAG, "disconnect");
        isUserDisconnect = true;
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stopAllThreads() {
        Log.d(TAG, "stopAllThreads");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInSecureAcceptThread != null) {
            mInSecureAcceptThread.cancel();
            mInSecureAcceptThread = null;
        }

        setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_IDLE);
        mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_IDLE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     */
    public synchronized void write(BluetoothMessageEntity bluetoothMessageEntity) {
        if (bluetoothMessageEntity.getMessageContent().length() > 0) {
            // Synchronize a copy of the ConnectedThread
            Log.d(TAG, "Write");
            //perform the write
            mConnectedThread.write(bluetoothMessageEntity);
        } else {
            Log.d(TAG, "Write: Nothing to write");
        }
    }

    public synchronized int getBTConnectionState() {
        // 0 = disconnected, 1 = connecting, 2 = connected, 3 = disconnecting, 4 = idle, 5 = listening, 6 = state changed
        switch (mBTCurrentState) {
            case 0:
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_DISCONNECTED (" + mBTCurrentState + ")");
                break;
            case 1:
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_CONNECTING (" + mBTCurrentState + ")");
                break;
            case 2:
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_CONNECTED (" + mBTCurrentState + ")");
                break;
            case 3:
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_DISCONNECTING (" + mBTCurrentState + ")");
                break;
            case 4:
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_IDLE (" + mBTCurrentState + ")");
                break;
            case 5:
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_CANNOTLISTEN (" + mBTCurrentState + ")");
                break;
            case 6:
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_LISTENING (" + mBTCurrentState + ")");
                break;
            case 7:
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_CHANGE (" + mBTCurrentState + ")");
                break;
        }
        return mBTCurrentState;
    }

    public synchronized void setBTConnectionState(int newState) {
        if (mBTCurrentState != newState) {
            Log.d(TAG, "setBTConnectionState: State changed: " + mBTCurrentState + " -> " + newState);
            Log.d(TAG, "States(BT_CONNECTION_STATE_DISCONNECTED = 0, BT_CONNECTION_STATE_CONNECTING = 1, " +
                    "BT_CONNECTION_STATE_CONNECTED = 2, BT_CONNECTION_STATE_DISCONNECTING = 3, " +
                    "BT_CONNECTION_STATE_IDLE = 4, BT_CONNECTION_STATE_LISTENING = 5, " +
                    "BT_CONNECTION_STATE_CHANGE = 6");
            mBTCurrentState = newState;
        } else {
            Log.d(TAG, "setBTConnectionState: Already in this state: " + mBTCurrentState);
        }
        // Give the new state to the Handler so the UI Activity can update
        // if(newState==BT_CONNECTION_STATE_CONNECTED)

    }

    public BluetoothDevice getConnectedRemoteDevice() {
        if (mConnectedRemoteDevice != null)
            return mConnectedRemoteDevice;
        else {
            return null;
        }
    }

    public void setConnectedRemoteDevice(BluetoothDevice bluetoothDevice) {
        this.mConnectedRemoteDevice = bluetoothDevice;
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket - current device hosting the server socket
        // When connecting two devices, one must act as a server by holding an open BluetoothServerSocket.
        private final BluetoothServerSocket mServerSocket;
        private BluetoothDevice mRemoteDevice;
        private boolean isConnectionSecure;

        public AcceptThread(boolean isConnectionSecure) {
            Log.d(TAG, "AcceptThread: Started");
            BluetoothServerSocket tmp = null;
            this.isConnectionSecure = isConnectionSecure;
            // Create a new listening server socket
            try {
                if (isConnectionSecure) {
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SERVERNAME_SECURE, MY_UUID_SECURE);
                    Log.d(TAG, "AcceptThread: isConnectionSecure: " + isConnectionSecure + ". Using: " + MY_UUID_SECURE);
                } else {
                    tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(SERVERNAME_INSECURE, MY_UUID_INSECURE);
                    Log.d(TAG, "AcceptThread: isConnectionSecure: " + isConnectionSecure + ". Using: " + MY_UUID_INSECURE);
                }
                Log.d(TAG, "AcceptThread: RFCOM server socket: " + tmp.toString());
                if (mBTCurrentState != GlobalVariables.BT_CONNECTION_STATE_LISTENING) {
                    setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_LISTENING);
                    mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_LISTENING, 0).sendToTarget();
                }
            } catch (IOException e) {
                // cannot start listening for connection
                setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_CANNOTLISTEN);
                mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_CANNOTLISTEN, 0).sendToTarget();
                if (isConnectionSecure) {
                    Log.e(TAG, "AcceptThread: mBluetoothAdapter.listenUsingRfcommWithServiceRecord(): " + e.getMessage());
                    mSecureAcceptThread = null;
                } else {
                    Log.e(TAG, "AcceptThread: mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(): " + e.getMessage());
                    mInSecureAcceptThread = null;
                }
                cancel();
            }
            mServerSocket = tmp;
        }

        public void run() { // automatically executed
            Log.d(TAG, "AcceptThread: Running");
            if (mServerSocket != null) {
                BluetoothSocket mSocket = null;
                try {
                    Log.d(TAG, "AcceptThread: RFCOM server socket started");
                    mSocket = mServerSocket.accept();
                    Log.d(TAG, "AcceptThread: RFCOM server socket accepted a connection. Remote device: " + mSocket.getRemoteDevice().getName());
                    Log.d(TAG, "AcceptThread: RFCOM server socket: " + mServerSocket.toString());
                    Log.d(TAG, "AcceptThread: BluetoothSocket: " + mSocket.toString());
                    mConnectedRemoteDevice = mRemoteDevice = mSocket.getRemoteDevice();
                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread: mServerSocket.accept(): " + e.getMessage());
                }
                if (mSocket != null) {
                    synchronized (BluetoothConnection.this) {
                        switch (mBTCurrentState) {
                            case BT_CONNECTION_STATE_LISTENING:
                            case BT_CONNECTION_STATE_CONNECTING:
                                // Start the connected thread.
                                startConnectedThread(mSocket, isConnectionSecure);
                                break;
                            case BT_CONNECTION_STATE_IDLE:
                            case BT_CONNECTION_STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    Log.d(TAG, "AcceptThread: Closing bluetooth socket");
                                    mSocket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "AcceptThread: socket.close(); failed. " + e.getMessage());
                                }
                                break;
                        }
                    }
                }
            } else {
                Log.d(TAG, "AcceptThread: RFCOM server socket was null");
            }
            Log.i(TAG, "AcceptThread: Stopped running");
        }

        public void cancel() {
            try {
                Log.d(TAG, "AcceptThread: Closing server socket");
                if (mServerSocket != null)
                    mServerSocket.close();
                Log.d(TAG, "AcceptThread: Server socket closed");
            } catch (IOException e) {
                Log.e(TAG, "cancel:  mServerSocket.close(); failed. " + e.getMessage());
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mSocket;
        private BluetoothDevice mRemoteDevice;
        private boolean isConnectionSecure;

        public ConnectThread(BluetoothDevice mRemoteDevice, boolean isConnectionSecure) {
            Log.d(TAG, "ConnectThread: Started");
            Log.d(TAG, "ConnectThread: Connecting with " + mRemoteDevice.getName());
            this.mRemoteDevice = mRemoteDevice;
            this.isConnectionSecure = isConnectionSecure;

            try {
                if (isConnectionSecure) {
                    mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(REMOTE_UUID_SECURE);
                    Log.d(TAG, "ConnectThread: Is connection secure: " + isConnectionSecure + ". Using: " + REMOTE_UUID_SECURE);
                } else {
                    mSocket = mRemoteDevice.createInsecureRfcommSocketToServiceRecord(REMOTE_UUID_SECURE);
                    Log.d(TAG, "ConnectThread: Is connection secure: " + isConnectionSecure + ". Using: " + REMOTE_UUID_INSECURE);
                }
                Log.d(TAG, "ConnectThread: BluetoothSocket: " + mSocket.toString());
                setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_CONNECTING);
                mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_CONNECTING, 0).sendToTarget();

            } catch (IOException e) {
                setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_LISTENING);
                mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_LISTENING, 0).sendToTarget();
                if (isConnectionSecure) {
                    Log.e(TAG, "ConnectThread: mRemoteDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE); failed. " + e.getMessage());
                } else {
                    Log.e(TAG, "ConnectThread: mRemoteDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE); failed. " + e.getMessage());
                }
                mSocket = null;
            }

        }

        public void run() { // automatically executed
            Log.i(TAG, "ConnectThread: Running");
            // Always cancel discovery because memory intensive when searching for device
            mBluetoothAdapter.cancelDiscovery();
            if (mSocket == null) {
                Log.i(TAG, "ConnectThread: mSocket is null");
            } else {
                Log.i(TAG, "ConnectThread: mSocket: " + mSocket.toString());

                // Make a connection to the BluetoothSocket
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    Log.d(TAG, "ConnectThread: Making connection with " + mSocket.getRemoteDevice().getName());
                    mSocket.connect();
                    Log.i(TAG, "ConnectThread: mSocket: " + mSocket.toString());
                    if (mSocket.isConnected()) {
                        Log.d(TAG, "ConnectThread: Connected with " + mSocket.getRemoteDevice().getName());
                        mConnectedRemoteDevice = mRemoteDevice;
                        startConnectedThread(mSocket, isConnectionSecure);
                    } else {
                        Log.i(TAG, "ConnectThread: mSocket unable to connect with : " + mSocket.toString());
                    }
                } catch (IOException e) {
                    setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_LISTENING);
                    mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_LISTENING, 0).sendToTarget();
                    Log.e(TAG, "ConnectThread: mSocket.connect(); failed. " + e.getMessage());
                    // Close the socket
                    try {
                        mSocket.close();
                        Log.d(TAG, "ConnectThread: Closed Socket.");
                    } catch (IOException e1) {
                        Log.e(TAG, "ConnectThread: mSocket.close(); failed. " + e1.getMessage());
                    }
                    // connection failed
                    mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CONNECTIONFAILED).sendToTarget();
                    startAcceptThread(true);
                }
                // Once connected, reset ConnectThread
                synchronized (BluetoothConnection.this) {
                    mConnectThread = null;
                }
                Log.i(TAG, "ConnectThread: Stopped running");
            }
        }

        public void cancel() {
            try {
                Log.d(TAG, "ConnectThread: Closing socket");
                mSocket.close();
                Log.d(TAG, "ConnectThread: Socket closed");
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: mSocket.close(); failed. " + e.getMessage());
            }
        }
    }

    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Started");
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                // getting the in/out put streamfrom the socket
                Log.d(TAG, "ConnectedThread: " + socket.getRemoteDevice().getName() + ", BluetoothSocket: " + mmSocket.toString());
                Log.d(TAG, "ConnectedThread: Getting input/output stream from the socket");
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
                mBTConversationArrayList = getmBTConversationArrayList();
                mBTCommandArrayList = getmBTCommandArrayList();

                // save the socket of the last connected device
                configFile.getBluetoothConfig().setLastConnectedDevice_Name(mConnectedRemoteDevice.getName());
                configFile.getBluetoothConfig().setLastConnectedDevice_MACAddress(mConnectedRemoteDevice.getAddress());
                configFileHandler.writeToExternalStorage(configFile);
                // update BT connection status
                setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_CONNECTED);
                mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_CONNECTED, 0).sendToTarget();
            } catch (IOException e) {
                setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_CONNECTING);
                mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_CONNECTING, 0).sendToTarget();
                Log.e(TAG, "ConnectedThread: Error getting input/output stream from the socket. ");
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d(TAG, "ConnectedThread: Running");
            Log.d(TAG, "ConnectedThread: BluetoothSocket: " + mmSocket.toString());

            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "ConnectedThread: InputStream: " + incomingMessage);
                    BluetoothMessageEntity mBluetoothMessageEntity;
                    if (incomingMessage.contains("GRID ")) {
                        // command message received
                        mBluetoothMessageEntity = new BluetoothMessageEntity(mConnectedRemoteDevice.getName(), "MDPGRP17_Android", MESSAGE_COMMAND, incomingMessage);
                        mBTCommandArrayList.add(mBluetoothMessageEntity);
                    } else {
                        // TODO:
                        char destination = incomingMessage.charAt(0);
                        char source = incomingMessage.charAt(1);
                        String content = incomingMessage.substring(2);


                        mBluetoothMessageEntity = new BluetoothMessageEntity(mConnectedRemoteDevice.getName(), "MDPGRP17", MESSAGE_CONVERSATION, incomingMessage);
                        mBTConversationArrayList.add(mBluetoothMessageEntity);
                    }

                    // mHandler.obtainMessage(GlobalVariables.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    mHandler.obtainMessage(GlobalVariables.MESSAGE_READ, mBluetoothMessageEntity).sendToTarget();
                } catch (IOException e) {
                    setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_LISTENING);
                    mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_LISTENING, 0).sendToTarget();
                    Log.e(TAG, "ConnectedThread: Write: mmInStream.read(buffer); failed. " + e.getMessage());

                    // connection lost
                    if (isUserDisconnect) {
                        // no need to reconnect to last device
                    } else {
                        stopAllThreads();
                        startAcceptThread(true);
                        mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CONNECTIONLOST).sendToTarget();
                        // startConnectThread(mConnectedRemoteDevice, true);
                    }

                    break;
                }
            }
            Log.i(TAG, "ConnectedThread: Stopped running");
        }

        //Call this from the main activity to send data to the remote device
        public void write(BluetoothMessageEntity bluetoothMessageEntity) {
            byte[] bytes = bluetoothMessageEntity.getMessageContent().getBytes();
            String outgoingMessage = bluetoothMessageEntity.getMessageContent();
            try {
                Log.d(TAG, "ConnectedThread: Writing to outputstream: " + outgoingMessage);
                mmOutStream.write(bytes);

                if (bluetoothMessageEntity.getMessageType() == MESSAGE_COMMAND) {
                    String command = bluetoothMessageEntity.getMessageContent();
                    if (command.equals(CMD_FORWARD) || command.equals(CMD_REVERSE) || command.equals(CMD_ROTATELEFT) || command.equals(CMD_ROTATERIGHT)) {
                        mBTCommandArrayList.add(bluetoothMessageEntity);
                        write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
                    } else {
                        // other commands such as begin exploration, fastest
                        mBTCommandArrayList.add(bluetoothMessageEntity);
                    }
                }
                if (bluetoothMessageEntity.getMessageType() == MESSAGE_CONVERSATION) {
                    mBTConversationArrayList.add(bluetoothMessageEntity);
                }
                mHandler.obtainMessage(GlobalVariables.MESSAGE_WRITE, bluetoothMessageEntity).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: mmOutStream.write(bytes); failed. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                Log.d(TAG, "ConnectedThread: Closing Socket");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: mSocket.close(); failed. " + e.getMessage());
            }
        }

    }

}

