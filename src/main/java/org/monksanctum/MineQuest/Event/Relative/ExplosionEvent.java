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
package org.monksanctum.MineQuest.Event.Relative;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;

public class ExplosionEvent extends RelativeEvent {

	private double x;
	private double y;
	private double z;
	private float radius;
	private int damage;
	private CraftWorld world;
	private Entity entity;

	public ExplosionEvent(long delay, World world, Entity entity, double x, double y, double z, float radius, int damage) {
		super(delay);
		this.world = (CraftWorld)world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.radius = radius;
		this.damage = damage;
		this.entity = entity;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		Location loc = entity.getLocation();
		world.getHandle().createExplosion(null, x + loc.getX(), y + loc.getY(), z + loc.getZ(), radius, false);
		
		List<LivingEntity> entities = world.getLivingEntities();
		Location location = new Location(null, x + loc.getX(), y + loc.getY(), z + loc.getZ());
		for (LivingEntity entity : entities) {
			if (MineQuest.distance(location, entity.getLocation()) < radius) {
				MineQuest.damage(entity, damage);
			}
		}
	}

	@Override
	public String getName() {
		return "Relative Explosion Event";
	}

}
