package backend.resource;


import backend.resource.serialization.SerializableLabel;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

import org.eclipse.egit.github.core.Label;

import util.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TurboLabel implements Comparable<TurboLabel> {

    public enum Grouping { EXCLUSIVE, NON_EXCLUSIVE, NONE }

    public static final String DEFAULT_COLOR = "ffffff";
    public static final String EXCLUSIVE_DELIMITER = ".";
    public static final String NONEXCLUSIVE_DELIMITER = "-";

    private final String fullName;
    private final String shortName;
    private final String groupName;
    private final Grouping grouping;

    private final String repoId;

    @SuppressWarnings("PMD")
    private String colour;

    public TurboLabel(String repoId, String name) {
        this.fullName = name;
        this.grouping = determineGrouping();
        this.shortName = extractShortName(name);
        this.groupName = extractGroupName(name);
        this.colour = DEFAULT_COLOR;
        this.repoId = repoId;
    }

    public TurboLabel(String repoId, String colour, String name) {
        this(repoId, name);
        this.colour = colour;
    }

    /**
     * Copy constructor
     */
    public TurboLabel(TurboLabel label) {
        this(label.getRepoId(), label.getFullName());
        this.colour = label.getColour();
    }

    public TurboLabel(String repoId, Label label) {
        this(repoId, label.getName());
        this.colour = label.getColor();
    }

    public TurboLabel(String repoId, SerializableLabel label) {
        this(repoId, label.getFullName());
        this.colour = label.getColour();
    }


    /**
     * Extracts delimiters from a label that belongs to a group to classify
     * a label as exclusive or not. Only extracts first matching delimiter 
     * i.e "priority.high-low" will return "."
     * @param labelName
     * @return
     */
    public static Optional<String> getDelimiter(String labelName) {
        // Escaping due to constants not being valid regexes
        Pattern p = Pattern.compile(String.format("^[^\\%s\\%s]+(\\%s|\\%s)",
            EXCLUSIVE_DELIMITER,
            NONEXCLUSIVE_DELIMITER,
            EXCLUSIVE_DELIMITER,
            NONEXCLUSIVE_DELIMITER));
        Matcher m = p.matcher(labelName);

        if (m.find()) {
            return Optional.of(m.group(1));
        } else {
            return Optional.empty();
        }
    }


    /**
     * Assumption: the labelName matches at least 1 TurboLabel
     * @return the first TurboLabel in labels that matches labelName
     */
    public static TurboLabel getFirstMatchingTurboLabel(List<TurboLabel> labels, String labelName) {
        Optional<TurboLabel> firstMatchingLabel = getMatchedLabels(labels, labelName).stream().findFirst();
        assert firstMatchingLabel.isPresent();
        return firstMatchingLabel.get();
    }

    public static List<String> getLabelsNameList(List<TurboLabel> labels) {
        return labels.stream()
                .map(TurboLabel::getFullName)
                .collect(Collectors.toList());
    }

    /**
     * Matches label name that contains nameQuery and the matching is
     * case-insensitive
     * @param repoLabels
     * @param nameQuery
     * @return the list of label names that matches a query
     */
    public static List<TurboLabel> filterByNameQuery(List<TurboLabel> repoLabels, String nameQuery) {
        return repoLabels
                .stream()
                .filter(label -> Utility.containsIgnoreCase(label.getShortName(), nameQuery))
                .collect(Collectors.toList());
    }

    /**
     * Matches label name that contains groupQuery and the matching is
     * case-insensitive
     * @param repoLabels
     * @param groupQuery
     * @return the list of label names that belongs to a group and matches a query
     */
    public static List<TurboLabel> filterByGroupQuery(List<TurboLabel> repoLabels, String groupQuery) {
        if (groupQuery.isEmpty()) return repoLabels;

        return repoLabels
                .stream()
                .filter(label -> {
                    if (label.isInGroup()) {
                        return Utility.containsIgnoreCase(label.getGroupName(), groupQuery);
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * @param repoLabels
     * @param keyword
     * @return the list of labels that matches the keyword i.e. the label's group 
     * contains keyword's group and label's name contains keyword's name
     */
    public static List<TurboLabel> getMatchedLabels(List<TurboLabel> repoLabels, String keyword) {
        String[] extractedNames = extractNames(keyword);

        List<TurboLabel> newMatchedLabels = new ArrayList<>();
        newMatchedLabels.addAll(repoLabels);
        newMatchedLabels = filterByNameQuery(newMatchedLabels, extractedNames[1]);
        newMatchedLabels = filterByGroupQuery(newMatchedLabels, extractedNames[0]);
        return newMatchedLabels;
    }

    /**
     * A label is matching if:
     * the label's group contains keyword's group and label's name contains keyword's name
     * @param repoLabels
     * @param keyword
     * @return true if there is there is at least 1 matching label for the keyword
     */
    public static boolean hasMatchedLabel(List<TurboLabel> repoLabels, String keyword) {
        return !getMatchedLabels(repoLabels, keyword).isEmpty();
    }

    public String getGroupName() {
        return groupName;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean isInExclusiveGroup() {
        return grouping == Grouping.EXCLUSIVE;
    }

    public final boolean isInGroup() {
        return grouping != Grouping.NONE;
    }

    public String getStyle() {
        String colour = getColour();
        int r = Integer.parseInt(colour.substring(0, 2), 16);
        int g = Integer.parseInt(colour.substring(2, 4), 16);
        int b = Integer.parseInt(colour.substring(4, 6), 16);
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        boolean bright = luminance > 128;
        return "-fx-background-color: #" + getColour() + "; -fx-text-fill: " + (bright ? "black;" : "white;");
    }

    public Node getNode() {
        javafx.scene.control.Label node = new javafx.scene.control.Label(shortName);
        node.getStyleClass().add("labels");
        node.setStyle(getStyle());
        if (isInGroup()) {
            Tooltip groupTooltip = new Tooltip(groupName);
            node.setTooltip(groupTooltip);
        }
        return node;
    }

    @Override
    public String toString() {
        return fullName;
    }

    public String getRepoId() {
        return repoId;
    }

    public String getColour() {
        return colour;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TurboLabel that = (TurboLabel) o;
        return fullName.equals(that.fullName) && colour.equals(that.colour);
    }

    @Override
    public int hashCode() {
        int result = fullName.hashCode();
        result = 31 * result + colour.hashCode();
        return result;
    }

    @Override
    public int compareTo(TurboLabel o) {
        return fullName.compareTo(o.getFullName());
    }

    private Grouping determineGrouping() {
        if (!getDelimiter(fullName).isPresent()) return Grouping.NONE;

        return getDelimiter(fullName).get().equals(EXCLUSIVE_DELIMITER) 
                ? Grouping.EXCLUSIVE : Grouping.NON_EXCLUSIVE;
    }

    /**
     * Extracts shortName and groupName from a keyword
     * groupName is name of the group associated with a keyword
     * shortName is name of the keyword after omitting its group name 
     * Example: "priority.high" -> groupName = "priority", shortName = "high"
     * @param keyword
     * @return shortName and groupName of a given keyword
     */
    public static String[] extractNames(String keyword) {
        if (!getDelimiter(keyword).isPresent()) return new String[] {"", keyword};

        int delimiterIndex = keyword.indexOf(getDelimiter(keyword).get());
        String shortName = keyword.substring(delimiterIndex + 1);
        String groupName = keyword.substring(0, delimiterIndex);
        return new String[] {groupName, shortName};
    }

    /**
     * Extracts the group name from the fullName
     * Returns empty string if no group name is extracted
     * @param fullName
     * @return 
     */
    private String extractGroupName(String fullName) {
        return extractNames(fullName)[0];
    }

    /**
     * @param fullName
     * @return the name of fullName after omitting its group name
     * i.e "priority.high" will return "high"
     */
    private String extractShortName(String fullName) {
        return extractNames(fullName)[1];
    }
}
