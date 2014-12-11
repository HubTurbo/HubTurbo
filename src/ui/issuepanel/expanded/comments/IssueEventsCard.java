package ui.issuepanel.expanded.comments;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import service.TurboIssueEvent;

public class IssueEventsCard extends VBox{
	
	private TurboIssueEvent event;
	
	public IssueEventsCard(TurboIssueEvent item) {
		this.event = item;
		initialiseUIComponents();
	}
	
	private void initialiseUIComponents() {
		getChildren().add(new Label(event.getActor().getLogin()));
	}
}
