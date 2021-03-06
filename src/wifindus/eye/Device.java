package wifindus.eye;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import wifindus.Debugger;
import wifindus.EventObject;
import wifindus.MySQLResultRow;
import wifindus.MySQLUpdateTarget;
import wifindus.ResourcePool;

/**
 * A client device in use by medical, security or wifindus personnel.
 * @author Mark 'marzer' Gillard
 */
public class Device extends EventObject<DeviceEventListener> implements MySQLUpdateTarget, MappableObject
{
	//normally 30 seconds, but for testing purposes we'll make it 'infinite'
	public long TIMEOUT_THRESHOLD = Long.MAX_VALUE;//1000 * 30; 
	
	/**
	 * A description of a client device's 'type'.
	 * @author Mark 'marzer' Gillard
	 */
	public enum Type
	{
		/**
		 * This device is an Android or iOS phone. Since a grey area exists where
		 * tablets may have cellular connections and make VOIP calls etc,
		 * a device being of type 'Phone' must be able to do all of the above,
		 * in addition to being able to make cellular-network-based phone calls.
		 */
		Phone,
		
		/**
		 * Any Android or iOS device that does not make cellular phone calls and
		 * is not a smart watch. 
		 */
		Tablet,
		
		/**
		 * Android or iOS smartwatches (galaxy gear, iWatch, etc.)
		 */
		Watch,
		
		/**
		 * A laptop/netbook/computer running a wifindus EMT client application.
		 * Since no such application currently exists (and is unlikely), this will
		 * probably not be used (placeholder). 
		 */
		Computer,
		
		/**
		 * A placeholder for other/unknown device types.
		 */
		Other
	}
	
	//properties
	private String hash = "";
	private Type type = Type.Other;
	private InetAddress address = null;
	private Location location = Location.EMPTY;
	private Atmosphere atmosphere = Atmosphere.EMPTY;
	private Timestamp lastUpdate = new Timestamp(0);
	private boolean selected = false;
	
	//database relationships
	private transient User currentUser = null;
	private transient Incident currentIncident = null;
	
	//marker stuff
	private static final Image pinImage, pinHoverImage, pinSelectedImage;
	private static final Map<Incident.Type, Image> adornmentImages = new HashMap<>();
	static
	{
		ResourcePool.loadImage("pin", "images/pin.png" );
		ResourcePool.loadImage("pin_hover", "images/pin_hover.png" );
		ResourcePool.loadImage("pin_selected", "images/pin_selected.png" );
		ResourcePool.loadImage("cog_pin", "images/cog_pin.png" );
		ResourcePool.loadImage("shield_pin", "images/shield_pin.png" );
		ResourcePool.loadImage("question_pin", "images/question_pin.png" );
		ResourcePool.loadImage("cross_pin", "images/cross_pin.png" );
		
		pinImage = ResourcePool.getImage("pin");
		pinHoverImage = ResourcePool.getImage("pin_hover");
		pinSelectedImage = ResourcePool.getImage("pin_selected");
		adornmentImages.put(Incident.Type.Medical, ResourcePool.getImage("cross_pin"));
		adornmentImages.put(Incident.Type.Security, ResourcePool.getImage("shield_pin"));
		adornmentImages.put(Incident.Type.WiFindUs, ResourcePool.getImage("cog_pin"));
		adornmentImages.put(Incident.Type.None, ResourcePool.getImage("question_pin"));
	}
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a new Device, representing a Device entry from the database.
	 * @param hash The randomly 8-character hash key of this Device.
	 * @param type The device's type.
	 * @param listeners A variable-length list of event listeners that will watch this device's state.
	 * @throws NullPointerException if <code>hash</code> is null.
	 * @throws IllegalArgumentException if <code>hash</code> is not valid.
	 * @throws UnsupportedOperationException if <code>Computer</code> is used as <code>type</code> (not currently supported).
	 */
	public Device(String hash, Type type, DeviceEventListener... listeners)
	{
		super(listeners);
		
		if (hash == null)
			throw new NullPointerException("Parameter 'hash' cannot be null.");
		if (!Hash.isValid(hash))
			throw new IllegalArgumentException("Parameter 'hash' is not a valid WFU device hash ("+hash+").");
		if (type == Type.Computer)
			throw new UnsupportedOperationException("Parameter 'type' does not currently support Type.Computer.");
		
		this.hash = hash;
		this.type = type;
	}

	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////

	public void toggleSelected()
	{
		fireEvent("selectionChanged");
		if (selected == true)
		{
				selected = false;
				
		}
		else
			selected = true;
	}
	
