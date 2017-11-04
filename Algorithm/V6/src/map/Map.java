package map;

import java.util.ArrayList;
import java.util.List;
import common.Console;
import robot.Robot;
import common.GridVector;

public class Map {
    // constants
    public static final int ROW = 15;
    public static final int COL = 20;
    public static final GridVector START_POS = new GridVector(1, 1);
    public static final GridVector GOAL_POS = new GridVector(ROW - 2, COL - 2);
    
    private final Waypoint[][] _wpMap;
    
    public Map() {
        _wpMap = new Waypoint[ROW][COL];
        for (int i = 0; i < ROW	; i++) {
            for (int j = 0; j < COL; j++) {
                // init default values
                GridVector curPos = new GridVector(i, j);
                WPSpecialState curSpecState = WPSpecialState.NA;
                WPObstacleState curObsState = WPObstacleState.IsWalkable;
                
                // change special state if applicable
                if (START_POS.equals(curPos)) {
                    curSpecState = WPSpecialState.IsStart;
                } else if (GOAL_POS.equals(curPos)) {
                    curSpecState = WPSpecialState.IsGoal;
                } else if (i == 0 || j == 0 || i == ROW - 1 || j == COL - 1) {
                    curObsState = WPObstacleState.IsVirtualObstacle;
                }
                
                // create point
                _wpMap[i][j] = new Waypoint(curPos, curSpecState, curObsState);
            }
        }
    }
    
