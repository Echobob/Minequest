/*
 * MineQuest - Bukkit Plugin for adding RPG characteristics to minecraft
 * Copyright (C) 2011  Jason Monk
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.monksanctum.MineQuest.Quest;

import java.util.List;

import org.bukkit.Location;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.TargetEvent;
import org.monksanctum.MineQuest.Quester.Quester;

public abstract class Target {
	private int id;
	
	public static Target newTarget(String split[], Quest quest) throws Exception{
		int id = Integer.parseInt(split[1]);
		Target target = null;
		
		if (split[2].equals("AreaTarget")) {
			Location location = new Location(null,
					Double.parseDouble(split[3]),
					Double.parseDouble(split[4]),
					Double.parseDouble(split[5]));
			double radius = Double.parseDouble(split[6]);
			target = new AreaTarget(location, radius);
		} else if (split[2].equals("AreaTargetQuester")) {
			Target t = quest.getTarget(Integer.parseInt(split[3]));
			double radius = Double.parseDouble(split[4]);
			
			target = new AreaTargetQuester(t, radius);
		} else if (split[2].equals("RandomTarget")) {
			Target t = quest.getTarget(Integer.parseInt(split[3]));
			
			target = new RandomTarget(t);
		} else if (split[2].equals("PartyTarget")) {
			target = new PartyTarget(quest.getParty());
		} else if (split[2].equals("NPCTarget")) {
			String[] names = split[3].split(",");
			
			target = new NPCTarget(names);
		} else if (split[2].equals("Targetter") || split[2].equals("Targeter")) {
			String[] event_ids = split[3].split(",");
			TargetEvent[] events = new TargetEvent[event_ids.length];
			int i = 0;
			
			for (String eid : event_ids) {
				events[i++] = (TargetEvent)quest.getEvent(Integer.parseInt(eid));
			}
			
			target = new Targetter(events);
		} else if (split[2].equals("NPCTarget")) {
			String[] names = split[3].split(",");
			
			target = new NPCTarget(names);
		} else if (split[2].equals("TargetterEdit")) {
			String[] event_ids = split[3].split(",");
			TargetEvent[] events = new TargetEvent[event_ids.length];
			int i = 0;
			
			for (String eid : event_ids) {
				events[i++] = (TargetEvent)quest.getEdit(Integer.parseInt(eid));
			}
			
			target = new Targetter(events);
		} else {
			MineQuest.log("Error: Unknown Target Type " + split[2]);
			throw new Exception();
		}
		
		target.setId(id);
		
		return target;
	}

	public abstract List<Quester> getTargets();
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
}
