package Combine;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import communication.MessageTranslator;
import robot.Robot;
import communication.ITranslate;
import simulation.EventHandler;
import simulation.GUI;
import simulation.ClickEventHandler;
import simulation.IUpdate;
import Exploration.MovementFormulator;

public class Main {

    private static IUpdate _gui;
    private static ITranslate _rpi;

    private static boolean _isSimulating = false;

    public static void main(String[] args) throws IOException {
        System.out.println("Initiating GUI...");
        startGUI();
    }

    public static boolean isSimulating() {
        return _isSimulating;
    }

    public static void isSimulating(boolean isSimulating) {
        _isSimulating = isSimulating;
    }

    public static IUpdate getGUI() {
        return _gui;
    }

    public static ITranslate getRpi() {
        return _rpi;
    }

    public static void startGUI() {
        SwingUtilities.invokeLater(() -> {
            _gui = new GUI();
        });
    }

    public static void connectToRpi() throws IOException {
        _rpi = new MessageTranslator();
        _rpi.connect(() -> {
            try {
                _listenToRPi();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private static void _listenToRPi() throws IOException {
        _rpi.listen(() -> {
            String inStr = _rpi.getInputBuffer();
            //System.out.println("inStr = " + inStr);
            if(inStr.contains("ae")){
            	System.out.println("Triggering Exploration");
                _gui.trigger(ClickEventHandler.OnExploration);
            }
            else if(inStr.contains("as")){            	
            	System.out.println("Triggering ShortestPath");
            	EventHandler.setWaypointString(inStr);
                _gui.trigger(ClickEventHandler.OnShortestPath);
            }
            else if(inStr.contains("bd")){
            	System.out.println("Received d");
            	MovementFormulator.calibrationCompletedCallBack();
            }
            else{
            	if(inStr.length()==6){
            		System.out.println("Analyzing sensing information");
            		MovementFormulator.sensingDataCallback(inStr);
            		Robot.actionCompletedCallBack();
            	}
            	else{
            		System.out.println("Unrecognized input");
            	}
            }
        });
    }

}

