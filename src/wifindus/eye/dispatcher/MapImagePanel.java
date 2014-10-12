package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import wifindus.ConfigFile;
import wifindus.ResourcePool;
import wifindus.eye.Atmosphere;
import wifindus.eye.Device;
import wifindus.eye.DeviceEventListener;
import wifindus.eye.EyeApplication;
import wifindus.eye.EyeApplicationListener;
import wifindus.eye.GPSRectangle;
import wifindus.eye.Incident;
import wifindus.eye.IncidentEventListener;
import wifindus.eye.Location;
import wifindus.eye.Node;
import wifindus.eye.NodeEventListener;
import wifindus.eye.User;

//TODO: implement the EyeApplicationListener
public class MapImagePanel extends JPanel implements EyeApplicationListener,
	NodeEventListener, IncidentEventListener, DeviceEventListener, ComponentListener
{
	private static final long serialVersionUID = -9123187374720204706L;
	private transient ArrayList<Device> devices = new ArrayList<>();
	private transient ArrayList<Incident> incidents = new ArrayList<>();
	private transient ArrayList<Node> nodes = new ArrayList<>();
	private transient Image mapImage;
	private GPSRectangle gpsArea;
	private boolean drawImage = true;
	private boolean drawGrid = true;
	private boolean drawNodes = true;
	private boolean drawIncidents = true;
	private boolean drawDevices = true;
	private boolean drawAssignedDevices = true;
	private boolean drawUnassignedDevices = true;
	private int gridRows = 10;
	private int gridColumns = 10;
	private transient Map<Incident.Type, Image> deviceMarkers = new HashMap<>();
	private transient Map<Incident.Type, Image> deviceMarkersUnavailable = new HashMap<>();
	private transient Map<Incident.Type, Image> incidentMarkers = new HashMap<>();
	private transient Image nodeMarker;
	
	static
	{
		ResourcePool.loadImage("device_marker_wfu", "images/device_marker_wfu.png" );
		ResourcePool.loadImage("device_marker_wfu_na", "images/device_marker_wfu_na.png" );
		ResourcePool.loadImage("device_marker_medical", "images/device_marker_medical.png" );
		ResourcePool.loadImage("device_marker_medical_na", "images/device_marker_medical_na.png" );
		ResourcePool.loadImage("device_marker_security", "images/device_marker_security.png" );
		ResourcePool.loadImage("device_marker_security_na", "images/device_marker_security_na.png" );
		ResourcePool.loadImage("device_marker_unassigned", "images/device_marker_unassigned.png" );
		ResourcePool.loadImage("incident_marker_medical", "images/incident_marker_medical.png" );
		ResourcePool.loadImage("incident_marker_security", "images/incident_marker_security.png" );
		ResourcePool.loadImage("incident_marker_wfu", "images/incident_marker_wfu.png" );
		ResourcePool.loadImage("node_marker_inactive", "images/node_marker_inactive.png" );
		ResourcePool.loadImage("node_marker_active", "images/node_marker_active.png" );
	}
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public MapImagePanel()
	{
		//get application config reference
		ConfigFile config = EyeApplication.get().getConfig();
		
		//load map image if it hasn't been already
		ResourcePool.loadImage("map", config.getString("map.image") );
		mapImage = ResourcePool.getImage("map");
		
		//get grid row and column counts from config 
		gridRows = config.getInt("map.grid_rows");
		gridColumns = config.getInt("map.grid_columns");

		//get marker images
		deviceMarkers.put(Incident.Type.Medical, ResourcePool.getImage("device_marker_medical"));
		deviceMarkers.put(Incident.Type.Security, ResourcePool.getImage("device_marker_security"));
		deviceMarkers.put(Incident.Type.WiFindUs, ResourcePool.getImage("device_marker_wfu"));
		deviceMarkersUnavailable.put(Incident.Type.Medical, ResourcePool.getImage("device_marker_medical_na"));
		deviceMarkersUnavailable.put(Incident.Type.Security, ResourcePool.getImage("device_marker_security_na"));
		deviceMarkersUnavailable.put(Incident.Type.WiFindUs, ResourcePool.getImage("device_marker_wfu_na"));
		deviceMarkersUnavailable.put(Incident.Type.None, ResourcePool.getImage("device_marker_unassigned"));
		incidentMarkers.put(Incident.Type.Medical, ResourcePool.getImage("incident_marker_medical"));
		incidentMarkers.put(Incident.Type.Security, ResourcePool.getImage("incident_marker_security"));
		incidentMarkers.put(Incident.Type.WiFindUs, ResourcePool.getImage("incident_marker_wfu"));
		nodeMarker = ResourcePool.getImage("node_marker_active");
		
		//create GPS rectangle
		gpsArea = new GPSRectangle(
			config.getDouble("map.latitude_start"),
			config.getDouble("map.longitude_start"),
			config.getDouble("map.latitude_end"),
			config.getDouble("map.longitude_end"));
		
		//initialise lists with all current nodes, incidents and devices
		devices.addAll(EyeApplication.get().getDevices());
		incidents.addAll(EyeApplication.get().getIncidents());
		nodes.addAll(EyeApplication.get().getNodes());
		
		//attach ourselves as an EyeApplicationListener so we get notified of new
		//devices, incidents and nodes as they are created
		EyeApplication.get().addEventListener(this);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	public void deviceCreated(Device device)
	{
		if (!devices.contains(device))
			devices.add(device);
		repaintDevices();
	}
	
	@Override
	public void incidentCreated(Incident incident)
	{
		if (!incidents.contains(incident))
			incidents.add(incident);
		repaintIncidents();
	}
	
	@Override
	public void nodeCreated(Node node)
	{
		if (!nodes.contains(node))
			nodes.add(node);
		repaintNodes();
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
	public void deviceLocationChanged(Device device, Location oldLocation, Location newLocation)
	{
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
	public void nodeTimedOut(Node node)
	{
		repaintNodes();
	}
	
	@Override
	public void nodeLocationChanged(Node node, Location oldLocation,Location newLocation)
	{
		repaintNodes();
	}
	
	/**
	 * 
	 * @param drawImage
	 */
	public final void setDrawImage(boolean drawImage)
	{
		if (drawImage != this.drawImage)
		{
			this.drawImage = drawImage;
			repaint();
		}
	}

	/**
	 * @param drawGrid the drawGrid to set
	 */
	public final void setDrawGrid(boolean drawGrid)
	{
		if (drawGrid != this.drawGrid)
		{
			this.drawGrid = drawGrid;
			repaint();
		}
	}

	/**
	 * @param drawNodes the drawNodes to set
	 */
	public final void setDrawNodes(boolean drawNodes)
	{
		if (drawNodes != this.drawNodes)
		{
			this.drawNodes = drawNodes;
			repaintNodes();
		}
	}

	/**
	 * @param drawIncidents the drawIncidents to set
	 */
	public final void setDrawIncidents(boolean drawIncidents)
	{
		if (drawIncidents != this.drawIncidents)
		{
			this.drawIncidents = drawIncidents;
			repaintIncidents();
		}
	}

	/**
	 * @param drawDevices the drawDevices to set
	 */
	public final void setDrawDevices(boolean drawDevices)
	{
		if (drawDevices != this.drawDevices)
		{
			this.drawDevices = drawDevices;
			repaintDevices();
		}
	}

	/**
	 * @param drawAssignedDevices the drawAssignedDevices to set
	 */
	public final void setDrawAssignedDevices(boolean drawAssignedDevices)
	{
		if (drawAssignedDevices != this.drawAssignedDevices)
		{
			this.drawAssignedDevices = drawAssignedDevices;
			repaintDevices();
		}
	}

	/**
	 * @param drawUnassignedDevices the drawUnassignedDevices to set
	 */
	public final void setDrawUnassignedDevices(boolean drawUnassignedDevices)
	{
		if (drawUnassignedDevices != this.drawUnassignedDevices)
		{
			this.drawUnassignedDevices = drawUnassignedDevices;
			repaintDevices();
		}
	}

	/////////////////////////////////////////////////////////////////////
	// UNIMPLEMENTED INTERFACE METHODS
	/////////////////////////////////////////////////////////////////////
	
	//ComponentListener
	@Override public void componentMoved(ComponentEvent e) { }
	@Override public void componentHidden(ComponentEvent e) { }

	//DeviceEventListener
	@Override public void deviceAtmosphereChanged(Device device, Atmosphere oldAtmosphere, Atmosphere newAtmosphere) { }
	@Override public void deviceAddressChanged(Device device, InetAddress oldAddress,InetAddress newAddress) { }
	@Override public void deviceUpdated(Device device) { }

	//IncidentEventListener
	@Override public void incidentArchived(Incident incident) { }
	@Override public void incidentAssignedDevice(Incident incident, Device device) { }
	@Override public void incidentUnassignedDevice(Incident incident, Device device) { }
	
	//NodeEventListener
	@Override public void nodeVoltageChanged(Node node, Double oldVoltage,	Double newVoltage) { }
	@Override public void nodeUpdated(Node node) { }
	@Override public void nodeAddressChanged(Node node, InetAddress oldAddress,	InetAddress newAddress) { }
	
	//EyeApplicationListener
	@Override public void userCreated(User user) { }
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
    @Override
    protected final void paintComponent(Graphics g) 
    {
    	//paint background etc
    	super.paintComponent(g);
    	
    	//determine proportional area
    	int width = getWidth();
    	int height = getHeight();
    	double imageRatio = (double)mapImage.getWidth(null) / (double)mapImage.getHeight(null);
    	double panelRatio = (double)width / (double)height;
    	Rectangle targetArea;
        if (imageRatio < panelRatio) //"narrower" than the panel
        {
        	double delta = 1.0 - (imageRatio / panelRatio);
        	targetArea = new Rectangle(
        		(int)((delta / 2.0) * width),
        		0,
        		(int)((1.0f - delta) * width),
        		height);
        }
        else //"wider" than the panel
        {
        	double delta = 1.0 - (panelRatio / imageRatio);
        	targetArea = new Rectangle(
    			0,
    			(int)((delta / 2.0) * height),
        		width,
        		(int)((1.0f - delta) * height));
        }
        
        //determine marker scale using ratios, constrain to 0.4 - 1.5 times normal size
        double markerScale = Math.max(0.4,  Math.min(1.5, Math.max((double)targetArea.width / 800.0, (double)targetArea.height / 600.0))) * 0.25;
        
        //call paint functions
        if (drawImage)
        	paintMapImage(g, targetArea);
    	if (drawGrid)
    		paintGrid(g, targetArea);
    	if (drawNodes)
    		paintNodes(g, targetArea, markerScale);
    	if (drawDevices)
    		paintDevices(g, targetArea, markerScale);
    	if (drawIncidents)
    		paintIncidents(g, targetArea, markerScale);
    }
    
    protected void paintMapImage(Graphics g, Rectangle targetArea)
    {
    	if (!drawImage || mapImage == null || g == null || targetArea == null)
    		return;
		g.drawImage(
			//source
			mapImage,
			//destination coords
			targetArea.x, targetArea.y, targetArea.x + targetArea.width, targetArea.y + targetArea.height,
			//source coords
			0, 0, mapImage.getWidth(null), mapImage.getHeight(null),
			//observer
			null);
    }
    
    protected void paintGrid(Graphics g, Rectangle targetArea)
    {
    	if (!drawGrid || g == null || targetArea == null)
    		return;
    	
        int wStep = (int)((double)targetArea.width / 10.0);
        int hStep = (int)((double)targetArea.height / 10.0);
        g.setColor(Color.WHITE);
        
        //rows
        for (int i = 0; i < gridRows; i++)
        {
			if (i < gridRows-1)
				g.drawLine(targetArea.x, targetArea.y + hStep * (i + 1), targetArea.x + targetArea.width, targetArea.y + hStep * (i + 1));
			char letter = (char) ('A' + i);
			String label = "" + letter;
			g.drawString(label, targetArea.x + 2, targetArea.y + hStep / 2 + (hStep * i)) ;
        }
        
        //columns
        for (int i = 0; i < gridColumns; i++)
        {
        	if (i < gridColumns-1)
        		g.drawLine(targetArea.x + wStep * (i + 1), targetArea.y, targetArea.x + wStep * (i + 1), targetArea.y + targetArea.height);
			g.drawString(Integer.toString(i+1), targetArea.x + wStep / 2 + (wStep * i),  targetArea.y + 12);
        }
    }
    
    protected void paintNodes(Graphics g, Rectangle targetArea, double markerScale)
    {
    	if (!drawNodes || g == null || targetArea == null)
    		return;
    	
    	for (Node node : nodes)
    	{
    		if (!gpsArea.contains(node.getLocation()))
    			continue;
    		paintMarker(g, nodeMarker, gpsArea.translate(targetArea, node.getLocation()),markerScale );
    	}
    }
    
    protected void paintIncidents(Graphics g, Rectangle targetArea, double markerScale)
    {
    	if (!drawIncidents || g == null || targetArea == null)
    		return;
    	
    	for (Incident incident : incidents)
    	{
    		if (!gpsArea.contains(incident.getLocation()))
    			continue;
    		paintMarker(g, incidentMarkers.get(incident.getType()), gpsArea.translate(targetArea, incident.getLocation()), markerScale );   		
    	}
    }
    
    protected void paintDevices(Graphics g, Rectangle targetArea, double markerScale)
    {
    	if (!drawDevices
    			|| (!drawAssignedDevices && !drawUnassignedDevices) || g == null || targetArea == null)
    		return;
    	
    	for (Device device : devices)
    	{
    		if (!gpsArea.contains(device.getLocation()))
    			continue;
    		
    		Image marker;
    		if (device.getCurrentUser() == null)
    			marker = deviceMarkersUnavailable.get(Incident.Type.None);
    		else if (device.getCurrentIncident() != null)
    			marker = deviceMarkersUnavailable.get(device.getCurrentUser().getType());
    		else
    			marker = deviceMarkers.get(device.getCurrentUser().getType());
    		
    		paintMarker(g, marker, gpsArea.translate(targetArea, device.getLocation()), markerScale ); 
    	}
    }
    
    protected void paintMarker(Graphics g, Image image, Point point, double scale)
    {
    	if (g == null || image == null || point == null || scale <= 0.0)
    		return;
    	
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		int xOffset = -(int)(((double)imageWidth/2.0) * scale);
		int yOffset = -(int)(((double)imageHeight) * scale);

		
		g.drawImage( image,
				//destination coords
				point.x+xOffset, point.y+yOffset,
				point.x+xOffset+(int)(imageWidth*scale),
				point.y+yOffset+(int)(imageHeight*scale),
				
				//source coords
				0,0,imageWidth,imageHeight,
				
				null);
    	
    }
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////

    //quick check to see if it's worth firing a repaint()
    private final void repaintNodes()
    {
		if (nodes.size() > 0)
			repaint();
    }

    //quick check to see if it's worth firing a repaint()
    private final void repaintIncidents()
    {
		if (incidents.size() > 0)
			repaint();
    }
    
    //quick check to see if it's worth firing a repaint()
    private final void repaintDevices()
    {
    	if (devices.size() > 0)
			repaint();
    }
}
	
	
	
	
	
	


