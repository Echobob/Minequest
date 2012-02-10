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

import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.NormalEvent;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class EntityTeleportEvent extends NormalEvent {
	protected LivingEntity entity;
	protected Location location;
	private Quester quester;

	public EntityTeleportEvent(long delay, LivingEntity entity, Location location) {
		super(delay);
		this.entity = entity;
		this.location = location;
		quester = null;
	}

	public EntityTeleportEvent(int delay, Quester quester,
			Location location) {
		super(delay);
		this.quester = quester;
		this.location = location;
	}

	@Override
	public void activate(EventParser eventParser) {
		try {
			CraftWorld cworld;
			CraftWorld aworld;
			Player target = null;
			if (quester != null) {
				cworld = (CraftWorld)quester.getPlayer().getWorld();
				aworld = (CraftWorld)location.getWorld();
				target = quester.getPlayer();
				if (quester instanceof NPCQuester) {
					((NPCQuester)quester).teleport(location);
				} else {
					quester.getPlayer().teleport(location);
				}
			} else {
				cworld = (CraftWorld)entity.getWorld();
				aworld = (CraftWorld)location.getWorld();
				if (entity instanceof Player) {
					target = (Player)entity;
				}
				entity.teleport(location);
			}
			if ((target != null) && !cworld.getName().equals(aworld.getName())) {
				WorldServer world = cworld.getHandle();
				world.manager.removePlayer(((CraftPlayer)target).getHandle());
			}
		} catch (Exception e) {
			eventParser.setComplete(false);
		}
	}

	@Override
	public String getName() {
		return "Generic Teleport Event";
	}

}
