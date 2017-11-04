package Exploration;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import map.Map;
import map.WPSpecialState;
import robot.Robot;
import robot.RobotMovement;
import ShortestPath.ShortPathSolve;
import ShortestPath.ShortPathResult;
import common.GridVector;
import common.Direction;
import Combine.Main;
import map.WPObstacleState;
import communication.MessageTranslator;
import communication.SocketComm;


public class MapRef  {

    private Map map;
    // 1 empty, 2 obstacle, 0 havent explored
    private int[][] explored;
    private int[][] robotPosition;
    private int[][] confidentDetectionArea;
    private int[][] scanningRepeatedArea;
    public LinkedList<RobotMovementHistory> robotMovementHistory;

    MapRef() {
        map = new Map();
        explored = new int[][] { 
        		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };
        robotMovementHistory = new LinkedList<RobotMovementHistory>();
        robotPosition = new int[][] { 
        		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };
        confidentDetectionArea = new int[][] { 
        		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };
        
        scanningRepeatedArea = new int[][] { 
        		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };          
                                      
    }

    public boolean checkScanningRepeatedArea(GridVector v){
        if (map.checkValidPosition(v)) {
            if (scanningRepeatedArea[v.x()][v.y()] == 1)
                return true;
        }
        return false;        
    }
    
    public void markScanningRepeatedArea(GridVector v){
        if (map.checkValidPosition(v)) {
            scanningRepeatedArea[v.x()][v.y()] =1;                
        }
    }      
    
    public boolean markRobotVisited(GridVector v) {
        if (map.checkValidPosition(v)) {
            robotPosition[v.x()][v.y()] = 1;
            return true;
        }
        return false;
    }

    public boolean checkConfidence(GridVector v) {
        if (map.checkValidBoundary(v)) {
            if (confidentDetectionArea[v.x()][v.y()] == 1)
                return true;
            else
                return false;
        }
        return true;
    }

    public boolean markConfidentRange(Robot robot) {
        GridVector edge, edge_l, edge_r, edge_rm, edge_rf, edge_lf, left;

        GridVector robotPosition = robot.position();
        markConfidentDetection(robotPosition);
        markConfidentDetectionDirect(robotPosition.x(), robotPosition.y() + 1);
        markConfidentDetectionDirect(robotPosition.x(), robotPosition.y() - 1);
        markConfidentDetectionDirect(robotPosition.x() - 1, robotPosition.y());
        markConfidentDetectionDirect(robotPosition.x() + 1, robotPosition.y());
        markConfidentDetectionDirect(robotPosition.x() + 1, robotPosition.y() + 1);
        markConfidentDetectionDirect(robotPosition.x() - 1, robotPosition.y() - 1);
        markConfidentDetectionDirect(robotPosition.x() + 1, robotPosition.y() - 1);
        markConfidentDetectionDirect(robotPosition.x() - 1, robotPosition.y() + 1);

        edge = robot.position().fnAdd(robot.orientation().toVector2().fnMultiply(2));
        edge_l = edge.fnAdd(robot.orientation().getLeft().toVector2());
        edge_r = edge.fnAdd(robot.orientation().getRight().toVector2());
        left = robot.position().fnAdd(robot.orientation().toVector2())
                .fnAdd(robot.orientation().getLeft().toVector2().fnMultiply(2));

        edge_rm = robot.position().fnAdd(robot.orientation().getRight().toVector2().fnMultiply(2));
        edge_rf = robot.position().fnAdd(robot.orientation().getRight().toVector2().fnMultiply(2))
                .fnAdd(robot.orientation().toVector2());
        edge_lf = robot.position().fnAdd(robot.orientation().getLeft().toVector2().fnMultiply(2))
                .fnAdd(robot.orientation().toVector2());

        markConfidentDetection(edge);
        markConfidentDetection(edge_l);
        markConfidentDetection(edge_r);
        markConfidentDetection(edge_rm);
        markConfidentDetection(edge_rf);
        markConfidentDetection(edge_lf);
        markConfidentDetection(left);
        return true;
    }

