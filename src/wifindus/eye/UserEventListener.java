package wifindus.eye;

/**
 * An object which listens for changes in the state of a User object. 
 * @author Mark 'marzer' Gillard
 */
public interface UserEventListener
{
	/**
	 * Event fired when an User is first created.
	 * @param user The new user object.
	 */
	public void userCreated(User user);
}