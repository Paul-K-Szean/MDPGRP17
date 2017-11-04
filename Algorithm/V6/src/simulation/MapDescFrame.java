package simulation;

import java.awt.Dimension;
import javax.swing.JFrame;

public class MapDescFrame extends JFrame {
        
    private static final String _FRAME_NAME = "Hex Map Descriptor";
    private static final int _FRAME_WIDTH = 600;
    private static final int _FRAME_HEIGHT = 210;
    
    private MapDescPanel MpFramePanel;
    
    public MapDescPanel getHexFramePanel() {
        return MpFramePanel;
    }
    
    public MapDescFrame() {
        this.setTitle(_FRAME_NAME);
        this.setSize(new Dimension(_FRAME_WIDTH, _FRAME_HEIGHT));
        
        MpFramePanel = new MapDescPanel();
        this.setContentPane(MpFramePanel);
        this.setVisible(true);
    }
}
