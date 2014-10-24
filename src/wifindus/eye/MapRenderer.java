package wifindus.eye;

import java.awt.Graphics2D;
import java.awt.Image;
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
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;
import wifindus.ConfigFile;
import wifindus.Debugger;
import wifindus.GPSRectangle;
import wifindus.HighResolutionTimerListener;
import wifindus.ImageContainer;

public class MapRenderer
{
	//constants to match the values in excel
	private static final double CHUNK_RADIUS = 0.01126;
	private static final double CHUNK_LONG_SCALE = 1.22;
	private static final int CHUNK_IMAGE_SIZE = 1024;
	private static final int CHUNK_MIN_ZOOM = 15;
	private static final String MAPS_URL_BASE = "https://maps.googleapis.com/maps/api/staticmap?";
	private static final File MAPS_DIR = new File("maps/");
	
	private GPSRectangle bounds;
	private Image mapImage;
	private BufferedImage buffer;
	private Graphics2D context;
	private volatile File mapFile;
	private volatile CopyOnWriteArrayList<MapRenderListener> listeners = new CopyOnWriteArrayList<>();
	private volatile URL mapDownloadURL; 
	private volatile boolean abortThread = false;
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public MapRenderer(double latitude, double longitude, int zoom, boolean highRes, String apiKey, String mapType)
	{
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
			+ String.format("%.6f_%.6f_%d_%s.png", latitude, longitude, zoom, highRes ? "high" : "low"));
		
		//load or download image
		Debugger.i("MapRenderer loading... (source: \""+mapFile+"\")");
		if (loadMapImage())
		{
			Debugger.i("Map image loaded OK.");
			repaint();
		}
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
	 * Adds a new MapRenderListener.
	 * @param listener subscribes an MapRenderListener to this MapRenderer's events.
	 */
	public final void addRenderListener(MapRenderListener listener)
	{
		if (listener == null || listeners.contains(listener))
			return;
		
		synchronized(listeners)
		{
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes an existing MapRenderListener. 
	 * @param listener unsubscribes a MapRenderListener from this MapRenderer's events.
	 * Has no effect if this parameter is null, or is not currently subscribed to this object.
	 */
	public final void removeRenderListener(MapRenderListener listener)
	{
		if (listener == null)
			return;
		synchronized(listeners)
		{
			listeners.remove(listener);
		}
	}
	
	/**
	 * Unsubscribes all MapRenderListener from this MapRenderer's events.
	 */
	public final void clearRenderListeners()
	{
		synchronized(listeners)
		{
			listeners.clear();
		}
	}
	
	public final void dispose()
	{
		abortThread = true;
		if (mapImage != null)
			mapImage = null;
		if (context != null)
		{
			context.dispose();
			context = null;
		}
		if (buffer != null)
			buffer = null;
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private void repaint()
	{	
		// creation event
		synchronized(listeners)
		{
			ListIterator<MapRenderListener> iterator = listeners.listIterator();
			while(iterator.hasNext())
				iterator.next().mapImageChanged(this);
		}
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
			mapImage = ImageIO.read(mapFile);
			return true;
		}
		catch (IOException e)
		{
			Debugger.ex(e);
		}
		
		return false;
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
				mapImage = get();
				repaint();
			}
			catch (Exception e)
			{
				Debugger.ex(e);
			}
		}		
	};
}
