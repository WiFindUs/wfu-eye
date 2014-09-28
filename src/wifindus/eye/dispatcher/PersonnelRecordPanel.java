package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class PersonnelRecordPanel extends JPanel implements ActionListener,ItemListener
                                            
{

	private static final long serialVersionUID = -953467312117311967L;
	// Record is Selected
    boolean selected = false;
    Person p;
    JButton createIncidentButton;
    int width;
    ArrayList<IncidentRecordPanel> incidents;
    ArrayList<Incident> incidentRecords;
    IncidentRecordPanel incident1;
    int incidentNo = 1;
    JCheckBox selectedCheckBox;
    JPanel personnelDetailsPanel;
     
    public PersonnelRecordPanel(Person p)
    {
        this.p = p;
        setBackground(new Color(0x00CCFF));
        setBackground(Color.WHITE);
        
        //Border blackLineLeft = BorderFactory.createMatteBorder(0,2,0,0,Color.black);
        
        Border blackLine = BorderFactory.createLineBorder(Color.black);
        setBorder(blackLine);
        
        setMaximumSize(new Dimension(20,20));
      
        incidents = new ArrayList<>();
	incidentRecords = new ArrayList<>();
       
        // Selected check box
        selectedCheckBox = new JCheckBox();
        selectedCheckBox.addItemListener(this);
        selectedCheckBox.setSelected(selected);
        add(selectedCheckBox);
        
        ImageIcon medicalLogo = new ImageIcon("images/medical_logo.png");
  
        setLayout (new BoxLayout (this, BoxLayout.X_AXIS));
        JLabel logo = new JLabel(medicalLogo);
               
        personnelDetailsPanel = new JPanel();
        personnelDetailsPanel.setLayout (new BoxLayout (personnelDetailsPanel, BoxLayout.X_AXIS));        
       
        personnelDetailsPanel.setBackground(Color.WHITE);
        
        // Name
        JLabel name = new JLabel(p.getFirstName() + " " + p.getLastName());
        name.setFont(name.getFont().deriveFont(13.0f));
        
        // Location
        JLabel location = new JLabel("34.9290° S, 138.6010° E");
        location.setFont(location.getFont().deriveFont(13.0f));
        
        // Status
        JLabel status = new JLabel(p.getStatus()); 
        
        if("Availible".equals(p.getStatus()))
        {
           status.setForeground(new Color(0x009900)); 
        }
        else
        {
            status.setForeground(Color.RED); 
        }
        
         
        
        status.setFont(status.getFont().deriveFont(13.0f));
        
        // Create an Incident 
        createIncidentButton = new JButton("New Incident");
       
        JPanel createIncidentButtonPanel = new JPanel();
        createIncidentButtonPanel.setLayout(new BoxLayout(createIncidentButtonPanel, BoxLayout.X_AXIS));
        createIncidentButtonPanel.add(createIncidentButton);
        createIncidentButton.setMaximumSize(new Dimension(107,30));
        createIncidentButtonPanel.setBackground(new Color(0f,0f,0f,0f ));
        createIncidentButton.addActionListener(this);
        
     
        JPanel nameAndLocationPanel = new JPanel();
        nameAndLocationPanel.setLayout(new GridLayout (3,1));
       nameAndLocationPanel.setBackground(new Color(0f,0f,0f,0f ));
        nameAndLocationPanel.add(name);
        nameAndLocationPanel.add(status);
        nameAndLocationPanel.add(location);
        
   
        //namePanel.add(status);

        personnelDetailsPanel.add(nameAndLocationPanel);
        personnelDetailsPanel. add(createIncidentButtonPanel);
        personnelDetailsPanel.add(Box.createRigidArea(new Dimension(10,0)));
        //personnelDetailsPanel.add (location);
        
        
        add(logo);
        add(personnelDetailsPanel);
       
    } 
     
    
    @Override
    public void itemStateChanged(ItemEvent e) 
    {
        selected = e.getStateChange() == ItemEvent.SELECTED;
    }
 
    
    // Listener for New Incident button 
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        if(e.getSource() == createIncidentButton)
        {
            Dispatcher.incidentPanel.newIncident(p.id,p.latitude,p.longitude);
        }
    }
     
    // Toggle an individual being selected 
    public void toggleSelected ()
    {
        if(selected == true)
        {
            selected = false;
            setBackground(Color.WHITE);
            selectedCheckBox.setSelected(selected);
            personnelDetailsPanel.setBackground(Color.WHITE);
        }
        else
        {
            selected = true;
            
            selectedCheckBox.setSelected(selected);
            
            //Color when selected
            personnelDetailsPanel.setBackground(new Color(0x00CCFF));
            setBackground(new Color(0x00CCFF));
        }
    }
    
    // Return if a person is selected
    public boolean getSelected ()
    {
        return selected;
    }
}