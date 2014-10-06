package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import wifindus.Debugger;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import wifindus.eye.Location;


public class MapImagePanel extends JPanel implements ComponentListener{

    BufferedImage mapImage;
    String path;
    BufferedImage scaledImage;
    resizedImage resize;
    ImageIcon ii;

    public MapImagePanel() 
    {
    	addComponentListener(this);
    	path = EyeApplication.get().getConfig().getString("map.image");
    	resize = new resizedImage();
    	
    	ii = new ImageIcon(path);
       
       // Get size of the image
       BufferedImage bimg = null;
       try 
       {
    	   	bimg = ImageIO.read(new File(path));
       } 
       catch (IOException e) 
       {
    	   e.printStackTrace();
       }
       mapImage = resize.scaleImage(300, 300, path);
       
       //Get latitude and longitude for corners of map
       Location starts = new Location(EyeApplication.get().getConfig().getDouble("map.latitude_start"), EyeApplication.get().getConfig().getDouble("map.longitude_start"));
       Location ends = new Location(EyeApplication.get().getConfig().getDouble("map.latitude_end"), EyeApplication.get().getConfig().getDouble("map.longitude_end"));

       Double startLat = starts.getLatitude();
       Double endLat = ends.getLatitude();
       
       System.out.println("Map Corners:" + starts);
    }
    
	////////////////////////////////////////////////////////////////
    // Move map markers when the device location changes
	////////////////////////////////////////////////////////////////
    public static void deviceLocationChanged(Device device, Location oldLocation, Location newLocation)
    {
    	System.out.println("device: " + device + "old: " + oldLocation + "new: "+ newLocation);
    }
    
	////////////////////////////////////////////////////////////////
    // paint component
	////////////////////////////////////////////////////////////////
    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        g.drawImage(mapImage, 0, 0, null); 
        
        int wStep = 0;
        int hStep = 0;
       
        g.setColor(Color.WHITE);
        try
        {
        	 wStep = mapImage.getWidth()/10;
        	 hStep = mapImage.getHeight()/10;
        
        	 for (int i = 0; i < 10; i++)
        	 {
        		 //Horizontal
        		 g.drawLine(0, hStep * (i + 1), mapImage.getWidth(), hStep * (i + 1));
        	
        		 char letter = (char) ('A' + i);
        		 String label = "" + letter;
        		 g.drawString(label, 2, hStep / 2 + (hStep * i)) ;
        	
        		 //Vertical
        		 g.drawLine(wStep * (i + 1),0, wStep * (i + 1), mapImage.getHeight());
        		 g.drawString(Integer.toString(i+1), wStep / 2 + (wStep * i), 12);
        	 }
        }
        catch(NullPointerException e)
        {
        	
        }
    }
    
    ////////////////////////////////////////////////////////////////
    // create resized image
    ////////////////////////////////////////////////////////////////
	public class resizedImage
	{
		public BufferedImage scaleImage(int width, int height, String filename) {
			scaledImage = null;
		    try 
		    {
		    	scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		    	Graphics2D g = (Graphics2D) scaledImage.createGraphics();
		        g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY));
		        g.drawImage(ii.getImage(), 0, 0, width, height, null);
		        repaint();
		        revalidate();
		    } 
		    catch (Exception e) 
		    {
		        return scaledImage;
		    }
		    return scaledImage;
		}
	}


	
	
	////////////////////////////////////////////////////////////////
	// Component Listeners
	////////////////////////////////////////////////////////////////
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		System.out.println("componentHidden");
			}
	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		System.out.println("componentMoved");
	}
	@Override
	public void componentResized(ComponentEvent e) 
	{
		Dimension boundary = new Dimension(getWidth(), getHeight());
		Dimension imgSize = new Dimension(1052 * 2, 871 * 2);
		Dimension d = getScaledDimension(imgSize, boundary);
		mapImage = resize.scaleImage(d.width, d.height, path);
	}
	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		System.out.println("componentShown");
	}

	
	
	////////////////////////////////////////////////////////////////
	// Scale the image to the new panel size
	////////////////////////////////////////////////////////////////

	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

	    int original_width = imgSize.width;
	    int original_height = imgSize.height;
	    
	    int bound_width = boundary.width;
	    int bound_height = boundary.height;
	   
	    int new_width = original_width;
	    int new_height = original_height;

	    // first check if we need to scale width
	    if (original_width > bound_width) {
	        //scale width to fit
	        new_width = bound_width;
	        //scale height to maintain aspect ratio
	        new_height = (new_width * original_height) / original_width;
	    }

	    // then check if we need to scale even with the new height
	    if (new_height > bound_height) {
	        //scale height to fit instead
	        new_height = bound_height;
	        //scale width to maintain aspect ratio
	        new_width = (new_height * original_width) / original_height;
	    }

	    return new Dimension(new_width, new_height);
	}
	
}
	
	
	
	
	
	


