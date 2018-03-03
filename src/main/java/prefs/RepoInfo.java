package prefs;

import com.google.common.base.Objects;

public class RepoInfo {
    private String id;
    private String alias;

    public RepoInfo(String id) {
        this.id = id;
        this.alias = "";
    }

    public RepoInfo(String id, String alias) {
        this.id = id;
        this.alias = alias;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean hasSameRepoId(RepoInfo repo) {
        return repo.getId().toLowerCase().equals(this.getId().toLowerCase());
    }

    public boolean hasSameAlias(RepoInfo repo) {
        if (this.getAlias() == null || this.getAlias().isEmpty()) {
            return false;
        }
        if (repo.getAlias() == null || repo.getAlias().isEmpty()) {
            return false;
        }
        return repo.getAlias().toLowerCase().equals(this.getAlias().toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepoInfo repoInfo = (RepoInfo) o;
        return Objects.equal(id, repoInfo.id) &&
                Objects.equal(alias, repoInfo.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, alias);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(id);
        sb.append(", ");
        sb.append(alias);
        sb.append("}");
        return sb.toString();
    }
}
