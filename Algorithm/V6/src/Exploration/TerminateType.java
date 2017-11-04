package Exploration;

import java.util.Timer;
import java.util.TimerTask;
import map.Map;

public class TerminateType {

    private enum TerminatorType {
        Coverage, Time, Round
    }

    private static final int _PROBING_PERIOD = 10;

    private float maxCover;
    private long maxTime;
    private TerminatorType termType;
    private Runnable callback;

    private java.util.Timer thread;

    public TerminateType(int round, Runnable callbacked) {
        // round not used for now
        termType = TerminatorType.Round;
        callback = callbacked;
    }

    public TerminateType(float maxCoverage, Runnable callbacked) {
        maxCover = maxCoverage;
        termType = TerminatorType.Coverage;
        callback = callbacked;
    }

    public TerminateType(long maxDiffTime, Runnable callbacked) {
        maxTime = maxDiffTime;
        termType = TerminatorType.Time;
        callback = callbacked;
    }

    public void observe() {
        System.out.println("///////////////// " + termType);
        switch (termType) {
            case Coverage:
                int maxExplored = Map.ROW * Map.COL;
                thread = new Timer();
                thread.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MapRef mapViewer = ExploreSolve.getMapViewer();
                        if (mapViewer != null) {
                            int[][] explored = mapViewer.getExplored();
                            int exploredCount = 0;
                            for (int[] row : explored) {
                                for (int exploreState : row) {
                                    exploredCount += (exploreState >= 1) ? 1 : 0;
                                }
                            }
                            if (((float) exploredCount) / ((float) maxExplored) >= maxCover) {
                                System.out.println("Coverage Terminator activated");
                                thread.cancel();
                                callback.run();
                            }
                        }
                    }
                }, 0, _PROBING_PERIOD);
                break;
            case Time:
                thread = new Timer();
                thread.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Time Terminator activated");
                        callback.run();
                    }
                }, maxTime * 1000);
                break;
            case Round:
                thread = new Timer();
                thread.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (ExploreSolve.hasFinishedFirstRound()) {
                            System.out.println("Round Terminator activated");
                            thread.cancel();
                            callback.run();
                        }
                    }
                }, 0, _PROBING_PERIOD);
        }
    }

}
