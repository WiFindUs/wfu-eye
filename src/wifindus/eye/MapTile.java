package wifindus.eye;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
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
	public static final int CHUNK_STANDARD_ZOOM = 15;
	public static final int CHUNK_IMAGE_SIZE = 640;
	
	private static volatile int runningLoaderCount = 0;
	private static final int CONCURRENT_LOADER_LIMIT = 3;
	private static final Color LOADING_FILL = new Color(0, 0, 0, 15);
	private static final Color LOADING_PROGRESS = new Color(255, 255, 255, 50);
	private static final Color BORDERS = new Color(255, 102, 0, 155);
	private static final Stroke BORDER_STROKE = new BasicStroke(3);
	private static final String IMAGE_FORMAT = "png";
	private static final String MAPS_URL_BASE = "https://maps.googleapis.com/maps/api/staticmap?";
	private static final File MAPS_DIR = new File("maps/");
	private static final double CHUNK_RADIUS = 0.01126;
	private static final double CHUNK_LONG_SCALE = 1.22;
	private GPSRectangle bounds;
	private double latitude, longitude;
	private int zoom;
	private String apiKey;
	private MapRenderer renderer;
	private Map<String, Image> images = new HashMap<>();
	private ImageLoadWorker loader = null;
	private volatile Map<String, Boolean> failedLoads = new HashMap<>();
	private volatile boolean abortThread = false;

	public MapTile(MapRenderer owner, double latitude, double longitude, int zoom, String apiKey)
	{
		//settings 
		this.latitude = latitude;
		this.longitude = longitude;
		this.zoom = zoom;
		this.apiKey = apiKey;
		this.renderer = owner;
		
		//create bounds
		double scaledRadius = CHUNK_RADIUS / Math.pow(2.0,(zoom - CHUNK_STANDARD_ZOOM));
		bounds = new GPSRectangle(
				latitude + scaledRadius,
				longitude - (scaledRadius * CHUNK_LONG_SCALE),
			latitude - scaledRadius,
			longitude + (scaledRadius * CHUNK_LONG_SCALE));
	}

	public void paintTile(Graphics2D graphics, String type, Rectangle2D.Double tileArea, Rectangle2D.Double shownArea)
	{
		//check for intersection
		if (!tileArea.intersects(shownArea))
			return;
		
		//try getting image first
		Image image = images.get(type); //"satellite", "roadmap", "terrain" and "hybrid"
		int x = (int)tileArea.x;
		int y = (int)tileArea.y;
		int width = (int)tileArea.width;
		int height = (int)tileArea.height;
		
		//draw placeholder/download progress square
		if (image == null)
		{
			graphics.setStroke(BORDER_STROKE);
			graphics.setColor(BORDERS);
			graphics.drawRect(x, y, width, height);
			graphics.setColor(LOADING_FILL);
			graphics.fillRect(x, y, width, height);
			
			//not loading currently
			if (loader == null)
			{
				//failed already
				if (failedLoads.get(type) == Boolean.TRUE)
					return;
				
				//load it
				if (runningLoaderCount < CONCURRENT_LOADER_LIMIT)
				{
					runningLoaderCount++;
					loader = new ImageLoadWorker(type);
					loader.execute();
				}
			}
			
			if (loader != null) //currently loading this image
			{
				graphics.setColor(LOADING_PROGRESS);
				graphics.fillRect(x, y, (int)(width * loader.percentage), height);
			}
			return;
		}
		
		Rectangle2D.Double drawnArea = new Rectangle2D.Double();
		Rectangle2D.Double.intersect(tileArea, shownArea, drawnArea);
		double imageWidth = (double)image.getWidth(null);
		double imageHeight = (double)image.getHeight(null);
		
		int sourceX = (int)((drawnArea.x - tileArea.x) / tileArea.width * imageWidth);
		int sourceY = (int)((drawnArea.y - tileArea.y) / tileArea.height * imageHeight);
		int sourceWidth = (int)(drawnArea.width / tileArea.width * imageWidth);
		int sourceHeight = (int)(drawnArea.height / tileArea.height * imageHeight);
		
		graphics.drawImage(
			//source image
			image,
			//destination coords
			(int)drawnArea.x, (int)drawnArea.y, //top left
			(int)(drawnArea.x + drawnArea.width), (int)(drawnArea.y + drawnArea.height), //bottom right
			//source coords
			sourceX, sourceY, //top left
			sourceX+sourceWidth, sourceY+sourceHeight, //bottom right
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
		
		
		public ImageLoadWorker(String type)
		{
			this.type = type;
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
			        Thread.sleep(2); //just to slow the download a tiny bit
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
			runningLoaderCount--;
			loader = null;
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
