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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quester.Quester;

public class AuraEvent extends RelativeEvent {
	protected LivingEntity player;
	protected World world;
	private long total_time;
	private int change;
	private boolean players;
	private long count;
	private double radius;

	public AuraEvent(Quester quester, long delay, long total_time, int change, boolean players, double radius) {
		super(delay);
		player = quester.getPlayer();
		world = player.getWorld();
		this.total_time = total_time;
		this.count = 0;
		this.change = change;
		this.players = players;
		this.radius = radius;
	}

	public void activate(EventParser eventParser) {
		List<LivingEntity> nearby = getEntities(player, radius);
		List<LivingEntity> affected = sort(nearby, players);
		
		for (LivingEntity entity : affected) {
			if (players) {
				MineQuest.questerHandler.getQuester((Player)entity).setHealth(MineQuest.questerHandler.getQuester((Player)entity).getHealth() + change);
			} else {
				MineQuest.mobHandler.getMob(entity).damage(change);
			}
		}
		
		count += delay;
		if (count < total_time) {
			eventParser.setComplete(false);
		} else {
			eventParser.setComplete(true);
		}
	}
	
	/**
	 * Gets the entities within a area of a player. name
	 * 
	 * Not Implemented in bukkit yet!
	 * 
	 * @param player
	 * @param radius
	 * @return List of Entities within the area
	 */
	public List<LivingEntity> getEntities(LivingEntity entity, double radius) {
		List<LivingEntity> entities = new ArrayList<LivingEntity>(0);
		List<LivingEntity> serverList = entity.getWorld().getLivingEntities();
		int i;
		
		for (i = 0; i < serverList.size(); i++) {
			if ((MineQuest.distance(entity.getLocation(), serverList.get(i).getLocation()) <= (radius + .001))) {
				entities.add(serverList.get(i));
			}
		}
		
		return entities;
	}

	private List<LivingEntity> sort(List<LivingEntity> nearby, boolean players) {
		List<LivingEntity> ret = new ArrayList<LivingEntity>();
		
		for (LivingEntity entity : nearby) {
			if (players && (entity instanceof Player)) {
				ret.add(entity);
			} else if (!players && !(entity instanceof Player)){
				ret.add(entity);
			}
		}
		
		return ret;
	}

	@Override
	public String getName() {
		return "Relative Aura Event";
	}
}
