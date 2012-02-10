package org.monksanctum.MineQuest.Event;

import org.monksanctum.MineQuest.Quest.Idle.IdleTask;

public class IdleTaskEvent extends NormalEvent {

	private IdleTask idle;

	public IdleTaskEvent(long delay, IdleTask idle) {
		super(delay);
		this.idle = idle;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		idle.continueQuest();
	}

	@Override
	public String getName() {
		return "Idle Task Event";
	}

}
