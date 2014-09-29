package wifindus;

import javax.swing.JFrame;

public class DebuggerFrame extends JFrame
{
	private static final long serialVersionUID = 7198711131827826655L;

	public DebuggerFrame()
	{
		getContentPane().add(new DebuggerPanel());
	}
}
