package communication;

import java.util.LinkedList;
import java.util.List;
import common.GridVector;
import map.Map;
import robot.RobotMovement;

public interface ITranslate{

    String getInputBuffer();
    
    void connect(Runnable callback);

    void listen(Runnable handler);

    void sendInfoToAndroid(Map map, int[][] explored, LinkedList<RobotMovement> actions);

    void sendMoveCommand(List<RobotMovement> actions, String mode);
    
    void sendShortestPathMoveCommand(List<RobotMovement> actions, String mode);
   
    void sendExplorationEndMarker();

	void sendAndroidMoveCommand(List<RobotMovement> actions, String mode);

	void sendAndroidObsCommand(int[][] explored);

//	void sendExplorationEndMarkerToAndroid();
    
}
