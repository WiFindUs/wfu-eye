package wifindus;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import wifindus.Debugger.Verbosity;

public class DebuggerPanel extends JScrollPane implements DebuggerEventListener
{
	private static final long serialVersionUID = 5230192536977384055L;
	private JTextPane textPane = null; 
	private StyledDocument textDocument = null;
	private Map<Debugger.Verbosity, Style> styles = new HashMap<>();
	
	public DebuggerPanel()
	{
		super(new JTextPane());
		textPane = (JTextPane)this.getViewport().getView();
		textPane.setEditable(false);
		textDocument = textPane.getStyledDocument();
		textPane.setBackground(Color.BLACK);
		((DefaultCaret)textPane.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		//base
		Style baseStyle = textDocument.addStyle( "base",
			StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE ) );
		StyleConstants.setForeground( baseStyle, Color.white );
		StyleConstants.setFontFamily(baseStyle, "monospaced");
		StyleConstants.setFontSize( baseStyle, 14 );

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
		
		//console
		Style console = textDocument.addStyle("console", baseStyle);
		StyleConstants.setForeground( console, new Color(0,128,0) );
		styles.put(Verbosity.Console, console);
		
		Debugger.addEventListener(this);
	}
	
	@Override
	public void debuggerLoggedText(Verbosity verbosity, String timestamp,
			String text)
	{
		try
		{
			textDocument.insertString(textDocument.getLength(),
					(verbosity == Verbosity.Console ? "" : timestamp+ " ")  + text + "\n",
					styles.get(verbosity));
			textPane.setCaretPosition(textDocument.getLength());
		}
		catch (BadLocationException e)
		{
		}		
	}
	
	public void clear()
	{
		textPane.setText("");
	}
}
