package util.events;

public class IssueSelectedEvent extends Event {
	public int id;
	public IssueSelectedEvent(int i) {
		id = i;
	}
}
