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

import org.bukkit.plugin.Plugin;
import org.monksanctum.MineQuest.MineQuest;

public class EventQueue {
	private Plugin minequest;

	public EventQueue(Plugin plugin) {
		minequest = plugin;
	}
	
	public int addEvent(Event event) {
		EventParser newEventParser = new EventParser(event);
		newEventParser.setId(MineQuest.getSServer().getScheduler().scheduleSyncRepeatingTask(minequest, newEventParser, 1, 1));
		return newEventParser.getId();
	}
	
	public int addEventAsync(Event event) {
		EventParser newEventParser = new EventParser(event);
		newEventParser.setId(MineQuest.getSServer().getScheduler().scheduleAsyncRepeatingTask(minequest, newEventParser, 1, 1));
		return newEventParser.getId();
	}

	public void cancel(int[] ids) {
		for (int id : ids) {
			cancel(id);
		}
	}

	public void cancel(int id) {
		MineQuest.getSServer().getScheduler().cancelTask(id);
	}
}
