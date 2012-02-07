package org.monksanctum.MineQuest.Event.Idle;

import org.bukkit.Location;
import org.monksanctum.MineQuest.Quest.Party;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quest.Idle.AreaIdleTask;
import org.monksanctum.MineQuest.Quest.Idle.IdleTask;

public class NamedAreaIdleEvent extends IdleEvent {
	private Location location;
	private double radius;
	private String name;

	public NamedAreaIdleEvent(long delay, Party party, Quest quest, int task_id, 
			Location location, double radius, String name) throws Exception {
		super(delay, party, quest, task_id);
		this.location = location;
		this.radius = radius;
		this.name = name;
	}

	@Override
	public String getName() {
		return "Named Area Idle Event";
	}

	@Override
	public IdleTask createEvent() {
		return new AreaIdleTask(quest.getProspect(), id, task_id, party.getQuesters().get(0), name, location, radius);
	}

}
