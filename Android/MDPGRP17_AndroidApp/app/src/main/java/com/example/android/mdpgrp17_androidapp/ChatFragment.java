package com.example.android.mdpgrp17_androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";

    // Bluetooth Objects
    BluetoothConnection mBluetoothConnection;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mPairedDevice;
    public ArrayList<BluetoothMessageEntity> mBTMsgArrayList;
    public ArrayAdapter mBTMsgArrayAdapter;
    // UI Objects
    EditText ET_MessageArea;
    TextView TV_Notice;
    public ImageButton imageButton_send;
    ListView listView_MessageContent;
    RelativeLayout layout_MessageArea;

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
        ET_MessageArea = (EditText) rootView.findViewById(R.id.ET_MessageArea);
        TV_Notice = (TextView) rootView.findViewById(R.id.TV_Notice);
        imageButton_send = (ImageButton) rootView.findViewById(R.id.imageButton_Send);
        listView_MessageContent = (ListView) rootView.findViewById(R.id.listView_MessageContent);
        layout_MessageArea = (RelativeLayout) rootView.findViewById(R.id.layout_MessageArea);
        updateGUI_MessageContent();

        imageButton_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Image Button Send Clicked.");
                mBluetoothAdapter.cancelDiscovery();
                String messageToSend = ET_MessageArea.getText().toString();
                mBluetoothConnection.write(BluetoothMessageEntity.sendConversation(messageToSend));
                ET_MessageArea.getText().clear();

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
                    updateGUI_MessageContent();
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
        int checkBTCurrentState = mBluetoothConnection.getBTConnectionState();
        if (checkBTCurrentState == GlobalVariables.BT_CONNECTION_STATE_CONNECTED) {
            layout_MessageArea.setVisibility(View.VISIBLE);
            listView_MessageContent.setVisibility(View.VISIBLE);
            TV_Notice.setVisibility(View.INVISIBLE);
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
            listView_MessageContent.setAdapter(mBTMsgArrayAdapter);
            listView_MessageContent.post(new Runnable() {
                public void run() {
                    listView_MessageContent.setSelection(listView_MessageContent.getCount() - 1);
                }
            });
        } else {
            layout_MessageArea.setVisibility(View.INVISIBLE);
            listView_MessageContent.setVisibility(View.INVISIBLE);
            TV_Notice.setVisibility(View.VISIBLE);
        }
    }

}
