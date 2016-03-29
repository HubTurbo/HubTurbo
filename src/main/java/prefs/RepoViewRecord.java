package prefs;

import java.time.LocalDateTime;

public class RepoViewRecord implements Comparable<RepoViewRecord> {

    private final String repository;
    private final LocalDateTime timestamp;

    public String getRepository() {
        return repository;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public RepoViewRecord(String repository) {
        this.repository = repository;
        this.timestamp = LocalDateTime.now();
    }

    public RepoViewRecord(String repository, LocalDateTime timestamp) {
        this.repository = repository;
        this.timestamp = timestamp;
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RepoViewRecord other = (RepoViewRecord) obj;
        if (repository == null) {
            if (other.repository != null) {
                return false;
            }
        } else if (!repository.equals(other.repository)) {
            return false;
        }
        return true;
    }

}
