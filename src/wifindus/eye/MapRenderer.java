package wifindus.eye;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import wifindus.GPSRectangle;
import wifindus.HighResolutionTimerListener;
import wifindus.MathHelper;
import wifindus.eye.Incident.Type;

public class MapRenderer implements EyeApplicationListener, NodeEventListener,
	IncidentEventListener, DeviceEventListener, HighResolutionTimerListener
{
	public static final String TYPE_SATELLITE = "satellite";
	public static final String TYPE_ROADMAP = "roadmap";
	public static final String TYPE_TERRAIN = "terrain";
	public static final String TYPE_HYBRID = "hybrid";
	public static final double ZOOM_SPEED = 2.0;
	public static final double ZOOM_MAX = 10.0;
	public static final double ZOOM_MIN = 0.25;
	public static final int BACKGROUND_LEVELS = 3;
	private volatile Map<JComponent, ClientSettings> clients = new HashMap<>();
	private int gridRows = 10;
	private int gridColumns = 10;
	private final ArrayList<MappableObject> devices = new ArrayList<>();
	private final ArrayList<MappableObject> incidents = new ArrayList<>();
	private final ArrayList<MappableObject> nodes = new ArrayList<>();
	private final ArrayList<MappableObject> hover = new ArrayList<>();
	private final ArrayList<MappableObject> selected = new ArrayList<>();
	private JComponent lastClient = null;
	private ClientSettings lastSettings = null;
	private final MapTile[] backgrounds = new MapTile[BACKGROUND_LEVELS];
	private final double gridScaleX;
	private final double gridScaleY;
	private final MapTile[][] tiles = new MapTile[][]
	{
		new MapTile[1],
		new MapTile[4],
		new MapTile[16],
		new MapTile[64],
	};

	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public MapRenderer(double latitude, double longitude, String apiKey, int gridRows, int gridColumns, double gridScaleX, double gridScaleY)
	{
		//properties
		this.gridRows = gridRows;
		this.gridColumns = gridColumns;
		this.gridScaleX = gridScaleX;
		this.gridScaleY = gridScaleY;
		
		//background tiles
		for (int i = 0; i < BACKGROUND_LEVELS; i++)
			backgrounds[i] = new MapTile(this, latitude, longitude, MapTile.CHUNK_STANDARD_ZOOM-i-1, apiKey, false);
		
		//base level tile
		tiles[0][0] = new MapTile(this, latitude, longitude, MapTile.CHUNK_STANDARD_ZOOM, apiKey, true);
		double longWest = tiles[0][0].getBounds().getNorthWest().getLongitude().doubleValue();
		double latNorth = tiles[0][0].getBounds().getNorthWest().getLatitude().doubleValue();
		
		//sub tiles
		for (int zoom = 1; zoom < tiles.length; zoom++)
		{
			int row = 0, col = 0;
			double tileWidth = tiles[0][0].getBounds().getWidth() / Math.pow(2.0, zoom);
			double tileHeight = tiles[0][0].getBounds().getHeight() / Math.pow(2.0, zoom);
			for (int tile = 0; tile < tiles[zoom].length; tile++)
			{
				tiles[zoom][tile] = new MapTile(this,
					latNorth - tileHeight * (0.5 + (1.0 * (double)row)),
					longWest + tileWidth * (0.5 + (1.0 * (double)col)),
					MapTile.CHUNK_STANDARD_ZOOM+zoom,
					apiKey, true);
				
				col++;
				if (col >= Math.pow(2.0, zoom))
				{
					col = 0;
					row++;
				}
			}
			
		}
		
		//initialise lists with all current nodes, incidents and devices
		devices.addAll(EyeApplication.get().getDevices());
		incidents.addAll(EyeApplication.get().getIncidents());
		nodes.addAll(EyeApplication.get().getNodes());
		
		//attach ourselves as an EyeApplicationListener so we get notified of new
		//devices, incidents and nodes as they are created
		EyeApplication.get().addEyeListener(this);
		EyeApplication.get().addTimerListener(this);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Adds a new render client.
	 * @param client subscribes a client to this MapRenderer's events.
	 */
	public final void addRenderClient(JComponent client)
	{
		if (client == null || clients.containsKey(client))
			return;
		
		ClientSettings settings = new ClientSettings();
		settings.client = client;
		clients.put(client, settings);
		regenerateGeometry(client);
		client.repaint();
	}
	
	/**
	 * Removes an existing render client.
	 * @param client unsubscribes a client from this MapRenderer's events.
	 * Has no effect if this parameter is null, or is not currently subscribed to this object.
	 */
	public final void removeRenderClient(JComponent client)
	{
		if (client == null)
			return;
		ClientSettings settings = clients.get(client);
		if (settings == null)
			return;
		
		settings.objectData.clear();
		clients.remove(client);
		if (client == lastClient)
		{
			lastClient = null;
			lastSettings = null;
		}
	}

	/**
	 * Unsubscribes all render clients from this MapRenderer's events.
	 */
	public final void clearRenderClients()
	{
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
			entry.getValue().objectData.clear();
		clients.clear();
		lastClient = null;
		lastSettings = null;
	}
	
	public final void dispose()
	{
		EyeApplication.get().removeTimerListener(this);
		EyeApplication.get().removeEyeListener(this);
		for (Device device : EyeApplication.get().getDevices())
			device.removeEventListener(this);
		for (Incident incident : EyeApplication.get().getIncidents())
			incident.removeEventListener(this);
		for (Node node : EyeApplication.get().getNodes())
			node.removeEventListener(this);
		clearRenderClients();
		for (int zoom = 0; zoom < tiles.length; zoom++)
			for (int tile = 0; tile < tiles[zoom].length; tile++)
				tiles[zoom][tile].dispose();
	}

	public final void setPan(JComponent client, double xPos, double yPos, boolean interpolated)
	{
		getSettings(client).setPan(xPos, yPos, interpolated);
	}
	
	public final void setPan(JComponent client, MappableObject target, boolean interpolated)
	{
		if (target == null)
			return;
		ClientSettings settings = getSettings(client);
		ClientObjectData data = settings.objectData.get(target);
		if (data == null || data.point == null)
			return;
		getSettings(client).setPan(
			(data.point.x - settings.mapArea.x) / settings.mapArea.width,
			(data.point.y - settings.mapArea.y) / settings.mapArea.height,
			interpolated);
	}
	
	public final void dragPan(JComponent client, double xDelta, double yDelta, boolean interpolated)
	{
		ClientSettings settings = getSettings(client);
		settings.setPan(settings.xPos - (xDelta / settings.mapSize),
			settings.yPos - (yDelta / settings.mapSize),
			interpolated);
	}
	
	public final void setZoom(JComponent client, double zoom, boolean interpolated)
	{
		getSettings(client).relativeZoom(zoom);
	}
	
	public final void dragZoom(JComponent client, double zoomDelta, boolean interpolated)
	{
		ClientSettings settings = getSettings(client);
		settings.relativeZoom(settings.zoomTarget - zoomDelta);
	}
	
	public final void setTileType(JComponent client, String type)
	{
		ClientSettings settings = getSettings(client);
		if (type == null)
			return;
		
		type = type.toLowerCase();
		if (type.equals(settings.tileType)
			|| (!type.equals(TYPE_SATELLITE) && !type.equals(TYPE_ROADMAP)
				&& !type.equals(TYPE_TERRAIN) && !type.equals(TYPE_HYBRID)))
			return;
		settings.tileType = type;
		settings.client.repaint();
	}
	
	public final String getTileType(JComponent client)
	{
		return getSettings(client).tileType;
	}
	
	public final boolean isDrawingGrid(JComponent client)
	{
		return getSettings(client).drawGrid;
	}
	
	public final void setDrawingGrid(JComponent client, boolean draw)
	{
		ClientSettings settings = getSettings(client);
		if (settings.drawGrid == draw)
			return;
		settings.drawGrid = draw;
		settings.client.repaint();
	}
	
	public final boolean isDrawingIncidents(JComponent client)
	{
		return getSettings(client).drawIncidents;
	}
	
	public final void setDrawingIncidents(JComponent client, boolean draw)
	{
		ClientSettings settings = getSettings(client);
		if (settings.drawIncidents == draw)
			return;
		settings.drawIncidents = draw;
		settings.client.repaint();
	}
	
	public final boolean isDrawingDevices(JComponent client)
	{
		return getSettings(client).drawDevices;
	}
	
	public final void setDrawingDevices(JComponent client, boolean draw)
	{
		ClientSettings settings = getSettings(client);
		if (settings.drawDevices == draw)
			return;
		settings.drawDevices = draw;
		settings.client.repaint();
	}
	
	public final boolean isDrawingNodes(JComponent client)
	{
		return getSettings(client).drawNodes;
	}
	
	public final void setDrawingNodes(JComponent client, boolean draw)
	{
		ClientSettings settings = getSettings(client);
		if (settings.drawNodes == draw)
			return;
		settings.drawNodes = draw;
		settings.client.repaint();
	}
	
	public final boolean isDrawingMedical(JComponent client)
	{
		return getSettings(client).drawMedical;
	}
	
	public final void setDrawingMedical(JComponent client, boolean draw)
	{
		ClientSettings settings = getSettings(client);
		if (settings.drawMedical == draw)
			return;
		settings.drawMedical = draw;
		settings.client.repaint();
	}
	
	public final boolean isDrawingSecurity(JComponent client)
	{
		return getSettings(client).drawSecurity;
	}
	
	public final void setDrawingSecurity(JComponent client, boolean draw)
	{
		ClientSettings settings = getSettings(client);
		if (settings.drawSecurity == draw)
			return;
		settings.drawSecurity = draw;
		settings.client.repaint();
	}
	
	public final boolean isDrawingWiFindUs(JComponent client)
	{
		return getSettings(client).drawWiFindUs;
	}
	
	public final void setDrawingWiFindUs(JComponent client, boolean draw)
	{
		ClientSettings settings = getSettings(client);
		if (settings.drawWiFindUs == draw)
			return;
		settings.drawWiFindUs = draw;
		settings.client.repaint();
	}

	public final void regenerateGeometry(JComponent client)
	{
		ClientSettings settings = clients.get(client);
		
		//determine bounds
		settings.mapSize = (double)MapTile.CHUNK_IMAGE_SIZE * 2.0 * settings.zoom;
		if (settings.clientArea == null)
			settings.clientArea = new Rectangle2D.Double();
		settings.clientArea.setRect(0.0,0.0,client.getWidth(),client.getHeight());
		if (settings.mapArea == null)
			settings.mapArea = new Rectangle2D.Double();
		settings.mapArea.setRect(
			(settings.clientArea.width / 2.0) - (settings.mapSize * settings.xPos),
			(settings.clientArea.height / 2.0) - (settings.mapSize * settings.yPos),
			settings.mapSize, settings.mapSize);
		for (int i = 0; i < BACKGROUND_LEVELS; i++)
		{
			if (settings.backgroundAreas[i] == null)
				settings.backgroundAreas[i] = new Rectangle2D.Double();
			double factor = Math.pow(2.0, i);
			settings.backgroundAreas[i].setRect(
				settings.mapArea.x + (settings.mapArea.width / 2.0) - settings.mapArea.width*factor,
				settings.mapArea.y + (settings.mapArea.height / 2.0) - settings.mapArea.height*factor,
				settings.mapArea.width*factor*2.0, settings.mapArea.height*factor*2.0);
		}
		if (settings.shownArea == null)
			settings.shownArea = new Rectangle2D.Double();
		Rectangle2D.Double.intersect(settings.clientArea, settings.mapArea, settings.shownArea);
		
		GPSRectangle bounds = tiles[0][0].getBounds();
		settings.clientAreaGPS  = new GPSRectangle(
				bounds.getNorthWest().getLatitude() + (-settings.mapArea.y / settings.mapSize) * bounds.getHeight(),
				bounds.getNorthWest().getLongitude() + (-settings.mapArea.x / settings.mapSize) * bounds.getWidth(),
				bounds.getSouthEast().getLatitude() + (-settings.mapArea.y / settings.mapSize) * bounds.getHeight(),
				bounds.getSouthEast().getLongitude() + (-settings.mapArea.x / settings.mapSize) * bounds.getWidth());
		
		//grid stuff
		if (settings.gridArea == null)
			settings.gridArea = new Rectangle2D.Double();
		settings.gridArea.width = settings.mapSize * gridScaleX;
		settings.gridArea.height = settings.mapSize * gridScaleY;
		settings.gridArea.x = settings.mapArea.x + (settings.mapArea.width / 2.0) - (settings.gridArea.width / 2.0);
		settings.gridArea.y = settings.mapArea.y + (settings.mapArea.height / 2.0) - (settings.gridArea.height / 2.0);
        settings.gridStepX = settings.gridArea.width / (double)gridColumns;
        settings.gridStepY = settings.gridArea.height / (double)gridRows;
        
        //object locations
        settings.setPoints(devices);
        settings.setPoints(incidents);
        settings.setPoints(nodes);
	}
	
	
	
	public final void paintMap(JComponent client, Graphics2D graphics)
	{
		//sanity checks
		if (graphics == null) 
			throw new NullPointerException("Parameter 'graphics' cannot be null.");
		
		ClientSettings settings = clients.get(client);
		graphics.setFont(settings.font);
			
		//draw map images
		if (settings.drawMapTiles)
		{
			//background tile
			if (settings.drawBackground)
			{
				for (int i = BACKGROUND_LEVELS-1; i >= 0; i--)
					backgrounds[i].paintTile(graphics, settings.tileType, settings.backgroundAreas[i], settings.clientArea);
				graphics.setColor(settings.backgroundOverlayColor);
				graphics.fillRect(0, 0, (int)settings.clientArea.width,  (int)settings.clientArea.height);
			}
			
			if (settings.zoom >= 5)
				paintTilesAtZoomLevel(graphics, settings, 3);
			else if (settings.zoom >= 1.5)
				paintTilesAtZoomLevel(graphics, settings, 2);
			else if (settings.zoom >= 0.75)
				paintTilesAtZoomLevel(graphics, settings, 1);
			else
				paintTilesAtZoomLevel(graphics, settings, 0);
		}
		
		//draw grid
		if (settings.drawGrid)
		{
	        //bounding box
	        graphics.setStroke(settings.gridStroke);
	        graphics.setColor(settings.gridLineColor);
	        graphics.drawRect((int)settings.gridArea.x,
	        		(int)settings.gridArea.y,
	        		(int)settings.gridArea.width,
	        		(int)settings.gridArea.height);
	        
	        //metrics etc
	        FontMetrics metrics = graphics.getFontMetrics();
	        //rows
	        for (int i = 0; i < gridRows; i++)
	        {
	        	int lineY = (int)(settings.gridArea.y + settings.gridStepY * (double)(i + 1));
	        	if (lineY < settings.shownArea.y)
	        		continue;
	        	if (lineY > (settings.shownArea.y+settings.shownArea.height))
	        		break;
				char letter = (char) ('A' + i);
				String label = "" + letter;
				int stringW = metrics.stringWidth(label);
				int stringH = metrics.getAscent() + metrics.getDescent();
				int labelSize = (int)(Math.max(stringW,stringH) * 1.2);
				int labelX = settings.gridArea.x > labelSize ? (int)(settings.gridArea.x-labelSize) : 0;
				int labelY = lineY - (int)(settings.gridStepY/2.0);
	        	
	        	//text shadow
		        graphics.setColor(settings.gridShadingColor);
		        graphics.fillOval(labelX,
		        		labelY-(labelSize/2),
		        		labelSize, labelSize);
	        	
	        	//line
		        if (i < gridRows-1)
		        {
		        	graphics.setColor(settings.gridLineColor);
	        		graphics.drawLine((int)settings.gridArea.x, lineY, (int)(settings.gridArea.x+settings.gridArea.width), lineY);
		        }
	        	
	        	//text
	        	graphics.setColor(settings.gridTextColor);
				graphics.drawString(label,
						labelX+(labelSize/2)-(stringW/2),
						(labelY - (stringH/2) + metrics.getAscent()));
	        }
	        
	        //columns
	        for (int i = 0; i < gridColumns; i++)
	        {
	        	int lineX = (int)(settings.gridArea.x + settings.gridStepX * (double)(i + 1));
	        	if (lineX < settings.shownArea.x)
	        		continue;
	        	if (lineX > (settings.shownArea.x+settings.shownArea.width))
	        		break;
				String label = Integer.toString(i+1);
				int stringW = metrics.stringWidth(label);
				int stringH = metrics.getAscent() + metrics.getDescent();
				int labelSize = Math.max(stringW,stringH);
				int labelX = lineX - (int)(settings.gridStepX/2.0);
				int labelY = settings.gridArea.y > labelSize ? (int)(settings.gridArea.y-labelSize) : 0;
	        	
	        	//text shadow
		        graphics.setColor(settings.gridShadingColor);
		        graphics.fillOval(labelX-(labelSize/2),
		        		labelY,
		        		labelSize, labelSize);
	        	
	        	//line
		        if (i < gridColumns - 1)
		        {
		        	graphics.setColor(settings.gridLineColor);
		        	graphics.drawLine(lineX,(int)settings.gridArea.y,lineX,(int)(settings.gridArea.y+settings.gridArea.height));
		        }

	        	//text
	        	graphics.setColor(settings.gridTextColor);
				graphics.drawString(label,
						labelX-(stringW/2),
						(labelY - (stringH/2) + metrics.getAscent()) + labelSize/2);
	        }
		}
		
		ArrayList<MappableObject> sortedObjects = new ArrayList<>();
		//draw layers
		if (settings.drawNodes && settings.drawWiFindUs)
			sortedObjects.addAll(nodes);
		if (settings.drawDevices)
		{
			for (MappableObject object : devices)
			{
				Device device = (Device)object;
				if (device.isTimedOut())
					continue;
				if ((device.getCurrentUserType() == Type.Medical && settings.drawMedical)
					|| (device.getCurrentUserType() == Type.Security && settings.drawSecurity)
					|| (device.getCurrentUserType() == Type.WiFindUs && settings.drawWiFindUs)
					|| (device.getCurrentUserType() == Type.None && settings.drawUnknown))
					sortedObjects.add(object);
			}
		}
		if (settings.drawIncidents)
		{
			for (MappableObject object : incidents)
			{
				Incident incident = (Incident)object;
				if ((incident.getType() == Type.Medical && settings.drawMedical)
					|| (incident.getType() == Type.Security && settings.drawSecurity)
					|| (incident.getType() == Type.WiFindUs && settings.drawWiFindUs))
					sortedObjects.add(object);
			}
		}
		Collections.sort(sortedObjects, MarkerLatitudeComparator);
			paintObjects(graphics, sortedObjects, settings, false);
	
	}
	
	public static void paintMarkerText(Graphics2D graphics, int x, int y, final String s)
	{
    	FontMetrics metrics = graphics.getFontMetrics();
    	int stringH =  metrics.getDescent();
    	int stringX = x-metrics.stringWidth(s)/2;
    	graphics.setColor(Color.BLACK);
    	graphics.fillRect(stringX-2, y-metrics.getAscent()-4+metrics.getLeading(), metrics.stringWidth(s)+4, metrics.getAscent()+2);
    	graphics.setColor(Color.WHITE);
		graphics.drawString(s, stringX, y-stringH);
	}
	
	@Override
	public void deviceTimedOut(Device device)
	{
		repaintDevices();
	}

	@Override
	public void deviceInUse(Device device, User user)
	{
		repaintDevices();
	}

	@Override
	public void deviceNotInUse(Device device, User oldUser)
	{
		repaintDevices();
	}

	@Override
	public void deviceLocationChanged(Device device, Location oldLocation,
			Location newLocation)
	{
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
			entry.getValue().setPoint(device);
		repaintDevices();
	}

	@Override
	public void deviceAssignedIncident(Device device, Incident incident)
	{
		repaintDevices();
	}

	@Override
	public void deviceUnassignedIncident(Device device, Incident incident)
	{
		repaintDevices();
	}
	
	@Override
	public void incidentArchived(Incident incident)
	{
		removeMappableObject(incident);
		incident.removeEventListener(this);
		repaintClients();
	}

	@Override
	public void incidentAssignedDevice(Incident incident, Device device)
	{
		repaintIncidents();
	}
	
	@Override
	public void incidentUnassignedDevice(Incident incident, Device device)
	{
		repaintIncidents();
	}
	
	@Override
	public void nodeTimedOut(Node node)
	{
		repaintNodes();
	}

	@Override
	public void nodeLocationChanged(Node node, Location oldLocation,
			Location newLocation)
	{
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
			entry.getValue().setPoint(node);
		repaintNodes();
	}

	@Override
	public void deviceCreated(Device device)
	{
		if (devices.contains(device))
			return;
		devices.add(device);
		device.addEventListener(this);
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
			entry.getValue().setPoint(device);
		repaintDevices();
	}

	@Override
	public void incidentCreated(Incident incident)
	{
		if (incidents.contains(incident))
			return;
		incidents.add(incident);
		incident.addEventListener(this);
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
			entry.getValue().setPoint(incident);
		repaintIncidents();
	}

	@Override
	public void nodeCreated(Node node)
	{
		if (nodes.contains(node))
			return;
		nodes.add(node);
		node.addEventListener(this);
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
			entry.getValue().setPoint(node);
		repaintNodes();
	}

	@Override public void incidentDescriptionChanged(Incident incident) { }
	@Override public void incidentArchivedResponderAdded(Incident incident, User user) { }
	@Override public void incidentSeverityChanged(Incident incident, int oldSeverity, int newSeverity){ }
	@Override public void incidentCodeChanged(Incident incident, String oldCode, String newCode) { }
	@Override public void incidentReportingUserChanged(Incident incident, User oldUser, User newUser){ }
	@Override public void deviceAtmosphereChanged(Device device, Atmosphere oldAtmosphere, Atmosphere newAtmosphere) { }
	@Override public void deviceAddressChanged(Device device, InetAddress oldAddress,InetAddress newAddress) { }
	@Override public void deviceUpdated(Device device) { }
	@Override public void nodeVoltageChanged(Node node, Double oldVoltage,	Double newVoltage) { }
	@Override public void nodeUpdated(Node node) { }
	@Override public void nodeAddressChanged(Node node, InetAddress oldAddress,	InetAddress newAddress) { }
	@Override public void userCreated(User user) { }
	
	public void setSelectedObjects(List<MappableObject> objects)
	{
		if (updateList(selected, objects))
			repaintClients();
	}
	
	@SuppressWarnings("unchecked")
	public List<MappableObject> getSelectedObjects()
	{
		return (List<MappableObject>)selected.clone();
	}
	
	public void setHoverObjects(List<MappableObject> objects)
	{
		if (updateList(hover, objects))
			repaintClients();
	}
	
	@SuppressWarnings("unchecked")
	public List<MappableObject> getHoverObjects()
	{
		return (List<MappableObject>)hover.clone();
	}
	
	public List<MappableObject> getObjectsAtPoint(JComponent client, int x, int y)
	{
		ClientSettings settings = getSettings(client);
		List<MappableObject> foundObjects = new ArrayList<>();
		
		if (settings.mapArea.contains(x, y))
		{
			for (Map.Entry<MappableObject, ClientObjectData> entry : settings.objectData.entrySet())
			{
				ClientObjectData data = entry.getValue();
				if (data.hitbox != null && data.hitbox.contains(x, y))
					foundObjects.add(entry.getKey());
			}
		}
		
		return foundObjects;
	}
	
	@Override
	public void timerTick(double deltaTime)
	{
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
		{
			ClientSettings settings = entry.getValue();
			boolean regen = false;
			boolean repaint = false;
			if (!MathHelper.equal(settings.zoomInterp, 1.0))
			{
				settings.zoomInterp += deltaTime * ZOOM_SPEED;
				if (settings.zoomInterp > 1.0)
					settings.zoomInterp = 1.0;
				settings.zoom = MathHelper.coserp(settings.zoomStart, settings.zoomTarget, settings.zoomInterp);
				regen = true;
				repaint = true;
			}
			
			if (!MathHelper.equal(settings.posInterp, 1.0))
			{
				settings.posInterp += deltaTime * ZOOM_SPEED;
				if (settings.posInterp > 1.0)
					settings.posInterp = 1.0;
				settings.xPos = MathHelper.coserp(settings.xPosStart, settings.xPosTarget, settings.posInterp);
				settings.yPos = MathHelper.coserp(settings.yPosStart, settings.yPosTarget, settings.posInterp);
				regen = true;
				repaint = true;
			}
			
			if (regen)
				regenerateGeometry(entry.getKey());
			if (repaint)
				entry.getKey().repaint();
		}
	}
	
	public void repaintClients()
	{	
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
			entry.getKey().repaint();
	}
	
	public Location screenToLocation(JComponent client, int left, int top)
	{
		return screenToLocation(client, (double)left, (double)top);
	}
	
	public Location screenToLocation(JComponent client, Point point)
	{
		return screenToLocation(client, (double)point.x, (double)point.y);
	}
	
	public Location screenToLocation(JComponent client, Point2D.Double point)
	{
		return screenToLocation(client, point.x, point.y);
	}
	
	public Location screenToLocation(JComponent client, double left, double top)
	{
		ClientSettings settings = getSettings(client);
		return new Location(settings.clientAreaGPS.getNorthWest().getLatitude() + settings.clientAreaGPS.getHeight() * (top/client.getHeight()),
				settings.clientAreaGPS.getNorthWest().getLongitude() + settings.clientAreaGPS.getWidth() * (left/client.getWidth())
				);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	private static Comparator<MappableObject> MarkerLatitudeComparator = new Comparator<MappableObject>()
	{
	    @Override
	    public int compare(MappableObject o1, MappableObject o2)
	    {
	    	if (o1.getLocation().getLatitude() == null && o2.getLocation().getLatitude() == null)
	    		return 0;
	    	if (o1.getLocation().getLatitude() != null && o2.getLocation().getLatitude() != null)
	        	return Double.compare(o2.getLocation().getLatitude(), o1.getLocation().getLatitude());
	        return o1.getLocation().getLatitude() != null ? 1 : -1;
	    }
	};
	
	private void paintTilesAtZoomLevel(Graphics2D graphics, ClientSettings settings, int zoomLevel)
	{
		if (zoomLevel == 0)
			tiles[0][0].paintTile(graphics, settings.tileType, settings.mapArea, settings.shownArea);
		else
		{
			for (int tile = 0; tile < tiles[zoomLevel].length; tile++)
			{
				tiles[zoomLevel][tile].paintTile(graphics, settings.tileType,
						tiles[0][0].getBounds().translate(settings.mapArea, tiles[zoomLevel][tile].getBounds()),
						settings.shownArea);
			}
		}
	}
	
	private boolean updateList(List<MappableObject> list, List<MappableObject> input)
	{
		if ((list == null || list.size() == 0) && (input == null || input.size() == 0))
			return false;
		
		if (input == null || input.size() == 0)
		{
			list.clear();
			return true;
		}
		else if (!input.equals(list))
		{
			list.clear();
			list.addAll(input);
			return true;
		}
		return false;
	}
	
	private void removeMappableObject(MappableObject object)
	{
		if (object == null)
			return;
		
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
			entry.getValue().objectData.remove(object);
		
		devices.remove(object);
		incidents.remove(object);
		nodes.remove(object);
		hover.remove(object);
		selected.remove(object);
	}
	
	private ClientSettings getSettings(JComponent client)
	{
		if (client == null)
			throw new NullPointerException("Parameter 'client' cannot be null.");
		
		if (client == lastClient)
			return lastSettings;
		
		ClientSettings settings = clients.get(client);
		if (settings == null)
			throw new IllegalArgumentException("The given client has not been subscribed to this MapRenderer.");
		
		lastClient = client;
		lastSettings = settings;
		return settings;
	}
	
    private void paintObjects(final Graphics2D graphics, 	final Collection<MappableObject> objects,
    		final ClientSettings settings, boolean skipInteractives)
    {
		for (MappableObject object : objects)
		{
			if (skipInteractives && (hover.contains(object) || selected.contains(object)))
				continue;
			paintObject(graphics,object,settings);
		}
    }
	
    private void paintObject(final Graphics2D graphics, final MappableObject object, final ClientSettings settings)
    {
		if (!object.getLocation().hasLatLong())
			return;
		
    	Point2D.Double point = settings.objectData.get(object).point;
		if (point == null || !settings.clientArea.contains(point))
			return;
		
		object.paintMarker(graphics, (int)point.x, (int)point.y, hover.contains(object), selected.contains(object));
    }

	private class ClientObjectData
	{
		public Point2D.Double point = null;
		public Polygon hitbox = null;
	}
	
	private class ClientSettings
	{
		//client
		public JComponent client;
		
		//geometry
		public double xPos = 0.5, xPosStart = 0.5, xPosTarget = 0.5, posInterp = 1.0;
		public double yPos = 0.5, yPosStart = 0.5, yPosTarget = 0.5;
		public double zoom = 1.0, zoomStart = 1.0, zoomTarget = 1.0, zoomInterp = 1.0;
		public final HashMap<MappableObject, ClientObjectData> objectData = new HashMap<>();
		public double mapSize;
		Rectangle2D.Double clientArea;
		Rectangle2D.Double mapArea;
		Rectangle2D.Double shownArea;
		Rectangle2D.Double gridArea;
		Rectangle2D.Double[] backgroundAreas = new Rectangle2D.Double[BACKGROUND_LEVELS];
        public double gridStepX;
        public double gridStepY;
        public GPSRectangle clientAreaGPS;
        
        //cosmetics
        public boolean drawBackground = true;
		public boolean drawMapTiles = true;
		public boolean drawGrid = true;
		public boolean drawNodes = true;
		public boolean drawIncidents = true;
		public boolean drawDevices = true;
		public boolean drawMedical = true;
		public boolean drawSecurity = true;
		public boolean drawWiFindUs = true;
		public boolean drawUnknown = true;
		public Color gridLineColor = new Color(0, 0, 0, 70);
		public Color gridTextColor = new Color(255, 255, 255, 200);
		public Color gridShadingColor = new Color(0, 0, 0, 150);
		public Color backgroundOverlayColor = new Color(255, 255, 255, 25);
		public Stroke gridStroke = new BasicStroke(2);
		public Font font = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 18);
		public String tileType = TYPE_SATELLITE;
		
		public void setPoint(MappableObject object)
        {
			ClientObjectData data = objectData.get(object);
			if (data == null)
				objectData.put(object,data = new ClientObjectData());
			data.point = object.getLocation().hasLatLong() ? tiles[0][0].getBounds().translate(mapArea, object.getLocation()) : null;
			data.hitbox = data.point == null ? null : object.generateMarkerHitbox((int)data.point.x, (int)data.point.y); 
        }
		
        public void setPoints(List<MappableObject> objects)
        {
            for (MappableObject object : objects)
            	setPoint(object);
        }
        
        public void setPan(double x, double y, boolean interpolated)
        {
        	x = Math.min(1.0, Math.max(0.0,x));
        	y = Math.min(1.0, Math.max(0.0,y));
    		if (MathHelper.equal(xPos, x) && MathHelper.equal(yPos, y))
    			return;
        	
    		if (interpolated)
    		{
    			xPosStart = xPos;
    			xPosTarget = x;
    			yPosStart = yPos;
    			yPosTarget = y;
    			posInterp = 0.0;
    		}
    		else
    		{
    			xPosStart = xPos = xPosTarget = x;
    			yPosStart = yPos = yPosTarget = y;
    			posInterp = 1.0;
    			
    			regenerateGeometry(client);
    			client.repaint();
    		}
        }
        
        public void setZoom(double target, boolean interpolated)
        {
        	target = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN,target));
    		if (MathHelper.equal(zoom, target))
    			return;
        	
    		if (interpolated)
    		{
    			zoomStart = zoom;
    			zoomTarget = target;
    			zoomInterp = 0.0;
    		}
    		else
    		{
    			zoomStart = zoom = zoomTarget = target;
    			zoomInterp = 1.0;
    			
    			regenerateGeometry(client);
    			client.repaint();
    		}
        }
        
        public void relativeZoom(double target)
        {
    		if (MathHelper.equal(zoom, target))
    			return;
        	
    		if (zoomInterp >= 1.0)
    			setZoom(target, true);
    		else
    			zoomTarget = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN,target));
        }
	}

    private final void repaintNodes()
    {
		if (clients.size() > 0 && nodes.size() > 0)
			repaintClients();
    }

    private final void repaintIncidents()
    {
		if (clients.size() > 0 && incidents.size() > 0)
			repaintClients();
    }
    
    private final void repaintDevices()
    {
    	if (clients.size() > 0 && devices.size() > 0)
    		repaintClients();
    }

	@Override
	public void deviceSelectionChanged(Device device) {
		// TODO Auto-generated method stub
		
	}
}
