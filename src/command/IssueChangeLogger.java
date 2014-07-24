package command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import service.ServiceManager;
import util.CollectionUtilities;
import model.Model;
import model.TurboIssue;
import model.TurboLabel;
import model.TurboMilestone;
import model.TurboUser;

public class IssueChangeLogger {
	public static final String LABEL_CHANGELOG_HEADER = "[Edited labels]\n";
	public static final String LABEL_EXCLUSIVE_CHANGELOG_FORMAT = "**%1s**: %2s ~> %3s\n";
	protected static final String LABEL_NONEXCLUSIVE_CHANGELOG_FORMAT = "**%1s**: [added: %2s] [removed: %3s]";
	protected static final String DESCRIPTION_CHANGE_LOG = "Edited description. \n"; 
	protected static final String MILESTONE_CHANGE_LOG = "**Milestone changed:** %1s ~> %2s\n";
	protected static final String ASSIGNEE_CHANGE_LOG = "**Assignee changed:** %1s ~> new: %2s\n";
	
	protected static final String ADDITIONAL_COMMENTS_FORMAT = "\n [Remarks] %1s \n";
	
	private static void logChangesInGithub(TurboIssue issue, String changeLog){
		ServiceManager.getInstance().logIssueChanges(issue.getId(), changeLog);
	}
	
	public static String logLabelsChange(Model model, TurboIssue issue, List<TurboLabel> original, List<TurboLabel> edited){
		String changeLog = getLabelsChangeLog(model, original, edited);
		logChangesInGithub(issue, changeLog);
		return changeLog;
	}
	
	public static String getLabelsChangeLog(Model model, List<TurboLabel> original, List<TurboLabel> edited){
		HashMap<String, HashSet<TurboLabel>> changes = CollectionUtilities.getChangesToList(original, edited);
		HashSet<TurboLabel> removed = changes.get(CollectionUtilities.REMOVED_TAG);
		HashSet<TurboLabel> added = changes.get(CollectionUtilities.ADDED_TAG);
		
		HashMap<String,  ArrayList<TurboLabel>> groupedRemoved = TurboLabel.groupLabels(removed, "Ungrouped");
		HashMap<String,  ArrayList<TurboLabel>> groupedAdded = TurboLabel.groupLabels(added, "Ungrouped");
		
		Set<String> removedLabelGrps = groupedRemoved.keySet();
		Set<String> addedLabelGrps = groupedAdded.keySet();
		Set<String> allGroups = new HashSet<>();
		allGroups.addAll(removedLabelGrps);
		allGroups.addAll(addedLabelGrps);
		
		StringBuilder log = new StringBuilder();
		for(String grpName : allGroups){
			 ArrayList<TurboLabel> addedLabs = groupedAdded.get(grpName);
			 ArrayList<TurboLabel> remLabs = groupedRemoved.get(grpName);
			if(model.isExclusiveLabelGroup(grpName)){
				log.append(getExclusiveLabelLog(grpName, addedLabs, remLabs));
			}else{
				log.append(getNonexclusiveLabelLog(grpName, addedLabs, remLabs));
			}
		}
		return log.toString();
	}
	
	public static String getExclusiveLabelLog(String group, ArrayList<TurboLabel> added, ArrayList<TurboLabel> removed){
		TurboLabel removedLabel =  null;
		TurboLabel addedLabel = null;
		if(added != null && added.size() > 0){
			addedLabel = added.get(0);
		}
		if(removed != null && removed.size() > 0){
			removedLabel = removed.get(0);
		}
		return String.format(LABEL_EXCLUSIVE_CHANGELOG_FORMAT, group, getDisplayedLabelName(removedLabel), getDisplayedLabelName(addedLabel));
	}
	
	public static String getNonexclusiveLabelLog(String grp, ArrayList<TurboLabel> added, ArrayList<TurboLabel> removed){
		String addedlist = "";
		String removedlist = "";
		if(added != null){
			addedlist = getLabelPrintoutList(added);
		}
		if(removed != null){
			removedlist = getLabelPrintoutList(removed);
		}
		return String.format(LABEL_NONEXCLUSIVE_CHANGELOG_FORMAT, grp, removedlist, addedlist);
	}
	
	private static String getLabelPrintoutList(ArrayList<TurboLabel> labels){
		StringBuilder printout = new StringBuilder();
		String printoutDelim = ", ";
		for(TurboLabel label : labels){
			printout.append(printoutDelim + getDisplayedLabelName(label));
		}
		return printout.toString().substring(printoutDelim.length());
	}
	
	public static String getDisplayedLabelName(TurboLabel label){
		if(label == null){
			return "<none>";
		}else{
			return label.getListName();
		}
	}
	
	public static String logTitleChange(TurboIssue issue, String original, String edited){
		String changeLog = IssueChangeLogger.getTitleChangeLog(original, edited);
		logChangesInGithub(issue, changeLog);
		return changeLog;
	}
	
	public static String getTitleChangeLog(String original, String edited){
		return "Title edited: [previous: " + original + "] [new: " + edited + "]\n";
	}
	
	public static String logDescriptionChange(TurboIssue issue, String original, String edited){
		String changeLog = IssueChangeLogger.getDescriptionChangeLog(original, edited);
		logChangesInGithub(issue, changeLog);
		return changeLog;
	}
	
	public static String getDescriptionChangeLog(String original, String edited){
		return DESCRIPTION_CHANGE_LOG;
	}
	
	public static String logMilestoneChange(TurboIssue issue, TurboMilestone original, TurboMilestone edited){
		String changeLog = IssueChangeLogger.getMilestoneChangeLog(original, edited);
		logChangesInGithub(issue, changeLog);
		return changeLog;
	}
	
	public static String getMilestoneChangeLog(TurboMilestone original, TurboMilestone edited){
		return String.format(MILESTONE_CHANGE_LOG, getDisplayedMilestoneName(original), getDisplayedMilestoneName(edited));
	}
	
	public static String getDisplayedMilestoneName(TurboMilestone milestone){
		if(milestone == null){
			return "<none>";
		}else{
			return milestone.getTitle();
		}
	}
	
	public static String logAssigneeChange(TurboIssue issue, TurboUser original, TurboUser edited){
		String changeLog = IssueChangeLogger.getAssigneeChangeLog(original, edited);
		logChangesInGithub(issue, changeLog);
		return changeLog;
	}
	
	public static String getAssigneeChangeLog(TurboUser original, TurboUser edited){
		return String.format(ASSIGNEE_CHANGE_LOG, getDisplayedAssigneeName(original), getDisplayedAssigneeName(edited));
	}
	
	public static String getDisplayedAssigneeName(TurboUser user){
		if(user == null){
			return "<none>";
		}else{
			return user.getGithubName();
		}
	}
	
	public static String logParentChange(TurboIssue issue, Integer original, Integer edited){
		String changeLog = getParentChangeLog(original, edited);
		logChangesInGithub(issue, changeLog);
		return changeLog;
	}
	
	public static String getParentChangeLog(Integer original, Integer edited){
		String changeLog;
		if(edited < 0){
			changeLog = String.format("Removed issue parent: %1d\n", original);
		}else if(original > 0){
			changeLog = String.format("Changed Issue parent from %1d to %2d\n", original, edited);
		}else{
			changeLog = String.format("Set Issue parent to %1d\n", edited);
		}
		return changeLog;
	}
}
