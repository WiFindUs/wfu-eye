package wifindus;

/**
 * An object that is subscribed to a high-frequency, high-resolution timer event.
 * @author Mark 'marzer' Gillard
 */
public interface HighResolutionTimerListener
{
	/**
	 * Event fired each time the timer 'ticks' over an interval.
	 * @param deltaTime The time, in seconds, between now and the previous firing of the event.
	 */
	void timerTick(double deltaTime);
}
