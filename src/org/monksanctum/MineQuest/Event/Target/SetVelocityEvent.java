package org.monksanctum.MineQuest.Event.Target;

import org.bukkit.util.Vector;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class SetVelocityEvent extends TargetedEvent {

	private Vector vel;

	public SetVelocityEvent(long delay, Target target, Vector vel) {
		super(delay, target);
		this.vel = vel;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : target.getTargets()) {
			if (quester.getPlayer() != null) {
				quester.getPlayer().setVelocity(vel);
			}
		}
	}

	@Override
	public String getName() {
		return "Set Velocity Event";
	}

}
