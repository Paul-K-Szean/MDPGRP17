package simulation;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DescMainPanel extends JPanel {
    
    private static final Color _BG_COLOR = new Color(0,209,214);
    
    private JTextField _filePathField;
    private JButton _openDescBtn;
    private JButton _saveDescBtn;
    private JButton _getHexBtn;
    
    public DescMainPanel() {
        this.setBackground(_BG_COLOR);
        
        _filePathField = new JTextField("", 10);
        _openDescBtn = new JButton("Open");
        _saveDescBtn = new JButton("Save");
        _getHexBtn = new JButton("Get Hex");
        _filePathField.setHorizontalAlignment(JTextField.RIGHT);
        JLabel pathLabel = new JLabel(".txt");
        pathLabel.setForeground(Color.BLACK);
        this.add(_filePathField);
        this.add(pathLabel);
        this.add(_openDescBtn);
        this.add(_saveDescBtn);
        this.add(_getHexBtn);
    }

    public JTextField getFilePathTextField() {
        return _filePathField;
    }

    public JButton getOpenDescBtn() {
        return _openDescBtn;
    }

    public JButton getSaveDescBtn() {
        return _saveDescBtn;
    }

    public JButton getGetHexBtn() {
        return _getHexBtn;
    }

    
}

