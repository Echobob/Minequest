package org.monksanctum.MineQuest.Event.Target;

import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class ReputationAddEvent extends TargetedEvent {

	private String type;
	private int amount;

	public ReputationAddEvent(long delay, Target target, String type, int amount) {
		super(delay, target);
		this.type = type;
		this.amount = amount;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : target.getTargets()) {
			quester.addReputation(type, amount);
		}
	}

	@Override
	public String getName() {
		return "Reputation Add Event";
	}

}
