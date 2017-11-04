package Exploration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import Combine.Main;
import common.Direction;
import map.Map;
import map.WPObstacleState;
import robot.Robot;
import common.GridVector;
import robot.RobotMovement;
import ShortestPath.ShortPathSolve;
import ShortestPath.ShortPathResult;
import ShortestPath.ShortPathSolveType;
import simulation.IUpdate;

public class ExploreSolve {

    private static Map objective_map; // generated from map solver

    private static Simulator simulator;

    private static MapRef mapViewer;
    private static MovementFormulator actionFormulator;
    private static GoalFormulator goalFormulator;

    private static int _exePeriod;
    private static Robot _robot;

    private static volatile boolean permitTermination = true;

    public static void setPermitTerminationState(boolean var) {
        permitTermination = var;
    }

    public static boolean checkPermitTerminationState() {
        return permitTermination;
    }

    private static boolean _hasFinishedFirstRound;

    public static void solve(Map map, int exePeriod) throws InterruptedException, IOException {
        mapViewer = new MapRef();
        goalFormulator = new GoalFormulator(mapViewer);
        _exePeriod = exePeriod;
        objective_map = map;
        simulator = new Simulator(objective_map);

        GridVector robotPos = new GridVector(1, 1);
        Direction robotDir = Direction.Right;
        actionFormulator = new MovementFormulator(mapViewer, simulator);
        _robot = new Robot(robotPos, robotDir, mapViewer, actionFormulator);

        boolean goalZoneReached = false;
        System.out.println("For Simulation Purpose");
        //System.out.println(objective_map.toString(_robot));
        
        //System.out.println(_robot.position());

        // default to rotate in order to get initial sensing data
        _robot.bufferAction(RobotMovement.RotateRight);
        _robot.executeBufferActions(ExploreSolve.getExePeriod());
        // start exploration
        _hasFinishedFirstRound = false;
        while (!goalZoneReached || !goalFormulator.checkIfReachStartZone(_robot.position())) {
            if (_robot.position().equals(new GridVector(map.ROW - 2, map.COL - 2))) {
                goalZoneReached = true;
            }
            actionFormulator.rightWallFollower(_robot);            
            actionFormulator.actionSimplifier(_robot);
        }
        _hasFinishedFirstRound = true;
        if (!mapViewer.checkIfNavigationComplete() && !Main.getGUI().isSingleRoundRun()) {            
            actionFormulator.exploreRemainingArea(_robot);
        }
    }

    public static int getExePeriod() {
        return _exePeriod;
    }

    public static MapRef getMapViewer() {
        return mapViewer;
    }

    public static Robot getRobot() {
        return _robot;
    }

    public static boolean hasFinishedFirstRound() {
        return _hasFinishedFirstRound;
    }

    // look through map and update
    public static void goBackToStart(Map map, Robot robot, Runnable callback) throws IOException, InterruptedException {
        System.out.println("Going back to start with the following map");
        //System.out.println(map.toString(robot));
        ShortPathResult result = new ShortPathSolve().solve(map, robot, Map.START_POS, ShortPathSolveType.Safe);
        LinkedList<RobotMovement> actions = RobotMovement.fromPath(robot, result.shortestPath);

        robot.cleanBufferedActions();
        for (RobotMovement action : actions) {
            robot.bufferAction(action);
            robot.executeBufferActions(ExploreSolve.getExePeriod());
        }
        if (robot.checkBufferActionSize() != 0) {
            robot.executeBufferActions(ExploreSolve.getExePeriod());
        }

        System.out.println("Starting callback");
        _restoreOrientation(callback);
        if (!Main.isSimulating()) {        	
            Main.getRpi().sendExplorationEndMarker();
        }
    }

    private static void _restoreOrientation(Runnable callback) throws IOException {
        System.out.println("restoring orientation");
        if (!_robot.orientation().equals(new Robot().orientation())) {
            if (_robot.orientation().equals(Direction.Up)) {
                _robot.bufferAction(RobotMovement.RotateLeft);
                _robot.executeBufferActions(_exePeriod);
            }
            _robot.bufferAction(RobotMovement.RotateRight);
            _robot.executeBufferActions(_exePeriod);
            _robot.bufferAction(RobotMovement.RotateRight);
            _robot.executeBufferActions(_exePeriod);
        }
        callback.run();
    }
}