    public Map(int[][] explored, boolean isStillExploring) {
        _wpMap = new Waypoint[ROW][COL];
        List<GridVector> obstacles = new ArrayList<>();
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                // init default values
                GridVector curPos = new GridVector(i, j);
                WPSpecialState curSpecState = WPSpecialState.NA;
                WPObstacleState curObsState = WPObstacleState.IsWalkable;
                
                // change special state if applicable
                if (START_POS.equals(curPos)) {
                    curSpecState = WPSpecialState.IsStart;
                } else if (GOAL_POS.equals(curPos)) {
                    curSpecState = WPSpecialState.IsGoal;
                } else {
                    switch (explored[i][j]) {
                        case 0:
                        		if (!isStillExploring) {
                             	obstacles.add(curPos);
                        		}
                            break;
                        case 2:
                            obstacles.add(curPos);
                            curSpecState = WPSpecialState.IsExplored;
                            break;
                        case 1:
                            curSpecState = WPSpecialState.IsExplored;
                            break;
                        
                    }
                }
                
                
                if(i == 0 || i== ROW-1 || j == 0 || j==COL-1)
                		curObsState = WPObstacleState.IsVirtualObstacle;
                // create point
                _wpMap[i][j] = new Waypoint(curPos, curSpecState, curObsState);
            }
        }
        addObstacle(obstacles);
    }
    
    private void _setObstacle(GridVector pos, WPObstacleState obsState) {
        _wpMap[pos.x()][pos.y()].obstacleState(obsState);
    }
    
    private void _processObstacleList(List<GridVector> obsList, 
                                        WPObstacleState actualState,
                                        WPObstacleState virtualCheckState,
                                        WPObstacleState virtualState) {
        obsList.forEach((curObsPos) -> {
            // add blocking tag for the obstacle
            _setObstacle(curObsPos, actualState);
            
            // add blocking tag for points adjacent to the obstacle
            for (int deltaI = -1; deltaI <= 1; deltaI++) {
                for (int deltaJ = -1; deltaJ <= 1; deltaJ++) {
                    int adjI = curObsPos.x() + deltaI;
                    int adjJ = curObsPos.y() + deltaJ;
                    if (adjI > -1 && adjI < ROW && adjJ > -1 && adjJ < COL &&
                        _wpMap[adjI][adjJ].obstacleState() == virtualCheckState) {
                        GridVector adjacentPos = new GridVector(adjI, adjJ);
                        _setObstacle(adjacentPos, virtualState);
                    }
                }
            }
        });
    }
    public void addObstacle(List<GridVector> obstaclePositions) {
        _processObstacleList(
            obstaclePositions,
            WPObstacleState.IsActualObstacle,
            WPObstacleState.IsWalkable,
            WPObstacleState.IsVirtualObstacle
        );
    }
    public void addObstacle(GridVector pos) {
        List<GridVector> temp = new ArrayList<>();
        temp.add(pos);
        addObstacle(temp);
    }
    public void clearObstacle(List<GridVector> obstaclePositions) {
        _processObstacleList(
            obstaclePositions,
            WPObstacleState.IsWalkable,
            WPObstacleState.IsVirtualObstacle,
            WPObstacleState.IsWalkable
        );
    }
    public void clearObstacle(GridVector pos) {
        List<GridVector> temp = new ArrayList<>();
        temp.add(pos);
        clearObstacle(temp);
    }
    public List<Waypoint> toList() {
        List<Waypoint> result = new ArrayList<>();
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                result.add(_wpMap[i][j]);
            }
        }
        return result;
    }
    
    public void highlight(List<GridVector> hightlightPositions, WPSpecialState wPSpecialState) {
        hightlightPositions.forEach((pos) -> {
            Waypoint curPoint = _wpMap[pos.x()][pos.y()];
            if (curPoint.specialState() != WPSpecialState.IsStart &&
                curPoint.specialState() != WPSpecialState.IsGoal) {
                curPoint.specialState(wPSpecialState);
            }
        });
    }
    public void clearAllHighlight() {
        for (Waypoint[] row : _wpMap) {
            for (Waypoint wp : row) {
                if (!wp.specialState().equals(WPSpecialState.IsStart) && 
                    !wp.specialState().equals(WPSpecialState.IsGoal)) {
                    wp.specialState(WPSpecialState.NA);
                }
            }
        }
    }
    
    public String toString(Robot robot) {
        String result = "";
        for (int i = -1; i <= ROW; i++) {
            for (int j = -1; j <= COL; j++) {
                if (i == -1 || j == -1 || i == ROW || j == COL) {
                    result += "# ";
                } else {
                    Waypoint curPoint = _wpMap[i][j];
                    if (curPoint.position().equals(robot.position())) {
                        String symbol = "  ";
                        switch (robot.orientation()) {
                            case Up:
                                symbol = "^ ";
                                break;
                            case Down:
                                symbol = "_ ";
                                break;
                            case Left:
                                symbol = "< ";
                                break;
                            case Right:
                                symbol = "> ";
                                break;
                        }
                        result += Console.ANSI_RED + symbol + Console.ANSI_RESET;
                        continue;
                    }
                    switch (curPoint.specialState()) {
                        case IsStart:
                            result += Console.ANSI_CYAN + "S " + Console.ANSI_RESET;
                            continue;
                        case IsGoal:
                            result += Console.ANSI_PURPLE + "G " + Console.ANSI_RESET;
                            continue;
                        case IsPathPoint:
                            result += Console.ANSI_GREEN + "x " + Console.ANSI_RESET;
                            continue;
                        case IsOpenedPoint:
                            result += "x ";
                            continue;
                        case IsClosedPoint:
                            result += Console.ANSI_YELLOW + "x " + Console.ANSI_RESET;
                            continue;
                        case NA:
                            break;
                    }
                    switch (curPoint.obstacleState()) {
                        case IsActualObstacle:
                            result += Console.ANSI_WHITE_BACKGROUND + " " + Console.ANSI_RESET + " ";
                            break;
                        case IsVirtualObstacle:
                            result += "  ";
                            break;
                        case IsWalkable:
                            result += Console.ANSI_GRAY + "+ " + Console.ANSI_RESET;
                            break;
                    }
                }
            }
            result += "\n";
        }
        return result;
    }
    
    public Waypoint getPoint(GridVector position) {
    		//System.out.println("i "+ position.i() + "j "+ position.j());
        return _wpMap[position.x()][position.y()];
    }
    
    public boolean checkValidPosition(GridVector pos) {
        return pos.x() > 0 && pos.x() < ROW &&
               pos.y() > 0 && pos.y() < COL;
    }
    
    public boolean checkValidBoundary(GridVector pos){
    		return pos.x() >=0 && pos.x() < ROW &&
                pos.y() >=0 && pos.y() < COL;
    }
    
    
}
