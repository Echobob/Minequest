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
package org.monksanctum.MineQuest.Event.Target;

import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class EntityTeleportEvent extends TargetedEvent {
	protected Location location;

	public EntityTeleportEvent(long delay, Target target, Location location) {
		super(delay, target);
		this.location = location;
	}

	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		for (Quester quester : target.getTargets()) {
			CraftWorld cworld = (CraftWorld)quester.getPlayer().getWorld();
			CraftWorld aworld = (CraftWorld)location.getWorld();
			Player target = quester.getPlayer();
			if (quester instanceof NPCQuester) {
				((NPCQuester)quester).teleport(location);
			} else {
				quester.getPlayer().teleport(location);
			}
			if ((target != null) && !cworld.getName().equals(aworld.getName())) {
				WorldServer world = cworld.getHandle();
				world.manager.removePlayer(((CraftPlayer)target).getHandle());
			}
		}
	}

	@Override
	public String getName() {
		return "Targeted Generic Teleport Event";
	}

}
