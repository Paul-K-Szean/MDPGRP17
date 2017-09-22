package com.example.android.mdpgrp17_androidapp;

/**
 * Created by szean on 14/9/2017.
 */

public class ConfigFile {

    BluetoothConfig bluetoothConfig;
    FunctionConfig functionConfig;

    public ConfigFile() {
    }

    public ConfigFile(BluetoothConfig bluetoothConfig, FunctionConfig functionConfig) {
        this.bluetoothConfig = bluetoothConfig;
        this.functionConfig = functionConfig;
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

    public class BluetoothConfig {
        // private BluetoothSocket lastConnectedDevice_Socket;
        private String lastConnectedDevice_Name;
        private String lastConnectedDevice_MACAddress;

        public BluetoothConfig(String lastConnectedDevice_Name, String lastConnectedDevice_MACAddress) {
            // this.lastConnectedDevice_Socket = lastConnectedDevice_Socket;
            this.lastConnectedDevice_Name = lastConnectedDevice_Name;
            this.lastConnectedDevice_MACAddress = lastConnectedDevice_MACAddress;
        }

//        public BluetoothSocket getLastConnectedDevice_Socket() {
//            return lastConnectedDevice_Socket;
//        }
//
//        public void setLastConnectedDevice_Socket(BluetoothSocket lastConnectedDevice_Socket) {
//            this.lastConnectedDevice_Socket = lastConnectedDevice_Socket;
//        }

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
}