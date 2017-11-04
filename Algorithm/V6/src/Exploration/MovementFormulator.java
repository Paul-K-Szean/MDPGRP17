package Exploration;

import map.Waypoint;
import robot.Robot;
import robot.RobotMovement;

import java.io.IOException;
import java.util.LinkedList;

import Combine.Main;
import common.Direction;
import common.GridVector;
import map.Map;
import map.WPObstacleState;
import communication.*;
import ShortestPath.ShortPathSolve;
import ShortestPath.ShortPathResult;
import ShortestPath.ShortPathSolveType;;

public class MovementFormulator {

    private MapRef mapViewer;

    private Simulator simulator;

    private static volatile boolean calibrationCompleted = false;
    private static volatile boolean isSensingDataArrived = false;
    private static volatile String sensingDataFromRPI;

    public MovementFormulator(MapRef mapV, Simulator s) {
        mapViewer = mapV;
        simulator = s;
    }

    public String actionToTake(Waypoint frontier, Robot robot) {
        String result = "";
        return result;
    }

    public void reverseToThePoint(RobotMovementHistory robotState, Robot robot)
            throws InterruptedException, IOException {
        ShortPathSolve astarsolver = new ShortPathSolve();
        ShortPathResult astarSolverResult = astarsolver.solve(mapViewer.getSubjectiveMap(), robot,
                robotState.pos);
        LinkedList<RobotMovement> robotActions = RobotMovement.fromPath(robot, astarSolverResult.shortestPath);
        // System.out.println("Action size: " + robotActions.size());
        robotActions.forEach((action) -> {
            // System.out.println("Here3");
            robot.bufferAction(action);
        });
        view(robot);

        while (robot.orientation() != robotState.dir) {
            robot.bufferAction(RobotMovement.RotateLeft);
            view(robot);
        }

    }

    public void exploreRemainingArea(Robot _robot) throws InterruptedException, IOException {
        boolean reachablePointFound = false;
        LinkedList<GridVector> reachableList;
        ShortPathSolve astarSolver = new ShortPathSolve();
        LinkedList<RobotMovement> robotActions;

        while (!mapViewer.checkIfNavigationComplete()) {
            boolean ventureIntoDangerousZone = false;
            boolean dangerousZoneEntered = false;

            LinkedList<GridVector> goalList = mapViewer.findUnexploredInAscendingDistanceOrder(_robot);
            System.out.println("My goal list  " + goalList.toString());
            GridVector goal = new GridVector(-1, -1);

            for (int i = 0; i < goalList.size(); i++) {
                if (mapViewer.markGhostBlock(goalList.get(i))) {
                    System.out.println("Ghost block " + goalList.get(i).toString() + " marked");
                    goalList.remove(i);

                }
            }
            System.out.println(mapViewer.exploredAreaToString());
            System.out.println(mapViewer.confidenceDetectionAreaToString());

            for (int i = 0; i < goalList.size(); i++) {

                System.out.println("Processing goal " + goalList.get(i).toString());

                reachableList = ExploreCal.findScannableReachableFromGoal(goalList.get(i), _robot , mapViewer);
                for (int j = 0; j < reachableList.size(); j++) {

                    if (!mapViewer.checkRobotVisited(reachableList.get(j))) {
                        // if
                        // (!mapViewer.checkScanningRepeatedArea(reachableList.get(j)))
                        // {

                        goal = reachableList.get(j);
                        reachablePointFound = true;
                        System.out.println("Goal found " + goal);
                        break;
                    }
                }

                if (reachablePointFound) {
                    break; /// findFirst goal
                }
                mapViewer.markUnreachable(goalList.get(i));
            }

            if (reachablePointFound) {

                System.out.println("Current goal: " + goal.toString());

                ShortPathResult astarSolverResult = astarSolver.solve(mapViewer.getSubjectiveMap(), _robot, goal,
                        ShortPathSolveType.Safe);

                robotActions = RobotMovement.fromPath(_robot, astarSolverResult.shortestPath);

                System.out.println(robotActions.toString());
                // System.out.println("Action size: " + robotActions.size());
                int i = 0;
                for (i = 0; i < robotActions.size(); i++) {
                    Robot robotSimulator = new Robot(new GridVector(_robot.position().x(), _robot.position().y()),
                            _robot.orientation());
                    if (i >= 1)
                        robotSimulator.execute(robotActions.get(i - 1));

                    if (ventureIntoDangerousZone == false && !ExploreCal.checkIfInDangerousZone(robotSimulator, mapViewer)) {
                        robotSimulator.execute(robotActions.get(i));

                        if (ExploreCal.checkIfInDangerousZone(robotSimulator, mapViewer)) {

                            ventureIntoDangerousZone = true;

                            System.out.println("Venturing into somewhere dangerous");
                           // if (!Main.isSimulating())
                             //   Main.getRpi().sendCalibrationCommand(SensorCalibration.Emergency);
                        }
                    }
                    // for actions , check next move and give calibration
                    // command
                    System.out.println(_robot.position().toString());
                    System.out.println(_robot.orientation().toString());
                    view(_robot);
                    if (mapViewer.checkIfNavigationComplete())
                        break;

                    if (ExploreCal.checkIfInDangerousZone(_robot , mapViewer)) {
                        dangerousZoneEntered = true;
                    }

                    if (dangerousZoneEntered && ventureIntoDangerousZone && !ExploreCal.checkIfInDangerousZone(_robot, mapViewer)) {
                        dangerousZoneEntered = false;
                        ventureIntoDangerousZone = false;
                    }

                    if (!mapViewer.validate(_robot, robotActions.get(i))) {
                        // actionFormulator.circumvent(_robot);
                        // System.out.println("Here2");
                        // in circumvent, stop circumventing when the obstacle
                        // is fully identified
                        view(_robot); // take a look , update map
                        break;
                    }
                    _robot.bufferAction(robotActions.get(i));
                }
                view(_robot);
            }
            // prepare for next loop
            reachablePointFound = false;
        }
        System.out.println("All remaining blocks explored or checked as unreachable ");
        System.out.println("Exploration completed");
    }

