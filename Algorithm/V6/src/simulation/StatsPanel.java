package simulation;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatsPanel extends JPanel {
    
    private static final Color _BG_COLOR = new Color(0,209,214);
    private static final Color _FG_COLOR = Color.BLACK;
    
    private JLabel _time;

    public StatsPanel() {
        // config
        this.setBackground(_BG_COLOR);
        
        // children
        _time = new JLabel("00:00");
        _time.setFont(new Font(_time.getFont().getName(), Font.BOLD, 25));
        _time.setForeground(_FG_COLOR);
        this.add(_time);
        
    }

    public void setTime(String time) {
        _time.setText(time);
    }
    
}
