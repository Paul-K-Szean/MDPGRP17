package communication;

import java.io.IOException;
import java.util.LinkedList;

import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import common.GridVector;
import map.Map;
import robot.RobotMovement;

public class MessageTranslator implements ITranslate {

    private static final String TO_ARDUINO = "b";
    private static final String TO_ANDROID = "a";
    private static final String FROM_ALGO = "c";
    
    private static final String EXPLORATION_END = "q";

    private static final String MSG_SEPARATOR = "_";

    private static final int PROBING_PERIOD = 20;
    
    public static final String MODE_0 = "";
    public static final String MODE_1 = "m1";
    public static final String MODE_2 = "m2";

    private SocketComm socketCommu;
    private String inputBuf;

    public MessageTranslator() throws IOException {
        inputBuf = "";
    }

    @Override
    public void connect(Runnable callback) {
        socketCommu = new SocketComm(callback);
    }

    @Override
    public String getInputBuffer() {
        return inputBuf;
    }
    
    // Arduino
    @Override
    public void sendMoveCommand(List<RobotMovement> actions, String mode) {
        try {        	
            String message = TO_ARDUINO + FROM_ALGO + MessageCompiler.compileActions(actions, mode);
            //System.out.println("message(ARDUINO MOVE) = " + message);
            socketCommu.echo(message);
        } catch (IOException ex) {
            Logger.getLogger(MessageTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

	@Override
	public void sendShortestPathMoveCommand(List<RobotMovement> actions, String mode) {
		try {        	
            String message = TO_ARDUINO + FROM_ALGO + "m" +MessageCompiler.compileShortestPathActions(actions, mode);
            //System.out.println("message(ARDUINO MOVE) = " + message);
            socketCommu.echo(message);
        } catch (IOException ex) {
            Logger.getLogger(MessageTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
    @Override
    public void sendExplorationEndMarker() {
        try {
            String message = TO_ARDUINO + FROM_ALGO + MessageCompiler.compileArbitrary(EXPLORATION_END);
            System.out.println("message (ARDUINO EXPLORATION END MARKER) = " + message);
            socketCommu.echo(message);            
        } catch (IOException ex) {
            Logger.getLogger(MessageTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Android
    @Override
    public void sendInfoToAndroid(Map map, int[][] explored, LinkedList<RobotMovement> actions) {
        try {
            String message = TO_ANDROID + FROM_ALGO + "m" +MessageCompiler.compileMap(map, explored);
            //System.out.println("message(MapDescriptor) = " + message);
            socketCommu.echo(message);
        } catch (IOException ex) {
            Logger.getLogger(MessageTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void sendAndroidMoveCommand(List<RobotMovement> actions, String mode) {
        try {        	
            String message = TO_ANDROID + FROM_ALGO + MessageCompiler.compileActions(actions, mode);
            //System.out.println("message(ANDROID MOVE) = " + message);
            socketCommu.echo(message);
        } catch (IOException ex) {
            Logger.getLogger(MessageTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void sendAndroidObsCommand(int[][] explored) {
    	try {
            String message1 = TO_ANDROID + FROM_ALGO + "EXPLORED," + MessageCompiler.giveMap(explored);
            //System.out.println("message (ANDROID OBS) = " + message1);
            socketCommu.echo(message1);
        } catch (IOException ex) {
            Logger.getLogger(MessageTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    private String _readAsString() {
        try {
            return socketCommu.read();
        } catch (IOException ex) {
            Logger.getLogger(MessageTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "ERRR";
    }

    @Override
    public void listen(Runnable handler) {
        new Thread() {
            @Override
            public void run() {
                Timer probingTimer = new Timer();
                probingTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("listening...");
                        String readResult = _readAsString();
                        inputBuf = "";
                        if (!readResult.isEmpty()) {
                            inputBuf = readResult;
                            handler.run();
                            System.out.println();
//                                probingTimer.cancel();
                        }
                    }
                }, 0, PROBING_PERIOD);
            }
        }.start();
    }    
}
