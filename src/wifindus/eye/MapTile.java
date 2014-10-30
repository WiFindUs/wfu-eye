package wifindus.eye;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

import wifindus.Debugger;
import wifindus.GPSRectangle;


public class MapTile
{
	public static final int CHUNK_MIN_ZOOM = 15;
	public static final int CHUNK_IMAGE_SIZE = 640;
	
	private static final String IMAGE_FORMAT = "png";
	private static final String MAPS_URL_BASE = "https://maps.googleapis.com/maps/api/staticmap?";
	private static final File MAPS_DIR = new File("maps/");
	private static final double CHUNK_RADIUS = 0.01126;
	private static final double CHUNK_LONG_SCALE = 1.22;
	private static ImageLoadWorker loadThread = null;
	private volatile GPSRectangle bounds;
	private volatile double latitude, longitude;
	private volatile Map<String, Image> images = new HashMap<>();
	private volatile Map<String, Boolean> failedLoads = new HashMap<>();
	private int zoom;
	private volatile boolean abortThread = false;
	private String apiKey;
	private volatile MapRenderer renderer;
	private static final Color LOADING_FILL = new Color(255, 255, 255, 50);
	private static final Color BORDERS = new Color(0, 0, 0, 50);
	
	public MapTile(MapRenderer owner, double latitude, double longitude, int zoom, String apiKey)
	{
		//settings 
		this.latitude = latitude;
		this.longitude = longitude;
		this.zoom = zoom;
		this.apiKey = apiKey;
		this.renderer = owner;
		
		//create bounds
		double scaledRadius = CHUNK_RADIUS / Math.pow(2.0,(zoom - (zoom < CHUNK_MIN_ZOOM ? zoom : CHUNK_MIN_ZOOM)));
		bounds = new GPSRectangle(
				latitude + scaledRadius,
				longitude - (scaledRadius * CHUNK_LONG_SCALE),
			latitude - scaledRadius,
			longitude + (scaledRadius * CHUNK_LONG_SCALE));
	}
	
	public void paintTile(Graphics2D graphics, String type, Rectangle2D.Double tileArea)
	{
		//try getting image first
		Image image = images.get(type); //"satellite", "roadmap", "terrain" and "hybrid"
		int x = (int)tileArea.x;
		int y = (int)tileArea.y;
		int width = (int)tileArea.width;
		int height = (int)tileArea.height;
		
		if (image == null)
		{
			//draw a placeholder square
			graphics.setColor(BORDERS);
			graphics.drawRect(x, y, width, height);
			
			//not loading currently
			if (loadThread == null)
			{
				//failed already
				if (failedLoads.get(type) == Boolean.TRUE)
					return;
				
				//load it
				loadThread = new ImageLoadWorker(this, type);
				loadThread.execute();
			}
			else if (loadThread.owner == this) //currently loading this image
			{
				graphics.setColor(LOADING_FILL);
				graphics.fillRect(x, y, (int)(width * loadThread.percentage), height);
				
			}
			return;			
		}
		
		graphics.drawImage(
			//source image
			image,
			//destination coords
			x, y, //top left
			width, height, //bottom right
			//observer
			null);
	}
	
	public GPSRectangle getBounds()
	{
		return bounds;
	}
	
	public void dispose()
	{
		abortThread = true;
		images.clear();
	}
	
	private class ImageLoadWorker extends SwingWorker<Image, Double>
	{
		volatile int contentLength = -1;
		volatile int read = 0;
		volatile double percentage = 0.0;
		volatile double lastPercentage = 0.0;
		volatile File file;
		volatile URL url; 
		volatile String type;
		volatile MapTile owner;
		
		public ImageLoadWorker(MapTile owner, String type)
		{
			this.type = type;
			this.owner = owner;
			try 
			{
				url = new URL(String.format(
					"%scenter=%.6f,%.6f&zoom=%d&scale=%d&size=%dx%d&key=%s&maptype=%s&format=%s",
					MAPS_URL_BASE, latitude, longitude, zoom,
					2, CHUNK_IMAGE_SIZE, CHUNK_IMAGE_SIZE,
					apiKey, type, IMAGE_FORMAT));
			}
			catch (MalformedURLException e)
			{
				Debugger.ex(e);
				url = null;
			}
			file = new File("maps/"
					+ String.format("%.6f_%.6f_%d_%s.%s", latitude, longitude, zoom, type, IMAGE_FORMAT));
			renderer.repaintClients();
		}
		
		@Override
		protected Image doInBackground() throws Exception
		{
			//try reading it locally first
			Debugger.i("Loading map tile image... (source: \""+file+"\")");
			if (file.exists() && file.isFile())
			{
				try
				{
					return ImageIO.read(file);
				}
				catch (IOException e)
				{
					Debugger.e("Error reading file!");
				}
				
			}
			
			//if url was null, set the failure bit and return.
			if (url == null)
			{
				Debugger.e("Couldn't download using invalid URL.");
				failedLoads.put(type, Boolean.TRUE);
				return null;
			}
			
			//download file into a byte array
			Debugger.i("Downloading static maps image from url:\n" + url.toString());
			byte[] imageBytes = null;
			InputStream in = null;
			ByteArrayOutputStream baos = null;
			try
			{				
				URLConnection connection = url.openConnection();
				in = connection.getInputStream();
				contentLength = connection.getContentLength();
				baos = new ByteArrayOutputStream(contentLength == -1 ? 1048576 : contentLength);
				int bufSize = 512;
				byte[] buf = new byte[bufSize];
			    while (true)
			    {
		        	if (abortThread)
		        		return null;
			    	
			    	int len = in.read(buf);
			        if (len == -1)
			            break;
			        baos.write(buf, 0, len);
			        
			        read += len;
			        percentage = ((double)read/(double)contentLength);
			        if ((percentage - lastPercentage) > 0.10) //push results each 10%
			        {
			        	lastPercentage = percentage;
			        	publish(percentage);			        	
			        }
			    }
			    Debugger.i("Map image download complete.");
			    in.close();
			    baos.flush();
			    imageBytes = baos.toByteArray();
			    
			}
			catch (Exception e)
			{
				Debugger.ex(e);
				failedLoads.put(type, Boolean.TRUE);
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
				failedLoads.put(type, Boolean.TRUE);
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
			    ImageIO.write(image, "png", file);
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
			renderer.repaintClients();
		}

		@Override
		protected void done()
		{
			loadThread = null;
			if (abortThread)
        		return;
			try
			{
				images.put(type,get());
				renderer.repaintClients();
			}
			catch (Exception e)
			{
				Debugger.ex(e);
			}
		}		
	}
}
