package org.monksanctum.MineQuest.Event.Target;

import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.Absolute.CreateBoatEvent;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;


public class NPCEnterBoat extends TargetedEvent {

	private CreateBoatEvent boat_event;

	public NPCEnterBoat(long delay, Target target, CreateBoatEvent boat_event) {
		super(delay, target);
		this.boat_event = boat_event;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		if (target.getTargets().size() > 0) {
			Quester quester = target.getTargets().get(0);
			
			boat_event.getBoat().setPassenger(quester.getPlayer());
		}
	}

	@Override
	public String getName() {
		return "NPC Enter Boat Event";
	}

}
