package org.monksanctum.MineQuest.Event.Target;

import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class PoisonEvent extends TargetedEvent {
	protected int amount;

	public PoisonEvent(long delay, Target target, int amount) {
		super(delay, target);
		this.amount = amount;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		int i;

		for (Quester quester : target.getTargets()) {
			for (i = 0; i < amount; i++) {
				quester.poison();
			}
		}
	}

	@Override
	public String getName() {
		return "Targeted Poison Event";
	}

}
