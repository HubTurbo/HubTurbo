package ui;

import com.google.gson.Gson;

public class DragData {
	public enum Source {
		ISSUE_CARD, COLUMN
	}
	private Source source;
	private int columnIndex;
	private int issueIndex;
	
	public DragData(Source source, int col, int issue) {
		this.setSource(source);
		this.columnIndex = col;
		this.issueIndex = issue;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public int getIssueIndex() {
		return issueIndex;
	}

	public void setIssueIndex(int issueIndex) {
		this.issueIndex = issueIndex;
	}
	
	public String serialise() {
		return (new Gson()).toJson(this);
	}

	public static DragData deserialise(String json) {
		return (new Gson()).fromJson(json, DragData.class);
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}
}
