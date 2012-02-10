package org.monksanctum.MineQuest.Event.Absolute;

import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quest.CanEdit.CanEdit;

public class CanEditPattern extends QuestEvent {

	private CanEdit[] edits;
	private boolean[] flags;

	public CanEditPattern(Quest quest, long delay, int index, 
			CanEdit edits[], boolean flags[]) {
		super(quest, delay, index);
		this.edits = edits;
		this.flags = flags;
	}

	@Override
	public void activate(EventParser eventParser) {
		eventParser.setComplete(false);
		int i;
		boolean flag = true;
		
		for (i = 0; i < edits.length; i++) {
			if (edits[i].isActive() != flags[i]) {
				flag = false;
				break;
			}
		}
		
		if (flag) {
			eventParser.setComplete(true);
			eventComplete();
		}
	}
}
