package Exploration;

import common.Direction;
import common.GridVector;

public class RobotMovementHistory {
	public GridVector pos;
	public Direction dir;
	
	public RobotMovementHistory(GridVector p , Direction d){
		pos= p;
		dir = d;
	}
	
	public static boolean compare(RobotMovementHistory a, RobotMovementHistory b){
		return a.pos==b.pos && a.dir == b.dir;		
	}
	
}
