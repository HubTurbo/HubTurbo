package ui;

import com.google.gson.Gson;

public class IssuePanelDragData {
	private int columnIndex;
	private int issueIndex;
	
	public IssuePanelDragData(int col, int issue) {
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

	public static IssuePanelDragData deserialise(String json) {
		return (new Gson()).fromJson(json, IssuePanelDragData.class);
	}
}
