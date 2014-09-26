package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MedicalPersonnelPanel extends JPanel 
{
    int numOfSecurityPersonnel = 10;
    ArrayList<PersonnelRecordPanel> medicalPersonnelPanel;
    ArrayList<Person> availibleMedical;
    public static ArrayList<Person> people;
    
    
    public static ArrayList<Person> getAvailibleMedical()
    {
        return people;
    }

    
    public MedicalPersonnelPanel()
    {
        setLayout (new GridLayout (numOfSecurityPersonnel+1,1));
       
        JLabel medicalTitle = new JLabel("Medical Personnel");
        add(medicalTitle);
        
        medicalPersonnelPanel = new ArrayList<>();
        availibleMedical = new ArrayList<>();
                     
        Person p1 = new Person(1, "AAAAAAAA", "AAAAAAAA", "Medical", "Availible", 1, 1);
        Person p2 = new Person(2, "BBBBBBBBB", "BBBBBBBBB", "Medical","On Assignment", 1, 1);
        Person p3 = new Person(3, "CCCCCCCCC", "CCCCCCCCC", "Medical", "On Assignment",1, 1);
        Person p4 = new Person(4, "DDDDDDDDD", "DDDDDDDDD", "Medical", "Availible",1, 1);
        Person p5 = new Person(5, "EEEEEEEEE", "EEEEEEEEE", "Medical", "Availible",1, 1);
        Person p6 = new Person(6, "FFFFFFFFF", "FFFFFFFFF", "Security", "Availible",1, 1);
        Person p7 = new Person(7, "GGGGGGGGG", "GGGGGGGGG", "Security", "Availible",1, 1);
        Person p8 = new Person(8, "HHHHHHHHH", "HHHHHHHHH", "Security", "Availible",1, 1);
        Person p9 = new Person(9, "IIIIIIIII", "IIIIIIIII", "Security", "Availible",1, 1);
        Person p10 = new Person(10, "JJJJJJJJJJ", "JJJJJJJJJJ", "Security", "Availible",1, 1);
        
        people = new ArrayList<>(); 
         
        ArrayList<PersonnelRecordPanel> personnel = new ArrayList<>(); 
         
        people.add(p1);
        people.add(p2);
        people.add(p3);
        people.add(p4);
        people.add(p5);
        people.add(p6);
        people.add(p7);
        people.add(p8);
        people.add(p9);
        people.add(p10);
         
        for(int i = 0; i <= people.size()-1; i++)
        {
            personnel.add(new PersonnelRecordPanel(people.get(i)));
        }
        
        for(int i = 0; i <= personnel.size()-1; i++)
        {
            medicalPersonnelPanel.add(personnel.get(i));
        }
 
        for(int i = 0; i < medicalPersonnelPanel.size(); i++)
        {
            add(medicalPersonnelPanel.get(i));
            medicalPersonnelPanel.get(i).addMouseListener(new RatingMouseListener(i));
        }
    }
    
    private class RatingMouseListener extends MouseAdapter 
    {
        private final int index;

        public RatingMouseListener(int index) 
        {
            this.index = index;
        }

        @Override
        public void mouseEntered(MouseEvent e) 
        {
            if(medicalPersonnelPanel.get(index).getSelected() == false)
            {
                medicalPersonnelPanel.get(index).setBackground(new Color(0x0099FF)); 
                medicalPersonnelPanel.get(index).personnelDetailsPanel.setBackground(new Color(0x0099FF)); 
            }
        }

        @Override
        public void mouseExited(MouseEvent e) 
        {
            if(medicalPersonnelPanel.get(index).getSelected() == false)
            {
                medicalPersonnelPanel.get(index).setBackground(Color.WHITE);
                medicalPersonnelPanel.get(index).personnelDetailsPanel.setBackground(Color.WHITE);
            }
        }
        @Override
        public void mouseClicked(MouseEvent e) 
        {
             medicalPersonnelPanel.get(index).toggleSelected();
        }
    }
}
    
  
    
   
