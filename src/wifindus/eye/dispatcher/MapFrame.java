package wifindus.eye.dispatcher;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;


public class MapFrame extends JFrame implements ComponentListener, ActionListener
{
	private MapImagePanel mp;
	private JSplitPane splitPane;
	private JRadioButton grid;
	
	public MapFrame()
	{
		
		JPanel mapControls = new JPanel();
		mapControls.setLayout(new BoxLayout(mapControls, BoxLayout.Y_AXIS));
		
		 grid = new JRadioButton("Grid");
		JRadioButton incidents = new JRadioButton("Incidents");
		JRadioButton allPeople = new JRadioButton("All People");
		JCheckBox onlyAvailible = new JCheckBox("Only Availible");
		JCheckBox onlyUnavailible = new JCheckBox("Only Unavailible");
		JCheckBox medical = new JCheckBox("Medical");
		JCheckBox security = new JCheckBox("Security");
		JCheckBox wfuPersonnel = new JCheckBox("WFU Personnel");
		JRadioButton nodes = new JRadioButton("Nodes");
		
		grid.addActionListener(this);
		
		mapControls.add(grid);
		mapControls.add(incidents);
		mapControls.add(allPeople);
		mapControls.add(onlyAvailible);
		mapControls.add(onlyUnavailible);
		mapControls.add(medical);
		mapControls.add(security);
		mapControls.add(wfuPersonnel);
		mapControls.add(nodes);
		
		addComponentListener(this);
		
		JPanel mapPanel = new JPanel();
		mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.X_AXIS));
		
		setMinimumSize(new Dimension(400,400));
		
		 mp = new MapImagePanel();
	 
		mapPanel.add(mp);
		mapPanel.add(mapControls);
	
		
		 splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,mp, mapControls);
		
		
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(300);

		//Provide minimum sizes for the two components in the split pane
		double minSplitSize = getWidth() * 0.80;
		
		Dimension minimumSize = new Dimension((int)minSplitSize, 50);
		mp.setMinimumSize(minimumSize);
		
		
		
		add(splitPane);
	}
	
	public MapImagePanel getMapPanel()
	{
		return mp;
	}

	@Override
	public void componentResized(ComponentEvent arg0)
	{
		double minSplitSize = getWidth() * 0.75;
		Dimension minimumSize = new Dimension((int)minSplitSize, 50);
		splitPane.setDividerLocation((int)minSplitSize);
		mp.setMinimumSize(minimumSize);
		
	}
	
	@Override public void componentHidden(ComponentEvent arg0) { }
	@Override public void componentMoved(ComponentEvent arg0) { }
	@Override public void componentShown(ComponentEvent arg0) { }
	
	
	
	
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == grid)
		{
			if(grid.isSelected())
			{
				MapImagePanel.toggleGrid(true);
			}

			else
			{
				MapImagePanel.toggleGrid(false);
			}
		
			repaint();
			revalidate();
		}
	} 
}
