package wifindus;

import java.util.HashMap;

/**
 * A map of MySQLResultRows.
 * @author Mark 'marzer' Gillard
 */
public class MySQLResultSet extends HashMap< Object, MySQLResultRow >
{
	private static final long serialVersionUID = -2208579144079038606L;
	
	/**
	 * Puts a new row into the collection.
	 * @param key The key at which to add the new row.
	 * @param kvps A list of key-value pairs representing column names and values.
	 * The even numbered elements in the list (0, 2, 4...) need to be strings containing the column names,
	 * and the odd numbered elements (1, 3, 5...) are the contents of the column at the given row. 
	 */
	public void put(Object key, Object... kvps)
	{
		if (key == null)
			throw new NullPointerException("Parameter 'key' cannot be null.");
		if (kvps.length > 0 && (kvps.length % 2) != 0)
			throw new IllegalArgumentException("Parameter 'kvps' must either be omitted, or have an even number of elements.");
		
		MySQLResultRow values = get(key);
		if (values == null)
		{
			values = new MySQLResultRow();
			put (key, values);
		}
		
		for (int i = 0; i < kvps.length; i+=2)
			values.put((String)kvps[i],kvps[i+1]);
	}

}