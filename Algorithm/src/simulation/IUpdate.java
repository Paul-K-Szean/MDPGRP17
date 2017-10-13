package simulation;

import map.Map;
import robot.Robot;
import simulation.ClickEventHandler;

public interface IUpdate {
    
//    public enum ManualTrigger { Exploration, ShortestPath, Combined, Stop }

//    void trigger(ManualTrigger trigger);
    void trigger(ClickEventHandler hdlr);

    void update(Map map, Robot robot);

    void update(Map map);

    void update(Robot robot);
    
    boolean isSingleRoundRun();
    
}
