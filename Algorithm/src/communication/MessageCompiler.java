package communication;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.List;
import common.GridVector;
import map.MapDescriptor;
import map.Map;
import robot.Robot;
import robot.RobotMovement;
import Exploration.SensorCalibration;

public class MessageCompiler {

    private static final String MOVE_FORWARD = "i";
    private static final String MOVE_BACKWARD = "k";
    private static final String ROTATE_LEFT = "j";
    private static final String ROTATE_RIGHT = "l";

    private static final String CAL_FRONT_LR = "y";
    private static final String CAL_FRONT_ML = "y";
    private static final String CAL_FRONT_MR = "y";
    private static final String CAL_RIGHT = "y";
    private static final String CAL_LEFT = "y";
    private static final String CAL_EMERGENCY = "y";
    
    
    private static final String CAL_LEFT_SPECIAL = "y";
    private static final String SENSING_REQUEST = "s";
    private static final String TRAILER = "|";

    private static final String DESC_SEPARATOR = "g";

    private static final String DECIMAL_FORMAT = "###.###";

    private static String _roundToString(double target) {
        return new DecimalFormat(DECIMAL_FORMAT).format(target);
    }
    
    static String compileArbitrary(String action) {
        return action+ TRAILER;
    }

    static String compileActions(List<RobotMovement> actions, String mode) {
        String result = "";
        result += !mode.isEmpty() ? mode + TRAILER : "";
        int count = 1;
        String lastAction = "";
        for (RobotMovement action : actions) {
            String nextActionStr;
            switch (action) {
                case MoveForward:
                    nextActionStr = MOVE_FORWARD;
                    break;
                case MoveBackward:
                    nextActionStr = MOVE_BACKWARD;
                    break;
                case RotateLeft:
                    nextActionStr = ROTATE_LEFT;
                    break;
                case RotateRight:
                    nextActionStr = ROTATE_RIGHT;
                    break;
                default:
                    nextActionStr = " ";
                    break;
            }
            if (result.length() != 0) {
                if (lastAction.equals(nextActionStr)) {
                    boolean isRotating = lastAction.equals(ROTATE_LEFT) || lastAction.equals(ROTATE_RIGHT);
                    if (isRotating) {
                        result += TRAILER + lastAction;
                    } else {
                        count++;
                    }
                } else {
                    boolean isRotating = lastAction.equals(ROTATE_LEFT) || lastAction.equals(ROTATE_RIGHT);
                    result += (isRotating ? "" : count) + TRAILER + nextActionStr;
                    count = 1;
                }
            } else {
                result += nextActionStr;
            }
            lastAction = nextActionStr;
        }
        boolean isRotating;
        if (result.length() != 1) {
            isRotating = lastAction.equals(ROTATE_LEFT) || lastAction.equals(ROTATE_RIGHT);
        } else {
            isRotating = result.equals(ROTATE_LEFT) || result.equals(ROTATE_RIGHT);
        }

        //result += (isRotating ? "" : count) + TRAILER;
        result += TRAILER;
        System.out.println("Sending out: " + result);
        
        return result;
    }

    static String compileSmoothActions(List<GridVector> smoothPath, String mode) {
        String result = "";
        result += !mode.isEmpty() ? mode + TRAILER : "";
        double orientation;
        switch (new Robot().orientation()) {
            case Up:
                orientation = 90;
                break;
            case Down:
                orientation = 270;
                break;
            case Left:
                orientation = 180;
                break;
            case Right:
            default:
                orientation = 0;
                break;
        }
        for (int i = 0; i < smoothPath.size() - 1; i++) {
            // calculate vector difference
            GridVector posDiff = smoothPath.get(i + 1).fnAdd(smoothPath.get(i).fnMultiply(-1));
            
            // calculate rotation
            double alpha = Math.toDegrees(
                    Math.atan(((double) posDiff.x()) / ((double) posDiff.y()))
            );
            double angle = alpha + (posDiff.y() < 0 ? 90 : 0);
            double rotation = Math.abs(angle - orientation);
            String rotateDirection = angle - orientation > 0 ? "r" : "l";
            String roundedAngle = _roundToString(rotation);
            String rotationStr;
            if (!"0".equals(roundedAngle)) {
                rotationStr = rotateDirection + _roundToString(rotation * 0.99f) + TRAILER;
            } else {
                rotationStr = "";
            }
            
            // calculate distance to travel
            double distance = Math.sqrt(Math.pow(posDiff.x(), 2) + Math.pow(posDiff.y(), 2));
            String distanceStr = "f" + _roundToString(distance * 0.99f) + TRAILER;
            
            // wrapping up
            result += rotationStr + distanceStr;
            orientation = angle;
        }
        return result;
    }

    static String compileMap(Map map, int[][] explored) {
        String strResult = MapDescriptor.stringify(map, explored);
        String[] hexDesc = MapDescriptor.toHex(strResult);
        return hexDesc[0] + DESC_SEPARATOR + hexDesc[1];
    }

    static String compileCalibration(SensorCalibration calType) {
        switch (calType) {
            case Right:
                return CAL_RIGHT + TRAILER;
            case Front_LR:
                return CAL_FRONT_LR + TRAILER;
            case Front_ML:
                return CAL_FRONT_ML + TRAILER;
            case Front_MR:
                return CAL_FRONT_MR + TRAILER;
            case LeftSpecial:
                return CAL_LEFT_SPECIAL + TRAILER;
            case Left:
                return CAL_LEFT + TRAILER;
            case Emergency:
                return CAL_EMERGENCY + TRAILER;
            default:
                return "";
        }
    }

    static String compileSensingRequest() {
        return SENSING_REQUEST+ TRAILER;
    }
}