    public void rightWallFollower(Robot robot) throws InterruptedException, IOException {

        view(robot); // for scanning purpose

        if (null != ExploreCal.checkWalkable(robot, Direction.Right, mapViewer)) {

            switch (ExploreCal.checkWalkable(robot, Direction.Right, mapViewer)) {
            case Yes:
                robot.bufferAction(RobotMovement.RotateRight);
                view(robot);
                robot.bufferAction(RobotMovement.MoveForward);
                break;
            case No:
                turnLeftTillEmpty(robot); // now didnt turn left , so execute
                                          // directly
                break;
            case Unsure: {
                robot.bufferAction(RobotMovement.RotateRight);
                view(robot);
                if (null == ExploreCal.checkWalkable(robot, Direction.Up, mapViewer)) {
                    System.out.println("Error1");
                } else {
                    switch (ExploreCal.checkWalkable(robot, Direction.Up, mapViewer)) {
                    case Yes:
                        robot.bufferAction(RobotMovement.MoveForward);
                        break;
                    case No:
                        robot.bufferAction(RobotMovement.RotateLeft);
                        turnLeftTillEmpty(robot);
                        break;
                    default:
                        System.out.println("Error1");
                        break;
                    }
                }
                break;
            }
            default:
                break;
            }
        }
        view(robot);
    }

    public static void sensingDataCallback(String input) {
        sensingDataFromRPI = input;

    }

    // look through map and update
    public Map view(Robot robot) throws InterruptedException, IOException {

        /// check current map configuration , give calibration command

        if (robot.checkIfHavingBufferActions()) {
          //  predictAndSendCalibrationReminder(robot, robot.getBufferedActions().get(0));
            robot.executeBufferActions(ExploreSolve.getExePeriod());
        }

        SensorData s = new SensorData(); // otherwise s may not have been
                                           // initialized
        if (Main.isSimulating()) {
            s = simulator.getSensingData(robot);
        } else {

            // RPI call here
            // while (isSensingDataArrived != true) {}
            if (sensingDataFromRPI.isEmpty()) {
                System.out.println("ERROR: empty sensing data");
            }

            s.front_l = Integer.parseInt(Character.toString(sensingDataFromRPI.charAt(1)));
            s.left = Integer.parseInt(Character.toString(sensingDataFromRPI.charAt(2)));
            s.right_f = Integer.parseInt(Character.toString(sensingDataFromRPI.charAt(3)));
            s.front_r = Integer.parseInt(Character.toString(sensingDataFromRPI.charAt(4)));
            s.front_m = Integer.parseInt(Character.toString(sensingDataFromRPI.charAt(5))); 
          //  s.right_m = Integer.parseInt(Character.toString(sensingDataFromRPI.charAt(6)));

        }

        Map subjective_map = mapViewer.updateMap(robot, s);
        //System.out.println(mapViewer.exploredAreaToString());
        // System.out.println(mapViewer.robotVisitedPlaceToString());
        // System.out.println(mapViewer.confidenceDetectionAreaToString());
        //System.out.println(subjective_map.toString(robot));

        isSensingDataArrived = false;

        return subjective_map;
    }

    public static void calibrationCompletedCallBack() {
        calibrationCompleted = true;
    }

    public void circumvent(Robot robot) throws InterruptedException, IOException {
        GridVector initialPosition = robot.position();
        while (robot.position().x() != initialPosition.x() && robot.position().y() != initialPosition.y()) {
            // rightWallFollower(robot);
            // System.out.println("Loop");
        }
    }

