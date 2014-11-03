package wifindus.eye;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import wifindus.MySQLConnection;
import wifindus.MySQLResultRow;
import wifindus.MySQLResultSet;

/**
 * A refined MySQLConnection wrapper containing methods specifically for manipulating
 * the Eye project's database.
 * @author Mark 'marzer' Gillard
 */
public class EyeMySQLConnection extends MySQLConnection
{
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
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
				"archived", (resultSet.getShort("archived") != 0),
				"archivedTime", resultSet.getTimestamp("archivedTime"),
				"severity", resultSet.getInt("severity"),
				"code", resultSet.getString("code"),
				"description", resultSet.getString("description"),
				"reportingUserID", getNullableInt(resultSet, "reportingUserID")
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
			parseDeviceRow(resultSet, results);
		release(resultSet);
		release(statement);
		return results;	
	}
	
	public MySQLResultRow fetchSingleDevice(String hash) throws SQLException
	{
		if (hash == null)
			throw new NullPointerException("Parameter 'hash' cannot be null.");
		if (!Hash.isValid(hash))
			throw new IllegalArgumentException("Parameter 'hash' is not a valid WFU device hash ("+hash+").");
		
		PreparedStatement statement = prepareStatement("SELECT * FROM Devices WHERE hash='"+hash+"' LIMIT 1");
		ResultSet resultSet = statement.executeQuery();
		MySQLResultSet results = new MySQLResultSet();
		while (resultSet.next())
			parseDeviceRow(resultSet, results);
		release(resultSet);
		release(statement);
		if (results.size() == 0)
			return null;
		return results.get(hash);
	}

	public MySQLResultSet fetchPastIncidentResponders() throws SQLException
	{
		PreparedStatement statement = prepareStatement("SELECT * FROM PastIncidentResponders ORDER BY incidentID ASC");
		ResultSet resultSet = statement.executeQuery();
		MySQLResultSet results = new MySQLResultSet();
		int i = 0;
		while (resultSet.next())
		{
			results.put(i++,
				"userID", Integer.valueOf(resultSet.getInt("userID")),
				"incidentID", Integer.valueOf(resultSet.getInt("incidentID"))
				);
		}
		release(resultSet);
		release(statement);
		return results;	
	}
	
	/////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////////////////////////////////////////
	
	public void parseDeviceRow(final ResultSet resultSet, final MySQLResultSet results) throws SQLException
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
			"respondingIncidentID", getNullableInt(resultSet, "respondingIncidentID"),
			"userID", getNullableInt(resultSet, "userID"));
	}
}
