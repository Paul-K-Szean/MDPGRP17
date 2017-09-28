package com.example.android.mdpgrp17_androidapp;

import android.os.Handler;
import android.util.Log;

/**
 * Created by szean on 26/9/2017.
 */

public class TimerThread extends Thread {
    private static final String TAG = "TimerThread";
    private final static int sleepTime = 200;
    private boolean running = false;
    private long startTime = 0;
    Handler timerHandler = new Handler();

    private int waitTimeInSec;

    public TimerThread(int waitTimeInSec) {
        super();
        this.waitTimeInSec = waitTimeInSec;
        super.start();
    }

    public void startTimer() {
        Log.d(TAG, "TimerThread: startTimer: Started");
        this.running = true;
    }

    public void run() {
        Log.d(TAG, "TimerThread: Running");
        try {
            while (true) {
                sleep(sleepTime);
                while (this.running == false) {
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }

}
