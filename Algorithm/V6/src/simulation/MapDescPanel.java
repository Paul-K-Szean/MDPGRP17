package simulation;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MapDescPanel extends JPanel {
    
    private JTextArea _hexTextArea;

    public JTextArea getHexTextArea() {
        return _hexTextArea;
    }
    
    public MapDescPanel() {
        _hexTextArea = new JTextArea("Hex descriptor populated there", 10, 50);
        this.add(_hexTextArea);
    }
    
}
