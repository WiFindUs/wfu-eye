package wifindus.eye.dispatcher;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SecurityPersonnelPanel extends JPanel
{
	private static final long serialVersionUID = -700461718729384611L;

	public SecurityPersonnelPanel()
    {
        setBackground(Color.WHITE);
         JLabel securityTitle = new JLabel("Security Personnel");
         add(securityTitle);
         add (Box.createRigidArea(new Dimension(0, 10)));
    }
}
