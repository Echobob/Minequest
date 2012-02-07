package org.monksanctum.MineQuest.Event.Target;

import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class NPCPropertyEvent extends TargetedEvent {
	private String type;
	private String value;

	public NPCPropertyEvent(long delay, Target target, String type, String value) {
		super(delay, target);
		this.type = type; 
		this.value = value;
	}

	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : target.getTargets()) {
			if (quester instanceof NPCQuester) {
				((NPCQuester)quester).setProperty(type, value);
			}
		}
	}
	
	@Override
	public String getName() {
		return "NPC Set Property Event";
	}
}
