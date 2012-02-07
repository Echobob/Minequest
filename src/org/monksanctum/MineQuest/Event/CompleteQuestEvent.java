package org.monksanctum.MineQuest.Event;

import org.monksanctum.MineQuest.Quest.Party;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quester.Quester;

public class CompleteQuestEvent extends NormalEvent {
	private Quest quest;
	private Party party;

	public CompleteQuestEvent(long delay, Quest quest, Party party) {
		super(delay);
		this.quest = quest;
		this.party = party;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : party.getQuesterArray()) {
			quester.completeQuest(quest.getProspect());
			if (!quest.getProspect().isRepeatable()) {
				quester.remQuestAvailable(quest.getProspect());
			}
		}
	}

	@Override
	public String getName() {
		return "Complete Quest Event";
	}

}
