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
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.PeriodicEvent;
import org.monksanctum.MineQuest.Mob.MQMob;
import org.monksanctum.MineQuest.Mob.SpecialMob;

public class EntitySpawnerEvent extends PeriodicEvent {
	protected LivingEntity entity;
	protected World world;
	protected Location location;
	protected CreatureType creatureType;
	protected boolean complete;
	protected boolean superm;

	public EntitySpawnerEvent(long delay, Location location, CreatureType creatureType, boolean superm) {
		super(delay);
		this.world = location.getWorld();
		this.location = location;
		this.creatureType = creatureType;
		this.superm = superm;
		entity = null;
		complete = false;
	}
	
	public void setComplete(boolean state) {
		complete = state;
	}
	
	public LivingEntity getEntity() {
		return entity;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		
		if ((entity == null) || (entity.getHealth() <= 0)) {
			if (creatureType == null) {
				MineQuest.log("Null CreatureType!!");
			}
			MineQuest.config.setSpawning(true);
			entity = world.spawnCreature(location, creatureType);
			MineQuest.config.setSpawning(false);
			if (entity != null) {
				if (superm) {
					MineQuest.mobHandler.setMQMob(new SpecialMob(entity));
				} else {
					MineQuest.mobHandler.setMQMob(new MQMob(entity));
				}
			}
			MineQuest.mobHandler.getMob(entity).setSpawned();
		}
		
		if (complete) {
			MineQuest.mobHandler.getMob(entity).setHealth(0);
		}
		eventParser.setComplete(complete);
	}

	@Override
	public String getName() {
		return "Repeating Entity Spawner Event";
	}
	
	@Override
	public void cancelEvent() {
		super.cancelEvent();
		
		if (entity != null) {
			if (MineQuest.mobHandler.getMob(entity) != null) {
				MineQuest.mobHandler.getMob(entity).setHealth(0);
			} else {
				entity.setHealth(0);
			}
		}
	}
}
