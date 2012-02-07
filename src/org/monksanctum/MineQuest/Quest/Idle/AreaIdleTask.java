package org.monksanctum.MineQuest.Quest.Idle;

import org.bukkit.Location;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quest.QuestProspect;
import org.monksanctum.MineQuest.Quester.Quester;

public class AreaIdleTask extends IdleTask {

	private Location location;
	private double radius;
	private String name;

	public AreaIdleTask(QuestProspect quest, int event_id, int id, Quester quester, String name, Location location, double radius) {
		super(quest, event_id, id, quester);
		this.location = location;
		this.radius = radius;
		this.name = name;
	}

	@Override
	public boolean isComplete() {
		if (quester.getPlayer() == null) return false;
		
		if (MineQuest.distance(location, quester.getPlayer().getLocation()) < radius) {
			return true;
		}
		
		return false;
	}

	@Override
	public String getTarget() {
		return "";
	}

	@Override
	public IdleType getType() {
		return IdleType.AREA;
	}

	@Override
	public int getTypeId() {
		return IdleType.AREA.getId();
	}

	@Override
	public void printStatus() {
		if (name != null) {
			quester.sendMessage(quest.getName() + ": Looking for " + name);
		} else {
			quester.sendMessage(quest.getName() + ": Looking for " + 
					location.getBlockX() + " " + location.getBlockY() + " " + 
					location.getBlockZ());
		}
	}

	@Override
	public void setTarget(String target) {
		
	}

}
