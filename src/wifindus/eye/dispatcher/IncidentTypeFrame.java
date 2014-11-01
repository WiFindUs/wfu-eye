package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import wifindus.Debugger;
import wifindus.ResourcePool;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;


public class IncidentTypeFrame extends JFrame implements MouseListener
{
	Device device;
	String type;
	JLabel medical, security, wfu;
	JButton cancel;
	Color hover;
	
	public IncidentTypeFrame(Device device)
	{
		this.device = device;
		
		setPreferredSize(new Dimension(400, 300));
		setResizable(false);
		
		hover = new Color(0xCCFFFF);
		
		Font titleFont = new Font("Arial", Font.BOLD, 25);
		Font font = new Font("Arial", Font.BOLD, 15);
		
		GridLayout typelayout = new GridLayout(5,0);
		setLayout(typelayout);
		
		JLabel title = new JLabel("Select Incident Type");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(titleFont);
		title.setOpaque(true);
		title.setBackground(Color.WHITE);
		add(title);

		ResourcePool.loadImage("medical", "images/medical.png");
		ResourcePool.loadImage("security", "images/security.png");
		ResourcePool.loadImage("wfu", "images/wfu.png");
	      
		medical = new JLabel("  Medical");
		security = new JLabel("  Security");
		wfu = new JLabel("  WiFindUs");
		
		cancel = new JButton("Cancel");
		cancel.setFocusPainted(false);
		Border emptyBorder = BorderFactory.createEmptyBorder();
		cancel.setBorder(emptyBorder);
		
		medical.setFont(font);
		security.setFont(font);
		wfu.setFont(font);
		cancel.setFont(font);
		
		medical.setIcon(ResourcePool.getIcon("medical"));
		security.setIcon(ResourcePool.getIcon("security"));
		wfu.setIcon(ResourcePool.getIcon("wfu"));
		
		medical.addMouseListener(this);
		security.addMouseListener(this);
		wfu.addMouseListener(this);
		cancel.addMouseListener(this);
		
		medical.setOpaque(true);
		security.setOpaque(true);
		wfu.setOpaque(true);
		cancel.setOpaque(true);
		
		medical.setBackground(Color.WHITE);
		security.setBackground(Color.WHITE);
		wfu.setBackground(Color.WHITE);
		cancel.setBackground(Color.WHITE);
		
		add(medical);
		add(security);
		add(wfu);
		add(cancel);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		

	}
	
	

	public String getIncidentType()
	{
		return type;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == medical)
		{
			Incident i= EyeApplication.get().db_createIncident(Incident.Type.Medical, device.getLocation());
			EyeApplication.get().db_setIncidentReportingUser(i, device.getCurrentUser());
			Debugger.i("New incident reported by "+ device.getCurrentUser().getNameFull() +" at "+ device.getLocation());
    		Dispatcher.get().setEnabled(true);
			dispose();
		}
		else if(e.getSource() == security)
		{
			Incident i= EyeApplication.get().db_createIncident(Incident.Type.Security, device.getLocation());
			EyeApplication.get().db_setIncidentReportingUser(i, device.getCurrentUser());
			Debugger.i("New incident reported by "+ device.getCurrentUser().getNameFull() +" at "+ device.getLocation());
    		Dispatcher.get().setEnabled(true);
			dispose();
		}
		else if(e.getSource() == wfu)
		{
			Incident i= EyeApplication.get().db_createIncident(Incident.Type.WiFindUs, device.getLocation());
			EyeApplication.get().db_setIncidentReportingUser(i, device.getCurrentUser());
			Debugger.i("New incident reported by "+ device.getCurrentUser().getNameFull() +" at "+ device.getLocation());
    		Dispatcher.get().setEnabled(true);
			dispose();
		}
		else if(e.getSource() == cancel)
		{
			Dispatcher.get().setEnabled(true);
			 dispose();
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	
		if(e.getSource() == medical)
		{
			medical.setBackground(hover);
		}
		else if(e.getSource() == security)
		{
			security.setBackground(hover);
		}
		else if(e.getSource() == wfu)
		{
			wfu.setBackground(hover);
		}
		else if(e.getSource() == cancel)
		{
			cancel.setBackground(hover);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == medical)
		{
			medical.setBackground(Color.WHITE);
		}
		else if(e.getSource() == security)
		{
			security.setBackground(Color.WHITE);
		}
		else if(e.getSource() == wfu)
		{
			wfu.setBackground(Color.WHITE);
		}
		else if(e.getSource() == cancel)
		{
			cancel.setBackground(Color.WHITE);
		}
		
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
