package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import wifindus.ResourcePool;
import wifindus.eye.Incident;

public class MapFrame extends JFrame implements ActionListener
{
	private JSplitPane splitPane;
	public MapImagePanel mapImagePanel;
	private JButton gridButton, incidentsButton, nodesButton, devicesButton;
	private Color green = new Color(0x0a9a06);
	private transient JLabel gridLabel, incidentsLabel, nodesLabel, devicesLabel;
	
	static
	{
		ResourcePool.loadImage("grid_icon", "images/grid_icon.png" );
		ResourcePool.loadImage("incident_icon", "images/incident_icon.png" );
		ResourcePool.loadImage("node_icon", "images/node_icon.png" );
		ResourcePool.loadImage("device_icon", "images/device_icon.png" );
	}

	   

	public MapFrame()
	{
		//frame properties
		setMinimumSize(new Dimension(400,400));
		
		//left panel - map		
		JPanel mapPanel = new JPanel();
		mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.X_AXIS));
		mapPanel.add(mapImagePanel = new MapImagePanel());
		
		//right panel - map controls
		ImageIcon gridIcon = ResourcePool.getIcon("grid_icon");	
		ImageIcon incidentIcon = ResourcePool.getIcon("incident_icon");	
		ImageIcon nodeIcon = ResourcePool.getIcon("node_icon");	
		ImageIcon deviceIcon = ResourcePool.getIcon("device_icon");	
		
		Font labelFont = new Font("Courier", Font.PLAIN,26);
				
		gridLabel = new JLabel("Grid");
		gridLabel.setFont(labelFont);
		gridLabel.setIcon(gridIcon);
		
		incidentsLabel = new JLabel("Incidents");
		incidentsLabel.setFont(labelFont);
		incidentsLabel.setIcon(incidentIcon);
		
		nodesLabel = new JLabel("Nodes");
		nodesLabel.setFont(labelFont);
		nodesLabel.setIcon(nodeIcon);
		
		devicesLabel = new JLabel("Devices");
		devicesLabel.setFont(labelFont);
		devicesLabel.setIcon(deviceIcon);
		
		gridButton = new JButton("On");
		gridButton.setPreferredSize(new Dimension(116,30));
		gridButton.setForeground(Color.white);
		gridButton.setBackground(green);
		gridButton.setOpaque(true);
		gridButton.addActionListener(this);
		
		incidentsButton = new JButton("On");
		incidentsButton.setPreferredSize(new Dimension(116,30));
		incidentsButton.setForeground(Color.white);
		incidentsButton.setBackground(green);
		incidentsButton.setOpaque(true);
		incidentsButton.addActionListener(this);
        
		nodesButton = new JButton("On");
		nodesButton.setPreferredSize(new Dimension(116,30));
		nodesButton.setForeground(Color.white);
		nodesButton.setBackground(green);
		nodesButton.setOpaque(true);
		nodesButton.addActionListener(this);
		
		devicesButton = new JButton("On");
		devicesButton.setPreferredSize(new Dimension(116,30));
		devicesButton.setForeground(Color.white);
		devicesButton.setBackground(green);
		devicesButton.setOpaque(true);
		devicesButton.addActionListener(this);
        
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.X_AXIS));
		gridPanel.add(Box.createRigidArea(new Dimension(10,0)));
		gridPanel.add(gridLabel);
		gridPanel.add(Box.createHorizontalGlue());
        gridPanel.add(gridButton);
        
    	JPanel incidentsPanel = new JPanel();
    	incidentsPanel.setLayout(new BoxLayout(incidentsPanel, BoxLayout.X_AXIS));
    	incidentsPanel.add(Box.createRigidArea(new Dimension(10,0)));
    	incidentsPanel.add(incidentsLabel);
    	incidentsPanel.add(Box.createHorizontalGlue());
    	incidentsPanel.add(incidentsButton);
        
    	JPanel nodesPanel = new JPanel();
    	nodesPanel.setLayout(new BoxLayout(nodesPanel, BoxLayout.X_AXIS));
    	nodesPanel.add(Box.createRigidArea(new Dimension(10,0)));
    	nodesPanel.add(nodesLabel);
    	nodesPanel.add(Box.createHorizontalGlue());
    	nodesPanel.add(nodesButton);
    	
    	JPanel devicesPanel = new JPanel();
    	devicesPanel.setLayout(new BoxLayout(devicesPanel, BoxLayout.X_AXIS));
    	devicesPanel.add(Box.createRigidArea(new Dimension(10,0)));
    	devicesPanel.add(devicesLabel);
    	devicesPanel.add(Box.createHorizontalGlue());
    	devicesPanel.add(devicesButton);
		
		JPanel mapControls = new JPanel();
		
		mapControls.setLayout(new BoxLayout(mapControls, BoxLayout.Y_AXIS));
		mapControls.add(Box.createRigidArea(new Dimension(0,10)));
		mapControls.add(gridPanel);
		mapControls.add(Box.createRigidArea(new Dimension(0,10)));
		mapControls.add(incidentsPanel);
		mapControls.add(Box.createRigidArea(new Dimension(0,10)));
		mapControls.add(nodesPanel);
		mapControls.add(Box.createRigidArea(new Dimension(0,10)));
		mapControls.add(devicesPanel);
						
		//outer splitter
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, mapControls);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(300);

		add(splitPane);
	}
	

	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if (source == null)
			return;
		
		if(source == gridButton)
		{
			
			if(gridButton.getText() == "On")
			{
				mapImagePanel.setDrawGrid(false);
				gridButton.setBackground(Color.red);
				gridButton.setText("Off");
			}
			else
			{
				mapImagePanel.setDrawGrid(true);
				gridButton.setBackground(green);
				gridButton.setText("On");
			}
		}
		else if(source == incidentsButton)
		{
			if(incidentsButton.getText() == "On")
			{
				mapImagePanel.setDrawIncidents(false);
				incidentsButton.setBackground(Color.red);
				incidentsButton.setText("Off");
			}
			else
			{
				mapImagePanel.setDrawIncidents(true);
				incidentsButton.setBackground(green);
				incidentsButton.setText("On");
			}
		}
		 
		else if(source == nodesButton)
		{
			if(nodesButton.getText() == "On")
			{
				mapImagePanel.setDrawNodes(false);
				nodesButton.setBackground(Color.red);
				nodesButton.setText("Off");
			}
			else
			{
				mapImagePanel.setDrawNodes(true);
				nodesButton.setBackground(green);
				nodesButton.setText("On");
			}
		}
		else if(source == devicesButton)
		{
			if(devicesButton.getText() == "On")
			{
				mapImagePanel.setDrawDevices(false);
				devicesButton.setBackground(Color.red);
				devicesButton.setText("Off");
			}
			else
			{
				mapImagePanel.setDrawDevices(true);
				devicesButton.setBackground(green);
				devicesButton.setText("On");
			}
		}
			
	} 

}