    public boolean markConfidentDetection(GridVector v) {
        if (checkValidExploredRange(v))
            confidentDetectionArea[v.x()][v.y()] = 1;
        return true;
    }

    public boolean markConfidentDetectionDirect(int i, int j) {
        if (checkValidExploredRange(new GridVector(i, j)))
            confidentDetectionArea[i][j] = 1;
        return true;
    }

    public boolean checkRobotVisited(GridVector v) {
        if (map.checkValidPosition(v)) {
            if (robotPosition[v.x()][v.y()] == 1)
                return true;
        }
        return false;
    }

    public int[][] getExplored() {
        return explored;
    }

    public Map getSubjectiveMap() {
        return map;
    }

    public void markExploredEmpty(GridVector v) {
        if (map.checkValidBoundary(v) && (!checkConfidence(v))) {
            explored[v.x()][v.y()] = 1;
        }
    }

    public void markUnreachable(GridVector v) {
        if (map.checkValidBoundary(v) && (!checkConfidence(v))) {
            explored[v.x()][v.y()] = -1;
        }
    }

    private void markExploredObstacle(GridVector v) {
    	
    	int row = v.x();
    	int col = v.y();
    	// Check for out of bounds
        if (map.checkValidBoundary(v) && (!checkConfidence(v))){ 
            if ((!(v.x() >= map.ROW - 3 && v.y() >= map.COL - 3)) && (!(v.x() < 3 && v.y() < 3))){
                explored[v.x()][v.y()] = 2;               
            }
            else
                explored[v.x()][v.y()] = 1;   //goal and start position set as empty
        }
    }

    private void markExploredDirectEmpty(int i, int j) {

        explored[i][j] = 1;
    }

    public int checkExploredState(GridVector v) {
        if (!map.checkValidBoundary(v)) {
            return 2;
        }
        return explored[v.x()][v.y()];

    }

    public void markRobotHistory(GridVector p, Direction d) {
        robotMovementHistory.add(new RobotMovementHistory(p, d));
    }

    public int detectCircle(GridVector p, Direction d) {
        RobotMovementHistory tmp = new RobotMovementHistory(p, d);
        for (int i = 0; i < robotMovementHistory.size(); i++) {
            if (RobotMovementHistory.compare(tmp, robotMovementHistory.get(i))) {
                return i - 1;
            }
        }
        return -1;
    }

    public LinkedList<RobotMovementHistory> getRobotMovementHistory() {
        return robotMovementHistory;
    }

    public boolean checkIfNavigationComplete() {
        boolean complete = true;
        for (int i = 0; i < Map.ROW; i++) {
            for (int j = 0; j < Map.COL; j++) {
                if (explored[i][j] == 0) {
                    complete = false;
                    break;
                }
            }
        }        
        return complete;
    }

    public String exploredAreaToString() {
        String result = "";
        for (int i = -1; i <= Map.ROW; i++) {
            for (int j = -1; j <= Map.COL; j++) {
                if (i == -1 || j == -1 || i == Map.ROW || j == Map.COL) {
                    result += "# ";
                } else {
                    switch (explored[i][j]) {
                    case 0:
                        result += "0 ";
                        break;
                    case 2:
                        result += "x ";
                        break;
                    case -1:
                        result += "? ";
                        break;    
                    default:
                        result += "  ";
                        break;
                    }
                }
            }
            result += "\n";
        }
        return result;
    }

    public String robotVisitedPlaceToString() {
        String result = "";
        for (int i = -1; i <= Map.ROW; i++) {
            for (int j = -1; j <= Map.COL; j++) {
                if (i == -1 || j == -1 || i == Map.ROW || j == Map.COL) {
                    result += "# ";
                } else {
                    switch (robotPosition[i][j]) {
                    case 1:
                        result += ": ";
                        break;

                    default:
                        result += "  ";
                        break;
                    }
                }
            }
            result += "\n";
        }
        return result;
    }

