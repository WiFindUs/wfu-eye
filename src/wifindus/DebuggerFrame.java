package wifindus;

import java.awt.Dimension;

import javax.swing.JFrame;

public class DebuggerFrame extends JFrame
{
	private static final long serialVersionUID = 7198711131827826655L;

	public DebuggerFrame()
	{
		setMinimumSize(new Dimension(400,400));
		getContentPane().add(new DebuggerPanel());
	}
}
