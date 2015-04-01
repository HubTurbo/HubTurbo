package util.events;

public class IssueSelectedEvent extends Event {
	public int id;
	public int columnIndex;
	
	public IssueSelectedEvent(int id, int columnIndex) {
		this.id = id;
		this.columnIndex = columnIndex;
	}
}
