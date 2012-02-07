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
package org.monksanctum.MineQuest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.martin.bukkit.npclib.NPCManager;
import org.monksanctum.MineQuest.Ability.Ability;
import org.monksanctum.MineQuest.Configuration.AbilityConfigManager;
import org.monksanctum.MineQuest.Configuration.CombatClassConfig;
import org.monksanctum.MineQuest.Configuration.HealItemConfig;
import org.monksanctum.MineQuest.Configuration.ResourceClassConfig;
import org.monksanctum.MineQuest.Economy.NPCStringConfig;
import org.monksanctum.MineQuest.Event.DelayedSQLEvent;
import org.monksanctum.MineQuest.Event.EventQueue;
import org.monksanctum.MineQuest.Listener.MineQuestBlockListener;
import org.monksanctum.MineQuest.Listener.MineQuestEntityListener;
import org.monksanctum.MineQuest.Listener.MineQuestPlayerListener;

import org.monksanctum.MineQuest.Listener.MineQuestWorldListener;
import org.monksanctum.MineQuest.Quest.FullParty;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quester.Quester;
import org.monksanctum.MineQuest.Quester.SkillClass.SkillClass;
import org.monksanctum.MineQuest.World.Town;

//Vault imports. Used to find and hook into permissions and economy. 
import net.milkbowl.vault.economy.*;
import net.milkbowl.vault.permission.*;


/**
 * This is the main class of MineQuest. It holds static lists of players in the server,
 * Towns in the server, Properties owned by players, and in the future will hold lists
 * of quest status information. It has public static methods to access all of these
 * including the bukkit server.
 *  
 * @author jmonk
 *
 */

public class MineQuest extends JavaPlugin {
	private static EventQueue eventQueue;
	private static NPCManager npc_m;
	private static NPCStringConfig npc_strings = new NPCStringConfig();
	private static Quest[] quests;
	private static Server server;
//	private MineQuestVehicleListener vl;
//	private MineQuestWorldListener wl;
	public static ConfigHandler config;
	public static MobHandler mobHandler;
	public static Permission permission = null;
	public static Economy economy = null;
	
	
	/**
	 * This function adds a quest to the list of quests that MineQuest knows
	 * about. Any quest started from an external source should be added using
	 * this function.
	 * 
	 * @param quest Quest to add
	 */
	public static void addQuest(Quest quest) {
		Quest[] new_quests = new Quest[quests.length + 1];
		int i = 0;
		for (Quest qst : quests) {
			new_quests[i++] = qst;
		}
		
		new_quests[i] = quest;
		
		quests = new_quests;
	}
	
	/**
	 * This function should be used any time any other plugin wants to damage
	 * any living entity. This function will automatically determine whether the
	 * entity is a quester or a mob or not tracked by MQ at all and assign
	 * damage as needed.
	 * 
	 * @param entity Entity to take the damage
	 * @param i Amount of damage
	 */
	public static void damage(LivingEntity entity, int i) {
		if (entity instanceof HumanEntity) {
			Quester quester = questerHandler.getQuester((HumanEntity)entity);
			quester.setHealth(quester.getHealth() - i);
		} else if (mobHandler.getMob(entity) != null) {
			mobHandler.getMob(entity).damage(i);
		} else {
			int newHealth = entity.getHealth() - i;
			
			if (newHealth <= 0) newHealth = 0;
			
			entity.setHealth(newHealth);
		}
	}

	/**
	 * This function should be used any time any other plugin wants to damage
	 * any living entity. This function will automatically determine whether the
	 * entity is a quester or a mob or not tracked by MQ at all and assign
	 * damage as needed. When possible this call should be used over the other
	 * damage function, as this will cause mobs to retailiate properly.
	 * 
	 * @param entity Entity to take the damage
	 * @param i Amount of damage
	 * @param source Cause of damage
	 */
	public static void damage(LivingEntity entity, int i, Quester source) {
		if (entity instanceof HumanEntity) {
			Quester quester = questerHandler.getQuester((HumanEntity)entity);
			quester.setHealth(quester.getHealth() - i);
		} else if (mobHandler.getMob(entity) != null) {
			mobHandler.getMob(entity).damage(i, source);
		} else {
			if (source != null) {
				entity.damage(i, source.getPlayer());
			} else {
				entity.damage(i, null);
			}
		}
	}

