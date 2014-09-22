package wifindus.eye;

import java.io.Serializable;

/**
 * An immutable, serializable packet of data describing an object's location.
 * Values within are represented with Java's boxing Double type to allow for incomplete location data
 * (as reporting devices may not fully report their location, or may omit information).
 * @author Mark 'marzer' Gillard
 */
public class Location implements Serializable
{
	/**
	 * A location with no data. 
	 */
	public static final Location EMPTY = new Location();
	
	private Double latitude = null;
	private Double longitude = null;
	private Double altitude = null;
	private Double accuracy = null;
	private static final long serialVersionUID = -8132425175759103068L;
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a new Location object.
	 * @param latitude The latitude of the coordinate. Use null for 'no data'.
	 * @param longitude The longitude of the coordinate. Use null for 'no data'.
	 * @param accuracy The radius of 68% confidence as reported by the device. Use null for 'no data'.
	 * @param altitude The altitude of of the coordinate, as meters above sea level. Use null for 'no data'.
	 */
	public Location (Double latitude, Double longitude, Double accuracy, Double altitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.accuracy = accuracy;
	}
	
	/**
	 * Creates a new Location object.
	 * @param latitude The latitude of the coordinate. Use null for 'no data'.
	 * @param longitude The longitude of the coordinate. Use null for 'no data'.
	 * @param accuracy The radius of 68% confidence as reported by the device. Use null for 'no data'.
	 */
	public Location(Double latitude, Double longitude, Double accuracy)
	{
		this (latitude, longitude, accuracy, null);
	}
	
	/**
	 * Creates a new Location object.
	 * @param latitude The latitude of the coordinate. Use null for 'no data'.
	 * @param longitude The longitude of the coordinate. Use null for 'no data'.
	 */
	public Location(Double latitude, Double longitude)
	{
		this (latitude, longitude, null);
	}
	
	/**
	 * Creates a new Location object with null information.
	 */
	private Location()
	{
		this (null, null);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Tests if this is an entirely empty Location structure.
	 * @return TRUE if all members of this Location are NULL, FALSE otherwise.
	 */
	public boolean isEmpty()
	{
		if (this == Location.EMPTY)
			return true;
		
		return latitude == null && longitude == null && accuracy == null && altitude == null;
	}
	
	/**
	 * Returns the latitude of the location 
	 * @return A value between -90.0 and 90.0 (inclusive), or null (for no data).
	 */
	public final Double getLatitude()
	{
		return latitude;
	}
	
	/**
	 * Returns the longitude of the location 
	 * @return A value between -180.0 and 180.0 (inclusive), or null (for no data).
	 */
	public final Double getLongitude()
	{
		return longitude;
	}
	
	/**
	 * Returns the radius of 68% confidence of the horizontal positioning
	 * @return A value between 0 and 9999.9999 (inclusive), or null (for no data).
	 */
	public final Double getAccuracy()
	{
		return accuracy;
	}
	
	/**
	 * Returns the altitude of the location as meters above sea level.
	 * @return A value greater than or equal to 0.0, or null (for no data).
	 */
	public final Double getAltitude()
	{
		return altitude;
	}
}
