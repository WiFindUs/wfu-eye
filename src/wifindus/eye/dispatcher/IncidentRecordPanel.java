package wifindus.eye.dispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

public class IncidentRecordPanel extends JPanel implements ActionListener//implements ComponentListener 
{

	private static final long serialVersionUID = 4363655046753158080L;

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	/*
    String type = null;
    String description = null;
     
    static int seconds;
    static java.util.Timer timer;
    static int minutes = 0;
    static int hours = 0;
    
    JButton resolveIncident;
    JButton cancelIncident;
   
    JButton addMedicalButton;
    JButton addSecurityButton;
    
    JButton editDescription;
    JButton saveDescription;
    JPanel centerPanel;
    JPanel listOfRespondents;
        
    
    JRadioButton allRadio;
     JRadioButton medicalRadio;
        JRadioButton securityRadio;
    
    public IncidentRecordPanel(int incidentNo, int id, int latitude, int longitude) throws InterruptedException 
    {      
        ///////////////////////////////////////////////////////////
        // Incident Timer
        ////////////////////////////////////////////////////////////
        int delay = 1000;
        int period = 1000;
        timer = new java.util.Timer();
        seconds = 0;
        timer.scheduleAtFixedRate(new TimerTask() 
        {
            @Override
            public void run() 
            {
                setInterval();
            
                String secondsDisplay = Integer.toString(seconds);
                String minutesDisplay = Integer.toString(minutes);
                String hoursDisplay = Integer.toString(hours);
            
                if(seconds < 10)
                {
                    secondsDisplay = "0" + Integer.toString(seconds);
                }
                if(minutes < 10)
                {
                    minutesDisplay = "0" + Integer.toString(minutes);
                }
                if(hours < 10)
                {
                    hoursDisplay = "0" + Integer.toString(hours);
                }
                incidentTime.setText("Time: "+hoursDisplay+" : "+minutesDisplay+" : "+secondsDisplay);
            }
        }, delay, period);

        ///////////////////////////////////////////////////////////
        // Add Respondents
        ////////////////////////////////////////////////////////////
        
        // Center Panel
        centerPanel = new JPanel();
        centerPanel.setBorder(blackLine);
        centerPanel.setLayout (new BoxLayout (centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        
        
       // JPanel titlePanel = new JPanel();
        //titlePanel.setLayout (new BoxLayout (titlePanel, BoxLayout.X_AXIS));
        
        // Title
        JLabel addRespondentsTitle = new JLabel("Responding Personnel");
        addRespondentsTitle.setFont(addRespondentsTitle.getFont().deriveFont(21.0f));
        //addRespondentsTitle.setHorizontalAlignment(JLabel.CENTER);
       
         centerPanel.add(addRespondentsTitle);
        
          listOfRespondents = new JPanel();
            listOfRespondents.setLayout (new BoxLayout(listOfRespondents, BoxLayout.Y_AXIS));
        
            
            
            
            
            // Respondent List Titles
            JPanel respondentRecord = new JPanel();
            respondentRecord.setBackground(new Color(0f,0.3f,1f,0.3f ));
            
            respondentRecord.setBorder(BorderFactory.createMatteBorder(2,0,2,0,Color.black));
            respondentRecord.setLayout(new BoxLayout(respondentRecord, BoxLayout.X_AXIS));
            respondentRecord.setMaximumSize(new Dimension(20000, 33));
            respondentRecord.setLayout(new GridLayout(1,2));
            ImageIcon individualMedicalLogo = new ImageIcon("images/all_logo_small.png");
            JLabel respondentName = new JLabel("Respondent");
            respondentName.setIcon(individualMedicalLogo);
            JLabel respondentDistanceToIncident = new JLabel("Distance to Incident");
            respondentRecord.add(respondentName);
            respondentRecord.add(respondentDistanceToIncident);
            listOfRespondents.add(respondentRecord);
            centerPanel.add(listOfRespondents);
            
            
            
            
            
            
            
            // Add a new Respondent
            JPanel addRespondent = new JPanel();
            addRespondent.setBorder(BorderFactory.createMatteBorder(2,0,2,0,Color.black));
            addRespondent.setLayout(new BoxLayout(addRespondent, BoxLayout.X_AXIS));
            addRespondent.setMaximumSize(new Dimension(20000, 33));
            addRespondent.setLayout(new GridLayout(1,2));
            ImageIcon  addMedicalLogo = new ImageIcon("images/add_medical_logo_small.png");
            ImageIcon addSecurityLogo = new ImageIcon("images/add_security_logo_small.png");
            addMedicalButton = new JButton(addMedicalLogo);
            addSecurityButton = new JButton(addSecurityLogo);
            addMedicalButton.addActionListener(this);
            addSecurityButton.addActionListener(this);
            JLabel assignNearestPersonTitle = new JLabel("  Assign nearest availible person");
            JPanel addRespondantButtonsAndTitle = new JPanel();
            
            addRespondantButtonsAndTitle.setBackground(new Color(0f,1f,0f,0.5f ));
            addRespondantButtonsAndTitle.setLayout(new BoxLayout(addRespondantButtonsAndTitle, BoxLayout.X_AXIS));
            addRespondantButtonsAndTitle.add(addMedicalButton);
            addRespondantButtonsAndTitle.add(addSecurityButton);
            addRespondantButtonsAndTitle.add(assignNearestPersonTitle);
            addRespondent.add(addRespondantButtonsAndTitle);
            centerPanel.add(addRespondent);
            
            add(centerPanel, BorderLayout.CENTER);
        
        ///////////////////////////////////////////////////////////
        // Description
        ////////////////////////////////////////////////////////////
        JPanel eastPanel = new JPanel();
        eastPanel.setBackground(Color.WHITE);
        eastPanel.setBorder(blackLine);
        
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
        
        JLabel descriptionLabel = new JLabel("Description");
        descriptionLabel.setHorizontalAlignment(JLabel.LEFT);
        descriptionLabel.setFont(addRespondentsTitle.getFont().deriveFont(21.0f));  
        
        JPanel descriptionPanel = new JPanel(); 
        descriptionPanel.setLayout(new GridLayout(1,2));
      
        
        editDescription = new JButton("Edit"); 
        saveDescription = new JButton("Save");
        editDescription.addActionListener(this);
        saveDescription.addActionListener(this);
       
        eastPanel.add(descriptionLabel);
         descriptionPanel.add(editDescription);
        descriptionPanel.add(saveDescription);
        eastPanel.add(descriptionPanel);
        
        JTextArea textArea = new JTextArea();
        JScrollPane incidentDescription = new JScrollPane(textArea);
        incidentDescription.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        incidentDescription.setPreferredSize(new Dimension(300, 100));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        eastPanel.add(incidentDescription);
       
        ///////////////////////////////////////////////////////////
        // Severity
        ////////////////////////////////////////////////////////////
        ///JPanel severityPanel = new JPanel(); 
        
        JLabel severityLabel = new JLabel("Severity");
        severityLabel.setFont(addRespondentsTitle.getFont().deriveFont(21.0f));
        //JPanel severityDisplayPanel = new JPanel();
        JSlider severitySlider = new JSlider (JSlider.HORIZONTAL, 1, 5, 1);
        severitySlider.setBackground(Color.WHITE);
        severitySlider.setMajorTickSpacing(1);
        severitySlider.setPaintTicks(true);
        severitySlider.setPaintLabels(true);
       // severityDisplayPanel.add(severitySlider);
        
        eastPanel.add(severityLabel);
        eastPanel.add(severitySlider);
       
       // eastPanel.add(severityPanel);
        
        ///////////////////////////////////////////////////////////
        // Resolve and Archive Incident
        ////////////////////////////////////////////////////////////
        JPanel resolveAndArchivePanel = new JPanel(); 
        resolveAndArchivePanel.setLayout(new BoxLayout(resolveAndArchivePanel, BoxLayout.X_AXIS));
        
        resolveIncident = new JButton("Set Resolved and Archive");
        resolveIncident.addActionListener(this);
        
        cancelIncident = new JButton("Cancel Incident");
        
        resolveAndArchivePanel.add(resolveIncident);
        resolveAndArchivePanel.add(cancelIncident);
        eastPanel.add(resolveAndArchivePanel);
        add(eastPanel, BorderLayout.EAST);
    }

    
    
    public void actionPerformed(ActionEvent e) 
    {
     
         if(e.getSource() == allRadio)
         {
             allRadio.setBackground(new Color(0x00CCFF));
             securityRadio.setBackground(Color.WHITE);
                  medicalRadio.setBackground(Color.WHITE);
         }
            
       
               if(e.getSource() == securityRadio)
         {
             securityRadio.setBackground(new Color(0x00CCFF));
             allRadio.setBackground(Color.WHITE);
              medicalRadio.setBackground(Color.WHITE);
         }
        
               if(e.getSource() == medicalRadio)
         {
             medicalRadio.setBackground(new Color(0x00CCFF));
             allRadio.setBackground(Color.WHITE);
              securityRadio.setBackground(Color.WHITE);
         }
        
        // Button to resolve and archive incident
        if(e.getSource() == resolveIncident)
        {
            Debugger.w("Resolve and archive");
        }
        
        // Button to add a medical respondent
        if(e.getSource() == addMedicalButton)
        {
            ArrayList<Person> allMedical=  new ArrayList<Person>();
            allMedical = MedicalPersonnelPanel.getAvailibleMedical();

            for(int i = 0; i <= allMedical.size()-1; i++)
                 {
                     if("Availible".equals(allMedical.get(i).getStatus()) && "Medical".equals(allMedical.get(i).getType()))
                     {
                        //availibleMedical.add(allMedical.get(i));
                        MedicalPersonnelPanel.people.get(i).setStatus("On Assignment");
                        JPanel respondentRecord = new JPanel();
                        respondentRecord.setBackground(Color.WHITE);
                        respondentRecord.setMaximumSize(new Dimension(20000, 30));
                        respondentRecord.setLayout(new GridLayout(1,2));
                        String iconSource;
                       
                        iconSource = "images/medical_logo_small.png";
                   
                        //Add the person to the list of respondents
                        JLabel name = new JLabel(allMedical.get(i).getFirstName() + " " + allMedical.get(i).getLastName());
                        ImageIcon respondentIcon = new ImageIcon(iconSource);
                        name.setIcon(respondentIcon);
                        JLabel respondentDistanceToIncident = new JLabel(allMedical.get(i).getLatitude() + " meter(s)");
                        respondentRecord.add(name);
                        respondentRecord.add(respondentDistanceToIncident);
                        listOfRespondents.add(respondentRecord);
                        break;
                     }
                 }
        }
        
        
        
          if(e.getSource() == addSecurityButton)
        {
            
            ArrayList<Person> allMedical=  new ArrayList<Person>();
            allMedical = MedicalPersonnelPanel.getAvailibleMedical();

            for(int i = 0; i <= allMedical.size()-1; i++)
                 {
                     if("Availible".equals(allMedical.get(i).getStatus()) && "Security".equals(allMedical.get(i).getType()))
                     {
                        //availibleMedical.add(allMedical.get(i));
                        MedicalPersonnelPanel.people.get(i).setStatus("On Assignment");
                        
                        JPanel respondentRecord = new JPanel();
                         respondentRecord.setBackground(Color.WHITE);
                        respondentRecord.setMaximumSize(new Dimension(20000, 30));
                        respondentRecord.setLayout(new GridLayout(1,2));
                        String iconSource;
                        iconSource = "images/security_logo_small.png";
                   
                        //Add the person to the list of respondents
                        JLabel name = new JLabel(allMedical.get(i).getFirstName() + " " + allMedical.get(i).getLastName());
                        ImageIcon respondentIcon = new ImageIcon(iconSource);
                        name.setIcon(respondentIcon);
                        JLabel respondentDistanceToIncident = new JLabel(allMedical.get(i).getLatitude() + " meter(s)");
                        respondentRecord.add(name);
                        respondentRecord.add(respondentDistanceToIncident);
                        listOfRespondents.add(respondentRecord);
                        break;
                     }
                 }
        }
          
        if(e.getSource() == editDescription)
        {
            Debugger.w("edit");
        }
        
        if(e.getSource() == saveDescription)
        {
        	Debugger.w("save");
        }
        
             
    } 
     
    private static int setInterval() 
    {
        seconds++;
        if (seconds == 60)
        {
            minutes++;
            seconds = 0;
        }
        
        if(minutes == 60)
        {
            hours++;
            minutes = 0;
        }
    return seconds;
    }
    */
}
