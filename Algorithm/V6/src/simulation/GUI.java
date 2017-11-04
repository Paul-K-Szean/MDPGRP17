package simulation;

import simulation.MainFrame;
import simulation.EventHandler;
import map.Map;
import robot.Robot;
import simulation.ClickEventHandler;

public class GUI implements IControl {
    
    private Map map;
    private Robot robot;
    
    private MainFrame mainFrame;
    private EventHandler eventHandler;

    public GUI() {
        mainFrame = new MainFrame();
        eventHandler = new EventHandler(this);
    }

    @Override
    public Map getMap() {
        return map;
    }

    @Override
    public Robot getRobot() {
        return robot;
    }

    @Override
    public MainFrame getMainFrame() {
        return mainFrame;
    }
    
    @Override
    public void reset() {
        this.update(new Map(), new Robot());
    }
    
    @Override
    public void update(Map m, Robot r) {
        map = m;
        robot = r;
        mainFrame.getMainPanel().getGridPanel().getGridContainer().fillGrid(map, robot);
        mainFrame.revalidate();
    }
    
    @Override
    public void update(Map m) {
        update(m, robot);
    }
    
    @Override
    public void update(Robot r) {
        update(map, r);
    }

    @Override
    public void trigger(ClickEventHandler hdlr) {
        eventHandler.resolveHandler(hdlr, null);
    }

    @Override
    public boolean isSingleRoundRun() {
        return mainFrame.getMainPanel().getIntrCtrlPanel().getTermRoundCheckbox().isSelected();
    }    
}