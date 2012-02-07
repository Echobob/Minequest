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

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Mob.HealthMob;
import org.monksanctum.MineQuest.Quest.Quest;

public class HealthEntitySpawn extends QuestEvent {
	private int health;
	private CreatureType creatureType;
	private LivingEntity entity;
	private Location location;
	private boolean stay;

	public HealthEntitySpawn(Quest quest, long delay, int task, Location location, CreatureType creatureType, int health, boolean stay) {
		super(quest, delay, task);
		this.health = health;
		this.creatureType = creatureType;
		this.entity = null;
		this.location = location;
		this.stay = stay;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		eventParser.setComplete(false);
		if (entity == null) {
			entity = location.getWorld().spawnCreature(location, creatureType);
			if (entity != null) {
				MineQuest.mobHandler.setMQMob(new HealthMob(entity, health));
			} else {
				MineQuest.log("Unable to create Health Entity");
				eventParser.setComplete(true);
			}
			
			return;
		}
		if (stay) {
			entity.teleport(location);
		}
		
		if (!(entity.getHealth() > 0)) {
			eventComplete();
			eventParser.setComplete(true);
		}
	}
	
	@Override
	public String getName() {
		return "Health Entity Spawner";
	}

}
