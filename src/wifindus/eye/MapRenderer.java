package wifindus.eye;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
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

public class MapRenderer implements EyeApplicationListener, NodeEventListener, IncidentEventListener, DeviceEventListener 
{
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
	private final boolean highRes;
	private final ArrayList<MappableObject> devices = new ArrayList<>();
	private final ArrayList<MappableObject> incidents = new ArrayList<>();
	private final ArrayList<MappableObject> nodes = new ArrayList<>();

	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public MapRenderer(double latitude, double longitude, int zoom, boolean highRes, String apiKey, String mapType, int gridRows, int gridColumns)
	{
		//properties
		this.gridRows = gridRows;
		this.gridColumns = gridColumns;
		this.highRes = highRes;
		
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
		
		clients.put(client, new ClientSettings());
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
		clients.remove(client);
	}
	
	/**
	 * Unsubscribes all render clients from this MapRenderer's events.
	 */
	public final void clearRenderClients()
	{
		clients.clear();
	}
	
	public final void dispose()
	{
		clients.clear();
		abortThread = true;
		if (mapImage != null)
			mapImage = null;
	}
	
	public final void paintMap(JComponent client, Graphics2D graphics, double xPos, double yPos, double zoom)
	{
		//sanity checks
		if (client == null)
			throw new NullPointerException("Parameter 'client' cannot be null.");
		if (graphics == null) 
			throw new NullPointerException("Parameter 'graphics' cannot be null.");
		ClientSettings settings = clients.get(client);
		if (settings == null)
			throw new IllegalArgumentException("The given client has not been subscribed to this MapRenderer.");
		
		//range checks
		xPos = Math.min(1.0, Math.max(0.0,xPos));
		yPos = Math.min(1.0, Math.max(0.0,yPos));
		zoom = Math.min(4.0, Math.max(0.2,zoom));
		
		//determine bounds
		double mapSize = (double)MAP_SIZE * zoom;
		Rectangle2D.Double clientArea = new Rectangle2D.Double(0.0,0.0,client.getWidth(),client.getHeight());
		Rectangle2D.Double mapArea = new Rectangle2D.Double(
			(clientArea.width / 2.0) - (mapSize * xPos),
			(clientArea.height / 2.0) - (mapSize * yPos),
			mapSize, mapSize);
		Rectangle2D.Double shownArea = new Rectangle2D.Double();
		Rectangle2D.Double.intersect(clientArea, mapArea, shownArea);
		
		//draw map image
		if (settings.drawImage && mapImage != null)
		{
			graphics.drawImage(
				//source image
				mapImage,
				//destination coords
				(int)shownArea.x, (int)shownArea.y, //top left
				(int)(shownArea.x + shownArea.width), (int)(shownArea.y + shownArea.height), //bottom right
				//source coords
				(int)((shownArea.x-mapArea.x)/zoom), (int)((shownArea.y-mapArea.y)/zoom), //top left
				(int)((shownArea.x-mapArea.x+shownArea.width)/zoom), (int)((shownArea.y-mapArea.y+shownArea.height)/zoom), //bottom right
				//observer
				null);
		}
		
		//draw grid
		if (settings.drawGrid)
		{
	        double gridStepX = mapSize / (double)gridColumns;
	        double gridStepY = mapSize / (double)gridRows;
	        
	        //metrics etc
	        graphics.setFont(settings.gridFont);
	        graphics.setStroke(settings.gridStroke);
	        FontMetrics metrics = graphics.getFontMetrics();
	        
	        //rows
	        for (int i = 0; i < gridRows; i++)
	        {
	        	int lineY = (int)(mapArea.y + gridStepY * (double)(i + 1));
				char letter = (char) ('A' + i);
				String label = "" + letter;
				int stringW = metrics.stringWidth(label);
				int stringH = metrics.getAscent() + metrics.getDescent();
				int labelSize = Math.max(stringW,stringH);
				int labelX = (int)(mapArea.x+2.0);
				int labelY = lineY - (int)(gridStepY/2.0);
	        	
	        	//text shadow
		        graphics.setColor(settings.gridShadingColor);
		        graphics.fillOval(labelX,
		        		labelY-(labelSize/2),
		        		labelSize, labelSize);
	        	
	        	//line
	        	graphics.setColor(settings.gridLineColor);
	        	if (i < gridRows-1)
	        		graphics.drawLine((int)mapArea.x, lineY, (int)(mapArea.x+mapArea.width), lineY);
	        	
	        	//text
	        	graphics.setColor(settings.gridTextColor);
				graphics.drawString(label,
						labelX+(labelSize/2)-(stringW/2),
						(labelY - (stringH/2) + metrics.getAscent()));
	        }
	        
	        //columns
	        for (int i = 0; i < gridColumns; i++)
	        {
	        	int lineX = (int)(mapArea.x + gridStepX * (double)(i + 1));
				String label = Integer.toString(i+1);
				int stringW = metrics.stringWidth(label);
				int stringH = metrics.getAscent() + metrics.getDescent();
				int labelSize = Math.max(stringW,stringH);
				int labelX = lineX - (int)(gridStepX/2.0);
				int labelY = (int)(mapArea.y+2.0);
	        	
	        	//text shadow
		        graphics.setColor(settings.gridShadingColor);
		        graphics.fillOval(labelX-(labelSize/2),
		        		labelY,
		        		labelSize, labelSize);
	        	
	        	//line
	        	graphics.setColor(settings.gridLineColor);
	        	if (i < gridColumns-1)
	        		graphics.drawLine(lineX,(int)mapArea.y,lineX,(int)(mapArea.y+mapArea.height));

	        	//text
	        	graphics.setColor(settings.gridTextColor);
				graphics.drawString(label,
						labelX-(stringW/2),
						(labelY - (stringH/2) + metrics.getAscent()) + labelSize/2);
	        }
		}
		
		//draw layers
		if (settings.drawNodes)
			paintObjects(graphics, nodes, mapArea, shownArea);
		if (settings.drawDevices)
			paintObjects(graphics, devices, mapArea, shownArea);
		if (settings.drawIncidents)
			paintObjects(graphics, incidents, mapArea, shownArea);
		
		//draw download notification
		if (downloadThread != null)
		{
			int progressHeight = 30;
			int padding = 3;
			graphics.setColor(settings.gridShadingColor);
			graphics.fillRect(0, (int)clientArea.height-progressHeight, (int)clientArea.width, progressHeight);
			graphics.setColor(settings.gridTextColor);
			graphics.fillRect(padding, (int)(clientArea.height-progressHeight+padding), (int)((clientArea.width-padding*2)*downloadThread.percentage), progressHeight-padding*2);
		}
	}
	
