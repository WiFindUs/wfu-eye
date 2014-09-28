package wifindus;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import wifindus.Debugger.Verbosity;

public class DebuggerFrame extends JFrame implements DebuggerEventListener
{
	private static final long serialVersionUID = 7198711131827826655L;
	private JTextPane textPane = null; 
	private StyledDocument textDocument = null;
	private Map<Debugger.Verbosity, Style> styles = new HashMap<>();

	public DebuggerFrame()
	{
		Debugger.addEventListener(this);
		getContentPane().add(new JScrollPane(textPane = new JTextPane()));
		textDocument = textPane.getStyledDocument();
		textPane.setBackground(Color.DARK_GRAY);
		
		//base
		Style baseStyle = textDocument.addStyle( "base",
			StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE ) );
		StyleConstants.setForeground( baseStyle, Color.white );
		StyleConstants.setFontFamily(baseStyle, "monospaced");

		//verbose
		Style verbose = textDocument.addStyle("verbose", baseStyle);
		StyleConstants.setForeground( verbose, Color.gray );
		StyleConstants.setItalic( verbose, true );
		styles.put(Verbosity.Verbose, verbose);
		
		//info
		Style info = textDocument.addStyle("info", baseStyle);
		styles.put(Verbosity.Information, info);
		
		//warning
		Style warning = textDocument.addStyle("warning", baseStyle);
		StyleConstants.setForeground( warning, Color.yellow );
		styles.put(Verbosity.Warning, warning);
		
		//error
		Style error = textDocument.addStyle("error", baseStyle);
		StyleConstants.setForeground( error, Color.red );
		styles.put(Verbosity.Error, error);
		
		//exception
		Style exception = textDocument.addStyle("exception", error);
		StyleConstants.setBold( exception,true );
		styles.put(Verbosity.Exception, exception);
	}

	@Override
	public void debuggerLoggedText(Verbosity verbosity, String timestamp,
			String text)
	{
		try
		{
			textDocument.insertString(textDocument.getLength(),
					timestamp + " " + text + "\n",
					styles.get(verbosity));
		}
		catch (BadLocationException e)
		{
			//// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
	}
}
