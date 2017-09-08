package com.example.android.mdpgrp17_androidapp;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by szean on 8/9/2017.
 */

public class Arena extends View {
    private static final String TAG = "Arena";
    private int col = 15;
    private ArenaProperties arenaSettings;
    private ArenaThread thread;
    private int gridSize;
    private int[] grid;
    private int[][] obstacles = new int[15][20];

    public Arena(Context context, int[] array) {
        super(context);
        Log.d(TAG, "Arena");
        arenaSettings = new ArenaProperties();
        thread = new ArenaThread(this);
        thread.startThread();
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        try {
            RelativeLayout arenaView = (RelativeLayout) getRootView().findViewById(R.id.arenaGrid);
            gridSize = ((arenaView.getMeasuredWidth()) - (arenaView.getMeasuredWidth() / col)) / col;
            arenaSettings.drawArena(canvas, gridSize);
        } catch (Exception e) {
        }
    }

    public void setGridArray(int[] gridArray) {
        Log.d(TAG, "setGridArray");
        this.grid = gridArray;
    }

    public void setObstacles(int[][] obstacles) {
        Log.d(TAG, "setObstacles");
        this.obstacles = obstacles;
    }

    public void update() {
        Log.d(TAG, "update");
        arenaSettings.setGridSettings(grid);
        arenaSettings.setObstacles(obstacles);
    }

}
