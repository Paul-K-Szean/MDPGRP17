package ShortestPath;

import java.util.ArrayList;
import java.util.List;
import common.GridVector;

public class  ShortPathResult{
    public List<GridVector> shortestPath;
    public List<GridVector> openedPoints;
    public List<GridVector> closedPoints;
    
    ShortPathResult() {
        shortestPath = new ArrayList<>();
        openedPoints = new ArrayList<>();
        closedPoints = new ArrayList<>();
    }
}

