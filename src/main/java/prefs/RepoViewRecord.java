package prefs;

import java.time.LocalDateTime;

class RepoViewRecord implements Comparable<RepoViewRecord>{
	
	private String repository;

	public String getRepository() {
		return repository;
	}

	private LocalDateTime timestamp;

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime lastViewedTimestamp) {
		this.timestamp = lastViewedTimestamp;
	}

	public RepoViewRecord(String repository) {
		this.repository = repository;
		this.timestamp = LocalDateTime.now();
	}
	
	@Override
	public int compareTo(RepoViewRecord other) {
		return this.timestamp.compareTo(other.timestamp);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((repository == null) ? 0 : repository.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RepoViewRecord other = (RepoViewRecord) obj;
		if (repository == null) {
			if (other.repository != null)
				return false;
		} else if (!repository.equals(other.repository))
			return false;
		return true;
	}

}
