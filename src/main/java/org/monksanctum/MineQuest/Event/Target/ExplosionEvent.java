package org.monksanctum.MineQuest.Event.Target;

import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class ExplosionEvent extends TargetedEvent {
	protected int damage;
	protected float radius;
	private long real_delay;

	public ExplosionEvent(long delay, Target target, float radius, int damage) {
		super(0, target);
		this.radius = radius;
		this.damage = damage;
		this.real_delay = delay;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : target.getTargets()) {
			MineQuest.getEventQueue().addEvent(
					new org.monksanctum.MineQuest.Event.Absolute.ExplosionEvent(
							real_delay, 
							quester.getPlayer().getWorld(), 
							quester.getPlayer().getLocation().getX(), 
							quester.getPlayer().getLocation().getY(),
							quester.getPlayer().getLocation().getZ(), 
							radius, damage));
		}
	}

	@Override
	public String getName() {
		return "Targeted Explosion Event";
	}

}
