package wifindus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A wrapper around MySQL's Connector/J.
 * @author Mark 'marzer' Gillard
 */
public class MySQLConnection
{
	private static boolean driverLoaded = false;
	private Connection connection = null;

	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Connects to a MySQL database.
	 * @param address Address of the server (without port).
	 * @param port The port on which to connect.
	 * @param database The name of the database to connect to.
	 * @param username The MySQL server username.
	 * @param password The MySQL server password.
	 * @throws IllegalStateException If the driver could not be loaded, if a connection already exists, or if the connection failed.
	 */
	public final void connect(String address, int port, String database, String username, String password) throws IllegalStateException
	{
		//sanity checks
		if (connection != null)
			throw new IllegalStateException("A connection has already been made! Use disconnect() first.");
		if (address == null)
			throw new NullPointerException("Parameter 'address' cannot be null.");
		if ((address = address.trim()).isEmpty())
			throw new IllegalArgumentException("Parameter 'address' cannot be an empty string");
		if (port <= 1023 || port >= 65536)
			throw new IllegalArgumentException("Parameter 'port' must be between 1024 and 65535 (inclusive).");
		if (database == null)
			throw new NullPointerException("Parameter 'database' cannot be null.");
		if ((database = database.trim()).isEmpty())
			throw new IllegalArgumentException("Parameter 'database' cannot be an empty string");
		if (username == null)
			throw new NullPointerException("Parameter 'username' cannot be null.");
		if ((username = username.trim()).isEmpty())
			throw new IllegalArgumentException("Parameter 'username' cannot be an empty string");
		if (password == null)
			throw new NullPointerException("Parameter 'password' cannot be null.");
		if ((password = password.trim()).isEmpty())
			throw new IllegalArgumentException("Parameter 'password' cannot be an empty string");
		
		//load mysql connector/j driver
		if (!loadDriver())
			throw new IllegalStateException("The Connector/J driver could not be loaded.");
		
		//make connection
		try
		{			
			connection = DriverManager.getConnection(
				"jdbc:mysql://" + address + ":" + port + "/" + database
				+ "?user=" + username
				+ "&password=" + password);
		}
		catch (SQLException ex)
		{
			Debugger.e("SQLException: " + ex.getMessage());
			Debugger.e("SQLState: " + ex.getSQLState());
			Debugger.e("VendorError: " + ex.getErrorCode());
		    throw new IllegalStateException("Connecting to the MySQL server failed.");
		}
	}
	
	/**
	 * Connects to a MySQL database on port 3306.
	 @param address Address of the server (without port).
	 * @param database The name of the database to connect to.
	 * @param username The MySQL server username.
	 * @param password The MySQL server password.
	 * @throws IllegalStateException If the driver could not be loaded, if a connection already exists, or if the connection failed.
	 */
	public final void connect(String address, String database, String username, String password) throws IllegalStateException
	{
		connect(address,3306,database,username,password);
	}
	
	/**
	 * Connects to a MySQL database at localhost:3306.
	 * @param database The name of the database to connect to.
	 * @param username The MySQL server username.
	 * @param password The MySQL server password.
	 * @throws IllegalStateException If the driver could not be loaded, if a connection already exists, or if the connection failed.
	 */
	public final void connect(String database, String username, String password) throws IllegalStateException
	{
		connect("localhost",3306,database,username,password);
	}
	
	/**
	 * Disconnects from the database, freeing the connection resources. If you have any ResultSets,
	 * Statements or PreparedStatements cached, you should call release() on them before disconnecting. 
	 */
	public final void disconnect()
	{
		try
		{
			if (connection != null)
				connection.close();
		}
		catch (Exception e)
		{
			//
		}
		connection = null;
	}
	
	/**
	 * Frees the resources used by the given Statements or PreparedStatements.
	 * @param statements One or more Statement objects to release.
	 * @throws IllegalStateException If no MySQL connection has been established.
	 */
	public final void release(Statement... statements) throws IllegalStateException
	{
		if (connection == null)
			throw new IllegalStateException("No MySQL connection has been established.");
		
		for ( Statement statement : statements )
		{
			try
			{
				if (statement != null)
					statement.close();
			}
			catch (Exception e)
			{
				//
			}
		}
	}
	
