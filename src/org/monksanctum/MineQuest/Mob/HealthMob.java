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

import org.bukkit.entity.LivingEntity;

public class HealthMob extends MQMob {
	protected int health;
	protected int max_health;

	public HealthMob(LivingEntity entity, int max_health) {
		super(entity);
		this.health = this.max_health = max_health;
	}

	public int defend(int damage, LivingEntity player) {
		int newHealth;

	    health -= damage;
	    
	    newHealth = 20 * health / max_health;
	    
	    if ((newHealth == 0) && (health > 0)) {
	    	newHealth++;
	    }
	    
	    if (health > max_health) {
	    	health = max_health;
	    }
	    if (newHealth <= 0) {
	    	dead = true;
	    }
        if (entity.getHealth() >= newHealth) {
        	return entity.getHealth() - newHealth;
        } else {
        	if (entity.getHealth() < 20) {
        		player.setHealth(health + 1);
        		return 1;
        	} else {
        		return 0;
        	}
        }
	}
	
	public void damage(int i) {
		health -= i;
		updateHealth();
	}

	public void updateHealth() {
		int newValue;
		
		newValue = 20 * health / max_health;
		
		if ((newValue == 0) && (health > 0)) {
			newValue++;
		}

		if (newValue < 0) {
			newValue = 0;
		}
		
		entity.setHealth(newValue);
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int i) {
		health = i;
		updateHealth();
	}
}
