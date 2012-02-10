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
package org.monksanctum.MineQuest.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Economy.NPCSignShop;
import org.monksanctum.MineQuest.Economy.Store;
import org.monksanctum.MineQuest.Event.CheckMobEvent;
import org.monksanctum.MineQuest.Quester.NPCMode;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class Town extends Village {
	private Location npc_spawn;
	private List<Property> properties;
	private Location spawn;
	private Location start;
	private List<NPCSignShop> stores;
	
	public Town(String name, World world) {
		super(name, world);
		ResultSet results;
		this.name = name;
		
		properties = new ArrayList<Property>();
		stores = new ArrayList<NPCSignShop>();
		
		results = MineQuest.getSQLServer().query("SELECT * FROM " + name);
		try {
			while (results.next()) {
				int height = results.getInt("height");
				Location start = new Location(world, (double)results.getInt("x"), (double)results.getInt("y"), (double)results.getInt("z"));
				Location end = new Location(world, (double)results.getInt("max_x"), (double)results.getInt("y") + height, (double)results.getInt("max_z"));
				if (results.getBoolean("store_prop")) {
					String store = results.getString("name");
					
					stores.add(new NPCSignShop(store, start, end));
				} else {
					properties.add(new Property(results.getString("name"), start, end, height > 0, results.getLong("price")));
				}
			}
		} catch (SQLException e) {
			MineQuest.log("Error: could not initialize properties of town " + name);
		}
		
		for (Store s : stores) {
			s.queryData();
		}
		
		if (MineQuest.config.town_no_mobs) {
			MineQuest.getEventQueue().addEvent(new CheckMobEvent(this));
		}
	}

	public void addMerc(String name, Quester quester) {
		if (!getTownProperty().canEdit(quester)) {
			quester.sendMessage("You are not authorized to edit town");
			return;
		}
		if (MineQuest.questerHandler.getQuester(name) != null) {
			quester.sendMessage("Quester with that name exists already");
			return;
		}
		if ((npc_spawn.getX() == 0) &&
				(npc_spawn.getY() == 0) &&
				(npc_spawn.getZ() == 0)) {
			quester.sendMessage("Mercenary spawn not set");
			return;
		}
		
		World world = MineQuest.getSServer().getWorlds().get(0);
		MineQuest.questerHandler.addQuester(new NPCQuester(name, NPCMode.FOR_SALE, world, getNPCSpawn()));
		((NPCQuester)MineQuest.questerHandler.getQuester(name)).setTown(getName());
	}

	public void buy(Quester quester, Property prop) {
		if (MineQuest.economy != null)
		{
			quester.withdrawBalance(prop.getPrice());
		}
		else
		{
			quester.setCubes(quester.getCubes() - prop.getPrice());
		} 
		
		prop.setOwner(quester);
		
		MineQuest.getSQLServer().aupdate("UPDATE " + name + " SET name='" + quester.getName() 
				+ "' WHERE x='" + prop.getX() + "' AND z='" + prop.getZ() + "' AND y='" + prop.getY() + "'");
		quester.sendMessage("You now own this property");
	}
	
	public double calcDistance(Location loc) {
		return MineQuest.distance(loc, getTownProperty().getLocation());
	}
	
	public double calcDistance(Player player) {
		return calcDistance(player.getLocation());
	}
	
	public void createProperty(Player player) {
		if (getTownProperty().canEdit(MineQuest.questerHandler.getQuester(player))) {
			player.getName();
			start = player.getLocation();
		} else {
			player.sendMessage("You do not have town permissions");
		}
	}
	
	public void createStore(Player player) {
		if (getTownProperty().canEdit(MineQuest.questerHandler.getQuester(player))) {
			player.getName();
			start = player.getLocation();
		} else {
			player.sendMessage("You do not have town permissions");
		}
	}
	
	public void delete() {
		for (Store store : stores) {
			store.delete();
		}
		MineQuest.getSQLServer().update("DROP TABLE " + name);
		MineQuest.getSQLServer().update(
				"DELETE FROM towns WHERE name='" + name + "'");
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String) {
			return name.equals(obj);
		}
		if (obj instanceof Town) {
			return name.equals(((Town)obj).getName());
		}
		return super.equals(obj);
	}
	
	public void expand(Quester quester) {
		if (!getTownProperty().canEdit(quester)) {
			quester.sendMessage("You cannot edit " + name);
		}
		
		Location loc = quester.getPlayer().getLocation();
		
		if (loc.getBlockX() < getTownProperty().getX()) {
			if ((loc.getBlockZ() < getTownProperty().getZ()) || (loc.getBlockZ() > getTownProperty().getMaxZ())) {
				quester.sendMessage("Can only expand in one direction at a time!");
			}
			getTownProperty().setX(loc.getBlockX());
			quester.sendMessage("Town expanded to min x of " + loc.getBlockX());
		}
		
		if (loc.getBlockX() > getTownProperty().getMaxX()) {
			if ((loc.getBlockZ() < getTownProperty().getZ()) || (loc.getBlockZ() > getTownProperty().getMaxZ())) {
				quester.sendMessage("Can only expand in one direction at a time!");
			}
			getTownProperty().setMaxX(loc.getBlockX());
			quester.sendMessage("Town expanded to max x of " + loc.getBlockX());
		}
		
		if (loc.getBlockZ() < getTownProperty().getZ()) {
			getTownProperty().setZ(loc.getBlockZ());
			quester.sendMessage("Town expanded to min z of " + loc.getBlockZ());
		}
		
		if (loc.getBlockZ() > getTownProperty().getMaxZ()) {
			getTownProperty().setMaxZ(loc.getBlockZ());
			quester.sendMessage("Town expanded to max z of " + loc.getBlockZ());
		}
		
		quester.sendMessage("You are within the x-z getTownProperty() of the town");
		quester.sendMessage("/expand_town can only be used to expand horizontally");
	}
	
	public void finishProperty(Player player, boolean b) {
		if (getTownProperty().canEdit(MineQuest.questerHandler.getQuester(player))) {
			Location end = player.getLocation();
			int x, y, z, max_x, max_z, height;
			if (end.getX() > start.getX()) {
				x = (int)start.getX();
				max_x = (int)end.getX();
			} else {
				x = (int)end.getX();
				max_x = (int)start.getX();
			}
			if (end.getZ() > start.getZ()) {
				z = (int)start.getZ();
				max_z = (int)end.getZ();
			} else {
				z = (int)end.getZ();
				max_z = (int)start.getZ();
			}
			if (end.getY() < start.getY()) {
				y = (int)end.getY();
				height = (int)(start.getY() - end.getY());
			} else {
				y = (int)start.getY();
				height = (int)(end.getY() - start.getY());;
			}
			if (!b) {
				height = 0;
			}

			Location start = new Location(player.getWorld(), (double)x, (double)y, (double)z);
			Location ends = new Location(player.getWorld(), max_x, (double)y + height, max_z);
			properties.add(new Property(null, start, ends, height > 0, 10000000));
			MineQuest.getSQLServer().aupdate("INSERT INTO " + name + 
					" (name, x, y, z, max_x, max_z, height, store_prop, price) VALUES('null', '" + x + "', '" + y + "', '" + z
					+ "', '" + max_x + "', '" + max_z + "', '" + height
					+ "', '0', '10000000')");
			player.sendMessage("Property Created!");
		} else {
			player.sendMessage("You do not have town permissions");
		}
	}

	public void finishStore(Player player, String name) {
		if (getTownProperty().canEdit(MineQuest.questerHandler.getQuester(player))) {
			Location end = player.getLocation();
			int x, y, z, max_x, max_z, height;
			if (end.getX() > start.getX()) {
				x = (int)start.getX();
				max_x = (int)end.getX();
			} else {
				x = (int)end.getX();
				max_x = (int)start.getX();
			}
			if (end.getZ() > start.getZ()) {
				z = (int)start.getZ();
				max_z = (int)end.getZ();
			} else {
				z = (int)end.getZ();
				max_z = (int)start.getZ();
			}
			if (end.getY() < start.getY()) {
				y = (int)end.getY();
				height = (int)(start.getY() - end.getY());
			} else {
				y = (int)start.getY();
				height = (int)(end.getY() - start.getY());;
			}
			MineQuest.getSQLServer().aupdate("INSERT INTO " + this.name + 
					" (name, x, y, z, max_x, max_z, height, store_prop, price) VALUES('"
					+ name + "', '" + x + "', '" + y + "', '" + z
					+ "', '" + max_x + "', '" + max_z + "', '" + height
					+ "', '1', '10000000')");
			MineQuest.getSQLServer().aupdate("CREATE TABLE IF NOT EXISTS " + name + " (item_id INT, price DOUBLE, quantity INT, type VARCHAR(30))");
			stores.add(new NPCSignShop(name, this.name));
			stores.get(stores.size() - 1).queryData();
			player.sendMessage("Store " + name + " created");
		} else {
			player.sendMessage("You do not have town permissions");
		}
	}

	public List<NPCQuester> getAvailableNPCs() {
		List<NPCQuester> npcs = new ArrayList<NPCQuester>();
		
		for (Quester quester : MineQuest.questerHandler.getQuesters()) {
			if (quester instanceof NPCQuester) {
				NPCQuester npc = (NPCQuester)quester;
				if ((npc.getMode() == NPCMode.FOR_SALE) &&
						equals(npc.getNPCTown())) {
					npcs.add((NPCQuester)quester);
				}
			}
		}
		
		return npcs;
	}

	public int[] getCenter() {
		int center[] = new int[2];
		
		center[0] = center_x;
		center[1] = center_z;
		
		return center;
	}
	
	public Location getLocation() {
		return spawn;
	}

	public Location getNPCSpawn() {
		return new Location(npc_spawn.getWorld(),
				npc_spawn.getX() + (new Random()).nextDouble() * 5,
				npc_spawn.getY(),
				npc_spawn.getZ() + (new Random()).nextDouble() * 5,
				0, 0);
	}

	public Property getProperty(Location loc) {
		int i;
		
		for (i = 0; i < properties.size(); i++) {
			if (properties.get(i).inProperty(loc)) {
				return properties.get(i);
			}
		}
		
		return null;
	}

	public Property getProperty(Player player) {
		return getProperty(player.getLocation());
	}

	public Location getSpawn() {
		spawn.setWorld(MineQuest.getSServer().getWorlds().get(0));
		return spawn;
	}

	public NPCSignShop getStore(HumanEntity player) {
		return getStore(player.getLocation());
	}

	public NPCSignShop getStore(Location loc) {
		int i;
		
		for (i = 0; i < stores.size(); i++) {
			if (stores.get(i).inStore(loc)) {
				return stores.get(i);
			}
		}
		
		return null;
	}

	public List<NPCSignShop> getStores() {
		return stores;
	}

	public Property getTownProperty() {
		return (Property)area;
	}
	
	public void remove(Store store) {
		stores.remove(store);
	}

	public void setHeight(Quester quester, int height) {
		if (!getTownProperty().canEdit(quester)) {
			quester.sendMessage("You cannot edit " + name);
		}
		
		getTownProperty().setHeight(height);
		quester.sendMessage("Town set to height of " + height);
	}
	
	public void setMERCSpawn(Location spawn) {
		this.npc_spawn = new Location(spawn.getWorld(),
				spawn.getX(), spawn.getY(), spawn.getZ());
		MineQuest.getSQLServer().aupdate("UPDATE towns SET merc_x='" + (int)spawn.getX() + "', merc_y='" + 
				(int)spawn.getY() + "', merc_z='" + (int)spawn.getZ() + "' WHERE name='" + name + "'");
	}
	
	public void setMinY(Quester quester, int y) {
		if (!getTownProperty().canEdit(quester)) {
			quester.sendMessage("You cannot edit " + name);
		}
		
		getTownProperty().setY(y);
		quester.sendMessage("Town set to min y of " + y);
	}
	
	public void setOwner(String string) {
		if (MineQuest.questerHandler.getQuester(string) != null) {
			MineQuest.getSQLServer().aupdate("UPDATE towns SET owner='" + string + "' WHERE name='" + name + "'");
		}
		getTownProperty().setOwner(MineQuest.questerHandler.getQuester(string));
	}
	
	public void setPrice(Player player, long price) {
		Property prop = getProperty(player);
		if (prop != null) {
			getProperty(player).setPrice(price);
			MineQuest.getSQLServer().aupdate("UPDATE " + name + " SET price='" + price + "' WHERE x='" + 
					prop.getX() + "' AND y='" + prop.getY() + "' AND z='" + prop.getZ() + "'");
			player.sendMessage("Price Set!");
		}
	}

	public void setSpawn(Location spawn) {
		this.spawn = spawn;
		MineQuest.getSQLServer().aupdate("UPDATE towns SET spawn_x='" + (int)spawn.getX() + "', spawn_y='" + 
				(int)spawn.getY() + "', spawn_z='" + (int)spawn.getZ() + "' WHERE name='" + name + "'");
	}

	public void remove(Property prop) {
		MineQuest.getSQLServer().aupdate(
				"DELETE FROM " + name + " WHERE x='" + prop.getX()
						+ "' AND y='" + prop.getY() + "' AND z='" + prop.getZ()
						+ "' AND max_x='" + prop.getMaxX() + "' AND max_z='"
						+ prop.getMaxZ() + "'");
		properties.remove(prop);
	}

	public void getSpawn(ResultSet results, World world) throws SQLException {
		spawn = new Location(world, 
				(double)results.getInt("spawn_x"), 
				(double)results.getInt("spawn_y"), 
				(double)results.getInt("spawn_z"));
		npc_spawn = new Location(MineQuest.getSServer().getWorlds().get(0), 
				results.getDouble("merc_x"), 
				results.getDouble("merc_y"), 
				results.getDouble("merc_z"));
	}
}
