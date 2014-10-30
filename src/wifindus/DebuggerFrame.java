package wifindus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JTextField;

public class DebuggerFrame extends JFrame implements KeyListener
{
	private static final long serialVersionUID = 7198711131827826655L;
	private DebuggerPanel debuggerPanel;
	private JTextField textBox;
	private transient static final Pattern PATTERN_SET
	= Pattern.compile( "^set[ \t]+([a-z0-9_\\-.]+)[ \t]*(.+)[ \t]*$", Pattern.CASE_INSENSITIVE);

	public DebuggerFrame()
	{
		this.setTitle("Console");
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(400,400));
		getContentPane().add(debuggerPanel = new DebuggerPanel(), BorderLayout.CENTER);
		getContentPane().add(textBox = new JTextField(), BorderLayout.PAGE_END);
		textBox.setBackground(Color.DARK_GRAY);
		textBox.setForeground(Color.WHITE);
		textBox.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		textBox.addKeyListener(this);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
		
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			command(textBox.getText().trim());
			textBox.setText("");
			e.consume();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
		
	}
	
	private void command(String s)
	{
		if (s.equalsIgnoreCase("clear"))
		{
			debuggerPanel.clear();
			Debugger.c("Console cleared.");
			return;
		}
		
		Debugger.c("> " + s);
		
		Matcher m = PATTERN_SET.matcher(s);
		if (m.matches())
		{
			switch (m.group(1).toLowerCase())
			{
				case "verbosity":
					try
					{
						Debugger.setMinVerbosity(Integer.parseInt(m.group(2)));
						Debugger.c("Debugger verbosity set to " + Debugger.getMinVerbosity().toString());
					}
					catch (NumberFormatException e)
					{
						Debugger.c("Can't set verbosity: not a valid integer \"%s\"", m.group(2));
					}
					break;
			}
			
			return;
		}
	}
}
