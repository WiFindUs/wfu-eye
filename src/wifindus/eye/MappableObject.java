package wifindus.eye;

import java.awt.Graphics2D;
import java.awt.Polygon;

public interface MappableObject
{
	public Location getLocation();
	public void paintMarker(Graphics2D graphics, int x, int y, boolean isHovering, boolean isSelected);
	public Polygon generateMarkerHitbox(int x, int y);
}
