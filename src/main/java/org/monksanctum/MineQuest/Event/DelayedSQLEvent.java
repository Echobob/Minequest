package org.monksanctum.MineQuest.Event;

import org.monksanctum.MineQuest.MineQuest;

public class DelayedSQLEvent extends NormalEvent {
	private String update;

	public DelayedSQLEvent(long delay, String update) {
		super(delay);
		this.update = update;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		MineQuest.getSQLServer().update(update);
		
		super.activate(eventParser);
	}

	@Override
	public String getName() {
		return "Delayed SQL Event";
	}

}
