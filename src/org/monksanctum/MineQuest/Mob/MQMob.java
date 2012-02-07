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
package org.monksanctum.MineQuest.Mob;

import org.bukkit.Chunk;
import org.bukkit.entity.LivingEntity;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

public class MQMob {
	protected LivingEntity entity;
	private Quester last_attack;
	protected boolean dead;
	protected boolean spawned;

	public MQMob(LivingEntity entity) {
		this.entity = entity;
		last_attack = null;
		dead = false;
		spawned = false;
	}
	
	public boolean isDead() {
		return dead;
	}
	
	public Quester getLastAttacker() {
		return last_attack;
	}
	
	public LivingEntity getMonster() {
		return entity;
	}

	public int getId() {
		return entity.getEntityId();
	}
	
	public double dodgeChance() {
		return .01;
	}
	
	public void cancel() {
	}

	public void dropLoot() {
	}

	public int defend(int damage, LivingEntity player) {
		if ((entity.getHealth() - damage) <= 0) {
			dead = true;
		}
		if (MineQuest.questerHandler.getQuester(player) != null) {
			last_attack = MineQuest.questerHandler.getQuester(player);
		}
		return damage;
	}

	public int attack(int amount, LivingEntity player) {
		return amount;
	}

	public int getHealth() {
		return entity.getHealth();
	}

	public void setHealth(int i) {
		if (i < 0) {
			i = 0;
		} else if (i > 20) {
			i = 20;
		}
		entity.setHealth(i);
	}

	public void damage(int i) {
		if ((entity.getHealth() - i) <= 0) {
			dead = true;
		}
		entity.damage(i);
	}

	public void damage(int i, Quester source) {
		if (source != null) {
			this.last_attack = source;
		}
		if ((entity.getHealth() - i) <= 0) {
			dead = true;
		}
		if (source != null) {
			entity.damage(i, source.getPlayer());
		} else {
			entity.damage(i, null);
		}
	}

	public boolean isSpawned() {
		return spawned;
	}
	
	public void setSpawned() {
		spawned = true;
	}

	public boolean inChunk(Chunk chunk) {
		Chunk other = entity.getWorld().getChunkAt(entity.getLocation());
		
		if (chunk.getX() == other.getX()) {
			if (chunk.getZ() == other.getZ()) {
				return true;
			}
		}
		
		return false;
	}
}
