package wifindus.eye;

import java.util.regex.Pattern;

public abstract class Hash
{
	private static final Pattern PATTERN_HASH = Pattern.compile( "^[0-9a-zA-Z]{8}$" );
	
	public static boolean isValid(String test)
	{
		if (test == null)
			return false;
		return PATTERN_HASH.matcher(test).matches();
	}
}