    public void turnLeftTillEmpty(Robot robot) throws InterruptedException, IOException {

        Confirm check = ExploreCal.checkWalkable(robot, Direction.Up, mapViewer);

        /* if (check == Know.Unsure) { */
        view(robot);
        /* } */
        // make sure it is viewed before turn
        // update
        check = ExploreCal.checkWalkable(robot, Direction.Up, mapViewer);

        if (check == Confirm.Yes) {
            robot.bufferAction(RobotMovement.MoveForward);
            return;
        }

        if (check == Confirm.No) {
            robot.bufferAction(RobotMovement.RotateLeft);
            view(robot);
            turnLeftTillEmpty(robot);

        }

    }

    public void actionSimplifier(Robot _robot) throws InterruptedException, IOException {

        if (!_robot.checkIfRobotVisitedBefore()) {
            boolean rightBlocked = (ExploreCal.checkWalkable(_robot, Direction.Right, mapViewer) == Confirm.No);
            boolean frontBlocked = (ExploreCal.checkWalkable(_robot, Direction.Up, mapViewer) == Confirm.No);
            if (checkLeftWallCondition(_robot)) {
                if (cutRightWall(_robot)) {
                    // both cut
                    _robot.bufferAction(RobotMovement.RotateLeft);
                    view(_robot);
                    _robot.bufferAction(RobotMovement.MoveForward);
                    view(_robot);
                } else if (rightBlocked && frontBlocked) {
                    // only cut left
                    _robot.bufferAction(RobotMovement.RotateLeft);
                    view(_robot);
                    _robot.bufferAction(RobotMovement.RotateLeft);
                    view(_robot);
                    _robot.bufferAction(RobotMovement.MoveForward);
                    view(_robot);
                }
                // else go left without cutting left
            } else {
                // left cut not allowed, check right cut
                cutRightWall(_robot);
            }
        }
    }
  
