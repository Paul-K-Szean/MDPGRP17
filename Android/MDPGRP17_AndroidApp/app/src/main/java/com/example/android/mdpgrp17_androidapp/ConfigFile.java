package com.example.android.mdpgrp17_androidapp;

/**
 * Created by szean on 14/9/2017.
 */

public class ConfigFile {
    BluetoothConfig bluetoothConfig;
    FunctionConfig functionConfig;
    TiltConfig tiltConfig;

    public ConfigFile() {
    }

    public ConfigFile(BluetoothConfig bluetoothConfig, FunctionConfig functionConfig, TiltConfig tiltConfig) {
        this.bluetoothConfig = bluetoothConfig;
        this.functionConfig = functionConfig;
        this.tiltConfig = tiltConfig;
    }

    public BluetoothConfig getBluetoothConfig() {
        return bluetoothConfig;
    }

    public void setBluetoothConfig(BluetoothConfig bluetoothConfig) {
        this.bluetoothConfig = bluetoothConfig;
    }

    public FunctionConfig getFunctionConfig() {
        return functionConfig;
    }

    public void setFunctionConfig(FunctionConfig functionConfig) {
        this.functionConfig = functionConfig;
    }

    public TiltConfig getTiltConfig() {
        return tiltConfig;
    }

    public void setTiltConfig(TiltConfig tiltConfig) {
        this.tiltConfig = tiltConfig;
    }

    public class BluetoothConfig {
        // private BluetoothSocket lastConnectedDevice_Socket;
        private String lastConnectedDevice_Name;
        private String lastConnectedDevice_MACAddress;

        public BluetoothConfig(String lastConnectedDevice_Name, String lastConnectedDevice_MACAddress) {
            // this.lastConnectedDevice_Socket = lastConnectedDevice_Socket;
            this.lastConnectedDevice_Name = lastConnectedDevice_Name;
            this.lastConnectedDevice_MACAddress = lastConnectedDevice_MACAddress;
        }

        public String getLastConnectedDevice_Name() {
            return lastConnectedDevice_Name;
        }

        public void setLastConnectedDevice_Name(String lastConnectedDevice_Name) {
            this.lastConnectedDevice_Name = lastConnectedDevice_Name;
        }

        public String getLastConnectedDevice_MACAddress() {
            return lastConnectedDevice_MACAddress;
        }

        public void setLastConnectedDevice_MACAddress(String lastConnectedDevice_MACAddress) {
            this.lastConnectedDevice_MACAddress = lastConnectedDevice_MACAddress;
        }
    }

    public class FunctionConfig {
        private String function01;
        private String function02;
        private String function03;

        public FunctionConfig(String function01, String function02, String function03) {
            this.function01 = function01;
            this.function02 = function02;
            this.function03 = function03;
        }

        public String getFunction01() {
            return function01;
        }

        public void setFunction01(String function01) {
            this.function01 = function01;
        }

        public String getFunction02() {
            return function02;
        }

        public void setFunction02(String function02) {
            this.function02 = function02;
        }

        public String getFunction03() {
            return function03;
        }

        public void setFunction03(String function03) {
            this.function03 = function03;
        }


    }

    public class TiltConfig {
        private float minimum_Up;
        private float minimum_Down;
        private float minimum_Left;
        private float minimum_Right;

        public TiltConfig(float minimum_Up, float minimum_Down, float minimum_Left, float minimum_Right) {
            this.minimum_Up = minimum_Up;
            this.minimum_Down = minimum_Down;
            this.minimum_Left = minimum_Left;
            this.minimum_Right = minimum_Right;
        }

        public float getMinimum_Left() {
            return minimum_Left;
        }

        public void setMinimum_Left(float minimum_Left) {
            this.minimum_Left = minimum_Left;
        }

        public float getMinimum_Right() {
            return minimum_Right;
        }

        public void setMinimum_Right(float minimum_Right) {
            this.minimum_Right = minimum_Right;
        }

        public float getMinimum_Up() {
            return minimum_Up;
        }

        public void setMinimum_Up(float minimum_Up) {
            this.minimum_Up = minimum_Up;
        }

        public float getMinimum_Down() {
            return minimum_Down;
        }

        public void setMinimum_Down(float minimum_Down) {
            this.minimum_Down = minimum_Down;
        }
    }
}