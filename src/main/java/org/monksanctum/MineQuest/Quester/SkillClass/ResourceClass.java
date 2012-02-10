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

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Ability.Ability;
import org.monksanctum.MineQuest.Ability.BreakingAbility;
import org.monksanctum.MineQuest.Configuration.ResourceClassConfig;
import org.monksanctum.MineQuest.Quester.Quester;

public class ResourceClass extends SkillClass {

	public ResourceClass(Quester quester, String type) {
		super(quester, type);
	}
	
	public ResourceClass(String type) {
		// Shell
		super(type);
	}
	
	@Override
	public void blockBreak(BlockBreakEvent event) {
		super.blockBreak(event);
		
		if (isClassItem(event.getBlock().getType())) {
			expAdd(MineQuest.config.destroy_class_exp);
		} else {
			expAdd(MineQuest.config.destroy_non_class_exp);
		}

		if (level >= MineQuest.config.destroy_materials_level) {
			if (isStoneWoodenTool(quester.getPlayer().getItemInHand())) {
				if (isBlockGiveType(event.getBlock().getTypeId())) {
					quester.getPlayer().getInventory().addItem(getItemGive(event.getBlock().getTypeId()));
				}
			}
		}
		
		for (Ability ability : ability_list) {
			if (ability instanceof BreakingAbility) {
				((BreakingAbility)ability).blockBreak(quester, event.getBlock());
			}
		}
	}

	/**
	 * Determines if the item is a stone tool or
	 * a wooden tool.
	 * 
	 * @param itemStack Item to check.
	 * @return True if wooden or stone
	 */
	protected boolean isStoneWoodenTool(ItemStack itemStack) {

		switch (itemStack.getTypeId()) {
		case 272:
		case 268:
		case 274:
		case 270:
		case 275:
		case 271:
		case 273:
		case 269:
		case 292:
		case 290:
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public boolean isClassBlock(Block block) {
		int ids[] = getSkillConfig().getBlocks(type);
		int id = block.getTypeId();
		
		for (int the_id : ids) {
			if (the_id == id) {
				return true;
			}
		}
		
		return false;
	}
	 
	@Override
	public ResourceClassConfig getSkillConfig() {
		return MineQuest.getResourceConfig();
	}

	/**
	 * Get Item stack that should be produced by a given
	 * block type being destroyed.
	 * 
	 * @param type2 Block type destroyed
	 * @return Item stack to go in inventory
	 */
	private ItemStack getItemGive(int type2) {
		switch (type2) {
		case 14: //gold
			return new ItemStack(266, 1);
		case 56: //diamond
			return new ItemStack(264, 1);
		case 73: //red stone
			return new ItemStack(331, 2);
		case 74: //more red stone
			return new ItemStack(331, 2);
		default:
			return null;
		}
	}

}
