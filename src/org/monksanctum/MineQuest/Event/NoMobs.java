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
package org.monksanctum.MineQuest.Event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.MineQuest;

public class NoMobs extends PeriodicEvent {
	private String world;

	public NoMobs(long delay, String world) {
		super(delay);
		this.world = world;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (LivingEntity entity : MineQuest.getSServer().getWorld(world).getLivingEntities()) {
			if (!(entity instanceof Player)) {
				if (MineQuest.mobHandler.getMob(entity) != null) {
					MineQuest.mobHandler.getMob(entity).setHealth(0);
				} else {
					entity.setHealth(0);
				}
			}
		}
		
		eventParser.setComplete(true);
	}

}
