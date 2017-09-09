package com.example.android.mdpgrp17_androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";

    // Bluetooth Objects
    BluetoothConnection mBluetoothConnection;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mPairedDevice;
    public ArrayList<BluetoothMessageEntity> mBTMsgArrayList;
    public ArrayAdapter mBTMsgArrayAdapter;
    // UI Objects
    EditText editText_message;
    public ImageButton imageButton_send;
    ListView listView_message;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        // Bluetooth Object
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothConnection = BluetoothConnection.getmBluetoothConnection(mHandler);
        mBluetoothConnection.setHandler(mHandler);
        mBTMsgArrayList = mBluetoothConnection.getmBTConversationArrayList();


        // UI Objects
        editText_message = (EditText) rootView.findViewById(R.id.editText_message);
        imageButton_send = (ImageButton) rootView.findViewById(R.id.imageButton_send);
        listView_message = (ListView) rootView.findViewById(R.id.listView_message);
        updateGUI_MessageContent();

        imageButton_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Image Button Send Clicked.");
                mBluetoothAdapter.cancelDiscovery();
                String messageToSend = editText_message.getText().toString();
                mBluetoothConnection.write(BluetoothMessageEntity.sendConversation(messageToSend));
                editText_message.getText().clear();

            }
        });
        return rootView;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "mHandler: msg.what: " + msg.what);
            Log.d(TAG, "mHandler: msg.arg1: " + msg.arg1);
            Log.d(TAG, "mHandler: msg.arg2: " + msg.arg2);
            Log.d(TAG, "mHandler: msg.toString(): " + msg.toString());
            switch (msg.what) {
                case GlobalVariables.BT_CONNECTION_STATE_CHANGE: {

                    break;
                }
                case GlobalVariables.MESSAGE_READ: {
                    if (msg.obj instanceof BluetoothMessageEntity) {
                        updateGUI_MessageContent();
                    }
                    break;
                }
                case GlobalVariables.MESSAGE_WRITE: {
                    if (msg.obj instanceof BluetoothMessageEntity) {
                        updateGUI_MessageContent();
                    }
                    break;
                }
            }
        }
    };


    public void updateGUI_MessageContent() {
        Log.d(TAG, "updateGUI_MessageContent");
        mBTMsgArrayList = mBluetoothConnection.getmBTConversationArrayList();
        // message object
        mBTMsgArrayAdapter = new ArrayAdapter<BluetoothMessageEntity>(getContext(), R.layout.item_message,
                R.id.textView_message, mBTMsgArrayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView_message = ((TextView) view.findViewById(R.id.textView_message));
                BluetoothMessageEntity bluetoothMessageEntity = this.getItem(position);
                if (bluetoothMessageEntity.getFrom().equals("MDPGRP17")) {
                    // FROM MDPGRP17 = sender
                    textView_message.setBackgroundResource(R.color.sender_background);
                    textView_message.setText(bluetoothMessageEntity.getMessageContent());
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textView_message.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    textView_message.setLayoutParams(params); //causes layout update
                } else {
                    // FROM mRemoteDevice = receiver
                    textView_message.setBackgroundResource(R.color.receiver_background);
                    textView_message.setText(bluetoothMessageEntity.getMessageContent());
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textView_message.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    textView_message.setLayoutParams(params); //causes layout update
                }
                return view;
            }
        };
        mBTMsgArrayAdapter.notifyDataSetChanged();
        listView_message.setAdapter(mBTMsgArrayAdapter);
        listView_message.post(new Runnable() {
            public void run() {
                listView_message.setSelection(listView_message.getCount() - 1);
            }
        });
    }

    // Create a BroadcastReceiver to capture bluetooth activities
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final int currentAdapterState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            final int currentDeviceState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            final int previousDeviceState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
            BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                // 10 = OFF,11 = TURNING OFF, 11 = TURNING ON, 12 = ON
                Log.d(TAG, "onReceive: ACTION_STATE_CHANGED (" + BluetoothAdapter.STATE_ON + "/" +
                        BluetoothAdapter.STATE_TURNING_ON + "/" +
                        BluetoothAdapter.STATE_TURNING_OFF + "/" +
                        BluetoothAdapter.STATE_OFF + ")");
                Log.d(TAG, "currentDeviceState: " + currentAdapterState);
                if (currentAdapterState == BluetoothAdapter.STATE_ON) {
                    Log.d(TAG, "onReceive: STATE ON");
                }
                if (currentAdapterState == BluetoothAdapter.STATE_OFF) {
                    Log.d(TAG, "onReceive: STATE OFF");
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_STARTED");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED");
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_FOUND; Device added: " + mDevice.getName() + ", " + mDevice.getBondState());
            }
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_BOND_STATE_CHANGED, mPairedDevice: " + mPairedDevice.getName());
                // 10 = bond_none,11 = bond_bonding,12 = bond_bonded
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED. ");
                    mPairedDevice = mDevice; // assign the bonded device to the variable so it can be access to the fragment
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

}
