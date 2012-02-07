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
package org.monksanctum.MineQuest.Event.Absolute;

import org.bukkit.entity.LivingEntity;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.PeriodicEvent;
import org.monksanctum.MineQuest.Quest.Quest;

public class EntitySpawnerCompleteNMEvent extends PeriodicEvent {
	private Quest quest;
	private boolean first;
	private LivingEntity entities[];
	private int index;
	private EntitySpawnerEvent[] events;

	public EntitySpawnerCompleteNMEvent(Quest quest, long delay, int index, EntitySpawnerEvent[] events) {
		super(delay);
		this.quest = quest;
		this.index = index;
		first = true;
		this.events = events;
		this.entities = new LivingEntity[events.length];
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		if (first) {
			int i = 0;
			for (EntitySpawnerEvent e : events) {
				entities[i++] = e.getEntity();
				e.setComplete(true);
			}
		
			first = false;
		} else {
			boolean flag = true;
			int i;
			for (i = 0; i < entities.length; i++) {
				if ((entities[i] != null) && (entities[i].getHealth() > 0)) {
					flag = false;
				} else if (entities[i] != null) {
					entities[i] = null;
				}
			}
			
			if (flag) {
				eventComplete();
				eventParser.setComplete(true);
			}
		}
	}
	
	public void eventComplete() {
		quest.issueNextEvents(index);
	}
	
	@Override
	public String getName() {
		return "Entity Spawner No Move Destruction Event";
	}

}
