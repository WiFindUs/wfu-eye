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
	
	/**
	 * The mean radius of the earth, in kilometers
	 */
	public static final double EARTH_RADIUS_MEAN = 6378.1370;
	
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
		if (latitude != null && (latitude > 90.0 || latitude < -90.0))
			throw new IllegalArgumentException("Latitude must be between -90.0 and 90.0 (inclusive).");
		if (longitude != null && (longitude > 180.0 || longitude < -180.0))
			throw new IllegalArgumentException("Longitude must be between -180.0 and 180.0 (inclusive).");
		if (accuracy != null && accuracy <= 0.0)
			throw new IllegalArgumentException("Accuracy must be greater than 0m.");
		
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
	public final boolean isEmpty()
	{
		if (this == Location.EMPTY)
			return true;
		
		return latitude == null && longitude == null && accuracy == null && altitude == null;
	}
	
	/**
	 * Tests if this location structure has lat/long components
	 * @return TRUE if latitude and longtitude are not null, FALSE otherwise.
	 */
	public final boolean hasLatLong()
	{
		if (this == Location.EMPTY)
			return false;
		
		return latitude != null && longitude != null;
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
	
	@Override
	public boolean equals(Object other)
	{
		//object
		if (other == null)
			return false;
	    if (other == this)
	    	return true;
	    if (!(other instanceof Location))
	    	return false;
	    
	    //empty
	    Location loc = (Location)other;
	    if (loc.isEmpty() && isEmpty())
	    	return true;
	    if (loc.isEmpty() != isEmpty())
	    	return false;
	    
	    //latitude
	    if ((loc.getLatitude() == null && latitude != null)
	    	|| (loc.getLatitude() != null && latitude == null)
	    	|| (latitude != null && latitude.compareTo(loc.getLatitude()) != 0))
	    	return false;
	    
	    //longitude
	    if ((loc.getLongitude() == null && longitude != null)
	    	|| (loc.getLongitude() != null && longitude == null)
	    	|| (longitude != null && longitude.compareTo(loc.getLongitude()) != 0))
	    	return false;
    
	    //accuracy
	    if ((loc.getAccuracy() == null && accuracy != null)
	    	|| (loc.getAccuracy() != null && accuracy == null)
	    	|| (accuracy != null && accuracy.compareTo(loc.getAccuracy()) != 0))
	    	return false;
	    
	    //altitude
	    if ((loc.getAltitude() == null && altitude != null)
	    	|| (loc.getAltitude() != null && altitude == null)
	    	|| (altitude != null && altitude.compareTo(loc.getAltitude()) != 0))
	    	return false;

	    //if we get here, we're the same
	    return true;
	}
	
	@Override
	public String toString()
	{
		return (toShortString()+(accuracy == null ? "" : " (" + accuracy + "m acc.)")
			+(altitude == null ? "" : " " + altitude + "m alt.")).trim();
	}
	
	/**
	 * Gets a short lat/long only representation of this Location.
	 * @return A string object of the format 0.00000°N 0.000000°E
	 */
	public String toShortString()
	{
		return 
			((latitude == null ? "" : Math.abs(latitude) + "°" + (latitude >= 0 ? "N" : "S"))
			+(longitude == null ? "" : " " + Math.abs(longitude) + "°" + (longitude >= 0 ? "E" : "W"))
			).trim();
	}
	
	/**
	 * Returns the horizontal (spherical) distance between two GPS coordinates, according to the haversine formula.
	 * @param other The point to measure to, starting from the current one.
	 * @return Distance 'as the crow flies' between the two points, in <strong>meters</strong>.
	 * @throws NullPointerException if other is null
	 * @throws IllegalArgumentException if this or the other coordinate are missing horizontal positioning data
	 */
	public final double distanceTo(Location other) 
	{
		if (other == null)
			throw new NullPointerException("Parameter 'other' cannot be null.");
		if (!hasLatLong() || !other.hasLatLong())
			throw new IllegalArgumentException("Both location points must have horizontal positioning data");
		
		double latitudeDistance = Math.toRadians(latitude - other.getLatitude());
	    double longitudeDistance = Math.toRadians(longitude - other.getLongitude());
	    double d = Math.sin(latitudeDistance/2.0) * Math.sin(latitudeDistance/2.0) +
	               Math.cos(Math.toRadians(other.getLatitude())) * Math.cos(Math.toRadians(latitude)) *
	               Math.sin(longitudeDistance/2.0) * Math.sin(longitudeDistance/2.0);
	    
	    return (2.0 * Math.atan2(Math.sqrt(d), Math.sqrt(1.0-d))) * EARTH_RADIUS_MEAN * 1000.0;
	}
}
