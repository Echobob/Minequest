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

import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.World;

import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.util.Vector;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class FireballEvent extends TargetedEvent {

	private Target target;
	private Vector vector;

	public FireballEvent(long delay, Target target, Vector vector) {
		super(delay, target);
		this.target = target;
		this.vector = vector;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		for (Quester quester : target.getTargets()) {
			if (quester.getPlayer() != null) {
				EntityLiving entity = ((CraftPlayer)quester.getPlayer()).getHandle();
				World worldObj = ((CraftWorld)entity.getBukkitEntity().getWorld()).getHandle();
				
                EntityFireball entityfireball = new EntityFireball(worldObj, entity, vector.getX(), vector.getY(), vector.getZ());
                entityfireball.locX = entity.locX;// + vec3d.a * d8;
                entityfireball.locY = entity.locY;// + (double)(entity.height / 2.0F) + 0.5D;
                entityfireball.locZ = entity.locZ;// + vec3d.c * d8;
//                worldObj.entityJoinedWorld(entityfireball);
                worldObj.addEntity(entityfireball);
//				((CraftWorld)entity.getBukkitEntity().getWorld()).getHandle().entityList.add(new EntityFireball(((CraftWorld)entity.getBukkitEntity().getWorld()).getHandle(), entity, vector.getX(), vector.getY(), vector.getZ()));
				
				MineQuest.log("Creating Fireball");
			}
		}

		super.activate(eventParser);
	}
	
	@Override
	public String getName() {
		return "Arrow Event";
	}

}
