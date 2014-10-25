package wifindus.eye;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import wifindus.ConfigFile;
import wifindus.Debugger;
import wifindus.GPSRectangle;
import wifindus.HighResolutionTimerListener;
import wifindus.ImageContainer;
import wifindus.eye.Incident.Type;

public class MapRenderer
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
	private int gridRows = 10;
	private int gridColumns = 10;
	private final boolean highRes;
	
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
		double scaledRadius = CHUNK_RADIUS / Math.pow(2.0, (double)(zoom < CHUNK_MIN_ZOOM ? CHUNK_MIN_ZOOM : zoom));
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
	
	public final void paintMap(JComponent client, Graphics graphics, double xPos, double yPos, double zoom)
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
		xPos = Math.min(1.0, Math.max(0.0,zoom));
		yPos = Math.min(1.0, Math.max(0.0,zoom));
		zoom = Math.min(4.0, Math.max(0.25,zoom));
		
		//get destination rectangle
		int clientWidth = client.getWidth();
		int clientHeight = client.getHeight();
		
		//determine source rectangle and draw image
		double viewWidth = (double)clientWidth / zoom;
		double viewHeight = (double)clientHeight / zoom;
		double viewLeft = MAP_SIZE * xPos - (viewWidth / 2.0);
		double viewTop = MAP_SIZE * yPos - (viewHeight / 2.0);
		double viewRight = viewLeft + viewWidth;
		double viewBottom = viewTop + viewHeight;
		if (settings.drawImage && mapImage != null)
		{
			graphics.drawImage(
				//source image
				mapImage,
				//destination coords
				0, 0, clientWidth, clientHeight,
				//source coords
				(int)viewLeft, (int)viewTop, (int)viewRight, (int)viewBottom,
				//observer
				null);
		}
		/*
		//draw grid
		if (settings.drawGrid)
		{
	        double gridStepX = viewWidth / (double)gridColumns;
	        double gridStepY = viewHeight / (double)gridRows;
	        
	        //rows
	        for (int i = 0; i < gridRows; i++)
	        {
				if (i < gridRows-1)
					graphics.drawLine(targetArea.x, targetArea.y + hStep * (i + 1), targetArea.x + targetArea.width, targetArea.y + hStep * (i + 1));
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
		*/
	}
	
	public final void paintMap(JComponent client, Graphics graphics)
	{
		paintMap(client, graphics, 0.5, 0.5, 1.0);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
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
		ImageDownloadWorker downloadWorker = new ImageDownloadWorker();
		downloadWorker.execute();
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
		public boolean drawAssignedDevices = true;
		public boolean drawUnassignedDevices = true;
	}
	
	private class ImageDownloadWorker extends SwingWorker<Image, Integer>
	{
		private volatile int contentLength = -1;
		
		private String downloadProgressString(int read)
		{
			return String.format("Image downloaded: %d bytes %s",
				read, (contentLength <= -1? "" : String.format("(%.2f%%)", ((double)read/(double)contentLength)*100.0)));
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
				int read = 0;
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
			        if (readChunk >= 1024*64) //push results every 64k
			        {
			        	readChunk = 0;
			        	Debugger.i(downloadProgressString(read));
			        }
			    }
			    Debugger.i(downloadProgressString(read));
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
		protected void done()
		{
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
	};
}
