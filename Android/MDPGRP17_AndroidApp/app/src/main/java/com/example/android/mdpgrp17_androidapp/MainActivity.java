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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTED;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_IDLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_FORWARD;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_REVERSE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_ROTATELEFT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_ROTATERIGHT;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.CMD_SENDARENAINFO;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_FROM;


/**
 * Provides UI for the main screen.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
private MotionEvent simulationEvent;
    // GUI Objects
    private DrawerLayout mDrawerLayout;
    private Menu menu;
    private RecyclerView recyclerView_Command;
    private RelativeLayout arenaGrid;
    private Button BTN_GetArenaInfo;
    private Button BTN_CMD_Forward;
    private Button BTN_CMD_Reverse;
    private Button BTN_CMD_RotateLeft;
    private Button BTN_CMD_RotateRight;
    private Button BTN_TestEncodeString;
    private Button BTN_JsonTest_Write;
    private Button BTN_JsonTest_Read;
    private TextView TV_RobotStatusValue;
    private TextView TV_JsonTest_Write;
    private TextView TV_JsonTest_Read;
    // Bluetooth objects
    private BluetoothConnection mBluetoothConnection;
    private BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothMessageEntity> mBTCommandArrayList;
    public BTCommandAdapter mBTCommandArrayAdapter; // recycler view adapter


    //Arena objects
    private Arena arena;

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
            if (mBluetoothConnection.getConnectedRemoteDevice() != null) {
                mBluetoothConnection.startConnectThread(mBluetoothConnection.getConnectedRemoteDevice(), true);
            } else {
                showToast("You have not connected to any device yet.");
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
        arenaGrid = (RelativeLayout) findViewById(R.id.arenaGrid);
        recyclerView_Command = (RecyclerView) findViewById(R.id.listView_Command);
        BTN_GetArenaInfo = (Button) findViewById(R.id.BTN_GetArenaInfo);
        BTN_CMD_Forward = (Button) findViewById(R.id.BTN_CMD_Forward);
        BTN_CMD_Reverse = (Button) findViewById(R.id.BTN_CMD_Reverse);
        BTN_CMD_RotateLeft = (Button) findViewById(R.id.BTN_CMD_RotateLeft);
        BTN_CMD_RotateRight = (Button) findViewById(R.id.BTN_CMD_RotateRight);
        BTN_TestEncodeString = (Button) findViewById(R.id.BTN_TestEncodeString);
        BTN_JsonTest_Write = (Button) findViewById(R.id.BTN_JsonTest_Write);
        BTN_JsonTest_Read = (Button) findViewById(R.id.BTN_JsonTest_Read);
        TV_RobotStatusValue = (TextView) findViewById(R.id.TV_RobotStatusValue);
        TV_JsonTest_Write = (TextView) findViewById(R.id.TV_JsonTest_Write);
        TV_JsonTest_Read = (TextView) findViewById(R.id.TV_JsonTest_Read);
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

        BTN_GetArenaInfo.setOnClickListener(this);
        BTN_CMD_Forward.setOnClickListener(this);
        BTN_CMD_Reverse.setOnClickListener(this);
        BTN_CMD_RotateLeft.setOnClickListener(this);
        BTN_CMD_RotateRight.setOnClickListener(this);
        BTN_JsonTest_Write.setOnClickListener(this);
        BTN_JsonTest_Read.setOnClickListener(this);
        BTN_TestEncodeString.setOnClickListener(this);
arenaGrid.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event == simulationEvent)
                    return false;
                int action = event.getAction();
                int x = (int)event.getX();
                int y = (int)event.getY();
                Log.e("onTouchListener", "User touch at X:" + x + " Y:" + y);
                long length = 0;
                if (action == MotionEvent.ACTION_DOWN) {
                    click(v, x, y);
                }
                return false;
            }
        });
    }
public void click(final View view, final int x, final int y) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // SIMULATION BEGINS HERE AFTER 2000 Millis ///
                long touchTime = SystemClock.uptimeMillis();
                simulationEvent = MotionEvent.obtain(touchTime, touchTime,
                        MotionEvent.ACTION_DOWN, x, y, 0);
                view.dispatchTouchEvent(simulationEvent);

                simulationEvent = MotionEvent.obtain(touchTime, touchTime,
                        MotionEvent.ACTION_UP, x, y, 0);
                view.dispatchTouchEvent(simulationEvent);
                Log.e("simulatedTouch","simulated touch executed at X:"+x+" Y:"+y);
            }
        }, 2000);
    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();

    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        setup_ArenaGrid();
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "onResume: BT on");
            // BT on
            mBluetoothConnection = BluetoothConnection.getmBluetoothConnection(mHandler);
            mBluetoothConnection.setHandler(mHandler);
            int checkBTCurrentstate = mBluetoothConnection.getBTConnectionState();
            if (checkBTCurrentstate == BT_CONNECTION_STATE_IDLE) {
                mBluetoothConnection.startAcceptThread(true);
            }
            if (checkBTCurrentstate == BT_CONNECTION_STATE_CONNECTED) {
                mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
            }
            updateGUI_ToolBar_BTConnectionState();
            updateGUI_MessageContent();
            updateGUI_MenuIcon();
        } else {
            // BT off
            Log.d(TAG, "onResume: BT off");
            TV_RobotStatusValue.setText("None");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
    }

    public void onClick(View view) {

        if (view.getId() == R.id.BTN_TestEncodeString) {
            Log.d(TAG, "Clicked On BTN_TestEncodeString");
            String encodedmsg = "GRID 20 15 12 7 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 1 1 0 0 0 0 1 1 1 1 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0";
            updateGUI_ArenaGrid(encodedmsg);
        }
        if (view.getId() == R.id.BTN_JsonTest_Write) {
            Log.d(TAG, "Clicked On BTN_JsonTest_Write");

        }
        if (view.getId() == R.id.BTN_JsonTest_Read) {
            Log.d(TAG, "Clicked On BTN_JsonTest_Read");

        }


        if (mBluetoothAdapter.isEnabled()) {
            if (mBluetoothConnection.getBTConnectionState() == BT_CONNECTION_STATE_CONNECTED) {
                switch (view.getId()) {
                    case R.id.BTN_GetArenaInfo: {
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
                        break;
                    }
                    case R.id.BTN_CMD_Forward: {
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_FORWARD));
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
                        TV_RobotStatusValue.setText("Moving forward");
                        break;
                    }
                    case R.id.BTN_CMD_Reverse: {
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_REVERSE));
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
                        TV_RobotStatusValue.setText("Moving reverse");
                        break;
                    }
                    case R.id.BTN_CMD_RotateLeft: {
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_ROTATELEFT));
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
                        TV_RobotStatusValue.setText("Rotating left");
                        break;
                    }
                    case R.id.BTN_CMD_RotateRight: {
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_ROTATERIGHT));
                        mBluetoothConnection.write(BluetoothMessageEntity.sendCommand(CMD_SENDARENAINFO));
                        TV_RobotStatusValue.setText("Rotating right");
                        break;
                    }
                }
            }
        } else {
            showToast("Please turn on bluetooth and connect to a device");

        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void updateGUI_MenuIcon() {
        Log.d(TAG, "updateMenuIcon");
        // update GUI menu icon
        if (menu != null) {
            if (mBluetoothAdapter != null && mBluetoothConnection != null) {
                if (mBluetoothAdapter.isEnabled()) {
                    menu.findItem(R.id.action_settings_bluetooth).setIcon(R.drawable.ic_bluetooth_black_24dp); // default
                    int currentBTConnectionState = mBluetoothConnection.getBTConnectionState();
                    if (currentBTConnectionState == GlobalVariables.BT_CONNECTION_STATE_DISCONNECTED) {
                        // default
                    } else if (currentBTConnectionState == GlobalVariables.BT_CONNECTION_STATE_CONNECTING) {
                        menu.findItem(R.id.action_settings_bluetooth).setIcon(R.drawable.ic_bluetooth_searching_black_24dp);
                    } else if (currentBTConnectionState == BT_CONNECTION_STATE_CONNECTED) {
                        menu.findItem(R.id.action_settings_bluetooth).setIcon(R.drawable.ic_bluetooth_connected_black_24dp);
                    } else if (currentBTConnectionState == GlobalVariables.BT_CONNECTION_STATE_DISCONNECTING) {
                        // default
                    } else if (currentBTConnectionState == BT_CONNECTION_STATE_IDLE) {
                        // default
                    } else if (currentBTConnectionState == GlobalVariables.BT_CONNECTION_STATE_LISTENING) {
                        // default
                    }
                } else {
                    // BT OFF
                    menu.findItem(R.id.action_settings_bluetooth).setIcon(R.drawable.ic_bluetooth_disabled_black_24dp);
                }
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
        recyclerView_Command.setLayoutManager(mLayoutManager);
        recyclerView_Command.setItemAnimator(new DefaultItemAnimator());
        recyclerView_Command.setAdapter(mBTCommandArrayAdapter);
        recyclerView_Command.scrollToPosition(mBTCommandArrayList.size() - 1);

    }

    public void updateGUI_ArenaGrid(String messageContent) {
        Log.d(TAG, "updateGUI_ArenaGrid");
        arena.decodeArenaInfo(messageContent);
    }

    private void updateGUI_ToolBar_BTConnectionState() {
        int checkCurrentConnectionState = mBluetoothConnection.getBTConnectionState();
        getSupportActionBar().setSubtitle("No device connected");// default status
        switch (checkCurrentConnectionState) {
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
                    TV_RobotStatusValue.setText("Idling");
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
            case GlobalVariables.BT_CONNECTION_STATE_LISTENING:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_LISTENING");
                break;
            case GlobalVariables.BT_CONNECTION_STATE_OFF:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_OFF");
                break;
        }
    }

    private void updateGUI_ArenaRobotStatus() {
        TV_RobotStatusValue.setText("Idling");
    }

    public void setup_ArenaGrid() {
        Log.d(TAG, "setup_ArenaGrid");
        if (arena != null) {
            arenaGrid.removeView(arena);
            arena = null;
        }
        arena = new Arena(this, arenaGrid);
        arenaGrid.addView(arena);
        updateGUI_ArenaGrid("NAKEDGRID");
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

            switch (msg.what) {
                case GlobalVariables.BT_CONNECTION_STATE_CHANGE: {
                    updateGUI_ToolBar_BTConnectionState();
                    updateGUI_MenuIcon();
                    break;
                }
                case GlobalVariables.MESSAGE_READ: {
                    if (msg.obj instanceof BluetoothMessageEntity) {
                        updateGUI_MessageContent();
                        updateGUI_ArenaGrid(((BluetoothMessageEntity) msg.obj).getMessageContent());
                        updateGUI_ArenaRobotStatus();
                    } else {
                        byte[] read = (byte[]) msg.obj;
                        String incomingMessage = new String(read, 0, msg.arg1);  // byte[]; offset; byteCount
                        Log.d(TAG, "MESSAGE_READ: " + incomingMessage);
                        showToast("Msg received: " + incomingMessage);
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
                        showToast("Msg sent: " + incomingMessage);
                    }
                    break;
                }
            }
        }
    };


    // Create a BroadcastReceiver to capture bluetooth activities
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
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
            switch (action) {
                // handle BT connection state
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_CONNECTED: " + mRemoteDevice.getName());
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_DISCONNECTED: " + mRemoteDevice.getName());
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_DISCONNECT_REQUESTED " + mRemoteDevice.getName());
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.d(TAG, "onReceive: ACTION_CONNECTION_STATE_CHANGED");
                    // 0 = disconnected, 1 = connecting ON, 2 = connected, 3 = disconnecting
                    if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_DISCONNECTED) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_DISCONNECTED (" + currentConnectionState_ThisDevice + ")" + mRemoteDevice.getName());
                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_CONNECTING) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_CONNECTING (" + currentConnectionState_ThisDevice + ")" + mRemoteDevice.getName());
                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_CONNECTED) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_CONNECTED (" + currentConnectionState_ThisDevice + "), " + mRemoteDevice.getName());

                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_DISCONNECTING) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_DISCONNECTING (" + currentConnectionState_ThisDevice + ")" + mRemoteDevice.getName());
                    } else {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: action: (" + action.toString() + ")");
                    }
                    updateGUI_ToolBar_BTConnectionState();
                    updateGUI_MenuIcon();
                    break;
            }
        }
    };


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
                if (bluetoothMessageEntity.getMessageContent().contains("GRID")) {
                    String displayMsg = "";
                    holder.textView_message.setText(displayMsg);
                } else {
                    holder.textView_message.setText(bluetoothMessageEntity.getMessageContent());
                }
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
