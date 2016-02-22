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

    public static enum Grouping { EXCLUSIVE, NON_EXCLUSIVE, NONE }

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
        this.grouping = initGroupMembership();
        this.shortName = extractShortName(name);
        this.groupName = extractGroupName(name);
        this.colour = DEFAULT_COLOR;
        this.repoId = repoId;
    }

    public TurboLabel(String repoId, String colour, String name) {
        this(repoId, name);
        this.colour = colour;
    }

    public static TurboLabel nonexclusive(String repoId, String group, String name) {
        return new TurboLabel(repoId, buildFullName(group, name, false));
    }

    public static TurboLabel exclusive(String repoId, String group, String name) {
        return new TurboLabel(repoId, buildFullName(group, name, true));
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
     * Returns the first TurboLabel in labels that matches labelName
     * Assumption: the labelName matches at least 1 TurboLabel
     * @return TurboLabel that matches labelName
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
     * Returns the list of label names that matches a query
     * Matches label name that contains sequence of chars in nameQuery and the matching is
     * case-insensitive
     * @param repoLabels
     * @param nameQuery
     * @return
     */
    public static List<TurboLabel> filterByNameQuery(List<TurboLabel> repoLabels, String nameQuery) {
        return repoLabels
                .stream()
                .filter(label -> Utility.containsIgnoreCase(label.getShortName(), nameQuery))
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of label names that belongs to a group and matches a query
     * Uses partial matching and matching is case-insensitive
     * @param repoLabels
     * @param groupQuery
     * @return
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
     * Returns the list of labels that matches the keyword
     * i.e. the label's group contains keyword's group and label's name contains keyword's name
     * @param repoLabels
     * @param keyword
     * @return
     */
    public static List<TurboLabel> getMatchedLabels(List<TurboLabel> repoLabels, String keyword) {
        TurboLabel query = new TurboLabel("", keyword);

        List<TurboLabel> newMatchedLabels = new ArrayList<>();
        newMatchedLabels.addAll(repoLabels);
        newMatchedLabels = filterByNameQuery(newMatchedLabels, query.getShortName());
        newMatchedLabels = filterByGroupQuery(newMatchedLabels, query.getGroupName());
        return newMatchedLabels;
    }

    /**
     * Returns true if there is exactly 1 matching label for keyword
     *
     * A label is matching if:
     * the label's group contains keyword's group and label's name contains keyword's name
     * @param repoLabels
     * @param keyword
     * @return
     */
    public static boolean hasExactlyOneMatchedLabel(List<TurboLabel> repoLabels, String keyword) {
        return getMatchedLabels(repoLabels, keyword).size() == 1;
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

    public Optional<String> getGroup() {
        if (getDelimiter(fullName).isPresent()) {
            String delimiter = getDelimiter(fullName).get();
            // Escaping due to constants not being valid regexes
            String[] segments = fullName.split("\\" + delimiter);
            assert segments.length >= 1;
            if (segments.length == 1) {
                if (fullName.endsWith(delimiter)) {
                    // group.
                    return Optional.of(segments[0]);
                } else {
                    // .name
                    return Optional.empty();
                }
            } else {
                // group.name
                assert segments.length == 2;
                return Optional.of(segments[0]);
            }
        } else {
            // name
            return Optional.empty();
        }
    }

    public String getName() {
        if (getDelimiter(fullName).isPresent()) {
            String delimiter = getDelimiter(fullName).get();
            // Escaping due to constants not being valid regexes
            String[] segments = fullName.split("\\" + delimiter);
            assert segments.length >= 1;
            if (segments.length == 1) {
                if (fullName.endsWith(delimiter)) {
                    // group.
                    return "";
                } else {
                    // .name
                    return segments[0];
                }
            } else {
                // group.name
                assert segments.length == 2;
                return segments[1];
            }
        } else {
            // name
            return fullName;
        }
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
        javafx.scene.control.Label node = new javafx.scene.control.Label(getName());
        node.getStyleClass().add("labels");
        node.setStyle(getStyle());
        if (getGroup().isPresent()) {
            Tooltip groupTooltip = new Tooltip(getGroup().get());
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

    private static String buildFullName(String group, String name, boolean exclusive) {
        return group + (exclusive ? EXCLUSIVE_DELIMITER : NONEXCLUSIVE_DELIMITER) + name;
    }

    private Grouping initGroupMembership() {
        if (!getDelimiter(fullName).isPresent()) return Grouping.NONE;

        return getDelimiter(fullName).get().equals(EXCLUSIVE_DELIMITER) 
                ? Grouping.EXCLUSIVE : Grouping.NON_EXCLUSIVE;
    }

    /**
     * Determines and returns the group that labelName belongs to
     * A labelName is considered to be in a group if getDelimiter(labelName).isPresent() is true.
     * @return
     */
    private String extractGroupName(String fullName) {
        if (!isInGroup()) return "";
        return fullName.substring(0, fullName.indexOf(getDelimiter(fullName).get()));
    }

    /**
     * Returns the name of labelName after omitting its group name
     * i.e "priority.high" will return "high"
     * @return
     */
    private String extractShortName(String fullName) {
        if (!isInGroup()) return fullName;
        return fullName.substring(fullName.indexOf(getDelimiter(fullName).get()) + 1);
    }

}
