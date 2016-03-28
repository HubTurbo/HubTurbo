package util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GithubPageElements {
    // Web Element Names
    public static final String BODY = "body";
    public static final String NEW_COMMENT = "new_comment_field";
    public static final String LOGIN_FIELD = "login";
    public static final String PASSWORD_FIELD = "password";

    // PR tab names
    public static final String DISCUSSION_TAB = "discussion";
    public static final String COMMITS_TAB = "commits";
    public static final String FILES_TAB = "files";

    //Javascript
    public static final String SCROLL_TO_TOP = "window.scrollTo(0, 0)";
    public static final String SCROLL_TO_BOTTOM = "window.scrollTo(0, document.body.scrollHeight)";
    public static final String SCROLL_UP = "window.scrollBy(0, -100)";
    public static final String SCROLL_DOWN = "window.scrollBy(0, 100)";

    //Extract Issue Number from issue description
    public static final Pattern ISSUE_NUMBER_PATTERN = Pattern.compile(
            "(close|closes|closed|fix|fixes|fixed|resolve|resolves|resolved) #(\\d+)",
            Pattern.CASE_INSENSITIVE);

    public static Optional<Integer> extractIssueNumber(String text) {
        Matcher m = ISSUE_NUMBER_PATTERN.matcher(text);
        if (m.find()) {
            return Optional.of(Integer.parseInt(m.group(2)));
        }
        return Optional.empty();
    }

    private GithubPageElements() {
    }
}
