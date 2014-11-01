package wifindus.eye;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import wifindus.HighResolutionTimerListener;
import wifindus.MathHelper;
import wifindus.ResourcePool;

public class MapPanel extends JPanel
	implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener, HighResolutionTimerListener
{
	private static final Color OVERLAY_COLOR = new Color(255,255,255,175);
	
	private static final int RADIAL_INCIDENTS = 0;
	private static final int RADIAL_DEVICES = 1;
	private static final int RADIAL_NODES = 2;
	private static final int RADIAL_GRID = 3;
	private static final int RADIAL_TYPE = 4;
	private static final int RADIAL_MEDICAL = 5;
	private static final int RADIAL_SECURITY = 6;
	private static final int RADIAL_WFU = 7;
	
	private static final double RADIAL_MENU_SPEED = 6.0;
	private static final double RADIAL_MENU_SPIN = 1.0;
	private static final int RADIAL_MENU_ITEM_COUNT = 8;
	private static final int RADIAL_MENU_MAX_RADIUS = 140;
	private static final double ZOOM_AMOUNT = 0.2;
	private static final long serialVersionUID = -1496522215279713246L;
	private MapRenderer renderer;
	private boolean mouseEntered = false;
	private boolean mouseDownLeft = false;
	private boolean mouseDownMiddle = false;
	private Point mouseLocation = new Point(), radialMenuLocation = new Point();
	private int radialMenuRadius = 0;
	private RadialMenuItem[] radialMenuItems = new RadialMenuItem[RADIAL_MENU_ITEM_COUNT];
	private RadialMenuItem mouseOverItem = null;
	private double radialMenuInterp = 0.0;
	
	//middle-click menu stuff
	private static final Image radialMenuItemImage, tagImage, pinImage, nodeImage,
		gridImage, satelliteImage, roadImage, crossImage, shieldImage, cogImage,
		radialMenuItemImageHover, offImage, onImage, swapImage;
	static
	{
		radialMenuItemImage = ResourcePool.loadImage("radial_menu_item", "images/radial_menu_item.png" );
		radialMenuItemImageHover = ResourcePool.loadImage("radial_menu_item_hover", "images/radial_menu_item_hover.png" );
		tagImage = ResourcePool.loadImage("tag_white", "images/tag_white.png" );
		pinImage = ResourcePool.loadImage("pin_white", "images/pin_white.png" );
		nodeImage = ResourcePool.loadImage("node_white", "images/node_white.png" );
		gridImage = ResourcePool.loadImage("grid_white", "images/grid_white.png" );
		satelliteImage = ResourcePool.loadImage("satellite_white", "images/satellite_white.png" );
		roadImage = ResourcePool.loadImage("road_white", "images/road_white.png" );
		crossImage = ResourcePool.loadImage("cross_white", "images/cross_white.png" );
		shieldImage = ResourcePool.loadImage("shield_white", "images/shield_white.png" );
		cogImage = ResourcePool.loadImage("cog_white", "images/cog_white.png" );
		
		offImage = ResourcePool.loadImage("off", "images/off.png" );
		onImage = ResourcePool.loadImage("on", "images/on.png" );
		swapImage = ResourcePool.loadImage("swap", "images/swap.png" );
	}
	