    public String confidenceDetectionAreaToString() {
        String result = "";
        for (int i = -1; i <= Map.ROW; i++) {
            for (int j = -1; j <= Map.COL; j++) {
                if (i == -1 || j == -1 || i == Map.ROW || j == Map.COL) {
                    result += "# ";
                } else {
                    switch (confidentDetectionArea[i][j]) {
                    case 1:
                        result += "+ ";
                        break;

                    default:
                        result += "  ";
                        break;
                    }
                }
            }
            result += "\n";
        }
        return result;
    }

    public Confirm checkAllAroundEmpty(Robot robot) throws InterruptedException, IOException {
        if (robot.checkIfHavingBufferActions()) {
            robot.executeBufferActions(ExploreSolve.getExePeriod());
        }
        Confirm l, r, b, f;
        l = ExploreCal.checkWalkable(robot, Direction.Left, this);
        r = ExploreCal.checkWalkable(robot, Direction.Right, this);
        b = ExploreCal.checkWalkable(robot, Direction.Down,this);
        f = ExploreCal.checkWalkable(robot, Direction.Up, this);

        if (l == Confirm.Yes && r == Confirm.Yes && b == Confirm.Yes && f == Confirm.Yes) {
            return Confirm.Yes;
        }
        if (l == Confirm.Unsure || r == Confirm.Unsure || b == Confirm.Unsure || f == Confirm.Unsure) {
            return Confirm.Unsure;
        } else {
            return Confirm.No;
        }
    }

    public boolean checkIfRight5SquaresEmpty(Robot robot) throws InterruptedException, IOException {
        return checkBackRightConnectingPoint(robot) != WPObstacleState.IsActualObstacle
                && checkFrontRightConnectingPoint(robot) != WPObstacleState.IsActualObstacle
                && ExploreCal.checkWalkable(robot, Direction.Right,this) == Confirm.Yes;
    }

