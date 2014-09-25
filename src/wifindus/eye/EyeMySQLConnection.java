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
	public Map<Integer, Map<String, Object>> fetchUsers() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM Users");
		ResultSet resultSet = statement.executeQuery();
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
		release(statement);
		return dataset;
	}
	
	public Map<String, Map<String, Object>> fetchNodes()
	{
		return null;
		
	}
	
	public Map<Integer, Map<String, Object>> fetchIncidents()
	{
		return null;
		
	}
	
	public Map<String, Map<String, Object>> fetchDevices()
	{
		return null;
		
	}
}
