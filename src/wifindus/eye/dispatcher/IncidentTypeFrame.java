package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import wifindus.Debugger;
import wifindus.ResourcePool;
import wifindus.eye.Device;
import wifindus.eye.EyeApplication;
import wifindus.eye.Incident;


public class IncidentTypeFrame extends JFrame implements MouseListener, ActionListener, WindowFocusListener
{
	private static final long serialVersionUID = 322716282507971893L;
	private transient Device device;
	private transient String type;
	private transient JButton medicalBtn, securityBtn, wfuBtn;
	private transient JPanel panel, buttonsPanel;
	private transient Color hover;
	
	static
	{
    	ResourcePool.loadImage("cog_inverted_themed", "images/cog_inverted_themed.png");
    	ResourcePool.loadImage("cross_inverted_themed", "images/cross_inverted_themed.png");
    	ResourcePool.loadImage("shield_inverted_themed", "images/shield_inverted_themed.png");
	}
	
	public IncidentTypeFrame(Device device)
	{
		this.device = device;
		
		setPreferredSize(new Dimension(250, 110));
		setResizable(false);
		
		panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		hover = new Color(0xCCFFFF);
		Border emptyBorder = BorderFactory.createEmptyBorder();
		Font titleFont = new Font("Arial", Font.BOLD, 15);
		Font font = new Font("Arial", Font.BOLD, 12);
		
		JLabel title = new JLabel("Select Incident Type");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(titleFont);
		title.setOpaque(true);
		title.setBackground(Color.WHITE);
	    
		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		buttonsPanel.setBackground(Color.white);
		buttonsPanel.setMinimumSize(new Dimension(230, 70));
		
		medicalBtn = new JButton("Medical");
		medicalBtn.setBackground(Color.white);
		medicalBtn.setIcon(ResourcePool.getIcon("cross_inverted_themed"));
		medicalBtn.setBorder(emptyBorder);
		medicalBtn.setFont(font);
		medicalBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
		medicalBtn.setHorizontalTextPosition(SwingConstants.CENTER);
		medicalBtn.addActionListener(this);
		medicalBtn.addMouseListener(this);
		
		securityBtn = new JButton("Security");
		securityBtn.setBackground(Color.white);
		securityBtn.setIcon(ResourcePool.getIcon("shield_inverted_themed"));
		securityBtn.setBorder(emptyBorder);
		securityBtn.setFont(font);
		securityBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
		securityBtn.setHorizontalTextPosition(SwingConstants.CENTER);
		securityBtn.addActionListener(this);
		securityBtn.addMouseListener(this);
		
		wfuBtn = new JButton("WiFindUs");
		wfuBtn.setBackground(Color.white);
		wfuBtn.setIcon(ResourcePool.getIcon("cog_inverted_themed"));
		wfuBtn.setBorder(emptyBorder);
		wfuBtn.setFont(font);
		wfuBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
		wfuBtn.setHorizontalTextPosition(SwingConstants.CENTER);
		wfuBtn.addActionListener(this);
		wfuBtn.addMouseListener(this);
		
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(medicalBtn);
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(securityBtn);
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(wfuBtn);
		buttonsPanel.add(Box.createHorizontalGlue());
		
		panel.add(title);
		panel.add(Box.createVerticalGlue());
		panel.add(buttonsPanel);
		panel.add(Box.createVerticalGlue());
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		add(panel);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setUndecorated(true);
		pack();
		setVisible(true);
		addWindowFocusListener(this);
		
	}
	
	public String getIncidentType()
	{
		return type;
	}
	
	
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == medicalBtn)
		{
			Incident i= EyeApplication.get().db_createIncident(Incident.Type.Medical, device.getLocation());
			EyeApplication.get().db_setIncidentReportingUser(i, device.getCurrentUser());
			Debugger.i("New incident reported by "+ device.getCurrentUser().getNameFull() +" at "+ device.getLocation());
    		Dispatcher.get().setEnabled(true);
			dispose();
		}
		else if(e.getSource() == securityBtn)
		{
			Incident i= EyeApplication.get().db_createIncident(Incident.Type.Security, device.getLocation());
			EyeApplication.get().db_setIncidentReportingUser(i, device.getCurrentUser());
			Debugger.i("New incident reported by "+ device.getCurrentUser().getNameFull() +" at "+ device.getLocation());
    		Dispatcher.get().setEnabled(true);
			dispose();
		}
		else if(e.getSource() == wfuBtn)
		{
			Incident i= EyeApplication.get().db_createIncident(Incident.Type.WiFindUs, device.getLocation());
			EyeApplication.get().db_setIncidentReportingUser(i, device.getCurrentUser());
			Debugger.i("New incident reported by "+ device.getCurrentUser().getNameFull() +" at "+ device.getLocation());
    		Dispatcher.get().setEnabled(true);
			dispose();
		}
		
	}
	
	
	@Override public void mouseClicked(MouseEvent e) { }
	@Override public void mousePressed(MouseEvent arg0) { }
	@Override public void mouseReleased(MouseEvent arg0) { }
	@Override public void windowGainedFocus(WindowEvent  we) { }
	
	@Override
	public void mouseEntered(MouseEvent e)
	{
		((JButton)e.getSource()).setBackground(hover);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		((JButton)e.getSource()).setBackground(Color.WHITE);
	}

	@Override
	public void windowLostFocus(WindowEvent  we) 
	{
		Dispatcher.get().setEnabled(true);  
		dispose();
	}
}
