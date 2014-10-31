package wifindus.eye.dispatcher;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import wifindus.Debugger;
import wifindus.ResourcePool;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;
import wifindus.eye.User;
import wifindus.eye.Incident.Type;
import wifindus.eye.IncidentEventListener;


public class IncidentTypeFrame extends JFrame implements MouseListener
{
	Device device;
	String type;
	JLabel medical, security, wfu;
	JButton cancel;
	
	public IncidentTypeFrame(Device device)
	{
		this.device = device;
		setPreferredSize(new Dimension(400, 300));
		setResizable(false);
		
		Font titleFont = new Font("Arial", Font.BOLD, 25);
		Font font = new Font("Arial", Font.BOLD, 15);
		
		GridLayout typelayout = new GridLayout(5,0);
		setLayout(typelayout);
		
		JLabel title = new JLabel("Select Incident Type");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(titleFont);
		add(title);

		ResourcePool.loadImage("medical", "images/medical.png");
		ResourcePool.loadImage("security", "images/security.png");
		ResourcePool.loadImage("wfu", "images/wfu.png");
	      
		medical = new JLabel("  Medical");
		security = new JLabel("  Security");
		wfu = new JLabel("  WiFindUs");
		
		cancel = new JButton("Cancel");
		
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
		
		add(medical);
		add(security);
		add(wfu);
		add(cancel);
		

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
		}
		else if(e.getSource() == security)
		{
			Incident i= EyeApplication.get().db_createIncident(Incident.Type.Security, device.getLocation());
			EyeApplication.get().db_setIncidentReportingUser(i, device.getCurrentUser());
			Debugger.i("New incident reported by "+ device.getCurrentUser().getNameFull() +" at "+ device.getLocation());
		}
		else if(e.getSource() == wfu)
		{
			Incident i= EyeApplication.get().db_createIncident(Incident.Type.WiFindUs, device.getLocation());
			EyeApplication.get().db_setIncidentReportingUser(i, device.getCurrentUser());
			Debugger.i("New incident reported by "+ device.getCurrentUser().getNameFull() +" at "+ device.getLocation());
		}
		
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
