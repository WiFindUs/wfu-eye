package wifindus.eye;

import java.awt.Graphics2D;

public interface MappableObject
{
	public Location getLocation();
	public void paintMarker(Graphics2D graphics, int x, int y);
}
