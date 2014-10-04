package wifindus.eye.dispatcher;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class MapFrame extends JFrame
{
	public MapFrame()
	{
		setMinimumSize(new Dimension(400,400));
		JLabel mapText = new JLabel("Map");
		add(mapText);
	}
}
