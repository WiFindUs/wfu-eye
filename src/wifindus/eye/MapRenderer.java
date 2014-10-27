package wifindus.eye;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

import wifindus.Debugger;
import wifindus.GPSRectangle;
import wifindus.HighResolutionTimerListener;
import wifindus.MathHelper;

public class MapRenderer implements EyeApplicationListener, NodeEventListener,
	IncidentEventListener, DeviceEventListener, HighResolutionTimerListener
{
	public static final double ZOOM_SPEED = 2.0;
	public static final int CHUNK_IMAGE_SIZE = 640;
	public static final int MAP_SIZE = CHUNK_IMAGE_SIZE * 2;
	private static final double CHUNK_RADIUS = 0.01126;
	private static final double CHUNK_LONG_SCALE = 1.22;
	private static final int CHUNK_MIN_ZOOM = 15;
	private static final String MAPS_URL_BASE = "https://maps.googleapis.com/maps/api/staticmap?";
	private static final File MAPS_DIR = new File("maps/");
	
	private final GPSRectangle bounds;
	private Image mapImage;
	private volatile File mapFile;
	private volatile Map<JComponent, ClientSettings> clients = new HashMap<>();
	private volatile URL mapDownloadURL; 
	private volatile boolean abortThread = false;
	private volatile ImageDownloadWorker downloadThread = null;
	private int gridRows = 10;
	private int gridColumns = 10;
	private final ArrayList<MappableObject> devices = new ArrayList<>();
	private final ArrayList<MappableObject> incidents = new ArrayList<>();
	private final ArrayList<MappableObject> nodes = new ArrayList<>();
	private MappableObject callout = null;
	private final ArrayList<MappableObject> hover = new ArrayList<>();
	private final ArrayList<MappableObject> selected = new ArrayList<>();
	private JComponent lastClient = null;
	private ClientSettings lastSettings = null;

	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public MapRenderer(double latitude, double longitude, int zoom, boolean highRes, String apiKey, String mapType, int gridRows, int gridColumns)
	{
		//properties
		this.gridRows = gridRows;
		this.gridColumns = gridColumns;
		
		//create bounds
		double scaledRadius = CHUNK_RADIUS / Math.pow(2.0, zoom - (double)(zoom < CHUNK_MIN_ZOOM ? CHUNK_MIN_ZOOM : zoom));
		bounds = new GPSRectangle(
				latitude + scaledRadius,
				longitude - (scaledRadius * CHUNK_LONG_SCALE),
			latitude - scaledRadius,
			longitude + (scaledRadius * CHUNK_LONG_SCALE));
		
		//generate urls
		try
		{
			mapDownloadURL = new URL(String.format(
				"%scenter=%.6f,%.6f&zoom=%d&scale=%d&size=%dx%d&key=%s&maptype=%s",
				MAPS_URL_BASE, latitude, longitude, zoom,
				highRes ? 2 : 1, CHUNK_IMAGE_SIZE, CHUNK_IMAGE_SIZE,
				apiKey, mapType));
		}
		catch (MalformedURLException e)
		{
			Debugger.ex(e);
			mapDownloadURL = null;
		}
		mapFile = new File("maps/"
			+ String.format("%.6f_%.6f_%d_%s_%s.png", latitude, longitude, zoom, mapType, highRes ? "high" : "low"));
		
		//load or download image
		Debugger.i("MapRenderer loading... (source: \""+mapFile+"\")");
		if (loadMapImage())
			Debugger.i("Map image loaded OK.");
		else
		{
			Debugger.w("Failed to load map image." + (mapDownloadURL == null ? "" : " Attempting download..."));
			downloadMapImage();
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
		abortThread = true;
		if (mapImage != null)
			mapImage = null;
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
			data.point.x / settings.mapArea.width,
			data.point.y / settings.mapArea.height,
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
		getSettings(client).setZoom(zoom, interpolated);
	}
	
	public final void dragZoom(JComponent client, double zoomDelta, boolean interpolated)
	{
		ClientSettings settings = getSettings(client);
		settings.setZoom(settings.zoomTarget - zoomDelta, interpolated);
	}
	
	public final void regenerateGeometry(JComponent client)
	{
		ClientSettings settings = clients.get(client);
		
		//determine bounds
		settings.mapSize = (double)MAP_SIZE * settings.zoom;
		settings.clientArea = new Rectangle2D.Double(0.0,0.0,client.getWidth(),client.getHeight());
		settings.mapArea = new Rectangle2D.Double(
			(settings.clientArea.width / 2.0) - (settings.mapSize * settings.xPos),
			(settings.clientArea.height / 2.0) - (settings.mapSize * settings.yPos),
			settings.mapSize, settings.mapSize);
		settings.shownArea = new Rectangle2D.Double();
		Rectangle2D.Double.intersect(settings.clientArea, settings.mapArea, settings.shownArea);
		
		//grid stuff
        settings.gridStepX = settings.mapSize / (double)gridColumns;
        settings.gridStepY = settings.mapSize / (double)gridRows;
        
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
		
		//draw map image
		if (settings.drawImage && mapImage != null)
		{
			graphics.drawImage(
				//source image
				mapImage,
				//destination coords
				(int)settings.shownArea.x, (int)settings.shownArea.y, //top left
				(int)(settings.shownArea.x + settings.shownArea.width), (int)(settings.shownArea.y + settings.shownArea.height), //bottom right
				//source coords
				(int)((settings.shownArea.x-settings.mapArea.x)/settings.zoom), (int)((settings.shownArea.y-settings.mapArea.y)/settings.zoom), //top left
				(int)((settings.shownArea.x-settings.mapArea.x+settings.shownArea.width)/settings.zoom), (int)((settings.shownArea.y-settings.mapArea.y+settings.shownArea.height)/settings.zoom), //bottom right
				//observer
				null);
		}
		
		//draw grid
		if (settings.drawGrid)
		{
	        //metrics etc
	        graphics.setFont(settings.gridFont);
	        graphics.setStroke(settings.gridStroke);
	        FontMetrics metrics = graphics.getFontMetrics();
	        
	        //rows
	        for (int i = 0; i < gridRows; i++)
	        {
	        	int lineY = (int)(settings.mapArea.y + settings.gridStepY * (double)(i + 1));
	        	if (lineY < settings.shownArea.y)
	        		continue;
	        	if (lineY > (settings.shownArea.y+settings.shownArea.height))
	        		break;
				char letter = (char) ('A' + i);
				String label = "" + letter;
				int stringW = metrics.stringWidth(label);
				int stringH = metrics.getAscent() + metrics.getDescent();
				int labelSize = Math.max(stringW,stringH);
				int labelX = settings.shownArea.x > labelSize ? (int)(settings.shownArea.x-labelSize) : 0;
				int labelY = lineY - (int)(settings.gridStepY/2.0);
	        	
	        	//text shadow
		        graphics.setColor(settings.gridShadingColor);
		        graphics.fillOval(labelX,
		        		labelY-(labelSize/2),
		        		labelSize, labelSize);
	        	
	        	//line
	        	graphics.setColor(settings.gridLineColor);
	        	if (i < gridRows-1)
	        		graphics.drawLine((int)settings.mapArea.x, lineY, (int)(settings.mapArea.x+settings.mapArea.width), lineY);
	        	
	        	//text
	        	graphics.setColor(settings.gridTextColor);
				graphics.drawString(label,
						labelX+(labelSize/2)-(stringW/2),
						(labelY - (stringH/2) + metrics.getAscent()));
	        }
	        
	        //columns
	        for (int i = 0; i < gridColumns; i++)
	        {
	        	int lineX = (int)(settings.mapArea.x + settings.gridStepX * (double)(i + 1));
	        	if (lineX < settings.shownArea.x)
	        		continue;
	        	if (lineX > (settings.shownArea.x+settings.shownArea.width))
	        		break;
				String label = Integer.toString(i+1);
				int stringW = metrics.stringWidth(label);
				int stringH = metrics.getAscent() + metrics.getDescent();
				int labelSize = Math.max(stringW,stringH);
				int labelX = lineX - (int)(settings.gridStepX/2.0);
				int labelY = settings.shownArea.y > labelSize ? (int)(settings.shownArea.y-labelSize) : 0;
	        	
	        	//text shadow
		        graphics.setColor(settings.gridShadingColor);
		        graphics.fillOval(labelX-(labelSize/2),
		        		labelY,
		        		labelSize, labelSize);
	        	
	        	//line
	        	graphics.setColor(settings.gridLineColor);
	        	if (i < gridColumns-1)
	        		graphics.drawLine(lineX,(int)settings.mapArea.y,lineX,(int)(settings.mapArea.y+settings.mapArea.height));

	        	//text
	        	graphics.setColor(settings.gridTextColor);
				graphics.drawString(label,
						labelX-(stringW/2),
						(labelY - (stringH/2) + metrics.getAscent()) + labelSize/2);
	        }
		}
		
		//draw layers
		if (settings.drawNodes)
			paintObjects(graphics, nodes, settings, true);
		if (settings.drawDevices)
			paintObjects(graphics, devices, settings, true);
		if (settings.drawIncidents)
			paintObjects(graphics, incidents, settings, true);
		if (selected.size() > 0)
			paintObjects(graphics, selected, settings, false);
		if (hover.size() > 0)
			paintObjects(graphics, hover, settings, false);
		
		//draw special "callout"
		if (callout != null)
		{
			graphics.setColor(settings.calloutOverlayColor);
			graphics.fillRect(0, 0, (int)settings.clientArea.width,  (int)settings.clientArea.height);
			paintObject(graphics, callout, settings);
		}		
		
		//draw download notification
		if (downloadThread != null)
		{
			int progressHeight = 30;
			int padding = 3;
			graphics.setColor(settings.gridShadingColor);
			graphics.fillRect(0, (int)settings.clientArea.height-progressHeight, (int)settings.clientArea.width, progressHeight);
			graphics.setColor(settings.gridTextColor);
			graphics.fillRect(padding, (int)(settings.clientArea.height-progressHeight+padding), (int)((settings.clientArea.width-padding*2)*downloadThread.percentage), progressHeight-padding*2);
		}
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
	public void deviceSelectionChanged(Device device)
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
	public void incidentSelectionChanged(Incident incident)
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
	public void nodeSelectionChanged(Node node)
	{
		repaintNodes();	
	}

	@Override
	public void deviceCreated(Device device)
	{
		if (devices.contains(device))
			return;
		devices.add(device);
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
	
	public void setCallout(MappableObject object)
	{
		if (object == callout)
			return;
		callout = object;
		repaintClients();
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
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
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
		if (callout == object)
			callout = null;
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
			if (skipInteractives && (hover.contains(object) || selected.contains(object) || object == callout))
				continue;
			paintObject(graphics,object,settings);
		}
    }
	
    private void paintObject(final Graphics2D graphics, final MappableObject object, final ClientSettings settings)
    {
		if (!object.getLocation().hasLatLong())
			return;
		
    	Point2D.Double point = settings.objectData.get(object).point;
		if (point == null || !settings.shownArea.contains(point))
			return;
		
		object.paintMarker(graphics, (int)point.x, (int)point.y, hover.contains(object), selected.contains(object));
    }
	
	private void setMapImage(Image image)
	{
		if (image == mapImage)
			return;
		mapImage = image;
		repaintClients();
	}
	
	private void repaintClients()
	{	
		for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
			entry.getKey().repaint();
	}

	private void downloadMapImage()
	{
		if (downloadThread != null)
			return;
		downloadThread = new ImageDownloadWorker();
		downloadThread.execute();
		repaintClients();
	}
	
	private boolean loadMapImage()
	{
		if (!mapFile.exists() || !mapFile.isFile())
			return false;

		try
		{
			setMapImage(ImageIO.read(mapFile));
			return true;
		}
		catch (IOException e)
		{
			Debugger.ex(e);
		}
		
		return false;
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
        public double gridStepX;
        public double gridStepY;
        
        //cosmetics
		public boolean drawImage = true;
		public boolean drawGrid = true;
		public boolean drawNodes = true;
		public boolean drawIncidents = true;
		public boolean drawDevices = true;
		public Color gridLineColor = new Color(0, 0, 0, 70);
		public Color gridTextColor = new Color(255, 255, 255, 200);
		public Color gridShadingColor = new Color(0, 0, 0, 150);
		public Stroke gridStroke = new BasicStroke(1);
		public Font gridFont = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 18);
		public Color calloutOverlayColor = new Color(255, 255, 255, 100);
		
		public void setPoint(MappableObject object)
        {
			ClientObjectData data = objectData.get(object);
			if (data == null)
				objectData.put(object,data = new ClientObjectData());
			data.point = object.getLocation().hasLatLong() ? bounds.translate(mapArea, object.getLocation()) : null;
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
        	target = Math.min(4.0, Math.max(0.2,target));
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
	}
	
	private class ImageDownloadWorker extends SwingWorker<Image, Double>
	{
		public volatile int contentLength = -1;
		public volatile int read = 0;
		public volatile double percentage = 0.0;
		
		private String downloadProgressString()
		{
			return String.format("Image downloaded: %d bytes %s",
				read, (contentLength <= -1? "" : String.format("(%.2f%%)", percentage*100.0)));
		}
		
		@Override
		protected Image doInBackground() throws Exception
		{
			Debugger.i("Downloading static maps image from url:\n" + mapDownloadURL.toString());

			//download file into a byte array
			byte[] imageBytes = null;
			InputStream in = null;
			ByteArrayOutputStream baos = null;
			try
			{				
				URLConnection connection = mapDownloadURL.openConnection();
				in = connection.getInputStream();
				contentLength = connection.getContentLength();
				baos = new ByteArrayOutputStream(contentLength == -1 ? 1048576 : contentLength);
				int bufSize = 512;
				byte[] buf = new byte[bufSize];
				int readChunk = 0;
			    while (true)
			    {
		        	if (abortThread)
		        		return null;
			    	
			    	int len = in.read(buf);
			        if (len == -1)
			            break;
			        baos.write(buf, 0, len);
			        
			        read += len;
			        readChunk += len;
			        percentage = ((double)read/(double)contentLength);
			        if (readChunk >= 1024*64) //push results every 64k
			        {
			        	readChunk = 0;
			        	Debugger.i(downloadProgressString());
			        	publish(percentage);			        	
			        }
			    }
			    Debugger.i(downloadProgressString());
			    Debugger.i("Map image download complete.");
			    in.close();
			    baos.flush();
			    imageBytes = baos.toByteArray();
			    
			}
			catch (Exception e)
			{
				Debugger.ex(e);
				return null;
			}
			finally
			{
				if (in != null)
				{
					in.close();
					in = null;
				}
				if (baos != null)
					baos.close();
			}
		    
        	if (abortThread)
        		return null;
			
		    //put the download bytes into an image
        	BufferedImage image = null;
        	try
        	{
			    in = new ByteArrayInputStream(imageBytes);
			    image = ImageIO.read(in);
        	}
			catch (Exception e)
			{
				Debugger.ex(e);
				return null;
			}
			finally
			{
				if (in != null)
					in.close();
			}
        	
        	if (abortThread)
        		return null;
		    
		    //write image file to disk
        	try
        	{
        		Debugger.i("Writing download map to disk...");
        		MAPS_DIR.mkdirs();
			    ImageIO.write(image, "png", mapFile);
			    Debugger.i("Image file written OK.");
        	}
			catch (Exception e)
			{
				Debugger.ex(e);
			}
		    
		    return image;
		}
		
		@Override
		protected void process(List<Double> chunks)
		{
			repaintClients();
		}

		@Override
		protected void done()
		{
			downloadThread = null;
			if (abortThread)
        		return;
			try
			{
				setMapImage(get());
			}
			catch (Exception e)
			{
				Debugger.ex(e);
			}
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
}
