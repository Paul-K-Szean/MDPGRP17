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
import Exploration.MapRef;

public class MessageCompiler {

    private static final String MOVE_FORWARD = "i";
    private static final String MOVE_BACKWARD = "k";
    private static final String ROTATE_LEFT = "j";
    private static final String ROTATE_RIGHT = "l";

    private static final String TRAILER = "|";

    private static final String DESC_SEPARATOR = "g";

    private static final String DECIMAL_FORMAT = "###.###";
    
    static String compileArbitrary(String action) {
        return action+ TRAILER;
    }

    static String compileActions(List<RobotMovement> actions, String mode) {
        String result = "";
        //result += !mode.isEmpty() ? mode + TRAILER : "";
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
                        result += lastAction;
                    } else {
                        count++;
                    }
                } else {
                    boolean isRotating = lastAction.equals(ROTATE_LEFT) || lastAction.equals(ROTATE_RIGHT);
                    result += (isRotating ? "" : count) + nextActionStr;
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
        //System.out.println("Sending out: " + result);        
        return result;
    }

    static String compileMap(Map map, int[][] explored) {
        String strResult = MapDescriptor.stringify(map, explored);
        String[] hexDesc = MapDescriptor.toHex(strResult);
        return hexDesc[0] + DESC_SEPARATOR + hexDesc[1];
    }
    
    static String giveMap(int[][] explored){
    	return MapRef.exploredArrayToString(explored);    	
    }

	static String compileShortestPathActions(List<RobotMovement> actions, String mode) {
		String result = "";
        //result += !mode.isEmpty() ? mode + TRAILER : "";
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
                        result += lastAction;
                    } else {
                        count++;
                    }
                } else {
                    boolean isRotating = lastAction.equals(ROTATE_LEFT) || lastAction.equals(ROTATE_RIGHT);
                    result += (isRotating ? "" : count) + nextActionStr;
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

        result += (isRotating ? "" : count) + TRAILER;
        //result += TRAILER;
        //System.out.println("Sending out: " + result);        
        return result;
	}

}