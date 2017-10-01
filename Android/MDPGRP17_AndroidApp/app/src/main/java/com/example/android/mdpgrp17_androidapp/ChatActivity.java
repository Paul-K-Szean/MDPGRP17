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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CANNOTLISTEN;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_CONNECTED;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_IDLE;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_LISTENING;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private BluetoothConnection mBluetoothConnection;
    private BluetoothAdapter mBluetoothAdapter;
    private int mBTCurrentState;
    // GUI object
    private Menu menu;
    // Config objects
    private ConfigFileHandler configFileHandler;
    private ConfigFile configFile;

    private ViewPager viewPager;

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        Log.d(TAG, "setupViewPager");
        Adapter adapter = new Adapter(getSupportFragmentManager());

        adapter.addFragment(new ChatFragment(), "Chat");
        adapter.addFragment(new FunctionFragment(), "Functions");

        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

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
            Log.d(TAG, "Clicked On action_settings_bluetooth");
            Intent intent_bluetooth = new Intent(this, BluetoothActivity.class);
            startActivity(intent_bluetooth);
        } else if (id == R.id.action_settings_bluetooth_reconnect) {
            Log.d(TAG, "Clicked On action_settings_bluetooth_reconnect: mBTCurrentState: " + mBTCurrentState);
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
                Log.d(TAG, "onClick: action_settings_bluetooth_reconnect: BT_CONNECTION_STATE_CANNOTLISTEN");
                showToast_Short("Please restart bluetooth");
            } else {
                Log.d(TAG, "onClick: action_settings_bluetooth_reconnect: " + mBTCurrentState);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        menu.getItem(2).setVisible(false);
        menu.getItem(3).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothConnection = BluetoothConnection.getmBluetoothConnection(mHandler);

        // Display back to home navigation
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Setting ViewPager for each Tabs
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        configFileHandler = new ConfigFileHandler();
        configFile = configFileHandler.getConfigFile();

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
        mBTCurrentState = mBluetoothConnection.getBTConnectionState();
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "onResume: BT on");
            // BT on
            if (mBTCurrentState == BT_CONNECTION_STATE_IDLE) {
                mBluetoothConnection.startAcceptThread(true);
            }
            setupViewPager(viewPager); // update the fragment
            updateGUI_ToolBar_BTConnectionState();
            updateGUI_MenuIcon();
        } else {
            // BT off
            Log.d(TAG, "onResume: BT off");
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
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showToast_Short(String message) {
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
                    } else if (currentBTConnectionState == BT_CONNECTION_STATE_LISTENING) {
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

    public void updateGUI_ToolBar_BTConnectionState() {
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
                        showToast_Short("Msg received: " + ((BluetoothMessageEntity) msg.obj).getMessageContent());
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
                    Log.d(TAG, "onReceive: ACTION_ACL_CONNECTED");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_DISCONNECTED");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED:
                    Log.d(TAG, "onReceive: ACTION_ACL_DISCONNECT_REQUESTED");
                    break;
            }
        }
    };

}
