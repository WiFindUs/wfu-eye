package wifindus.eye;

import java.util.concurrent.ConcurrentHashMap;
import wifindus.EventObject;

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
	//database relationships
	private ConcurrentHashMap<String,Device> respondingDevices = new ConcurrentHashMap<>();
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public Incident(int id, Type type, Location location, IncidentEventListener... listeners)
	{
		super(listeners);
		
		if (id < 0)
			throw new IllegalArgumentException("Parameter 'id' cannot be negative.");
		if (location == null)
			throw new IllegalArgumentException("Parameter 'location' cannot be null.");
		if (location.isEmpty())
			throw new IllegalArgumentException("Parameter 'location' cannot be empty.");
		
		this.id = id;
		this.type = type;
		this.location = location;
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	protected void mapEvents(String event, IncidentEventListener listener,
			Object data)
	{
		switch(event)
		{
			case "created":
				listener.incidentCreated(this);
				break;
			case "deleted":
				listener.incidentDeleted(this);
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
