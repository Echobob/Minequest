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

import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.Event;

public class RepeatingQuestTask extends QuestTask {

	public RepeatingQuestTask(Event[] events, int id) {
		super(events, id);
	}

	public void issueEvents() {
		if (events != null) {
			int i;
			int new_ids[] = new int[ids.length + events.length];
			for (i = 0; i < ids.length; i++) {
				new_ids[i] = ids[i];
			}
			for (Event event : events) {
				new_ids[i++] = MineQuest.getEventQueue().addEvent(event);
			}
			ids = new_ids;
		}
	}

}
