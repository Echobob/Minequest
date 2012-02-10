package org.monksanctum.MineQuest.Event.Absolute;

import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.PeriodicEvent;
import org.monksanctum.MineQuest.Quester.Quester;

public class HealEvent extends PeriodicEvent {

	private int amount;

	public HealEvent(long delay, int amount) {
		super(delay);
		this.amount = amount;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : MineQuest.questerHandler.getQuesters()) {
			if (!quester.inQuest()) {
				quester.setHealth(quester.getHealth() + amount);
			}
		}
	}

}
