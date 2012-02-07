package org.monksanctum.MineQuest.Event.Target;

import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class SetVehicleVelocityEvent extends TargetedEvent {

	private Vector vel;

	public SetVehicleVelocityEvent(long delay, Target target, Vector vel) {
		super(delay, target);
		this.vel = vel;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : target.getTargets()) {
			if (quester.getPlayer() != null) {
				Vehicle vehicle = quester.getPlayer().getVehicle();
				if (vehicle != null) {
					vehicle.setVelocity(vel);
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Set Velocity Event";
	}

}
