package simulation;

import java.awt.Color;
import javax.swing.JPanel;

public class GridPanel extends JPanel {
    
    private static final Color _BG_COLOR = new Color(0,209,214);
    
    private GridFill gridContain;

    public GridFill getGridContainer() {
        return gridContain;
    }

    public GridPanel() {
        this.setBackground(_BG_COLOR); 
        gridContain = new GridFill();
        this.add(gridContain);
    }  
    
}