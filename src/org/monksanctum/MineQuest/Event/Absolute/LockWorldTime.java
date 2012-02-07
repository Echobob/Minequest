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

import org.bukkit.World;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.PeriodicEvent;

public class LockWorldTime extends PeriodicEvent {

	private World world;
	private long time;
	private long time_2;

	public LockWorldTime(long delay, World world, long time, long time_2) {
		super(delay);
		this.world = world;
		this.time = time;
		this.time_2 = time_2;
		world.setTime(time);
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		if (world.getTime() > time_2) {
			world.setTime(time);
		}
	}

}
