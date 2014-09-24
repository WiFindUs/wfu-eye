package wifindus.eye;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import wifindus.EventObject;

/**
 * An security, medical or wifindus incident occurring in the field, as reported by field personnel. 
 * @author Mark 'marzer' Gillard
 */
public class Incident extends EventObject<IncidentEventListener>
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
		WiFindUs
	}
	
	//properties
	private int id;
	private Type type;
	private Location location = Location.EMPTY;
	private Timestamp created = new Timestamp(0);
	private boolean archived = false;
	//database relationships
	private ConcurrentHashMap<String,Device> respondingDevices = new ConcurrentHashMap<>();
	
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
	 */
	public Incident(int id, Type type, Location location, Timestamp created, IncidentEventListener... listeners)
	{
		super(listeners);
		
		if (id < 0)
			throw new IllegalArgumentException("Parameter 'id' cannot be negative.");
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
	public final int getId()
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
		}
		
	}

	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
}
