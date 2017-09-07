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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


/**
 * Provides UI for the main screen.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private BluetoothConnection mBluetoothConnection;
    private BluetoothAdapter mBluetoothAdapter;
    private DrawerLayout mDrawerLayout;
    private Menu menu;

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
        // Adding Floating Action Button to bottom right of main view
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Hello Snackbar!",
                        Snackbar.LENGTH_LONG).show();
            }
        });

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
        if (mBluetoothAdapter.isEnabled()) {
            if (mBluetoothConnection != null) {
                Log.d(TAG, "onResume: BT ON");
                checkBTConectionState();
            }
        } else Log.d(TAG, "onResume: BT OFF");
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
                    } else if (currentBTConnectionState == GlobalVariables.BT_CONNECTION_STATE_CONNECTED) {
                        menu.findItem(R.id.action_settings_bluetooth).setIcon(R.drawable.ic_bluetooth_connected_black_24dp);
                    } else if (currentBTConnectionState == GlobalVariables.BT_CONNECTION_STATE_DISCONNECTING) {
                        // default
                    } else if (currentBTConnectionState == GlobalVariables.BT_CONNECTION_STATE_IDLE) {
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
            Log.e(TAG, "menu was null");
        }
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
                    checkBTConectionState();
                    break;
                }
                case GlobalVariables.MESSAGE_READ: {
                    if (msg.obj instanceof BluetoothMessageEntity) {
                        showToast("Msg received: " + ((BluetoothMessageEntity) msg.obj).getMessageContent());
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
                        showToast("Msg sent: " + ((BluetoothMessageEntity) msg.obj).getMessageContent());
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
                    if (mBluetoothConnection.getBTConnectionState() != GlobalVariables.BT_CONNECTION_STATE_CONNECTED) {
                        mBluetoothConnection.setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_CONNECTED);
                        mBluetoothConnection.startConnectThread(mRemoteDevice, true);
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_DISCONNECTED: " + mRemoteDevice.getName());
                    if (mBluetoothConnection.getBTConnectionState() != GlobalVariables.BT_CONNECTION_STATE_DISCONNECTED) {
                        mBluetoothConnection.setBTConnectionState(GlobalVariables.BT_CONNECTION_STATE_DISCONNECTED);
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_DISCONNECT_REQUESTED " + mRemoteDevice.getName());
                    break;
            }
        }
    };


    private void checkBTConectionState() {
        int checkCurrentConnectionState = mBluetoothConnection.getBTConnectionState();
        switch (checkCurrentConnectionState) {
            case GlobalVariables.BT_CONNECTION_STATE_DISCONNECTED:
                Log.d(TAG, "checkBTConectionState: BT connection state: BT_CONNECTION_STATE_DISCONNECTED");
                break;
            case GlobalVariables.BT_CONNECTION_STATE_CONNECTING:
                Log.d(TAG, "checkBTConectionState: BT connection state: BT_CONNECTION_STATE_CONNECTING");
                break;
            case GlobalVariables.BT_CONNECTION_STATE_CONNECTED:
                Log.d(TAG, "checkBTConectionState: BT connection state: BT_CONNECTION_STATE_CONNECTED");
                if (mBluetoothConnection.getConnectedRemoteDevice() != null) {
                    getSupportActionBar().setSubtitle("Connected to " + mBluetoothConnection.getConnectedRemoteDevice().getName());
                } else {
                    Log.e(TAG, "checkBTConectionState: No device connected. Error");
                    getSupportActionBar().setSubtitle("No device connected");
                }
                break;
            case GlobalVariables.BT_CONNECTION_STATE_DISCONNECTING:
                Log.d(TAG, "checkBTConectionState: BT connection state: BT_CONNECTION_STATE_DISCONNECTING");
                break;
            case GlobalVariables.BT_CONNECTION_STATE_IDLE:
                Log.d(TAG, "checkBTConectionState: BT connection state: BT_CONNECTION_STATE_IDLE");
                mBluetoothConnection.startAcceptThread(true);
                getSupportActionBar().setSubtitle("No device connected");
                break;
            case GlobalVariables.BT_CONNECTION_STATE_LISTENING:
                Log.d(TAG, "checkBTConectionState: BT connection state: BT_CONNECTION_STATE_LISTENING");
                getSupportActionBar().setSubtitle("No device connected");
                break;
            case GlobalVariables.BT_CONNECTION_STATE_OFF:
                Log.d(TAG, "checkBTConectionState: BT connection state: BT_CONNECTION_STATE_OFF");
                getSupportActionBar().setSubtitle("No device connected");
                break;
        }
    }
}