	public MapPanel(final MapRenderer renderer)
	{
		setRenderer(renderer);
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		setBackground(Color.DARK_GRAY);
	
		radialMenuItems[RADIAL_INCIDENTS] = new RadialMenuItem(tagImage, "Toggle the display\nof Incidents") {
			@Override public void onClick() { renderer.setDrawingIncidents(MapPanel.this, !renderer.isDrawingIncidents(MapPanel.this)); }
		};
		radialMenuItems[RADIAL_DEVICES] = new RadialMenuItem(pinImage, "Toggle the display\nof Devices") {
			@Override public void onClick() { renderer.setDrawingDevices(MapPanel.this, !renderer.isDrawingDevices(MapPanel.this)); }
		};
		radialMenuItems[RADIAL_NODES] = new RadialMenuItem(nodeImage, "Toggle the display\nof Mesh Nodes") {
			@Override public void onClick() { renderer.setDrawingNodes(MapPanel.this, !renderer.isDrawingNodes(MapPanel.this)); }
		};
		radialMenuItems[RADIAL_GRID] = new RadialMenuItem(gridImage, "Toggle the grid") {
			@Override public void onClick() { renderer.setDrawingGrid(MapPanel.this, !renderer.isDrawingGrid(MapPanel.this)); }
		};
		radialMenuItems[RADIAL_TYPE] = new RadialMenuItem(satelliteImage, "Switch between\nSatellite and Roadmap") {
			@Override public void onClick()
			{
				renderer.setTileType(MapPanel.this, renderer.getTileType(MapPanel.this).equals(MapRenderer.TYPE_SATELLITE)
					? MapRenderer.TYPE_ROADMAP : MapRenderer.TYPE_SATELLITE);
				}
		};
		radialMenuItems[RADIAL_TYPE].adornment = swapImage;
		radialMenuItems[RADIAL_MEDICAL] = new RadialMenuItem(crossImage, "Toggle the display\nof Medical items") {
			@Override public void onClick() { renderer.setDrawingMedical(MapPanel.this, !renderer.isDrawingMedical(MapPanel.this)); }
		};
		radialMenuItems[RADIAL_SECURITY] = new RadialMenuItem(shieldImage, "Toggle the display\nof Security items") {
			@Override public void onClick() { renderer.setDrawingSecurity(MapPanel.this, !renderer.isDrawingSecurity(MapPanel.this)); }
		};
		radialMenuItems[RADIAL_WFU] = new RadialMenuItem(cogImage, "Toggle the display\nof WFU items") {
			@Override public void onClick() { renderer.setDrawingWiFindUs(MapPanel.this, !renderer.isDrawingWiFindUs(MapPanel.this)); }
		};
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
		updateMouseLocation(e);
		if (e.getButton() == MouseEvent.BUTTON1)
			mouseDownLeft = true;
		else if (e.getButton() == MouseEvent.BUTTON2)
		{
			EyeApplication.get().addTimerListener(this);
			mouseDownMiddle = true;
			showRadialMenu(mouseLocation.x,mouseLocation.y);
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (mouseDownMiddle)
			return;
		
		if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) 
			renderer.dragZoom(this, e.getWheelRotation() * ZOOM_AMOUNT, true);
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		updateMouseLocation(e);
		if (e.getButton() == MouseEvent.BUTTON1)
			mouseDownLeft = false;
		else if (e.getButton() == MouseEvent.BUTTON2)
		{
			if (mouseOverItem != null)
			{
				mouseOverItem.onClick();
				mouseOverItem = null;
			}
			mouseDownMiddle = false;
			EyeApplication.get().removeTimerListener(this);
			repaint();
		}
	}	

	@Override
	public void mouseEntered(MouseEvent e)
	{
		mouseEntered = true;
		updateMouseLocation(e);
	}
	
	@Override
	public void mouseExited(MouseEvent e)
	{
		mouseEntered = false;
		updateMouseLocation(e);
	}
	
	@Override
	public void mouseDragged(MouseEvent e)
	{
		int x = mouseLocation.x, y = mouseLocation.y;
		updateMouseLocation(e);
		if (mouseDownLeft && !mouseDownMiddle)
			renderer.dragPan(this,(e.getX() - x), (e.getY() - y), false);
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		renderer.regenerateGeometry(this);
		repaint();
	}
	
	@Override
	public void componentShown(ComponentEvent e)
	{
		repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		updateMouseLocation(e);
		if (e.getButton() == MouseEvent.BUTTON1 && !mouseDownMiddle)
			renderer.setSelectedObjects(renderer.getObjectsAtPoint(this, e.getX(), e.getY()));
	}