	/**
	 * Frees the resources used by the given ResultSets.
	 * @param resultSets One or more ResultSet objects to release.
	 * @throws IllegalStateException If no MySQL connection has been established.
	 */
	public final void release(ResultSet... resultSets) throws IllegalStateException
	{
		if (connection == null)
			throw new IllegalStateException("No MySQL connection has been established.");
		
		for ( ResultSet resultSet : resultSets )
		{
			try
			{
				if (resultSet != null)
					resultSet.close();
			}
			catch (Exception e)
			{
				//
			}
		}
	}
	
	/**
	 * Creates a Statement object using the MySQL connection.
	 * @throws IllegalStateException If no MySQL connection has been established or if an error occurs.
	 */
	public final Statement createStatement() throws IllegalStateException
	{
		if (connection == null)
			throw new IllegalStateException("No MySQL connection has been established.");
		try
		{
			return connection.createStatement();
		}
		catch (SQLException ex)
		{
			Debugger.e("SQLException: " + ex.getMessage());
			Debugger.e("SQLState: " + ex.getSQLState());
			Debugger.e("VendorError: " + ex.getErrorCode());
		    throw new IllegalStateException("There was an error creating a statement.");
		}
	}
	
	/**
	 * Creates a PreparedStatement object using the MySQL connection.
	 * @param query The SQL query to execute.
	 * @throws IllegalStateException If no MySQL connection has been established or if an error occurs.
	 */
	public final PreparedStatement prepareStatement(String query) throws IllegalStateException
	{
		if (connection == null)
			throw new IllegalStateException("No MySQL connection has been established.");
		try
		{
			return connection.prepareStatement(query);
		}
		catch (SQLException ex)
		{
			Debugger.e("SQLException: " + ex.getMessage());
			Debugger.e("SQLState: " + ex.getSQLState());
			Debugger.e("VendorError: " + ex.getErrorCode());
		    throw new IllegalStateException("There was an error creating a statement.");
		}
	}
	
	/**
	 * Gets a nullable Double value, properly enforcing null returns in the case of SQL NULL.
	 * @param resultSet The set of query results from which to get the nullable value.
	 * @param columnLabel The name of the column holding the value
	 * @return A Double containing the given value, or null if it was SQL NULL.
	 * @throws SQLException if an SQL error occurs.
	 * @throws NullPointerException if resultSet or columnLabel are null. 
	 */
	public static final Double getNullableDouble(ResultSet resultSet, String columnLabel) throws SQLException
	{
		if (resultSet == null)
			throw new NullPointerException("Parameter 'resultSet' cannot be null.");
		if (columnLabel == null)
			throw new NullPointerException("Parameter 'columnLabel' cannot be null.");
		
		Double val = Double.valueOf(resultSet.getDouble(columnLabel));
		return resultSet.wasNull() ? null : val;
	}
	
	/**
	 * Gets a nullable Integer value, properly enforcing null returns in the case of SQL NULL.
	 * @param resultSet The set of query results from which to get the nullable value.
	 * @param columnLabel The name of the column holding the value
	 * @return An Integer containing the given value, or null if it was SQL NULL.
	 * @throws SQLException if an SQL error occurs.
	 * @throws NullPointerException if resultSet or columnLabel are null. 
	 */
	public static final Integer getNullableInt(ResultSet resultSet, String columnLabel) throws SQLException
	{
		if (resultSet == null)
			throw new NullPointerException("Parameter 'resultSet' cannot be null.");
		if (columnLabel == null)
			throw new NullPointerException("Parameter 'columnLabel' cannot be null.");
		
		Integer val = Integer.valueOf(resultSet.getInt(columnLabel));
		return resultSet.wasNull() ? null : val;
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	private static boolean loadDriver()
	{
        if (driverLoaded)
        	return true;
		try
        {
        	Class.forName("com.mysql.jdbc.Driver").newInstance();
        	driverLoaded = true;
        	return true;
        }
        catch (Exception e)
        {
        	Debugger.ex(e);
        }
		return false;
	}
}
