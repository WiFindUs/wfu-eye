package wifindus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ConfigFile is a set of key/value pairs, one per line,
 * with the KVP being delimited with a colon (:) or equals (=) symbol.
 * <br><br>
 * Keys may contain the characters A-Z, a-z, 0-9, and underscores, and are
 * stored in a case-insensitive manner (i.e. there is no difference between
 * "port" and "PORT").
 * <br><br>
 * Values can be anything non-blank. Values will have outer whitespace trimmed,
 * and if they're strings, the surrounding " or ' characters will be stripped.
 * <br><br>
 * String values don't have to use surrounding quotes, but macro replacement will
 * not be performed in this instance (e.g. if you include "\n" it won't get replaced
 * with a '\n' character).
 * <br><br>
 * Config files may include C++-style comments (//) that will be ignored by the parser.
 * @author Mark 'marzer' Gillard
 */
public class ConfigFile
{
	private static final Pattern PATTERN_CONFIG_KVP
	= Pattern.compile(
		"^([a-zA-Z0-9_]+)" //key name
		+ "[ \t]*[:=][ \t]*" //delimiter (w/ whitespace)
		+ "(.+)$" //value
		);
	private static final String PATTERN_COMMENT = "//.*$";
	private LinkedHashMap<String, String> kvps = new LinkedHashMap<>();
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Insantiates a ConfigFile using a Java.IO File object.
	 * @param file The File object representing the config file's path.
	 */
	public ConfigFile(File file)
	{
		if (file == null)
		{
			System.err.println("Error reading ConfigFile: null File parameter passed.");
			return;
		}
		
		if (!file.exists() || !file.isFile())
		{
			System.err.println("Error reading ConfigFile: does not exist or is a directory!");
			return;
		}
		
		try
		{
			readStream(new FileInputStream(file));
		}
		catch (IOException e)
		{
			System.err.println("Error reading ConfigFile: " + e.getMessage());
			return;
		}
	}
	
	/**
	 * Insantiates a ConfigFile from an embedded resource.
	 * @param file The name of the embedded resource from which to load the config.
	 */
	public ConfigFile(String resource)
	{
		if (resource == null)
		{
			System.err.println("Error reading ConfigFile: null resource parameter passed.");
			return;
		}
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream in = classLoader.getResourceAsStream(resource);
		if (in == null)
		{
			System.err.println("Error reading ConfigFile: getResourceAsStream() returned null");
			return;
		}
		readStream(in);
	}
	
	/**
	 * Gets an integer value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does
	 * not exist or did not contain a valid integer.
	 * @return The parsed value, or the default.
	 */
	public int get(String key, int defaultValue)
	{
		String val = getValue(key);
		if (val == null)
			return defaultValue;
		
		try
		{
			return Integer.parseInt(val);
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	/**
	 * Gets a float value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does
	 * not exist or did not contain a valid float.
	 * @return The parsed value, or the default.
	 */
	public float get(String key, float defaultValue)
	{
		String val = getValue(key);
		if (val == null)
			return defaultValue;
		
		try
		{
			return Float.parseFloat(val);
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	/**
	 * Gets a double value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does
	 * not exist or did not contain a valid double.
	 * @return The parsed value, or the default.
	 */
	public double get(String key, double defaultValue)
	{
		String val = getValue(key);
		if (val == null)
			return defaultValue;
		
		try
		{
			return Double.parseDouble(val);
		}
		catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	/**
	 * Gets a boolean value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does
	 * not exist. Considers, insensitive of case, "true", "on", "yes" and "1" as TRUE values, and anything else as FALSE.
	 * @return The parsed value, or the default.
	 */
	public boolean get(String key, boolean defaultValue)
	{
		String val = getValue(key);
		if (val == null)
			return defaultValue;
		
		val = val.toLowerCase();
		return val.equals("true") || val.equals("on") || val.equals("yes") || val.equals("1");
	}
	
	/**
	 * Gets a string value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does not exist.
	 * @return The parsed value, or the default.
	 */
	public String get(String key, String defaultValue)
	{
		String val = getValue(key);
		return val == null ? defaultValue : val;
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	private void readStream(InputStream in)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		try
		{
			//line by line
			while ((line = reader.readLine()) != null)
			{
				//strip comments and trim
				line = line.replaceAll(PATTERN_COMMENT, "").trim();
				if (line.length() == 0)
					continue;
				
				//check for match
				Matcher lineMatch = PATTERN_CONFIG_KVP.matcher(line);
				if (!lineMatch.find())
					continue;
				
				String key = lineMatch.group(1).toLowerCase();
				String value = lineMatch.group(2);
				
				//test for an explicit string
				if (value.length() >= 2 && 
					((value.charAt(0) == '\'' && value.charAt(value.length()-1) == '\'')
					|| (value.charAt(0) == '"' && value.charAt(value.length()-1) == '"')))
				{
					value = value.substring(1, value.length()-1)
							.replace("\\n", "\n")
							.replace("\\t", "\t");
				}
				
				//store
				kvps.put(key, value);
			}
			reader.close();
		}
		catch (IOException e)
		{
			System.err.println("Error reading ConfigFile: " + e.getMessage());
			return;
		}
	}
	
	private String getValue(String key)
	{
		if (key == null || (key = key.trim().toLowerCase()).isEmpty())
			return null;
		return kvps.get(key);
	}
}
