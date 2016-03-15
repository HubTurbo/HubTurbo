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
    public static final String GROUP_PATTERN = String.format(
        "^([^\\%1$s\\%2$s]+)(\\%1$s|\\%2$s)([^\\%1$s\\%2$s]*)", EXCLUSIVE_DELIMITER, NONEXCLUSIVE_DELIMITER);

    private final String fullName;
    private final String shortName;
    private final String groupName;
    private final Grouping grouping;

    private final String colour;
    private final String repoId;


    public TurboLabel(String repoId, String colour, String name) {
        this.fullName = name;

        String[] splitted = splitKeyword(name);
        this.groupName = splitted[0];
        this.grouping = determineGrouping(splitted[1]);
        this.shortName = splitted[2];

        this.colour = colour;
        this.repoId = repoId;
    }

    public TurboLabel(String repoId, String name) {
        this(repoId, DEFAULT_COLOR, name);
    }

    /**
     * Copy constructor
     */
    public TurboLabel(TurboLabel label) {
        this(label.getRepoId(), label.getColour(), label.getFullName());
    }

    public TurboLabel(String repoId, Label label) {
        this(repoId, label.getColor(), label.getName());
    }

    public TurboLabel(String repoId, SerializableLabel label) {
        this(repoId, label.getColour(), label.getFullName());
    }


    /**
     * Extracts group name, group delimiter and short name from a keyword
     * group name is name of the group associated with a keyword
     * short name is name of the keyword after omitting its group name and group delimiter
     * Example: "priority.high" -> group name = "priority", group delimiter = ".", short name = "high"
     * @param keyword
     * @return String array of group name, group delimiter and short name
     */
    private static String[] splitKeyword(String keyword) {
        Pattern p = Pattern.compile(GROUP_PATTERN);
        Matcher m = p.matcher(keyword);

        if (!m.find()) return new String[] {"", "", keyword};
            
        return new String[] {m.group(1), m.group(2), m.group(3)};

    }

    /**
     * Assumption: the labelName matches at least 1 TurboLabel
     * @return the first TurboLabel in labels that matches labelName
     */
    public static final TurboLabel getFirstMatchingTurboLabel(List<TurboLabel> labels, String labelName) {
        Optional<TurboLabel> firstMatchingLabel = getMatchedLabels(labels, labelName).stream().findFirst();
        assert firstMatchingLabel.isPresent();
        return firstMatchingLabel.get();
    }


    public static List<String> getLabelNames(List<TurboLabel> labels) {
        return labels.stream()
                .map(TurboLabel::getFullName)
                .collect(Collectors.toList());
    }

    /**
     * Matches label name that contains nameQuery and the matching is case-insensitive
     * @param allLabels
     * @param nameQuery
     * @return the list of label names that matches a query
     */
    public static List<TurboLabel> filterByNameQuery(List<TurboLabel> allLabels, String nameQuery) {
        return allLabels
                .stream()
                .filter(label -> Utility.containsIgnoreCase(label.getShortName(), nameQuery))
                .collect(Collectors.toList());
    }

    /**
     * Matches label name that contains groupQuery and the matching is
     * case-insensitive
     * @param allLabels
     * @param groupQuery
     * @return the list of label names that belongs to a group and matches a query
     */
    public static List<TurboLabel> filterByGroupQuery(List<TurboLabel> allLabels, String groupQuery) {
        if (groupQuery.isEmpty()) return allLabels;

        return allLabels
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
     * @param allLabels
     * @param keyword
     * @return the list of labels that matches the keyword i.e. the label's group 
     * contains keyword's group and label's name contains keyword's name
     */
    public static List<TurboLabel> getMatchedLabels(List<TurboLabel> allLabels, String keyword) {
        String[] extractedNames = splitKeyword(keyword);

        List<TurboLabel> newMatchedLabels = new ArrayList<>();
        newMatchedLabels.addAll(allLabels);
        newMatchedLabels = filterByNameQuery(newMatchedLabels, extractedNames[2]);
        newMatchedLabels = filterByGroupQuery(newMatchedLabels, extractedNames[0]);
        return newMatchedLabels;
    }

    /**
     * @param allLabels
     * @param labelNames
     * @return list of labels in allLabels that matches list of label names
     */
    public static List<TurboLabel> getMatchedLabels(List<TurboLabel> allLabels, List<String> labelNames) {
        return allLabels.stream()
            .filter(label -> labelNames.contains(label.getFullName()))
            .collect(Collectors.toList());
    }

    /**
     * @return group name if the label belongs to a group. Empty string if otherwise.
     * Optional is not returned to make standardise handling of names consistent with short name 
     * which always return a String
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @return short name if the label belongs to a group. 
     * full name will be returned for label without a group 
     */
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

    private Grouping determineGrouping(String delimiter) {
        switch (delimiter) {
        case EXCLUSIVE_DELIMITER:
            return Grouping.EXCLUSIVE;
        case NONEXCLUSIVE_DELIMITER:
            return Grouping.NON_EXCLUSIVE;
        case "":
            return Grouping.NONE;
        default:
            assert false : "Unsupported delimiter found";
            // should not reach here
            return Grouping.NONE;
        }
    }
}
