package wifindus.eye;

import java.io.Serializable;

/**
 * An immutable, serializable packet of data describing the atmospheric condition at an object's location.
 * Values within are represented with Java's boxing Double type to allow for incomplete data
 * (as reporting devices may not fully report their conditions, or may lack sensors to do so).
 * @author Mark 'marzer' Gillard
 */
public class Atmosphere implements Serializable
{
	/**
	 * An Atmosphere with no data. 
	 */
	public static final Atmosphere EMPTY = new Atmosphere();
	
	private Double humidity = null;
	private Double airPressure = null;
	private Double temperature = null;
	private Double lightLevel = null;
	private static final long serialVersionUID = -160414951139431493L;
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates a new Atmosphere object.
	 * @param humidity The relative humidity (as a percentage) recorded by the device's hygrometer (if present). Use null for 'no data'.
	 * @param airPressure Barometric pressure (in mbar) recorded by the device's pressure sensor (if present). Use null for 'no data'.
	 * @param temperature Temperature (in degrees celcius) recorded by the device's temperature sensor (if present). Use null for 'no data'.
	 * @param lightLevel Ambient light level (in lux) recorded by the device's light sensor (if present). Use null for 'no data'.
	 */
	public Atmosphere (Double humidity, Double airPressure, Double temperature, Double lightLevel)
	{
		this.humidity = humidity;
		this.airPressure = airPressure;
		this.temperature = temperature;
		this.lightLevel = lightLevel;
	}
	
	/**
	 * Creates a new Atmosphere object.
	 * @param humidity The relative humidity (as a percentage) recorded by the device's hygrometer (if present). Use null for 'no data'.
	 * @param airPressure Barometric pressure (in mbar) recorded by the device's pressure sensor (if present). Use null for 'no data'.
	 * @param temperature Temperature (in degrees celcius) recorded by the device's temperature sensor (if present). Use null for 'no data'.
	 */
	public Atmosphere (Double humidity, Double airPressure, Double temperature)
	{
		this (humidity, airPressure, temperature, null);
	}
	
	/**
	 * Creates a new Atmosphere object.
	 * @param humidity The relative humidity (as a percentage) recorded by the device's hygrometer (if present). Use null for 'no data'.
	 * @param airPressure Barometric pressure (in mbar) recorded by the device's pressure sensor (if present). Use null for 'no data'.
	 */
	public Atmosphere (Double humidity, Double airPressure)
	{
		this (humidity, airPressure, null);
	}
	
	/**
	 * Creates a new Atmosphere object.
	 * @param humidity The relative humidity (as a percentage) recorded by the device's hygrometer (if present). Use null for 'no data'.
	 */
	public Atmosphere (Double humidity)
	{
		this (humidity, null);
	}
	
	/**
	 * Creates a new Atmosphere object with null information.
	 */
	private Atmosphere()
	{
		this (null);
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Tests if this is an entirely empty Atmosphere structure.
	 * @return TRUE if all members of this Atmosphere are NULL, FALSE otherwise.
	 */
	public final boolean isEmpty()
	{
		if (this == Atmosphere.EMPTY)
			return true;
		
		return humidity == null && airPressure == null && temperature == null && lightLevel == null;
	}
	
	/**
	 * Returns the relative humidity as a percentage
	 * @return A value between 0.0 and 1.0 (inclusive), or null (for no data).
	 */
	public final Double getHumidity()
	{
		return humidity;
	}
	
	/**
	 * Returns the air pressure in millibar
	 * @return A value greater than or equal to 0.0, or null (for no data).
	 */
	public final Double getAirPressure()
	{
		return airPressure;
	}
	
	/**
	 * Returns the temperature in degrees celcius
	 * @return A double, or null (for no data).
	 */
	public final Double getTemperature()
	{
		return temperature;
	}
	
	/**
	 * Returns the light level in lux
	 * @return A value greater than or equal to 0.0, or null (for no data).
	 */
	public final Double getLightLevel()
	{
		return lightLevel;
	}
}
