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
package org.monksanctum.MineQuest.Quester;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.World.Town;



public class ChestSet {
	private boolean add;
	private Quester quester;
	private List<Location> chests;
	private int selected;
	
	public ChestSet(Quester quester, String select) {
		this.quester = quester;
		add = false;
		selected = -1;
		chests = new ArrayList<Location>();

		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM chests WHERE name='" + quester.getSName() + "'");
		try {
			while (results.next()) {
				chests.add(new Location(null, results.getInt("x"), results.getInt("y"), results.getInt("z")));
				if (results.getString("town").equals(select)) {
					selected = chests.size() - 1;
				}
			}
		} catch (SQLException e) {
			MineQuest.log("[TownSpawn] [ChestSet] Error: reading query");
		}
	}

	
	public ChestSet(Quester quester) {
		this.quester = quester;
		add = false;
		selected = -1;
		chests = new ArrayList<Location>();
	}
	

	public String getName() {
		return quester.getName();
	}

	public void clicked(Player player, Block chest) {
		if (add) {
			Town town = MineQuest.townHandler.getTown(chest.getLocation());
			
			if (town != null) {
				int i;
				
				for (i = 0; i < chests.size(); i++) {
					if (town.isWithin(chests.get(i))) {
						player.sendMessage("You already have a stash in " + town.getName());
						add = false;
						
						return;
					}
				}
				
				chests.add(chest.getLocation());
				MineQuest.getSQLServer().aupdate("INSERT INTO chests (name, town, x, y, z) VALUES('" + quester.getSName() + "', '" + town.getName() + "', '" 
						+ chest.getX() + "', '" 
						+ chest.getY() + "', '" + chest.getZ() + "')");
			} else {
				int i;
				boolean flag = false;
				
				for (i = 0; i < chests.size(); i++) {
					Town this_town = MineQuest.townHandler.getTown(chests.get(i));
					if (this_town == null) {
						flag = true;
					}
				}
				if (!flag) {
					chests.add(chest.getLocation());
					MineQuest.getSQLServer().update("INSERT INTO chests (name, town, x, y, z) VALUES('" + quester.getSName() + "', '" + "none" + "', '" + chest.getX() + "', '" 
							+ chest.getY() + "', '" + chest.getZ() + "')");
				} else {
					player.sendMessage("You already have a stash outside of towns");
				}
			}
		}
		add = false;
		int i;
		
		for (i = 0; i < chests.size(); i++) {
			if (((int)chest.getX() == (int)chests.get(i).getX()) 
					&& ((int)chest.getY() == (int)chests.get(i).getY()) 
					&& ((int)chest.getZ() == (int)chests.get(i).getZ())) {
				if (selected != i) {
					setSelected(i);
				}
				
				return;
			}
		}
	}

	public void setSelected(int i) {
		if (selected != -1) {
			moveContents(getChest(chests.get(i)).getInventory(), getChest(chests.get(selected)).getInventory());
		}

		selected = i;
		Town town = MineQuest.townHandler.getTown(chests.get(i));
		if (((town != null) && (MineQuest.getSQLServer().update("UPDATE questers SET selected_chest='" + town.getName() 
					+ "' WHERE name='" + quester.getSName() + "'") == -1))) {
			MineQuest.log("[ChestSet] Error: Unable to update selected chest");
			if (selected != -1) {
				MineQuest.log("[ChestSet] Performing chest dump");
				Inventory inven = getChest(chests.get(selected)).getInventory();
				for (i = 0; i < inven.getContents().length; i++) {
					MineQuest.log("[ChestSet] Item " + inven.getItem(i).getTypeId() + " " 
							+ inven.getItem(i).getAmount());
				}
			} else {
				MineQuest.log("[TownSpawn] [ChestSet] No chest select - no dump available");
			}
		} else if ((town == null) && ((MineQuest.getSQLServer().update("UPDATE questers SET selected_chest='" 
				+ "none" + "' WHERE name='" + quester.getSName() + "'") == -1))) {
			MineQuest.log("[ChestSet] Error: Unable to update selected chest");
			if (selected != -1) {
				MineQuest.log("[ChestSet] Performing chest dump");
				Inventory inven = getChest(chests.get(selected)).getInventory();
				for (i = 0; i < inven.getContents().length; i++) {
					MineQuest.log("[ChestSet] Item " + inven.getItem(i).getTypeId() + " " 
							+ inven.getItem(i).getAmount());
				}
			} else {
				MineQuest.log("[ChestSet] No chest select - no dump available");
			}
		}
	}

	private Chest getChest(Location location) {
		location.setWorld(quester.getPlayer().getWorld());
		Block block = location.getWorld().getBlockAt(location);
		if (block.getType() == Material.CHEST) {
			Chest chest = new CraftChest(block);
			
			return chest;
		} else {
			return null;
		}
	}


	private void moveContents(Inventory to, Inventory from) {
		int i;
		ItemStack[] new_contents = new ItemStack[from.getContents().length];
		
		for (i = 0; i < new_contents.length; i++) {
			new_contents[i] = from.getContents()[i];
		}
		to.setContents(new_contents);
		from.clear();
	}

	public void add(Player player) {
		if (!add) {
			player.sendMessage("Adding next chest opened (please use empty chest, may destroy contents)");
			add = true;
		} else {
			player.sendMessage("Already adding a chest...");
		}
		
		return;
	}
	
	public void rem(Player player, Town town) {
		int i;
		
		for (i = 0; i < chests.size(); i++) {
			if ((town == null) && (MineQuest.townHandler.getTown(chests.get(i)) == null)) {
				if (chests.size() > 1) {
					if (i > 0) {
						setSelected(0);
					} else {
						setSelected(1);
						selected = 0;
					}
				}
				MineQuest.getSQLServer().update(
						"DELETE FROM chests WHERE town='" + "none"
								+ "' AND name='" + quester.getSName() + "'");
				chests.remove(i);
				player.sendMessage("Chest is now longer instance of stash");
				return;
			} else if ((town != null) && (town.isWithin(chests.get(i)))) {
				if (chests.size() > 1) {
					if (i > 0) {
						setSelected(0);
					} else {
						setSelected(1);
						selected = 0;
					}
				}
				MineQuest.getSQLServer().update(
						"DELETE FROM chests WHERE town='" + town.getName()
								+ "' AND name='" + quester.getSName() + "'");
				chests.remove(i);
				player.sendMessage("Chest is now longer instance of stash");
				return;
			}
		}
		
		player.sendMessage("You have no stash in this town currently");
		return;
	}

	public void cancelAdd(Player player) {
		if (add) {
			add = false;
			player.sendMessage("Adding cancelled");
		} else {
			player.sendMessage("You were not adding a chest...");
		}
		
		return;
	}

}