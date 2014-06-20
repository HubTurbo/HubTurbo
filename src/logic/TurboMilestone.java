package logic;

import org.eclipse.egit.github.core.Milestone;

public class TurboMilestone implements Listable {
	private Milestone ghMilestone;
	private String title;
	
	public TurboMilestone(Milestone milestone) {
		assert milestone != null;

		this.ghMilestone = milestone;
		this.title = milestone.getTitle();
	}
	
	public Milestone getGhMilestone() {
		return ghMilestone;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	@Override
	public String getListName() {
		return getTitle();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		TurboMilestone other = (TurboMilestone) obj;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
	
	
	
}
