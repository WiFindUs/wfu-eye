package wifindus;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
	private double width, height;
	private Location northWest;
	private Location southEast;
	private Location center;
	
	/**
	 * Creates a new GPS rectangle using the given coordinates.
	 * @param northLatitude The latitude component of the top-left corner of the rectangle.
	 * @param westLongitude The longitude component of the top-left corner of the rectangle.
	 * @param southLatitude The latitude component of the bottom-right corner of the rectangle.
	 * @param eastLongitude The longitude component of the bottom-right corner of the rectangle.
	 * @throws IllegalArgumentException if you provide values that don't make sense
	 */
	public GPSRectangle(double northLatitude, double westLongitude, double southLatitude, double eastLongitude)
	{
		if (northLatitude > 90.0 || northLatitude < -90.0 || southLatitude > 90.0 || southLatitude < -90.0)
			throw new IllegalArgumentException("Latitudes must be between -90.0 and 90.0 (inclusive).");
		if (westLongitude > 180.0 || westLongitude < -180.0 || eastLongitude > 180.0 || eastLongitude < -180.0)
			throw new IllegalArgumentException("Longitudes must be between -180.0 and 180.0 (inclusive).");
		if (eastLongitude < westLongitude)
			throw new IllegalArgumentException("East longitude must be greater than West longitude (left-to-right).");
		if (northLatitude < southLatitude)
			throw new IllegalArgumentException("End latitude must be lower than Start latitude (top-to-bottom).");
		
		northWest = new Location(Double.valueOf(northLatitude), Double.valueOf(westLongitude));
		southEast = new Location(Double.valueOf(southLatitude), Double.valueOf(eastLongitude));
		width = eastLongitude - westLongitude;
		height = northLatitude - southLatitude;
		center = new Location(Double.valueOf(northLatitude - (height/2.0)), Double.valueOf(westLongitude + (width/2.0)));
	}
	
	/**
	 * Checks if a coordinate is contained by this GPSRectangle.
	 * @param latitude The latitude of the coordinate
	 * @param longitude The longitude of the coordinate
	 * @return TRUE if the given point is contained within this rectangle.
	 */
	public boolean contains(double latitude, double longitude)
	{
		return latitude <= northWest.getLatitude()
			&& latitude >= southEast.getLatitude()
			&& longitude >= northWest.getLongitude()
			&& longitude <= southEast.getLongitude();
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
	
	public Location getNorthWest()
	{
		return northWest;
	}
	
	public Location getCenter()
	{
		return center;
	}
	
	public Location getSouthEast()
	{
		return southEast;
	}
	
	public Point2D.Double translate(Rectangle2D.Double target, double latitude, double longitude)
	{
		return new Point2D.Double(
				target.x + (((longitude - northWest.getLongitude()) / width) * target.width),
				target.y + (((northWest.getLatitude() - latitude) / height) * target.height)
			);
	}
	
	public Point2D.Double translate(Rectangle2D.Double target, Location coords)
	{
		return translate(target, coords.getLatitude(), coords.getLongitude());
	}
	
	public Point translate(Rectangle target, double latitude, double longitude)
	{
		return new Point(
				target.x + (int)(((longitude - northWest.getLongitude()) / width) * (double)target.width),
				target.y + (int)(((northWest.getLatitude() - latitude) / height) * (double)target.height)
			);
	}
	
	public Point translate(Rectangle target, Location coords)
	{
		return translate(target, coords.getLatitude(), coords.getLongitude());
	}
}
