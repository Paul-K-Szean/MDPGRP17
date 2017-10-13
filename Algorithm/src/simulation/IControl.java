package simulation;

import map.Map;
import robot.Robot;
import simulation.MainFrame;

public interface IControl extends IUpdate {

    MainFrame getMainFrame();

    Map getMap();

    Robot getRobot();

    void reset();
}
