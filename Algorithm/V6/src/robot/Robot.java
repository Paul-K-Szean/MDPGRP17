package robot;

import java.io.IOException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

import common.Direction;
import common.GridVector;
import communication.MessageTranslator;
import Exploration.MovementFormulator;
import Exploration.ExploreSolve;
import Exploration.GoalFormulator;
import Exploration.MapRef;
import Combine.Main;
import map.Map;

public class Robot {

    private GridVector pos;
    private Direction orient;

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
    private int count =0;
    private boolean reachedFinal = false;
    private boolean ran = true;
    public void executeBufferActions(int sleepPeriod) throws IOException {
        try {         	
            ExploreSolve.setPermitTerminationState(false);    
            if (!Main.isSimulating()) {
        		exeEndTime = System.currentTimeMillis();
        		//System.out.println("Computational time for next movement"+ (exeStartTime- exeEndTime) + "ms");
        		//Scanner s=new Scanner(System.in);
        		//System.out.println("Waiting Enter");
            	//s.nextLine();            	
        		Main.getRpi().sendMoveCommand(bufferedActions, MessageTranslator.MODE_0); 
        		Thread.sleep(150);
            	Main.getRpi().sendAndroidMoveCommand(bufferedActions, MessageTranslator.MODE_0);
            	Thread.sleep(150);
                
                while (!actionCompleted) {
                }
                exeStartTime = System.currentTimeMillis();
                Map map = mapViewer.getSubjectiveMap();
                int[][] explored = mapViewer.getExplored();
                GoalFormulator gF = new GoalFormulator(mapViewer);
                //System.out.println(mapViewer.exploredArrayToString(explored)); 
                count++;
                if(count==5){
                	Main.getRpi().sendAndroidObsCommand(explored);
                	Thread.sleep(380);
                	count=0;
                }   
                if(gF.checkIfReachFinalGoal(pos)){
                	System.out.println("REACHED FINAL");
                	reachedFinal = true;
                }
                // send info to android
                if(reachedFinal && gF.checkIfReachStartZone(pos) && ran){
                	System.out.println("Sending MDF");
                	Main.getRpi().sendInfoToAndroid(map, explored, bufferedActions); 
                	Thread.sleep(350);
                	System.out.println("Sending Obstacle");
                	Main.getRpi().sendAndroidObsCommand(explored);
                	Thread.sleep(350);
                	ran = false;
                }
                actionCompleted = false;
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

    public  LinkedList<RobotMovement> getBufferedActions(){
        return bufferedActions;
    }

}

