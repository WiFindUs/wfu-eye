package wifindus;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * Creates image buffers (and associated Graphics2D contexts) using high-performance quality settings.
 * @author Mark 'marzer' Gillard
 */
public abstract class BufferFactory
{
	public static BufferedImage createBufferedImage(int w, int h, int transparency)
	{
		return GraphicsEnvironment
			.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice()
			.getDefaultConfiguration()
			.createCompatibleImage(w, h, transparency);
	}
	
	public static BufferedImage createBufferedImage(int w, int h)
	{
		return BufferFactory.createBufferedImage(w, h, Transparency.TRANSLUCENT);
	}
	
	public static Graphics2D getGraphicsContext(BufferedImage img)
	{
		Graphics2D g2d = img.createGraphics();
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		return g2d;
	}
}
