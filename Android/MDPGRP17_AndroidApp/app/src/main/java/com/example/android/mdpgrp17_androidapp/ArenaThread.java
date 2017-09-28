package com.example.android.mdpgrp17_androidapp;

/**
 * Created by NUR SUHAYLAH GHOUSE on 14/2/2017.
 */

import android.util.Log;

/**
 * The ArenaThread class implements Runnable for Arena class
 * through its run method while updating the arena with a 0.2 second delay.
 */

public class ArenaThread extends Thread {
    private static final String TAG = "ArenaThread";
    private Arena arena;
    private boolean running = false;
    private final static int sleepTime = 300;

    public ArenaThread(Arena arena) {
        super();
        Log.d(TAG, "ArenaThread");
        this.arena = arena;
        super.start();
    }

    public void startThread() {
        Log.d(TAG, "ArenaThread: Started");
        this.running = true;
    }

    public void run() {
        Log.d(TAG, "ArenaThread: Running");
        try {
            while (true) {
                // room.updateList();
                arena.update();
                arena.postInvalidate();
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