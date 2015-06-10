package util.events;

public class ColumnClickedEvent extends Event {
	public int columnIndex;
	
	public ColumnClickedEvent(int columnIndex) {
		this.columnIndex = columnIndex;
	}
}
