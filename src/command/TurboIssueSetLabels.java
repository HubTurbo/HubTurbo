package command;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Label;

import service.ServiceManager;
import util.CollectionUtilities;
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
		issue.setLabels(newLabels);
		ArrayList<Label> ghLabels = CollectionUtilities.getGithubLabelList(newLabels);
		try {
			service.setLabelsForIssue(issue.getId(), ghLabels);
			if(issue.getOpen() == true){
				service.openIssue(issue.getId());
			}else{
				service.closeIssue(issue.getId());
			}
			isSuccessful = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			issue.setLabels(previousLabels);
			isSuccessful = false;
		}
		return isSuccessful;
	}

	@Override
	public boolean undo() {
		if(isSuccessful){
		//TODO:	
		}
		return true;
	}

}
