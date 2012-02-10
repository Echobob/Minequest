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


import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

public class MineQuestBlockListener extends BlockListener {
	
//	@Override
//	public void onBlockInteract( event) {
//		MineQuest.log("Block Event " + event.getBlock().getType());
//		super.onBlockInteract(event);
//	}
	
	@Override
	public void onBlockDamage(org.bukkit.event.block.BlockDamageEvent event) {
		if (!MineQuest.isMQEnabled(event.getPlayer())) return;
		Quester quester = MineQuest.questerHandler.getQuester(event.getPlayer());
		
		if (quester.isDebug()) {
			quester.sendMessage(event.getBlock().getX() + " " + 
					event.getBlock().getY() + " " + event.getBlock().getZ()
					 + " " + event.getBlock().getType() + " " + 
					 event.getBlock().getData());
		}
	
		quester.checkItemInHand();
		if (quester.checkItemInHandAbil()) {
			quester.callAbility(event.getBlock());
			event.setCancelled(true);
			return;
		}

		if (quester.canEdit(event.getBlock())) {
			quester.damageBlock(event);
		} else {
			event.setCancelled(true);
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (!MineQuest.isMQEnabled(event.getPlayer())) return;
		Quester quester = MineQuest.questerHandler.getQuester(event.getPlayer());
		
		if (quester.isDebug()) {
			quester.sendMessage(event.getBlock().getX() + " " + 
					event.getBlock().getY() + " " + event.getBlock().getZ()
					 + " " + event.getBlock().getType() + " " + 
					 event.getBlock().getData());
		}
	
		quester.checkItemInHand();
		if (quester.checkItemInHandAbil()) {
			quester.callAbility(event.getBlock());
			event.setCancelled(true);
			return;
		}

		if (quester.canEdit(event.getBlock())) {
			quester.destroyBlock(event);
		} else {
			event.setCancelled(true);
		}
	}
	
//	@Override
//	public void onBlockRightClick(org.bukkit.event.block.BlockRightClickEvent event) {
//		Quester quester = MineQuest.questerHandler.getQuester(event.getPlayer());
//		
//		if (quester.inQuest()) {
//			quester.getQuest().canEdit(quester, event.getBlock());
//		}
//		
//		if (quester.isDebug()) {
//			quester.sendMessage(event.getBlock().getX() + " " + 
//					event.getBlock().getY() + " " + event.getBlock().getZ()
//					 + " " + event.getBlock().getType() + " " + 
//					 event.getBlock().getData());
//		}
//		
//		quester.checkItemInHand();
//		if (quester.checkItemInHandAbil()) {
//			quester.callAbility(event.getBlock());
//			return;
//		}
//		
//		if (event.getBlock().getType() == Material.CHEST) {
//			quester.getChestSet().clicked(event.getPlayer(), event.getBlock());
//		}
//		
//		quester.rightClick(event.getBlock());
//		
//		super.onBlockRightClick(event);
//	}
	
	@Override
	public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
		if (!MineQuest.isMQEnabled(event.getPlayer())) return;
		Quester quester = MineQuest.questerHandler.getQuester(event.getPlayer());
		
		if (quester.isDebug()) {
			quester.sendMessage(event.getBlock().getX() + " " + 
					event.getBlock().getY() + " " + event.getBlock().getZ()
					 + " " + event.getBlock().getType() + " " + 
					 event.getBlock().getData());
		}
		
		quester.checkItemInHand();
		if (quester.checkItemInHandAbil()) {
			quester.callAbility(event.getBlock());
			event.setCancelled(true);
			return;
		}
		
		event.setCancelled(!quester.canEdit(event.getBlock()));
	}

}
