package wifindus;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * A global, static repository for all disk-loaded resources, such as images.
 * @author Mark 'marzer' Gillard
 */
public abstract class ResourcePool
{
	private static transient Map<String, ImageContainer> images = new HashMap<String, ImageContainer>();

	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Loads an image from a disk resource, skipping it if an image with the given key already exists.
	 * Also returns the a reference to the Image for convenience. 
	 * @param key The key to use for the image. Case insensitive.
	 * @param file The file to load.
	 * @return An ImageIcon object if the associated Image has been loaded, or NULL. 
	 */
	public static Image loadImage(String key, String file)
	{
		key = checkKey(key);
		if (file == null)
			throw new NullPointerException("Parameter 'file' cannot be null.");
		if (!images.containsKey(key))
		{
			try
			{
				ImageContainer container = new ImageContainer(ImageIO.read(new File(file)));
				images.put(key, container);
			}
			catch (IOException e)
			{
				Debugger.e("Exception reading file [%s] %s", key, file);
			}
		}
		return getImage(key);
	}
	
	/**
	 * Gets a pre-loaded image resource associated with a key.
	 * @param key The key to look up.
	 * @return An image object if it has been loaded, or NULL. 
	 */
	public static Image getImage(String key)
	{
		ImageContainer container = images.get(checkKey(key));
		return container == null ? null : container.getImage();
	}
	
	/**
	 * Gets an ImageIcon associated with a pre-loaded image resource.
	 * @param key The key to look up.
	 * @return An ImageIcon object if the associated Image has been loaded, or NULL. 
	 */
	public static ImageIcon getIcon(String key)
	{
		ImageContainer container = images.get(checkKey(key));
		return container == null ? null : container.getIcon();
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private static String checkKey(String key)
	{
		if (key == null)
			throw new NullPointerException("Parameter 'key' cannot be null.");
		key = key.trim().toLowerCase();
		if (key.length() == 0)
			throw new IllegalArgumentException("Parameter 'key' cannot be an empty string.");
		return key;
	}
}
