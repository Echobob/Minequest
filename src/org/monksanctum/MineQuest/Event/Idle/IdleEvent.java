package org.monksanctum.MineQuest.Event.Idle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.NormalEvent;
import org.monksanctum.MineQuest.Quest.Party;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quest.Idle.IdleTask;

public abstract class IdleEvent extends NormalEvent {
	public static IdleEvent newIdleEvent(Quest quest, String[] split) throws Exception {
		IdleEvent event = null;
		
		long delay = Integer.parseInt(split[4]);
		int task_id = Integer.parseInt(split[5]);
		if (split[3].equals("AreaIdleEvent")) {
			Location location = new Location(quest.getWorld(),
					Double.parseDouble(split[6]),
					Double.parseDouble(split[7]),
					Double.parseDouble(split[8]));
			double radius = Double.parseDouble(split[9]);
			event = new AreaIdleEvent(delay, quest.getParty(), quest, task_id, location, radius);
		} else if (split[3].equals("NamedAreaIdleEvent")) {
			Location location = new Location(quest.getWorld(),
					Double.parseDouble(split[7]),
					Double.parseDouble(split[8]),
					Double.parseDouble(split[9]));
			double radius = Double.parseDouble(split[10]);
			event = new NamedAreaIdleEvent(delay, quest.getParty(), quest, task_id, location, radius, split[6]);
		} else if (split[3].equals("KillIdleEvent")) {
			int i;
			String[] kill_names = split[6].split(",");
			CreatureType[] creatures = new CreatureType[kill_names.length];
			int[] kills = new int[split[7].split(",").length];
			if (kill_names.length != kills.length) {
				MineQuest.log("Error: Unmatched Length of Names and Quantities");
				throw new Exception();
			}
			for (String kill_name : kill_names) {
				if (CreatureType.fromName(kill_name) == null) {
					MineQuest.log("Error: Invalid Creature Name " + kill_name);
					throw new Exception();
				}
			}
			i = 0;
			for (String count : split[7].split(",")) {
				creatures[i] = CreatureType.fromName(kill_names[i]);
				kills[i++] = Integer.parseInt(count);
			}
			if (kill_names.length == 0) {
				MineQuest.log("Error: Cannot Have 0 Targets");
				throw new Exception();
			}

			event = new KillIdleEvent(delay, quest.getParty(), quest, task_id, creatures, kills);
		} else if (split[3].equals("DestroyIdleEvent")) {
			int i;
			String[] material_names = split[6].split(",");
			Material[] materials = new Material[material_names.length];
			int[] destroys = new int[split[7].split(",").length];
			if (material_names.length != destroys.length) {
				MineQuest.log("Error: Unmatched Length of Names and Quantities");
				throw new Exception();
			}
			for (String material_name : material_names) {
				if (CreatureType.fromName(material_name) == null) {
					MineQuest.log("Error: Invalid Material Name " + material_name);
					throw new Exception();
				}
			}
			i = 0;
			for (String count : split[7].split(",")) {
				materials[i] = Material.getMaterial(material_names[i]);
				destroys[i++] = Integer.parseInt(count);
			}
			if (material_names.length == 0) {
				MineQuest.log("Error: Cannot Have 0 Targets");
				throw new Exception();
			}

			event = new DestroyIdleEvent(delay, quest.getParty(), quest, task_id, materials, destroys);
		} else {
			throw new Exception();
		}
		event.setId(Integer.parseInt(split[1]));
		
		return event;
	}

	protected Party party;
	protected Quest quest;
	protected int task_id;

	public IdleEvent(long delay, Party party, Quest quest, int task_id) throws Exception {
		super(delay);
		this.party = party;
		this.quest = quest;
		this.task_id = task_id;
		if (party.getQuesters().size() == 0) throw new Exception();
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		quest.issueNextEvents(-1);
		if (party.getQuesters().size() > 0) {
			party.getQuesters().get(0).addIdle(createEvent());
		}
	}

	public abstract IdleTask createEvent();
}
