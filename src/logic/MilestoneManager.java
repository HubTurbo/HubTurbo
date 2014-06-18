package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.MilestoneService;

public class MilestoneManager {
	private static final String STATE_ALL = "all";
	private static final String STATE_OPEN = "open";
	private static final String STATE_CLOSED = "closed";
	
	private MilestoneService service;
	private GitHubClient client;
	
	MilestoneManager(GitHubClient client) {
		this.client = client;
		service = new MilestoneService(client);
	}
	
	List<TurboMilestone> getAllMilestones(IRepositoryIdProvider repository) {
		List<TurboMilestone> turboMilestones = new ArrayList<TurboMilestone>();
		try {		
			List<Milestone> milestones = service.getMilestones(repository, STATE_ALL);
			for (Milestone milestone : milestones) {
				turboMilestones.add(new TurboMilestone(milestone));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return turboMilestones;
	}
}
