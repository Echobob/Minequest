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

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Ability.Ability;
import org.monksanctum.MineQuest.Event.NormalEvent;
import org.monksanctum.MineQuest.Event.SpecialMobHandler;
import org.monksanctum.MineQuest.Quester.SkillClass.SkillClass;

public class SpecialMob extends MQMob {
	protected boolean half;
	protected Random generator;
	protected int id;

	public SpecialMob(LivingEntity entity) {
		super(entity);
		half = false;
		generator = new Random();
		
		id = MineQuest.getEventQueue().addEvent(new SpecialMobHandler(1, this));
	}

	@Override
	public double dodgeChance() {
		return .05;
	}

	@Override
	public void dropLoot() {
		if (entity instanceof Zombie) {
			entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(39, 2));
		} else if (entity instanceof Spider) {
			entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(89, 6));
		} else if (entity instanceof Skeleton) {
			entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(331, 6));
		} else if (entity instanceof Creeper) {
			entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(46));
		}

	}
	
	@Override
	public void cancel() {
		MineQuest.getEventQueue().cancel(id);
		NormalEvent.count--;
	}

	@Override
	public int attack(int amount, LivingEntity player) {
		if (entity instanceof Zombie) {
			if (generator.nextDouble() < .8) {
				MineQuest.questerHandler.getQuester((Player)player).poison();
			}
		} else if (entity instanceof Spider) {
			if (generator.nextDouble() < .2) {
				Ability trap = Ability.newAbility("Trap", null);
				trap.castAbility(null, player.getLocation(), player);
			}
		} else if (entity instanceof Skeleton) {
			double num = generator.nextDouble();
			SkillClass skill = SkillClass.newShell(MineQuest.config.skeleton_type);
			List<Ability> abilities = Ability.newAbilities(skill);
			int index = (int)(num * abilities.size());
			
			if (!abilities.get(index).getName().equals("Trap")) {
				abilities.get(index).castAbility(null, player.getLocation(), player);
			}
		} else if (entity instanceof Creeper) {
			Location location = new Location(entity.getWorld(), (int) entity.getLocation().getX(), 
					Ability.getNearestY(entity.getWorld(), (int) entity.getLocation().getX(), (int) entity.getLocation().getY(),
					(int) entity.getLocation().getZ()), (int) entity.getLocation().getZ());
			Block nblock = entity.getWorld().getBlockAt(location);
			nblock.setType(Material.TNT);
		}
		
		amount *= 2;
		
		return amount;
	}

	@Override
	public int defend(int damage, LivingEntity player) {
		if (entity instanceof Zombie) {
			if (generator.nextDouble() < .10) {
				double rot = entity.getLocation().getYaw();
				while (rot < 0)	rot += 360;

				if ((rot < 45) || (rot > 315)) {
					((Player)player).sendMessage("Zombie Dodged");
					entity.teleport(new Location(entity.getWorld(),
							entity.getLocation().getX() - 1,
							Ability.getNearestY(entity.getWorld(), (int) entity.getLocation().getX(),
									(int) entity.getLocation().getY(), (int) entity.getLocation().getZ()),
									entity.getLocation().getZ()));
				} else if ((rot > 45) && (rot < 135)) {
					((Player)player).sendMessage("Zombie Dodged");
					entity.teleport(new Location(entity.getWorld(),
							entity.getLocation().getX(),
							Ability.getNearestY(entity.getWorld(), (int) entity.getLocation().getX(),
									(int) entity.getLocation().getY(), (int) entity.getLocation().getZ()),
									entity.getLocation().getZ() - 1));
				} else if ((rot > 135) && (rot < 225)) {
					((Player)player).sendMessage("Zombie Dodged");
					entity.teleport(new Location(entity.getWorld(),
							entity.getLocation().getX() + 1,
							Ability.getNearestY(entity.getWorld(), (int) entity.getLocation().getX(),
									(int) entity.getLocation().getY(), (int) entity.getLocation().getZ()),
									entity.getLocation().getZ()));
				} else {
					((Player)player).sendMessage("Zombie Dodged");
					entity.teleport(new Location(entity.getWorld(),
							entity.getLocation().getX(),
							Ability.getNearestY(entity.getWorld(), (int) entity.getLocation().getX(),
									(int) entity.getLocation().getY(), (int) entity.getLocation().getZ()),
									entity.getLocation().getZ() + 1));
				}
				return 0;
			}
		}

		if (damage == 1) {
			if (!half) {
				half = true;
			} else {
				if ((entity.getHealth() - 1) <= 0) {
					dead = true;
				}
				half = false;
				return 1;
			}
		}
		if ((entity.getHealth() - (damage / 2)) <= 0) {
			dead = true;
		}
		return damage / 2;
	}
	
	@Override
	public void damage(int i) {
		if (i == 1) {
			if (!half) {
				half = true;
			} else {
				half = false;
				super.damage(1);
			}
		}
		super.damage(i / 2);
	}

}
