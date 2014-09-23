package wifindus.eye;

import java.util.regex.Pattern;

/**
 * A global, static class containing WiFindUs hash ID string
 * manipulation functions.
 * @author Mark 'marzer' Gillard
 */
public abstract class Hash
{
	private static final Pattern PATTERN_HASH = Pattern.compile( "^[0-9a-zA-Z]{8}$" );
	
	/**
	 * Tests if a given string is a valid WiFindUs randomly-generated hash.
	 * @param test The string to test.
	 * @return TRUE if it is not null, is 8 characters long and does not contain any invalid characters, FALSE otherwise.
	 */
	public static final boolean isValid(String test)
	{
		if (test == null)
			return false;
		return PATTERN_HASH.matcher(test).matches();
	}
}
