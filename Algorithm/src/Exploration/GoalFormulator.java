package Exploration;

import map.Map;
import map.Waypoint;
import common.GridVector;

public class GoalFormulator {

    private MapRef mapViewer;

    public GoalFormulator(MapRef mv) {
        mapViewer = mv;
    }

    public Waypoint findFirstFrontier() {
        Waypoint waypoint = new Waypoint();

        return waypoint;
    }

    public boolean checkIfReachFinalGoal(GridVector v) {
        return (v.x() == 13) && (v.y() == 18);
    }

    public boolean checkIfReachStartZone(GridVector v) {
        return (v.x() == 1) && (v.y() == 1);
    }
}