    // take RPI data from Solver , update what I saw
    public Map updateMap(Robot robot, SensorData s) {
        GridVector obstaclePosition;
        GridVector edge, edge_l, edge_r, edge_b, edge_rm;
        int i = 1;
        int leftRange= 5;
        GridVector robotPosition = robot.position();
        markExploredEmpty(robotPosition);
        markExploredDirectEmpty(robotPosition.x(), robotPosition.y() + 1);
        markExploredDirectEmpty(robotPosition.x(), robotPosition.y() - 1);
        markExploredDirectEmpty(robotPosition.x() - 1, robotPosition.y());
        markExploredDirectEmpty(robotPosition.x() + 1, robotPosition.y());
        markExploredDirectEmpty(robotPosition.x() + 1, robotPosition.y() + 1);
        markExploredDirectEmpty(robotPosition.x() - 1, robotPosition.y() - 1);
        markExploredDirectEmpty(robotPosition.x() + 1, robotPosition.y() - 1);
        markExploredDirectEmpty(robotPosition.x() - 1, robotPosition.y() + 1);

        edge = robot.position().fnAdd(robot.orientation().toVector2());
        edge_l = edge.fnAdd(robot.orientation().getLeft().toVector2());
        edge_r = edge.fnAdd(robot.orientation().getRight().toVector2());
        edge_b = robot.position().fnAdd(robot.orientation().getRight().toVector2())
                .fnAdd(robot.orientation().getBehind().toVector2());
        edge_rm = robot.position().fnAdd(robot.orientation().getRight().toVector2());
        
        if (s.front_m != 0) {
            obstaclePosition = edge.fnAdd(robot.orientation().toVector2().fnMultiply(s.front_m));
            if (map.checkValidBoundary(obstaclePosition)) {

                markExploredObstacle(obstaclePosition);
            }
            for (i = 1; i < s.front_m; i++) {
                markExploredEmpty(edge.fnAdd(robot.orientation().toVector2().fnMultiply(i)));
            }

        } else {
            for (i = 1; i <= 2; i++) {
                markExploredEmpty(edge.fnAdd(robot.orientation().toVector2().fnMultiply(i)));
            }
        }

        if (s.front_l != 0) {
            obstaclePosition = edge_l.fnAdd(robot.orientation().toVector2().fnMultiply(s.front_l));
            if (map.checkValidBoundary(obstaclePosition)) {

                markExploredObstacle(obstaclePosition);
            }
            for (i = 1; i < s.front_l; i++) {
                markExploredEmpty(edge_l.fnAdd(robot.orientation().toVector2().fnMultiply(i)));
            }
            markExploredObstacle(edge_l.fnAdd(robot.orientation().toVector2().fnMultiply(i)));
        } else {
            for (i = 1; i <= 2; i++) {
                markExploredEmpty(edge_l.fnAdd(robot.orientation().toVector2().fnMultiply(i)));
            }
        }

        if (s.front_r != 0) {
            obstaclePosition = edge_r.fnAdd(robot.orientation().toVector2().fnMultiply(s.front_r));
            if (map.checkValidBoundary(obstaclePosition)) {

                markExploredObstacle(obstaclePosition);
            }
            for (i = 1; i < s.front_r; i++) {
                markExploredEmpty(edge_r.fnAdd(robot.orientation().toVector2().fnMultiply(i)));
            }

        } else {
            for (i = 1; i <= 2; i++) {
                markExploredEmpty(edge_r.fnAdd(robot.orientation().toVector2().fnMultiply(i)));
            }
        }

        if (s.left != 0) {
            obstaclePosition = edge_l.fnAdd(robot.orientation().getLeft().toVector2().fnMultiply(s.left));
            if (map.checkValidBoundary(obstaclePosition)) {

                markExploredObstacle(obstaclePosition);
            }
            for (i = 1; i < s.left; i++) {
                markExploredEmpty(edge_l.fnAdd(robot.orientation().getLeft().toVector2().fnMultiply(i)));
            }

        } else {
            for (i = 1; i <= leftRange; i++) {
                markExploredEmpty(edge_l.fnAdd(robot.orientation().getLeft().toVector2().fnMultiply(i)));
            }
        }

        if (s.right_f != 0) {
            obstaclePosition = edge_r.fnAdd(robot.orientation().getRight().toVector2().fnMultiply(s.right_f));
            if (map.checkValidBoundary(obstaclePosition)) {

                markExploredObstacle(obstaclePosition);

            }
            for (i = 1; i < s.right_f; i++) {
                markExploredEmpty(edge_r.fnAdd(robot.orientation().getRight().toVector2().fnMultiply(i)));
            }

        } else {
            for (i = 1; i <= 3; i++) {
                markExploredEmpty(edge_r.fnAdd(robot.orientation().getRight().toVector2().fnMultiply(i)));
            }
        }

        /// mark confident explored area
        markConfidentRange(robot);
        // update map with proper obstacles
        map = new Map(explored, true);

        //insertExploredIntoMap();
        Main.getGUI().update(map);
        return map;
    }

    public LinkedList<GridVector> findUnexploredInAscendingDistanceOrder(Robot robot) {
        int i = 1;
        LinkedList<GridVector> total = new LinkedList<GridVector>();
        do {
            total.addAll(IdentifyUnexploredAround(i, robot.position()));
            i++;
        } while (i != 20);
        return total;
    }

    private List<GridVector> IdentifyUnexploredAround(int width, GridVector center) {
        GridVector traverse = center.fnAdd(new GridVector(width, -width));
        List<GridVector> list = new ArrayList<>();

        int i;
        for (i = 0; i < width * 2; i++) {
            if (checkValidExploredRange(traverse)) {

                if (explored[traverse.x()][traverse.y()] == 0) {
                    list.add(new GridVector(traverse.x(), traverse.y()));

                }
            }
            traverse.add(new GridVector(0, 1));
        }

        for (i = 0; i < width * 2; i++) {
            if (checkValidExploredRange(traverse)) {
                if (explored[traverse.x()][traverse.y()] == 0) {
                    list.add(new GridVector(traverse.x(), traverse.y()));

                }
            }
            traverse.add(new GridVector(-1, 0));
        }

        for (i = 0; i < width * 2; i++) {
            if (checkValidExploredRange(traverse)) {
                if (explored[traverse.x()][traverse.y()] == 0) {
                    list.add(new GridVector(traverse.x(), traverse.y()));

                }
            }
            traverse.add(new GridVector(0, -1));
        }

        for (i = 0; i < width * 2; i++) {
            if (checkValidExploredRange(traverse)) {
                if (explored[traverse.x()][traverse.y()] == 0) {
                    list.add(new GridVector(traverse.x(), traverse.y()));

                }
            }
            traverse.add(new GridVector(1, 0));
        }

        return list;
    }

