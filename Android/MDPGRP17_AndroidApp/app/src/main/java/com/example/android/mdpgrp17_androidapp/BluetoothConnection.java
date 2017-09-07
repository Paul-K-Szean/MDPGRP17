package com.example.android.mdpgrp17_androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTING;


/**
 * Created by szean on 1/9/2017.
 */

public class BluetoothConnection {
    private static final String TAG = "BluetoothConnection";
    private static final String SERVERNAME_SECURE = "MDP_AndroidApp_Secure";
    private static final String SERVERNAME_INSECURE = "MDP_AndroidApp_Insecure";

    // 00001115-0000-1000-8000-00805f9b34fb || 00001101-0000-1000-8000-00805f9b34fb
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static UUID REMOTE_UUID_SECURE;
    private static UUID REMOTE_UUID_INSECURE;

    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mConnectedRemoteDevice;   // connected remote device
    private BluetoothMessageEntity mBluetoothMessageEntity;
    private int mBTCurrentState;
    private Handler mHandler;

    // constructor
    public BluetoothConnection(Handler mHandler) {
        int checkCurrentState = getBTConnectionState();
        Log.d(TAG, "BluetoothConnection: getBTConnectionState(): " + checkCurrentState);
        if (checkCurrentState != GlobalVariables.BT_CONNECTION_STATE_LISTENING ||
                checkCurrentState != GlobalVariables.BT_CONNECTION_STATE_CONNECTED)
            this.mBTCurrentState = GlobalVariables.BT_CONNECTION_STATE_IDLE;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = mHandler;
    }

    public void setHandler(Handler handler) {
        Log.d(TAG, "setHandler");
        mHandler = handler;
    }

    private static BluetoothConnection mBluetoothConnection;

    public static BluetoothConnection getmBluetoothConnection(Handler mHandler) {
        if (mBluetoothConnection == null) {
            Log.d(TAG, "getmBluetoothConnection: BluetoothConnection was null");
            mBluetoothConnection = new BluetoothConnection(mHandler);
        }
        return mBluetoothConnection;
    }

    private ArrayList<BluetoothMessageEntity> mBTMsgArrayList;

    public ArrayList<BluetoothMessageEntity> getmBTMsgArrayList() {
        if (mBTMsgArrayList == null) {
            Log.d(TAG, "getmBTMsgArrayList: mBTMsgArrayList was null, created new mBTMsgArrayList");
            mBTMsgArrayList = new ArrayList<BluetoothMessageEntity>();
        }
        Log.d(TAG, "getmBTMsgArrayList: mBTMsgArrayList was not null, size: " + mBTMsgArrayList.size() + ", returned mBTMsgArrayList");
        return mBTMsgArrayList;
    }


