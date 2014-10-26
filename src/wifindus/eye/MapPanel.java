package wifindus.eye;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;

public class MapPanel extends JPanel
	implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener
{
	private static final Color OVERLAY_COLOR = new Color(0,0,0,50);
	
	private static final double PAN_SPEED = 1.0;
	private static final double ZOOM_SPEED = 0.2;
	private static final long serialVersionUID = -1496522215279713246L;
	private MapRenderer renderer;
	private boolean mouseEntered = false;
	private boolean mouseDownLeft = false;
	private boolean mouseDownMiddle = false;
	private int mouseX = 0;
	private int mouseY = 0;
	private double xPos = 0.5;
	private double yPos = 0.5;
	private double zoom = 0.5;	

	public MapPanel(MapRenderer renderer)
	{
		setRenderer(renderer);
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		this.setBackground(Color.DARK_GRAY);
	}
	
	public void setRenderer(MapRenderer renderer)
	{
		if (renderer == null)
			throw new NullPointerException("Parameter 'renderer' cannot be null.");
		if (this.renderer != null)
			this.renderer.removeRenderClient(this);
		this.renderer = renderer;
		this.renderer.addRenderClient(this);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			mouseX = e.getX();
			mouseY = e.getY();
			mouseDownLeft = true;
		}
		else if (e.getButton() == MouseEvent.BUTTON2)
		{
			mouseDownMiddle = true;
			repaint();
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (mouseDownMiddle)
			return;
		
		if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) 
		{
			zoom = Math.min(4.0, Math.max(0.2, zoom - (e.getWheelRotation() * ZOOM_SPEED)));
			repaint();
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON1)
			mouseDownLeft = false;
		else if (e.getButton() == MouseEvent.BUTTON2)
		{
			mouseDownMiddle = false;
			repaint();
		}
	}	

	@Override
	public void mouseEntered(MouseEvent e)
	{
		mouseEntered = true;
	}
	
	@Override
	public void mouseExited(MouseEvent e)
	{
		mouseEntered = false;
	}
	
	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (mouseDownLeft)
		{
			xPos = Math.min(1.0, Math.max(0.0,  xPos - (((double)(e.getX() - mouseX) / (double)MapRenderer.MAP_SIZE) * PAN_SPEED / zoom)));
			yPos = Math.min(1.0, Math.max(0.0,  yPos - (((double)(e.getY() - mouseY) / (double)MapRenderer.MAP_SIZE) * PAN_SPEED / zoom)));
			mouseX = e.getX();
			mouseY = e.getY();
			repaint();
		}
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		repaint();
	}
	
	@Override
	public void componentShown(ComponentEvent e)
	{
		repaint();
	}
	
	@Override public void mouseClicked(MouseEvent e) { }
	@Override public void componentMoved(ComponentEvent e) { }
	@Override public void componentHidden(ComponentEvent e) { }
	@Override public void mouseMoved(MouseEvent e) { }	
	
	@Override
    protected void paintComponent(Graphics g) 
    {
    	super.paintComponent(g);
    	renderer.paintMap(this, (Graphics2D)g, xPos, yPos, zoom);
    	if (mouseDownMiddle)
    	{
    		g.setColor(OVERLAY_COLOR);
    		g.fillRect(0,0,this.getWidth(),this.getHeight());
    	}
    }

	private class RadialMenuItem
	{
		public Image image;
		public String description;
		public Rectangle bounds;
	}




}
