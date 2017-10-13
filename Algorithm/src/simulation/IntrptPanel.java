package simulation;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class IntrptPanel extends JPanel {
    
    private static final Color _BG_COLOR = new Color(0,209,214);    
    private JTextField _termCoverageText;
    private JTextField _termTimeText;
    private JCheckBox _termRoundCheckbox;
    
    public IntrptPanel() {
        this.setBackground(_BG_COLOR);
        _termCoverageText = new JTextField("100", 3);
        _termTimeText = new JTextField("0", 3);
        _termRoundCheckbox = new JCheckBox("Terminate after 1st round");
        JLabel termLabel = new JLabel("Terminate after: ");
        JLabel coverageLabel = new JLabel("% or");
        JLabel timeLabel = new JLabel("seconds");
        termLabel.setForeground(Color.BLACK);
        _termRoundCheckbox.setBackground(_BG_COLOR);
        _termRoundCheckbox.setForeground(Color.BLACK);
        coverageLabel.setForeground(Color.BLACK);
        timeLabel.setForeground(Color.BLACK);
        this.add(termLabel);
        this.add(_termCoverageText);
        this.add(coverageLabel);
        this.add(_termTimeText);
        this.add(timeLabel);
        this.add(_termRoundCheckbox);
    }

    public JTextField getTermCoverageText() {
        return _termCoverageText;
    }

    public JTextField getTermTimeText() {
        return _termTimeText;
    }

    public JCheckBox getTermRoundCheckbox() {
        return _termRoundCheckbox;
    }
    
}

