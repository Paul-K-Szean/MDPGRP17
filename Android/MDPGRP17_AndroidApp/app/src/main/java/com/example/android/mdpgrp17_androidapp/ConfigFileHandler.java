package com.example.android.mdpgrp17_androidapp;

import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by szean on 14/9/2017.
 */

public class ConfigFileHandler {
    private static final String TAG = "ConfigFileHandler";
    private static final String FILEPATH = "MDPGRP17";
    private static final String FILENAME = "configfile.json";
    private static ConfigFile configFile;
    private File EXTERNALFILE_CONFIGFILE;

    public ConfigFileHandler() {
        this.EXTERNALFILE_CONFIGFILE = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILEPATH, FILENAME);
        EXTERNALFILE_CONFIGFILE.getParentFile().mkdir();
        Log.d(TAG, "isDirectory: " + String.valueOf(EXTERNALFILE_CONFIGFILE.isDirectory()));
        Log.d(TAG, "isFile: " + String.valueOf(EXTERNALFILE_CONFIGFILE.isFile()));
    }

    public ConfigFile getConfigFile() {
        Log.d(TAG, "getConfigFile");
        configFile = readFromExternalStorage();
        if (configFile == null) {
            ConfigFile.BluetoothConfig bluetoothConfig = new ConfigFile().new BluetoothConfig("no device", "99-99-99-99-99");
            ConfigFile.FunctionConfig functionConfig = new ConfigFile().new FunctionConfig("f1", "f2", "f3");
            ConfigFile.TiltConfig tiltConfig = new ConfigFile().new TiltConfig(2.5f, 7.5f, -3.5f, 3.5f);
            this.configFile = new ConfigFile(bluetoothConfig, functionConfig, tiltConfig);
            writeToExternalStorage(configFile);    // write into external storage
        }
        return configFile;
    }

    // convert config file object into json string
    private static String toJSon(ConfigFile configFile) {
        Log.d(TAG, "toJSon");
        try {
            JSONObject jsonObj = new JSONObject();

            // bluetooth config
            JSONObject jsonAdd_BluetoothConfig = new JSONObject();
            jsonAdd_BluetoothConfig.put("lastConnectedDevice_Name", configFile.bluetoothConfig.getLastConnectedDevice_Name());
            jsonAdd_BluetoothConfig.put("lastConnectedDevice_MACAddress", configFile.bluetoothConfig.getLastConnectedDevice_MACAddress());
            // add the object to the main object
            jsonObj.put("bluetoothConfig", jsonAdd_BluetoothConfig);

            // function config
            JSONObject jsonAdd_FunctionConfig = new JSONObject();
            jsonAdd_FunctionConfig.put("function01", configFile.functionConfig.getFunction01());
            jsonAdd_FunctionConfig.put("function02", configFile.functionConfig.getFunction02());
            jsonAdd_FunctionConfig.put("function03", configFile.functionConfig.getFunction03());
            // add the object to the main object
            jsonObj.put("functionConfig", jsonAdd_FunctionConfig);

            // function config
            JSONObject jsonAdd_TiltConfig = new JSONObject();
            jsonAdd_TiltConfig.put("minimum_Up", configFile.tiltConfig.getMinimum_Up());
            jsonAdd_TiltConfig.put("minimum_Down", configFile.tiltConfig.getMinimum_Down());
            jsonAdd_TiltConfig.put("minimum_Left", configFile.tiltConfig.getMinimum_Left());
            jsonAdd_TiltConfig.put("minimum_Right", configFile.tiltConfig.getMinimum_Right());
            // add the object to the main object
            jsonObj.put("tiltConfig", jsonAdd_TiltConfig);


            Log.d(TAG, jsonObj.toString());
            return jsonObj.toString();

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // convert jason string into config file
    private static ConfigFile toConfigFile(String jsonString) {
        Log.d(TAG, "toConfigFile: " + jsonString);
        ConfigFile configFile = null;
        if (jsonString.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(jsonString); // the whole json object
                JSONObject jsonObj_BluetoothConfig = jsonObj.getJSONObject("bluetoothConfig"); // the Bluetooth Config object
                ConfigFile.BluetoothConfig bluetoothConfig =
                        new ConfigFile().new BluetoothConfig(
                                jsonObj_BluetoothConfig.getString("lastConnectedDevice_Name"),
                                jsonObj_BluetoothConfig.getString("lastConnectedDevice_MACAddress"));
                JSONObject jsonObj_FunctionConfig = jsonObj.getJSONObject("functionConfig");// the Function Config object
                ConfigFile.FunctionConfig functionConfig =
                        new ConfigFile().new FunctionConfig(
                                jsonObj_FunctionConfig.getString("function01"),
                                jsonObj_FunctionConfig.getString("function02"),
                                jsonObj_FunctionConfig.getString("function03"));
                JSONObject jsonObj_TiltConfig = jsonObj.getJSONObject("tiltConfig");// the Title Config object
                ConfigFile.TiltConfig tiltConfig =
                        new ConfigFile().new TiltConfig(
                                (float) jsonObj_TiltConfig.getDouble("minimum_Up"),
                                (float) jsonObj_TiltConfig.getDouble("minimum_Down"),
                                (float) jsonObj_TiltConfig.getDouble("minimum_Left"),
                                (float) jsonObj_TiltConfig.getDouble("minimum_Right"));
                configFile = new ConfigFile(bluetoothConfig, functionConfig, tiltConfig);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return configFile;
    }

    // write json string into external storage
    public void writeToExternalStorage(ConfigFile configFile) {
        Log.d(TAG, "writeToExternalStorage");
        if (configFile != null) {
            String jsonString = toJSon(configFile);
            try {
                if (jsonString.length() > 0) {
                    FileOutputStream FOS = new FileOutputStream(EXTERNALFILE_CONFIGFILE);
                    FOS.write(jsonString.getBytes());
                    FOS.close();
                } else {
                    Log.d(TAG, "jsonString was empty or null");
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "writeToExternalStorage: FileNotFoundException: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "writeToExternalStorage: IOException: File write failed: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "writeToExternalStorage: configFile was null");
        }
    }

    // read json string into external storage
    public ConfigFile readFromExternalStorage() {
        Log.d(TAG, "readFromExternalStorage");
        String myData = "";
        ConfigFile configFile = null;
        try {
            FileInputStream FIS = new FileInputStream(EXTERNALFILE_CONFIGFILE);
            BufferedReader BR = new BufferedReader(new InputStreamReader(FIS));
            String strLine;
            while ((strLine = BR.readLine()) != null) {
                myData = myData + strLine;
            }
            BR.close();
            FIS.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "readFromExternalStorage: FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "readFromExternalStorage: IOException: File read failed: " + e.getMessage());
        }

        if (myData.length() > 0) {
            configFile = toConfigFile(myData);
        }

        return configFile;
    }


}

