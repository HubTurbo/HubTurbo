package view;

import javafx.beans.value.ChangeListener;
import javafx.stage.Stage;
import model.Model;

public class IssueCardView {
	public Stage mainStage;
	public Model model;
	public int parentColumnIndex;
	public ChangeListener<String> titleChangeListener;

	public IssueCardView() {
	}
}