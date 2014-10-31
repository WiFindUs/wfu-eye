package wifindus.eye;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wifindus.EventObject;
import wifindus.MySQLResultRow;
import wifindus.MySQLUpdateTarget;
import wifindus.ResourcePool;

/**
 * An security, medical or wifindus incident occurring in the field, as reported by field personnel. 
 * @author Mark 'marzer' Gillard
 */
public class Incident extends EventObject<IncidentEventListener> implements MySQLUpdateTarget, MappableObject
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
	
	public static final Color COLOR_NONE = new Color(192,192,192);
	public static final Color COLOR_MEDICAL = new Color(255,102,102);
	public static final Color COLOR_SECURITY = new Color(102,178,255);
	public static final Color COLOR_WIFINDUS= new Color(178,255,102);
	
	//properties
	private int id;
	private Type type;
	private Location location = Location.EMPTY;
	private Timestamp created;
	private boolean archived = false;
	private Timestamp archivedTime;
	private int severity = 0;
	private String code = ""; 
	private String description = "";
	
	//database relationships
	private transient volatile ConcurrentHashMap<String,Device> respondingDevices = new ConcurrentHashMap<>();
	private transient volatile User reportingUser = null;
	private transient volatile ConcurrentHashMap<Integer,User> archivedResponders = new ConcurrentHashMap<>();
	
	//marker stuff
	private static final Image tagImage, tagHoverImage, tagSelectedImage;
	private static final Map<Incident.Type, Image> adornmentImages = new HashMap<>();
	static
	{
		ResourcePool.loadImage("tag", "images/tag.png" );
		ResourcePool.loadImage("tag_hover", "images/tag_hover.png" );
		ResourcePool.loadImage("tag_selected", "images/tag_selected.png" );
		ResourcePool.loadImage("cog_white", "images/cog_white.png" );
		ResourcePool.loadImage("cross_white", "images/cross_white.png" );
		ResourcePool.loadImage("shield_white", "images/shield_white.png" );
		
		tagImage = ResourcePool.getImage("tag");
		tagHoverImage = ResourcePool.getImage("tag_hover");
		tagSelectedImage = ResourcePool.getImage("tag_selected");
		adornmentImages.put(Incident.Type.Medical, ResourcePool.getImage("cross_white"));
		adornmentImages.put(Incident.Type.Security, ResourcePool.getImage("shield_white"));
		adornmentImages.put(Incident.Type.WiFindUs, ResourcePool.getImage("cog_white"));
	}
	
	
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
		if (!location.hasLatLong())
			throw new IllegalArgumentException("Parameter 'location' is missing horizontal positioning data.");
		
		this.id = id;
		this.type = type;
		this.location = location;
		this.created = created;
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
	 * Gets this Incident's archival timestamp.
	 * @return An sql.Timestamp object representing the date/time the incident was archived.
	 * Since Timestamp is a mutable structure, the returned object is a clone, and thus calling any
	 * mutators will have no effect on the Incident's timestamp data.
	 * 
	 * Note that this will return null if the incident has not yet been archived.
	 */
	public final Timestamp getArchivedTime()
	{
		return (Timestamp)archivedTime.clone();
	}
	
	/**
	 * Gets the severity of the incident.
	 * @return An integer containing the incident's severity level.
	 */
	public final int getSeverity()
	{
		return severity;
	}
	
	/**
	 * Gets the code of the incident.
	 * @return A string containing the incident's code.
	 */
	public final String getCode()
	{
		return code;
	}
	
	/**
	 * Gets the general description of the incident
	 * @return A string containing the incident's description.
	 */
	public final String getDescription()
	{
		return description;
	}
	
	/**
	 * Gets the User who called in this incident, if applicable.
	 * @return A reference to a User object representing the user who reported the incident, or null if this information is not available.  
	 */
	public final User getReportingUser()
	{
		return reportingUser;
	}
	
	/**
	 * Gets a list of all Users who using devices that were assigned to this incident at the time of it's archival.  
	 * @return An ArrayList of Devices assigned to this Incident.
	 * Since the backing collection is a mutable structure, the returned list is a clone,
	 * and thus calling any mutators will have no effect on the backing collection data.
	 * 
	 * Note that the list returned by this function will return null if the incident has not yet been archived.
	 */
	public final ArrayList<User> getArchivedResponders()
	{
		return archived ? new ArrayList<User>(archivedResponders.values()) : null;
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
		switch (key.toUpperCase())
		{
			case "MED": return Incident.Type.Medical;
			case "SEC": return Incident.Type.Security;
			case "WFU": return Incident.Type.WiFindUs;
		}
		
		throw new IllegalArgumentException("Parameter 'key' does not match an Incident.Type database key.");
	}
	
	/**
	 * Gets a database key from a Type.
	 * @param type The type to convert to it's corresponding key.
	 * @return A string containing the database key for the type.
	 * @throws IllegalArgumentException If you pass an invalid type (e.g. None).
	 */
	public static final String getDatabaseKeyFromType(Type type)
	{
		switch (type)
		{
			case Medical: return "MED";
			case Security: return "SEC";
			case WiFindUs: return "WFU";
			default:
				throw new IllegalArgumentException("Parameter 'type' was a value that is not currently supported in the database.");
		}
	}
	
	@Override
	public String toString()
	{
		return "Incident["+getID()+"]";
	}
	
	@Override
	public void updateFromMySQL(MySQLResultRow resultRow)
	{
		if (resultRow == null)
			throw new NullPointerException("Parameter 'resultRow' cannot be null.");
		if (((Integer)resultRow.get("id")).intValue() != getID())
			throw new IllegalArgumentException("Parameter 'resultRow' does not have the same primary key as this object.");

		int severity = (int)resultRow.get("severity");
		if (severity != this.severity)
		{
			int oldSeverity = this.severity;
			this.severity = severity;
			fireEvent("severity",oldSeverity,this.severity);
		}
		
		String code = ((String)resultRow.get("code")).trim();
		if (!code.equals(this.code))
		{
			String oldCode = this.code;
			this.code = code;
			fireEvent("code",oldCode,this.code);
		}
		
		updateDescription((String)resultRow.get("description"));
			
		boolean archived = (boolean)resultRow.get("archived");
		if (!this.archived && archived)
			archive((Timestamp)resultRow.get("archivedTime"));
	}
	
	/**
	 * Adds a user to the list of archived responders.
	 * <strong>DO NOT</strong> call this in client/UI code; this is handled at a higher level.
	 * @param responder The user to add to the list.
	 */
	public void assignArchivedResponder(User responder)
	{
		if (responder == null)
			throw new NullPointerException("Parameter 'responder' cannot be null.");
		if (!archived)
			throw new IllegalArgumentException("You cannot add an archived responder to an incident not flagged as archived.");
		Integer key = Integer.valueOf(responder.getID());
		if (archivedResponders.containsKey(key))
			return;
		archivedResponders.put(key, responder);
		fireEvent("reponderadded", responder);
	}
	
	/**
	 * Sets this incident's reporting User.
	 * <strong>DO NOT</strong> call this in client/UI code; this is handled at a higher level.
	 * @param newDevice The user to set as the reporting user.
	 */
	public void updateReportingUser(User user)
	{
		if (user == reportingUser)
			return;
		User oldReportingUser = reportingUser;
		reportingUser = user;
		fireEvent("reportinguser", oldReportingUser, reportingUser);
	}
	
	/**
	 * Sets this incident's description.
	 * <strong>DO NOT</strong> call this in client/UI code; this is handled at a higher level.
	 * @param description The description to set.
	 */
	public void updateDescription(String description)
	{
		if (description == null)
			description = "";
		if ((description = description.trim()).equals(this.description))
			return;
		this.description = description;
		fireEvent("description");
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
	public void archive(Timestamp archivedTime)
	{
		if (archived)
			return;
		archived = true;
		this.archivedTime = archivedTime;
		fireEvent("archived", this);
	}
	
	@Override
	public void paintMarker(Graphics2D graphics, int x, int y, boolean isHovering, boolean isSelected)
	{
		Image adornment = adornmentImages.get(type);
		int left = x-32;
		int top = y-84;
		graphics.drawImage(isSelected ? tagSelectedImage : tagImage, left, top, null);
		if (adornment != null)
			graphics.drawImage(adornment, left+8, top+8, 48, 48, null);
		if (isHovering)
			graphics.drawImage(tagHoverImage, left, top, null);
	}
	
	@Override
	public Polygon generateMarkerHitbox(int x, int y)
	{
		Polygon poly = new Polygon();
		poly.addPoint(x, y);
		poly.addPoint(x-12,y-19);
		poly.addPoint(x-32,y-19);
		poly.addPoint(x-32,y-83);
		poly.addPoint(x+31,y-83);
		poly.addPoint(x+31,y-19);
		poly.addPoint(x+11,y-19);
		return poly;
	}

	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	protected void mapEvents(String event, IncidentEventListener listener, Object... data)
	{
		switch(event)
		{
			case "archived":
				listener.incidentArchived(this);
				break;
			case "assigned":
				listener.incidentAssignedDevice(this, (Device)data[0]);
				break;
			case "unassigned":
				listener.incidentUnassignedDevice(this, (Device)data[0]);
				break;
			case "reponderadded":
				listener.incidentArchivedResponderAdded(this, (User)data[0]);
				break;
			case "severity":
				listener.incidentSeverityChanged(this, (int)data[0], (int)data[1]);
				break;
			case "code":
				listener.incidentCodeChanged(this, (String)data[0], (String)data[1]);
				break;
			case "reportinguser":
				listener.incidentReportingUserChanged(this, (User)data[0], (User)data[1]);
				break;
			case "description":
				listener.incidentDescriptionChanged(this);
				break;
		}
	}

	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
}
