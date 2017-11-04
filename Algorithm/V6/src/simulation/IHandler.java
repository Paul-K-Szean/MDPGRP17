package simulation;

import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public interface IHandler {

	void resolveHandler(ClickEventHandler hdlr, MouseEvent e);

	void resolveFrameHandler(WindowEventHandler hdlr, WindowEvent e);
	    
}