    public boolean checkLeftWallCondition(Robot _robot) {
        GridVector left_m, left_b, left_f, leftDiagonal, basicVector, doubtedPosition;
        basicVector = _robot.orientation().getLeft().toVector2();
        doubtedPosition = _robot.position().fnAdd(basicVector);
        left_m = _robot.position().fnAdd(_robot.orientation().getLeft().toVector2().fnMultiply(2));
        left_f = left_m.fnAdd(_robot.orientation().toVector2());
        left_b = left_m.fnAdd(_robot.orientation().getBehind().toVector2());
        leftDiagonal = left_m.fnAdd(_robot.orientation().getBehind().toVector2().fnMultiply(2));
        if (mapViewer.getSubjectiveMap().checkValidBoundary(left_m)
                && mapViewer.getSubjectiveMap().checkValidBoundary(left_f)
                && mapViewer.getSubjectiveMap().checkValidBoundary(left_b)) {
            if (mapViewer.getExploredState(left_f) == 1 && mapViewer.getExploredState(left_m) == 1 && mapViewer.getExploredState(left_b) == 1){
                left_m = left_m.fnAdd(basicVector);
                left_f = left_f.fnAdd(basicVector);
                left_b = left_b.fnAdd(basicVector);
                if ((!mapViewer.getSubjectiveMap().checkValidBoundary(left_f)|| mapViewer.getExploredState(left_f) == 2)
                        && (!mapViewer.getSubjectiveMap().checkValidBoundary(left_m)|| mapViewer.getExploredState(left_m) == 2)
                        && (!mapViewer.getSubjectiveMap().checkValidBoundary(left_b)|| mapViewer.getExploredState(left_b) == 2)){
                    if (mapViewer.getSubjectiveMap().checkValidBoundary(leftDiagonal)&& mapViewer.getExploredState(leftDiagonal) == 2){
                        if (!doubtedPosition.equals(new GridVector(1, 1))
                                && !doubtedPosition.equals(new GridVector(Map.ROW - 2, Map.COL - 2)))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean cutRightWall(Robot _robot) throws InterruptedException, IOException {
        if (_robot.checkIfRobotVisitedBefore())
            return false;
        GridVector right_up, right_down, right_middle, front_m, front_l, front_r,unexploredDiagonal1, unexploredDiagonal2, unexploredDiagonal3;
        boolean frontWall;
        boolean right_up_explored, right_down_explored, right_middle_explored;
        int counter;
        frontWall = false;
        right_up_explored = false;
        right_down_explored = false;
       right_middle_explored = false;
        right_up = _robot.position().fnAdd(_robot.orientation().toVector2())
                .fnAdd(_robot.orientation().getRight().toVector2().fnMultiply(2));
        right_down = _robot.position().fnAdd(_robot.orientation().getBehind().toVector2())
                .fnAdd(_robot.orientation().getRight().toVector2().fnMultiply(2));
        right_middle = _robot.position().fnAdd(_robot.orientation().getRight().toVector2().fnMultiply(2));
        front_m = _robot.position().fnAdd(_robot.orientation().toVector2().fnMultiply(2));
        front_l = front_m.fnAdd(_robot.orientation().getLeft().toVector2());
        front_r = front_m.fnAdd(_robot.orientation().getRight().toVector2());

        if (!mapViewer.getSubjectiveMap().checkValidBoundary(front_r)
                && !mapViewer.getSubjectiveMap().checkValidBoundary(front_l)
                && !mapViewer.getSubjectiveMap().checkValidBoundary(front_m)) {
            frontWall = true;
        }
        counter = 0;
        while (counter < 4 && mapViewer.getSubjectiveMap().checkValidBoundary(right_up)
                && mapViewer.getSubjectiveMap().getPoint(right_up).obstacleState() != WPObstacleState.IsActualObstacle
                && mapViewer.getExploredState(right_up) != 0) {
            right_up = right_up.fnAdd(_robot.orientation().getRight().toVector2());
            counter++;
        }

        if (!mapViewer.getSubjectiveMap().checkValidBoundary(right_up)
                || mapViewer.getSubjectiveMap().getPoint(right_up).obstacleState() == WPObstacleState.IsActualObstacle)
            right_up_explored = true;

        
        counter = 0;
        while (counter < 4 && mapViewer.getSubjectiveMap().checkValidBoundary(right_up)
                && mapViewer.getSubjectiveMap().getPoint(right_up).obstacleState() != WPObstacleState.IsActualObstacle
                && mapViewer.getExploredState(right_up) != 0) {
            right_up = right_up.fnAdd(_robot.orientation().getRight().toVector2());
            counter++;
        }

        if (!mapViewer.getSubjectiveMap().checkValidBoundary(right_up)
                || mapViewer.getSubjectiveMap().getPoint(right_up).obstacleState() == WPObstacleState.IsActualObstacle)
            right_up_explored = true;
       
        counter = 0;
        while (counter < 4 && mapViewer.getSubjectiveMap().checkValidBoundary(right_down)
                && mapViewer.getSubjectiveMap().getPoint(right_down).obstacleState() != WPObstacleState.IsActualObstacle
                && mapViewer.getExploredState(right_down) != 0) {
            right_down = right_down.fnAdd(_robot.orientation().getRight().toVector2());
            counter++;
        }

        if (!mapViewer.getSubjectiveMap().checkValidBoundary(right_down) || mapViewer.getSubjectiveMap()
                .getPoint(right_down).obstacleState() == WPObstacleState.IsActualObstacle)
            right_down_explored = true;
        

        
        counter = 0;
        while (counter < 4 && mapViewer.getSubjectiveMap().checkValidBoundary(right_middle)
                && mapViewer.getSubjectiveMap().getPoint(right_middle)
                        .obstacleState() != WPObstacleState.IsActualObstacle
                && mapViewer.getExploredState(right_middle) != 0) {
            right_middle = right_middle.fnAdd(_robot.orientation().getRight().toVector2());
            counter++;
        }

        if (!mapViewer.getSubjectiveMap().checkValidBoundary(right_middle) || mapViewer.getSubjectiveMap()
                .getPoint(right_middle).obstacleState() == WPObstacleState.IsActualObstacle)
            right_middle_explored = true;

        
        unexploredDiagonal1 = _robot.position().fnAdd(_robot.orientation().getBehind().toVector2().fnMultiply(2))
                .fnAdd(_robot.orientation().getRight().toVector2().fnMultiply(2));
        unexploredDiagonal2 = unexploredDiagonal1.fnAdd(_robot.orientation().getRight().toVector2());
        unexploredDiagonal3 = unexploredDiagonal2.fnAdd(_robot.orientation().getRight().toVector2());

        if (right_middle_explored && right_down_explored && right_up_explored && frontWall) {

            if ((!mapViewer.getSubjectiveMap().checkValidBoundary(unexploredDiagonal1) || mapViewer.getSubjectiveMap()
                    .getPoint(unexploredDiagonal1).obstacleState() == WPObstacleState.IsActualObstacle)
                    && (!mapViewer.getSubjectiveMap().checkValidBoundary(unexploredDiagonal2)
                            || mapViewer.getExploredState(unexploredDiagonal2) != 0)
                    && (!mapViewer.getSubjectiveMap().checkValidBoundary(unexploredDiagonal3)
                            || mapViewer.getExploredState(unexploredDiagonal3) != 0)) {
                _robot.bufferAction(RobotMovement.RotateLeft);
                view(_robot);
                return true;
            }
        }
        return false;
        
    }
}
