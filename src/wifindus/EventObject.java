package wifindus;

import java.util.ArrayList;

/**
 * Generic parent object for event listener management.
 * @author Mark 'marzer' Gillard
 * @param <T> The event listener interface type.
 */
public abstract class EventObject<T>
{
	private ArrayList<T> listeners = new ArrayList<>();
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	@SafeVarargs
	public EventObject(T... listeners)
	{
		for (T listener : listeners)
			addEventListener(listener);
	}
	
	public EventObject()
	{
		
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	public final void addEventListener(T listener)
	{
		if (listener == null || listeners.contains(listener))
			return;
		listeners.add(listener);
	}
	
	public final void removeEventListener(T listener)
	{
		if (listener == null)
			return;
		listeners.remove(listener);
	}
	
	public final void clearEventListeners(T listener)
	{
		if (listener == null)
			return;
		listeners.clear();
	}
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	protected final void fireEvent(String event, Object data)
	{
		if (event == null || event.equals(""))
			return;
		for (T listener : listeners)
			mapEvents(event, listener, data);
	}
	
	protected final void fireEvent(String event)
	{
		fireEvent(event, null);	
	}
	
	protected abstract void mapEvents(String event, T listener, Object data);
}