    public boolean markGhostBlock(GridVector center) {
        GridVector up, down, right, left;
        boolean upBlocked = false;
        boolean downBlocked = false;
        boolean rightBlocked = false;
        boolean leftBlocked = false;
        up = center.fnAdd(new GridVector(-1, 0));
        down = center.fnAdd(new GridVector(1, 0));
        right = center.fnAdd(new GridVector(0, 1));
        left = center.fnAdd(new GridVector(0, -1));
        int i;

        for (i = 0; i < 3; i++) {
            if (!map.checkValidBoundary(up) || map.getPoint(up).obstacleState() == WPObstacleState.IsActualObstacle) {
                upBlocked = true;
                break;
            }
            up.add(new GridVector(-1, 0));
        }

        for (i = 0; i < 3; i++) {
            if (!map.checkValidBoundary(down)
                    || map.getPoint(down).obstacleState() == WPObstacleState.IsActualObstacle) {
                downBlocked = true;
                break;
            }
            down.add(new GridVector(1, 0));
        }

        for (i = 0; i < 3; i++) {
            if (!map.checkValidBoundary(right)
                    || map.getPoint(right).obstacleState() == WPObstacleState.IsActualObstacle) {
                rightBlocked = true;
                break;
            }
            right.add(new GridVector(0, 1));
        }

        for (i = 0; i < 3; i++) {
            if (!map.checkValidBoundary(left)
                    || map.getPoint(left).obstacleState() == WPObstacleState.IsActualObstacle) {
                leftBlocked = true;
                break;
            }
            left.add(new GridVector(0, -1));
        }
        if ((upBlocked == true) && (downBlocked == true) && (leftBlocked == true) && (rightBlocked == true)) {
            // System.out.println("Here");
            markUnreachable(center);
            return true;
        } else
            return false;

    }

    public boolean checkValidExploredRange(GridVector v) {
        return v.x() >= 0 && v.x() < (map.ROW) && v.y() >= 0 && v.y() < (map.COL);
    }

    public boolean validate(Robot robot, RobotMovement action) throws InterruptedException, IOException {
        switch (action) {
        case MoveForward:
            if (ExploreCal.checkWalkable(robot, Direction.Up, this) == Confirm.No) {
                return false;
            }
            break;
        default:
            return true;
        }
        return true;
    }

    public boolean checkValidBoundary(GridVector v){
        
        return map.checkValidBoundary(v);
    }
    
    public WPObstacleState getObstacleState(GridVector v){
        
        return map.getPoint(v).obstacleState();
    }
    

    public ArrayList<GridVector> getUnExplored() {
        ArrayList<GridVector> unexplored = new ArrayList<GridVector>();
        for (int row = 0; row < Map.ROW; ++row) {
            for (int col = 0; col < Map.COL; ++col) {
                if (explored[row][col] == 0) {
                    if (unexplored.size() > 0 && unexplored.get(unexplored.size() - 1).y() == row) {
                        unexplored.remove(unexplored.size() - 1);
                    }
                    GridVector pair = new GridVector(row, col);
                    System.out.println(pair);
                    unexplored.add(pair);

                }
            }
        }
        int count = 0;
        while (unexplored.size() - 2 >= count) {
            if (unexplored.get(count + 1).x() == unexplored.get(count).x()) {
                if (unexplored.get(count + 1).y() - unexplored.get(count).y() <= 2) {
                    unexplored.remove(count);
                    --count;
                }
            }
            ++count;
        }

        return unexplored;
    }

