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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

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


/**
 * Provides UI for the main screen.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "MainActivity";

    // GUI Objects
    private DrawerLayout mDrawerLayout;
    private Menu menu;
    private RecyclerView RCYLRVW_Command;
    private RelativeLayout RLO_ArenaGrid;
    private LinearLayout LLO_MapMode_PreviousNext;
    private LinearLayout LLO_WayPoint;
    private Button BTN_CMD_GetArenaInfo;
    private Button BTN_CMD_Forward;
    private Button BTN_CMD_Reverse;
    private Button BTN_CMD_RotateLeft;
    private Button BTN_CMD_RotateRight;
    private Button BTN_MapMode_Previous;
    private Button BTN_MapMode_Next;
    private Button BTN_Reset;
    private TextView TXTVW_RobotStatusValue;
    private TextView TXTVW_MapModeIndexValue;
    private TextView TXTVW_WayPointValue;
    private TextView TXTVW_WayPoint;
    private ToggleButton TGLBTN_MapMode;
    // Bluetooth objects
    private BluetoothConnection mBluetoothConnection;
    private BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothMessageEntity> mBTCommandArrayList;
    public BTCommandAdapter mBTCommandArrayAdapter; // recycler view adapter
    private int mBTCurrentState;
    // Arena objects
    private Arena arena;
    private boolean isWayPointLocked;

    // Config objects
    private ConfigFileHandler configFileHandler;
    private ConfigFile configFile;

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
            Intent intent_bluetooth = new Intent(this, BluetoothActivity.class);
            startActivity(intent_bluetooth);
        } else if (id == R.id.action_settings) {
            Intent intent_settings = new Intent(this, SettingsActivity.class);
            startActivity(intent_settings);
        } else if (id == R.id.action_settings_bluetooth_reconnect) {

            if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                mBluetoothConnection.disconnect();
            }
            if (mBTCurrentState == BT_CONNECTION_STATE_LISTENING) {
                BluetoothDevice lastConnectedDevice = mBluetoothConnection.getConnectedRemoteDevice();
                if (lastConnectedDevice != null) {
                    mBluetoothConnection.startConnectThread(lastConnectedDevice, true);
                } else {
                    lastConnectedDevice = mBluetoothAdapter.getRemoteDevice(configFileHandler.getConfigFile().getBluetoothConfig().getLastConnectedDevice_MACAddress());
                    if (lastConnectedDevice != null) {
                        mBluetoothConnection.startConnectThread(lastConnectedDevice, true);
                    } else {
                        showToast_Short("You have not connected to any device yet.");
                    }
                }
            }

        } else if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        Log.d(TAG, "onOptionsItemSelected");
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
        LLO_MapMode_PreviousNext = (LinearLayout) findViewById(R.id.LLO_MapMode_PreviousNext);
        LLO_WayPoint = (LinearLayout) findViewById(R.id.LLO_WayPoint);
        RCYLRVW_Command = (RecyclerView) findViewById(R.id.RCYLRVW_Command);
        BTN_CMD_GetArenaInfo = (Button) findViewById(R.id.BTN_CMD_GetArenaInfo);
        BTN_CMD_Forward = (Button) findViewById(R.id.BTN_CMD_Forward);
        BTN_CMD_Reverse = (Button) findViewById(R.id.BTN_CMD_Reverse);
        BTN_CMD_RotateLeft = (Button) findViewById(R.id.BTN_CMD_RotateLeft);
        BTN_CMD_RotateRight = (Button) findViewById(R.id.BTN_CMD_RotateRight);
        BTN_MapMode_Previous = (Button) findViewById(R.id.BTN_MapMode_Previous);
        BTN_MapMode_Next = (Button) findViewById(R.id.BTN_MapMode_Next);
        BTN_Reset = (Button) findViewById(R.id.BTN_Reset);
        TXTVW_RobotStatusValue = (TextView) findViewById(R.id.TXTVW_RobotStatusValue);
        TXTVW_MapModeIndexValue = (TextView) findViewById(R.id.TXTVW_MapModeIndexValue);
        TXTVW_WayPointValue = (TextView) findViewById(R.id.TXTVW_WayPointValue);
        TXTVW_WayPoint = (TextView) findViewById(R.id.TXTVW_WayPoint);
        TGLBTN_MapMode = (ToggleButton) findViewById(R.id.TGLBTN_MapMode);
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create Navigation drawer and inflate layout
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            VectorDrawableCompat indicator
                    = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu, getTheme());
            indicator.setTint(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Set item in checked state
                        menuItem.setChecked(true);
                        // Closing drawer on item click
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

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

        BTN_CMD_GetArenaInfo.setOnClickListener(this);
        BTN_CMD_Forward.setOnClickListener(this);
        BTN_CMD_Reverse.setOnClickListener(this);
        BTN_CMD_RotateLeft.setOnClickListener(this);
        BTN_CMD_RotateRight.setOnClickListener(this);
        BTN_MapMode_Previous.setOnClickListener(this);
        BTN_MapMode_Next.setOnClickListener(this);
        BTN_Reset.setOnClickListener(this);
        TGLBTN_MapMode.setOnClickListener(this);
        LLO_WayPoint.setOnTouchListener(this);

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
            if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
                // updates the Arena GUI at handler
            }
            updateGUI_ToolBar_BTConnectionState();
            updateGUI_MessageContent();
            updateGUI_MenuIcon();
        } else {
            // BT off
            Log.d(TAG, "onResume: BT off");
            TXTVW_RobotStatusValue.setText("None");
            setup_ArenaGrid();
        }


        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
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
        super.onDestroy();
    }

    public void onClick(View view) {
        int controlID = view.getId();

        if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
            if (controlID == R.id.TGLBTN_MapMode) {
                Log.d(TAG, "Clicked On TGLBTN_MapMode");
                // set the map mode
                arena.setMapMode(TGLBTN_MapMode.isChecked());
                if (TGLBTN_MapMode.isChecked()) {
                    // auto map mode
                    LLO_MapMode_PreviousNext.setVisibility(View.INVISIBLE);
                    TXTVW_MapModeIndexValue.setText("Auto");
                    arena.setSaveStateIndex_Pause(-1); // reset the pause index
                    arena.setSaveStateIndex_InArray(-1);// reset the transverse index
                } else {
                    // manual map mode
                    LLO_MapMode_PreviousNext.setVisibility(View.VISIBLE);
                    // set the pause index to the current arena info size
                    arena.setSaveStateIndex_Pause((arena.getArenaSaveStateArrayListSize() - 1));
                    // check if index of transverse was set to the initial value of the pause index
                    if (arena.getSaveStateIndex_InArray() == -1)
                        arena.setSaveStateIndex_InArray(arena.getSaveStateIndex_Pause());
                    arena.printArenaInfo();
                }
            }
            if (controlID == R.id.BTN_MapMode_Previous) {
                if (arena.getSaveStateIndex_InArray() > 0) {
                    Log.d(TAG, "Clicked On BTN_MapMode_Previous: -1");
                    arena.setSaveStateIndex_InArray(arena.getSaveStateIndex_InArray() - 1);
                    if (arena.getSaveStateArrayList().get(arena.getSaveStateIndex_InArray()).getWayPointReached()) {
                        TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_StartPosition));
                        TXTVW_WayPoint.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_StartPosition));
                    } else {
                        TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_WayPoint));
                        TXTVW_WayPoint.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_WayPoint));
                    }
                } else {
                    Log.d(TAG, "Clicked On BTN_MapMode_Previous: at the start");
                    showToast_Short("You are at the start state!");
                }
            }
            if (controlID == R.id.BTN_MapMode_Next) {
                if (arena.getSaveStateIndex_InArray() < (arena.getArenaSaveStateArrayListSize() - 1)) {
                    Log.d(TAG, "Clicked On BTN_MapMode_Next: +1");
                    arena.setSaveStateIndex_InArray(arena.getSaveStateIndex_InArray() + 1);
                    if (arena.getSaveStateArrayList().get(arena.getSaveStateIndex_InArray()).getWayPointReached()) {
                        TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_StartPosition));
                        TXTVW_WayPoint.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_StartPosition));
                    } else {
                        TXTVW_WayPointValue.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_WayPoint));
                        TXTVW_WayPoint.setTextColor(ContextCompat.getColor(this, R.color.color_Arena_WayPoint));
                    }
                } else {
                    Log.d(TAG, "Clicked On BTN_MapMode_Next: ");
                    showToast_Short("You can't view the future yet!");
                }
            }
            if (controlID == R.id.BTN_CMD_GetArenaInfo) {
                Log.d(TAG, "Clicked On BTN_CMD_GetArenaInfo: at the end");
                mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
            }

            if (isWayPointLocked) {
                switch (view.getId()) {
                    case R.id.BTN_CMD_Forward: {
                        if (arena.checkMovement(CMD_FORWARD)) {
                            mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_FORWARD));
                            TXTVW_RobotStatusValue.setText("Moving forward");
                        } else {
                            showToast_Long("Unable to move forward");
                        }
                        break;
                    }
                    case R.id.BTN_CMD_Reverse: {
                        if (arena.checkMovement(CMD_REVERSE)) {
                            mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_REVERSE));
                            TXTVW_RobotStatusValue.setText("Moving reverse");
                        } else {
                            showToast_Long("Unable to move backward");
                        }
                        break;
                    }
                    case R.id.BTN_CMD_RotateLeft: {
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_ROTATELEFT));
                        TXTVW_RobotStatusValue.setText("Rotating left");
                        break;
                    }
                    case R.id.BTN_CMD_RotateRight: {
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_ROTATERIGHT));
                        TXTVW_RobotStatusValue.setText("Rotating right");
                        break;
                    }
                }
            } else {
                showToast_Short("Please lock the way point first");
            }

            if (arena.getMapMode()) {

            } else {
                // manual mode
                TXTVW_MapModeIndexValue.setText("At: " + arena.getSaveStateIndex_InArray() + "/" + (arena.getArenaSaveStateArrayListSize() - 1));
            }
        } else {
            Log.d(TAG, "Please turn on bluetooth and connect to a device");
            showToast_Short("Please turn on bluetooth and connect to a device");
        }

        if (controlID == R.id.BTN_Reset) {
            Log.d(TAG, "Clicked On BTN_Reset");
            setup_ArenaGrid();
            arena.setMapMode(true);
            TGLBTN_MapMode.setChecked(true);
            LLO_MapMode_PreviousNext.setVisibility(View.INVISIBLE);
            arena.setSaveStateIndex_Pause(-1);
            arena.setSaveStateIndex_InArray(-1);
            arena.setupNakedGrid();
            arena.setSaveStateArrayList(new ArrayList<ArenaSaveState>());
            isWayPointLocked = false;
            updateGUI_WayPoint();
            if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
            }
            TXTVW_MapModeIndexValue.setText("Auto");
            TXTVW_WayPointValue.setText("0,0");
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // TODO Auto-generated method stub
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouch: isWayPointLocked: " + isWayPointLocked + "; " + arena.getWayPointPosition_Row_Center() + "," + arena.getWayPointPosition_Col_Center());
                if (isWayPointLocked) {
                    // go to unlock mode
                    isWayPointLocked = false;
                } else {
                    // go to locked mode
                    if (arena.getWayPointPosition_Row_Center() != 0 && arena.getWayPointPosition_Col_Center() != 0) {
                        isWayPointLocked = true;
                    } else {
                        isWayPointLocked = false;
                        showToast_Short("Please select a way point first");
                    }
                }
                updateGUI_WayPoint();
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.d(TAG, String.format("ACTION_MOVE | x:%s y:%s",
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
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

    public void updateGUI_MessageContent() {
        Log.d(TAG, "updateGUI_MessageContent");
        mBTCommandArrayList = mBluetoothConnection.getmBTCommandArrayList();
        // message object
        mBTCommandArrayAdapter = new BTCommandAdapter(mBTCommandArrayList);
        mBTCommandArrayAdapter.notifyDataSetChanged();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        RCYLRVW_Command.setLayoutManager(mLayoutManager);
        RCYLRVW_Command.setItemAnimator(new DefaultItemAnimator());
        RCYLRVW_Command.setAdapter(mBTCommandArrayAdapter);
        RCYLRVW_Command.scrollToPosition(mBTCommandArrayList.size() - 1);

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
                    showToast_Long("Connection with remote device failed");
                    break;
                }
                case GlobalVariables.BT_CONNECTION_STATE_CONNECTIONLOST: {
                    showToast_Long("Connection with remote device lost");
                    break;
                }
                case GlobalVariables.MESSAGE_READ: {
                    if (msg.obj instanceof BluetoothMessageEntity) {
                        BluetoothMessageEntity bluetoothMessageEntity = (BluetoothMessageEntity) msg.obj;
                        if (bluetoothMessageEntity.getMessageType() == MESSAGE_COMMAND) {
                            updateGUI_MessageContent();
                            updateGUI_ArenaGrid(bluetoothMessageEntity.getMessageContent());
                            updateGUI_ArenaRobotStatus();
                        } else {

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
                        updateGUI_MessageContent();
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
                    mBluetoothConnection.startConnectThread(mRemoteDevice, true);
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
                        mBluetoothConnection.startConnectThread(mRemoteDevice, true);
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
