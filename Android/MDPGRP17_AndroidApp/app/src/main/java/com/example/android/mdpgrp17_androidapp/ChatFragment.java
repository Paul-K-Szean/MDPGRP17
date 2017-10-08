package com.example.android.mdpgrp17_androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "ChatFragment";

    // Bluetooth Objects
    BluetoothConnection mBluetoothConnection;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mPairedDevice;
    public ArrayList<BluetoothMessageEntity> mBTConversationArrayList;
    public ArrayList<BluetoothMessageEntity> mBTCommandArrayList;
    public ArrayAdapter mBTConversationArrayAdapter;
    public ArrayAdapter mBTCommandArrayAdapter; // recycler view adapter
    private int mBTCurrentState;
    // UI Objects
    private RelativeLayout RLO_MessageArea;
    private LinearLayout LLO_FunctionButtons;
    private ListView LTVW_MessageContent;
    private ListView LTVW_CommandContent;
    private EditText ETTXT_MessageArea;
    private TextView TXTVW_Notice;
    private TextView TXTVW_MessageLog;
    private TextView TXTVW_CommandLog;
    private ImageButton IMGBTN_Send;
    private Button BTN_Function01;
    private Button BTN_Function02;
    private Button BTN_Function03;

    private ConfigFileHandler configFileHandler;
    private ConfigFile configFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        // UI Objects
        RLO_MessageArea = (RelativeLayout) rootView.findViewById(R.id.RLO_MessageArea);
        LLO_FunctionButtons = (LinearLayout) rootView.findViewById(R.id.LLO_FunctionButtons);
        LTVW_MessageContent = (ListView) rootView.findViewById(R.id.LTVW_MessageContent);
        LTVW_CommandContent = (ListView) rootView.findViewById(R.id.LTVW_CommandContent);
        ETTXT_MessageArea = (EditText) rootView.findViewById(R.id.ETTXT_MessageArea);
        TXTVW_Notice = (TextView) rootView.findViewById(R.id.TXTVW_Notice);
        TXTVW_MessageLog = (TextView) rootView.findViewById(R.id.TXTVW_MessageLog);
        TXTVW_CommandLog = (TextView) rootView.findViewById(R.id.TXTVW_CommandLog);
        IMGBTN_Send = (ImageButton) rootView.findViewById(R.id.IMGBTN_Send);

        BTN_Function01 = (Button) rootView.findViewById(R.id.BTN_Function01);
        BTN_Function02 = (Button) rootView.findViewById(R.id.BTN_Function02);
        BTN_Function03 = (Button) rootView.findViewById(R.id.BTN_Function03);
        configFileHandler = new ConfigFileHandler();
        configFile = configFileHandler.getConfigFile();

        // Bluetooth Object
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothConnection = BluetoothConnection.getmBluetoothConnection(mHandler);
        mBluetoothConnection.setHandler(mHandler);
        mBTConversationArrayList = mBluetoothConnection.getmBTConversationArrayList();
        mBTCurrentState = mBluetoothConnection.getBTConnectionState();
        updateGUI_MessageContent();
        IMGBTN_Send.setOnClickListener(this);
        BTN_Function01.setOnClickListener(this);
        BTN_Function02.setOnClickListener(this);
        BTN_Function03.setOnClickListener(this);
        LTVW_MessageContent.setOnTouchListener(this);
        LTVW_CommandContent.setOnTouchListener(this);
        TXTVW_MessageLog.setOnTouchListener(this);
        TXTVW_CommandLog.setOnTouchListener(this);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ETTXT_MessageArea.getWindowToken(), 0);
        return rootView;
    }


    @Override
    public void onClick(View view) {
        int controlID = view.getId();
        if (controlID == R.id.IMGBTN_Send) {
            Log.d(TAG, "Clicked on IMGBTN_Send");
            mBluetoothAdapter.cancelDiscovery();
            String messageToSend = ETTXT_MessageArea.getText().toString();
            mBluetoothConnection.write(BluetoothMessageEntity.sendConversation(messageToSend));
            ETTXT_MessageArea.getText().clear();
        }
        String previousText = ETTXT_MessageArea.getText().toString().trim();

        if (controlID == R.id.BTN_Function01) {
            previousText += " " + configFileHandler.getConfigFile().getFunctionConfig().getFunction01();
            ETTXT_MessageArea.setText(previousText.trim());
            showVirtualKeyboard();
        }
        if (controlID == R.id.BTN_Function02) {
            previousText += " " + configFileHandler.getConfigFile().getFunctionConfig().getFunction02();
            ETTXT_MessageArea.setText(previousText.trim());
            showVirtualKeyboard();
        }
        if (controlID == R.id.BTN_Function03) {
            previousText += " " + configFileHandler.getConfigFile().getFunctionConfig().getFunction03();
            ETTXT_MessageArea.setText(previousText.trim());
            showVirtualKeyboard();
        }
        ETTXT_MessageArea.setSelection(ETTXT_MessageArea.getText().length());
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        int controlID = view.getId();
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            if (controlID == R.id.LTVW_MessageContent || controlID == R.id.LTVW_CommandContent ||
                    controlID == R.id.TXTVW_MessageLog || controlID == R.id.TXTVW_CommandLog) {
                Log.d(TAG, "onTouch: hide virtual keyboard");
                hideVirtualKeyboard();
            }
        }

        return false;
    }

    public void updateGUI_MessageContent() {
        Log.d(TAG, "updateGUI_MessageContent");
        if (mBTCurrentState == GlobalVariables.BT_CONNECTION_STATE_CONNECTED) {
            LLO_FunctionButtons.setVisibility(View.VISIBLE);
            RLO_MessageArea.setVisibility(View.VISIBLE);
            LTVW_MessageContent.setVisibility(View.VISIBLE);
            LTVW_CommandContent.setVisibility(View.VISIBLE);
            TXTVW_Notice.setVisibility(View.GONE);
            TXTVW_MessageLog.setVisibility(View.VISIBLE);
            TXTVW_CommandLog.setVisibility(View.VISIBLE);
            mBTConversationArrayList = mBluetoothConnection.getmBTConversationArrayList();
            mBTCommandArrayList = mBluetoothConnection.getmBTCommandArrayList();

            final LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // message object
            mBTConversationArrayAdapter = new ArrayAdapter<BluetoothMessageEntity>(getContext(), R.layout.item_message,
                    R.id.textView_message, mBTConversationArrayList) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    BluetoothMessageEntity bluetoothMessageEntity = this.getItem(position);
                    TextView textView_message;
                    if (bluetoothMessageEntity.getFrom().equals("MDPGRP17")) {
                        convertView = mInflater.inflate(R.layout.item_message_sender, null);
                    } else {
                        convertView = mInflater.inflate(R.layout.item_message_receiver, null);
                    }
                    textView_message = ((TextView) convertView.findViewById(R.id.textView_message));
                    textView_message.setText(bluetoothMessageEntity.getMessageContent());
                    return convertView;
                }
            };
            mBTConversationArrayAdapter.notifyDataSetChanged();
            LTVW_MessageContent.setAdapter(mBTConversationArrayAdapter);
            LTVW_MessageContent.post(new Runnable() {
                public void run() {
                    LTVW_MessageContent.setSelection(LTVW_MessageContent.getCount() - 1);
                }
            });

            // command object
            mBTCommandArrayAdapter = new ArrayAdapter<BluetoothMessageEntity>(getContext(), R.layout.item_message,
                    R.id.textView_message, mBTCommandArrayList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    BluetoothMessageEntity bluetoothMessageEntity = this.getItem(position);
                    TextView textView_message;
                    if (bluetoothMessageEntity.getFrom().equals("MDPGRP17")) {
                        convertView = mInflater.inflate(R.layout.item_message_sender, null);
                        textView_message = ((TextView) convertView.findViewById(R.id.textView_message));
                        textView_message.setText(bluetoothMessageEntity.getMessageContent());
                    } else {
                        convertView = mInflater.inflate(R.layout.item_message_receiver, null);
                        textView_message = ((TextView) convertView.findViewById(R.id.textView_message));
                        textView_message.setText(bluetoothMessageEntity.getMessageContent());
                    }
                    return convertView;
                }
            };
            mBTCommandArrayAdapter.notifyDataSetChanged();
            LTVW_CommandContent.setAdapter(mBTCommandArrayAdapter);
            LTVW_CommandContent.post(new Runnable() {
                public void run() {
                    LTVW_CommandContent.setSelection(LTVW_CommandContent.getCount() - 1);
                }
            });
        } else {
            TXTVW_MessageLog.setVisibility(View.INVISIBLE);
            TXTVW_CommandLog.setVisibility(View.INVISIBLE);
            RLO_MessageArea.setVisibility(View.INVISIBLE);
            LTVW_MessageContent.setVisibility(View.INVISIBLE);
            LTVW_CommandContent.setVisibility(View.INVISIBLE);
            LLO_FunctionButtons.setVisibility(View.INVISIBLE);
            TXTVW_Notice.setVisibility(View.VISIBLE);
        }


    }

    private void showVirtualKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void hideVirtualKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(ETTXT_MessageArea.getWindowToken(), 0);
        }
    }

    private void showToast_Short(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "mHandler: msg.what: " + msg.what);
            Log.d(TAG, "mHandler: msg.arg1: " + msg.arg1);
            Log.d(TAG, "mHandler: msg.arg2: " + msg.arg2);
            Log.d(TAG, "mHandler: msg.toString(): " + msg.toString());
            mBTCurrentState = mBluetoothConnection.getBTConnectionState();
            if (mBTCurrentState == GlobalVariables.BT_CONNECTION_STATE_CONNECTED) {
                RLO_MessageArea.setVisibility(View.VISIBLE);
                LTVW_MessageContent.setVisibility(View.VISIBLE);
                LLO_FunctionButtons.setVisibility(View.VISIBLE);
                TXTVW_Notice.setVisibility(View.INVISIBLE);
                switch (msg.what) {
                    case GlobalVariables.BT_CONNECTION_STATE_CHANGE: {
                        ChatActivity settingsActivity = (ChatActivity) getActivity();
                        settingsActivity.updateGUI_ToolBar_BTConnectionState();
                        updateGUI_MessageContent();
                        break;
                    }
                    case GlobalVariables.BT_CONNECTION_STATE_CONNECTIONFAILED: {
                        showToast_Short("Connection failed. Retrying... ");
                        break;
                    }
                    case GlobalVariables.BT_CONNECTION_STATE_CONNECTIONLOST: {
                        showToast_Short("Connection lost. Retrying... ");
                        break;
                    }
                    case GlobalVariables.MESSAGE_READ: {
                        if (msg.obj instanceof BluetoothMessageEntity) {
                            BluetoothMessageEntity bluetoothMessageEntity = (BluetoothMessageEntity) msg.obj;
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
            } else {
                RLO_MessageArea.setVisibility(View.INVISIBLE);
                LTVW_MessageContent.setVisibility(View.INVISIBLE);
                LTVW_CommandContent.setVisibility(View.INVISIBLE);
                LLO_FunctionButtons.setVisibility(View.INVISIBLE);
                TXTVW_Notice.setVisibility(View.VISIBLE);
            }
        }
    };


}
