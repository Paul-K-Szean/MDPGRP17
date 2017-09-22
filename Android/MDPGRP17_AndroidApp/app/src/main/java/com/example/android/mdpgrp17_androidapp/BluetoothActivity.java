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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTED;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTING;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_IDLE;

public class BluetoothActivity extends AppCompatActivity {
    private static final String TAG = "BluetoothActivity";
    // codes
    private static final int ACTION_REQUEST_BLUETOOTH_DISCOVERABLE = 0;
    // Bluetooth objects
    private BluetoothConnection mBluetoothConnection;
    private ArrayList<BluetoothDevice> mBTDeviceList;
    private ArrayAdapter mBTDeviceAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private int mBTCurrentState;
    // UI objects
    private ToggleButton toggle_bluetooth_onoff;
    private Menu menu;
    private RelativeLayout layout_BTAct_Bottom;
    private ListView listView_BTDeviceList;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode:" + requestCode);
        Log.d(TAG, "onActivityResult: resultCode:" + resultCode);
        Log.d(TAG, "onActivityResult: data:" + data);

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

        // GUI objects
        toggle_bluetooth_onoff = (ToggleButton) findViewById(R.id.toggle_bluetooth_onoff);
        listView_BTDeviceList = (ListView) findViewById(R.id.listView_BTDeviceList);
        layout_BTAct_Bottom = (RelativeLayout) findViewById(R.id.layout_BTAct_Bottom);
        toggle_bluetooth_onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "toggle button: state: " + isChecked);
                if (isChecked) {
                    mBluetoothAdapter.enable();     // switch on BT
                    Log.d(TAG, "toggle button: BT on");
                } else {
                    mBluetoothAdapter.disable();     // switch on BT
                    Log.d(TAG, "toggle button: BT off");
                }
            }
        });

        // Bluetooth objects
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothConnection = BluetoothConnection.getmBluetoothConnection(mHandler);

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
        // Register BT broadcasts when the there is a connection change
        intent_filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        //  Register all the activities
        registerReceiver(mReceiver, intent_filter);

        // checkBTStatus();
        if (mBTDeviceList != null)
            updateGUI_ListView_BTDeviceList();
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
            showEnabled();
        } else {
            Log.d(TAG, "onResume: BT off");
            // BT off
            showDisabled();
        }
        updateGUI_ToolBar_BTConnectionState();
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

        if (!mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.startDiscovery();
        // update UI - show menu refresh icon
        if (menu != null)
            menu.findItem(R.id.action_settings_bluetooth_refresh).setVisible(true);
        else {
            Log.d(TAG, "showEnabled: Unable to update menu icons");
        }
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
        else {
            Log.d(TAG, "showDisabled: Unable to update menu icons");
        }
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

    private void updateGUI_ListView_BTDeviceList() {
        Log.d(TAG, "updateGUI_ListView_BTDeviceList");

        if (mBTDeviceList != null) {
            // message object
            mBTDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this, R.layout.item_devices,
                    R.id.device_name, mBTDeviceList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    final BluetoothDevice mRemoteDevice = mBTDeviceList.get(position);
                    TextView device_name = ((TextView) view.findViewById(R.id.device_name));
                    TextView device_MACAddress = ((TextView) view.findViewById(R.id.device_MACAddress));
                    TextView device_status = ((TextView) view.findViewById(R.id.device_status));
                    Button device_unpair = ((Button) view.findViewById(R.id.device_unpair));

                    device_name.setText(mRemoteDevice.getName());
                    device_MACAddress.setText(mRemoteDevice.getAddress());
                    if (mBTDeviceList.get(position).getBondState() == BluetoothDevice.BOND_BONDED) {
                        if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED && mRemoteDevice == mBluetoothConnection.getConnectedRemoteDevice()) {

                            device_status.setText("Connected");
                        } else if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTING && mRemoteDevice == mBluetoothConnection.getConnectedRemoteDevice()) {
                            device_status.setText("Connecting");
                        } else {
                            device_status.setText("Paired");
                        }
                    } else if (mBTDeviceList.get(position).getBondState() == BluetoothDevice.BOND_BONDING) {
                        device_status.setText("Pairing");
                    } else if (mBTDeviceList.get(position).getBondState() == BluetoothDevice.BOND_NONE) {
                        device_status.setText("Not Paired");
                    }

                    device_unpair.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            unpairDevice(mRemoteDevice);
                            updateGUI_ListView_BTDeviceList();  // update GUI
                        }
                    });
                    return view;
                }

            };
            mBTDeviceAdapter.notifyDataSetChanged();
            listView_BTDeviceList = (ListView) findViewById(R.id.listView_BTDeviceList);
            listView_BTDeviceList.setAdapter(mBTDeviceAdapter);
            listView_BTDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    BluetoothDevice mRemoteDevice = mBTDeviceList.get(position);
                    Log.d(TAG, "Clicked on " + mRemoteDevice.getName() + ", bondstate: " + mRemoteDevice.getBondState());
                    if (mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                            mBluetoothConnection.disconnect();
                        } else {
                            mBluetoothConnection.startConnectThread(mRemoteDevice, true);
                        }
                    } else {
                        pairDevice(mRemoteDevice);
                    }
                    updateGUI_ListView_BTDeviceList();  // update GUI
                }
            });
            listView_BTDeviceList.post(new Runnable() {
                public void run() {
                    listView_BTDeviceList.setSelection(listView_BTDeviceList.getCount() - 1);
                }
            });
        } else {
            Log.e(TAG, "updateGUI_ListView_BTDeviceList: mBTDeviceList was null");
        }

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
                    updateGUI_ListView_BTDeviceList();
                    break;
                }
                case GlobalVariables.BT_CONNECTION_STATE_CONNECTIONFAILED: {
                    showToast("Connection with remote device failed");
                    break;
                }
                case GlobalVariables.BT_CONNECTION_STATE_CONNECTIONLOST: {
                    showToast("Connection with remote device lost");
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
            BluetoothDevice mRemoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // found remote devices
            Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices(); // list of paired devices
            mBTCurrentState = mBluetoothConnection.getBTConnectionState();
            switch (action) {
                // found a remote device
                case BluetoothDevice.ACTION_FOUND:
                    Log.d(TAG, "onReceive: ACTION_FOUND: " + mRemoteDevice.getName() + ", " + mRemoteDevice.getBondState());
                    if (mBTDeviceList.contains(mRemoteDevice)) {
                        mBTDeviceList.remove(mRemoteDevice); // renew the paired remote device
                    }
                    mBTDeviceList.add(mRemoteDevice);
                    updateGUI_ListView_BTDeviceList();  // update GUI
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
                    }
                    updateGUI_ListView_BTDeviceList();  // update GUI
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
                        mBluetoothConnection.startAcceptThread(true);
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_TURNING_ON) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_TURNING_ON (" + currentState_ThisDevice + ")");
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_TURNING_OFF) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_TURNING_OFF (" + currentState_ThisDevice + ")");
                        mBluetoothAdapter.cancelDiscovery();
                        mBluetoothConnection.stopAllThreads();
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_OFF) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_OFF (" + currentState_ThisDevice + ")");
                        showDisabled();
                    }
                    break;
                // handle BT discovery state
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_STARTED");
                    mBTDeviceList = new ArrayList<BluetoothDevice>();   // always create new list
                    for (BluetoothDevice pairedDevice : mPairedDevices) {
                        mBTDeviceList.add(pairedDevice);    // show paired devices
                    }
                    updateGUI_ListView_BTDeviceList(); // update GUI
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED");
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
                    } else if (currentConnectionState_ThisDevice == BluetoothAdapter.STATE_DISCONNECTING) {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: STATE_DISCONNECTING (" + currentConnectionState_ThisDevice + "), " + mRemoteDevice.getName());
                    } else {
                        Log.d(TAG, "onReceive: currentConnectionState_ThisDevice: action: (" + action.toString() + ")");
                    }
                    updateGUI_ToolBar_BTConnectionState();
                    updateGUI_ListView_BTDeviceList();
                    break;
            }
        }
    };

    public void pairDevice(BluetoothDevice mRemoteDevice) {
        // Cancel discovery because it is memory intensive
        mBluetoothAdapter.cancelDiscovery();
        try {
            Method method = mRemoteDevice.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(mRemoteDevice, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unpairDevice(BluetoothDevice mRemoteDevice) {
        try {
            Method method = mRemoteDevice.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(mRemoteDevice, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