	public boolean getSelected()
	{
		return selected;
	}
	
	/**
	 * Gets this Device's Hash.
	 * @return An 8-character String containing this Device's unique Hash ID.
	 */
	public final String getHash()
	{
		return hash;
	}

	/**
	 * Gets this Device's Type.
	 * @return A Device.Type value indicating what type of device this Device object represents.
	 */
	public final Type getType()
	{
		return type;
	}

	/**
	 * Gets this Device's IP Address.
	 * @return An InetAddress object containing information about the last-known address this Device connected from.
	 */
	public final InetAddress getAddress()
	{
		return address;
	}

	/**
	 * Gets this Device's Location.
	 * @return A Location object containing the last-known location information this Device reported. 
	 */
	public final Location getLocation()
	{
		return location;
	}

	/**
	 * Gets this Device's Atmosphere.
	 * @return An Atmosphere object containing the last-known atmosphere information this Device reported. 
	 */
	public final Atmosphere getAtmosphere()
	{
		return atmosphere;
	}

	/**
	 * Gets this Device's last update timestamp.
	 * @return An sql.Timestamp object representing the date/time the last known update was received.
	 * Since Timestamp is a mutable structure, the returned object is a clone, and thus calling any
	 * mutators will have no effect on the Device's timestamp data.
	 */
	public final Timestamp getLastUpdate()
	{
		return (Timestamp)lastUpdate.clone();
	}

	/**
	 * Gets the User currently signed in to the device.
	 * @return A reference to a User object representing the currently signed in user, or NULL if nobody is signed in.  
	 */
	public final User getCurrentUser()
	{
		return currentUser;
	}
	
	/**
	 * Gets type of the user currently logged into the device.
	 * @return The current user's Incident.Type, or None if there is no logged in user.
	 */
	public final Incident.Type getCurrentUserType()
	{
		return currentUser == null ? Incident.Type.None : currentUser.getType();
	}

	/**
	 * Gets the Incident this Device is currently assigned to.
	 * @return A reference to a Incident object representing the currently assigned Incident, or NULL if no incident has been assigned.  
	 */
	public final Incident getCurrentIncident()
	{
		return currentIncident;
	}
	
	/**
	 * Checks if a device is considered timed out based on a long period of inactivity;
	 * @return
	 */
	public final boolean isTimedOut()
	{
		return (System.currentTimeMillis() - lastUpdate.getTime()) >= TIMEOUT_THRESHOLD;
	}
	
	/**
	 * Gets a Type from a database type key.
	 * @param key The key to match with an Device.Type.
	 * @return The Device.Type enum value matching the given key.
	 * @throws NullPointerException if key was null.
	 * @throws IllegalArgumentException if key did not match an Device.Type database key.
	 */
	public static final Type getTypeFromDatabaseKey(String key)
	{
		if (key == null)
			throw new NullPointerException("Parameter 'key' cannot be null.");
		switch (key.toUpperCase())
		{
			case "PHO": return Device.Type.Phone;
			case "TAB": return Device.Type.Tablet;
			case "WAT": return Device.Type.Watch;
			case "COM": return Device.Type.Computer;
			case "OTH": return Device.Type.Other;
		}
		
		throw new IllegalArgumentException("Parameter 'key' does not match an Device.Type database key.");
	}
	
	@Override
	public final void updateFromMySQL(MySQLResultRow resultRow)
	{
		if (resultRow == null)
			throw new NullPointerException("Parameter 'resultRow' cannot be null.");
		if (!((String)resultRow.get("hash")).equals(getHash()))
			throw new IllegalArgumentException("Parameter 'resultRow' does not have the same primary key as this object.");
		
		//update location data
		Location loc = new Location(
				(Double)resultRow.get("latitude"),
				(Double)resultRow.get("longitude"),
				(Double)resultRow.get("accuracy"),
				(Double)resultRow.get("altitude"));
		if (!loc.equals(location))
		{
			Location old = location;
			location = loc;
			fireEvent("location", old, location);
		}
		
		//update atmosphere data
		Atmosphere atmos = new Atmosphere(
				(Double)resultRow.get("humidity"),
				(Double)resultRow.get("airPressure"),
				(Double)resultRow.get("temperature"),
				(Double)resultRow.get("lightLevel"));
		if (!atmos.equals(atmosphere))
		{
			Atmosphere old = atmosphere;
			atmosphere = atmos;
			fireEvent("atmosphere", old, atmosphere);
		}
	
		//internet address
		String addressString = (String)resultRow.get("address");
		InetAddress newAddress = null;
		if (addressString != null && !addressString.isEmpty())
		{
			try
			{
				newAddress = InetAddress.getByName(addressString);
			}
			catch (UnknownHostException e)
			{
				Debugger.ex(e);
			}
		}
		if ((address == null && newAddress != null)
			|| (address != null && (newAddress == null || !newAddress.equals(address))))
		{
			InetAddress old = address;
			address = newAddress;
			fireEvent("address", old, address);
		}
		
		//lastUpdate
		Timestamp ts = (Timestamp)resultRow.get("lastUpdate");
		if (ts != null && !lastUpdate.equals(ts))
		{
			lastUpdate = ts;
			fireEvent("updated");
		}
	}
	
