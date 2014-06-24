package command;

import java.io.IOException;
import java.util.List;

import model.TurboMilestone;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.MilestoneService;

public class GetMilestonesCommand implements Command {
	
	private MilestoneService milestoneService;
	private IRepositoryIdProvider repoId;
	private List<TurboMilestone> milestones;
	
	public GetMilestonesCommand(GitHubClient ghClient, IRepositoryIdProvider repoId, List<TurboMilestone> milestones) {
		this.milestoneService = new MilestoneService(ghClient);
		this.repoId = repoId;
		this.milestones = milestones;
	}

	@Override
	public void execute() {
		try {		
			List<Milestone> ghMilestones = milestoneService.getMilestones(repoId, model.MilestoneManager.STATE_ALL);
			for (Milestone ghMilestone : ghMilestones) {
				milestones.add(new TurboMilestone(ghMilestone));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
