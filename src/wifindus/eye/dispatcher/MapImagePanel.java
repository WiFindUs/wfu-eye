package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import wifindus.Debugger;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import wifindus.eye.Node;

import wifindus.eye.Location;


public class MapImagePanel extends JPanel implements ComponentListener
{
	private transient static MapImagePanel singleton;
	
	// Images for the display
    static BufferedImage mapImage, medicalIcon, securityIcon, wfuIcon,
    unavailibleMedicalIcon,  unavailibleSecurityIcon, unassignedDeviceIcon,
    medicalIncidentIcon, securityIncidentIcon,
    activeNodeIcon, inactiveNodeIcon;
    
    // Paths for images used in display
    static String path,medicalIconPath,
	securityIconPath,
	wfuIconPath,
	unavailibleMedicalIconPath,
	unavailibleSecurityIconPath,
	unassignedDeviceIconPath,
	medicalIncidentIconPath,
	securityIncidentIconPath,
	activeNodeIconPath,
	inactiveNodeIconPath;
    
    //Used for updating the panel when a user is moved (There is without doubt a better way of doing this)
	static updatePanel updateDisplay;
	static boolean updated;
	
    BufferedImage scaledImage;
    static resizedImage resize;
    static Location starts;
	static Location ends;
	
	static Device device;
	static Location oldLocation,  newLocation, nodeLocation, incidentLocation;
	
	static Node node;
	
	static Incident incident;
		
	ImageIcon ii;
	
	static Map<String, Device> devices = new TreeMap<String, Device>();
	static Map<String, Point> deviceLocations = new TreeMap<String, Point>();

	 
	static Map<String, Node> nodes = new TreeMap<String, Node>();
	static Map<String, Point> nodeLocations = new TreeMap<String, Point>();
	
	static Map<Integer, Incident> incidents = new TreeMap<Integer, Incident>();
	static Map<Integer, Point> incidentLocations = new TreeMap<Integer, Point>();
	
	public static boolean displayGrid = false;
	
	
	
    public MapImagePanel() 
    {
    	updateDisplay = new updatePanel();
    	updated = false;
    	
    	addComponentListener(this);
    	path = EyeApplication.get().getConfig().getString("map.image");
    	resize = new resizedImage();
    	
    	// Set image paths
    	medicalIconPath = "images/medical_marker.png";
    	securityIconPath = "images/security_marker.png";
    	wfuIconPath = "images/wfu_marker.png";
        unavailibleMedicalIconPath = "images/medical_marker_unavailible.png";
        unavailibleSecurityIconPath = "images/security_marker_unavailible.png";
        unassignedDeviceIconPath = "images/unassigned_device_marker.png";
        medicalIncidentIconPath = "images/medical_incident_marker.png";
        securityIncidentIconPath = "images/security_incident_marker.png";
        activeNodeIconPath = "images/node_marker_active.png";
        inactiveNodeIconPath = "images/node_marker_inactive.png";
    	
        // Initial scale of images
        mapImage = resize.scaleImage(1052, 871, path);
        medicalIcon = resize.scaleImage(87, 201, medicalIconPath);
       	securityIcon = resize.scaleImage(87, 201, securityIconPath);
       	wfuIcon = resize.scaleImage(87, 201, wfuIconPath);
       	unavailibleMedicalIcon = resize.scaleImage(87, 201, unavailibleMedicalIconPath);
       	unavailibleSecurityIcon = resize.scaleImage(87, 201, unavailibleSecurityIconPath);
       	unassignedDeviceIcon = resize.scaleImage(87, 201, unassignedDeviceIconPath);
       	medicalIncidentIcon = resize.scaleImage(77, 99, medicalIncidentIconPath);
       	securityIncidentIcon = resize.scaleImage(77, 99, securityIncidentIconPath);
       	activeNodeIcon = resize.scaleImage(77, 99, activeNodeIconPath);
       	inactiveNodeIcon = resize.scaleImage(77, 99, inactiveNodeIconPath);
       
       	//Get latitude and longitude for corners of map
        starts = new Location(EyeApplication.get().getConfig().getDouble("map.latitude_start"),
    		   EyeApplication.get().getConfig().getDouble("map.longitude_start"));
        		Debugger.v("Map Top-Left:" + starts);
       
        ends = new Location(EyeApplication.get().getConfig().getDouble("map.latitude_end"),
    		   EyeApplication.get().getConfig().getDouble("map.longitude_end"));
        		Debugger.v("Map Bottom-Right:" + ends);
    }
    
