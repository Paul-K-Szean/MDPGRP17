package simulation;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import Combine.Main;

public class SimPanel extends JPanel {
    
    private static final Color _BG_COLOR = new Color(0,209,214);
    
    private JCheckBox _simCheckBox;
    private JButton _connectBtn;

    public SimPanel() {
        // config
        this.setBackground(_BG_COLOR);
        
        // children
        _simCheckBox = new JCheckBox("Simulation", Main.isSimulating());
        _connectBtn = new JButton("Connect to RPi");
        _simCheckBox.setBackground(_BG_COLOR);
        _simCheckBox.setForeground(Color.BLACK);        
        this.add(_simCheckBox);
        this.add(_connectBtn);
    }

    public JCheckBox getSimCheckBox() {
        return _simCheckBox;
    }

    public JButton getConnectBtn() {
        return _connectBtn;
    }
    
    
}
