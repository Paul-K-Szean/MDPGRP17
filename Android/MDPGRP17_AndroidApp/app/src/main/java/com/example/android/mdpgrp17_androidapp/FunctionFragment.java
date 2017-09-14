package com.example.android.mdpgrp17_androidapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FunctionFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "FunctionFragment";
    // GUI Objects
    private EditText ET_Function01;
    private EditText ET_Function02;
    private EditText ET_Function03;
    private Button BTN_SaveFunction;
    // Config file objects
    private ConfigFile configFile;
    private ConfigFileHandler configFileHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_function, container, false);
        ET_Function01 = (EditText) rootView.findViewById(R.id.ET_Function01);
        ET_Function02 = (EditText) rootView.findViewById(R.id.ET_Function02);
        ET_Function03 = (EditText) rootView.findViewById(R.id.ET_Function03);
        BTN_SaveFunction = (Button) rootView.findViewById(R.id.BTN_SaveFunction);
        BTN_SaveFunction.setOnClickListener(this);
        configFileHandler = new ConfigFileHandler();
        configFile = configFileHandler.getConfigFile();

        upateGUI_Function();
        return rootView;
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick");
        switch (view.getId()) {
            case R.id.BTN_SaveFunction: {
                configFile.getFunctionConfig().setFunction01(ET_Function01.getText().toString());
                configFile.getFunctionConfig().setFunction02(ET_Function02.getText().toString());
                configFile.getFunctionConfig().setFunction03(ET_Function03.getText().toString());
                configFileHandler.writeToExternalStorage(configFile);
                showToast("Changes were saved");
                break;
            }
        }
    }

    private void upateGUI_Function() {
        Log.d(TAG, "upateGUI_Function");
        ET_Function01.setText(configFile.getFunctionConfig().getFunction01());
        ET_Function02.setText(configFile.getFunctionConfig().getFunction02());
        ET_Function03.setText(configFile.getFunctionConfig().getFunction03());
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
