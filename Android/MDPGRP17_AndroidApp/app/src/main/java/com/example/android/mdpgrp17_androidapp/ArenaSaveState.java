package com.example.android.mdpgrp17_androidapp;

/**
 * Created by szean on 28/9/2017.
 */

public class ArenaSaveState {
    private int[][] arenaInfo, travelInfo;
    private boolean isWayPointReached;
    private int robotPosition_Row_Center, robotPosition_Col_Center;   // center of way point

    public ArenaSaveState(int[][] arenaInfo, int[][] travelInfo, boolean isWayPointReached, int robotPosition_Row_Center, int robotPosition_Col_Center) {
        this.arenaInfo = arenaInfo;
        this.travelInfo = travelInfo;
        this.isWayPointReached = isWayPointReached;
        this.robotPosition_Row_Center = robotPosition_Row_Center;
        this.robotPosition_Col_Center = robotPosition_Col_Center;
    }

    public int getRobotPosition_Row_Center() {
        return robotPosition_Row_Center;
    }

    public void setRobotPosition_Row_Center(int robotPosition_Row_Center) {
        this.robotPosition_Row_Center = robotPosition_Row_Center;
    }

    public int getRobotPosition_Col_Center() {
        return robotPosition_Col_Center;
    }

    public void setRobotPosition_Col_Center(int robotPosition_Col_Center) {
        this.robotPosition_Col_Center = robotPosition_Col_Center;
    }

    public int[][] getArenaInfo() {
        return arenaInfo;
    }

    public void setArenaInfo(int[][] arenaInfo) {
        this.arenaInfo = arenaInfo;
    }

    public int[][] getTravelInfo() {
        return travelInfo;
    }

    public void setTravelInfo(int[][] travelInfo) {
        this.travelInfo = travelInfo;
    }

    public boolean getWayPointReached() {
        return isWayPointReached;
    }

    public void setWayPointReached(boolean wayPointReached) {
        isWayPointReached = wayPointReached;
    }
}
