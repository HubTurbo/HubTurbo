package util.events;

public interface EventDispatcher {
	/**
	 * Publish/subscribe API making use of Guava's EventBus.
	 * Takes a lambda expression to be called upon an event being fired.
	 */
	public void registerEvent(EventHandler handler);

	/**
	 * Publish/subscribe API making use of Guava's EventBus.
	 * Triggers all events of a certain type. EventBus will ensure that the
	 * event is fired for all subscribers whose parameter is either the same
	 * or a super type.
	 */
	public <T extends Event> void triggerEvent(T event);
}
