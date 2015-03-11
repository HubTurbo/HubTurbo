package util.events;

public class FilterFieldClickedEvent extends Event {
	public int columnIndex;
	
	public FilterFieldClickedEvent(int columnIndex) {
		this.columnIndex = columnIndex;
	}
}
