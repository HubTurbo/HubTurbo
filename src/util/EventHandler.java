package util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An exceedingly simple event dispatcher. Allows events to be registered and
 * subscribers to be notified when they are triggered.
 * 
 * Serves a different purpose from the similarly-named EventHandler interface in JavaFX:
 * that is a functional interface for an event callback with a specific signature
 * (? extends Event).
 * 
 * See util.Event for more details on this.
 */
public class EventHandler {

	private HashMap<Event, ArrayList<Runnable>> events;
	
	public EventHandler() {
		events = new HashMap<>();
	}
	
	public void on(Event type, Runnable callback) {
		if (!events.containsKey(type)) {
			events.put(type, new ArrayList<>());
		}
		events.get(type).add(callback);
	}

	public void trigger(Event type) {
		if (events.containsKey(type)) {
			for (Runnable r : events.get(type)) {
				r.run();
			}
		}
	}
}