	/**
     * This is a utility for various parts of MineQuest to calculate
     * the distance between two locations.
     * 
     * @param loc1 First Location
     * @param loc2 Second Location
     * @return Distance between first and second locations
     */
	static public double distance(Location loc1, Location loc2) {
		double x, y, z;
		if ((loc1.getWorld() != null) && (loc2.getWorld() != null)) {
			if (!loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
				return 10000;
			}
		}
		
		x = loc1.getX() - loc2.getX();
		y = loc1.getY() - loc2.getY();
		z = loc1.getZ() - loc2.getZ();
		
		return Math.sqrt(x*x + y*y + z*z);
	}

	/**
	 * This has MineQuest download the file from the specified URL to the
	 * specified location. Used for downloading templates and updated abilities
	 * files.
	 * 
	 * @param url Location of source file
	 * @param file Target of download
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void downloadFile(String url, String file) throws MalformedURLException, IOException {
		BufferedInputStream in = new BufferedInputStream(
				new java.net.URL(url).openStream());
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
		byte data[] = new byte[1024];
		int size;

		while ((size = in.read(data, 0, 1024)) >= 0) {
			bout.write(data, 0, size);
		}

		bout.close();
		in.close();
	}

	public static AbilityConfigManager getAbilityConfiguration() {
		return config.ability_config;
	}
	
	public static CombatClassConfig getCombatConfig() {
		return config.combat_config;
	}
	
	/**
     * Gets the EventParser being used by MineQuest.
     * 
     * @return EventParser
     */
	
    static public EventQueue getEventQueue() {
    	return eventQueue;
    }
//old Iconomy code. To be removed later
    
/*	public static iConomy getIConomy() {
		return IConomy;
	}

	public static boolean getIsConomyOn() {
		return IConomy != null;
	}
*/
    
