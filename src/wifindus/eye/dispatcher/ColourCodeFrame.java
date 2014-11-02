package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import wifindus.eye.Incident;

public class ColourCodeFrame extends JFrame implements ActionListener, WindowFocusListener{

	private Incident incident;
	private IncidentPanel incidentPanel;
	private String colour;
	private Color red, orange, blue, yellow, brown, purple, black, green, grey; 
	private JButton redBtn, orangeBtn, blueBtn, yellowBtn, brownBtn, purpleBtn, blackBtn, greenBtn, greyBtn;
	private JLabel redLbl, orangeLbl, blueLbl, yellowLbl, brownLbl, purpleLbl, blackLbl, greenLbl, greyLbl;
	private JPanel panel;
	
	public ColourCodeFrame(IncidentPanel incidentPanel)
	{
		this.incidentPanel = incidentPanel;
		setPreferredSize(new Dimension(400, 300));
		setResizable(false);
		panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setBorder(BorderFactory.createLineBorder(Color.black));
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		GroupLayout.SequentialGroup horizontal = layout.createSequentialGroup();
		GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		colour = "red";
		red = new Color(0xfd0b15);
		orange = new Color (0xff9c00);
		blue = new Color(0x004eff);
		yellow = Color.yellow;
		brown = new Color(0xa28725);
		purple = new Color(0xbf1de8);
		black = Color.black;
		green = Color.green;
		grey = Color.gray;
		
		redBtn = new JButton ("Red");
		redLbl = new JLabel ("Fire/Smoke");
		redBtn.setBackground(red);
		redBtn.setForeground(Color.white);
		redBtn.addActionListener(this);
		
		orangeBtn = new JButton ("Orange");
		orangeLbl = new JLabel ("Evacuate");
		orangeBtn.setBackground(orange);
		orangeBtn.setForeground(Color.white);
		orangeBtn.addActionListener(this);
		
		blueBtn = new JButton ("Blue");
		blueLbl = new JLabel ("Medical Emergency");
		blueBtn.setBackground(blue);
		blueBtn.setForeground(Color.white);
		blueBtn.addActionListener(this);
		
		yellowBtn = new JButton ("Yellow");
		yellowLbl = new JLabel ("Failure/threat of failure to essential services");
		yellowBtn.setBackground(yellow);
		yellowBtn.addActionListener(this);
		
		brownBtn = new JButton ("Brown");
		brownLbl = new JLabel ("External Emergencies");
		brownBtn.setBackground(brown);
		brownBtn.setForeground(Color.white);
		brownBtn.addActionListener(this);
		
		purpleBtn = new JButton ("Purple");
		purpleLbl = new JLabel ("Bomb Threat");
		purpleBtn.setBackground(purple);
		purpleBtn.setForeground(Color.white);
		purpleBtn.addActionListener(this);
		
		blackBtn = new JButton ("Black");
		blackLbl = new JLabel ("Personal Threat to others or self");
		blackBtn.setBackground(black);
		blackBtn.setForeground(Color.white);
		blackBtn.addActionListener(this);
		
		greenBtn = new JButton ("Green");
		greenLbl = new JLabel ("Correctional Health Services Emergency");
		greenBtn.setBackground(green);
		greenBtn.addActionListener(this);
		
		greyBtn = new JButton ("Grey");
		greyLbl = new JLabel ("Unarmed Threat");
		greyBtn.setBackground(grey);
		greyBtn.setForeground(Color.white);
		greyBtn.addActionListener(this);
		
		
		//horizontal layout
		GroupLayout.ParallelGroup columnBtns = layout.createParallelGroup();
		GroupLayout.ParallelGroup columnDesc = layout.createParallelGroup();
		
		columnBtns.addComponent(redBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnBtns.addComponent(orangeBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnBtns.addComponent(blueBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnBtns.addComponent(yellowBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnBtns.addComponent(brownBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnBtns.addComponent(purpleBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnBtns.addComponent(blackBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnBtns.addComponent(greenBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnBtns.addComponent(greyBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		columnDesc.addComponent(redLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnDesc.addComponent(orangeLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnDesc.addComponent(blueLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnDesc.addComponent(yellowLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnDesc.addComponent(brownLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnDesc.addComponent(purpleLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnDesc.addComponent(blackLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnDesc.addComponent(greenLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		columnDesc.addComponent(greyLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		horizontal.addGroup(columnBtns);
		horizontal.addGroup(columnDesc);
		
		//vertical layout
		GroupLayout.ParallelGroup redParallel = layout.createParallelGroup();
		redParallel.addComponent(redBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		redParallel.addComponent(redLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		GroupLayout.ParallelGroup orangeParallel = layout.createParallelGroup();
		orangeParallel.addComponent(orangeBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		orangeParallel.addComponent(orangeLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		GroupLayout.ParallelGroup blueParallel = layout.createParallelGroup();
		blueParallel.addComponent(blueBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		blueParallel.addComponent(blueLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		GroupLayout.ParallelGroup yellowParallel = layout.createParallelGroup();
		yellowParallel.addComponent(yellowBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		yellowParallel.addComponent(yellowLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		GroupLayout.ParallelGroup brownParallel = layout.createParallelGroup();
		brownParallel.addComponent(brownBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		brownParallel.addComponent(brownLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		GroupLayout.ParallelGroup purpleParallel = layout.createParallelGroup();
		purpleParallel.addComponent(purpleBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		purpleParallel.addComponent(purpleLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		GroupLayout.ParallelGroup blackParallel = layout.createParallelGroup();
		blackParallel.addComponent(blackBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		blackParallel.addComponent(blackLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		GroupLayout.ParallelGroup greenParallel = layout.createParallelGroup();
		greenParallel.addComponent(greenBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		greenParallel.addComponent(greenLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		GroupLayout.ParallelGroup greyParallel = layout.createParallelGroup();
		greyParallel.addComponent(greyBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		greyParallel.addComponent(greyLbl, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		
		vertical.addGroup(redParallel);
		vertical.addGroup(orangeParallel);
		vertical.addGroup(blueParallel);
		vertical.addGroup(yellowParallel);
		vertical.addGroup(brownParallel);
		vertical.addGroup(purpleParallel);
		vertical.addGroup(blackParallel);
		vertical.addGroup(greenParallel);
		vertical.addGroup(greyParallel);
		
		
		layout.setHorizontalGroup(horizontal);
		layout.setVerticalGroup(vertical);
		
		add(panel);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setUndecorated(true);
		pack();
		setVisible(true);
		addWindowFocusListener(this);
		
		
	}
	@Override
	public void windowGainedFocus(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowLostFocus(WindowEvent arg0) {
		// TODO Auto-generated method stub
		dispose();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == redBtn)
		{
			incidentPanel.setCode("Red", red);
		}
		if (e.getSource() == orangeBtn)
		{
			incidentPanel.setCode("Orange", orange);
		}
		if (e.getSource() == blueBtn)
		{
			incidentPanel.setCode("Blue", blue);
		}
		if (e.getSource() == yellowBtn)
		{
			incidentPanel.setCode("Yellow", yellow);
		}
		if (e.getSource() == brownBtn)
		{
			incidentPanel.setCode("Brown", brown);
		}
		if (e.getSource() == purpleBtn)
		{
			incidentPanel.setCode("Purple", purple);
		}
		if (e.getSource() == blackBtn)
		{
			incidentPanel.setCode("Black", black);
		}
		if (e.getSource() == greenBtn)
		{
			incidentPanel.setCode("Green", green);
		}
		if (e.getSource() == greyBtn)
		{
			incidentPanel.setCode("Grey", grey);
		}

		dispose();
	}
	
}
