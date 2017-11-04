package simulation;

import simulation.EventHandler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPanel;
import map.Map;
import robot.Robot;
import common.GridVector;
import map.WPObstacleState;
import map.WPSpecialState;

public class GridFill extends JPanel {
    
    private static final int ROW = 15;
    private static final int COL = 20;
    private static final int GRID_SIZE = 30;
    private static final int BORDER = 2;
    
    private GridTile[][] grid;
    private MouseAdapter gridMouseAdapter;

    public GridTile[][] getGrid() {
        return grid;
    }

    public GridFill() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT, BORDER, BORDER));
        this.setPreferredSize(new Dimension(COL * GRID_SIZE + (COL + 1) * BORDER,ROW * GRID_SIZE + (ROW + 1) * BORDER));
        this.setBackground(ColorConfig.BG);
        this.fillGrid(new Map(), new Robot());
    }
    
    private List<GridVector> form9block(GridVector curPos) {
        List<GridVector> result = new ArrayList<>();
        result.add(curPos);
        result.add(curPos.fnAdd(new GridVector(0, 1)));
        result.add(curPos.fnAdd(new GridVector(0, -1)));
        result.add(curPos.fnAdd(new GridVector(1, 0)));
        result.add(curPos.fnAdd(new GridVector(-1, 0)));
        result.add(curPos.fnAdd(new GridVector(1, 1)));
        result.add(curPos.fnAdd(new GridVector(1, -1)));
        result.add(curPos.fnAdd(new GridVector(-1, 1)));
        result.add(curPos.fnAdd(new GridVector(-1, -1)));
        return result;
    }
    
    private int getColorOrder(Color color) {
        if (color.equals(ColorConfig.ROBOT_HEAD)) {
            return 0;
        } else if (color.equals(ColorConfig.ROBOT_BODY)) {
            return -1;
        } else if (color.equals(ColorConfig.PATH) || 
                    color.equals(ColorConfig.OPENED) || 
                    color.equals(ColorConfig.CLOSED) ||
                    color.equals(ColorConfig.UNEXPLORED)) {
            return -2;
        } else {
            return -3;
        }
    }
    
    private Color readColor(String curKey, Color targetColor, HashMap<String, Color> specialColors) {
        Color existingColor = specialColors.getOrDefault(curKey, ColorConfig.NORMAL);
        return getColorOrder(existingColor) > getColorOrder(targetColor) ? 
                existingColor : targetColor;
    }    
    
    private void applyMouseAdapter() {
        for (GridTile[] row : grid) {
            for (GridTile square : row) {
                for (MouseListener mouseListener : square.getMouseListeners()) {
                    square.removeMouseListener(mouseListener);
                }
                square.addMouseListener(gridMouseAdapter);
            }
        }
    }
    
    private HashMap<String, Color> genSpecColor(Map map, Robot robot) {
        HashMap<String, Color> result = new HashMap<>();
        map.toList().forEach((curPoint) -> {
            String curKey;
            // 1x1
            // actual obstacle
            curKey = curPoint.position().toString();
            
            boolean isUnexplored = false;
            if (!EventHandler.isShortestPath()) {
                if (!curPoint.specialState().equals(WPSpecialState.IsExplored) &&
                    !curPoint.specialState().equals(WPSpecialState.IsStart) &&
                    !curPoint.specialState().equals(WPSpecialState.IsGoal)) {
                    result.put(curKey, readColor(curKey, ColorConfig.UNEXPLORED, result));
                    isUnexplored = true;
                }
            }
            if (curPoint.obstacleState().equals(WPObstacleState.IsActualObstacle)) {
                result.put(curKey, readColor(curKey, ColorConfig.OBSTACLE, result));
            }
            // 3x3
            switch (curPoint.specialState()) {
                case IsStart:
                    for (GridVector curPos : form9block(curPoint.position())) {
                        curKey = curPos.toString();
                        result.put(curKey, readColor(curKey, ColorConfig.START, result));
                    }
                    break;
                case IsGoal:
                    for (GridVector curPos : form9block(curPoint.position())) {
                        curKey = curPos.toString();
                        result.put(curKey, readColor(curKey, ColorConfig.GOAL, result));
                    }
                    break;
                case IsPathPoint:
                    if (!isUnexplored) {
                        curKey = curPoint.position().toString();
                        result.put(curKey, readColor(curKey, ColorConfig.PATH, result));
                    }
                    break;
                case IsClosedPoint:
                    if (!isUnexplored) {
                        curKey = curPoint.position().toString();
                        result.put(curKey, readColor(curKey, ColorConfig.CLOSED, result));
                    }
                    break;
                case IsOpenedPoint:
                    if (!isUnexplored) {
                        curKey = curPoint.position().toString();
                        result.put(curKey, readColor(curKey, ColorConfig.OPENED, result));
                    }
                    break;
            }
            // robot
            if (curPoint.position().equals(robot.position())) {
                for (GridVector curPos : form9block(curPoint.position())) {
                    Color robotSqColor = ColorConfig.ROBOT_BODY;
                    if (robot.position()
                            .fnAdd(robot.orientation().toVector2())
                            .equals(curPos)) {
                        robotSqColor = ColorConfig.ROBOT_HEAD;
                    }
                    curKey = curPos.toString();
                    result.put(curKey, readColor(curKey, robotSqColor, result));
                }
            }
            
        });
        
        return result;
    }
    
    public void fillGrid(Map map, Robot robot) {
        boolean isFirstTime = grid == null;
        if (isFirstTime) grid = new GridTile[ROW][COL];
        HashMap<String, Color> specialColors = genSpecColor(map, robot);
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                if (isFirstTime) grid[i][j] = new GridTile(new GridVector(i, j));
                GridVector curLocation = new GridVector(i, j);
                Color targetColor = ColorConfig.NORMAL;
                if (specialColors.containsKey(curLocation.toString())) {
                    targetColor = specialColors.get(curLocation.toString());
                }
                grid[i][j].setBackground(targetColor);            
                if (isFirstTime) {
                    grid[i][j].setPreferredSize(new Dimension(GRID_SIZE,GRID_SIZE));
                }
            }
        }
        if (isFirstTime) {
            for (GridTile[] row : grid) {
                for (GridTile square : row) {
                    this.add(square);
                }
            }
        }
    }
    
    public void setGridAdapter(MouseAdapter adapter) {
        gridMouseAdapter = adapter;
        applyMouseAdapter();
    }
    
}
