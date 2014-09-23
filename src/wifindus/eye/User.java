package wifindus.eye;

import wifindus.EventObject;

/**
 * A member of medical, security, or WiFindUs personnel.
 * @author Mark 'marzer' Gillard
 */
public class User extends EventObject<UserEventListener>
{
	//properties
	private int id;
	private String nameFirst, nameLast, nameMiddle;
	private Incident.Type type;
	private Device currentDevice = null;
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a new User, representing an User entry from the database.
	 * @param id The automatically-assigned unique integer key of this User.
	 * @param type The user's type (i.e. what sort of Incident they can respond to).
	 * @param nameFirst The user's first (given) name.
	 * @param nameMiddle The user's middle (given) name(s).
	 * @param nameLast The user's last (family) name.
	 * @param listeners A variable-length list of event listeners that will watch this incident's state.
	 * @throws NullPointerException if <code>nameFirst</code>, <code>nameMiddle</code> or <code>nameLast</code> are null.
	 * @throws IllegalArgumentException if <code>id</code> is negative.
	 */
	public User(int id, Incident.Type type, String nameFirst, String nameMiddle, String nameLast, UserEventListener... listeners)
	{
		super(listeners);
		
		if (id < 0)
			throw new IllegalArgumentException("Parameter 'id' cannot be negative.");
		if (nameFirst == null)
			throw new NullPointerException("Parameter 'nameFirst' cannot be null.");
		if (nameMiddle == null)
			throw new NullPointerException("Parameter 'nameMiddle' cannot be null.");
		if (nameLast == null)
			throw new NullPointerException("Parameter 'nameLast' cannot be null.");
		
		this.id = id;
		this.type = type;
		this.nameFirst = nameFirst.trim();
		this.nameMiddle = nameMiddle.trim();
		this.nameLast = nameLast.trim();
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets this User's ID.
	 * @return An integer representing this User's automatically-assigned id key.
	 */
	public final int getId()
	{
		return id;
	}
	
	/**
	 * Gets this User's Type.
	 * @return A Incident.Type value indicating what type of incident the user represented by this User object can respond to. 
	 */
	public final Incident.Type getType()
	{
		return type;
	}
	
	/**
	 * Gets this User's first (given) name.
	 * @return A string containing the user's first given name.
	 */
	public final String getNameFirst()
	{
		return nameFirst;
	}
	
	/**
	 * Gets this User's middle (given) name(s).
	 * @return A string containing the user's first given name.
	 */
	public final String getNameMiddle()
	{
		return nameMiddle;
	}
	
	/**
	 * Gets this User's last (family) name.
	 * @return A string containing the user's surname.
	 */
	public final String getNameLast()
	{
		return nameLast;
	}
	
	/**
	 * Gets this User's full name.
	 * @return A string containing the user's full name
	 * as a human would write/speak it (e.g. <code>Lee Harvey Oswald</code>).
	 */
	public final String getNameFull()
	{
		return nameFirst + " "
			+ (nameMiddle.isEmpty() ? "" : " " + nameMiddle)
			+ nameLast;
	}
	
	/**
	 * Gets this User's full name.
	 * @return A string containing the user's full name
	 * as it would appear in a surname-indexed list (e.g. <code>Oswald, Lee Harvey</code>).
	 */
	public final String getNameLogical()
	{
		return nameLast + ", "
				+ nameFirst
				+ (nameMiddle.isEmpty() ? "" : " " + nameMiddle);
	}
	
	/**
	 * Gets the Device the user is currently signed in to.
	 * @return A reference to a Device object representing the device this User is currently signed in to, or NULL if this user is not signed in.  
	 */
	public final Device getCurrentDevice()
	{
		return currentDevice;
	}
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	@Override
	protected void mapEvents(String event, UserEventListener listener, Object... data)
	{
		switch(event)
		{
			case "created":
				listener.userCreated(this);
				break;
		}
		
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
}
