package backend;

import java.util.Date;

/**
 * Aggregation of resource ETags and last-check time.
 * Characterises the state of a Model after the last update that occurred.
 */
public class UpdateSignature {

	public static final UpdateSignature empty = new UpdateSignature();

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
		this.lastCheckTime = lastCheckTime;
	}

	public boolean isEmpty() {
		return this == empty;
	}
}