    public Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) 
        {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	/**
	 * Determines the next available ability id in the abilities SQL table.
	 * 
	 * @return next available id
	 */
	public static int getNextAbilId() {
		int num = 0;
		try {
			ResultSet results = config.sql_server.query("SELECT * FROM abilities");
			while (results.next()) {
				num++;
			}
		} catch (SQLException e) {
			log("Unable to get max ability id");
		}
		
		return num;
	}

	public static NPCManager getNPCManager() {
		return npc_m;
	}
	
	public static NPCStringConfig getNPCStringConfiguration() {
		return npc_strings;
	}
	
	 public Boolean setupPermissions()
	    {
	        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
	        if (permissionProvider != null) {
	            permission = permissionProvider.getProvider();
	        }
	        return (permission != null);
	    }
	
	public static Quest[] getQuests() {
		return quests;
	}
	
	public static ResourceClassConfig getResourceConfig() {
		return config.resource_config;
	}
	
	/**
	 * Gets an interface to the mysql server being used by
	 * MineQuest.
	 * 
	 * @return mysql_interface of MineQuest DB
	 */
	public static MysqlInterface getSQLServer() {
		return config.sql_server;
	}
	
	/**
	 * Returns the Bukkit Server.
	 * 
	 * @return Bukkit Server
	 */
	public static Server getSServer() {
		return server;
	}
	
	/**
	 * Determines if all three axis of loc have higher value
	 * than loc2.
	 * 
	 * @param loc Larger Location
	 * @param loc2 Smaller Location
	 * @return Boolean true if loc is greater
	 */
	public static boolean greaterLoc(Location loc, Location loc2) {
		if (loc.getX() < loc2.getX()) {
			return false;
		}
		if (loc.getY() < loc2.getY()) {
			return false;
		}
		if (loc.getZ() < loc2.getZ()) {
			return false;
		}
		return true;
	}

	public static TownHandler townHandler;
	
	/**
	 * Determines if a Quester is a Mayor of any town.
	 * Used to determine permissions for creation of towns.
	 * 
	 * @param quester Quester to Test if Mayor
	 * @return Boolean true if Quester is a Mayor
	 */
	public static boolean isMayor(Quester quester) {
		if (quester.equals(MineQuest.config.server_owner)) {
			return true;
		}
		if (quester.getPlayer() != null) {
			if (isPermissionsEnabled()) {
				if (permission.playerHas(quester.getPlayer(), "MineQuest.Mayor")) {
					return true;
				}
			}
			
			if (quester.getPlayer().isOp()) {
				return true;
			}
		}
		
		for (Town t : townHandler.towns) {
			if (t.getTownProperty().getOwner().equals(quester)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Uses Permissions and MQ Config to determine if a Player should have MQ
	 * enabled.
	 * 
	 * @param player Player in question
	 * @return true if enabled
	 */
	public static boolean isMQEnabled(Player player) {
		if (!isWorldEnabled(player.getWorld())) {
			return false;
		}
		
		if (isPermissionsEnabled()) {
			if (!permission.playerHas(player.getWorld(), player.getName(), "MineQuest.Quester")) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Determines if a material is considered "open" or meaning a NPC should 
	 * through the block.
	 * 
	 * @param type Material to check
	 * @return true for a "open" material
	 */
	public static boolean isOpen(Material type) {
		if (type == Material.AIR) {
			return true;
		}
		if (type == Material.TORCH) {
			return true;
		}
		if (type == Material.SNOW) {
			return true;
		}
		if (type == Material.FIRE) {
			return true;
		}
		if (type == Material.SIGN) {
			return true;
		}
		if (type == Material.WALL_SIGN) {
			return true;
		}
		if (type == Material.SIGN_POST) {
			return true;
		}
		if (type == Material.FENCE) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isPermissionsEnabled() {
		return permission != null;
	}
	
	/**
	 * Checks MQ Config to determine whether MQ should be enabled on this world
	 * and should effect events.
	 * 
	 * @param world World in question
	 * @return true if enabled
	 */
	public static boolean isWorldEnabled(World world) {
		for (String name : MineQuest.config.disable_worlds) {
			if (world.getName().equals(name)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Gets a string containing the spell components for a given ability.
	 * 
	 * @param string Ability Name
	 * @return String of Components
	 */
	public static String listSpellComps(String string) {
		Ability ability = Ability.newAbility(string, null);
		String ret = new String();
		
		if (ability == null) {
			ret = string + " is not a valid ability";
			return ret;
		}
		ability.setSkillClass(SkillClass.newShell(MineQuest.getAbilityConfiguration().getSkillClass(string)));
		
		if (MineQuest.config.spell_comp) {
			for (ItemStack item : reduce(ability.getConfigSpellComps())) {
				ret = ret + item.getAmount() + " " + item.getType().toString() + " ";
			}
		}
		if (MineQuest.config.mana) {
			ret = ret + " " + ability.getRealManaCost() + " Mana";
		}
		
		return ret;
	}
	
	/**
	 * Prints to screen the message preceded by [MineQuest].
	 * 
	 * @param string Message to Print
	 */
	public static void log(String string) {
		//log.info("[MineQuest] " + string);
		System.out.println("[MineQuest] " + string);
	}

	/**
	 * Uses MQ config and permissions to determine if MQ damage should be 
	 * enabled for a specific quester.
	 * 
	 * @param quester Quester in Question
	 * @return true if enabled
	 */
	public static boolean mqDamageEnabled(Quester quester) {
		if (!MineQuest.config.mq_damage_system) {
			return false;
		}
		
		if (isPermissionsEnabled() && (quester.getPlayer() != null)) {
			if (permission.playerHas(quester.getPlayer(), "MineQuest.NormalHealth")) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Does a reduction on a list of materials for spell components to add
	 * together materials of the same type.
	 * 
	 * @param manaCost Spell Components
	 * @return Reduced Spell Components
	 */
	public static List<ItemStack> reduce(List<ItemStack> manaCost) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		boolean flag;
		
		for (ItemStack itm : manaCost) {
			flag = false;
			for (ItemStack item : ret) {
				if (item.getTypeId() == itm.getTypeId()) {
					flag = true;
					item.setAmount(item.getAmount() + itm.getAmount());
					break;
				}
			}
			if (!flag) {
				ret.add(itm);
			}
		}
		
		return ret;
	}

	/**
	 * This function removes the quest from MQs list of active quests. This
	 * should be called automatically by any quests that complete on their own.
	 * This should only be called in the result of trying to force remove a
	 * quest.
	 * 
	 * @param quest Quest to remove.
	 */
	public static void remQuest(Quest quest) {
		Quest[] new_quests = new Quest[quests.length - 1];
		int i = 0;
		for (Quest qst : quests) {
			if (!qst.equals(quest)) {
				new_quests[i++] = qst;
			}
		}
		
		quests = new_quests;
	}

	/**
	 * This sets the health of an entity based on a percent of its max health.
	 * Similar to the damage functions this will handle all types of living
	 * entities correctly.
	 * 
	 * @param entity Living entity to affect.
	 * @param percent Percent of max health.
	 */
	public static void setHealth(LivingEntity entity, double percent) {
		if (entity instanceof HumanEntity) {
			questerHandler.getQuester((HumanEntity)entity).setHealth((int)(percent * questerHandler.getQuester((HumanEntity)entity).getHealth()));
		} else if (mobHandler.getMob(entity) != null) {
			mobHandler.getMob(entity).setHealth((int)(mobHandler.getMob(entity).getHealth() * percent));
		} else {
			entity.setHealth((int)(entity.getHealth() * percent));
		}
	}

	/**
	 * This sets the health of an entity based on an integer number specified.
	 * Similar to the damage functions this will handle all types of living
	 * entities correctly.
	 * 
	 * @param entity Living entity to affect.
	 * @param health New health for entity.
	 */
	public static void setHealth(LivingEntity entity, int health) {
		if (entity instanceof HumanEntity) {
			questerHandler.getQuester((HumanEntity)entity).setHealth(health);
		} else if (mobHandler.getMob(entity) != null) {
			mobHandler.getMob(entity).setHealth(health);
		} else {
			if (health > 20) health = 20;
			if (health < 0) health = 0;
			entity.setHealth(health);
		}
	}

	
	private MineQuestBlockListener bl;
	private MineQuestEntityListener el;
	private MineQuestPlayerListener pl;
//	private MineQuestServerListener sl;
	private MineQuestWorldListener wl;
	private String version;
	public static QuesterHandler questerHandler;
	
	public MineQuest() {
		
	}
	
	private void addColumns(String db, String cols[], String types[]) {
		int i;
		for (i = 0; i < cols.length; i++) {
			try {
				if (!column_exists(db, cols[i])) {
					config.sql_server.update("ALTER TABLE " + db + " ADD COLUMN " + cols[i] + " " + types[i], false);
				}
			} catch (SQLException e) {
			}
		}
	}
	
	private boolean column_exists(String db, String column) throws SQLException {
		ResultSet results = config.sql_server.query("SELECT * FROM " + db);
		if (results == null) return false;
		ResultSetMetaData meta = results.getMetaData();
		
		int i;
		for (i = 0; i < meta.getColumnCount(); i++) {
			if (meta.getColumnName(i + 1).equals(column)) {
				return true;
			}
		}
		
		return false;
	}
	private void createDB() throws Exception {
		MineQuest.log("Your DB is too old to determine version");
		MineQuest.log("Upgrading DB to 0.50");
		
		upgradeDB(0, 5);
	}
	
	private void downloadAbilities() throws MalformedURLException, IOException {
		downloadFile("http://www.theminequest.com/download/abilities.jar", "MineQuest/abilities.jar");
	}

	@Override
	public void onDisable() {
		for (Quest quest : quests) {
			quest.issueNextEvents(-1);
		}
		questerHandler.disable();
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!" );
	}

	/**
	 * Sets up an instance of MineQuest. There should never be more than
	 * one instance of MineQuest required. If enabled this method will load all of the
	 * static variables with required information and creating a second instance
	 * will reset all of those, and eventually create inconsistancies in the server.
	 * 
	 * This loads all adjustable parameters from minequest.properties, including
	 * database location and login parameters.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {	
        
        PluginDescriptionFile pdfFile = this.getDescription();
        version = pdfFile.getVersion();		
		server = getServer();
		
        eventQueue = new EventQueue(this);
        
        quests = new Quest[0];
        
        npc_m = new NPCManager(this);
        
        (new File("MineQuest/")).mkdir();
        
        if ((!(new File("MineQuest/abilities.jar")).exists()) || (Ability.getVersion() < 4)) {
        	log("MineQuest/abilities.jar not found or too old: Downloading...");
        	try {
				downloadAbilities();
	        	log("MineQuest/abilities.jar download complete");
			} catch (Exception e) {
				log("Failed to download abilities.jar");
			}
        }
        
//        getEventQueue().addEvent(new RespawnEvent(300000));

        try {
            mobHandler = new MobHandler();
        	config = new ConfigHandler();
            questerHandler = new QuesterHandler();
            mobHandler.checkAllMobs();
            townHandler = new TownHandler();
            
			
			config.sql_server.update("CREATE TABLE IF NOT EXISTS"
							+ " npc (name VARCHAR(30), property VARCHAR(30), value VARCHAR(300))");

			config.sql_server.update("CREATE TABLE IF NOT EXISTS"
							+ " binds (name VARCHAR(30), abil VARCHAR(30), bind INT, bind_2 INT)");

			config.sql_server.update("CREATE TABLE IF NOT EXISTS"
							+ " chests (name VARCHAR(30), town VARCHAR(30), x INT, y INT, z INT)");

			config.sql_server.update("CREATE TABLE IF NOT EXISTS"
					+ " kills (name VARCHAR(30), type VARCHAR(30), count INT)");

			config.sql_server.update("CREATE TABLE IF NOT EXISTS"
							+ " quests (name VARCHAR(30), type VARCHAR(1), file VARCHAR(30))");

			config.sql_server.update("CREATE TABLE IF NOT EXISTS"
							+ " reps (name VARCHAR(30), type VARCHAR(30), amount INT)");

			config.sql_server.update("CREATE TABLE IF NOT EXISTS"
							+ " questers (name VARCHAR(30), health INT, "
							+ "max_health INT, cubes DOUBLE, exp INT, "
							+ "last_town VARCHAR(30), level INT, "
							+ "enabled INT, selected_chest VARCHAR(33), "
							+ "classes VARCHAR(150), mode VARCHAR(30) DEFAULT 'Quester', "
							+ "world VARCHAR(30) DEFAULT 'world', x DOUBLE DEFAULT '0', "
							+ "y DOUBLE DEFAULT '0', z DOUBLE DEFAULT '0', "
							+ "pitch DOUBLE DEFAULT '0', yaw DOUBLE DEFAULT '0')");
			config.sql_server.update("CREATE TABLE IF NOT EXISTS classes (name VARCHAR(30), "
							+ "class VARCHAR(30), exp INT, level INT, abil_list_id INT)");
			config.sql_server.update("CREATE TABLE IF NOT EXISTS abilities (abil_list_id INT, "
							+ "abil0 VARCHAR(30) DEFAULT '0', abil1 VARCHAR(30) DEFAULT '0', "
							+ "abil2 VARCHAR(30) DEFAULT '0',"
							+ "abil3 VARCHAR(30) DEFAULT '0', abil4 VARCHAR(30) DEFAULT '0', "
							+ "abil5 VARCHAR(30) DEFAULT '0', abil6 VARCHAR(30) DEFAULT '0', "
							+ "abil7 VARCHAR(30) DEFAULT '0', abil8 VARCHAR(30) DEFAULT '0', "
							+ "abil9 VARCHAR(30) DEFAULT '0')");
			
			config.sql_server.update("CREATE TABLE IF NOT EXISTS idle (name VARCHAR(30), file VARCHAR(30), type INT, event_id INT, target VARCHAR(180))");

			config.sql_server.update("CREATE TABLE IF NOT EXISTS towns (name VARCHAR(30), x INT, z INT, max_x INT, max_z INT, spawn_x INT, spawn_y INT, spawn_z INT, " +
					"owner VARCHAR(30), height INT, y INT, merc_x DOUBLE, merc_y DOUBLE, merc_z DOUBLE, world VARCHAR(30))");
			
			config.sql_server.update("CREATE TABLE IF NOT EXISTS claims (name VARCHAR(30), x INT, z INT, max_x INT, max_z INT, " +
					"owner VARCHAR(30), height INT, y INT, world VARCHAR(30))");
			
			config.sql_server.update("CREATE TABLE IF NOT EXISTS villages (name VARCHAR(30), x INT, z INT, max_x INT, max_z INT, " +
					"owner VARCHAR(30), height INT, y INT, world VARCHAR(30))");

		} catch (Exception e) {
			MineQuest.log("Unable to initialize configuration");
        	MineQuest.log("Check configuration in MineQuest directory");
        	e.printStackTrace();
        	setEnabled(false);
        	return;
        }
        
//        getEventParser().addEvent(new CheckMQMobs(10000));
		ResultSet results = config.sql_server.query("SELECT * FROM version");
		
		try {
			if ((results == null) || (!results.next())) {
				createDB();
			} else {
				if (!results.getString("version").equals(version)) {
					upgradeDB(results.getString("version"));
				}
				results = config.sql_server.query("SELECT * FROM version");
				results.next();
				MineQuest.log("DB Version: " + results.getString("version"));
			}
		} catch (SQLException e) {
			try {
				createDB();
			} catch (Exception e1) {
				log("Unable to upgrade DB1! - Disabling MineQuest");
//				e.printStackTrace();
//				e1.printStackTrace();
				onDisable();
				return;
			}
		} catch (Exception e) {
			log("Unable to upgrade DB - Disabling MineQuest");
			e.printStackTrace();
			onDisable();
			return;
		}

		List<String> names = new ArrayList<String>();
		results = config.sql_server.query("SELECT * FROM towns");
		List<String> worlds = new ArrayList<String>();
		
		try {
			while (results.next()) {
				names.add(results.getString("name"));
				worlds.add(results.getString("world"));
			}
		} catch (SQLException e) {
			log("Unable to get list of towns");
		}
		
		int i = 0;
		for (String name : names) {
			townHandler.towns.add(new Town(name, getServer().getWorld(worlds.get(i++))));
		}

		bl = new MineQuestBlockListener();
		el = new MineQuestEntityListener();
		pl = new MineQuestPlayerListener();
//		sl = new MineQuestServerListener();
		wl = new MineQuestWorldListener();
//		vl = new MineQuestVehicleListener();
		
        PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvent(Event.Type.PLAYER_JOIN, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_ANIMATION, pl, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_COMBUST, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_TARGET, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, el, Priority.Highest, this);
        pm.registerEvent(Event.Type.CREATURE_SPAWN, el, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_REGAIN_HEALTH, el, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_DAMAGE, bl, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, bl, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, bl, Priority.Normal, this);
 //     pm.registerEvent(Event.Type.PLUGIN_DISABLE, sl, Priority.Monitor, this);
 //     pm.registerEvent(Event.Type.PLUGIN_ENABLE, sl, Priority.Monitor, this);
        pm.registerEvent(Event.Type.CHUNK_LOAD, wl, Priority.Monitor, this);
        pm.registerEvent(Event.Type.CHUNK_UNLOAD, wl, Priority.Monitor, this);
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
		
		if (new File("MineQuest/main.script").exists()) {
			MineQuest.addQuest(new Quest("MineQuest/main.script", new FullParty()));
		}
	}

	private void upgradeDB(int oldVersion, int newVersion) throws Exception {
		String cols[] = null;
		String types[] = null;
		if (oldVersion == 0) {
			cols = new String[] {
					"world",
					"x",
					"y",
					"z",
					"mode",
					"pitch",
					"yaw"
			};
			types = new String[] {
					"varchar(30) DEFAULT 'world'",
					"double DEFAULT '0'",
					"double DEFAULT '0'",
					"double DEFAULT '0'",
					"varchar(30) DEFAULT 'Quester'",
					"double DEFAULT '0'",
					"double DEFAULT '0'"
			};
			
			addColumns("questers", cols, types);
			
			cols = new String[] {
					"merc_x",
					"merc_y",
					"merc_z"
			};

			types = new String[] {
					"double DEFAULT '0'",
					"double DEFAULT '0'",
					"double DEFAULT '0'"
			};

			addColumns("towns", cols, types);
		}
		if (oldVersion < 6) {
			cols = new String[] {
					"mana",
					"max_mana"
			};
			types = new String[] {
					"int DEFAULT '10'",
					"int DEFAULT '10'"
			};
			addColumns("questers", cols, types);
			
			cols = new String[] {
					"world",
			};
			types = new String[] {
					"VARCHAR(30) DEFAULT '" + server.getWorlds().get(0).getName() + "'"
			};
			addColumns("towns", cols, types);
		}
		if (oldVersion < 5) {
			ResultSet results = config.sql_server.query("SELECT * FROM questers");
			List<String> questers = new ArrayList<String>();
			List<Boolean> npc_flag = new ArrayList<Boolean>();
			
			try {
				while (results.next()) {
					questers.add(results.getString("name"));
					if (results.getString("mode").equals("Quester")) {
						npc_flag.add(false);
					} else {
						npc_flag.add(true);
					}
				}
			} catch (SQLException e) {
				log("DB Upgrading failed - Aborting!!");
				onDisable();
				throw new Exception();
			}
			
			int index = 0;
			for (String name : questers) {
				try {
					results = config.sql_server.query("SELECT * FROM " + name);
					List<String> abil = new ArrayList<String>();
					List<Integer> bind = new ArrayList<Integer>();
					List<Integer> bind_2 = new ArrayList<Integer>();
					
					while (results.next()) {
						abil.add(results.getString("abil"));
						bind.add(results.getInt("bind"));
						bind_2.add(results.getInt("bind_2"));
					}
					int i;
					for (i = 0; i < abil.size(); i++) {
						config.sql_server
								.update("INSERT INTO binds (name, abil, bind, bind_2) VALUES('"
										+ name
										+ "', '"
										+ abil.get(i)
										+ "', '"
										+ bind.get(i)
										+ "', '"
										+ bind_2.get(i)
										+ "')");
					}
					config.sql_server.update("DROP TABLE " + name);
				} catch (Exception e) {
				}
				
				try {
					results = config.sql_server.query("SELECT * FROM " + name + "_chests");
					List<String> town = new ArrayList<String>();
					List<Integer> x = new ArrayList<Integer>();
					List<Integer> y = new ArrayList<Integer>();
					List<Integer> z = new ArrayList<Integer>();
					
					while (results.next()) {
						town.add(results.getString("town"));
						x.add(results.getInt("x"));
						y.add(results.getInt("y"));
						z.add(results.getInt("z"));
					}
					int i;
					for (i = 0; i < town.size(); i++) {
						config.sql_server
								.update("INSERT INTO chests (name, town, x, y, z) VALUES('"
										+ name
										+ "', '"
										+ town.get(i)
										+ "', '"
										+ x.get(i)
										+ "', '"
										+ y.get(i)
										+ "', '"
										+ z.get(i)
										+ "')");
					}
					config.sql_server.update("DROP TABLE " + name + "_chests");
				} catch (Exception e) {
				}
				
				try {
					results = config.sql_server.query("SELECT * FROM " + name + "_kills");
					List<String> type = new ArrayList<String>();
					List<Integer> count = new ArrayList<Integer>();
					
					while (results.next()) {
						type.add(results.getString("name"));
						count.add(results.getInt("count"));
					}
					int i;
					for (i = 0; i < type.size(); i++) {
						config.sql_server
								.update("INSERT INTO kills (name, type, count) VALUES('"
										+ name
										+ "', '"
										+ type.get(i)
										+ "', '"
										+ count.get(i)
										+ "')");
					}
					config.sql_server.update("DROP TABLE " + name + "_kills");
				} catch (Exception e) {
				}
				
				try {
					results = config.sql_server.query("SELECT * FROM " + name + "_quests");
					List<String> type = new ArrayList<String>();
					List<String> file = new ArrayList<String>();
					
					while (results.next()) {
						type.add(results.getString("type"));
						file.add(results.getString("file"));
					}
					int i;
					for (i = 0; i < type.size(); i++) {
						config.sql_server
								.update("INSERT INTO quests (name, type, file) VALUES('"
										+ name
										+ "', '"
										+ type.get(i)
										+ "', '"
										+ file.get(i)
										+ "')");
					}
					config.sql_server.update("DROP TABLE " + name + "_quests");
				} catch (Exception e) {
				}
				
				if (npc_flag.get(index++)) {
					try {
						results = config.sql_server.query("SELECT * FROM " + name + "_npc");
						List<String> property = new ArrayList<String>();
						List<String> value = new ArrayList<String>();
						
						while (results.next()) {
							property.add(results.getString("property"));
							value.add(results.getString("value"));
						}
						int i;
						for (i = 0; i < property.size(); i++) {
							config.sql_server
									.update("INSERT INTO npc (name, property, value) VALUES('"
											+ name
											+ "', '"
											+ property.get(i)
											+ "', '"
											+ value.get(i)
											+ "')");
						}
						config.sql_server.update("DROP TABLE " + name + "_npc");
					} catch (Exception e) {
					}
				}
			}
		}
		
		config.sql_server.update("CREATE TABLE IF NOT EXISTS version (version VARCHAR(30))");
		config.sql_server.update("DELETE FROM version");
		config.sql_server.update("INSERT INTO version (version) VALUES('" + version + "')");
		
	}

	private void upgradeDB(String string) throws Exception {
		int oldVersion = 0;
		try {
			oldVersion = (int)(Double.parseDouble(string) * 10);
		} catch (Exception e) {
			MineQuest.log("Could not detect version - Previously running dev?");
		}

		upgradeDB(oldVersion, 5);
	}

	public static void delayUpdate(String string) {
		eventQueue.addEvent(new DelayedSQLEvent(50, string));
	}

	public static Quest getMainQuest() {
		for (Quest quest : quests) {
			if (quest.isMainQuest()) {
				return quest;
			}
		}
		return null;
	}

	public static HealItemConfig getHealthConfiguration() {
		return config.heal_item_config;
	}
}
