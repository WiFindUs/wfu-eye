package wifindus.eye;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashedUDPPacket implements Serializable
{
	private static final Pattern PATTERN_SPLIT = Pattern.compile( "[ \t]*\\|[ \t]*" );
	private static final Pattern PATTERN_KVP = Pattern.compile( "^([a-zA-Z0-9_\\-.]+)[ \t]*[:][ \t]*(.+)[ \t]*$" );
	
	private static final long serialVersionUID = 8194196852269581979L;
	private String hash;
	private String type;
	private Map<String, String> kvps = new HashMap<>();
	private InetAddress sourceAddress;
	private int sourcePort;

	public HashedUDPPacket(DatagramPacket packet)
	{
		//basic sanity checking
		if (packet == null)
			throw new NullPointerException("Parameter 'packet' cannot be null.");
		String packetSentence = new String(packet.getData()).trim();
		if (packetSentence.isEmpty())
			throw new IllegalArgumentException("Parameter 'packet' cannot be empty.");
		
		//split string on bar character '|'
		String[] tokens = PATTERN_SPLIT.split(packetSentence);
		if (tokens.length <= 1)
			throw new IllegalArgumentException("Parameter 'packet' is not formatted correctly.");
		
		//check hash
		if (!Hash.isValid(tokens[0]))
			throw new IllegalArgumentException("Parameter 'packet' does not contain a valid WFU device hash ("+tokens[0]+").");
		this.hash = tokens[0];
		
		//check type
		if ((tokens[1] = tokens[1].trim()).isEmpty())
			throw new IllegalArgumentException("Parameter 'packet' does not contain an origin type designation.");
		this.type = tokens[1];
		
		//parse rest of arguments into map
		for (int i = 2; i < tokens.length; i++)
		{
			//check for match
			Matcher argMatch = PATTERN_KVP.matcher(tokens[i]);
			if (!argMatch.find())
				continue;
			kvps.put(argMatch.group(1), argMatch.group(2).replace("%BAR%", "|").replace("%COLON%", ":"));
		}
		
		//network layer information
		sourceAddress = packet.getAddress();
		sourcePort = packet.getPort();
	}

	/**
	 * @return the hash
	 */
	public final String getHash()
	{
		return hash;
	}

	/**
	 * @return the type
	 */
	public final String getType()
	{
		return type;
	}

	/**
	 * @return the sourceAddress
	 */
	public final InetAddress getSourceAddress()
	{
		return sourceAddress;
	}

	/**
	 * @return the sourcePort
	 */
	public final int getSourcePort()
	{
		return sourcePort;
	}

	/**
	 * @return the kvps
	 */
	public final Collection<Entry<String,String>> getKvps()
	{
		return kvps.entrySet();
	}
}
