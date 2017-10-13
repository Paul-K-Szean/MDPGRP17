package communication;

import java.util.LinkedList;
import java.util.List;
import common.GridVector;
import map.Map;
import robot.RobotMovement;
import Exploration.SensorCalibration;

public interface ITranslate{

    String getInputBuffer();
    
    void connect(Runnable callback);

    void listen(Runnable handler);

    void sendInfoToAndroid(Map map, int[][] explored, LinkedList<RobotMovement> actions);
    
    void sendSensingRequest();

    void sendMoveCommand(List<RobotMovement> actions, String mode);
    
    void sendSmoothMoveCommand(List<GridVector> path);
    
    void sendCalibrationCommand(SensorCalibration calType);
    
    void sendExplorationEndMarker();

	void sendAndroidMoveCommand(List<RobotMovement> actions, String mode);
    
}
