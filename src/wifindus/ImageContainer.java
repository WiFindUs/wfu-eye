package wifindus;

import java.awt.Image;
import javax.swing.ImageIcon;

public class ImageContainer
{
	private Image image;
	private ImageIcon imageIcon;
	
	public ImageContainer(Image image)
	{
		this.image = image;
	}
	
	public Image getImage()
	{
		return image;
	}
	
	public ImageIcon getIcon()
	{
		if (imageIcon == null)
			imageIcon = new ImageIcon(image);
		return imageIcon;
	}
}
