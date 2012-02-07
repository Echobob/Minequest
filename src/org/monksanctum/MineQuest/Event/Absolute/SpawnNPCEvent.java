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
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.NormalEvent;
import org.monksanctum.MineQuest.Quester.NPCQuester;

public class SpawnNPCEvent extends NormalEvent {

	private NPCQuester npcQuester;
	private String world;
	private double x;
	private double y;
	private double z;
	private float pitch;
	private float yaw;

	public SpawnNPCEvent(long delay, NPCQuester npcQuester,
			String world, double x, double y, double z, float pitch, float yaw) {
		super(delay);
		this.npcQuester = npcQuester;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		if (MineQuest.getSServer().getWorld(world) == null) {
			eventParser.setComplete(false);
		} else {
			if (!npcQuester.isRemoved()) {
				Location l = new Location(MineQuest.getSServer().getWorld(world), 
						x, y, z, yaw, pitch);
				npcQuester.setEntity(MineQuest.getNPCManager().spawnNPC(npcQuester.getName(), l));
			}
//			npcQuester.setEntity(NpcSpawner.SpawnBasicHumanNpc(npcQuester.getName(), 
//					npcQuester.getName(), MineQuest.getSServer().getWorld(world), 
//					x, y, z, yaw, pitch));
		}
	}

	@Override
	public String getName() {
		return "Spawn NPC Event" + npcQuester.getName();
	}

}
