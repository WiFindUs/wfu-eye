package wifindus.eye.dispatcher;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import wifindus.eye.EyeApplication;


public class MapFrame extends JFrame
{
	public MapFrame()
	{
		
		
		
		//setMinimumSize(new Dimension(400,400));
		JLabel mapText = new JLabel("Map");
		add(mapText);
		
		
		
		
		
	
		
		  MapImagePanel mp = new MapImagePanel();
	
	
		 add(mp);
		 
		 
		 
		
	
		 
		 

	}
	
	
	
	
	
	

	
}
