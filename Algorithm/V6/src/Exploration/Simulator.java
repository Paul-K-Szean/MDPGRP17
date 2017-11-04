package Exploration;

import java.util.ArrayList;
import java.util.List;

import common.Direction;

import map.Map;
import robot.Robot;
import common.GridVector;
import map.WPObstacleState;
import map.Waypoint;

public class Simulator {

    private Map objective_map;
    private SensorData s;

    public Simulator(Map map) {
        objective_map = map;
        s = new SensorData();
    }

    public SensorData getSensingData(Robot robot) {
        GridVector edge, edge_l, edge_r;
        
        edge = robot.position().fnAdd(robot.orientation().toVector2());
        s.front_m = detectMiddle(edge, robot.orientation());
        edge_l = edge.fnAdd(robot.orientation().getLeft().toVector2());
        s.front_l = detectMiddle(edge_l, robot.orientation());
        edge_r = edge.fnAdd(robot.orientation().getRight().toVector2());
        s.front_r = detectMiddle(edge_r, robot.orientation());
        s.left = detectLong(edge_l, robot.orientation().getLeft());
        s.right_f = detectMiddle(edge_r, robot.orientation().getRight());        
        //edge_rm = robot.position().fnAdd(robot.orientation().getRight().toVector2());
        //s.right_m = detectMiddle(edge_rm, robot.orientation().getRight());
        //System.out.println("s.right " + s.right);
        return s;

    }
    
    private int detectMiddle(GridVector position, Direction dir) {
        GridVector tmp = new GridVector(position.x(), position.y());
        GridVector unit;
        unit = dir.toVector2();

        tmp.add(unit);
        //seeing range is not relevant to virtual obstacle
        if (!objective_map.checkValidBoundary(tmp) || objective_map.getPoint(tmp).obstacleState() == WPObstacleState.IsActualObstacle) {
            return 1;
        }
        tmp.add(unit);

        if (!objective_map.checkValidBoundary(tmp) || objective_map.getPoint(tmp).obstacleState() == WPObstacleState.IsActualObstacle) {
            return 2;
        }
        tmp.add(unit);

        if (!objective_map.checkValidBoundary(tmp) || objective_map.getPoint(tmp).obstacleState() == WPObstacleState.IsActualObstacle) {
            return 3;
        }
        return 0; // no obstacle in front
    }
    
    private int detectLong(GridVector position, Direction dir) {
        GridVector tmp = new GridVector(position.x(), position.y());
        GridVector unit;
        unit = dir.toVector2();
        tmp.add(unit);
        //seeing range is not relevant to virtual obstacle
        if (!objective_map.checkValidBoundary(tmp) || objective_map.getPoint(tmp).obstacleState() == WPObstacleState.IsActualObstacle) {
            return 1;
        }
        tmp.add(unit);

        if (!objective_map.checkValidBoundary(tmp) || objective_map.getPoint(tmp).obstacleState() == WPObstacleState.IsActualObstacle) {
            return 2;
        }
        tmp.add(unit);

        if (!objective_map.checkValidBoundary(tmp) || objective_map.getPoint(tmp).obstacleState() == WPObstacleState.IsActualObstacle) {
            return 3;
        }
        
        tmp.add(unit);
        if (!objective_map.checkValidBoundary(tmp) || objective_map.getPoint(tmp).obstacleState() == WPObstacleState.IsActualObstacle) {
            return 4;
        }
        
        tmp.add(unit);
        if (!objective_map.checkValidBoundary(tmp) || objective_map.getPoint(tmp).obstacleState() == WPObstacleState.IsActualObstacle) {
            return 5;
        }
        
        return 0; // no obstacle in front
    }
    
    private static List<GridVector> _genBlockers(int[][] obstacleMap) {
        List<GridVector> blockers = new ArrayList<>();
        for (int i = 0; i < obstacleMap.length; i++) {
            for (int j = 0; j < obstacleMap[0].length; j++) {
                if (obstacleMap[i][j] == 1) {
                    blockers.add(new GridVector(i, j));
                }
            }
        }
        return blockers;
    }

    public void addObstacle(int[][] obstacleMap) {
        objective_map.addObstacle(_genBlockers(obstacleMap));        
    }

}