	////////////////////////////////////////////////////////////////
    // Move map markers when the device location changes
	////////////////////////////////////////////////////////////////
    public static void deviceLocationChanged(Device localDevice, Location localOldLocation, Location localNewLocation)
    {
    	device = localDevice;
    	oldLocation = localOldLocation;
    	newLocation = localNewLocation;
    	
    	try
    	{
    		//Vertical Position
    		Double deviceLatDifference = newLocation.getLatitude() - starts.getLatitude();
    		Double latDifference = starts.getLatitude() - ends.getLatitude();
    		Double deviceLatPositionPercentage =  Math.abs(deviceLatDifference) / latDifference;
    		Double deviceVerticalPosition = mapImage.getHeight() * deviceLatPositionPercentage;
    	
    		//Horizontal Position
    		Double deviceLongDifference = newLocation.getLongitude() - starts.getLongitude();
    		Double longDifference = starts.getLongitude() - ends.getLongitude();
    		Double deviceLongPositionPercentage =  Math.abs(deviceLongDifference) / longDifference;
    		Double deviceHorizontalPosition = mapImage.getWidth() * deviceLongPositionPercentage;
    	
    		Point deviceLocation = new Point((int) Math.abs(deviceHorizontalPosition), (int)Math.abs(deviceVerticalPosition));
  
    		//The map doesn't actually need resizing, we just want to get the panel repainted to reflect the changes 
    		//mapImage = resize.scaleImage(mapImage.getWidth(), mapImage.getHeight(), path);
    		updated = updateDisplay.updateUserMovement();
    		
    		devices.put(device.getHash(), device); //Should replace existing value with the same hash
    		deviceLocations.put(device.getHash(), deviceLocation);
    	}
    	catch(NullPointerException e)
    	{
    		
    	}
    }
    
    
    
	////////////////////////////////////////////////////////////////
    // Place Incident markers on the map when incidents are created
	////////////////////////////////////////////////////////////////
	public static void incidentCreated(Incident localIncident, Device localDevice)
	{
    	incident = localIncident;
    	incidentLocation = incident.getLocation();
		incidents.put(incident.getID(), incident); //Should replace existing value with the same hash
    	
		System.out.println("CREATED AN INCIDENT  " + incident + "   "+incidentLocation);
		
    	try
    	{
    		//Vertical Position
    		Double deviceLatDifference = incidentLocation.getLatitude() - starts.getLatitude();
    		Double latDifference = starts.getLatitude() - ends.getLatitude();
    		Double deviceLatPositionPercentage =  Math.abs(deviceLatDifference) / latDifference;
    		Double deviceVerticalPosition = mapImage.getHeight() * deviceLatPositionPercentage;
    	
    		//Horizontal Position
    		Double deviceLongDifference = incidentLocation.getLongitude() - starts.getLongitude();
    		Double longDifference = starts.getLongitude() - ends.getLongitude();
    		Double deviceLongPositionPercentage =  Math.abs(deviceLongDifference) / longDifference;
    		Double deviceHorizontalPosition = mapImage.getWidth() * deviceLongPositionPercentage;
    	
    		Point deviceLocation = new Point((int) Math.abs(deviceHorizontalPosition), (int)Math.abs(deviceVerticalPosition));
  
    		//Update display to reflect change
    		updated = updateDisplay.updateUserMovement();
    		incidentLocations.put(incident.getID(), deviceLocation);
    	}
    	catch(NullPointerException e)
    	{
    	}
	}

    
	////////////////////////////////////////////////////////////////
	// Place Node markers on the map when nodes are created
	////////////////////////////////////////////////////////////////
	public static void nodeCreated(Node localNode, Location localNodeLocation)
	{
		
    	node = localNode;
    	nodeLocation = node.getLocation();
		nodes.put(node.getHash(), node); //Should replace existing value with the same hash
    	
    	try
    	{
    		//Vertical Position
    		Double deviceLatDifference = nodeLocation.getLatitude() - starts.getLatitude();
    		Double latDifference = starts.getLatitude() - ends.getLatitude();
    		Double deviceLatPositionPercentage =  Math.abs(deviceLatDifference) / latDifference;
    		Double deviceVerticalPosition = mapImage.getHeight() * deviceLatPositionPercentage;
    	
    		//Horizontal Position
    		Double deviceLongDifference = nodeLocation.getLongitude() - starts.getLongitude();
    		Double longDifference = starts.getLongitude() - ends.getLongitude();
    		Double deviceLongPositionPercentage =  Math.abs(deviceLongDifference) / longDifference;
    		Double deviceHorizontalPosition = mapImage.getWidth() * deviceLongPositionPercentage;
    	
    		Point deviceLocation = new Point((int) Math.abs(deviceHorizontalPosition), (int)Math.abs(deviceVerticalPosition));
  
    		//Update display to reflect change
    		updated = updateDisplay.updateUserMovement();
    		
    		nodeLocations.put(node.getHash(), deviceLocation);
    	}
    	catch(NullPointerException e)
    	{
    		
    	}
	}
    
