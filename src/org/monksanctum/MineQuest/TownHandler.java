package org.monksanctum.MineQuest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import org.monksanctum.MineQuest.World.Claim;
import org.monksanctum.MineQuest.World.Town;
import org.monksanctum.MineQuest.World.Village;

public class TownHandler {
	public List<Claim> claims = new ArrayList<Claim>();
	public Map<String, Location> start_locations = new HashMap<String, Location>();
	public List<Town> towns = new ArrayList<Town>();
	public List<Village> villages = new ArrayList<Village>();
	
	/**
	 * Adds a claim to the MineQuest Server. 
	 * Does not modify mysql database.
	 * 
	 * @param claim Claim to be added
	 */
	public void addClaim(Claim claim) {
		claims.add(claim);
	}

	/**
	 * Adds a town to the MineQuest Server. 
	 * Does not modify mysql database.
	 * 
	 * @param town Town to be added
	 */
	public void addTown(Town town) {
		towns.add(town);
	}

	/**
	 * Adds a village to the MineQuest Server. 
	 * Does not modify mysql database.
	 * 
	 * @param village Village to be added
	 */
	public void addVillage(Village village) {
		villages.add(village);
	}

	/**
	 * Finishes creation of vvillage based on Player
	 * Location.
	 * 
	 * @param player Player Creating Village
	 * @param name Name of Village
	 */
	public void finishClaim(Player player, String name) {
		if (MineQuest.config.is_claim_restricted) {
			
			if (!MineQuest.isPermissionsEnabled() || !MineQuest.permission.playerHas(player, "MineQuest.Claim")) {
				if (!player.isOp()) {
					player.sendMessage("You do not have permission to create a claim");
					return;
				}
			}
		}

		if (start_locations.get(player.getName()) == null) {
			player.sendMessage("You have to use /startcreate first...");
			return;
		}

		Location start = start_locations.get(player.getName());
		Location end = player.getLocation();
		int x, z, max_x, max_z;
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
		if (MineQuest.questerHandler.getQuester(player).canPay((max_x - x) * (max_z - z) * MineQuest.config.claim_cost)) {
			MineQuest.config.sql_server.aupdate("INSERT INTO claims (name, x, z, max_x, max_z, owner, height, y, world) VALUES('"
					+ name + "', '" + x + "', '" + z + "', '" + max_x + "', '" + max_z + "', '" + player.getName() + "', '0', '0', '" + player.getWorld() + "')");
			claims.add(new Claim(name, player.getWorld()));
			player.sendMessage("Claim " + name + " created");
		} else {
			player.sendMessage("You cannot afford to buy a claim of size " + (max_x - x) + " by " + (max_z - z));
			player.sendMessage("It would cost " + ((max_x - x) * (max_z - z) * MineQuest.config.claim_cost));
		}
	}

	/**
	 * Finishes creation of town based on Player
	 * Location.
	 * 
	 * @param player Player Creating Town
	 * @param name Name of Town
	 */
	public void finishTown(Player player, String name) {
		if (MineQuest.config.mayor_restricted) {
			if (!MineQuest.isMayor(MineQuest.questerHandler.getQuester(player))) {
				player.sendMessage("Only mayors are allowed to create towns");
				return;
			}
		} else if (MineQuest.config.op_restricted) {
			if (!player.isOp()) {
				player.sendMessage("Only ops are allowed to create towns");
				return;
			}
		}

		if (start_locations.get(player.getName()) == null) {
			player.sendMessage("You have to use /startcreate first...");
			return;
		}

		Location start = start_locations.get(player.getName());
		Location end = player.getLocation();
		int x, z, max_x, max_z;
		int spawn_x, spawn_y, spawn_z;
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
		spawn_x = (x + max_x) / 2;
		spawn_y = (int)(start.getY() + end.getY()) / 2;
		spawn_z = (z + max_z) / 2;
		if (MineQuest.questerHandler.getQuester(player).canPay((max_x - x) * (max_z - z) * MineQuest.config.town_cost)) {
			MineQuest.config.sql_server.aupdate("INSERT INTO towns (name, x, z, max_x, max_z, spawn_x, spawn_y, spawn_z, owner, height, y, world) VALUES('"
					+ name + "', '" + x + "', '" + z + "', '" + max_x + "', '" + max_z + "', '" + spawn_x + "', '"
					+ spawn_y + "', '" + spawn_z + "', '" + player.getName() + "', '0', '0', '" + player.getWorld() + "')");
			MineQuest.config.sql_server.aupdate("CREATE TABLE IF NOT EXISTS " + name + 
					"(height INT, x INT, y INT, z INT, max_x INT, max_z INT, price INT, name VARCHAR(30), store_prop BOOLEAN)");
			towns.add(new Town(name, player.getWorld()));
			player.sendMessage("Town " + name + " created");
		} else {
			player.sendMessage("You cannot afford to buy a town of size " + (max_x - x) + " by " + (max_z - z));
			player.sendMessage("It would cost " + ((max_x - x) * (max_z - z) * MineQuest.config.town_cost));
		}
	}