	public final void paintMap(JComponent client, Graphics2D graphics)
	{
		paintMap(client, graphics, 0.5, 0.5, 1.0);
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
		repaintIncidents();
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
	
	@Override public void deviceAtmosphereChanged(Device device, Atmosphere oldAtmosphere, Atmosphere newAtmosphere) { }
	@Override public void deviceAddressChanged(Device device, InetAddress oldAddress,InetAddress newAddress) { }
	@Override public void deviceUpdated(Device device) { }
	@Override public void nodeVoltageChanged(Node node, Double oldVoltage,	Double newVoltage) { }
	@Override public void nodeUpdated(Node node) { }
	@Override public void nodeAddressChanged(Node node, InetAddress oldAddress,	InetAddress newAddress) { }
	@Override public void userCreated(User user) { }
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
    private final void paintObjects(final Graphics2D graphics, final Collection<MappableObject> objects, final Rectangle2D.Double mapArea, final Rectangle2D.Double shownArea)
    {
		for (MappableObject object : objects)
			paintObject(graphics,object,mapArea,shownArea);
    }
	
    private final void paintObject(final Graphics2D graphics, final MappableObject object, final Rectangle2D.Double mapArea, final Rectangle2D.Double shownArea)
    {
		if (!object.getLocation().hasLatLong())
			return;
		
    	Point2D.Double point = bounds.translate(mapArea, object.getLocation());
		if (!shownArea.contains(point))
			return;
		
		object.paintMarker(graphics, (int)point.x, (int)point.y);
    }
	
    /*
    private final void paintMarker(Graphics2D graphics,
    		Image image, Location location, Rectangle2D.Double mapArea, Rectangle2D.Double shownArea)
    {
    	int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		int xOffset = -(int)((double)imageWidth/2.0);
		int yOffset = -imageHeight;
		
		graphics.drawImage(
				//source image
				image,
				//destination coords
				(int)point.x+xOffset, (int)point.y+yOffset,
				(int)point.x+xOffset+imageWidth,
				(int)point.y+yOffset+imageHeight,
				//source coords
				0, 0, imageWidth, imageHeight,
				//observer
				null);
    }
    */
	
	private void setMapImage(Image image)
	{
		if (image == mapImage)
			return;
		mapImage = image;
		repaint();
	}
	
	private void repaint(JComponent client)
	{	
		if (client != null) //one client
			client.repaint();
		else //trigger repaint on all subscribed clients
		{
			for (Map.Entry<JComponent, ClientSettings> entry : clients.entrySet())
				entry.getKey().repaint();
		}
	}
	
	private void repaint()
	{
		repaint(null);		
	}
	
	private void downloadMapImage()
	{
		if (downloadThread != null)
			return;
		downloadThread = new ImageDownloadWorker();
		downloadThread.execute();
		repaint();
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
	
	private class ClientSettings
	{
		public boolean drawImage = true;
		public boolean drawGrid = true;
		public boolean drawNodes = true;
		public boolean drawIncidents = true;
		public boolean drawDevices = true;

		public Color gridLineColor = new Color(0, 0, 0, 70);
		public Color gridTextColor = new Color(255, 255, 255, 150);
		public Color gridShadingColor = new Color(0, 0, 0, 150);
		public Stroke gridStroke = new BasicStroke(1);
		public Font gridFont = new Font(Font.SANS_SERIF, Font.BOLD | Font.ITALIC, 16);
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
			repaint();
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
			repaint();
    }

    //quick check to see if it's worth firing a repaint()
    private final void repaintIncidents()
    {
		if (clients.size() > 0 && incidents.size() > 0)
			repaint();
    }
    
    //quick check to see if it's worth firing a repaint()
    private final void repaintDevices()
    {
    	if (clients.size() > 0 && devices.size() > 0)
			repaint();
    }
}
