package com.example.android.mdpgrp17_androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;


/**
 * Created by szean on 29/8/2017.
 */
public class BluetoothDevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "BluetoothDevicesAdapter";
    Context context;
    View view;
    ArrayList<BluetoothDevice> mBTDeviceList;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnection mBluetoothConnection;


    public BluetoothDevicesAdapter(Context context, BluetoothConnection mBluetoothConnection, ArrayList<BluetoothDevice> mBTDeviceList) {
        this.context = context;

        this.mBluetoothConnection = mBluetoothConnection;
        this.mBTDeviceList = mBTDeviceList;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.item_devices, parent, false);
        Item item = new Item(row);
        return item;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        ((Item) holder).device_name.setText(mBTDeviceList.get(position).getName());
        ((Item) holder).device_MACAddress.setText(mBTDeviceList.get(position).getAddress());
        BluetoothDevice mRemoteDevice = mBTDeviceList.get(position);
        checkBTConnectionState(mRemoteDevice, ((Item) holder).device_status, ((Item) holder).device_unpair);
    }

    @Override
    public int getItemCount() {
        return mBTDeviceList.size();
    }


    public class Item extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView device_image;
        TextView device_name, device_MACAddress, device_status;
        Button device_unpair;


        public Item(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            device_image = (ImageView) itemView.findViewById(R.id.device_image);
            device_name = (TextView) itemView.findViewById(R.id.device_name);
            device_MACAddress = (TextView) itemView.findViewById(R.id.device_MACAddress);
            device_status = (TextView) itemView.findViewById(R.id.device_status);
            device_unpair = (Button) itemView.findViewById(R.id.device_unpair);
            device_unpair.setOnClickListener(this);
            view = itemView;
        }

        @Override
        public void onClick(View view) {
            BluetoothDevice mRemoteDevice = mBTDeviceList.get(this.getLayoutPosition());
            Log.d(TAG, "Remote device selected: " + mRemoteDevice.getName() + ", BOND_STATE (" + mRemoteDevice.getBondState() + ")");
            switch (view.getId()) {
                case R.id.device_unpair:
                    Log.d(TAG, "Un-pair button clicked");
                    unpairDevice(mRemoteDevice);
                    break;
                default:
                    Log.d(TAG, "onClick: Default clause");
                    int currentBTConnectionState = mRemoteDevice.getBondState();
                    // paired = start connection with remote device
                    if (currentBTConnectionState == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "onClick: Already paired with " + mRemoteDevice.getName() + ". Checking connection");
                        if (currentBTConnectionState == BluetoothAdapter.STATE_CONNECTED) {
                            Log.d(TAG, "onClick: Already connected with " + mRemoteDevice.getName());
                        } else if (currentBTConnectionState != BluetoothAdapter.STATE_CONNECTED) {
                            Log.d(TAG, "onClick: Not yet connected, starting connection with " + mRemoteDevice.getName());
                            mBluetoothConnection.startConnectThread(mRemoteDevice, true);
                        }
                    } else {
                        Log.d(TAG, "onClick: Pairing with " + mRemoteDevice.getName());
                        pairDevice(mRemoteDevice);
                    }
                    break;
            }
        }
    }

    private void checkBTConnectionState(BluetoothDevice mRemoteDevice, TextView device_status, Button device_unpair) {
        int currentBTConnectionState = mBluetoothConnection.getBTConnectionState();
        Log.d(TAG, "checkBTConnectionState: " + currentBTConnectionState);

        device_unpair.setVisibility(View.INVISIBLE);

        if (mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            Log.d(TAG, "Paired with " + mRemoteDevice.getName());
            device_status.setText("Paired");
            device_unpair.setVisibility(View.VISIBLE);
        } else if (mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
            Log.d(TAG, "Pairing with " + mRemoteDevice.getName());
            device_status.setText("Pairing");
        } else if (mRemoteDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            Log.d(TAG, mRemoteDevice.getName() + " not paired");
            device_status.setText("Not Paired");
        }


        if (currentBTConnectionState == mBluetoothAdapter.STATE_CONNECTED && mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            Log.d(TAG, "Paired and connected with " + mRemoteDevice.getName());
            device_status.setText("Connected");
            device_unpair.setVisibility(View.VISIBLE);
        } else if (currentBTConnectionState == mBluetoothAdapter.STATE_CONNECTING && mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            Log.d(TAG, "Paired and connecting with " + mRemoteDevice.getName());
            device_status.setText("Connecting");
        } else {
            // device_status.setText("Not Connected");
            // back to none/bonding/bonded
        }


    }

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