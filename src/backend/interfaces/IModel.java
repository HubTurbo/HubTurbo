package backend.interfaces;

import backend.resource.TurboIssue;

import java.util.List;

public interface IModel {
	public List<TurboIssue> getIssues();

	public default String summarise() {
		return getIssues().size() + " issues";
	}
}
