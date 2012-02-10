package org.monksanctum.MineQuest.Event;

import org.monksanctum.MineQuest.MineQuest;

public class SQLEvent extends NormalEvent {

	private String update;

	public SQLEvent(long delay, String update) {
		super(delay);
		this.update = update;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		MineQuest.getSQLServer().update(update);
	}

	@Override
	public String getName() {
		return "Asynchronous MySQL Update";
	}

}
