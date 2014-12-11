package service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Pattern p = Pattern.compile("(^|_)([a-z])" );
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, m.group(2).toUpperCase());
        }
        m.appendTail(sb);
        return IssueEventType.valueOf(sb.toString());
	}
}
