package org.monksanctum.MineQuest.Event.Target;

import org.bukkit.Location;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class LightningEvent extends TargetedEvent {

	public LightningEvent(long delay, Target target) {
		super(delay, target);
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : target.getTargets()) {
			Location location = quester.getPlayer().getLocation();
			location.getWorld().strikeLightning(location);
		}
	}

	@Override
	public String getName() {
		return "Targeted Lightning Event";
	}

}
