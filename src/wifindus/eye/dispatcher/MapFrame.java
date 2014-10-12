package wifindus.eye.dispatcher;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class MapFrame extends JFrame implements ActionListener
{
	private JSplitPane splitPane;
	private JCheckBox drawGrid, drawIncidents, drawNodes, drawDevices;
	private MapImagePanel mapImagePanel;

	public MapFrame()
	{
		//frame properties
		setMinimumSize(new Dimension(400,400));
		
		//left panel - map		
		JPanel mapPanel = new JPanel();
		mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.X_AXIS));
		mapPanel.add(mapImagePanel = new MapImagePanel());
		
		//right panel - map controls
		(drawGrid = new JCheckBox("Grid")).addActionListener(this);
		drawGrid.setSelected(true);
		(drawIncidents = new JCheckBox("Incidents")).addActionListener(this);
		drawIncidents.setSelected(true);
		(drawNodes = new JCheckBox("Nodes")).addActionListener(this);
		drawNodes.setSelected(true);
		(drawDevices = new JCheckBox("Devices/Users")).addActionListener(this);
		drawDevices.setSelected(true);
		JPanel mapControls = new JPanel();
		mapControls.setLayout(new BoxLayout(mapControls, BoxLayout.Y_AXIS));
		mapControls.add(drawGrid);
		mapControls.add(drawIncidents);
		mapControls.add(drawNodes);
		mapControls.add(drawDevices);

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
		
		if (source == drawGrid)
			mapImagePanel.setDrawGrid(drawGrid.isSelected());
		else if (source == drawIncidents)
			mapImagePanel.setDrawIncidents(drawIncidents.isSelected());
		else if (source == drawNodes)
			mapImagePanel.setDrawNodes(drawNodes.isSelected());
		else if (source == drawDevices)
			mapImagePanel.setDrawDevices(drawDevices.isSelected());
	} 
}
