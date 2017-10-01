/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.mdpgrp17_androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_CONTROL_MODE_BUTTON;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_CONTROL_MODE_SWIPE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_CONTROL_MODE_TILT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.ARENA_GAME_MODE_MANUALCONTROL;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CANNOTLISTEN;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTED;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_IDLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_LISTENING;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_FORWARD;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_REVERSE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_ROTATELEFT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_ROTATERIGHT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_SENDARENAINFO;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_COMMAND;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_FROM;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.TILT_MINIMUM_DOWN;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.TILT_MINIMUM_LEFT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.TILT_MINIMUM_RIGHT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.TILT_MINIMUM_UP;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.TILT_SENSITIVITY;
import static com.example.android.mdpgrp17_androidapp.R.color.color_ControlMode_Swipe;
import static com.example.android.mdpgrp17_androidapp.R.color.color_ControlMode_Tilt;


/**
 * Provides UI for the main screen.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, OnCheckedChangeListener, SensorEventListener {
    private static final String TAG = "MainActivity";

    // GUI Objects

    private Menu menu;
    private RelativeLayout RLO_ArenaGrid;
    private LinearLayout LLO_Fastest;
    private LinearLayout LLO_Exploration;
    private LinearLayout LLO_TiltMode;
    private LinearLayout LLO_WayPoint;
    private LinearLayout LLO_MapMode_PreviousNext;
    private LinearLayout LLO_ButtonControl;
    private Button BTN_CMD_Forward;
    private Button BTN_CMD_Reverse;
    private Button BTN_CMD_RotateLeft;
    private Button BTN_CMD_RotateRight;
    private Button BTN_MapMode_Previous;
    private Button BTN_MapMode_Next;
    private TextView TXTVW_RobotStatusValue;
    private TextView TXTVW_MapModeIndexValue;
    private TextView TXTVW_WayPointValue;
    private TextView TXTVW_WayPoint;
    private TextView TXTVW_ControlMode;
    private TextView TXTVW_CalibratedValues;
    private TextView TXTVW_SensorValues;
    private ToggleButton TGLBTN_MapMode;
    private ToggleButton TGLBTN_CalibrateTilt;
    private ToggleButton TGLBTN_StartTiltMode;
    // Bluetooth objects
    private BluetoothConnection mBluetoothConnection;
    private BluetoothAdapter mBluetoothAdapter;
    private int mBTCurrentState;
    // Arena objects
    private Arena arena;
    private boolean isWayPointLocked, isFastestStarted, isExplorationStarted, isTiltModeStarted;
    private int gameMode, controlMode;
    // Config objects
    private ConfigFileHandler configFileHandler;
    private ConfigFile configFile;
    // Tilt sensing object
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private ArrayList<Float> caliberate_UpDown, caliberate_LeftRight; // for calibrating tilt


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d(TAG, "onCreateOptionsMenu");
        updateGUI_MenuIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings_bluetooth) {
            Log.d(TAG, "onOptionsItemSelected: onClick: action_settings_bluetooth");
            Intent intent_bluetooth = new Intent(this, BluetoothActivity.class);
            startActivity(intent_bluetooth);
        } else if (id == R.id.action_settings_chat) {
            Log.d(TAG, "onOptionsItemSelected: onClick: action_settings_chat");
            Intent intent_settings = new Intent(this, ChatActivity.class);
            startActivity(intent_settings);
        } else if (id == R.id.action_settings_bluetooth_reconnect) {
            if (mBTCurrentState == BT_CONNECTION_STATE_IDLE) {
                Log.d(TAG, "onClick: action_settings_bluetooth_reconnect: BT_CONNECTION_STATE_IDLE");
                mBluetoothConnection.startAcceptThread(true);
            } else if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                Log.d(TAG, "onClick: action_settings_bluetooth_reconnect: BT_CONNECTION_STATE_CONNECTED");
                mBluetoothConnection.disconnect();
            } else if (mBTCurrentState == BT_CONNECTION_STATE_LISTENING) {
                Log.d(TAG, "onClick: action_settings_bluetooth_reconnect: BT_CONNECTION_STATE_LISTENING");
                BluetoothDevice lastConnectedDevice = mBluetoothConnection.getConnectedRemoteDevice();
                if (lastConnectedDevice != null) {
                    mBluetoothConnection.startConnectThread(lastConnectedDevice, true);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: BT_CONNECTION_STATE_LISTENING: lastConnectedDevice is null retry 1");
                    try {
                        lastConnectedDevice = mBluetoothAdapter.getRemoteDevice(configFileHandler.getConfigFile().getBluetoothConfig().getLastConnectedDevice_MACAddress());
                        if (lastConnectedDevice != null) {
                            mBluetoothConnection.startConnectThread(lastConnectedDevice, true);
                        } else {
                            Log.d(TAG, "onOptionsItemSelected: BT_CONNECTION_STATE_LISTENING: lastConnectedDevice is null retry 2");
                            showToast_Short("You have not connected to any device yet");
                            Log.d(TAG, "onOptionsItemSelected: You have not connected to any device yet");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showToast_Short("You have not connected to any device yet");
                    }
                }
            } else if (mBTCurrentState == BT_CONNECTION_STATE_CANNOTLISTEN) {
                Log.d(TAG, "onClick: action_settings_bluetooth_reconnect: BT_CONNECTION_STATE_LISTENING");
                showToast_Short("Please restart bluetooth");
            } else {
                Log.d(TAG, "onClick: action_settings_bluetooth_reconnect: " + mBTCurrentState);
            }
        } else if (id == R.id.action_settings_getarenainfo) {
            Log.d(TAG, "onOptionsItemSelected: onClick: action_settings_getarenainfo");
            if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
            } else {
                showToast_Short("You are not connected");
            }
        } else if (id == R.id.action_settings_controlmode_reset) {
            Log.d(TAG, "onOptionsItemSelected: onClick: action_settings_controlmode_reset");
            setup_ArenaGrid();
            arena.setMapMode(true);
            TGLBTN_MapMode.setChecked(true);
            LLO_MapMode_PreviousNext.setVisibility(View.INVISIBLE);
            arena.setSaveStateIndex_Pause(-1);
            arena.setSaveStateIndex_InArray(-1);
            arena.setupNakedGrid(); // reset
            // arena.setSaveStateArrayList(new ArrayList<ArenaSaveState>());
            isWayPointLocked = false;
            updateGUI_WayPoint();
            if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
            }
            TXTVW_MapModeIndexValue.setText("Auto");
            TXTVW_WayPointValue.setText("0,0");
            mBluetoothConnection.setmBTCommandArrayList(new ArrayList<BluetoothMessageEntity>());
        } else if (id == R.id.action_settings_controlmode_button) {
            Log.d(TAG, "onOptionsItemSelected: onClick: action_settings_controlmode_button");
            LLO_ButtonControl.setVisibility(VISIBLE);
            LLO_TiltMode.setVisibility(GONE);
            TXTVW_ControlMode.setVisibility(GONE);
            TGLBTN_CalibrateTilt.setVisibility(GONE);
            controlMode = ARENA_CONTROL_MODE_BUTTON;
            if (mSensorManager != null)
                mSensorManager.unregisterListener(this);
        } else if (id == R.id.action_settings_controlmode_swipe) {
            Log.d(TAG, "onOptionsItemSelected: onClick: action_settings_controlmode_swipe");
            LLO_ButtonControl.setVisibility(GONE);
            LLO_TiltMode.setVisibility(GONE);
            TXTVW_ControlMode.setVisibility(VISIBLE);
            TXTVW_ControlMode.setText("Swipe Mode");
            TXTVW_ControlMode.setTextColor(ContextCompat.getColor(this, color_ControlMode_Swipe));
            TGLBTN_CalibrateTilt.setVisibility(GONE);
            controlMode = ARENA_CONTROL_MODE_SWIPE;
            if (mSensorManager != null)
                mSensorManager.unregisterListener(this);

        } else if (id == R.id.action_settings_controlmode_tilt) {
            Log.d(TAG, "onOptionsItemSelected: onClick: action_settings_controlmode_tilt");
            LLO_ButtonControl.setVisibility(GONE);
            LLO_TiltMode.setVisibility(VISIBLE);
            TXTVW_ControlMode.setVisibility(VISIBLE);
            TXTVW_ControlMode.setText("Tilt Mode");
            TXTVW_ControlMode.setTextColor(ContextCompat.getColor(this, color_ControlMode_Tilt));
            controlMode = ARENA_CONTROL_MODE_TILT;
            float minimum_Up, minimum_Down, minimum_Left, minimum_Right;
            minimum_Up = Math.round(configFile.getTiltConfig().getMinimum_Up());
            minimum_Down = Math.round(configFile.getTiltConfig().getMinimum_Down());
            minimum_Left = Math.round(configFile.getTiltConfig().getMinimum_Left());
            minimum_Right = Math.round(configFile.getTiltConfig().getMinimum_Right());
            TXTVW_CalibratedValues.setText("Calibrated (" + minimum_Up + "," + minimum_Down + "," + minimum_Left + "," + minimum_Right + ")");
            TGLBTN_CalibrateTilt.setVisibility(VISIBLE);
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // GUI Objects
        RLO_ArenaGrid = (RelativeLayout) findViewById(R.id.RLO_ArenaGrid);
        LLO_Fastest = (LinearLayout) findViewById(R.id.LLO_Fastest);
        LLO_Exploration = (LinearLayout) findViewById(R.id.LLO_Exploration);
        LLO_TiltMode = (LinearLayout) findViewById(R.id.LLO_TiltMode);
        LLO_WayPoint = (LinearLayout) findViewById(R.id.LLO_WayPoint);
        LLO_MapMode_PreviousNext = (LinearLayout) findViewById(R.id.LLO_MapMode_PreviousNext);
        LLO_ButtonControl = (LinearLayout) findViewById(R.id.LLO_ButtonControl);
        BTN_CMD_Forward = (Button) findViewById(R.id.BTN_CMD_Forward);
        BTN_CMD_Reverse = (Button) findViewById(R.id.BTN_CMD_Reverse);
        BTN_CMD_RotateLeft = (Button) findViewById(R.id.BTN_CMD_RotateLeft);
        BTN_CMD_RotateRight = (Button) findViewById(R.id.BTN_CMD_RotateRight);

        BTN_MapMode_Previous = (Button) findViewById(R.id.BTN_MapMode_Previous);
        BTN_MapMode_Next = (Button) findViewById(R.id.BTN_MapMode_Next);
        TXTVW_RobotStatusValue = (TextView) findViewById(R.id.TXTVW_RobotStatusValue);
        TXTVW_MapModeIndexValue = (TextView) findViewById(R.id.TXTVW_MapModeIndexValue);
        TXTVW_WayPointValue = (TextView) findViewById(R.id.TXTVW_WayPointValue);
        TXTVW_WayPoint = (TextView) findViewById(R.id.TXTVW_WayPoint);
        TXTVW_ControlMode = (TextView) findViewById(R.id.TXTVW_ControlMode);
        TXTVW_CalibratedValues = (TextView) findViewById(R.id.TXTVW_CalibratedValues);
        TXTVW_SensorValues = (TextView) findViewById(R.id.TXTVW_SensorValues);
        TGLBTN_MapMode = (ToggleButton) findViewById(R.id.TGLBTN_MapMode);
        TGLBTN_CalibrateTilt = (ToggleButton) findViewById(R.id.TGLBTN_CalibrateTilt);
        TGLBTN_StartTiltMode = (ToggleButton) findViewById(R.id.TGLBTN_StartTiltMode);
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // set default mode
        controlMode = ARENA_CONTROL_MODE_BUTTON;
        gameMode = ARENA_GAME_MODE_MANUALCONTROL;
        // ConfigFileHandler & ConfigFile object
        configFileHandler = new ConfigFileHandler();
        configFile = configFileHandler.getConfigFile();
        // Bluetooth objects
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter intent_filter = new IntentFilter();
        // Register BT broadcasts when the bluetooth is turned ON/OFF
        intent_filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); // on/off state of bluetooth
        // Register BT broadcasts when a remote device is found
        intent_filter.addAction(BluetoothDevice.ACTION_FOUND);  // discovery found a device
        // Register BT broadcasts when the discovery started
        intent_filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); // start discovery
        // Register BT broadcasts when the discovery finished
        intent_filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); // end discovery
        // Register BT broadcasts when the remote device is not paired, pairing or paired
        intent_filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); // NONE/BONDING/BONDED
        // Register BT broadcasts when the remote device is connected
        intent_filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        // Register BT broadcasts when the remote device request a disconnection
        intent_filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        // Register BT broadcasts when the remote device is disconnected
        intent_filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        // Register BT broadcasts when the there is a connection change
        intent_filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        //  Register all the activities
        registerReceiver(mReceiver, intent_filter);
        LLO_Fastest.setOnTouchListener(this);
        LLO_Exploration.setOnTouchListener(this);
        LLO_WayPoint.setOnTouchListener(this);
        RLO_ArenaGrid.setOnTouchListener(this);
        BTN_CMD_Forward.setOnClickListener(this);
        BTN_CMD_Reverse.setOnClickListener(this);
        BTN_CMD_RotateLeft.setOnClickListener(this);
        BTN_CMD_RotateRight.setOnClickListener(this);

        BTN_MapMode_Previous.setOnClickListener(this);
        BTN_MapMode_Next.setOnClickListener(this);
        TGLBTN_MapMode.setOnCheckedChangeListener(this);
        TGLBTN_CalibrateTilt.setOnCheckedChangeListener(this);
        TGLBTN_StartTiltMode.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        mBluetoothConnection = BluetoothConnection.getmBluetoothConnection(mHandler);
        mBluetoothConnection.setHandler(mHandler);
        mBTCurrentState = mBluetoothConnection.getBTConnectionState();
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "onResume: BT on");
            // BT on
            if (mBTCurrentState == BT_CONNECTION_STATE_IDLE) {
                mBluetoothConnection.startAcceptThread(true);
                setup_ArenaGrid();
            }
            updateGUI_ToolBar_BTConnectionState();
            updateGUI_MenuIcon();
        } else {
            // BT off
            Log.d(TAG, "onResume: BT off");
            TXTVW_RobotStatusValue.setText("None");
            setup_ArenaGrid();
        }

        if (controlMode == ARENA_CONTROL_MODE_TILT) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            if (mSensorManager != null)
                mSensorManager.unregisterListener(this);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mBluetoothConnection != null) {
            mBluetoothConnection.stopAllThreads();
        }
        unregisterReceiver(mReceiver);
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
        super.onBackPressed();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int controlID = view.getId();

        // to lock, unlock way point
        if (controlID == R.id.LLO_WayPoint) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "onTouch: isWayPointLocked: " + isWayPointLocked + "; " + arena.getWayPointPosition_Col_Center() + "," + arena.getWayPointPosition_Row_Center());
                    if (isWayPointLocked) {
                        // go to unlock mode
                        isWayPointLocked = false;
                    } else {
                        // go to locked mode
                        if (arena.getWayPointPosition_Row_Center() != 0 && arena.getWayPointPosition_Col_Center() != 0) {
                            isWayPointLocked = true;
                            if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                                mBluetoothConnection.write(BluetoothMessageEntity.sendCommand("WayPoint (" + arena.getWayPointPosition_Col_Center() + "," + arena.getWayPointPosition_Row_Center() + ")"));
                            } else {
                                showToast_Long("Unable to send way point! Re-lock way point to send again");
                            }
                        } else {
                            isWayPointLocked = false;
                            showToast_Short("Please select a way point first");
                        }
                    }
                    updateGUI_WayPoint();
            }
        }


        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        float x = -sensorEvent.values[0];
        float y = -sensorEvent.values[1];
        // check if calibration mode is on/off
        if (TGLBTN_CalibrateTilt.isChecked()) {
            Log.d(TAG, "onSensorChanged: Calibration On: " + x + "," + y + ", Math.round: " + Math.round(x) + "," + Math.round(y));
            // add new reading to calibrate tilt
            caliberate_UpDown.add(y);
            caliberate_LeftRight.add(x);

            float maximum_Up, maximum_Down, maximum_Left, maximum_Right;
            maximum_Up = Math.round(Collections.max(caliberate_UpDown));
            maximum_Down = Math.round(Collections.min(caliberate_UpDown));
            maximum_Left = Math.round(Collections.min(caliberate_LeftRight));
            maximum_Right = Math.round(Collections.max(caliberate_LeftRight));
            if (maximum_Up == 0) {
                maximum_Up = TILT_MINIMUM_UP;
            }
            if (maximum_Down == 0) {
                maximum_Down = TILT_MINIMUM_DOWN;
            }
            if (maximum_Left == 0) {
                maximum_Left = TILT_MINIMUM_LEFT;
            }
            if (maximum_Right == 0) {
                maximum_Right = TILT_MINIMUM_RIGHT;
            }
            TXTVW_CalibratedValues.setText("Calibrate (" + maximum_Up + "," + maximum_Down + "," + maximum_Left + "," + maximum_Right + ")");
        } else {
            float minimum_Up, minimum_Down, minimum_Left, minimum_Right; // reading from external storage
            minimum_Up = configFile.getTiltConfig().getMinimum_Up();
            minimum_Down = configFile.getTiltConfig().getMinimum_Down();
            minimum_Left = configFile.getTiltConfig().getMinimum_Left();
            minimum_Right = configFile.getTiltConfig().getMinimum_Right();
            if (Math.abs(y) > Math.abs(x)) {
                if (y > minimum_Up) {
                    Log.d(TAG, "onSensorChanged: Device tilt up: " + y + ", minimum_Up: " + minimum_Up);
                    if (y < (y + TILT_SENSITIVITY) && isTiltModeStarted) {
                        moveRobot(CMD_FORWARD);
                    }
                }
                if (y < minimum_Down) {
                    Log.d(TAG, "onSensorChanged: Device tilt down: " + y + ", minimum_Down: " + minimum_Down);
                    if (y > (y + (-TILT_SENSITIVITY)) && isTiltModeStarted) {
                        moveRobot(CMD_REVERSE);
                    }
                }
            } else if (Math.abs(x) > Math.abs(y)) {
                if (x < minimum_Left) {
                    Log.d(TAG, "onSensorChanged: Device tilt left: " + x + ", minimum_Left: " + minimum_Left);
                    if (x > (x + (-TILT_SENSITIVITY)) && isTiltModeStarted) {
                        moveRobot(CMD_ROTATELEFT);
                    }
                }
                if (x > minimum_Right) {
                    Log.d(TAG, "onSensorChanged: Device tilt right: " + x + ", minimum_Right: " + minimum_Right);
                    if (x < (x + TILT_SENSITIVITY) && isTiltModeStarted) {
                        moveRobot(CMD_ROTATERIGHT);
                    }
                }
            } else if (y > minimum_Up && y < minimum_Down && x > minimum_Left && x < minimum_Right) {
                Log.d(TAG, "onSensorChanged: Device not tilt: " + x + "," + y);
            } else {
                Log.d(TAG, "onSensorChanged: " + x + "," + y);
            }

        }
        TXTVW_SensorValues.setText("Sensor (" + Math.round(x) + "," + Math.round(y) + ")");
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        Log.d(TAG, "onAccuracyChanged");
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int controlID = compoundButton.getId();
        // toggle map mode, auto / manual
        if (controlID == R.id.TGLBTN_MapMode) {
            Log.d(TAG, "onCheckedChanged: TGLBTN_MapMode: " + compoundButton.isChecked());
            // set the map mode
            arena.setMapMode(TGLBTN_MapMode.isChecked());
            if (compoundButton.isChecked()) {
                // auto map mode
                LLO_MapMode_PreviousNext.setVisibility(View.INVISIBLE);
                TXTVW_MapModeIndexValue.setText("At: " + arena.getSaveStateIndex_InArray() + "/" + (arena.getArenaSaveStateArrayListSize() - 1));
                arena.setSaveStateIndex_Pause(-1); // reset the pause index
                arena.setSaveStateIndex_InArray(-1);// reset the transverse index
            } else {
                // manual map mode
                LLO_MapMode_PreviousNext.setVisibility(VISIBLE);
                // set the pause index to the current arena info size
                arena.setSaveStateIndex_Pause((arena.getArenaSaveStateArrayListSize() - 1));
                // check if index of transverse was set to the initial value of the pause index
                if (arena.getSaveStateIndex_InArray() == -1)
                    arena.setSaveStateIndex_InArray(arena.getSaveStateIndex_Pause());
                // arena.printArenaInfo();
            }
        }
        // toggle calibration mode, on / off
        if (controlID == R.id.TGLBTN_CalibrateTilt) {
            if (compoundButton.isChecked()) {
                // The toggle is enabled
                Log.d(TAG, "onCheckedChanged: TGLBTN_CalibrateTilt: " + compoundButton.isChecked() + " Calibrating tilt");
                if (isTiltModeStarted) {
                    TGLBTN_StartTiltMode.setChecked(false);
                }
                caliberate_UpDown = new ArrayList<Float>();
                caliberate_LeftRight = new ArrayList<Float>();
            } else {
                // The toggle is disabled
                Log.d(TAG, "onCheckedChanged: TGLBTN_CalibrateTilt: " + compoundButton.isChecked() + " Calibrated tilt: ");
                float maximum_Up, maximum_Down, maximum_Left, maximum_Right;
                maximum_Up = Math.round(Collections.max(caliberate_UpDown));
                maximum_Down = Math.round(Collections.min(caliberate_UpDown));
                maximum_Left = Math.round(Collections.min(caliberate_LeftRight));
                maximum_Right = Math.round(Collections.max(caliberate_LeftRight));
                if (maximum_Up == 0) {
                    maximum_Up = TILT_MINIMUM_UP;
                }
                if (maximum_Down == 0) {
                    maximum_Down = TILT_MINIMUM_DOWN;
                }
                if (maximum_Left == 0) {
                    maximum_Left = TILT_MINIMUM_LEFT;
                }
                if (maximum_Right == 0) {
                    maximum_Right = TILT_MINIMUM_RIGHT;
                }
                configFile.getTiltConfig().setMinimum_Up(maximum_Up);
                configFile.getTiltConfig().setMinimum_Down(maximum_Down);
                configFile.getTiltConfig().setMinimum_Left(maximum_Left);
                configFile.getTiltConfig().setMinimum_Right(maximum_Right);

                configFileHandler.writeToExternalStorage(configFile); // save to external storage
                float minimum_Up, minimum_Down, minimum_Left, minimum_Right;
                minimum_Up = Math.round(configFile.getTiltConfig().getMinimum_Up());
                minimum_Down = Math.round(configFile.getTiltConfig().getMinimum_Down());
                minimum_Left = Math.round(configFile.getTiltConfig().getMinimum_Left());
                minimum_Right = Math.round(configFile.getTiltConfig().getMinimum_Right());
                TXTVW_CalibratedValues.setText("Calibrated (" + minimum_Up + "," + minimum_Down + "," + minimum_Left + "," + minimum_Right + ")");
            }
        }
        if (controlID == R.id.TGLBTN_StartTiltMode) {
            if (compoundButton.isChecked()) {
                Log.d(TAG, "onCheckedChanged: TGLBTN_StartTiltMode: " + compoundButton.isChecked() + ", isTiltModeStarted: " + isTiltModeStarted);
                TGLBTN_CalibrateTilt.setChecked(false);
                isTiltModeStarted = true;
                TXTVW_ControlMode.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_RobotPosition));

            } else {
                Log.d(TAG, "onCheckedChanged: TGLBTN_StartTiltMode: " + compoundButton.isChecked() + ", isTiltModeStarted: " + isTiltModeStarted);
                isTiltModeStarted = false;
                TXTVW_ControlMode.setTextColor(ContextCompat.getColor(this, R.color.color_ControlMode_Tilt));
            }
        }

    }

    public void onClick(View view) {
        int controlID = view.getId();
        // map mode manual, previous state
        if (controlID == R.id.BTN_MapMode_Previous) {
            if (arena.getSaveStateIndex_InArray() > 0) {
                Log.d(TAG, "onClick: BTN_MapMode_Previous: -1");
                arena.setSaveStateIndex_InArray(arena.getSaveStateIndex_InArray() - 1);
                if (arena.getSaveStateArrayList().get(arena.getSaveStateIndex_InArray()).getWayPointReached()) {
                    TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_RobotPosition));
                    TXTVW_WayPoint.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_RobotPosition));
                } else {
                    TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_WayPoint));
                    TXTVW_WayPoint.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_WayPoint));
                }
            } else {
                Log.d(TAG, "onClick: BTN_MapMode_Previous: You are at the start state!");
                showToast_Short("You are at the start state!");
            }
        }
        // map mode manual, next state
        if (controlID == R.id.BTN_MapMode_Next) {
            if (arena.getSaveStateIndex_InArray() < (arena.getArenaSaveStateArrayListSize() - 1)) {
                Log.d(TAG, "onClick: BTN_MapMode_Next: +1");
                arena.setSaveStateIndex_InArray(arena.getSaveStateIndex_InArray() + 1);

            } else {
                Log.d(TAG, "onClick: BTN_MapMode_Next: You can't view the future state yet!");
                showToast_Short("You can't view the future state yet!");
            }
        }

        // robot control movement
        if (controlID == R.id.BTN_CMD_Forward) {
            moveRobot(CMD_FORWARD);
        }
        if (controlID == R.id.BTN_CMD_Reverse) {
            moveRobot(CMD_REVERSE);
        }
        if (controlID == R.id.BTN_CMD_RotateLeft) {
            moveRobot(CMD_ROTATELEFT);
        }
        if (controlID == R.id.BTN_CMD_RotateRight) {
            moveRobot(CMD_ROTATERIGHT);
        }
        // update map index in manual mode
        if (!arena.getMapMode()) {
            // manual mode, update the index
            TXTVW_MapModeIndexValue.setText("At: " + arena.getSaveStateIndex_InArray() + "/" + (arena.getArenaSaveStateArrayListSize() - 1));
            if (arena.getSaveStateArrayList().get(arena.getSaveStateIndex_InArray()).getWayPointReached()) {
                TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_RobotPosition));
                TXTVW_WayPoint.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_RobotPosition));
            } else {
                TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_WayPoint));
                TXTVW_WayPoint.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_WayPoint));
            }
        }
    }

    public void moveRobot(String CMD_Movement) {
        if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
            if (isWayPointLocked) {
                switch (CMD_Movement) {
                    case CMD_FORWARD:
                        if (arena.checkMovement(CMD_FORWARD)) {
                            mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_FORWARD));
                            TXTVW_RobotStatusValue.setText("Moving forward");
                        } else {
                            showToast_Long("Unable to move forward");
                        }
                        break;
                    case CMD_REVERSE:
                        if (arena.checkMovement(CMD_REVERSE)) {
                            mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_REVERSE));
                            TXTVW_RobotStatusValue.setText("Reversing");
                        } else {
                            showToast_Long("Unable to reverse");
                        }
                        break;
                    case CMD_ROTATELEFT:
                        if (arena.checkMovement(CMD_ROTATELEFT)) {
                            mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_ROTATELEFT));
                            TXTVW_RobotStatusValue.setText("Rotating Left");
                        } else {
                            showToast_Long("Unable to rotate left");
                        }
                        break;
                    case CMD_ROTATERIGHT:
                        if (arena.checkMovement(CMD_ROTATERIGHT)) {
                            mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_ROTATERIGHT));
                            TXTVW_RobotStatusValue.setText("Rotating Right");
                        } else {
                            showToast_Long("Unable to rotate right");
                        }
                        break;
                }
            } else {
                showToast_Short("Please lock the way point first");
            }
        } else {
            Log.d(TAG, "Please turn on bluetooth and connect to a device");
            showToast_Short("Please turn on bluetooth and connect to a device");
        }
    }

    private void showToast_Short(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showToast_Long(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void updateGUI_MenuIcon() {
        Log.d(TAG, "updateMenuIcon");
        // update GUI menu icon
        if (menu != null) {
            if (mBluetoothAdapter.isEnabled()) {
                // BT ON
                menu.findItem(R.id.action_settings_bluetooth).setIcon(R.drawable.ic_bluetooth_black_24dp); // default
                menu.findItem(R.id.action_settings_bluetooth_reconnect).setVisible(true);
                if (mBluetoothAdapter != null && mBluetoothConnection != null) {

                    if (mBTCurrentState == GlobalVariables.BT_CONNECTION_STATE_DISCONNECTED) {
                        // default
                    } else if (mBTCurrentState == GlobalVariables.BT_CONNECTION_STATE_CONNECTING) {
                        menu.findItem(R.id.action_settings_bluetooth).setIcon(R.drawable.ic_bluetooth_searching_black_24dp);
                    } else if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                        menu.findItem(R.id.action_settings_bluetooth).setIcon(R.drawable.ic_bluetooth_connected_black_24dp);
                    } else if (mBTCurrentState == GlobalVariables.BT_CONNECTION_STATE_DISCONNECTING) {
                        // default
                    } else if (mBTCurrentState == BT_CONNECTION_STATE_IDLE) {
                        // default
                    } else if (mBTCurrentState == BT_CONNECTION_STATE_LISTENING) {
                        // default
                    }
                }
            } else {
                // BT OFF
                menu.findItem(R.id.action_settings_bluetooth).setIcon(R.drawable.ic_bluetooth_disabled_black_24dp);
                menu.findItem(R.id.action_settings_bluetooth_reconnect).setVisible(false);
            }
        } else {
            Log.e(TAG, "updateMenuIcon: Menu was null");
        }
    }

    public void updateGUI_WayPoint() {
        if (isWayPointLocked) {
            LLO_WayPoint.setBackgroundResource(R.drawable.border_locked);
        } else {
            LLO_WayPoint.setBackgroundResource(R.drawable.border_unlocked);
        }
    }

    public void updateGUI_ArenaGrid(String messageContent) {
        Log.d(TAG, "updateGUI_ArenaGrid");
        arena.decodeArenaInfo(messageContent);
    }

    private void updateGUI_ToolBar_BTConnectionState() {
        getSupportActionBar().setSubtitle("No device connected");// default status
        switch (mBTCurrentState) {
            case GlobalVariables.BT_CONNECTION_STATE_DISCONNECTED:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_DISCONNECTED");
                break;
            case GlobalVariables.BT_CONNECTION_STATE_CONNECTING:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_CONNECTING");
                break;
            case BT_CONNECTION_STATE_CONNECTED:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_CONNECTED");
                if (mBluetoothConnection.getConnectedRemoteDevice() != null) {
                    getSupportActionBar().setSubtitle("Connected to " + mBluetoothConnection.getConnectedRemoteDevice().getName());
                    mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));  // get arena info after reconnection
                    TXTVW_RobotStatusValue.setText("Idling");
                } else {
                    Log.e(TAG, "updateGUI_ToolBar_BTConnectionState: No device connected. Unable to find connected device");
                    getSupportActionBar().setSubtitle("No device connected");
                }
                break;
            case GlobalVariables.BT_CONNECTION_STATE_DISCONNECTING:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_DISCONNECTING");
                break;
            case BT_CONNECTION_STATE_IDLE:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_IDLE");
                break;
            case BT_CONNECTION_STATE_CANNOTLISTEN:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_CANNOTLISTEN");
                showToast_Short("Please restart bluetooth");
                break;
            case BT_CONNECTION_STATE_LISTENING:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_LISTENING");
                break;
            case GlobalVariables.BT_CONNECTION_STATE_OFF:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_OFF");
                break;
        }
    }

    private void updateGUI_ArenaRobotStatus() {
        TXTVW_RobotStatusValue.setText("Idling");
    }

    public void setup_ArenaGrid() {
        Log.d(TAG, "setup_ArenaGrid");
        if (arena != null) {
            RLO_ArenaGrid.removeView(arena);
            arena = null;
        }
        arena = new Arena(this, RLO_ArenaGrid);
        RLO_ArenaGrid.addView(arena);
        updateGUI_ArenaGrid("NakedArena");
    }

    private void hideVirtualKeyboard() {
        InputMethodManager imanager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imanager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "mHandler: msg.what: " + msg.what);
            Log.d(TAG, "mHandler: msg.arg1: " + msg.arg1);
            Log.d(TAG, "mHandler: msg.arg2: " + msg.arg2);
            Log.d(TAG, "mHandler: msg.toString(): " + msg.toString());
            mBTCurrentState = mBluetoothConnection.getBTConnectionState();
            switch (msg.what) {
                case GlobalVariables.BT_CONNECTION_STATE_CHANGE: {
                    updateGUI_ToolBar_BTConnectionState();
                    updateGUI_MenuIcon();
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
                        if (bluetoothMessageEntity.getMessageType() == MESSAGE_COMMAND) {
                            updateGUI_ArenaGrid(bluetoothMessageEntity.getMessageContent());
                            updateGUI_ArenaRobotStatus();
                        } else {
                            showToast_Short("Message received! (Chat)");
                        }
                    } else {
                        byte[] read = (byte[]) msg.obj;
                        String incomingMessage = new String(read, 0, msg.arg1);  // byte[]; offset; byteCount
                        Log.d(TAG, "MESSAGE_READ: " + incomingMessage);
                        showToast_Short("Msg received: " + incomingMessage);
                    }
                    break;
                }
                case GlobalVariables.MESSAGE_WRITE: {
                    if (msg.obj instanceof BluetoothMessageEntity) {
                    } else {
                        byte[] read = (byte[]) msg.obj;
                        String incomingMessage = new String(read, 0, msg.arg1);  // byte[]; offset; byteCount
                        Log.d(TAG, "MESSAGE_WRITE: " + incomingMessage);
                        showToast_Short("Msg sent: " + incomingMessage);
                    }
                    break;
                }
            }


        }
    };


    // Create a BroadcastReceiver to capture bluetooth activities
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            String action = intent.getAction();
            // ON/OFF STATES
            final int currentState_ThisDevice = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            final int previousState_ThisDevice = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.ERROR);
            // NONE/BONDING/BONDED
            final int currentBondState_RemoteDevice = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            final int previousBondState_RemoteDevice = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
            // DISCONNECTED/CONNECTING/CONNECTED/DISCONNECTING
            final int currentConnectionState_ThisDevice = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothDevice.ERROR);
            final int preivousConnectionState_ThisDevice = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, BluetoothDevice.ERROR);
            BluetoothDevice mRemoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            mBTCurrentState = mBluetoothConnection.getBTConnectionState();
            switch (action) {
                // handle BT connection state
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_CONNECTED: " + mRemoteDevice.getName());
                    // mBluetoothConnection.startConnectThread(mRemoteDevice, true);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_DISCONNECTED: " + mRemoteDevice.getName());
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_DISCONNECT_REQUESTED " + mRemoteDevice.getName());
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    Log.d(TAG, "onReceive: ACTION_PAIRING_REQUEST");
                    break;
                // BT ON/OFF
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    // 10 = OFF, 11 = TURNING ON, 12 = ON, 13 = TURNING OFF
                    if (currentState_ThisDevice == BluetoothAdapter.STATE_ON) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_ON (" + currentState_ThisDevice + ")");
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_TURNING_ON) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_TURNING_ON (" + currentState_ThisDevice + ")");
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_TURNING_OFF) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_TURNING_OFF (" + currentState_ThisDevice + ")");
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_OFF) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_OFF (" + currentState_ThisDevice + ")");
                    }
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.d(TAG, "onReceive: ACTION_CONNECTION_STATE_CHANGED");
                    // 0 = disconnected, 1 = connecting ON, 2 = connected, 3 = disconnecting
                    if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_DISCONNECTED) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_DISCONNECTED (" + currentConnectionState_ThisDevice + "), " + mRemoteDevice.getName());
                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_CONNECTING) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_CONNECTING (" + currentConnectionState_ThisDevice + "), " + mRemoteDevice.getName());
                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_CONNECTED) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_CONNECTED (" + currentConnectionState_ThisDevice + "), " + mRemoteDevice.getName());
                        // mBluetoothConnection.startConnectThread(mRemoteDevice, true);
                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_DISCONNECTING) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_DISCONNECTING (" + currentConnectionState_ThisDevice + "), " + mRemoteDevice.getName());
                    } else {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: action: (" + action.toString() + ")");
                    }

                    updateGUI_ToolBar_BTConnectionState();
                    updateGUI_MenuIcon();
                    break;
            }
        }
    };

    // Getter and Setters
    public boolean getIsWayPointLocked() {
        return isWayPointLocked;
    }

    public void setIsWayPointLocked(boolean wayPointLocked) {
        isWayPointLocked = wayPointLocked;
    }

    public int getControlMode() {
        return controlMode;
    }

    public class BTCommandAdapter extends RecyclerView.Adapter<BTCommandAdapter.MyViewHolder> {

        private List<BluetoothMessageEntity> BTCommandArrayList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;

            public MyViewHolder(View view) {
                super(view);
                textView_message = (TextView) view.findViewById(R.id.textView_message);

            }
        }

        public BTCommandAdapter(List<BluetoothMessageEntity> BTCommandArrayList) {
            this.BTCommandArrayList = BTCommandArrayList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            BluetoothMessageEntity bluetoothMessageEntity = BTCommandArrayList.get(position);
            if (bluetoothMessageEntity.getFrom().equals(MESSAGE_FROM)) {
                // FROM MDPGRP17 = sender
                holder.textView_message.setBackgroundResource(R.color.sender_background);
                holder.textView_message.setText(bluetoothMessageEntity.getMessageContent());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.textView_message.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.textView_message.setLayoutParams(params); //causes layout update
            } else {
                // FROM mRemoteDevice = receiver
                holder.textView_message.setBackgroundResource(R.color.receiver_background);
                holder.textView_message.setText(bluetoothMessageEntity.getMessageContent());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.textView_message.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.textView_message.setLayoutParams(params); //causes layout update
            }
        }

        @Override
        public int getItemCount() {
            return BTCommandArrayList.size();
        }
    }
}
