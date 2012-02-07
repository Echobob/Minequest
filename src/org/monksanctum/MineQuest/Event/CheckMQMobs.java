package org.monksanctum.MineQuest.Event;

import org.monksanctum.MineQuest.MineQuest;

public class CheckMQMobs extends PeriodicEvent {

	public CheckMQMobs(long delay) {
		super(delay);
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		MineQuest.mobHandler.checkMobs();
	}

}
