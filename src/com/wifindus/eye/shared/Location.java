package com.wifindus.eye.shared;

import java.io.Serializable;

public class Location implements Serializable
{
	/**
	 * Since 0.0 is an acceptable altitude value, we're considering anything lower than
	 * 12262.0 meters below sea level as 'invalid' (or NULL from mysql), since this is
	 * the depth of the Kola superdeep borehole and is the lowest known point on the earth's
	 * surface.
	 */
	public static final double MINIMUM_ALTITUDE = -12262.0;
	
	private double latitude;
	private double longitude;
	private double altitude;
	private static final long serialVersionUID = -8132425175759103068L;
	
	public Location (double latitude, double longitude, double altitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}
	
	public Location(double latitude, double longitude)
	{
		this (latitude, longitude, MINIMUM_ALTITUDE-1.0);
	}
	
	public Location()
	{
		this (0.0, 0.0, MINIMUM_ALTITUDE-1.0);
	}

	public final double getLatitude()
	{
		return latitude;
	}

	public final void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public final double getLongitude()
	{
		return longitude;
	}

	public final void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public final double getAltitude()
	{
		return this.altitude;
	}

	public final void setAltitude(double altitude)
	{
		this.altitude = altitude;
	}
	
	/**
	 * Set's this location's altitude component to null/invalid.
	 */
	public final void clearAltitude()
	{
		this.altitude = MINIMUM_ALTITUDE-1.0;
	}
	
	/**
	 * Checks if this location has a valid altitude.
	 * @return True if the location contains an altitude value greater than or equal to {@link #MINIMUM_ALTITUDE}, false otherwise (represents no data).
	 */
	public final boolean hasAltitude()
	{
		return this.altitude >= MINIMUM_ALTITUDE; 
	}
}