	////////////////////////////////////////////////////////////////
    //Toggle Grid on and off
	////////////////////////////////////////////////////////////////
    public static void toggleGrid(boolean toggle)
    {
    	displayGrid = toggle;
    }
    
	////////////////////////////////////////////////////////////////
    // paint component
	////////////////////////////////////////////////////////////////
    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        g.drawImage(mapImage, 0, 0, null); 
        
        
    	///////////////////////////////////////////////////////////////////////////////////
        //Grid Lines
        if(displayGrid == true)
        {
        int wStep = 0;
        int hStep = 0;
        g.setColor(Color.WHITE);
        try
        {
        	 wStep = mapImage.getWidth()/10;
        	 hStep = mapImage.getHeight()/10;
        
        	 for (int i = 0; i < 10; i++)
        	 {
        		 //Horizontal
        		 g.drawLine(0, hStep * (i + 1), mapImage.getWidth(), hStep * (i + 1));
        	
        		 char letter = (char) ('A' + i);
        		 String label = "" + letter;
        		 g.drawString(label, 2, hStep / 2 + (hStep * i)) ;
        	
        		 //Vertical
        		 g.drawLine(wStep * (i + 1),0, wStep * (i + 1), mapImage.getHeight());
        		 g.drawString(Integer.toString(i+1), wStep / 2 + (wStep * i), 12);
        	 }
        }
        catch(NullPointerException e)
        {
        	
        }
        }
        
        
        //////////////////////////////////////////////////////////////////////////////////////
        // Draw devices on the map
    	//////////////////////////////////////////////////////////////////////////////////////
        for(Map.Entry<String,Device> currentDevice : devices.entrySet()) 
		{
        	if(currentDevice.getValue().getCurrentUser() != null)
        	{
        		if(currentDevice.getValue().getCurrentUser().getType().toString().equals("Medical"))
        		{
        			g.drawImage(medicalIcon, (deviceLocations.get(currentDevice.getKey()).x)- medicalIcon.getWidth() / 2, (deviceLocations.get(currentDevice.getKey()).y) - medicalIcon.getHeight(), null); 
        		}
        		else if(currentDevice.getValue().getCurrentUser().getType().toString().equals("Security"))
        		{
        			g.drawImage(securityIcon, (deviceLocations.get(currentDevice.getKey()).x)- securityIcon.getWidth() / 2, (deviceLocations.get(currentDevice.getKey()).y) - securityIcon.getHeight(), null); 
        		}
		
        		else if(currentDevice.getValue().getCurrentUser().getType().toString().equals("WiFindUs"))
        		{
        			g.drawImage(wfuIcon, (deviceLocations.get(currentDevice.getKey()).x)- wfuIcon.getWidth() / 2, (deviceLocations.get(currentDevice.getKey()).y) - wfuIcon.getHeight(), null); 
        		}
        	}
        		else
        	{
            	g.drawImage(unassignedDeviceIcon, (deviceLocations.get(currentDevice.getKey()).x)- unassignedDeviceIcon.getWidth() / 2, (deviceLocations.get(currentDevice.getKey()).y) - unassignedDeviceIcon.getHeight(), null); 
        	}
		
		}
        
        //////////////////////////////////////////////////////////////////////////////////////
        // Draw nodes on the map
        //////////////////////////////////////////////////////////////////////////////////////
        for(Map.Entry<String,Node> currentNode : nodes.entrySet()) 
    		{
				g.drawImage(activeNodeIcon, (nodeLocations.get(currentNode.getKey()).x)- activeNodeIcon.getWidth() / 2, (nodeLocations.get(currentNode.getKey()).y) - activeNodeIcon.getHeight(), null); 
    		}
        

