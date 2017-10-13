package robot;

import java.util.LinkedList;
import java.util.List;
import common.Direction;
import common.GridVector;

public enum RobotMovement {
    MoveForward, MoveBackward, RotateLeft, RotateRight;
    
    public static LinkedList<RobotMovement> fromPath(Robot robot, List<GridVector> path) {
        LinkedList<RobotMovement> result = new LinkedList<>();
        
        GridVector curPos = robot.position();
        Robot tempRobot = new Robot(
            new GridVector(robot.position().x(), robot.position().y()),
            robot.orientation()
        );
        for (GridVector curPathPos : path) {
            GridVector diff = curPathPos.fnAdd(curPos.fnMultiply(-1));
            for (Direction dir : Direction.values()) {
                // check next pos direction
                if (diff.equals(dir.toVector2())) {
                    // check orientation
                    if (diff.equals(tempRobot.orientation().getLeft().toVector2())) {
                        tempRobot.execute(RotateLeft);
                        result.add(RotateLeft);
                    } else if (diff.equals(tempRobot.orientation().getRight().toVector2())) {
                        tempRobot.execute(RotateRight);
                        result.add(RotateRight);
                    } else if (diff.equals(tempRobot.orientation().getBehind().toVector2())) {
                        tempRobot.execute(RotateLeft);
                        tempRobot.execute(RotateLeft);
                        result.add(RotateLeft);
                        result.add(RotateLeft);
                    }
                    tempRobot.execute(MoveForward);
                    result.add(MoveForward);
                }
            }
            curPos = curPathPos;
        }
        
        return result;        
    }
}
