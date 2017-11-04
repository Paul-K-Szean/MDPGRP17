package map;

import common.GridVector;

public class Waypoint {
    
    private final GridVector pos;
    private WPSpecialState specState;
    private WPObstacleState obsState;
    
    public Waypoint(GridVector position, WPSpecialState specialState, WPObstacleState obstacleState) {
        pos = position;
        specState = specialState;
        obsState = obstacleState;    
    }
    
    public Waypoint(GridVector position) {
        this(position, WPSpecialState.NA, WPObstacleState.IsWalkable);
    }
    
    public Waypoint() {
        this(new GridVector(-1, -1));
    }
    
    public GridVector position() {
    	return pos; 
    }
    
    public WPSpecialState specialState() { 
    	return specState; 
    }
    
    public WPObstacleState obstacleState() { 
    	return obsState; 
    }
    
    public void obstacleState(WPObstacleState obstacleState) {
    	obsState = obstacleState; 
    }
    
    public void specialState(WPSpecialState specialState) {
    	specState = specialState; 
    }
    
}
