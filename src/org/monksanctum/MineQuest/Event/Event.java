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

public interface Event {
	/**
	 * This checks if the event should have been activated
	 * by now, returning true indicates it should be activated.
	 * 
	 * @param time current time
	 * @return true to request activation
	 */
	public boolean isPassed(long time);
	
	/**
	 * Resets the time of the event so that time of the event
	 * can be relative to activation, will be called when event
	 * is added to the event queue.
	 * 
	 * @param time current time
	 */
	public void reset(long time);
	
	/**
	 * Called when event has passed time for activation, the actual
	 * event happens now.
	 * 
	 * @param eventParser This is the parser calling the event.
	 */
	public void activate(EventParser eventParser);
	
	/**
	 * Gets identifiable name for the event
	 * 
	 * @return Name of event
	 */
	public String getName();
	
	/**
	 * Used for local organization. Does not have to be unique.
	 */
	void setId(int id);
	public int getId();

	public void setParser(EventParser eventParser);
	
	public void cancelEvent();
}
