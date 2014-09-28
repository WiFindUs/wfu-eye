package wifindus;

/**
 * An object which listens for changes in the Debugger output. 
 * @author Mark 'marzer' Gillard
 */
public interface DebuggerEventListener
{
	void debuggerLoggedText(Debugger.Verbosity verbosity, String timestamp, String text);
}
