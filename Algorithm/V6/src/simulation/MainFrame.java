package simulation;

import java.awt.Dimension;
import javax.swing.JFrame;

public class MainFrame extends JFrame {
    
    private static final String _FRAME_NAME = "MDP17 - Simulator";
    private static final int _FRAME_WIDTH = 700;
    private static final int _FRAME_HEIGHT = 730;
    
    private MainPanel _mainPanel;

    public MainPanel getMainPanel() {
        return _mainPanel;
    }
    
    public MainFrame() {
        // config
        this.setTitle(_FRAME_NAME);
        this.setSize(new Dimension(_FRAME_WIDTH, _FRAME_HEIGHT));
        
        // children
        _mainPanel = new MainPanel();
        this.setContentPane(_mainPanel);
        this.setVisible(true);
    }
    
}
