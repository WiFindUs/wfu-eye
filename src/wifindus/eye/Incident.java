package wifindus.eye;
import java.io.Serializable;

public class Incident implements Serializable
{
	public static final byte TYPE_NONE = 0;
	public static final byte TYPE_SECURITY = 1;
	public static final byte TYPE_MEDICAL = 2;
	public static final byte TYPE_ALL = TYPE_SECURITY | TYPE_MEDICAL;
	
	private static final long serialVersionUID = 6536603279066529731L;
	
	private long id = 0;
	private byte type;
	private Location location;
}
