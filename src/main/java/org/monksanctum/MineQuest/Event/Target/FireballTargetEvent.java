package org.monksanctum.MineQuest.Event.Target;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class FireballTargetEvent extends TargetedEvent {

	private Target target_2;

	public FireballTargetEvent(long delay, Target target, Target target_2) {
		super(delay, target);
		this.target_2 = target_2;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		if (target.getTargets().size() == 0) return;
		if (target_2.getTargets().size() == 0) return;
		Quester source = target.getTargets().get(0);
		Quester victim = target_2.getTargets().get(0);
		Location src = source.getPlayer().getLocation();
		Location tgt = victim.getPlayer().getLocation();
		double distance = MineQuest.distance(src, tgt);
		
		Vector dir = new Vector((tgt.getX() - src.getX())/distance, (tgt.getY() - src.getY()) / distance, (tgt.getZ() - src.getZ()) / distance);
		src.setX(src.getX() + .2 * dir.getX());
		src.setY(src.getY() + .2 * dir.getY());
		src.setZ(src.getZ() + .2 * dir.getZ());
		src.getWorld().spawnArrow(src, dir, 0.6f, 5f);
		(new FireballEvent(0, target, dir)).activate(null);
	}

	@Override
	public String getName() {
		return "Arrow Target Event";
	}

}
