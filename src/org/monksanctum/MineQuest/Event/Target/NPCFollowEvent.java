package org.monksanctum.MineQuest.Event.Target;

import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class NPCFollowEvent extends TargetedEvent {
	private Target other;

	public NPCFollowEvent(long delay, Target target, Target other) {
		super(delay, target);
		this.other = other;
	}

	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		if (other.getTargets().size() == 0) return;
		
		for (Quester quester : target.getTargets()) {
			if (quester instanceof NPCQuester) {
				((NPCQuester)quester).setProperty("follow", other.getTargets().get(0).getName());
			}
		}
	}
	
	@Override
	public String getName() {
		return "NPC Set Follow Event";
	}
}
