package wifindus.eye.dispatcher;

import java.awt.*;
import javax.swing.*;

// Panel connntaining lists of medical and security personnel
public class PersonnelPanel extends JPanel 
{
    public PersonnelPanel()
    {
        setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));
        
        // List of Medical personnel
        MedicalPersonnelPanel medicalPersonnelPanel = new MedicalPersonnelPanel();
        JScrollPane medicalPersonnelScrollPanel = new JScrollPane(medicalPersonnelPanel);
        
        //List of Security personnel
        SecurityPersonnelPanel securityPersonnelPanel = new SecurityPersonnelPanel();
        JScrollPane securityPersonnelScrollPanel = new JScrollPane(securityPersonnelPanel);
        
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, medicalPersonnelScrollPanel, securityPersonnelScrollPanel);
        sp.setResizeWeight(0.5);
        sp.setOneTouchExpandable(true);
        add (sp);
        
        setBackground(Color.CYAN);
    }
}
