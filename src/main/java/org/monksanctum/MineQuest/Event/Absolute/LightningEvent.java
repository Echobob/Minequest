package org.monksanctum.MineQuest.Event.Absolute;

import org.bukkit.Location;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.NormalEvent;

public class LightningEvent extends NormalEvent {
	
	private Location location;

	public LightningEvent(long delay, Location location) {
		super(delay);
		this.location = location;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		location.getWorld().strikeLightning(location);
	}

	@Override
	public String getName() {
		return "Lightning Event";
	}

}
