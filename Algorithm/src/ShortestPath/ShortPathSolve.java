package ShortestPath;

import java.util.Collections;
import java.util.HashMap;
import common.Direction;
import map.Map;
import common.GridVector;
import robot.Robot;
import map.WPObstacleState;
import map.Waypoint;

public class ShortPathSolve {

    private ShortPathResult _solve(Map map, Robot robot, GridVector goalPos, ShortPathSolveType solveType) {

        ShortPathResult result = new ShortPathResult();

        System.out.println("Solving shortest path:");

        // save points in map in a lookup hashtable
        HashMap<String, ShortPathPoint> openedPoints = new HashMap();
        HashMap<String, ShortPathPoint> closedPoints = new HashMap();

        // record robot position as cur point
        ShortPathPoint curPoint = new ShortPathPoint(new Waypoint(robot.position()));

        // check if cur point is goal
        if (curPoint.position().equals(goalPos)) {
            System.out.println("Robot already at goal.");
            return result;
        }

        // loop until a point next tp goal is found
        boolean isFirstCur = true;

        while (ShortPathCal.getMDistance(curPoint.position(), goalPos) != 1) {

            // close point
            openedPoints.remove(curPoint.position().toString());
            closedPoints.put(curPoint.position().toString(), curPoint);

            // detect adj points
            GridVector curPos = curPoint.position();
            for (Direction dir : Direction.values()) {
                // get adj point
                GridVector adjPos = curPos.fnAdd(dir.toVector2());
                if (map.checkValidPosition(adjPos)) {
                    
                    // gval
                    int baseGval;
                    int deltaGval;
                    Direction curDirection;
                    if (isFirstCur) {
                        baseGval = 0;
                        deltaGval = 1;
                    } else {
                        baseGval = curPoint.gval();
                        curDirection = curPoint.parentDir().getBehind();
                        switch (solveType) {
                            case Normal:
                            default:
                                deltaGval = ShortPathCal.getMoveCost(curDirection, dir);
                                break;
                            case Smooth:
                                deltaGval = ShortPathCal.getSmoothMoveCost(curDirection, dir);
                                break;

                        }
                    }
                    
                    // hval
                    int hval = ShortPathCal.getMDistance(adjPos, goalPos);
                    switch (solveType) {
                        case Safe:
                            hval += ShortPathCal.getSafetyBenefit(map, adjPos);
                    }
                    
                    ShortPathPoint adjPoint = new ShortPathPoint(
                            map.getPoint(adjPos),
                            hval,
                            baseGval + deltaGval,
                            dir.getBehind()
                    );

                    // check if adj is closed or already opened
                    String adjKey = adjPoint.position().toString();
                    if (!closedPoints.containsKey(adjKey)) {
                        if (openedPoints.containsKey(adjKey)) {
                            // point already in opened, update if possible
                            ShortPathPoint oldPoint = openedPoints.get(adjKey);
                            if (adjPoint.fval() < oldPoint.fval()) {
                                openedPoints.replace(adjKey, adjPoint);
                            }
                        } else {
                            // check if adj is walkable
                            if (adjPoint.obstacleState() == WPObstacleState.IsWalkable) {
                                // if yes then save new point info
                                openedPoints.put(adjKey, adjPoint);
                            }
                        }
                    }
                }
            }

            // check all open points for potential next cur point
            if (!openedPoints.isEmpty()) {
                // loop through map info to find lowest fval point
                ShortPathPoint lowestFvalPoint = new ShortPathPoint();
                for (String key : openedPoints.keySet()) {
                    ShortPathPoint cur = openedPoints.get(key);
                    if (cur.fval() < lowestFvalPoint.fval()) {
                        lowestFvalPoint = cur;
                    }
                }

                // set this as cur
                curPoint = lowestFvalPoint;
            } else {
                // path to goal is blocked
                System.out.println("No possible path to goal found.");
                return result;
            }
            if (isFirstCur) {
                isFirstCur = false;
            }
        }

        // path has been found
        result.shortestPath.add(goalPos);
        do {
            // save current point to result
            result.shortestPath.add(curPoint.position());

            // trace back the parent
            GridVector parentDirection = curPoint.parentDir().toVector2();
            GridVector parentPos = curPoint.position().fnAdd(parentDirection);

            // set cur to parent
            curPoint = closedPoints.get(parentPos.toString());
        } while (curPoint != null && ShortPathCal.getMDistance(curPoint.position(), robot.position()) != 0);
        Collections.reverse(result.shortestPath);

        // add opened & closed to result
        openedPoints.forEach((pointKey, point) -> {
            result.openedPoints.add(point.position());
        });
        closedPoints.forEach((pointKey, point) -> {
            result.closedPoints.add(point.position());
        });

        return result;

    }

    public ShortPathResult solve(Map map, Robot robot, GridVector goalPos) {
        return _solve(map, robot, goalPos, ShortPathSolveType.Normal);
    }

    public ShortPathResult solve(Map map, Robot robot) {
        return _solve(map, robot, map.GOAL_POS, ShortPathSolveType.Normal);
    }

    public ShortPathResult solve(Map map, Robot robot, ShortPathSolveType solveType) {
        return _solve(map, robot, map.GOAL_POS, solveType);
    }

    public ShortPathResult solve(Map map, Robot robot,  GridVector goalPos, ShortPathSolveType solveType) {
        return _solve(map, robot, goalPos, solveType);
    }
}

