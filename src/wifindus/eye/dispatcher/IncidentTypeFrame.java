package wifindus.eye.dispatcher;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import wifindus.ResourcePool;

public class IncidentTypeFrame extends JFrame implements MouseListener
{
	public IncidentTypeFrame()
	{
		setPreferredSize(new Dimension(400, 300));
		setResizable(false);
		
		Font titleFont = new Font("Arial", Font.BOLD, 25);
		Font font = new Font("Arial", Font.BOLD, 15);
		
		GridLayout typelayout = new GridLayout(4,0);
		setLayout(typelayout);
		
		JLabel title = new JLabel("Select Incident Type");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(titleFont);
		add(title);

		ResourcePool.loadImage("medical", "images/medical.png");
		ResourcePool.loadImage("security", "images/security.png");
		ResourcePool.loadImage("wfu", "images/wfu.png");
	      
		JLabel medical = new JLabel("  Medical");
		JLabel security = new JLabel("  Security");
		JLabel wfu = new JLabel("  WiFindUs");
		
		medical.setFont(font);
		security.setFont(font);
		wfu.setFont(font);
		
		medical.setIcon(ResourcePool.getIcon("medical"));
		security.setIcon(ResourcePool.getIcon("security"));
		wfu.setIcon(ResourcePool.getIcon("wfu"));
		
		medical.addMouseListener(this);
		security.addMouseListener(this);
		wfu.addMouseListener(this);
		
		add(medical);
		add(security);
		add(wfu);

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
