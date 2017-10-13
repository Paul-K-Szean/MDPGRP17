package simulation;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import Combine.Main;
import common.GridVector;
import communication.MessageTranslator;
import map.MapDescriptor;
import map.Map;
import map.WPObstacleState;
import map.WPSpecialState;
import robot.Robot;
import robot.RobotMovement;
import simulation.GridTile;
import simulation.MapDescFrame;
import simulation.IControl;
import Exploration.ExploreSolve;
import Exploration.TerminateType;
import ShortestPath.ShortPathSolve;
import ShortestPath.ShortPathResult;
import ShortestPath.ShortPathCal;
import ShortestPath.ShortPathSolveType;

public class EventHandler implements IHandler {

    private IControl gui;
    private Timer _shortestPathThread;
    private Thread _explorationThread;
    private static boolean _isShortestPath = true;
    private volatile boolean _callbackCalled;
    private Timer _timerThread;
    private String waypointRpi;

    public static boolean isShortestPath() {
        return _isShortestPath;
    }

    public EventHandler(IControl contr) {
        gui = contr;
        gui.reset();

        // frame event
        gui.getMainFrame().addWindowListener(_wrapWindowAdapter(WindowEventHandler.OnClose));

        // obstacle event
        gui.getMainFrame().getMainPanel().getGridPanel().getGridContainer().setGridAdapter(_wrapMouseAdapter(ClickEventHandler.OnToggleObstacle));

        // descriptor control event
        gui.getMainFrame().getMainPanel().getDescCtrlPanel().getOpenDescBtn().addMouseListener(_wrapMouseAdapter(ClickEventHandler.OnOpen));
        gui.getMainFrame().getMainPanel().getDescCtrlPanel().getSaveDescBtn().addMouseListener(_wrapMouseAdapter(ClickEventHandler.OnSave));
        gui.getMainFrame().getMainPanel().getDescCtrlPanel().getGetHexBtn().addMouseListener(_wrapMouseAdapter(ClickEventHandler.OnGetHex));

        // run control event
        gui.getMainFrame().getMainPanel().getRunCtrlPanel().getExplorationBtn().addMouseListener(_wrapMouseAdapter(ClickEventHandler.OnExploration));
        gui.getMainFrame().getMainPanel().getRunCtrlPanel().getShortestPathBtn().addMouseListener(_wrapMouseAdapter(ClickEventHandler.OnShortestPath));
        gui.getMainFrame().getMainPanel().getIntrCtrlPanel().getTermRoundCheckbox().addMouseListener(_wrapMouseAdapter(ClickEventHandler.OnToggleRound));
        gui.getMainFrame().getMainPanel().getSimCtrlPanel().getSimCheckBox().addMouseListener(_wrapMouseAdapter(ClickEventHandler.OnToggleSim));
        gui.getMainFrame().getMainPanel().getSimCtrlPanel().getConnectBtn().addMouseListener(_wrapMouseAdapter(ClickEventHandler.OnConnectBtn));
    }

    @Override
    public void resolveHandler(ClickEventHandler hdlr, MouseEvent e) {
        switch (hdlr) {
            case OnToggleObstacle:
                onTogObs(e);
                break;
            case OnOpen:
                onOpenDesc(e);
                break;
            case OnSave:
                onSaveDesc(e);
                break;
            case OnGetHex:
                onGetHex(e);
                break;
            case OnExploration:
                onExplore(e);
                break;
            case OnShortestPath:
                onShortPath(e);
                break;
            case OnRestart:
                onRestart(e);
                break;
            case OnToggleRound:
                onTogRound(e);
                break;
            case OnToggleSim:
                onTogSim(e);
                break;
            case OnConnectBtn:
                onConnect(e);
                break;
            case OnStartTimer:
                onStartTimer();
                break;
            case OnStopTimer:
                onStopTimer();
                break;
        }
    }

    @Override
    public void resolveFrameHandler(WindowEventHandler hdlr, WindowEvent e) {
        switch (hdlr) {
            case OnClose:
                onClose(e);
                break;
        }
    }

