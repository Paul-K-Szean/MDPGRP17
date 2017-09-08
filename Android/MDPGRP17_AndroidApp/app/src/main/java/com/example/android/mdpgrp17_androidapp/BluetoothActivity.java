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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";
    // codes
    private static final int ACTION_REQUEST_BLUETOOTH_DISCOVERABLE = 0;
    // Bluetooth objects
    private BluetoothConnection mBluetoothConnection;
    private ArrayList<BluetoothDevice> mBTDeviceList;
    private BluetoothAdapter mBluetoothAdapter;
    // UI objects
    private TextView devices_nearby;
    private RecyclerView recyclerview_btdevicelist;
    private ToggleButton toggle_bluetooth_onoff;
    private Menu menu;
    private RelativeLayout layout_BTAct_Bottom;

    // not invoked
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        // if replied yes = BT on and startAcceptThread discovery
        // if replied no = BT off and do nothing
        // Check which request we're responding to
        Log.d(TAG, "requestCode: " + requestCode + " == ACTION_REQUEST_BLUETOOTH_DISCOVERABLE: " + ACTION_REQUEST_BLUETOOTH_DISCOVERABLE +
                " ? " + String.valueOf(requestCode == ACTION_REQUEST_BLUETOOTH_DISCOVERABLE));

        if (requestCode == ACTION_REQUEST_BLUETOOTH_DISCOVERABLE) {
            // Make sure the request was successful
            switch (resultCode) {
                case 0: // don't activate discoverable to other device
                    Log.d(TAG, "resultCode: " + resultCode + " = no (0)");
                    // but discover other device
                    mBluetoothAdapter.startDiscovery();
                    break;
                case 300: // activate discoverable to other device
                    Log.d(TAG, "resultCode: " + resultCode + " = yes (300)");
                    // startAcceptThread discovery
                    mBluetoothAdapter.startDiscovery();
                    break;
                default:
                    Log.d(TAG, "resultCode: " + resultCode + ". In default case");
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_bluetoothdevices, menu);
        // Update BT refresh UI
        if (mBluetoothAdapter.isEnabled()) {
            menu.findItem(R.id.action_settings_bluetooth_refresh).setVisible(true);
        } else if (!mBluetoothAdapter.isEnabled()) {
            menu.findItem(R.id.action_settings_bluetooth_refresh).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        int id = item.getItemId();
        if (id == R.id.action_settings_bluetooth_refresh) {
            if (mBluetoothAdapter.isEnabled() && !mBluetoothAdapter.isDiscovering())
                // enable discoverable by other device
                if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent intent_discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    intent_discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(intent_discoverable);
                }
            mBluetoothAdapter.startDiscovery();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        // Display back to home navigation
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // find bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // find UI objects
        toggle_bluetooth_onoff = (ToggleButton) findViewById(R.id.toggle_bluetooth_onoff);
        devices_nearby = (TextView) findViewById(R.id.devices_nearby);
        recyclerview_btdevicelist = (RecyclerView) findViewById(R.id.recyclerview_btdevicelist);
        layout_BTAct_Bottom = (RelativeLayout) findViewById(R.id.layout_BTAct_Bottom);
        // toggle button action
        toggle_bluetooth_onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "toggle button clicked: state: " + isChecked);
                if (isChecked) {
                    mBluetoothAdapter.enable();     // switch on BT
                    Log.d(TAG, "Turning BT on");
                } else {
                    mBluetoothAdapter.disable();    // switch off BT
                    Log.d(TAG, "Turning BT off");
                    mBluetoothConnection.stopAllThreads();
                }
            }
        });

        checkBTStatus();
        mBTDeviceList = new ArrayList<BluetoothDevice>();
        recyclerview_btdevicelist.setLayoutManager(new LinearLayoutManager(this));
        recyclerview_btdevicelist.setAdapter(new BluetoothDevicesAdapter(getBaseContext(), mBluetoothConnection, mBTDeviceList));
        recyclerview_btdevicelist.getAdapter().notifyDataSetChanged();

        IntentFilter intent_filter = new IntentFilter();
        // Register BT broadcasts when the bluetooth is turned ON/OFF
        intent_filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); // on/off state of bluetooth
        // Register BT broadcasts when a remote device is found
        intent_filter.addAction(BluetoothDevice.ACTION_FOUND);  // discovery found a device
        // Register BT broadcasts when the discovery started
        intent_filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); // startAcceptThread discovery
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
        unregisterReceiver(mReceiver);
        super.onDestroy();

    }

    @Override
    public boolean onSupportNavigateUp() {
        mBluetoothAdapter.cancelDiscovery();
        onBackPressed();
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void checkBTStatus() {
        if (mBluetoothAdapter.isEnabled()) {
            // BT on = startAcceptThread. Invoked in mReceiver
            showEnabled(); // update UI
            Log.d(TAG, "checkBTStatus: ShowEnabled");
        } else if (!mBluetoothAdapter.isEnabled()) {
            // BT off = stopAllThread. Invoked in mReceiver
            showDisabled(); // update UI
            Log.d(TAG, "checkBTStatus: showDisabled");
        }
    }

    // Turn on BT
    // Check if current device is discoverable by other device
    // Start discoverability no matter the outcome of the user
    // Update UI - show menu refresh icon
    // Update UI - show BT devices layout
    // Update UI - set BT toggle button to ON
    private void showEnabled() {
        Log.d(TAG, "showEnabled");
        layout_BTAct_Bottom.setVisibility(View.VISIBLE);
        toggle_bluetooth_onoff.setChecked(true);

        // check if bluetooth is discoverable to other device
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent_discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent_discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(intent_discoverable);
        }
        Log.d(TAG, "Discovery status: " + mBluetoothAdapter.isDiscovering());
        if (!mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.startDiscovery();

        // update UI - show menu refresh icon
        if (menu != null)
            menu.findItem(R.id.action_settings_bluetooth_refresh).setVisible(true);
    }

    // Turn off BT
    // Update UI - hide menu refresh icon
    // Update UI - hide BT devices layout
    // Update UI - set BT toggle button to OFF
    private void showDisabled() {
        Log.d(TAG, "showDisabled");
        layout_BTAct_Bottom.setVisibility(View.INVISIBLE);
        toggle_bluetooth_onoff.setChecked(false);

        // update UI - hide menu refresh icon
        if (menu != null)
            menu.findItem(R.id.action_settings_bluetooth_refresh).setVisible(false);
    }

    public void updateRecycleView_BTDeviceList(Context context) {
        Log.d(TAG, "updateRecycleView_BTDeviceList");
        if (mBTDeviceList != null) {
            try {
                recyclerview_btdevicelist.setLayoutManager(new LinearLayoutManager(context));
                recyclerview_btdevicelist.setAdapter(new BluetoothDevicesAdapter(context, mBluetoothConnection, mBTDeviceList));
                recyclerview_btdevicelist.getAdapter().notifyDataSetChanged();
            } catch (Exception ex) {
                Log.e(TAG, "updateRecycleView_BTDeviceList failed: " + ex.getMessage());
            }
        }
    }

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

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "mHandler: msg.what: " + msg.what);
            Log.d(TAG, "mHandler: msg.arg1: " + msg.arg1);
            Log.d(TAG, "mHandler: msg.arg2: " + msg.arg2);
            Log.d(TAG, "mHandler: msg.toString(): " + msg.toString());
            updateRecycleView_BTDeviceList(getBaseContext());

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
            updateRecycleView_BTDeviceList(getBaseContext());
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
            int checkBTConnectionState = mBluetoothConnection.getBTConnectionState();
            switch (action) {
                // found a remote device
                case BluetoothDevice.ACTION_FOUND:
                    Log.d(TAG, "onReceive: ACTION_FOUND: " + mRemoteDevice.getName() + ", " + mRemoteDevice.getBondState());
                    if (mBTDeviceList == null)
                        mBTDeviceList = new ArrayList<BluetoothDevice>();
                    mBTDeviceList.add(mRemoteDevice);
                    updateRecycleView_BTDeviceList(getBaseContext());
                    showToast("Found " + mRemoteDevice.getName());
                    break;
                // handle bonding state of this and remote device
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    Log.d(TAG, "onReceive: ACTION_BOND_STATE_CHANGED");
                    // 10 = bond_none, 11 = bond_bonding, 12 = bond_bonded
                    if (currentBondState_RemoteDevice == BluetoothDevice.BOND_NONE) {
                        Log.d(TAG, "Un-pair with " + mRemoteDevice.getName() + ", BOND_NONE (" + currentBondState_RemoteDevice + ")");
                    } else if (currentBondState_RemoteDevice == BluetoothDevice.BOND_BONDING) {
                        Log.d(TAG, "Pairing with " + mRemoteDevice.getName() + ", BOND_BONDING (" + currentBondState_RemoteDevice + ")");
                    } else if (currentBondState_RemoteDevice == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "Paired with " + mRemoteDevice.getName() + ", BOND_BONDED (" + currentBondState_RemoteDevice + ")");
                        mBluetoothConnection.startConnectThread(mRemoteDevice, true);
                    }
                    break;
                case BluetoothDevice.ACTION_PAIRING_REQUEST:
                    Log.d(TAG, "onReceive: ACTION_PAIRING_REQUEST");
                    break;
                // BT ON/OFF
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    // 10 = OFF, 11 = TURNING ON, 12 = ON, 13 = TURNING OFF
                    if (currentState_ThisDevice == BluetoothAdapter.STATE_ON) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_ON (" + currentState_ThisDevice + ")");
                        showEnabled();
                        mBluetoothAdapter.startDiscovery();
                        if (mBluetoothConnection == null)
                            mBluetoothConnection = new BluetoothConnection(mHandler);
                        mBluetoothConnection.startAcceptThread(true);
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_TURNING_ON) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_TURNING_ON (" + currentState_ThisDevice + ")");

                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_TURNING_OFF) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_TURNING_OFF (" + currentState_ThisDevice + ")");
                        mBluetoothAdapter.cancelDiscovery();
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_OFF) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_OFF (" + currentState_ThisDevice + ")");
                        showDisabled();
                    }
                    break;
                // handle BT discovery state
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_STARTED");
                    mBTDeviceList = new ArrayList<BluetoothDevice>();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED");
                    break;
                case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                    Log.d(TAG, "onReceive: ACTION_CONNECTION_STATE_CHANGED");
                    // 0 = disconnected, 1 = connecting ON, 2 = connected, 3 = disconnecting
                    if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_DISCONNECTED) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_DISCONNECTED (" + currentConnectionState_ThisDevice + ")");
                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_CONNECTING) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_CONNECTING (" + currentConnectionState_ThisDevice + ")");
                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_CONNECTED) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_CONNECTED (" + currentConnectionState_ThisDevice + ")");
                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_DISCONNECTING) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_DISCONNECTING (" + currentConnectionState_ThisDevice + ")");
                    } else {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: action: (" + action.toString() + ")");
                    }
                    break;
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
            }
            // update UI
            updateRecycleView_BTDeviceList(getBaseContext());
        }
    };

}
