package wifindus;

import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Generic parent object for event listener management.
 * @author Mark 'marzer' Gillard
 * @param <T> The event listener interface type.
 */
public abstract class EventObject<T>
{
	private transient volatile CopyOnWriteArrayList<T> listeners = new CopyOnWriteArrayList<>();
	
	/////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructs a new EventObject.
	 * @param listeners A variable-length list of event listeners that will watch this object's state.
	 */
	@SafeVarargs
	public EventObject(T... listeners)
	{
		for (T listener : listeners)
			addEventListener(listener);
	}
	
	/**
	 * Constructs a new EventObject.
	 */
	public EventObject()
	{
		
	}
	
	/////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Adds a new event listener.
	 * @param listener subscribes an event listener to this object's state events.
	 */
	public final void addEventListener(T listener)
	{
		if (listener == null || listeners.contains(listener))
			return;
		
		synchronized(listeners)
		{
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes an existing event listener. 
	 * @param listener unsubscribes an event listener from this object's state events.
	 * Has no effect if this parameter is null, or is not currently subscribed to this object.
	 */
	public final void removeEventListener(T listener)
	{
		if (listener == null)
			return;
		synchronized(listeners)
		{
			listeners.remove(listener);
		}
	}
	
	/**
	 * Unsubscribes all event listeners from this object's state events.
	 */
	public final void clearEventListeners()
	{
		synchronized(listeners)
		{
			listeners.clear();
		}
	}
	
	/////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Fires an internal event, calling the appropriate listener functions on all
	 * subscribed listener objects.
	 * @param event The key representing the recipient event. Case-insensitive; will be converted to lowercase. 
	 * @param data Optional variable-length list of data to pass to recipient functions via {@link #mapEvents}.
	 */
	protected final void fireEvent(String event, Object... data)
	{
		if (event == null || event.equals(""))
			return;
		event = event.toLowerCase();
		synchronized(listeners)
		{
			ListIterator<T> iterator = listeners.listIterator();
			while(iterator.hasNext())
				mapEvents(event, iterator.next(), data);
		}
	}
	
	/**
	 * Maps event keys to recipient functions on listener objects.
	 * @param event The key of the event to map. Will always be in lowercase, regardless of how it was called.
	 * @param listener The listener object to fire the events on.
	 * @param data Variable-length list of data passed from the initial call to {@link #fireEvent}.
	 */
	protected abstract void mapEvents(String event, T listener, Object... data);
}
