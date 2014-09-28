package wifindus.eye;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import wifindus.MySQLConnection;
import wifindus.MySQLResultSet;

/**
 * A refined MySQLConnection wrapper containing methods specifically for manipulating
 * the Eye project's database.
 * @author Mark 'marzer' Gillard
 */
public class EyeMySQLConnection extends MySQLConnection
{
	public MySQLResultSet fetchUsers() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM Users");
		ResultSet resultSet = statement.executeQuery();
		MySQLResultSet results = new MySQLResultSet();
		while (resultSet.next())
		{
			Integer id = Integer.valueOf(resultSet.getInt("id"));
			results.put(id,
				"id", id,
				"nameFirst", resultSet.getString("nameFirst"),
				"nameMiddle", resultSet.getString("nameMiddle"),
				"nameLast", resultSet.getString("nameLast"),
				"personnelType", Incident.getTypeFromDatabaseKey(resultSet.getString("personnelType"))
				);
		}
		release(resultSet);
		release(statement);
		return results;
	}
	

	public MySQLResultSet fetchNodes() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM Nodes");
		ResultSet resultSet = statement.executeQuery();
		MySQLResultSet results = new MySQLResultSet();
		while (resultSet.next())
		{
			String hash = resultSet.getString("hash");
			results.put(hash,
				"hash", hash,				
				"address", resultSet.getString("address"),
				"latitude", getNullableDouble(resultSet, "latitude"),
				"longitude", getNullableDouble(resultSet, "longitude"),
				"altitude", getNullableDouble(resultSet, "altitude"),
				"accuracy", getNullableDouble(resultSet, "accuracy"),
				"voltage", getNullableDouble(resultSet, "voltage"),
				"lastUpdate", resultSet.getTimestamp("lastUpdate")
				);
		}
		release(resultSet);
		release(statement);
		return results;	
	}
	
	public MySQLResultSet fetchIncidents() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM Incidents");
		ResultSet resultSet = statement.executeQuery();
		MySQLResultSet results = new MySQLResultSet();
		while (resultSet.next())
		{
			Integer id = Integer.valueOf(resultSet.getInt("id"));
			results.put(id,
				"id", id,			
				"incidentType", Incident.getTypeFromDatabaseKey(resultSet.getString("incidentType")),
				"latitude", Double.valueOf(resultSet.getDouble("latitude")),
				"longitude", Double.valueOf(resultSet.getDouble("longitude")),
				"altitude", getNullableDouble(resultSet, "altitude"),
				"accuracy", getNullableDouble(resultSet, "accuracy"),
				"created", resultSet.getTimestamp("created"),
				"archived", (resultSet.getShort("archived") != 0)
				);
		}
		release(resultSet);
		release(statement);
		return results;	
	}
	

	public MySQLResultSet fetchDevices() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM Devices");
		ResultSet resultSet = statement.executeQuery();
		MySQLResultSet results = new MySQLResultSet();
		while (resultSet.next())
		{
			String hash = resultSet.getString("hash");
			results.put(hash,
				"hash", hash,		
				"deviceType", Device.getTypeFromDatabaseKey(resultSet.getString("deviceType")),
				"address", resultSet.getString("address"),
				"latitude", getNullableDouble(resultSet, "latitude"),
				"longitude", getNullableDouble(resultSet, "longitude"),
				"altitude", getNullableDouble(resultSet, "altitude"),
				"accuracy", getNullableDouble(resultSet, "accuracy"),
				"humidity", getNullableDouble(resultSet, "humidity"),
				"airPressure", getNullableDouble(resultSet, "airPressure"),
				"temperature", getNullableDouble(resultSet, "temperature"),
				"lightLevel", getNullableDouble(resultSet, "lightLevel"),
				"lastUpdate", resultSet.getTimestamp("lastUpdate"),
				"respondingIncidentID", getNullableInt(resultSet, "respondingIncidentID")
				);
		}
		release(resultSet);
		release(statement);
		return results;	
	}

	public MySQLResultSet fetchDeviceUsers() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM DeviceUsers");
		ResultSet resultSet = statement.executeQuery();
		MySQLResultSet results = new MySQLResultSet();
		int i = 0;
		while (resultSet.next())
		{
			results.put(i++,
				"userID", Integer.valueOf(resultSet.getInt("userID")),
				"deviceHash", resultSet.getString("deviceHash")
				);
		}
		release(resultSet);
		release(statement);
		return results;	
	}
}
