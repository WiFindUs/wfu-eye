package wifindus;

/**
 * An object which takes a MySQLResultRow object and
 * updates it's own internal state from the data contained within.
 * @author Mark 'marzer' Gillard
 */
public interface MySQLUpdateTarget
{
	/**
	 * Updates this object from database data.
	 * @param resultRow A MySQLResultRow object containing up-to-date information about this object. 
	 */
	public void update(MySQLResultRow resultRow);
}
