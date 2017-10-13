package robot;

import java.io.IOException;

import java.util.Arrays;
import java.util.LinkedList;

import common.Direction;
import common.GridVector;
import communication.MessageTranslator;
import Exploration.MovementFormulator;
import Exploration.ExploreSolve;
import Exploration.MapRef;
import Combine.Main;
import map.Map;

public class Robot {

    private GridVector pos;
    private Direction orient;

    private static volatile int calibrateCount = 0;
    private static volatile boolean actionCompleted = false;

    private static volatile boolean robotVisitedBefore = false;
    
    private static LinkedList<RobotMovement> bufferedActions = new LinkedList<>();
    private MapRef mapViewer;
    private MovementFormulator actionFormulator;
    private long exeStartTime = System.currentTimeMillis();
    private long exeEndTime ;
    public Robot() {
        this(new GridVector(1, 1), Direction.Right);
    }

    public Robot(GridVector position, Direction direction) {
        pos = position;
        orient = direction;
    }

    public Robot(GridVector position, Direction direction, MapRef mv , MovementFormulator ac) {
        pos = position;
        orient = direction;
        mapViewer = mv;
        actionFormulator = ac;
    }

    public boolean checkIfRobotVisitedBefore(){
        
        return robotVisitedBefore;
    }
    public GridVector position() {
        return pos;
    }

    public Direction orientation() {
        return orient;
    }

    public void position(GridVector position) {
        pos = position;
    }

    public void orientation(Direction direction) {
        orient = direction;
    }

    public void execute(RobotMovement action) {
        GridVector dirVector = orient.toVector2();
        switch (action) {
            case MoveForward:
                // RPI call
                pos.add(dirVector);
                break;
            case MoveBackward:
                // RPI call
                dirVector.multiply(-1);
                pos.add(dirVector);
                break;
            case RotateLeft:
                // RPI call
                orient = orient.getLeft();
                break;
            case RotateRight:
                // RPI call
                orient = orient.getRight();
                break;           
        }
    }

    public boolean bufferAction(RobotMovement action) {
        return bufferedActions.add(action);
    }
    
    public int checkBufferActionSize() {
        return bufferedActions.size();
    }
    
    public void cleanBufferedActions(){
    		bufferedActions.clear();
    		
    }
    
    public static void actionCompletedCallBack() {
        actionCompleted = true;

    }
    
    public void executeBufferActions(int sleepPeriod) throws IOException {
        try {            
            ExploreSolve.setPermitTerminationState(false);    
            if (!Main.isSimulating()) {
            		exeEndTime = System.currentTimeMillis();
            		System.out.println("Computational time for next movement"+ (exeStartTime- exeEndTime) + "ms");
                
            	Main.getRpi().sendMoveCommand(bufferedActions, MessageTranslator.MODE_0);
            	Main.getRpi().sendAndroidMoveCommand(bufferedActions, MessageTranslator.MODE_0);
                
                while (!actionCompleted) {
                }
                exeStartTime = System.currentTimeMillis();
                Map map = mapViewer.getSubjectiveMap();
                int[][] explored = mapViewer.getExplored();

                // send info to android
                Main.getRpi().sendInfoToAndroid(map, explored, bufferedActions);
                actionCompleted = false;
                //increment calibrationCounter
                calibrateCount += bufferedActions.size();
            }
            for (RobotMovement action : bufferedActions) {          
                execute(action);
                if(mapViewer.checkRobotVisited(pos)){
                    robotVisitedBefore= true;                    
                }
                else{
                    robotVisitedBefore = false;
                }
                mapViewer.markRobotVisited(pos);
                Main.getGUI().update(this);
                
                if (Main.isSimulating()) {
                    Thread.sleep(sleepPeriod);
                } 
            }
            bufferedActions.clear();
            ExploreSolve.setPermitTerminationState(true);   
        } catch (InterruptedException e) {
            System.out.println("Robot execution interrupted");
        }
    }

    public boolean checkIfHavingBufferActions() {
        return !bufferedActions.isEmpty();
    }

    public boolean checkIfCalibrationCounterReached() {
        return (calibrateCount >= 6);
    }

    public boolean clearCalibrationCounter() {
        calibrateCount = 0;
        return true;
    }
    
    public  LinkedList<RobotMovement> getBufferedActions(){
        return bufferedActions;
    }

}

