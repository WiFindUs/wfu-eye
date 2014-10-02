package wifindus.eye;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.ImageIcon;
import wifindus.EventObject;
import wifindus.MySQLResultRow;
import wifindus.MySQLUpdateTarget;

/**
 * An security, medical or wifindus incident occurring in the field, as reported by field personnel. 
 * @author Mark 'marzer' Gillard
 */
public class Incident extends EventObject<IncidentEventListener> implements MySQLUpdateTarget
{
	/**
	 * A description of an Incident's 'type' (i.e. who is supposed to respond to it).
	 * @author Mark 'marzer' Gillard
	 */
	public enum Type
	{
		/**
		 * This incident is medical in nature, and should be responded to by medical personnel.
		 */
		Medical,
		
		/**
		 * This incident is one of security, and should be responded to by security personnel.
		 */
		Security,
		
		/**
		 * This incident is technical/internal, and should be responded to by WiFindUs personnel.
		 */
		WiFindUs,
		
		/**
		 * This incident does not have a type (this is for UI purposes, do not create instances with this).
		 */
		None
	}
	
	//properties
	private int id;
	private Type type;
	private Location location = Location.EMPTY;
	private Timestamp created = new Timestamp(0);
	private boolean archived = false;
	//database relationships
	private transient volatile ConcurrentHashMap<String,Device> respondingDevices = new ConcurrentHashMap<>();
	private transient static HashMap<Incident.Type, ImageIcon> iconsLarge = new HashMap<>();
	private transient static HashMap<Incident.Type, ImageIcon> iconsSmall = new HashMap<>();
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a new Incident, representing an Incident entry from the database.
	 * @param id The automatically-assigned unique integer key of this Incident.
	 * @param type The incident's type (i.e. who is supposed to respond to it).
	 * @param location The location of the event.
	 * @param created The timestamp of when this Incident was created.
	 * @param listeners A variable-length list of event listeners that will watch this incident's state.
	 * @throws NullPointerException if <code>location</code> or <code>created</code> are null.
	 * @throws IllegalArgumentException if <code>id</code> is negative,
	 * <code>location.isEmpty()</code> returns TRUE, or if <code>location</code> is missing horizontal positoning data (lat/long).
	 * @throws UnsupportedOperationException if <code>None</code> is used as <code>type</code>.
	 */
	public Incident(int id, Type type, Location location, Timestamp created, IncidentEventListener... listeners)
	{
		super(listeners);
				
		if (id < 0)
			throw new IllegalArgumentException("Parameter 'id' cannot be negative.");
		if (type == Type.None)
			throw new UnsupportedOperationException("Parameter 'type' does not support Type.None.");
		if (location == null)
			throw new NullPointerException("Parameter 'location' cannot be null.");
		if (created == null)
			throw new NullPointerException("Parameter 'created' cannot be null.");
		if (location.isEmpty())
			throw new IllegalArgumentException("Parameter 'location' cannot be empty.");
		if (location.getLatitude() == null || location.getLongitude() == null)
			throw new IllegalArgumentException("Parameter 'location' is missing horizontal positioning data.");
		
		this.id = id;
		this.type = type;
		this.location = location;
		this.created = created;
		
		fireEvent("created");
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets this Incident's ID.
	 * @return An integer representing this Incident's automatically-assigned id key.
	 */
	public final int getID()
	{
		return id;
	}

	/**
	 * Gets this Incident's Type.
	 * @return A Incident.Type value indicating what type of incident this Incident object represents.
	 */
	public final Type getType()
	{
		return type;
	}

	/**
	 * Gets this Incident's Location.
	 * @return A Location object containing, at a minimum, the horizontal positioning information necessary to locate the incident in the field. 
	 */
	public final Location getLocation()
	{
		return location;
	}
	
	/**
	 * Gets this Incident's creation timestamp.
	 * @return An sql.Timestamp object representing the date/time the incident was created.
	 * Since Timestamp is a mutable structure, the returned object is a clone, and thus calling any
	 * mutators will have no effect on the Incident's timestamp data.
	 */
	public final Timestamp getCreated()
	{
		return (Timestamp)created.clone();
	}
	
	/**
	 * Gets this Incident's archived status.
	 * @return TRUE if this incident has been archived, FALSE otherwise. 
	 */
	public final boolean isArchived()
	{
		return archived;
	}

	/**
	 * Gets a list of all Devices currently assigned to respond to this Incident.
	 * @return An ArrayList of Devices assigned to this Incident.
	 * Since the backing collection is a mutable structure, the returned list is a clone,
	 * and thus calling any mutators will have no effect on the backing collection data.
	 */
	public final ArrayList<Device> getRespondingDevices()
	{
		return new ArrayList<Device>(respondingDevices.values());
	}
	
	/**
	 * Gets a Type from a database type key.
	 * @param key The key to match with an Incident.Type.
	 * @return The Incident.Type enum value matching the given key.
	 * @throws NullPointerException if key was null.
	 * @throws IllegalArgumentException if key did not match an Incident.Type database key.
	 */
	public static final Type getTypeFromDatabaseKey(String key)
	{
		if (key == null)
			throw new NullPointerException("Parameter 'key' cannot be null.");
		switch (key)
		{
			case "MED": return Incident.Type.Medical;
			case "SEC": return Incident.Type.Security;
			case "WFU": return Incident.Type.WiFindUs;
		}
		
		throw new IllegalArgumentException("Parameter 'key' does not match an Incident.Type database key.");
	}
	
	@Override
	public String toString()
	{
		return "Incident["+getID()+"]";
	}
	
	@Override
	public void update(MySQLResultRow resultRow)
	{
		if (resultRow == null)
			throw new NullPointerException("Parameter 'resultRow' cannot be null.");
		if (((Integer)resultRow.get("id")).intValue() != getID())
			throw new IllegalArgumentException("Parameter 'resultRow' does not have the same primary key as this object.");
		
		boolean archived = (boolean)resultRow.get("archived");
		if (!this.archived && archived)
			archive();
	}
	
	/**
	 * Adds a device to the list of currently assigned devices.
	 * <strong>DO NOT</strong> call this in client/UI code; this is handled at a higher level.
	 * @param newDevice The device to add to the list.
	 */
	public void assignDevice(Device newDevice)
	{
		if (newDevice == null)
			throw new NullPointerException("Parameter 'newDevice' cannot be null.");
		respondingDevices.put(newDevice.getHash(), newDevice);
		fireEvent("assigned", newDevice);
	}
	
	/**
	 * Removes a device from the list of currently assigned devices.
	 * <strong>DO NOT</strong> call this in client/UI code; this is handled at a higher level.
	 * @param oldDevice The device to remove from the list.
	 */
	public void unassignDevice(Device oldDevice)
	{
		if (oldDevice == null)
			throw new NullPointerException("Parameter 'oldDevice' cannot be null.");
		if (respondingDevices.remove(oldDevice.getHash()) != null)
			fireEvent("unassigned", oldDevice);
	}

	/**
	 * Flags this incident as being archived.
	 * <strong>DO NOT</strong> call this in client/UI code; this is handled at a higher level.
	 */
	public void archive()
	{
		if (archived)
			return;
		archived = true;
		fireEvent("archived", this);
	}
	
	/**
	 * Gets ImageIcons based on incident type.
	 * @param type The type of incident you need an icon for.
	 * @param small true for 30x30, false for 50x50.
	 * @return An ImageIcon loaded with the appropriate image resource.
	 */
	public static ImageIcon getIcon(Incident.Type type, boolean small)
	{
		HashMap<Incident.Type, ImageIcon> collection = (small ? iconsSmall : iconsLarge);
		ImageIcon icon = collection.get(type);
		if (icon == null)
		{
			String filename = (small ? "_small" : "") + ".png";
			switch (type)
			{
				case Medical: filename = "images/medical_logo" + filename; break;
				case Security: filename = "images/security_logo" + filename; break;
				case WiFindUs: filename = "images/wifi_logo" + filename; break;
				case None: filename = "images/none_logo" + filename; break;
			}
			collection.put(type, icon = new ImageIcon(filename));
		}
		return icon;
	}
	
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	protected void mapEvents(String event, IncidentEventListener listener, Object... data)
	{
		switch(event)
		{
			case "created":
				listener.incidentCreated(this);
				break;
			case "archived":
				listener.incidentArchived(this);
				break;
			case "assigned":
				listener.incidentAssignedDevice(this, (Device)data[0]);
				break;
			case "unassigned":
				listener.incidentUnassignedDevice(this, (Device)data[0]);
				break;
		}
		
	}

	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
}
