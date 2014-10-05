package wifindus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ConfigFile is a collection of key/value pairs parsed from text files, one per line,
 * with the KVP being delimited with a colon (:) or equals (=) symbol. Keys may contain
 * the characters A-Z, a-z, 0-9, hyphens, periods and underscores, and are stored in a
 * case-insensitive manner (i.e. there is no difference between "port" and "PORT"); Values
 * can be anything non-blank (outer whitespace will be trimmed and string values will have
 * the surrounding " or ' characters stripped).
 * <br><br>
 * String values may be provided without surrounding quotes, but macro replacement will
 * not be performed in this instance (e.g. "\n" will not be substituted with the '\n' character).
 * Config files may include C++-style comments (//) that will be ignored by the parser.
 * @author Mark 'marzer' Gillard
 */
public class ConfigFile implements Serializable
{
	private static final long serialVersionUID = -1878576970458542468L;
	private transient static final Pattern PATTERN_KEY = Pattern.compile( "[a-zA-Z0-9_\\-.]+(?:\\[[ \t]*\\])?" );
	private transient static final Pattern PATTERN_COMMENT = Pattern.compile( "//.*$" );
	private transient static final Pattern PATTERN_CONFIG_KVP
		= Pattern.compile( "^("+ PATTERN_KEY.pattern() +")[ \t]*[:=][ \t]*(.+)$");
	
	private Map<String, String> kvps = new ConcurrentHashMap<>();
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Instantiates an empty ConfigFile.
	 */
	public ConfigFile()
	{
		
	}
	
	/**
	 * Instantiates a ConfigFile using a Java.IO File object.
	 * @param file The File object representing the config file's path.
	 */
	public ConfigFile(File file)
	{
		try
		{
			read(file);
		}
		catch (Exception e)
		{
			System.err.println("A " + e.getClass().getName() + " was thrown reading file "
				+ (file == null ? "'null'" : file.getAbsolutePath()) + "; it has been ignored.");
		}
	}
	
	/**
	 * Instantiates a ConfigFile using a list of Java.IO File objects.
	 * @param files The iterable list of file paths to load. Any that are not found or throw an exception will be skipped.
	 */
	public ConfigFile(Iterable<File> files)
	{
		read(files);
	}
	
	/**
	 * Instantiates a ConfigFile from an embedded resource.
	 * @param resource The name of the embedded resource from which to load the config.
	 */
	public ConfigFile(String resource)
	{
		try
		{
			read(resource);
		}
		catch (Exception e)
		{
			System.err.println("A " + e.getClass().getName() + " was thrown reading resource file "
				+ (resource == null ? "'null'" : resource) + "; it has been ignored.");
		};
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////

	/**
	 * Reads a file using a Java.IO File object. Reading a file into the ConfigFile will not delete existing keys,
	 * so multiple files may be read to create one virtual configuration. Existing kvp's will only be overwritten
	 * if an existing key is found in subsequent files.
	 * @param file The File object representing the config file's path.
	 * @throws FileNotFoundException if the file path did not exist or was not a file.
	 */
	public void read(File file) throws FileNotFoundException
	{
		if (file == null)
			throw new NullPointerException("Parameter 'file' cannot be null.");
		if (!file.exists())
			throw new FileNotFoundException(file.getAbsolutePath()+" does not exist.");
		if (!file.isFile())
			throw new FileNotFoundException(file.getAbsolutePath()+" is not a file.");
		
		try
		{
			readStream(new FileInputStream(file));
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Reads a list of Java.IO File objects. Reading a file into the ConfigFile will not delete existing keys,
	 * so multiple files may be read to create one virtual configuration. Existing kvp's will only be overwritten
	 * if an existing key is found in subsequent files.
	 * @param files The iterable list of file paths to load. Any that are not found or throw an exception will be skipped.
	 */
	public void read(Iterable<File> files)
	{
		if (files == null)
			throw new NullPointerException("Parameter 'files' cannot be null.");
		
		for (File file : files)
		{
			try
			{
				read(file);
			}
			catch (Exception e)
			{
				System.err.println("A " + e.getClass().getName() + " was thrown reading file "
					+ (file == null ? "'null'" : file.getAbsolutePath()) + "; it has been ignored.");
			}
		}
	}
	
	/**
	 * Reads a file from an embedded resource. Reading a file into the ConfigFile will not delete existing keys,
	 * so multiple files may be read to create one virtual configuration. Existing kvp's will only be overwritten
	 * if an existing key is found in subsequent files.
	 * @param resource The name of the embedded resource from which to load the config.
	 * @throws FileNotFoundException if the ClassLoader could not find the given file resource.
	 */
	public void read(String resource) throws FileNotFoundException
	{
		if (resource == null)
			throw new NullPointerException("Parameter 'resource' cannot be null.");
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream in = classLoader.getResourceAsStream(resource);
		if (in == null)
			throw new FileNotFoundException("getResourceAsStream() returned null");
		readStream(in);
	}
	
	/**
	 * Gets an integer value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does
	 * not exist or did not contain a valid integer.
	 * @return The parsed value, or defaultValue if the key did not exist or the value could not be converted to an integer.
	 */
	public int getInt(String key, int defaultValue)
	{
		String val = kvps.get(checkKey(key));
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
	 * Gets an integer value from the ConfigFile.
	 * @param key The key to look up.
	 * @return The parsed value, or 0 if the key did not exist or the value could not be converted to an integer.
	 */
	public int getInt(String key)
	{
		return getInt(key,0);
	}
	
	/**
	 * Gets a float value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does
	 * not exist or did not contain a valid float.
	 * @return The parsed value, or defaultValue if the key did not exist or the value could not be converted to a float.
	 */
	public float getFloat(String key, float defaultValue)
	{
		String val = kvps.get(checkKey(key));
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
	 * Gets a float value from the ConfigFile.
	 * @param key The key to look up.
	 * @return The parsed value, or 0.0f if the key did not exist or the value could not be converted to a float.
	 */
	public float getFloat(String key)
	{
		return getFloat(key,0.0f);
	}
	
	/**
	 * Gets a double value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does
	 * not exist or did not contain a valid double.
	 * @return The parsed value, or defaultValue if the key did not exist or the value could not be converted to a double.
	 */
	public double getDouble(String key, double defaultValue)
	{
		String val = kvps.get(checkKey(key));
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
	 * Gets a double value from the ConfigFile.
	 * @param key The key to look up.
	 * @return The parsed value, or 0.0 if the key did not exist or the value could not be converted to a double.
	 */
	public double getDouble(String key)
	{
		return getDouble(key,0.0);
	}
	
	/**
	 * Gets a boolean value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does
	 * not exist.
	 * @return The parsed value, or defaultValue if the key did not exist or could not be evaluated as a boolean. Since values are stored as strings,
	 * this function considers "true", "on", "yes" and "1" as TRUE values, and is not case-sensitive.
	 */
	public boolean getBoolean(String key, boolean defaultValue)
	{
		String val = kvps.get(checkKey(key));
		if (val == null)
			return defaultValue;
		
		val = val.toLowerCase();
		return val.equals("true") || val.equals("on") || val.equals("yes") || val.equals("1");
	}
	
	/**
	 * Gets a boolean value from the ConfigFile.
	 * @param key The key to look up.
	 * @return The parsed value, or FALSE if the key did not exist or could not be evaluated as a boolean. Since values are stored as strings,
	 * this function considers "true", "on", "yes" and "1" as TRUE values, and is not case-sensitive.
	 */
	public boolean getBoolean(String key)
	{
		return getBoolean(key,false);
	}
	
	/**
	 * Gets a string value from the ConfigFile.
	 * @param key The key to look up.
	 * @param defaultValue The fallback value if the given key does not exist.
	 * @return The value, or defaultValue if the key did not exist.
	 */
	public String getString(String key, String defaultValue)
	{
		if (defaultValue == null)
			throw new NullPointerException("Parameter 'defaultValue' cannot be null.");
		
		String val = kvps.get(checkKey(key));
		return val == null ? defaultValue : val;
	}
	
	/**
	 * Gets a string value from the ConfigFile.
	 * @param key The key to look up.
	 * @return The value, or "" if the key did not exist.
	 */
	public String getString(String key)
	{
		return getString(key,"");
	}
	
	/**
	 * Sets a key/value pair.
	 * @param key The key to assign.
	 * @param value The value to assign at the given key.
	 */
	public void setString(String key, String value)
	{
		if (value == null)
			throw new NullPointerException("Parameter 'value' cannot be null.");

		kvps.put(checkKey(key), value);		
	}
	
	/**
	 * Sets a key/value pair.
	 * @param key The key to assign.
	 * @param value The value to assign at the given key.
	 */
	public void setDouble(String key, double value)
	{
		kvps.put(checkKey(key), Double.toString(value));		
	}
	
	/**
	 * Sets a key/value pair.
	 * @param key The key to assign.
	 * @param value The value to assign at the given key.
	 */
	public void setFloat(String key, float value)
	{
		kvps.put(checkKey(key), Float.toString(value));		
	}
	
	/**
	 * Sets a key/value pair.
	 * @param key The key to assign.
	 * @param value The value to assign at the given key.
	 */
	public void setInt(String key, int value)
	{
		kvps.put(checkKey(key), Integer.toString(value));
	}
	
	/**
	 * Sets a key/value pair.
	 * @param key The key to assign.
	 * @param value The value to assign at the given key.
	 */
	public void setBoolean(String key, boolean value)
	{
		kvps.put(checkKey(key), Boolean.toString(value));		
	}
	
	/**
	 * Deletes a key/value pair, if it exists.
	 * @param key The key to delete.
	 */
	public void delete(String key)
	{
		kvps.remove(checkKey(key));
	}
	
	/**
	 * Deletes all key/value pairs.
	 */
	public void clear()
	{
		kvps.clear();
	}
	
	/**
	 * Test if the ConfigFile contains a value at a particular key.
	 * @param key The key to search for.
	 * @return TRUE if the key was found, FALSE otherwise.
	 */
	public boolean containsKey(String key)
	{
		return kvps.containsKey(checkKey(key));
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("ConfigFile[");
		if (kvps.size() > 0)
		{
			sb.append("\n");
			for (Map.Entry<String, String> kvp : kvps.entrySet())
				sb.append("    " + kvp.getKey() + ": " + kvp.getValue() + "\n");
		}
		else
			sb.append(" ");
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Ensures a key is a valid value 'by default' by checking the stored value exists,
	 * is of the correct type, and is within an acceptable range. If any of these conditions
	 * fails, a default value is applied.
	 * @param key The key to search for.
	 * @param defaultValue The value to assign at the given key by default.
	 * @param min The minimum acceptable value.
	 * @param max The maximum acceptable value.
	 * @return The calling ConfigFile object, so you may chain operations.
	 */
	public ConfigFile defaultInt(String key, int defaultValue, int min, int max)
	{
		setInt((key = checkKey(key)),
			Math.min(Math.max(getInt(key, defaultValue),min),max));
		return this;		
	}
	
	/**
	 * Ensures a key is a valid value 'by default' by checking the stored value exists and
	 * is of the correct type. If any of these conditions fails, a default value is applied.
	 * @param key The key to search for.
	 * @param defaultValue The value to assign at the given key by default.
	 * @return The calling ConfigFile object, so you may chain operations.
	 */
	public ConfigFile defaultInt(String key, int defaultValue)
	{
		return defaultInt(key,defaultValue,Integer.MIN_VALUE,Integer.MAX_VALUE);		
	}
	
	/**
	 * Ensures a key is a valid value 'by default' by checking the stored value exists and
	 * is of the correct type. If any of these conditions fails, a default value is applied.
	 * @param key The key to search for.
	 * @param defaultValue The value to assign at the given key by default.
	 * @return The calling ConfigFile object, so you may chain operations.
	 */
	public ConfigFile defaultString(String key, String defaultValue)
	{
		setString((key = checkKey(key)), getString(key, defaultValue));
		return this;
	}
	
	/**
	 * Ensures a key is a valid value 'by default' by checking the stored value exists,
	 * is of the correct type, and is within an acceptable range. If any of these conditions
	 * fails, a default value is applied.
	 * @param key The key to search for.
	 * @param defaultValue The value to assign at the given key by default.
	 * @param min The minimum acceptable value.
	 * @param max The maximum acceptable value.
	 * @return The calling ConfigFile object, so you may chain operations.
	 */
	public ConfigFile defaultDouble(String key, double defaultValue, double min, double max)
	{
		setDouble((key = checkKey(key)),
			Math.min(Math.max(getDouble(key, defaultValue),min),max));
		return this;		
	}
	
	/**
	 * Ensures a key is a valid value 'by default' by checking the stored value exists and
	 * is of the correct type. If any of these conditions fails, a default value is applied.
	 * @param key The key to search for.
	 * @param defaultValue The value to assign at the given key by default.
	 * @return The calling ConfigFile object, so you may chain operations.
	 */
	public ConfigFile defaultDouble(String key, double defaultValue)
	{
		return defaultDouble(key,defaultValue,Double.MIN_VALUE,Double.MAX_VALUE);		
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
				line = PATTERN_COMMENT.matcher(line).replaceAll("").trim();
				if (line.length() == 0)
					continue;
				
				//check for match
				Matcher lineMatch = PATTERN_CONFIG_KVP.matcher(line);
				if (!lineMatch.find())
					continue;
				
				String key = checkKey(lineMatch.group(1));
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
			System.err.println(e.getMessage());
			return;
		}
	}
	
	private String checkKey(String key)
	{
		if (key == null)
			throw new NullPointerException("Parameter 'key' cannot be null.");
		key = key.trim().toLowerCase();
		if (key.length() == 0)
			throw new IllegalArgumentException("Parameter 'key' cannot be an empty string.");
		else if (!PATTERN_KEY.matcher(key).matches())
			throw new IllegalArgumentException("Parameter 'key' contains invalid characters.");
		return key;
	}
}
