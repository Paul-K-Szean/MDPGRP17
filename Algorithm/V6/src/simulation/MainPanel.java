package simulation;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class MainPanel extends JPanel {
    
    private GridPanel _gridPanel;
    private DescMainPanel _descCtrlPanel;
    private RunPanel _runCtrlPanel;
    private IntrptPanel _intrCtrlPanel;
    private SimPanel _simCtrlPanel;
    private StatsPanel _statsCtrlPanel;

    public GridPanel getGridPanel() {
        return _gridPanel;
    }

    public DescMainPanel getDescCtrlPanel() {
        return _descCtrlPanel;
    }

    public RunPanel getRunCtrlPanel() {
        return _runCtrlPanel;
    }

    public IntrptPanel getIntrCtrlPanel() {
        return _intrCtrlPanel;
    }

    public SimPanel getSimCtrlPanel() {
        return _simCtrlPanel;
    }

    public StatsPanel getStatsCtrlPanel() {
        return _statsCtrlPanel;
    }

    public MainPanel() {
        // config
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // children
        _gridPanel = new GridPanel();
        _descCtrlPanel = new DescMainPanel();
        _runCtrlPanel = new RunPanel();
        _intrCtrlPanel = new IntrptPanel();
        _simCtrlPanel = new SimPanel();
        _statsCtrlPanel = new StatsPanel();
        this.add(_gridPanel);
        this.add(_descCtrlPanel);
        this.add(_runCtrlPanel);
        this.add(_intrCtrlPanel);
        this.add(_simCtrlPanel);
        this.add(_statsCtrlPanel);
    }
    
}
