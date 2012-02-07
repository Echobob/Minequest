package org.monksanctum.MineQuest.Event.Absolute;

import org.bukkit.World;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.NormalEvent;

public class WeatherEvent extends NormalEvent {
	private World world;
	private boolean hasStorm;
	private int duration;

	public WeatherEvent(long delay, World world, boolean hasStorm, int duration) {
		super(delay);
		this.world = world;
		this.hasStorm = hasStorm;
		this.duration = duration;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		world.setStorm(hasStorm);
		world.setWeatherDuration(duration);
	}

	@Override
	public String getName() {
		return "Weather Event";
	}

}
