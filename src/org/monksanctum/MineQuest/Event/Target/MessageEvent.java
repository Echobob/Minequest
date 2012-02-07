package org.monksanctum.MineQuest.Event.Target;

import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class MessageEvent extends TargetedEvent {

	private String message;

	public MessageEvent(long delay, Target target, String message) {
		super(delay, target);
		this.message = message;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : target.getTargets()) {
			if (!(quester instanceof NPCQuester)) {
				quester.sendMessage(message);
			}
		}
	}

	@Override
	public String getName() {
		return "Targeted Message Event";
	}

}
