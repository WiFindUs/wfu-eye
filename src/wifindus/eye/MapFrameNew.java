package wifindus.eye;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;

public class MapFrameNew extends JFrame implements ComponentListener
{
	private static final long serialVersionUID = 1029694096196910006L;
	private transient MapPanel mapPanel;
	
	public MapFrameNew(MapRenderer renderer)
	{
		if (renderer == null)
			throw new NullPointerException("Parameter 'renderer' cannot be null.");
		setLayout(null);
		getContentPane().add(mapPanel = new MapPanel(renderer));
		addComponentListener(this);
		setMinimumSize(new Dimension(300,300));
		setSize(new Dimension(600,600));
	}
	
	@Override
	public void componentResized(ComponentEvent e)
	{
		positionComponents();
	}

	@Override public void componentMoved(ComponentEvent e) { }
	@Override public void componentShown(ComponentEvent e) { }
	@Override public void componentHidden(ComponentEvent e) { }
	
	private void positionComponents()
	{
		Dimension size = getContentPane().getSize();
		mapPanel.setLocation(0,0);
		mapPanel.setSize(size);		
	}
}

