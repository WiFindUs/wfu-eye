/**
 * 
 */
package wifindus.eye;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import wifindus.MySQLConnection;

/**
 * A refined MySQLConnection wrapper containing methods specifically for manipulating
 * the Eye project's database.
 * @author Mark 'marzer' Gillard
 */
public class EyeMySQLConnection extends MySQLConnection
{
	public ResultSet fetchUsers() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM Users");
		ResultSet resultSet = statement.executeQuery();
		release(statement);
		return resultSet;
		/*
		Map<Integer, Map<String, Object>> dataset = new HashMap<Integer, Map<String, Object>>();
		while (resultSet.next())
		{
			 Map<String, Object> entry = new HashMap<String, Object>();
			 Integer id = resultSet.getInt("userID");
			 entry.put("userID", id);
			 entry.put("nameFirst", resultSet.getString("nameFirst"));
			 entry.put("nameMiddle", resultSet.getString("nameMiddle"));
			 entry.put("nameLast", resultSet.getString("nameLast"));
			 entry.put("personnelType", resultSet.getString("personnelType"));
			 dataset.put(id, entry);
		}
		release(resultSet);
		
		return dataset;
		*/
	}
	
	public ResultSet fetchNodes() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM Nodes");
		ResultSet resultSet = statement.executeQuery();
		release(statement);
		return resultSet;		
	}
	
	public ResultSet fetchIncidents() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM Incidents");
		ResultSet resultSet = statement.executeQuery();
		release(statement);
		return resultSet;
	}
	
	public ResultSet fetchDevices() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM Devices");
		ResultSet resultSet = statement.executeQuery();
		release(statement);
		return resultSet;
	}
	
	public ResultSet fetchDeviceUsers() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM DeviceUsers");
		ResultSet resultSet = statement.executeQuery();
		release(statement);
		return resultSet;
	}
}