	/**
	 * Finishes creation of vvillage based on Player
	 * Location.
	 * 
	 * @param player Player Creating Village
	 * @param name Name of Village
	 */
	public void finishVillage(Player player, String name) {
		if (MineQuest.config.is_village_restricted) {
			if (!MineQuest.isPermissionsEnabled() || !MineQuest.permission.playerHas(player, "MineQuest.Village")) {
				if (!player.isOp()) {
					player.sendMessage("You do not have permission to create a village");
					return;
				}
			}
		}

		if (start_locations.get(player.getName()) == null) {
			player.sendMessage("You have to use /startcreate first...");
			return;
		}

		Location start = start_locations.get(player.getName());
		Location end = player.getLocation();
		int x, z, max_x, max_z;
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
		if (MineQuest.questerHandler.getQuester(player).canPay((max_x - x) * (max_z - z) * MineQuest.config.village_cost)) {
			MineQuest.config.sql_server.aupdate("INSERT INTO villages (name, x, z, max_x, max_z, owner, height, y, world) VALUES('"
					+ name + "', '" + x + "', '" + z + "', '" + max_x + "', '" + max_z + "', '" + player.getName() + "', '0', '0', '" + player.getWorld() + "')");
			villages.add(new Village(name, player.getWorld()));
			player.sendMessage("Village " + name + " created");
		} else {
			player.sendMessage("You cannot afford to buy a village of size " + (max_x - x) + " by " + (max_z - z));
			player.sendMessage("It would cost " + ((max_x - x) * (max_z - z) * MineQuest.config.village_cost));
		}
	}

	/**
	 * Gets a claim that a specific player is within.
	 * 
	 * @param player Player within claim
	 * @return claim that player is in or NULL if none exists
	 */
	public Claim getClaim(HumanEntity player) {
		return getClaim(player.getLocation());
	}
	
	/**
	 * Gets a claim that a specific location is within.
	 * 
	 * @param loc Location within claim.
	 * @return claim that location is in or NULL is none exists
	 */
	public Claim getClaim(Location loc) {
		int i;
		
		for (i = 0; i < claims.size(); i++) {
			if (claims.get(i).isWithin(loc)) {
				return claims.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets a claim based on name of the claim.
	 * 
	 * @param name Name of the claim
	 * @return claim with Name name or NULL is none exists
	 */
	public Claim getClaim(String name) {
		int i;
		
		for (i = 0; i < claims.size(); i++) {
			if (claims.get(i).equals(name)) {
				return claims.get(i);
			}
		}
		
		return null;
	}

	/**
	 * Gets the list of Claims in the server.
	 * 
	 * @return List of Claims
	 */
	public List<Claim> getClaims() {
		return claims;
	}
	
	/**
	 * Returns whatever town has the closest spawn point to
	 * the Location.
	 * 
	 * @param to Location
	 * @return Closest Town
	 */
	public Town getNearestTown(Location to) {
		if (towns.size() == 0) return null;
		
		Town town = towns.get(0);
		int i;
		
		for (i = 1; i < towns.size(); i++) {
			if (MineQuest.distance(to, town.getLocation()) > MineQuest.distance(to, towns.get(i).getLocation())) {
				town = towns.get(i);
			}
		}
		
		return town;
	}
	
	/**
	 * Gets a town that a specific player is within.
	 * 
	 * @param player Player within town
	 * @return Town that player is in or NULL if none exists
	 */
	public Town getTown(HumanEntity player) {
		return getTown(player.getLocation());
	}
	
	/**
	 * Gets a town that a specific location is within.
	 * 
	 * @param loc Location within town.
	 * @return Town that location is in or NULL is none exists
	 */
	public Town getTown(Location loc) {
		int i;
		
		for (i = 0; i < towns.size(); i++) {
			if (towns.get(i).isWithin(loc)) {
				return towns.get(i);
			}
		}
		
		return null;
	}

	/**
	 * Gets a town based on name of the town.
	 * 
	 * @param name Name of the town
	 * @return Town with Name name or NULL is none exists
	 */
	public Town getTown(String name) {
		int i;
		
		for (i = 0; i < towns.size(); i++) {
			if (towns.get(i).equals(name)) {
				return towns.get(i);
			}
		}
		
		return null;
	}

	/**
	 * Gets the list of towns in the server.
	 * 
	 * @return List of towns
	 */
	public List<Town> getTowns() {
		return towns;
	}
	
	/**
	 * Gets a village that a specific player is within.
	 * 
	 * @param player Player within village
	 * @return village that player is in or NULL if none exists
	 */
	public Village getVillage(HumanEntity player) {
		return getVillage(player.getLocation());
	}
	
	/**
	 * Gets a village that a specific location is within.
	 * 
	 * @param loc Location within village.
	 * @return village that location is in or NULL is none exists
	 */
	public Village getVillage(Location loc) {
		int i;
		
		for (i = 0; i < villages.size(); i++) {
			if (villages.get(i).isWithin(loc)) {
				return villages.get(i);
			}
		}
		
		return null;
	}

	/**
	 * Gets a village based on name of the village.
	 * 
	 * @param name Name of the village
	 * @return village with Name name or NULL is none exists
	 */
	public Village getVillage(String name) {
		int i;
		
		for (i = 0; i < towns.size(); i++) {
			if (towns.get(i).equals(name)) {
				return towns.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the list of villages in the server.
	 * 
	 * @return List of villages
	 */
	public List<Village> getVillages() {
		return villages;
	}
	
	/**
	 * Removes a Town from the MineQuester Server.
	 * Does not modify mysql database.
	 * 
	 * @param name Name of Town to remove
	 */
	public void remTown(String name) {
		towns.remove(getTown(name));
	}

	/**
	 * Removes a Town from the MineQuester Server.
	 * Does not modify mysql database.
	 * 
	 * @param town Town to remove
	 */
	public void remTown(Town town) {
		towns.remove(town);
	}
	
	/**
	 * Starts the creation of town based on Player
	 * Location.
	 * 
	 * @param player Player Creating the Town
	 */
	public void startCreate(Player player) {
		start_locations.put(player.getName(), player.getLocation());
	}

}
