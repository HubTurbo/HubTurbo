package backend.resource;


import backend.resource.serialization.SerializableLabel;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

import org.eclipse.egit.github.core.Label;
import util.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class TurboLabel implements Comparable<TurboLabel> {

    public static final String EXCLUSIVE_DELIMITER = ".";
    public static final String NONEXCLUSIVE_DELIMITER = "-";

    private void ______SERIALIZED_FIELDS______() {
    }

    private final String actualName;
    private final String colour;

    private void ______TRANSIENT_FIELDS______() {
    }

    private final String repoId;

    private void ______CONSTRUCTORS______() {
    }

    public TurboLabel(String repoId, String name) {
        this.actualName = name;
        this.colour = "ffffff";
        this.repoId = repoId;
    }

    public TurboLabel(String repoId, String colour, String name) {
        this.actualName = name;
        this.colour = colour;
        this.repoId = repoId;
    }

    public static TurboLabel nonexclusive(String repoId, String group, String name) {
        return new TurboLabel(repoId, joinWith(group, name, false));
    }

    public static TurboLabel exclusive(String repoId, String group, String name) {
        return new TurboLabel(repoId, joinWith(group, name, true));
    }

    /**
     * Copy constructor
     */
    public TurboLabel(TurboLabel label) {
        this.actualName = label.getActualName();
        this.colour = label.getColour();
        this.repoId = label.getRepoId();
    }

    public TurboLabel(String repoId, Label label) {
        this.actualName = label.getName();
        this.colour = label.getColor();
        this.repoId = repoId;
    }

    public TurboLabel(String repoId, SerializableLabel label) {
        this.actualName = label.getActualName();
        this.colour = label.getColour();
        this.repoId = repoId;
    }

    private void ______METHODS______() {
    }

    /**
     * Extracts delimiters from a label that belongs to a group to classify 
     * a label as exclusive or not.
     * @param name
     * @return
     */
    public static Optional<String> getDelimiter(String name) {

        // Escaping due to constants not being valid regexes
        Pattern p = Pattern.compile(String.format("^[^\\%s\\%s]+(\\%s|\\%s)",
            EXCLUSIVE_DELIMITER,
            NONEXCLUSIVE_DELIMITER,
            EXCLUSIVE_DELIMITER,
            NONEXCLUSIVE_DELIMITER));
        Matcher m = p.matcher(name);

        if (m.find()) {
            return Optional.of(m.group(1));
        } else {
            return Optional.empty();
        }
    }

    private static String joinWith(String group, String name, boolean exclusive) {
        return group + (exclusive ? EXCLUSIVE_DELIMITER : NONEXCLUSIVE_DELIMITER) + name;
    }

    public boolean isExclusive() {
        return getDelimiter(actualName).isPresent() && getDelimiter(actualName).get().equals(EXCLUSIVE_DELIMITER);
    }

    public boolean hasGroup() {
        return getGroup().isPresent();
    }

    public Optional<String> getGroup() {
        if (getDelimiter(actualName).isPresent()) {
            String delimiter = getDelimiter(actualName).get();
            // Escaping due to constants not being valid regexes
            String[] segments = actualName.split("\\" + delimiter);
            assert segments.length >= 1;
            if (segments.length == 1) {
                if (actualName.endsWith(delimiter)) {
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
        if (getDelimiter(actualName).isPresent()) {
            String delimiter = getDelimiter(actualName).get();
            // Escaping due to constants not being valid regexes
            String[] segments = actualName.split("\\" + delimiter);
            assert segments.length >= 1;
            if (segments.length == 1) {
                if (actualName.endsWith(delimiter)) {
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
            return actualName;
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
        return actualName;
    }

    private void ______BOILERPLATE______() {
    }

    public String getRepoId() {
        return repoId;
    }
    public String getColour() {
        return colour;
    }
    public String getActualName() {
        return actualName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TurboLabel that = (TurboLabel) o;
        return actualName.equals(that.actualName) && colour.equals(that.colour);
    }

    @Override
    public int hashCode() {
        int result = actualName.hashCode();
        result = 31 * result + colour.hashCode();
        return result;
    }

    @Override
    public int compareTo(TurboLabel o) {
        return actualName.compareTo(o.getActualName());
    }

    /**
     * Returns the TurboLabel in labels that matches labelName
     * Assumption: the labelName matches exactly 1 TurboLabel
     *
     * @param labels
     * @param labelName
     * @return
     */
    public static TurboLabel getMatchingTurboLabel(List<TurboLabel> labels, String labelName) {
        assert labels.stream()
                .filter(label -> label.getActualName().equals(labelName))
                .findFirst()
                .isPresent();
        return labels.stream()
                .filter(label -> label.getActualName().equals(labelName))
                .findFirst()
                .get();
    }

    public static List<String> getLabelsNameList(List<TurboLabel> labels) {
        return labels.stream()
                .map(TurboLabel::getActualName)
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of labels which name contains labelName
     * @param repoLabels
     * @param labelName
     * @return
     */
    public static List<String> filterByPartialName(List<String> repoLabels, String labelName) {
        return repoLabels
                .stream()
                .filter(name -> Utility.containsIgnoreCase(getName(name), labelName))
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of labels which group contains labelGroup.
     * Uses partial keyword matching and keyword is case-insensitive
     * @param repoLabels
     * @param labelGroup
     * @return
     */
    public static List<String> filterByPartialGroupName(List<String> repoLabels, String labelGroup) {
        if (labelGroup.isEmpty()) return repoLabels;

        return repoLabels
                .stream()
                .filter(name -> {
                    if (hasGroup(name)) {
                        return Utility.containsIgnoreCase(getGroup(name), labelGroup);
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns the first label that matches the keyword
     * i.e. the label's group contains keyword's group and label's name contains keyword's name
     * @param repoLabels
     * @param keyword
     * @return
     */
    public static String getMatchedLabelName(List<String> repoLabels, String keyword) {
        List<String> newMatchedLabels = new ArrayList<>();
        newMatchedLabels.addAll(repoLabels);
        newMatchedLabels = filterByPartialName(newMatchedLabels, getName(keyword));
        newMatchedLabels = filterByPartialGroupName(newMatchedLabels, getGroup(keyword));
        return newMatchedLabels.get(0);
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
    public static boolean hasExactlyOneMatchedLabel(List<String> repoLabels, String keyword) {
        List<String> newMatchedLabels = new ArrayList<>();
        newMatchedLabels.addAll(repoLabels);
        newMatchedLabels = filterByPartialName(newMatchedLabels, getName(keyword));
        newMatchedLabels = filterByPartialGroupName(newMatchedLabels, getGroup(keyword));
        return newMatchedLabels.size() == 1;
    }

    /**
     * Identifies if a label belongs to an exclusive group. Only one label
     * from an exclusive group is allowed to be assigned to an issue
     * @param labelName
     * @return
     */
    public static boolean hasExclusiveGroup(String labelName) {
        return TurboLabel.getDelimiter(labelName).isPresent() && TurboLabel.getDelimiter(labelName).get().equals(".");
    }

    /**
     * Determines the group that labelName belongs to
     * A labelName is considered to be in a group if getDelimiter(labelName).isPresent() is true.
     * @param labelName
     * @return
     */
    public static String getGroup(String labelName) {
        if (!hasGroup(labelName)) return "";
        return labelName.substring(0, labelName.indexOf(TurboLabel.getDelimiter(labelName).get()));
    }

    /**
     * Returns the name of labelName after removing its group
     * @param labelName
     * @return
     */
    public static String getName(String labelName) {
        if (!hasGroup(labelName)) return labelName;
        return labelName.substring(labelName.indexOf(TurboLabel.getDelimiter(labelName).get()) + 1);
    }

    public static boolean hasGroup(String name) {
        return TurboLabel.getDelimiter(name).isPresent();
    }
}