    /**
     * Start the chat service. Specifically startAcceptThread AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void startAcceptThread(boolean isSecureConnection) {
        // startAcceptThread the acceptThread
        Log.d(TAG, "startAcceptThread");
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {   // exists = cancel and create new
            Log.d(TAG, "startAcceptThread: mConnectThread.cancel() invoked");
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            Log.d(TAG, "startAcceptThread: mConnectedThread.cancel() invoked");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel any thread currently running a connection
        // startAcceptThread to listen on a BluetoothServerSocket
        if (isSecureConnection) {
            if (mSecureAcceptThread == null) {
                mSecureAcceptThread = new AcceptThread(isSecureConnection);
                mSecureAcceptThread.start();
            }
        } else {
            if (mInSecureAcceptThread == null) {
                mInSecureAcceptThread = new AcceptThread(isSecureConnection);
                mInSecureAcceptThread.start();
            }
        }
    }

    /**
     * Explicitly start the ConnectThread as the remote device is paired
     */
    public synchronized void startConnectThread(BluetoothDevice mPairedDevice, boolean secure) {
        Log.d(TAG, "startConnectThread");
        // cancel any thread attempt to make a connection
        if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTING) {
            if (mConnectThread != null) {
                Log.d(TAG, "startConnectThread: mConnectThread.cancel() invoked. " + mConnectThread.getName());
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }


        // cancel any thread currently running a connection
        if (mConnectedThread != null) {
            Log.d(TAG, "startConnectThread: mConnectedThread.cancel() invoked. " + mConnectedThread.getName());
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // startConnectThread with the paired device
        Log.d(TAG, "startConnectThread: mPairedDevice: " + mPairedDevice.getName());
        mConnectThread = new ConnectThread(mPairedDevice, secure);
        mConnectThread.start();
    }

    /**
     * */
    public synchronized void startConnectedThread(BluetoothSocket mmSocket, BluetoothDevice mmDevice, boolean isConnectionSecure) {
        Log.d(TAG, "startConnectedThread");
        // cancel any thread attempt to make a connection
        if (mConnectThread != null) {
            Log.d(TAG, "startConnectedThread: mConnectThread.cancel() invoked");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // cancel any thread currently running a connection
        if (mConnectedThread != null) {
            Log.d(TAG, "startConnectedThread: mConnectedThread.cancel() invoked");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // cancel the accept thread because we only want to connect to one device
        if (isConnectionSecure) {
            if (mSecureAcceptThread != null) {
                mSecureAcceptThread.cancel();
                mSecureAcceptThread = null;
            }
        } else {
            if (mInSecureAcceptThread != null) {
                mInSecureAcceptThread.cancel();
                mInSecureAcceptThread = null;
            }
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket, isConnectionSecure);
        mConnectedThread.start();

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

        setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_OFF);
        mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_OFF);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        Log.d(TAG, "Write()");
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket - current device hosting the server socket
        // When yconnecting two devices, one must act as a server by holding an open BluetoothServerSocket.
        private final BluetoothServerSocket mServerSocket;
        private BluetoothDevice mRemoteDevice;
        private boolean isConnectionSecure;

        public AcceptThread(boolean isConnectionSecure) {
            Log.d(TAG, "AcceptThread: Started");
            // Use a temporary object that is later assigned to mServerSocket
            // because mServerSocket is final.
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
                Log.d(TAG, "AcceptThread: BluetoothServerSocket: " + tmp.toString());
                if (mBTCurrentState != GlobalVariables.BT_CONNECTION_STATE_LISTENING) {
                    setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_LISTENING);
                    mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_LISTENING, 0).sendToTarget();
                }
            } catch (IOException e) {
                // cannot start listening for connection
                setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_IDLE);
                mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_IDLE, 0).sendToTarget();
                if (isConnectionSecure) {
                    Log.e(TAG, "AcceptThread: mBluetoothAdapter.listenUsingRfcommWithServiceRecord(): " + e.getMessage());
                } else {
                    Log.e(TAG, "AcceptThread: mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(): " + e.getMessage());
                }

            }
            mServerSocket = tmp;
        }

        public void run() { // automatically executed
            Log.d(TAG, "AcceptThread: Running");
            if (mServerSocket != null) {
                BluetoothSocket mSocket = null;
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    Log.d(TAG, "AcceptThread: RFCOM server socket started");
                    mSocket = mServerSocket.accept();
                    Log.d(TAG, "AcceptThread: RFCOM server socket accepted a connection. Remote device: " + mSocket.getRemoteDevice().getName());
                    mRemoteDevice = mSocket.getRemoteDevice();
                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread: mServerSocket.accept(): " + e.getMessage());
                }
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                if (mSocket != null) {
                    startConnectedThread(mSocket, mSocket.getRemoteDevice(), isConnectionSecure);
                    Log.e(TAG, "AcceptThread: Connected to remote device");
                    // Close the socket
                    try {
                        Log.e(TAG, "AcceptThread: Closing bluetooth socket");
                        mSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "AcceptThread: socket.close(); failed. " + e.getMessage());
                    }
                }
                Log.i(TAG, "AcceptThread: Stopped running");
            } else {

            }
        }

        public void cancel() {
            try {
                Log.d(TAG, "AcceptThread: Closing server socket");
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
            for (Parcelable UUID : mRemoteDevice.getUuids()) {
                Log.d(TAG, "Remote device UUID(s): " + UUID.toString());
            }

            REMOTE_UUID_INSECURE = REMOTE_UUID_SECURE = UUID.fromString(mRemoteDevice.getUuids()[0].toString());
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
                    mConnectedRemoteDevice = mRemoteDevice;
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

                }

                // Once connected, reset ConnectThread
                synchronized (BluetoothConnection.this) {
                    mConnectThread = null;
                }


                if (mSocket.isConnected()) {
                    Log.d(TAG, "ConnectThread: Connected with " + mRemoteDevice.getName());
                    startConnectedThread(mSocket, mRemoteDevice, isConnectionSecure);
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
        private boolean isConnectionSecure;

        public ConnectedThread(BluetoothSocket socket, boolean isConnectionSecure) {
            Log.d(TAG, "ConnectedThread: Started");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            this.isConnectionSecure = isConnectionSecure;
            try {
                // getting the in/out put streamfrom the socket
                Log.d(TAG, "ConnectedThread: BluetoothSocket: " + mmSocket.toString());
                Log.d(TAG, "ConnectedThread: Getting input/output stream from the socket");
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
                mBTMsgArrayList = getmBTMsgArrayList();
                setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_CONNECTED);
                mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_CONNECTED, 0).sendToTarget();
                // update recycle view
            } catch (IOException e) {
                setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_CONNECTING);
                mHandler.obtainMessage(GlobalVariables.BT_CONNECTION_STATE_CHANGE, GlobalVariables.BT_CONNECTION_STATE_CONNECTING, 0).sendToTarget();
                Log.e(TAG, "ConnectedThread: Error getting input/output stream from the socket. ");
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        // TODO: seperate Conversation and Command list
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
                    Log.d(TAG, "mBTMsgArrayList size: " + mBTMsgArrayList.size());

                    mBluetoothMessageEntity = new BluetoothMessageEntity(mConnectedRemoteDevice.getName(), "MDPGRP17", incomingMessage);
                    mBTMsgArrayList.add(mBluetoothMessageEntity);
                    // mHandler.obtainMessage(GlobalVariables.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    mHandler.obtainMessage(GlobalVariables.MESSAGE_READ, mBluetoothMessageEntity).sendToTarget();
                } catch (IOException e) {
                    // Disconnection
                    Log.e(TAG, "ConnectedThread: Write: mmInStream.read(buffer); failed. " + e.getMessage());
                    break;
                }
            }
            Log.i(TAG, "ConnectedThread: Stopped running");
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String outgoingMessage = new String(bytes, Charset.defaultCharset());
            try {
                Log.d(TAG, "ConnectedThread: Writing to outputstream: " + outgoingMessage);
                mmOutStream.write(bytes);
                mBluetoothMessageEntity = new BluetoothMessageEntity("MDPGRP17", mConnectedRemoteDevice.getName(), outgoingMessage);
                mBTMsgArrayList.add(mBluetoothMessageEntity);
                mHandler.obtainMessage(GlobalVariables.MESSAGE_WRITE, mBluetoothMessageEntity).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: mmOutStream.write(bytes); failed. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                Log.d(TAG, "ConnectedThread: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread: mSocket.close(); failed. " + e.getMessage());
            }
        }
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
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_LISTENING (" + mBTCurrentState + ")");
                break;
            case 6:
                Log.d(TAG, "getBTConnectionState: Current State: BT_CONNECTION_STATE_CHANGE (" + mBTCurrentState + ")");
                break;
        }
        return mBTCurrentState;
    }

    public BluetoothDevice getConnectedRemoteDevice() {
        if (mConnectedRemoteDevice != null)
            return mConnectedRemoteDevice;
        else {
            return null;
        }
    }


}

