package wifindus;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedUDPPacket implements Serializable
{
	private static final Pattern PATTERN_SPLIT = Pattern.compile( "[ \t]*\\|[ \t]*" );
	private static final Pattern PATTERN_KVP = Pattern.compile( "^([a-zA-Z0-9_\\-.]+)[ \t]*[:][ \t]*(.+)[ \t]*$" );
	
	private static final long serialVersionUID = 8194196852269581979L;
	private Map<String, String> kvps = new HashMap<>();
	private InetAddress sourceAddress;
	private int sourcePort;

	public ParsedUDPPacket(DatagramPacket packet)
	{
		//basic sanity checking
		if (packet == null)
			throw new NullPointerException("Parameter 'packet' cannot be null.");
		String packetSentence = new String(packet.getData()).trim();
		if (packetSentence.isEmpty())
			throw new IllegalArgumentException("Parameter 'packet' cannot be empty.");
		
		//split string on bar character '|'
		String[] tokens = PATTERN_SPLIT.split(packetSentence);
		if (tokens.length <= 0)
			throw new IllegalArgumentException("Parameter 'packet' is not formatted correctly.");
	
		//parse rest of arguments into map
		for (int i = 0; i < tokens.length; i++)
		{
			//check for match
			Matcher argMatch = PATTERN_KVP.matcher(tokens[i]);
			if (!argMatch.find())
				continue;
			kvps.put(argMatch.group(1).toLowerCase(), argMatch.group(2).replace("%BAR%", "|").replace("%COLON%", ":"));
		}
		
		//network layer information
		sourceAddress = packet.getAddress();
		sourcePort = packet.getPort();
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
	public final Map<String,String> getData()
	{
		return kvps;
	}
}
