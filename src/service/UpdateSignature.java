package service;

import java.util.Date;

/**
 * An immutable class for aggregating resource ETags and last check time.
 * Characterises an
 */
public class UpdateSignature {

	public final String issuesETag;
	public final String labelsETag;
	public final String milestonesETag;
	public final String collaboratorsETag;
	public final Date lastCheckTime;

	public UpdateSignature() {
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
}
