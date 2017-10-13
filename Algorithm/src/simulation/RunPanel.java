package simulation;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RunPanel extends JPanel {
    
    private static final Color _BG_COLOR = new Color(0,209,214);
    
    private JTextField _spWaypoint;
    private JButton _explorationBtn;
    private JButton _shortestPathBtn;
    private JTextField _exePeriod;
    
    public RunPanel() {
        // config
        this.setBackground(_BG_COLOR);
        
        JLabel wayPointLabel = new JLabel("Waypoint: ");
        wayPointLabel.setForeground(Color.BLACK);
        this.add(wayPointLabel);
        _explorationBtn = new JButton("Exploration");
        _shortestPathBtn = new JButton("Shortest Path");
        _exePeriod = new JTextField("100", 5);
        _spWaypoint = new JTextField("0,0", 3);
        this.add(_spWaypoint);
        this.add(_explorationBtn);
        this.add(_shortestPathBtn);
        this.add(_exePeriod);
       
        JLabel exePeriodLabel = new JLabel("s/action");
        exePeriodLabel.setForeground(Color.BLACK);
        
        this.add(exePeriodLabel);
    }
    
    public JButton getExplorationBtn() {
        return _explorationBtn;
    }

    public JButton getShortestPathBtn() {
        return _shortestPathBtn;
    }

    public JTextField getExePeriod() {
        return _exePeriod;
    }
    
    public JTextField getspWaypoint() {
        return _spWaypoint;
    }
    
}
