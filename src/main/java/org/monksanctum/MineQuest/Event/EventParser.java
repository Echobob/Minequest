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

import java.util.Calendar;

import org.monksanctum.MineQuest.MineQuest;

public class EventParser implements java.lang.Runnable {
	protected Event event;
	protected int id;
	protected boolean complete;
	private Calendar now;

	public EventParser(Event event) {
		this.event = event;
		complete = false;
		event.reset(getTime());
		event.setParser(this);
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	public boolean getComplete() {
		return complete;
	}

	public void run() {
		now = Calendar.getInstance();
		if (!complete && event.isPassed(now.getTimeInMillis())) {
			complete = true;
			try {
				event.activate(this);
				if (!complete) {
					event.reset(getTime());
				}
			} catch (Exception e) {
				MineQuest.log("Exception occured while parsing " + event.getName());
				e.printStackTrace();
			}
		} else if (complete) {
			MineQuest.getSServer().getScheduler().cancelTask(id);
			if (event instanceof NormalEvent) {
				NormalEvent.count--;
			}
		}
	}

	public int getId() {
		return id;
	}

	public long getTime() {
		now = Calendar.getInstance();
		return now.getTimeInMillis();
	}
}