	@Override
	public String toString()
	{
		return currentUser == null ? "Device[\""+getHash()+"\"]" : currentUser.getNameFull();
	}
	
	/**
	 * Updates the current user of this device.
	 * <strong>DO NOT</strong> call this in client/UI code; this is handled at a higher level.
	 * @param currentUser The current User of this device
	 */
	public final void updateUser(User currentUser)
	{
		if (this.currentUser == currentUser)
			return;

		//log out
		if (this.currentUser != null)
		{
			User oldUser = this.currentUser;
			this.currentUser = null;
			oldUser.updateDevice(null);
			fireEvent("loggedout", oldUser);
		}
		
		//log in
		if (currentUser != null)
		{
			this.currentUser = currentUser;
			currentUser.updateDevice(this);
			fireEvent("loggedin", currentUser);
		}
		
	}
	
	/**
	 * Updates the current Incident of this device.
	 * <strong>DO NOT</strong> call this in client/UI code; this is handled at a higher level.
	 * @param currentIncident The current Incident of this device
	 */
	public final void updateIncident(Incident currentIncident)
	{
		if (this.currentIncident == currentIncident)
			return;

		Incident oldIncident = this.currentIncident;
		this.currentIncident = currentIncident;
		
		//unassigned
		if (oldIncident != null)
		{
			oldIncident.unassignDevice(this);
			fireEvent("unassigned", oldIncident);
		}
		
		//log in
		if (currentIncident != null)
		{
			currentIncident.assignDevice(this);
			fireEvent("assigned", currentIncident);
		}
	}
	
	@Override
	public void paintMarker(Graphics2D graphics, int x, int y, boolean isHovering, boolean isSelected)
	{		
		Image adornment = adornmentImages.get(getCurrentUserType());
		int left = x-16;
		int top = y-51;
		graphics.setColor(Incident.getColorFromType(getCurrentUserType()));
		graphics.fillOval(left+3, top+3, 28, 28);
		graphics.drawImage(isSelected ? pinSelectedImage : pinImage, left, top, null);
		graphics.drawImage(adornment, left+8, top+8, null);
		if (isHovering)
			graphics.drawImage(pinHoverImage, left, top, null);
		if (isHovering || isSelected)
			MapRenderer.paintMarkerText(graphics,x,top,currentUser == null ? hash : currentUser.getNameFull());
	}
	
	@Override
	public Polygon generateMarkerHitbox(int x, int y)
	{
		Polygon poly = new Polygon();
		poly.addPoint(x,y);
		poly.addPoint(x-16,y-34);
		poly.addPoint(x-13,y-46);
		poly.addPoint(x,y-52);
		poly.addPoint(x+13,y-46);
		poly.addPoint(x+16,y-34);
		return poly;
	}
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	protected void mapEvents(String event, DeviceEventListener listener, Object... data)
	{
		switch(event)
		{
			case "timedout":
				listener.deviceTimedOut(this);
				break;
			case "loggedin":
				listener.deviceInUse(this, (User)data[0]);
				break;
			case "loggedout":
				listener.deviceNotInUse(this, (User)data[0]);
				break;
			case "location":
				listener.deviceLocationChanged(this, (Location)data[0], (Location)data[1]);
				break;
			case "atmosphere":
				listener.deviceAtmosphereChanged(this, (Atmosphere)data[0], (Atmosphere)data[1]);
				break;
			case "address":
				listener.deviceAddressChanged(this, (InetAddress)data[0], (InetAddress)data[1]);
				break;
			case "updated":
				listener.deviceUpdated(this);
				break;
			case "assigned":
				listener.deviceAssignedIncident(this, (Incident)data[0]);
				break;
			case "unassigned":
				listener.deviceUnassignedIncident(this, (Incident)data[0]);
				break;
			case "selectionChanged":
				listener.deviceSelectionChanged(this);
				break;
				
		}
	}

	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
}
