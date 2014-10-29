package wifindus.eye;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;

public class MapFrame extends JFrame implements ComponentListener
{
	private static final long serialVersionUID = 1029694096196910006L;
	private transient MapPanel mapPanel;
	
	public MapFrame(MapRenderer renderer)
	{
		if (renderer == null)
			throw new NullPointerException("Parameter 'renderer' cannot be null.");
		setTitle("Map");
		setLayout(null);
		getContentPane().add(mapPanel = new MapPanel(renderer));
		addComponentListener(this);
		setMinimumSize(new Dimension(300,300));
		setSize(new Dimension(600,600));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	@Override
	public void componentResized(ComponentEvent e)
	{
		positionComponents();
	}
	
	@Override public void componentMoved(ComponentEvent e) { }
	@Override public void componentShown(ComponentEvent e) { }
	@Override public void componentHidden(ComponentEvent e) { }
	
	public void setPan(MappableObject object, boolean interpolated)
	{
		mapPanel.setPan(object, interpolated);
	}
	
	public void setZoom(double zoom, boolean interpolated)
	{
		mapPanel.setZoom(zoom, interpolated);
	}
	
	private void positionComponents()
	{
		Dimension size = getContentPane().getSize();
		mapPanel.setLocation(0,0);
		mapPanel.setSize(size);		
	}
}

