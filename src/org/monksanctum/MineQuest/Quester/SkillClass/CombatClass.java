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
package org.monksanctum.MineQuest.Quester.SkillClass;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Configuration.CombatClassConfig;
import org.monksanctum.MineQuest.Quester.Quester;

public class CombatClass extends SkillClass {
 
	public CombatClass(Quester quester, String type) {
		super(quester, type);
	}
	
	public CombatClass(String type) {
		// Shell
		super(type);
	}

	/**
	 * Called whenever an attack is made by a Quester. Checks for
	 * bound abilities and if none are found sets the damage of the
	 * event as required.
	 * 
	 * @param defend Entity that is defending the attack
	 * @param event Event created for this attack
	 * @return boolean true
	 */
	public boolean attack(LivingEntity defend, EntityDamageByEntityEvent event) {
		event.setDamage(getDamage());

		expAdd(getExpMob(defend) + MineQuest.questerHandler.getAdjustment() * MineQuest.config.adjustment_multiplier);
		
		return true;
	}

	/**
	 * Get the experience that should be added to a class
	 * for damage to a given Entity.
	 * 
	 * @param defend Entity being damaged.
	 * @return Amount of experience
	 */
	protected int getExpMob(LivingEntity defend) {
		return MineQuest.config.exp_damage;
	}
	
	/**
	 * Gets the amount of damage that this class would do
	 * to a specific entity.
	 * 
	 * @param defend Defending Entity
	 * @return Damage to be dealt
	 */
	public int getDamage() {
		int damage;
		if (!isClassItem(quester.getPlayer().getItemInHand())) {
			damage = 2;
			damage += quester.getLevel() / 10;
			damage += level / 5;
			
			damage /= 2;
		} else {
			int index = getItemIndex(quester.getPlayer().getItemInHand());
			int base = getSkillConfig().getBaseDamage(type)[index];
			int max = getSkillConfig().getMaxDamage(type)[index];
			damage = generator.nextInt(1 + max - base) + base;
			damage += (quester.getLevel() / getSkillConfig().getCharLevelDmgAdj(type)[index]);
			damage += (level / getSkillConfig().getClassLevelDmgAdj(type)[index]);
			
			if (generator.nextDouble() < getCritChance(index)) {
				damage *= 2;
				quester.sendMessage("Critical Hit!");
			}
		}
		
		return damage;
	}
	
	private int getItemIndex(ItemStack itemInHand) {
		int i;
		int[] items = getSkillConfig().getTypes(type);
		
		for (i = 0; i < items.length; i++) {
			if (items[i] == itemInHand.getTypeId()) {
				return i;
			}
		}
		
		return 0;
	}

	@Override
	public CombatClassConfig getSkillConfig() {
		return MineQuest.getCombatConfig();
	}

	/**
	 * Gets the Critical Hit chance for this specific
	 * class.
	 * 
	 * @return Critical Hit Chance
	 */
	private double getCritChance(int index) {
		double chance;
		
		chance = getSkillConfig().getCritChance(type)[index];
		
		if ((getAbility("Deathblow") != null) && (getAbility("Deathblow").isEnabled())) {
			chance *= 2;
		}
		
		return chance;
	}

	/**
	 * Gets the chance for an armor set to block an
	 * attack.
	 * 
	 * @param armor Set of armor to check.
	 * @return Chance of armor blocking attack.
	 */
//	private double armorBlockChance(int[] armor) {
//		switch (armor[0]) {
//		case 302:
//			return .25;
//		case 310:
//			return .20;
//		case 306:
//			return .15;
//		case 314:
//			return .10;
//		case 298: 
//			return .05;
//		}
//
//		return 0;
//	}

}
