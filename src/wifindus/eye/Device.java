package wifindus.eye;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import wifindus.EventObject;

/**
 * A client device in use by medical, security or wifindus personnel.
 * @author Mark 'marzer' Gillard
 */
public class Device extends EventObject<DeviceEventListener>
{
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
	private Date lastUpdate = new Date(0);
	//database relationships
	private User currentUser = null;
	private Incident currentIncident = null;
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	public Device(String hash, Type type, DeviceEventListener... listeners)
	{
		super(listeners);
		
		if (hash == null)
			throw new IllegalArgumentException("Parameter 'hash' cannot be null.");
		if (!Hash.isValid(hash))
			throw new IllegalArgumentException("Parameter 'hash' is not a valid WFU device hash ("+hash+").");
		if (type == Type.Computer)
			throw new IllegalArgumentException("Parameter 'type' does not currently support Type.Computer.");
		
		this.hash = hash;
		this.type = type;
	}

	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	protected void mapEvents(String event, DeviceEventListener listener, Object data)
	{
		switch(event)
		{
			case "created":
				listener.deviceCreated(this);
				break;
			case "timedout":
				listener.deviceTimedOut(this);
				break;
			case "loggedin":
				listener.deviceUserLoggedIn(this, (User)data);
				break;
			case "loggedout":
				listener.deviceUserLoggedOut(this, (User)data);
				break;
			case "location":
				listener.deviceLocationChanged(this);
				break;
			case "atmosphere":
				listener.deviceAtmosphereChanged(this);
				break;
			case "address":
				listener.deviceAddressChanged(this);
				break;
			case "updated":
				listener.deviceUpdated(this);
				break;
			case "assigned":
				listener.deviceAssignedIncident(this, (Incident)data);
				break;
			case "unassigned":
				listener.deviceUnassignedIncident(this, (Incident)data);
				break;
			
		}
		
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
}
