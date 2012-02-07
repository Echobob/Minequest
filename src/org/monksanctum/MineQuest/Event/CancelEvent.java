package org.monksanctum.MineQuest.Event;

import org.monksanctum.MineQuest.Quest.Quest;

public class CancelEvent extends NormalEvent
{

	private Quest quest;
	private int[] cancel_ids;

	public CancelEvent(long delay, Quest quest, int id[]) {
		super(delay);
		this.quest = quest;
		this.cancel_ids = id;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (int cancel : cancel_ids) {
			quest.getEvent(cancel).cancelEvent();
		}
	}

	@Override
	public String getName() {
		return "Cancel Event";
	}

}
