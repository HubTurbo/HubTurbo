package filter;

import model.TurboIssue;

public class Predicate implements Expression {
	private String name;
	private String content;

	public Predicate(String name, String content) {
		this.name = name;
		this.content = content;
	}

	@Override
	public String toString() {
		return name + "(" + content + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Predicate other = (Predicate) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public boolean isSatisfiedBy(TurboIssue issue) {
		switch (name) {
		case "title":
			return issue.getTitle().contains(content);
		default:
			return false;
		}
	}
}