	@Override public void componentMoved(ComponentEvent e) { }
	@Override public void componentHidden(ComponentEvent e) { }
	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		updateMouseLocation(e);
		if (mouseEntered && !mouseDownMiddle)
			renderer.setHoverObjects(renderer.getObjectsAtPoint(this, e.getX(), e.getY()));
	}
	
	public void setPan(MappableObject object, boolean interpolated)
	{
		renderer.setPan(this, object, interpolated);
	}
	
	public void setZoom(double zoom, boolean interpolated)
	{
		renderer.setZoom(this, zoom, interpolated);
	}
	
	@Override
	public void timerTick(double deltaTime)
	{
		if (mouseDownMiddle && !MathHelper.equal(radialMenuInterp, 1.0))
		{
			radialMenuInterp += deltaTime * RADIAL_MENU_SPEED;
			if (radialMenuInterp > 1.0)
				radialMenuInterp = 1.0;
			positionRadialMenuItems();
			repaint();
		}
	}
	
	@Override
    protected void paintComponent(Graphics g) 
    {
    	super.paintComponent(g);
    	renderer.paintMap(this, (Graphics2D)g);
    	if (mouseDownMiddle)
    	{
    		g.setColor(OVERLAY_COLOR);
    		g.fillRect(0,0,this.getWidth(),this.getHeight());
    		paintRadialMenu(g);
    	}
    }
	
	private void updateMouseLocation(MouseEvent e)
	{
		mouseLocation.x = e.getX();
		mouseLocation.y = e.getY();
		
		if (mouseDownMiddle && radialMenuInterp >= 1.0)
		{
			RadialMenuItem overItem = null;
			for (int i = 0; i < RADIAL_MENU_ITEM_COUNT; i++)
			{
				if (radialMenuItems[i].bounds.contains(mouseLocation))
				{
					overItem = radialMenuItems[i];
					break;
				}
			}
			if (overItem != mouseOverItem)
			{
				mouseOverItem = overItem;
				repaint();
			}
		}
	}
	
	private void positionRadialMenuItems()
	{
		int w = (int)(radialMenuItemImage.getWidth(null) * (0.25 + (0.75 * radialMenuInterp)));
		int h = (int)(radialMenuItemImage.getHeight(null) * (0.25 + (0.75 * radialMenuInterp)));
		
		for (int i = 0; i < RADIAL_MENU_ITEM_COUNT; i++)
		{
			double theta = (i / (double)RADIAL_MENU_ITEM_COUNT) * Math.PI * 2;
			radialMenuItems[i].center.setLocation(
				radialMenuLocation.x + (int)(Math.cos(theta + radialMenuInterp * RADIAL_MENU_SPIN) * radialMenuRadius * radialMenuInterp),
				radialMenuLocation.y + (int)(Math.sin(theta + radialMenuInterp * RADIAL_MENU_SPIN) * radialMenuRadius * radialMenuInterp));
			radialMenuItems[i].bounds.setBounds(
				radialMenuItems[i].center.x - w/2,
				radialMenuItems[i].center.y - h/2,
				w, h);
		}
	}
	
	private void showRadialMenu(int x, int y)
	{
		//work out new geometry
		int w = radialMenuItemImage.getWidth(null);
		int h = radialMenuItemImage.getHeight(null);
		radialMenuRadius = Math.min(RADIAL_MENU_MAX_RADIUS,
			(Math.min(getWidth(), getHeight()) / 2)
			- (Math.max(w, h) / 2));
		radialMenuLocation.setLocation(
			Math.min(getWidth()-radialMenuRadius-w/2, Math.max(w/2+radialMenuRadius, x)),
			Math.min(getHeight()-radialMenuRadius-h/2, Math.max(h/2+radialMenuRadius, y)));
		radialMenuInterp = 0.0;
		
		//set state icons
		radialMenuItems[RADIAL_TYPE].image = renderer.getTileType(this).equals(MapRenderer.TYPE_SATELLITE)
			? satelliteImage : roadImage;
		radialMenuItems[RADIAL_GRID].setAdornment(renderer.isDrawingGrid(this));
		radialMenuItems[RADIAL_INCIDENTS].setAdornment(renderer.isDrawingIncidents(this));
		radialMenuItems[RADIAL_DEVICES].setAdornment(renderer.isDrawingDevices(this));
		radialMenuItems[RADIAL_NODES].setAdornment(renderer.isDrawingNodes(this));
		radialMenuItems[RADIAL_MEDICAL].setAdornment(renderer.isDrawingMedical(this));
		radialMenuItems[RADIAL_SECURITY].setAdornment(renderer.isDrawingSecurity(this));
		radialMenuItems[RADIAL_WFU].setAdornment(renderer.isDrawingWiFindUs(this));
		
		//reposition and paint
		positionRadialMenuItems();
		repaint();
	}
	
	private void paintRadialMenu(Graphics g)
	{
		for (int i = 0; i < RADIAL_MENU_ITEM_COUNT; i++)
		{		
			g.drawImage(radialMenuInterp >= 1.0 && radialMenuItems[i].bounds.contains(mouseLocation)
					? radialMenuItemImageHover : radialMenuItemImage,
				radialMenuItems[i].bounds.x,
				radialMenuItems[i].bounds.y,
				radialMenuItems[i].bounds.width,
				radialMenuItems[i].bounds.height,
				null);
			
			int w = (int)(radialMenuItems[i].imageWidth * radialMenuItems[i].imageScaleFactor * (0.25 + (0.75 * radialMenuInterp)));
			int h = (int)(radialMenuItems[i].imageHeight * radialMenuItems[i].imageScaleFactor * (0.25 + (0.75 * radialMenuInterp)));
			g.drawImage(radialMenuItems[i].image,
				radialMenuItems[i].center.x - w/2,
				radialMenuItems[i].center.y - h/2,
				w,
				h,
				null);
			
			if (radialMenuInterp >= 1.0 && radialMenuItems[i].adornment != null)
				g.drawImage(radialMenuItems[i].adornment,
						(int)(radialMenuItems[i].center.x + (radialMenuItems[i].bounds.width * 0.2)),
						(int)(radialMenuItems[i].center.y + (radialMenuItems[i].bounds.width * 0.2)),
						null);
		}
		
		if (radialMenuInterp >= 1.0 && mouseOverItem != null)
		{
        	String[] lines = mouseOverItem.description.split("[\\n\\r]+");
        	FontMetrics metrics = g.getFontMetrics();
        	int stringH = metrics.getAscent() + metrics.getDescent();
        	g.setColor(Color.BLACK);
        	int y = radialMenuLocation.y - (lines.length * stringH / 2);
        	for (int i = 0; i < lines.length; i++)
        	{
        		g.drawString(lines[i],
        			radialMenuLocation.x-(metrics.stringWidth(lines[i])/2),
        			(y - (stringH/2) + metrics.getAscent()));
        		y += stringH;
        	}
			
		}
	}

	private abstract class RadialMenuItem
	{
		public Image image;
		public String description;
		public Rectangle bounds = new Rectangle();
		public Point center = new Point();
		public int imageWidth, imageHeight;
		public double imageScaleFactor;
		public Image adornment = null;
		
		public RadialMenuItem(Image image, String description)
		{
			this.image = image;
			this.description = description;
			
			imageWidth = image.getWidth(null);
			imageHeight = image.getHeight(null);
			
			imageScaleFactor = imageWidth > imageHeight ?
				(radialMenuItemImage.getWidth(null) * 0.7) / imageWidth :
				(radialMenuItemImage.getHeight(null) * 0.7) / imageHeight;
		}
		
		public void setAdornment(boolean onCondition)
		{
			adornment = onCondition ? onImage : offImage;
		}
		
		public abstract void onClick();
	}
}

