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

public class PoisonEvent extends PeriodicEvent {
	private LivingEntity entity;
	private int amount;
	private int total;

	public PoisonEvent(long delay, LivingEntity entity, int amount, int total) {
		super(delay);
		this.entity = entity;
		if (entity instanceof Player) {
			((Player)entity).sendMessage("Time Poisoned! No Cure Available!");
		}
		this.amount = amount;
		this.total = total;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		total -= amount;
		
		MineQuest.damage(entity, amount);
		
		if (total <= 0) {
			eventParser.setComplete(true);
		}
	}
	
	@Override
	public String getName() {
		return "Poison Event";
	}

}
