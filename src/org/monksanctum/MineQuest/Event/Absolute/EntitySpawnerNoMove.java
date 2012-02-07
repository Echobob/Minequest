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
import org.monksanctum.MineQuest.Event.EventParser;

public class EntitySpawnerNoMove extends EntitySpawnerEvent {

	public EntitySpawnerNoMove(long delay, Location location,
			CreatureType creatureType, boolean superm) {
		super(delay, location, creatureType, superm);
	}
	
	@Override
	public void activate(EventParser eventParser) {
		if (!complete) {
			super.activate(eventParser);
		} else {
			eventParser.setComplete(true);
		}
		
		if (entity != null) entity.teleport(location);
	}

	@Override
	public String getName() {
		return "Repeating Entity Spawner Event with No Movement";
	}
}
