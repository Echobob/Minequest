package org.monksanctum.MineQuest.Event;

import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class RespawnEvent extends PeriodicEvent {

	public RespawnEvent(long delay) {
		super(delay);
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : MineQuest.questerHandler.getQuesters()) {
			if (quester instanceof NPCQuester) {
				((NPCQuester)quester).redo();
			}
		}
	}

	@Override
	public String getName() {
		return "NPC Respawn Event";
	}

}
