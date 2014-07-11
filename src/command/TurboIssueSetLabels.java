package command;

import java.lang.ref.WeakReference;
import java.util.List;

import service.ServiceManager;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;

public class TurboIssueSetLabels extends TurboIssueCommand{
	private List<TurboLabel> previousLabels;
	private List<TurboLabel> newLabels;
	
	public TurboIssueSetLabels(Model model, TurboIssue issue, List<TurboLabel> labels){
		this.issue = issue;
		this.model = new WeakReference<Model>(model);
		this.newLabels = labels;
	}
	
	@Override
	public boolean execute() {
		ServiceManager service = ServiceManager.getInstance();
		this.previousLabels = issue.getLabels(); //Is a copy of original list of labels
		
		//TODO:
		return false;
	}

	@Override
	public boolean undo() {
		if(isSuccessful){
		//TODO:	
		}
		return true;
	}

}
