package prefs;

import com.google.common.base.Objects;

public class RepoInfo {
    private String id;
    private String alias;

    public RepoInfo(String id) {
        this.id = id;
        this.alias = "";
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
