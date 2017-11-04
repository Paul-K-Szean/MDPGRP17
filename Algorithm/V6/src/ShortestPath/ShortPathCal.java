package ShortestPath;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import common.Direction;
import robot.Robot;
import common.GridVector;
import map.Map;
import map.WPObstacleState;
import map.WPSpecialState;
import robot.RobotMovement;

public class ShortPathCal {

    public static float calDistance(List<GridVector> path) {
        float result = 0;
        GridVector prev = null;
        for (GridVector cur : path) {
            if (prev != null) {
                GridVector diff = cur.fnAdd(prev.fnMultiply(-1));
                result += Math.sqrt(Math.pow(diff.x(), 2) + Math.pow(diff.y(), 2));
            }
            prev = cur;
        }
        return result;
    }

    public static int countTurn(List<GridVector> path) {
        LinkedList<RobotMovement> actions = RobotMovement.fromPath(new Robot(), path);
        return (int) actions.stream().filter(a -> a.equals(RobotMovement.RotateLeft) || a.equals(RobotMovement.RotateRight)).count();
    }

    public static int countTurnSmooth(List<GridVector> smoothPath) {
        return smoothPath.size() - 1;
    }

    public static int getMDistance(GridVector a, GridVector b) {
        return Math.abs(a.x() - b.x())+ Math.abs(a.y() - b.y());
    }

    public static int getMoveCost(Direction ori, Direction dir) {
        if (dir == ori) {
            return 1;
        } else if (dir == ori.getBehind()) {
            return 3;
        } else {
            return 2;
        }
    }

    public static int getSmoothMoveCost(Direction ori, Direction dir) {
        if (dir == ori) {
            return 1;
        } else if (dir == ori.getBehind()) {
            return 3;
        } else {
            return 2;
        }
    }

    public static int getSafetyBenefit(Map map, GridVector curPos) {
        int result = 0;
        for (Direction dir : Direction.values()) {
            GridVector adjPos = curPos.fnAdd(dir.toVector2());
            if (map.checkValidPosition(adjPos)) {
                if (!map.getPoint(adjPos).obstacleState()
                        .equals(WPObstacleState.IsWalkable)) {
                    result -= 1;
                }
            }
        }
        return result;
    }

}
