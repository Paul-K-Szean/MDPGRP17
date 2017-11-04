package simulation;


import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import common.GridVector;

public class GridTile extends JPanel {
    
    private GridVector pos;

    public GridTile(GridVector position) {
        pos = position;
        JLabel label = new JLabel(pos.x() + ", " + pos.y());
        label.setForeground(Color.white);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        this.add(label); 
    }

    public GridVector position() {
        return pos;
    }
    
    public void toggleBackground() {
        if (this.getBackground().equals(ColorConfig.OBSTACLE)) {
            this.setBackground(ColorConfig.NORMAL);
        } 
        else {
            this.setBackground(ColorConfig.OBSTACLE);
        }
    }
    
}