package org.monksanctum.MineQuest.Event.Target;

import java.util.List;

import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.QuestProspect;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class AdvancedStartQuestEvent extends StartQuestEvent {

	private String[] req;
	private String[] quest;

	public AdvancedStartQuestEvent(long delay, Target target, String req[], String quest[]) {
		super(delay, target, null);
		this.req = req;
		this.quest = quest;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		int i;
		List<Quester> questers = target.getTargets();
		for (i = 0; i < req.length; i++) {
			if (questers.size() > 0) {
				if ((req == null) || (questers.get(0).isCompleted(new QuestProspect(req[i])))) {
					startQuest(quest[i], target);
					return;
				}
			}
		}
		
	}

}
