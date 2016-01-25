
package filter.expression;

import java.util.*;
import java.util.stream.Collectors;

public enum QualifierType {

    EMPTY, // The empty qualifier, which always returns true
    FALSE, // The false qualifier, which always returns false

    KEYWORD, // A special value that indicates a search term

    ASSIGNEE, AUTHOR, CREATED, COUNT, DATE, DESCRIPTION, HAS, ID, IN,
    INVOLVES, IS, LABEL, LABELS, MILESTONE, MILESTONES, NO,
    REPO, SORT, STATE, TITLE, TYPE, UPDATED;

    private static final Map<String, QualifierType> ALIASES = initialiseAliases();
    private static final Map<QualifierType, String> VALID_INPUTS = initialiseValidInputs();

    private static Map<String, QualifierType> initialiseAliases() {
        Map<String, QualifierType> aliases = new HashMap<>();
        aliases.put("m", MILESTONE);
        aliases.put("as", ASSIGNEE);
        aliases.put("au", AUTHOR);
        aliases.put("s", STATE);
        aliases.put("creator", AUTHOR);
        aliases.put("user", INVOLVES);
        aliases.put("status", STATE);
        aliases.put("body", DESCRIPTION);
        aliases.put("desc", DESCRIPTION);
        return Collections.unmodifiableMap(aliases);
    }

    // Ignore Qualifiers that expects a string where any user input would be valid
    private static Map<QualifierType, String> initialiseValidInputs() {
        Map<QualifierType, String> validInputs = new HashMap<>();
        validInputs.put(ID, "a number or a number range");
        validInputs.put(UPDATED, "a number or  a number range");
        validInputs.put(STATE, "\"open\" or \"closed\"");
        validInputs.put(HAS, "\"label\", \"milestone\", or \"assignee\"");
        validInputs.put(NO, "\"label\", \"milestone\", or \"assignee\"");
        validInputs.put(IN, "\"title\" or \"body\"");
        validInputs.put(TYPE, "\"issue\" or \"pr\"");
        validInputs.put(CREATED, "a date or a date range");
        validInputs.put(REPO, "repo id");
        
        //Show only most common inputs to safe space
        validInputs.put(IS, "\"open\", \"closed\", \"pr\", \"issue\", \"merged\","
                + " \"read\", \"unread\", or \"unmerged\"");
        validInputs.put(SORT, "\"comments\", \"repo\", \"updated\", \"id\", "
                + " \"assignee\", \"label\", \"state\", or \"milestone\"");
        return Collections.unmodifiableMap(validInputs);
    }

    /**
     * Returns a list of keywords which may be completed when a prefix is typed in
     * the filter field. Defaults to the string forms of all qualifier types. Exceptions
     * may be added by modifying the collection before it is returned, for example, to
     * include additional keywords or remove conflicting ones.
     * @return a set of keywords to be used for completion
     */
    public static Set<String> getCompletionKeywords() {
        Set<String> defaultCompletions =
            new ArrayList<>(Arrays.asList(QualifierType.values())).stream()
                .map(QualifierType::name)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        defaultCompletions.removeAll(Arrays.asList("false", "empty", "keyword"));

        defaultCompletions.addAll(ALIASES.keySet());
        defaultCompletions.removeAll(Arrays.asList("m", "as", "au", "s"));

        defaultCompletions.addAll(Arrays.asList("closed", "open", "issue", "pr",
            "pullrequest", "read", "unread",  "merged", "unmerged", "comments",
            "nonSelfUpdate"));

        return Collections.unmodifiableSet(defaultCompletions);
    }

    public static Optional<QualifierType> parse(String input) {
        if (input == null || input.isEmpty()) {
            return Optional.empty();
        }

        String toBeParsed = input.trim().toLowerCase();

        if (isSpecialQualifier(toBeParsed)) {
            return Optional.empty();
        }

        try {
            return Optional.of(QualifierType.valueOf(toBeParsed.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return resolveAlias(toBeParsed);
        }
    }

    private static boolean isSpecialQualifier(String input) {
        switch (input) {
            case "empty":
            case "keyword":
            case "false":
                return true;
            default:
                return false;
        }
    }

    private static Optional<QualifierType> resolveAlias(String input) {
        return Optional.ofNullable(ALIASES.get(input));
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
    
    public String getValidInputs() {
        String defaultExpectedInput = "string";
        if (VALID_INPUTS.containsKey(this)) return VALID_INPUTS.get(this);
        return defaultExpectedInput;

    }
}
