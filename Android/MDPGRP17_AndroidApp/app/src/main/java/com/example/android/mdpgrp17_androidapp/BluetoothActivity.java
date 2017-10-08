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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import static android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
import static android.view.View.GONE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CANNOTLISTEN;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTED;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTING;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTIONFAILED;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTIONLOST;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_DISCOVERABLE_DURATION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_IDLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.COUNTDOWNTIMER_SERVICE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_READ;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.MESSAGE_WRITE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.REQUESTCODE_BLUETOOTH_CONNECTABLE_DISCOVERABLE;
import static com.example.android.mdpgrp17_androidapp.R.id.device_name;

public class BluetoothActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener {
    private static final String TAG = "BluetoothActivity";
    // codes
    private static final int ACTION_REQUEST_BLUETOOTH_DISCOVERABLE = 0;
    // Bluetooth objects
    private BluetoothConnection mBluetoothConnection;
    private ArrayList<BluetoothDevice> mBTDeviceList;
    private ArrayAdapter mBTDeviceAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private int mBTCurrentState;

    private BluetoothDevice mBluetoothDevice_Selected;
    // UI objects
    private RelativeLayout RLO_BT_DeviceList;
    private RelativeLayout RLO_BT_ThisDevice;
    private Menu menu;
    private ToggleButton TGLBTN_BT_OnOff;
    private ListView LTVW_BT_DeviceList;
    private TextView TXTVW_BT_ThisDevice;
    private TextView TXTVW_BT_ThisDevice_Discoverable;
    private TextView TXTVW_Device;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode);
        Log.d(TAG, "onActivityResult: resultCode: " + resultCode);
        Log.d(TAG, "onActivityResult: data: " + data);
        if (requestCode == REQUESTCODE_BLUETOOTH_CONNECTABLE_DISCOVERABLE) {
            if (resultCode == BT_CONNECTION_STATE_DISCOVERABLE_DURATION) { // i have no idea why resultcode is using the duration
                Log.d(TAG, "onActivityResult: YES");
                stopServiceCounting();
                startService(new Intent(this, CountDownTimerService.class));
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: NO");
                TXTVW_BT_ThisDevice_Discoverable.setText("Not visible to other device");
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
        RLO_BT_DeviceList = (RelativeLayout) findViewById(R.id.RLO_BT_DeviceList);
        RLO_BT_ThisDevice = (RelativeLayout) findViewById(R.id.RLO_BT_ThisDevice);
        LTVW_BT_DeviceList = (ListView) findViewById(R.id.listView_BTDeviceList);
        TGLBTN_BT_OnOff = (ToggleButton) findViewById(R.id.TGLBTN_BT_OnOff);
        TXTVW_BT_ThisDevice = (TextView) findViewById(R.id.TXTVW_BT_ThisDevice);
        TXTVW_BT_ThisDevice_Discoverable = (TextView) findViewById(R.id.TXTVW_BT_ThisDevice_Discoverable);
        TXTVW_Device = (TextView) findViewById(R.id.TXTVW_Device);
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
        // Register BT broadcasts when there is a connection change
        intent_filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        // Register BT broadcasts when there is a scan mode change
        intent_filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

        //  Register all the activities
        registerReceiver(mReceiver, intent_filter);
        registerReceiver(mReceiver_CountDownTimer, new IntentFilter(COUNTDOWNTIMER_SERVICE));
        if (mBTDeviceList != null)
            updateGUI_ListView_BTDeviceList();
        RLO_BT_ThisDevice.setOnTouchListener(this);
        TGLBTN_BT_OnOff.setOnCheckedChangeListener(this);

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
            //   updateGUI_ToolBar_BTConnectionState();
        } else {
            Log.d(TAG, "onResume: BT off");
            // BT off
            showDisabled();
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
        unregisterReceiver(mReceiver);
        if (mReceiver_CountDownTimer != null) {
            stopServiceCounting();
            unregisterReceiver(mReceiver_CountDownTimer);
        }
        super.onDestroy();

    }

    @Override
    public boolean onSupportNavigateUp() {
        mBluetoothAdapter.cancelDiscovery();
        onBackPressed();
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int controlID = compoundButton.getId();
        boolean isChecked = compoundButton.isChecked();
        if (controlID == R.id.TGLBTN_BT_OnOff) {
            Log.d(TAG, "onCheckedChanged: TGLBTN_BT_OnOff: state: " + isChecked);
            if (isChecked) {
                mBluetoothAdapter.enable();     // switch on BT
                Log.d(TAG, "onCheckedChanged: TGLBTN_BT_OnOff: BT switched on");
            } else {
                mBluetoothAdapter.disable();     // switch off BT
                Log.d(TAG, "onCheckedChanged: TGLBTN_BT_OnOff: BT switched off");
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int controlID = view.getId();
        if (controlID == R.id.RLO_BT_ThisDevice) {

            // enable discoverable by other device
            Log.d(TAG, "onTouch: RLO_BT_ThisDevice: enable discoverable");
            Intent intent_Discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent_Discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_CONNECTION_STATE_DISCOVERABLE_DURATION);
            startActivityForResult(intent_Discoverable, REQUESTCODE_BLUETOOTH_CONNECTABLE_DISCOVERABLE);
        }
        return false;
    }

    private void showToast_Short(String message) {
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
        RLO_BT_DeviceList.setVisibility(View.VISIBLE);
        RLO_BT_ThisDevice.setVisibility(View.VISIBLE);
        TGLBTN_BT_OnOff.setChecked(true);
        if (!mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.startDiscovery();
        // update UI - show menu refresh icon
        if (menu != null)
            menu.findItem(R.id.action_settings_bluetooth_refresh).setVisible(true);
        else {
            Log.d(TAG, "showEnabled: Unable to update menu icons");
        }
        TXTVW_BT_ThisDevice.setText(mBluetoothAdapter.getName());
    }

    // Turn off BT
    // Update UI - hide menu refresh icon
    // Update UI - hide BT devices layout
    // Update UI - set BT toggle button to OFF
    private void showDisabled() {
        Log.d(TAG, "showDisabled");
        RLO_BT_DeviceList.setVisibility(GONE);
        RLO_BT_ThisDevice.setVisibility(GONE);
        TGLBTN_BT_OnOff.setChecked(false);

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
            case BT_CONNECTION_STATE_CONNECTING:
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
            case BT_CONNECTION_STATE_CANNOTLISTEN:
                Log.d(TAG, "updateGUI_ToolBar_BTConnectionState: BT connection state: BT_CONNECTION_STATE_CANNOTLISTEN");
                showToast_Short("Please restart bluetooth");
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
                    device_name, mBTDeviceList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    final BluetoothDevice mRemoteDevice = mBTDeviceList.get(position);
                    ImageView device_image = ((ImageView) view.findViewById(R.id.device_image));
                    TextView device_name = ((TextView) view.findViewById(R.id.device_name));
                    TextView device_MACAddress = ((TextView) view.findViewById(R.id.device_MACAddress));
                    TextView device_status = ((TextView) view.findViewById(R.id.device_status));
                    Button device_unpair = ((Button) view.findViewById(R.id.device_unpair));
                    device_image.setImageResource(R.drawable.ic_devices_other_black_24dp);
                    device_name.setText(mRemoteDevice.getName());
                    device_MACAddress.setText(mRemoteDevice.getAddress());

                    if (mBTDeviceList.get(position).getBondState() == BluetoothDevice.BOND_BONDED) {
                        if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED && mRemoteDevice.equals(mBluetoothDevice_Selected)) {
                            device_status.setText("Connected");
                        } else if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTING && mRemoteDevice.equals(mBluetoothDevice_Selected)) {
                            device_status.setText("Connecting");
                        } else {
                            device_status.setText("Paired");
                        }
                    } else if (mBTDeviceList.get(position).getBondState() == BluetoothDevice.BOND_BONDING) {
                        device_status.setText("Pairing");
                    } else if (mBTDeviceList.get(position).getBondState() == BluetoothDevice.BOND_NONE) {
                        device_status.setText("Not Paired");
                        device_unpair.setVisibility(GONE);

                    }
                    device_unpair.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "Clicked on " + mRemoteDevice.getName() + ", bondstate: " + mRemoteDevice.getBondState() + ", removing now");
                            mBluetoothConnection.disconnect();
                            unpairDevice(mRemoteDevice);
                            // updateGUI_ListView_BTDeviceList();  // update GUI
                        }
                    });
                    return view;
                }

            };

            mBTDeviceAdapter.notifyDataSetChanged();
            LTVW_BT_DeviceList = (ListView) findViewById(R.id.listView_BTDeviceList);
            LTVW_BT_DeviceList.setAdapter(mBTDeviceAdapter);
            LTVW_BT_DeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    BluetoothDevice mRemoteDevice = mBTDeviceList.get(position);
                    Log.d(TAG, "Clicked on " + mRemoteDevice.getName() + ", bondstate: " + mRemoteDevice.getBondState());


                    if (mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        if (mBTCurrentState == BT_CONNECTION_STATE_CONNECTED) {
                            Log.d(TAG, "Clicked on " + mRemoteDevice.getName() + " was connected, disconnecting now");
                            mBluetoothConnection.disconnect();
                        } else {
                            Log.d(TAG, "Clicked on " + mRemoteDevice.getName() + " was not connected, connecting now");
                            mBluetoothConnection.startConnectThread(mRemoteDevice, true);
                            mBluetoothDevice_Selected = mRemoteDevice;
                        }
                        // updateGUI_ListView_BTDeviceList();  // update GUI
                    } else if (mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                        Log.d(TAG, "Clicked on " + mRemoteDevice.getName() + " was pairing, pairing again");
//
//                         mBluetoothConnection.disconnect();
//                        mBluetoothConnection.startConnectThread(mRemoteDevice, true);
                        pairDevice(mRemoteDevice);
//                        unpairDevice(mRemoteDevice);
                    } else {
                        Log.d(TAG, "Clicked on " + mRemoteDevice.getName() + " was not paired, pairing now");
//                        mBluetoothConnection.disconnect();
//                        mBluetoothConnection.startConnectThread(mRemoteDevice, true);
                        pairDevice(mRemoteDevice);
                    }


                }
            });
            LTVW_BT_DeviceList.post(new Runnable() {
                public void run() {
                    LTVW_BT_DeviceList.setSelection(LTVW_BT_DeviceList.getCount() - 1);
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
                case BT_CONNECTION_STATE_CONNECTIONFAILED: {
                    showToast_Short("Connection failed. Retrying... ");
                    break;
                }
                case BT_CONNECTION_STATE_CONNECTIONLOST: {
                    showToast_Short("Connection lost. Retrying... ");
                    break;
                }
                case MESSAGE_READ: {
                    if (msg.obj instanceof BluetoothMessageEntity) {
                        showToast_Short("Msg received: " + ((BluetoothMessageEntity) msg.obj).getMessageContent());
                    } else {
                        byte[] read = (byte[]) msg.obj;
                        String incomingMessage = new String(read, 0, msg.arg1);  // byte[]; offset; byteCount
                        Log.d(TAG, "MESSAGE_READ: " + incomingMessage);
                        showToast_Short("Msg received: " + incomingMessage);
                    }
                    break;
                }
                case MESSAGE_WRITE: {
                    if (msg.obj instanceof BluetoothMessageEntity) {
                        showToast_Short("Msg sent: " + ((BluetoothMessageEntity) msg.obj).getMessageContent());
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
            String action = intent.getAction();
            Log.d(TAG, "onReceive: action: " + action);
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
            final int scanMode_ThisDevice = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR); // get scan mode
            Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices(); // list of paired devices
            mBTCurrentState = mBluetoothConnection.getBTConnectionState();
            switch (action) {
                // found a remote device
                case BluetoothDevice.ACTION_FOUND:
                    Log.d(TAG, "onReceive: ACTION_FOUND: " + mRemoteDevice.getName() + ", " + mRemoteDevice.getBondState());
                    if (mBTDeviceList.contains(mRemoteDevice)) {
                        mBTDeviceList.remove(mRemoteDevice); // remmove the paired remote device
                    }
                    mBTDeviceList.add(mRemoteDevice);
                    updateGUI_ListView_BTDeviceList();  // update GUI
                    // showToast_Short("Found " + mRemoteDevice.getName());
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
                        mBluetoothConnection.startAcceptThread(true);
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_TURNING_ON) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_TURNING_ON (" + currentState_ThisDevice + ")");
                    } else if (currentState_ThisDevice == BluetoothAdapter.STATE_TURNING_OFF) {
                        Log.d(TAG, "currentState_ThisDevice: STATE_TURNING_OFF (" + currentState_ThisDevice + ")");
                        mBluetoothAdapter.cancelDiscovery();
                        mBluetoothConnection.stopAllThreads();
                        stopServiceCounting();
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
                    TXTVW_Device.setText("Devices (Discovering)");
                    updateGUI_ListView_BTDeviceList();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.d(TAG, "onReceive: ACTION_DISCOVERY_FINISHED");
                    TXTVW_Device.setText("Devices");
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
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    Log.d(TAG, "onReceive: ACTION_SCAN_MODE_CHANGED");
                    if (scanMode_ThisDevice == SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Log.d(TAG, "onReceive: ACTION_SCAN_MODE_CHANGED: SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                    } else if (scanMode_ThisDevice == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                        Log.d(TAG, "onReceive: ACTION_SCAN_MODE_CHANGED: SCAN_MODE_CONNECTABLE");
                    } else if (scanMode_ThisDevice == BluetoothAdapter.SCAN_MODE_NONE) {
                        Log.d(TAG, "onReceive: ACTION_SCAN_MODE_CHANGED: SCAN_MODE_NONE");
                        TXTVW_BT_ThisDevice_Discoverable.setText("Not visible to other device");
                    }
                    break;

            }

        }
    };

    private void stopServiceCounting() {
        stopService(new Intent(this, CountDownTimerService.class));
    }

    private final BroadcastReceiver mReceiver_CountDownTimer = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case COUNTDOWNTIMER_SERVICE:
                    if (intent.getExtras() != null) {

                        String mTime = intent.getStringExtra("countDownTimerService_mTime");
                        Log.i(TAG, "mReceiver_CountDownTimer: getExtras(): mTime: " + mTime);
                        TXTVW_BT_ThisDevice_Discoverable.setText("Visible for " + mTime);
                        String mfinish = intent.getStringExtra("countDownTimerService_finished");
                        Log.i(TAG, "mReceiver_CountDownTimer: getExtras(): mfinish: " + mfinish);
                        if (mfinish != null)
                            TXTVW_BT_ThisDevice_Discoverable.setText("Not visible to other device");
                    } else {
                        Log.i(TAG, "mReceiver_CountDownTimer: getExtras(): null");
                        TXTVW_BT_ThisDevice_Discoverable.setText("Not visible to other device");
                    }
                    break;
            }
        }
    };

    private void pairDevice(BluetoothDevice device) {
        mBluetoothAdapter.cancelDiscovery();
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getter and Setter
    public BluetoothDevice getmBluetoothDevice_Selected() {
        return mBluetoothDevice_Selected;
    }

    public void setmBluetoothDevice_Selected(BluetoothDevice mBluetoothDevice_Selected) {
        this.mBluetoothDevice_Selected = mBluetoothDevice_Selected;
    }


}
