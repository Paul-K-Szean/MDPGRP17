package ShortestPath;

import common.Direction;
import map.Waypoint;

public class ShortPathPoint extends Waypoint {
    
    private final int h;
    private int g;
    private Direction parD;
    
    public ShortPathPoint(Waypoint wp, int hval, int gval, Direction parentDir) {
        super(wp.position(), wp.specialState(), wp.obstacleState());
        h = hval;
        g = gval;
        parD = parentDir;
    }
    
    public ShortPathPoint(Waypoint wp) {
        this(wp, 1000000, 1000000, Direction.Left);
    }
    
    public ShortPathPoint() {
        this(new Waypoint());
    }    

    public int hval() { 
    	return h; 
    }
    
    public int gval() {
    	return g;
    }
    
    public void gval(int gval) {
    	g = gval; 
    }

    public Direction parentDir() { 
    	return parD; 
    }

    public void parentDir(Direction parentDir) {
    	parD = parentDir; 
    }    
    
    public int fval() {
    	return 100 * h + 95 * g; 
    }
}
