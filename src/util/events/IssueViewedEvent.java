package util.events;

public class IssueViewedEvent extends Event {
	public int id;
	public IssueViewedEvent(int i) {
		id = i;
	}
}
