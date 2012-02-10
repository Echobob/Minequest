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

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Ability.Ability;
import org.monksanctum.MineQuest.Event.Absolute.BlockEvent;
import org.monksanctum.MineQuest.Mob.SpecialMob;

public class SpecialMobHandler extends PeriodicEvent {
	protected SpecialMob mob;

	public SpecialMobHandler(long delay, SpecialMob mob) {
		super(delay);
		this.mob = mob;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		if (mob.getMonster() instanceof Monster) {
			if (((Monster)mob.getMonster()).getTarget() == null) {
				((Monster)mob.getMonster()).setTarget(getNearestPlayer(mob.getMonster()));
			}
		}
		
		Location loc = mob.getMonster().getLocation();
		Block block = mob.getMonster().getWorld().getBlockAt((int)loc.getX(), 
				Ability.getNearestY(mob.getMonster().getWorld(), (int)loc.getX(), (int)loc.getY(), (int)loc.getZ()) - 1, (int)loc.getZ());
		
		if (block.getType() != Material.DIRT){
			MineQuest.getEventQueue().addEvent(new BlockEvent(30000, block, block.getType()));
			MineQuest.getEventQueue().addEvent(new BlockEvent(10, block, Material.DIRT));
		}

		super.activate(eventParser);
		if (mob.getMonster().getHealth() <= 0) {
			if (mob.isDead()) {
				mob.dropLoot();
			}
			eventParser.setComplete(true);
		}
	}

	private LivingEntity getNearestPlayer(LivingEntity livingEntity) {
		List<LivingEntity> entities = livingEntity.getWorld().getLivingEntities();
		double distance = 100000;
		LivingEntity player = null;
		
		for (LivingEntity entity : entities) {
			if (entity instanceof Player) {
				if (player == null) {
					distance = MineQuest.distance(entity.getLocation(), livingEntity.getLocation());
					player = entity;
				} else if (MineQuest.distance(entity.getLocation(), livingEntity.getLocation()) < distance) {
					distance = MineQuest.distance(entity.getLocation(), livingEntity.getLocation());
					player = entity;
				}
			}
		}
		
		return player;
	}

}