        //////////////////////////////////////////////////////////////////////////////////////
        // Draw incidents on the map
        //////////////////////////////////////////////////////////////////////////////////////
        for(Map.Entry<Integer,Incident> currentIncident : incidents.entrySet()) 
    		{
				g.drawImage(medicalIncidentIcon, (incidentLocations.get(currentIncident.getKey()).x)- medicalIncidentIcon.getWidth() / 2, (incidentLocations.get(currentIncident.getKey()).y) - medicalIncidentIcon.getHeight(), null); 
    		}
        
        
    }

	////////////////////////////////////////////////////////////////
    // create resized image
    ////////////////////////////////////////////////////////////////
	public class resizedImage
	{
		public BufferedImage scaleImage(int width, int height, String filename) 
		{
			scaledImage = null;
		    try 
		    {
		    	scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		    	Graphics2D g = (Graphics2D) scaledImage.createGraphics();
		        g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY));
		        ii = new ImageIcon(filename);
				g.drawImage(ii.getImage(), 0, 0, width, height, null);
		        repaint();
		        revalidate();
		    } 
		    catch (Exception e) 
		    {
		        return scaledImage;
		    }
		    return scaledImage;
		}
	}
	
	
	////////////////////////////////////////////////////////////////
    // Repaint the panel
    ////////////////////////////////////////////////////////////////
	public class updatePanel
	{
		public boolean updateUserMovement()
		{
			repaint();
			revalidate();
			return true;
		}
	}


	
	
	////////////////////////////////////////////////////////////////
	// Component Listeners
	////////////////////////////////////////////////////////////////
	@Override public void componentHidden(ComponentEvent e) { }
	@Override public void componentMoved(ComponentEvent e) { }
	@Override
	public void componentResized(ComponentEvent e) 
	{
		Dimension boundary = new Dimension(getWidth(), getHeight());
		Dimension imgSize = new Dimension(1052 * 2, 871 * 2);
		Dimension d = getScaledDimension(imgSize, boundary);
		mapImage = resize.scaleImage(d.width, d.height, path);
		
		//Person Marker
		Dimension markerBoundary = new Dimension(getWidth()/20, getHeight()/20);
		Dimension personMarkerImgSize = new Dimension(87 * 2, 201 * 2);
		Dimension personMarkerScaled = getScaledDimension(personMarkerImgSize, markerBoundary);
		
		//Nodes and Incident markers
		Dimension incidentNodeMarkerImgSize = new Dimension(77 * 2, 99 * 2);
		Dimension incidentNodeMarkerScaled = getScaledDimension(incidentNodeMarkerImgSize, markerBoundary);

		medicalIcon = resize.scaleImage(personMarkerScaled.width, personMarkerScaled.height, medicalIconPath);
		securityIcon = resize.scaleImage(personMarkerScaled.width, personMarkerScaled.height, securityIconPath);
		wfuIcon = resize.scaleImage(personMarkerScaled.width, personMarkerScaled.height, wfuIconPath);
		unavailibleMedicalIcon = resize.scaleImage(personMarkerScaled.width, personMarkerScaled.height, unavailibleMedicalIconPath);
		unavailibleSecurityIcon = resize.scaleImage(personMarkerScaled.width, personMarkerScaled.height, unavailibleSecurityIconPath);
		unassignedDeviceIcon = resize.scaleImage(personMarkerScaled.width, personMarkerScaled.height, unassignedDeviceIconPath);
		medicalIncidentIcon = resize.scaleImage(incidentNodeMarkerScaled.width, incidentNodeMarkerScaled.height, medicalIncidentIconPath);
		securityIncidentIcon = resize.scaleImage(incidentNodeMarkerScaled.width, incidentNodeMarkerScaled.height, securityIncidentIconPath);
		activeNodeIcon = resize.scaleImage(incidentNodeMarkerScaled.width, incidentNodeMarkerScaled.height, activeNodeIconPath);
		inactiveNodeIcon = resize.scaleImage(incidentNodeMarkerScaled.width, incidentNodeMarkerScaled.height, inactiveNodeIconPath);

        //////////////////////////////////////////////////////////////////////////////////////
		//Resize images to match map
        //////////////////////////////////////////////////////////////////////////////////////

		for(Map.Entry<String,Device> currentDevice : devices.entrySet()) 
		{
			deviceLocationChanged(devices.get(currentDevice.getKey()),devices.get(currentDevice.getKey()).getLocation(),devices.get(currentDevice.getKey()).getLocation());
		}
		
		
		for(Map.Entry<String,Node> currentNode : nodes.entrySet()) 
		{
			nodeCreated(nodes.get(currentNode.getKey()), nodes.get(currentNode.getKey()).getLocation());
		}
		
		
		for(Map.Entry<Integer,Incident> currentIncident: incidents.entrySet()) 
		{
			incidentCreated(incidents.get(currentIncident.getKey()), incidents.get(currentIncident.getKey()).getRespondingDevices().get(0));
		}
		
	
	}
	
	@Override public void componentShown(ComponentEvent e) { }

	
	
	////////////////////////////////////////////////////////////////
	// Scale the image to the new panel size
	////////////////////////////////////////////////////////////////
	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) 
	{
	    int startWidth = imgSize.width;
	    int startHeight = imgSize.height;
	    
	    int boundaryWidth = boundary.width;
	    int boundaryHeight = boundary.height;
	   
	    int newWidth = startWidth;
	    int newHeight = startHeight;

	    // scale width
	    if (startWidth > boundaryWidth) 
	    {
	    	newWidth = boundaryWidth;
	    	newHeight = (newWidth * startHeight) / startWidth;
	    }

	    // scale height
	    if (newHeight > boundaryHeight) 
	    {
	    	newHeight = boundaryHeight;
	        newWidth = (newHeight * startWidth) / startHeight;
	    }
	    return new Dimension(newWidth, newHeight);
	}
}
	
	
	
	
	
	


