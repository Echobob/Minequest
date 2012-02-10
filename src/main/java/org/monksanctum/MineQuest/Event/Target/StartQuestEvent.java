package org.monksanctum.MineQuest.Event.Target;

import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.MessageEvent;
import org.monksanctum.MineQuest.Quest.Party;
import org.monksanctum.MineQuest.Quest.QuestProspect;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class StartQuestEvent extends TargetedEvent {

	private String quest;

	public StartQuestEvent(long delay, Target target, String quest) {
		super(delay, target);
		this.quest = quest;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		startQuest(quest, target);
	}
	
	public void startQuest(String quest, Target target) {
		Party party = new Party();
		
		for (Quester quester : target.getTargets()) {
			party.addQuester(quester);
		}

		if (party.getQuesters().size() > 0) {
			if (!party.getQuesters().get(0).inQuest()) {
				party.getQuesters().get(0).startQuest(quest);
				MineQuest.getEventQueue().addEvent(new MessageEvent(10, party, "Starting Quest: " + (new QuestProspect(quest)).getName()));
			}
		}
	}

	@Override
	public String getName() {
		return "Start Quest Event";
	}

}