    private MouseAdapter _wrapMouseAdapter(ClickEventHandler hdlr) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                resolveHandler(hdlr, e);
            }
        };

    }

    private WindowAdapter _wrapWindowAdapter(WindowEventHandler hdlr) {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resolveFrameHandler(hdlr, e);
            }
        };
    }

    private void onClose(WindowEvent e) {
        System.exit(0);
    }

    private void onSaveDesc(MouseEvent e) {
        try {
            String filePath = gui.getMainFrame().getMainPanel().getDescCtrlPanel().getFilePathTextField().getText();
            /////// for testing
            boolean[][] explored = new boolean[Map.ROW][Map.COL];
            for (int i = 0; i < Map.ROW; i++) {
                for (int j = 0; j < Map.COL; j++) {
                    explored[i][j] = true;
                }
            }
            ///////
            MapDescriptor.saveToFile(filePath, gui.getMap(), explored);
            System.out.println("Save desc completed.");
        } catch (IOException ex) {
            Logger.getLogger(IControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void onOpenDesc(MouseEvent e) {
        try {
            gui.reset();
            String filePath = gui.getMainFrame().getMainPanel().getDescCtrlPanel().getFilePathTextField().getText();
            System.out.println(filePath);
            gui.update(MapDescriptor.parseFromFile(filePath));
            System.out.println("Open desc completed.");
        } catch (IOException ex) {
            Logger.getLogger(IControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void onGetHex(MouseEvent e) {
        Map map = ExploreSolve.getMapViewer().getSubjectiveMap();
        int[][] explored = ExploreSolve.getMapViewer().getExplored();

        String descStr = MapDescriptor.stringify(map, explored);
        String content = String.join("\n", MapDescriptor.toHex(descStr));

        MapDescFrame hexFrame = new MapDescFrame();
        hexFrame
                .getHexFramePanel()
                .getHexTextArea().setText(content);

        System.out.println("Get hex completed.");
    }
    private void onExplore(MouseEvent e) {
        int exePeriod = Integer.parseInt(
                gui.getMainFrame().getMainPanel().getRunCtrlPanel().getExePeriod().getText());
        _explorationThread = new Thread(() -> {
            try {
                _explorationProcedure(exePeriod, () -> {
                    gui.trigger(ClickEventHandler.OnStopTimer);
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        _explorationThread.start();
    }

    private void onShortPath(MouseEvent e) {
        try {
            int exePeriod = Integer.parseInt(gui.getMainFrame().getMainPanel().getRunCtrlPanel().getExePeriod().getText());
            String sPwaypoint = gui.getMainFrame().getMainPanel().getRunCtrlPanel().getspWaypoint().getText();     
            _shortestPathProcedure(exePeriod, sPwaypoint);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void onRestart(MouseEvent e) {
        gui.getMainFrame().dispose();
        Main.startGUI();
        _isShortestPath = true;
        System.out.println("Restart completed.");
    }

    private void onTogObs(MouseEvent e) {
        GridTile target = (GridTile) e.getSource();
        GridVector clickedPos = target.position();
        WPObstacleState obsState = gui.getMap().getPoint(clickedPos).obstacleState();
        if (obsState.equals(WPObstacleState.IsActualObstacle)) {
            gui.getMap().clearObstacle(clickedPos);
        } else {
            gui.getMap().addObstacle(clickedPos);
        }
        target.toggleBackground();

        System.out.println("Toggled obstacle at " + clickedPos);
    }

    private void onTogRound(MouseEvent e) {
        boolean isChecked = gui.getMainFrame().getMainPanel().getIntrCtrlPanel().getTermRoundCheckbox().isSelected();
        if (isChecked) {
            System.out.println("Enabled terminating after 1st round");
        } else {
            System.out.println("Disabled terminating after 1st round");
        }
    }

    private void onTogSim(MouseEvent e) {
        boolean isSelected = gui.getMainFrame().getMainPanel().getSimCtrlPanel().getSimCheckBox().isSelected();
        Main.isSimulating(isSelected);
        if (isSelected) {
            System.out.println("Simulation mode enabled.");
        } else {
            System.out.println("Simulation mode disabled.");
        }
    }

    private void onConnect(MouseEvent e) {
        try {
            Main.connectToRpi();
        } catch (IOException ex) {
            Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void onStartTimer() {
        Date startTime = new Date();
        System.out.println("startTime = " + startTime);
        _timerThread = new Timer();
        _timerThread.schedule(new TimerTask() {
            @Override
            public void run() {
                Date diffTime = new Date(new Date().getTime() - startTime.getTime() - 1800000);
                String timeStr = new SimpleDateFormat("mm:ss").format(diffTime);
                gui.getMainFrame().getMainPanel().getStatsCtrlPanel().setTime(timeStr);
            }
        }, 1000, 1000);
    }

    private void onStopTimer() {
        _timerThread.cancel();
    }

    // shared procedures
    private void _explorationProcedure(int exePeriod, Runnable callback) throws InterruptedException, IOException {
        System.out.println("Starting Exploration");
        _isShortestPath = false;
        _callbackCalled = false;
        int termCoverage = Integer.parseInt(gui.getMainFrame().getMainPanel().getIntrCtrlPanel().getTermCoverageText().getText());
        long termTime = Integer.parseInt(gui.getMainFrame().getMainPanel().getIntrCtrlPanel().getTermTimeText().getText());
        boolean termRound = gui.getMainFrame().getMainPanel().getIntrCtrlPanel().getTermRoundCheckbox().isSelected();

        Runnable interruptCallback = () -> {
            if (!_callbackCalled) {
                _callbackCalled = true;
                System.out.println(">> STOP <<");
                Robot curRobot = ExploreSolve.getRobot();
                int[][] explored = ExploreSolve.getMapViewer().getExplored();
                _explorationThread.stop();
                Map finalMap = new Map(explored, false);
                try {
                    ExploreSolve.goBackToStart(finalMap, curRobot, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException ex) {
                    Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                Main.getGUI().update(finalMap);
            }
        };
        Runnable nonInterruptCallback = () -> {
            if (!_callbackCalled) {
                _callbackCalled = true;
                Robot curRobot = ExploreSolve.getRobot();
                int[][] explored = ExploreSolve.getMapViewer().getExplored();
                Map finalMap = new Map(explored, false);
                try {
                    ExploreSolve.goBackToStart(finalMap, curRobot, callback);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException ex) {
                    Logger.getLogger(EventHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                Main.getGUI().update(finalMap);
            }
        };

        TerminateType terminator = null;
        if (termRound) {
            terminator = new TerminateType(1, interruptCallback);
        } else if (termCoverage != 0 && termCoverage != 100) {
            terminator = new TerminateType(termCoverage / 100f, interruptCallback);
        } else if (termTime != 0) {
            terminator = new TerminateType(termTime, interruptCallback);
        }
        if (terminator != null) {
            terminator.observe();
        }

        gui.trigger(ClickEventHandler.OnStartTimer);
        ExploreSolve.solve(gui.getMap(), exePeriod);
        System.out.println("Exploration completed.");
        System.out.println(ExploreSolve.getMapViewer().robotVisitedPlaceToString());
        nonInterruptCallback.run();
    }

    private void _shortestPathProcedure(int exePeriod, String sPwaypoint) throws IOException {
        System.out.println("Starting Shortest Path");
        _isShortestPath = true;

        ShortPathSolve solver = new ShortPathSolve();
        Map map = gui.getMap();
        Robot robot = gui.getRobot();
        Robot tempbot = gui.getRobot();
        int xWaypoint = 0;
        int yWaypoint = 0;
        if(!Main.isSimulating()){
        	String fromRpi = waypointRpi;
        	String[] tempstr = fromRpi.split(",");
        	xWaypoint = Integer.parseInt(tempstr[1]);
            yWaypoint = Integer.parseInt(tempstr[2]);
        }
        else{
        	String[] tempstr = sPwaypoint.split(",");
            xWaypoint = Integer.parseInt(tempstr[0]);
            yWaypoint = Integer.parseInt(tempstr[1]);
        }        
        //Set Waypoint for shortest path
        GridVector Waypointpos = new GridVector(xWaypoint, yWaypoint);
        //Shortest Path from start to waypoint
        List<GridVector> normalSolve = solver.solve(map, robot, Waypointpos).shortestPath; 
        System.out.println(normalSolve);
        //Shortest Path from waypoint to goal
        tempbot.position(Waypointpos);
        List<GridVector> normalSolve2 = solver.solve(map, tempbot).shortestPath;
        System.out.println(normalSolve2);        
        normalSolve.addAll(normalSolve2);
        System.out.println(normalSolve);
        float normalPathCost= 0.65f * ShortPathCal.countTurn(normalSolve)+ 0.35f * ShortPathCal.calDistance(normalSolve);

        System.out.println("normalPathCost = " + normalPathCost);

        // do quad directional
        map.highlight(normalSolve, WPSpecialState.IsPathPoint);
        gui.getRobot().position(new GridVector(1, 1));
        LinkedList<RobotMovement> actions = RobotMovement
                .fromPath(gui.getRobot(), normalSolve);
        System.out.println(normalSolve);
        System.out.println(actions);

        System.out.println("Main.isSimulating() = " + Main.isSimulating());
        if (!Main.isSimulating()) {
            // messaging arduino
            System.out.println("Sending sensing request to rpi (-> arduino) ");
            Main.getRpi().sendMoveCommand(actions, MessageTranslator.MODE_1);
           
        }
        _shortestPathThread = new Timer();
        _shortestPathThread.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!actions.isEmpty()) {
                    gui.getRobot().execute(actions.pop());
                    gui.update(gui.getMap(), gui.getRobot());
                } else {
                    System.out.println("Shortest path completed.");
                    this.cancel();
                }
            }
        }, exePeriod, exePeriod);       
    }

	public static void setWaypointString(String inStr) {
		String waypointRpi = inStr;
	}

}