    public boolean checkRightFrontBack(Robot robot) {
        GridVector right_up, right_down, right_middle;

        /*
         * front_m =
         * robot.position().fnAdd(robot.orientation().toVector2().fnMultiply(2))
         * ; front_l = front_m.fnAdd(robot.orientation().getLeft().toVector2());
         * front_r = front_m.fnAdd(robot.orientation().getRight().toVector2());
         */
        right_up = robot.position().fnAdd(robot.orientation().toVector2())
                .fnAdd(robot.orientation().getRight().toVector2().fnMultiply(2));
        right_down = robot.position().fnAdd(robot.orientation().getBehind().toVector2())
                .fnAdd(robot.orientation().getRight().toVector2().fnMultiply(2));
        right_middle = robot.position().fnAdd(robot.orientation().getRight().toVector2().fnMultiply(2));

        if (!map.checkValidBoundary(right_up)
                || map.getPoint(right_up).obstacleState() == WPObstacleState.IsActualObstacle)
            if (!map.checkValidBoundary(right_down)
                    || map.getPoint(right_down).obstacleState() == WPObstacleState.IsActualObstacle) {
                if (map.checkValidBoundary(right_middle)
                        && map.getPoint(right_middle).obstacleState() != WPObstacleState.IsActualObstacle)
                    return true;
            }

        return false;

    }

    public WPObstacleState checkFrontRightConnectingPoint(Robot robot) {

        if (!map.checkValidBoundary(robot.position().fnAdd(robot.orientation().toVector2().fnMultiply(2))
                .fnAdd(robot.orientation().getRight().toVector2().fnMultiply(2))))
            return WPObstacleState.IsActualObstacle;

        return map.getPoint(robot.position().fnAdd(robot.orientation().toVector2().fnMultiply(2))
                .fnAdd(robot.orientation().getRight().toVector2().fnMultiply(2))).obstacleState();
    }

    public WPObstacleState checkBackRightConnectingPoint(Robot robot) {
        if (!map.checkValidBoundary(robot.position().fnAdd(robot.orientation().toVector2().fnMultiply(-2))
                .fnAdd(robot.orientation().getRight().toVector2().fnMultiply(2))))
            return WPObstacleState.IsActualObstacle;

        return map.getPoint(robot.position().fnAdd(robot.orientation().toVector2().fnMultiply(-2))
                .fnAdd(robot.orientation().getRight().toVector2().fnMultiply(2))).obstacleState();
    }

    public boolean checkLeftObstacles(Robot _robot) {
        GridVector left_f, left_m, left_b;
        int m = 0, f = 0, b = 0;
        left_m = _robot.position().fnAdd(_robot.orientation().getLeft().toVector2().fnMultiply(2));
        left_f = left_m.fnAdd(_robot.orientation().toVector2());
        left_b = left_m.fnAdd(_robot.orientation().getBehind().toVector2());

        if (!map.checkValidBoundary(left_m)
                || checkExploredState(left_m)==2) {
            m = 1;
        }

        if (!map.checkValidBoundary(left_f)
                || checkExploredState(left_f)==2) {
            f = 1;
        }
        if (!map.checkValidBoundary(left_b)
                || checkExploredState(left_b)==2) {
            b = 1;
        }

        return (m + f + b) == 3;
    }

    public static String exploredArrayToString(int[][] explored){
    	String msg= "";
    	for(int i=0; i<Map.ROW;i++){
    		for(int j=0; j<Map.COL;j++){
    			if(i==15 && j==20){
    				msg += explored[i][j];
    			}
    			else{
    				msg = msg + explored[i][j] + ",";
    			}
            }
    		//msg +="\n";
    	}
    	return msg;
    }  

    public int getExploredState(GridVector v) {
        if (map.checkValidBoundary(v))
            return explored[v.x()][v.y()];
        else
            return -2;
    }

 
}
