package backend;

import java.util.Date;

/**
 * Aggregation of resource ETags and last-check time.
 * Characterises the state of a Model after the last update that occurred.
 */
public class UpdateSignature {

    public static final UpdateSignature EMPTY = new UpdateSignature();

    public final String issuesETag;
    public final String labelsETag;
    public final String milestonesETag;
    public final String collaboratorsETag;
    public final Date lastCheckTime;

    private UpdateSignature() {
        issuesETag = null;
        labelsETag = null;
        milestonesETag = null;
        collaboratorsETag = null;

        // This initialisation is a reasonable default
        lastCheckTime = new Date();
    }

    public UpdateSignature(String issuesETag, String labelsETag, String milestonesETag, String collaboratorsETag,
            Date lastCheckTime) {

        this.issuesETag = issuesETag;
        this.labelsETag = labelsETag;
        this.milestonesETag = milestonesETag;
        this.collaboratorsETag = collaboratorsETag;
        this.lastCheckTime = new Date(lastCheckTime.getTime());
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    /**
     * lastCheckTime does not contribute to equality of signatures.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateSignature that = (UpdateSignature) o;
        return !(collaboratorsETag != null ?
                !collaboratorsETag.equals(that.collaboratorsETag) : that.collaboratorsETag != null) &&
                !(issuesETag != null ? !issuesETag.equals(that.issuesETag) : that.issuesETag != null) &&
                !(labelsETag != null ? !labelsETag.equals(that.labelsETag) : that.labelsETag != null) &&
                !(milestonesETag != null ? !milestonesETag.equals(that.milestonesETag) : that.milestonesETag != null);
    }

    @Override
    public int hashCode() {
        int result = issuesETag != null ? issuesETag.hashCode() : 0;
        result = 31 * result + (labelsETag != null ? labelsETag.hashCode() : 0);
        result = 31 * result + (milestonesETag != null ? milestonesETag.hashCode() : 0);
        result = 31 * result + (collaboratorsETag != null ? collaboratorsETag.hashCode() : 0);
        return result;
    }
}
