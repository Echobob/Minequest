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
package org.monksanctum.MineQuest.Listener;


import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Mob.MQMob;
import org.monksanctum.MineQuest.Quester.Quester;

public class MineQuestEntityListener extends EntityListener {
//	private int save_damage;

	@Override
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (!MineQuest.isWorldEnabled(event.getEntity().getWorld())) return;
		Entity entity = event.getEntity();
		
		if (MineQuest.mobHandler.canCreate(entity)) {
			if (entity instanceof Monster) {
				MineQuest.mobHandler.addMob((Monster)entity);
				MineQuest.mobHandler.checkMobs();
			}
		} else {
			event.setCancelled(true);
		}
	}

	@Override
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if (!MineQuest.isWorldEnabled(event.getEntity().getWorld())) return;
		if (event.getEntity() instanceof Player) {
			if (!MineQuest.isMQEnabled((Player) event.getEntity())) return;
			Quester quester = MineQuest.questerHandler.getQuester((Player)event.getEntity());
			
			quester.regain(event);
		}
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (!MineQuest.isWorldEnabled(event.getEntity().getWorld())) return;
		if (event.isCancelled()) return;
		
		// old way of getting ranged attacks
//		if (event instanceof EntityDamageByProjectileEvent) {
//			EntityDamageByEntityEvent evente = ((EntityDamageByProjectileEvent)event);
//
//			if (evente.getDamager() instanceof Player) {
//				if (MineQuest.questerHandler.getQuester((Player)evente.getDamager()) != null) {
//					MineQuest.questerHandler.getQuester((Player)evente.getDamager()).attackEntity(evente.getEntity(), evente);
//				}
//			}
//
//			if (!event.isCancelled()) {
//				if (event.getEntity() instanceof Player) {
//					MineQuest.questerHandler.getQuester((Player)evente.getEntity()).defendEntity(evente.getDamager(), evente);
//				} else if ((event.getEntity() instanceof LivingEntity) && 
//						(MineQuest.mobHandler.getMob((LivingEntity)event.getEntity()) != null)) {
//					if (evente.getDamager() instanceof LivingEntity) {
//						evente.setDamage(MineQuest.mobHandler.getMob((LivingEntity)event.getEntity()).defend(evente.getDamage(), 
//								(LivingEntity)evente.getDamager()));
//					} else {
//						evente.setDamage(MineQuest.mobHandler.getMob((LivingEntity)event.getEntity()).defend(evente.getDamage(), 
//								null));
//					}
//				}
//			}
//			return;
//		}

		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent evente = ((EntityDamageByEntityEvent)event);	// Change event type
			Entity damager = evente.getDamager();	// Get the attacker
			Entity damagee = evente.getEntity();	// Get the victim

			if (damager instanceof Projectile) {	// Check for ranged attacks
				damager = ((Projectile)damager).getShooter();	// MineQuest attack/defend system expects a shooter not a projectile
			}

			if (damager instanceof Player) {		// Attacker is a quester (parse damage hooks and abilities)
			    MineQuest.questerHandler.getQuester((Player)damager).attackEntity(damagee, evente);
			}

			if (!event.isCancelled()) {				// Damage still in effect
				
				if (damagee instanceof Player) {	// Victim is a player (Quester?) (parse defensive abilities and health changes)
					if (MineQuest.questerHandler.getQuester((Player)damagee) != null) {
						MineQuest.questerHandler.getQuester((Player)damagee).defendEntity(damager, evente);
					}
				} else if ((damagee instanceof LivingEntity) && 
						(MineQuest.mobHandler.getMob((LivingEntity)damagee) != null)) {
					if (damager instanceof LivingEntity) {	// Defender is a living entity
						evente.setDamage(MineQuest.mobHandler.getMob((LivingEntity)event.getEntity()).defend(evente.getDamage(), 
								(LivingEntity)damager));
					} else {								// Defender is something else (slime)
						evente.setDamage(MineQuest.mobHandler.getMob((LivingEntity)event.getEntity()).defend(evente.getDamage(), 
								null));
					}
				}
			}

			return;
		}
		
		if (event.getEntity() instanceof HumanEntity) {
			if (MineQuest.questerHandler.getQuester((Player)event.getEntity()) != null) {
				MineQuest.questerHandler.getQuester((Player)event.getEntity()).defend(event);
			}
		} else if ((event.getEntity() instanceof LivingEntity) && MineQuest.mobHandler.getMob((LivingEntity)event.getEntity()) != null) {
			MineQuest.mobHandler.getMob((LivingEntity)event.getEntity()).damage(event.getDamage());
        }
	}

	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (!MineQuest.isWorldEnabled(event.getEntity().getWorld())) return;
		if (!(event.getTarget() instanceof LivingEntity)) return;
		LivingEntity target = (LivingEntity) event.getTarget();
		
		if (MineQuest.questerHandler.getQuester(target) != null) {
			MineQuest.questerHandler.getQuester(target).targeted(event);
		}
	}
	
	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.getEntity() == null) return;
		if (!MineQuest.isWorldEnabled(event.getEntity().getWorld())) return;
		if (event.getEntity() instanceof Creeper) {
			MQMob mob = MineQuest.mobHandler.getMob((LivingEntity)event.getEntity());
			if (mob != null) {
				mob.setHealth(0);
			}
		}
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof LivingEntity) {
			MQMob mob = MineQuest.mobHandler.getMob((LivingEntity) entity);
			
			if (mob != null) {
				MineQuest.mobHandler.killMob(mob);
			}
		}
	}
}
