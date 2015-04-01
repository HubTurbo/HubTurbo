package service;

import util.Utility;

public enum IssueEventType {
	Closed,
	Reopened,
	Subscribed,
	Merged,
	Referenced,
	Mentioned,
	Assigned,
	Unassigned,
	Labeled,
	Unlabeled,
	Milestoned,
	Demilestoned,
	Renamed,
	Locked,
	Unlocked,
	HeadRefDeleted,
	HeadRefRestored;
	
	public static IssueEventType fromString(String str) {
        return IssueEventType.valueOf(Utility.snakeCaseToCamelCase(str));
	}
}
