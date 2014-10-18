package wifindus;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import wifindus.eye.Location;

/**
 * An immutable, serializable packet of data describing a rectangular area bound by GPS coordinates,
 * with the purpose of mapping those coordinates to a screen space.
 * @author Mark 'marzer' Gillard
 */
public class GPSRectangle implements Serializable
{
	private static final long serialVersionUID = 5246886591603837227L;
	private double latitudeStart, latitudeEnd, longitudeStart, longitudeEnd;
	private double width, height;
	
	/**
	 * Creates a new GPS rectangle using the given coordinates.
	 * @param latStart The latitude component of the top-left corner of the rectangle.
	 * @param longStart The longitude component of the top-left corner of the rectangle.
	 * @param latEnd The latitude component of the bottom-right corner of the rectangle.
	 * @param longEnd The longitude component of the bottom-right corner of the rectangle.
	 * @throws IllegalArgumentException if you provide values that don't make sense
	 */
	public GPSRectangle(double latStart, double longStart, double latEnd, double longEnd)
	{
		if (latStart > 90.0 || latStart < -90.0 || latEnd > 90.0 || latEnd < -90.0)
			throw new IllegalArgumentException("Latitudes must be between -90.0 and 90.0 (inclusive).");
		if (longStart > 180.0 || longStart < -180.0 || longEnd > 180.0 || longEnd < -180.0)
			throw new IllegalArgumentException("Longitudes must be between -180.0 and 180.0 (inclusive).");
		if (longEnd < longStart)
			throw new IllegalArgumentException("End longitude must be greater than Start longitude (left-to-right).");
		if (latStart < latEnd)
			throw new IllegalArgumentException("End latitude must be lower than Start latitude (top-to-bottom).");
		
		latitudeStart = latStart;
		latitudeEnd = latEnd;
		longitudeStart = longStart;
		longitudeEnd = longEnd;
		
		width = longitudeEnd - longitudeStart;
		height = latitudeStart - latitudeEnd;
	}
	
	/**
	 * Checks if a coordinate is contained by this GPSRectangle.
	 * @param latitude The latitude of the coordinate
	 * @param longitude The longitude of the coordinate
	 * @return TRUE if the given point is contained within this rectangle.
	 */
	public boolean contains(double latitude, double longitude)
	{
		return latitude <= latitudeStart
			&& latitude >= latitudeEnd
			&& longitude >= longitudeStart
			&& longitude <= longitudeEnd;
	}
	
	/**
	 * Checks if a coordinate is contained by this GPSRectangle.
	 * @param coords The location structure to check.
	 * @return TRUE if the given point is contained within this rectangle.
	 */
	public boolean contains(Location coords)
	{
		if (coords == null || !coords.hasLatLong())
			return false;
		return contains(coords.getLatitude(), coords.getLongitude());
	}
	
	public Point translate(Rectangle target, double latitude, double longitude)
	{
		return new Point(
				target.x + (int)(((longitude - longitudeStart) / width) * (double)target.width),
				target.y + (int)(((latitudeStart - latitude) / height) * (double)target.height)
			);
	}
	
	public Point translate(Rectangle target, Location coords)
	{
		return translate(target, coords.getLatitude(), coords.getLongitude());
	}
}
