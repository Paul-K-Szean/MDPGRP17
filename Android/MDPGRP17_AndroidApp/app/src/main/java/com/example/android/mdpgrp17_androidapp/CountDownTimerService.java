package com.example.android.mdpgrp17_androidapp;


import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import static com.example.android.mdpgrp17_androidapp.GlobalVariables.BT_CONNECTION_STATE_DISCOVERABLE_DURATION;
import static com.example.android.mdpgrp17_androidapp.GlobalVariables.COUNTDOWNTIMER_SERVICE;

/**
 * Created by szean on 26/9/2017.
 */


public class CountDownTimerService extends Service {
    private final static String TAG = "CountDownTimerService";
    Intent intent_CountDownTimerService = new Intent(COUNTDOWNTIMER_SERVICE);
    CountDownTimer cdt = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: Timer Started");
        cdt = new CountDownTimer((BT_CONNECTION_STATE_DISCOVERABLE_DURATION) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String mTime = String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                intent_CountDownTimerService.putExtra("countDownTimerService_mTime", mTime);
                sendBroadcast(intent_CountDownTimerService);
                Log.i(TAG, "onCreate: onTick: " + mTime);
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "onCreate: onFinish: Timer finished");
                intent_CountDownTimerService.putExtra("countDownTimerService_finished", "countDownTimerService_finished");
                sendBroadcast(intent_CountDownTimerService);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        cdt.cancel();
        Log.i(TAG, "onDestroy: Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBind");
        return null;
    }
}
