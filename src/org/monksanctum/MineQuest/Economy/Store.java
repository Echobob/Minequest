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
package org.monksanctum.MineQuest.Economy;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

/**
 * Store holds all of the information associated with rectangular
 * area in MineQuest where items can be bought and sold based
 * on what items the store supports.
 * 
 * @author jmonk
 *
 */
public class Store {
	protected List<StoreBlock> blocks;
	private Location end;
	private String name;
	private int num_page;
	private Location start;
	
	/**
	 * Creates a blank store not associated with the MySQL Database.
	 * 
	 * @param store_name Name of Store
	 * @param start First Corner of Store
	 * @param end Last Corner of Store
	 */
	public Store(String store_name, Location start, Location end) {
		name = store_name;
		
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Loads a Store from the MineQuest Database.
	 * 
	 * @param name Name of Store
	 * @param town Name of Town
	 */
	public Store(String name, String town) {
		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM " + town + " WHERE name='" + name + "'");
		try {
			results.next();
			int height = results.getInt("height");
			Location start = new Location(null, (double)results.getInt("x"), (double)results.getInt("y"), (double)results.getInt("z"));
			Location end = new Location(null, (double)results.getInt("max_x"), (double)results.getInt("y") + height, (double)results.getInt("max_z"));
			this.name = name;
			
			this.start = start;
			this.end = end;
		} catch (SQLException e) {
			MineQuest.log("Unable to load store " + name + " in " + town);
			return;
		}
	}

	/**
	 * Buy some quantity of a block.
	 * 
	 * @param quester Buyer
	 * @param item_id Type if Block
	 * @param quantity Amount being baught
	 */
	public void buy(Quester quester, int index, int quantity) {
		if (buy(quester, blocks.get(index), quantity)) {
			quester.getPlayer().sendMessage(index + " is not a valid index for this store - Contact Admin to have it added");
		}
	}
	

	/**
	 * Buy some quantity of a block.
	 * 
	 * @param quester Buyer
	 * @param block Type if Block
	 * @param quantity Amount being baught
	 * @return True if block is not valid
	 */
	public boolean buy(Quester quester, StoreBlock block, int quantity) {
		if (block != null) {
			block.buy(quester, quantity);
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Buy some quantity of a block.
	 * 
	 * @param quester Buyer
	 * @param name Type if Block
	 * @param quantity Amount being baught
	 */
	public void buy(Quester quester, String name, int quantity) {
		if (buy(quester, getBlock(name), quantity)) {
			quester.getPlayer().sendMessage(name + " is not a valid block type for this store - Contact Admin to have it added");
		}
	}
	
	/**
	 * 
	 * @param quester
	 * @param item_id
	 * @param quantity
	 * @param buy
	 */
	public void cost(Quester quester, int index, int quantity, boolean buy) {
		if (cost(quester, getBlock(index), quantity, buy)) {
			quester.getPlayer().sendMessage(index + " is not a valid index for this store - Contact Admin to have it added");
		}
	}
	
	public boolean cost(Quester quester, StoreBlock block, int quantity, boolean buy) {
		if (block != null) {
			block.cost(quester, quantity, buy);
			return false;
		}
		
		return true;
	}
	
	public void cost(Quester quester, String name, int quantity, boolean buy) {
		if (cost(quester, getBlock(name), quantity, buy)) {
			quester.getPlayer().sendMessage(name + " is not a valid block type for this store - Contact Admin to have it added");
		}
	}
	
	public void displayPage(Quester quester, int page) {
		Player player = quester.getPlayer();
		int i;
		
		if (page > num_page) {
			player.sendMessage("This store only has " + num_page + " pages");
			return;
		}
		if (page <= 0) {
			page = 1;
		}
		
		String cubes_string = StoreBlock.convert((long)quester.getCubes());
		player.sendMessage(name + ": page " + (page) + " of " + num_page + " - You have " + cubes_string + "C");
		player.sendMessage("     Type - Price - Quantity");
		
    	for (i = 6 * (page - 1); (i < (6 * (page))) && (i < blocks.size()); i++) {
    		blocks.get(i).display(player, i);
    	}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String) {
			return name.equals(obj);
		}
		if (obj instanceof Store) {
			return name.equals(((Store)obj).getName());
		}
		
		return super.equals(obj);
	}
	
	public StoreBlock getBlock(int item_id) {
		int i;
		
		for (i = 0; i < blocks.size(); i++) {
			if (blocks.get(i).getId() == item_id) {
				return blocks.get(i);
			}
		}
		
		return null;
	}
	
	protected StoreBlock getBlock(String name) {
		int i;
		
		for (i = 0; i < blocks.size(); i++) {
			if (blocks.get(i).equals(name)) {
				return blocks.get(i);
			}
		}
		
		return null;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean inStore(Location loc) {
		if (MineQuest.greaterLoc(loc, start) && MineQuest.greaterLoc(end, loc)) {
			return true;
		}
		return false;
		//return MineQuest.distance(loc, location) < radius;
	}
	
	public boolean inStore(HumanEntity player) {
		return inStore(player.getLocation());
	}
	
	public void queryData() {
		ResultSet results;
		
		blocks = new ArrayList<StoreBlock>();
		results = MineQuest.getSQLServer().query("SELECT * FROM " + name + " ORDER BY type");
		try {
			while (results.next()) {
				try {
					blocks.add(new StoreBlock(this, results.getString("type"), results.getInt("quantity"), results.getDouble("price"), results.getInt("item_id")));
				} catch (SQLException e) {
					MineQuest.log("[MineQuest] Unable to query data for block");
					return;
				}
			}
		} catch (SQLException e) {
			MineQuest.log("[MineQuest] Unable to query data for store");
		}
		
		num_page = blocks.size() / 6;
		if ((blocks.size() % 6) > 0) {
			num_page++;
		}
	}
	
	public void sell(Quester quester, int item_id, int quantity) {
		if (sell(quester, getBlock(item_id), quantity)) {
			quester.getPlayer().sendMessage(item_id + " is not a valid block id for this store - Contact Admin to have it added");
		}
	}
	
	public boolean sell(Quester quester, StoreBlock block, int quantity) {
		if (block != null) {
			block.sell(quester, quantity);
			return false;
		}
		
		return true;
	}
	
	public void sell(Quester quester, String name, int quantity) {
		if (sell(quester, getBlock(name), quantity)) {
			quester.getPlayer().sendMessage(name + " is not a valid block type for this store - Contact Admin to have it added");
		}
	}

	public void addBlock(String type, String price, String item_id) {
		MineQuest.getSQLServer().aupdate("INSERT INTO " + name + " (item_id, price, quantity, type) VALUES('" + 
				Integer.parseInt(item_id) + "', '" + Integer.parseInt(price) + "', '0', '" + type + "')");
		blocks.add(new StoreBlock(this, type, 0, Integer.parseInt(price), Integer.parseInt(item_id)));
	}
	
	public void remBlock(String type) {
		MineQuest.getSQLServer().aupdate("DELETE FROM " + name + " WHERE type='" + type + "'");
		blocks.remove(getBlock(type));
	}
	
	public void delete() {
		MineQuest.getSQLServer().aupdate("DROP TABLE " + name);
		MineQuest.getSQLServer().aupdate(
				"DELETE FROM " + MineQuest.townHandler.getTown(start).getName()
						+ " WHERE name='" + name + "'");
	}

	public StoreBlock getBest() {
		int i = 0;
		int amount = blocks.get(0).getQuantity();
		int index = 0;
		
		while (++i < blocks.size()) {
			if (blocks.get(i).getQuantity() > amount) {
				index = i;
				amount = blocks.get(i).getQuantity();
			}
		}
			
		return blocks.get(index);
	}
	
	public StoreBlock getSecondBest() {
		int i = 1;
		int amount = blocks.get(0).getQuantity();
		int index = 0;
		
		if (blocks.size() < 2) {
			return blocks.get(0);
		}
		int second_amount = blocks.get(1).getQuantity();
		int second_index = 0;
		
		if (second_amount > amount) {
			second_amount = blocks.get(0).getQuantity();
			amount = blocks.get(1).getQuantity();
			index = 1;
			second_index = 0;
		}
		
		while (++i < blocks.size()) {
			if (blocks.get(i).getQuantity() > amount) {
				second_index = index;
				second_amount = amount;
				index = i;
				amount = blocks.get(i).getQuantity();
			} else {
				if (blocks.get(i).getQuantity() > second_amount) {
					second_index = i;
					second_amount = blocks.get(i).getQuantity();
				}
			}
		}
		
		return blocks.get(second_index);
	}

	public void setBlockQuant(String type, int amount) {
		StoreBlock block = getBlock(type);
		if (block == null) return;
		
		block.setQuantity(amount);
	}

}
