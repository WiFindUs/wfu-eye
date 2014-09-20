package wifindus;

import java.io.Closeable;
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
	public void connect(String address, int port, String database, String username, String password) throws IllegalStateException
	{
		//sanity checks
		if (connection != null)
			throw new IllegalStateException("A connection has already been made! Use disconnect() first.");
		if (address == null)
			throw new IllegalArgumentException("Parameter 'address' cannot be null.");
		if ((address = address.trim()).isEmpty())
			throw new IllegalArgumentException("Parameter 'address' cannot be an empty string");
		if (port <= 1023 || port >= 65536)
			throw new IllegalArgumentException("Parameter 'port' must be between 1024 and 65535 (inclusive).");
		if (database == null)
			throw new IllegalArgumentException("Parameter 'database' cannot be null.");
		if ((database = database.trim()).isEmpty())
			throw new IllegalArgumentException("Parameter 'database' cannot be an empty string");
		if (username == null)
			throw new IllegalArgumentException("Parameter 'username' cannot be null.");
		if ((username = username.trim()).isEmpty())
			throw new IllegalArgumentException("Parameter 'username' cannot be an empty string");
		if (password == null)
			throw new IllegalArgumentException("Parameter 'password' cannot be null.");
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
		    System.err.println("SQLException: " + ex.getMessage());
		    System.err.println("SQLState: " + ex.getSQLState());
		    System.err.println("VendorError: " + ex.getErrorCode());
		    throw new IllegalStateException("Connecting to the MySQL server failed.");
		}
	}
	
	/**
	 * Connects to a MySQL database on port  3306.
	 @param address Address of the server (without port).
	 * @param database The name of the database to connect to.
	 * @param username The MySQL server username.
	 * @param password The MySQL server password.
	 * @throws IllegalStateException If the driver could not be loaded, if a connection already exists, or if the connection failed.
	 */
	public void connect(String address, String database, String username, String password) throws IllegalStateException
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
	public void connect(String database, String username, String password) throws IllegalStateException
	{
		connect("localhost",3306,database,username,password);
	}
	
	/**
	 * Disconnects from the database, freeing the connection resources. If you have any ResultSets,
	 * Statements or PreparedStatements cached, you should call release() on them before disconnecting. 
	 */
	public void disconnect()
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
	public void release(Statement... statements) throws IllegalStateException
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
	public void release(ResultSet... resultSets) throws IllegalStateException
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
	public Statement createStatement() throws IllegalStateException
	{
		if (connection == null)
			throw new IllegalStateException("No MySQL connection has been established.");
		try
		{
			return connection.createStatement();
		}
		catch (SQLException ex)
		{
		    System.err.println("SQLException: " + ex.getMessage());
		    System.err.println("SQLState: " + ex.getSQLState());
		    System.err.println("VendorError: " + ex.getErrorCode());
		    throw new IllegalStateException("There was an error creating a statement.");
		}
	}
	
	/**
	 * Creates a PreparedStatement object using the MySQL connection.
	 * @param query The SQL query to execute.
	 * @throws IllegalStateException If no MySQL connection has been established or if an error occurs.
	 */
	public PreparedStatement prepareStatement(String query) throws IllegalStateException
	{
		if (connection == null)
			throw new IllegalStateException("No MySQL connection has been established.");
		try
		{
			return connection.prepareStatement(query);
		}
		catch (SQLException ex)
		{
		    System.err.println("SQLException: " + ex.getMessage());
		    System.err.println("SQLState: " + ex.getSQLState());
		    System.err.println("VendorError: " + ex.getErrorCode());
		    throw new IllegalStateException("There was an error creating a statement.");
		}
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
			System.err.println(e.getMessage());
        }
		return false;
	}
}
