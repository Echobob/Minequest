package org.monksanctum.MineQuest;

import java.util.ArrayList;
import java.util.List;

import org.monksanctum.MineQuest.Configuration.AbilityConfigManager;
import org.monksanctum.MineQuest.Configuration.CombatClassConfig;
import org.monksanctum.MineQuest.Configuration.HealItemConfig;
import org.monksanctum.MineQuest.Configuration.ResourceClassConfig;
import org.monksanctum.MineQuest.Configuration.SkillClassConfig;
import org.monksanctum.MineQuest.Event.NoMobs;
import org.monksanctum.MineQuest.Event.NormalEvent;
import org.monksanctum.MineQuest.Event.Absolute.HealEvent;
import org.monksanctum.MineQuest.Event.Absolute.ManaEvent;

public class ConfigHandler {
	public AbilityConfigManager ability_config;
	public int adjustment_multiplier;
	public int cast_ability_exp;
	public int claim_cost;
	public CombatClassConfig combat_config;
	public boolean cubonomy_enable = true;
	public boolean debug_enable = true;
	public boolean deny_non_class;
	public int destroy_block_exp;
	public int destroy_class_exp;
	public int destroy_materials_level;
	public int destroy_non_class_exp;
	public String[] disable_worlds;
	public int exp_class_damage;
	public int exp_damage;
	public boolean half_damage;
	public int heal_event;
	public HealItemConfig heal_item_config;
	public boolean health_spawn_enable;
	public boolean is_claim_restricted;
	public boolean is_village_restricted;
	public int level_health;
    public int level_mana;
    public boolean log_health_change;
    public boolean mana;
	public int mana_event;
	public int maxClass;
	public boolean mayor_restricted;
	public long[] money_amounts;
	public String[] money_names;
	public boolean mq_damage_system;
	public String npc_attack_type;
	public int npc_cost;
	public int npc_cost_class;
	public boolean npc_enabled;
	public long npc_vulnerable_delay;
	public boolean op_restricted;
	public double price_change;
	public ResourceClassConfig resource_config;
	public double sell_percent;
	public String server_owner;
	public String skeleton_type;
	public boolean spawning;
	public boolean spell_comp;
	public MysqlInterface sql_server;
	public String[] starting_classes;
	public int starting_health;
	public int starting_mana;
	public int town_cost;
	public boolean town_enable = true;
	public int[] town_exceptions;
	public boolean town_no_mobs;
	public boolean town_protect;
	public boolean town_respawn;
	public boolean track_destroy;
	public boolean track_kills;
	public int village_cost;
	
	public ConfigHandler() throws Exception {
		heal_event = 0;
		setupMainProperties();
		setupExperienceProperties();
		setupGeneralProperties();
		setupNPCProperties();
		setupEconomoyProperties();
		setupPropertyProperties();
        
		combat_config = new CombatClassConfig();
		resource_config = new ResourceClassConfig();
        
		ability_config = new AbilityConfigManager();
	}
	
	/**
	 * This returns a list of all of the names of classes in the server, both
	 * combat and resource.
	 * 
	 * @return list of names
	 */
	public List<String> getFullClassNames() {
		List<String> names = new ArrayList<String>();
		
		for (String name : combat_config.getClassNames()) {
			names.add(name);
		}
		for (String name : resource_config.getClassNames()) {
			names.add(name);
		}
		return names;
	}

	public int getLevelHealth() {
		return level_health - 1;
	}

	public int getLevelMana() {
		return level_mana - 1;
	}

	public void reloadConfig() {
		MineQuest.log("Loading Economy Properties");
		setupEconomoyProperties();
		MineQuest.log("Loading Experience Properties");
		setupExperienceProperties();
		MineQuest.log("Loading General Properties");
		setupGeneralProperties();
		MineQuest.log("Loading Property Properties");
		setupPropertyProperties();
		MineQuest.log("Loading NPC Properties");
		setupNPCProperties();

		MineQuest.log("Loading Combat Class Config");
        combat_config = new CombatClassConfig();
		MineQuest.log("Loading Resource Class Config");
        resource_config = new ResourceClassConfig();
		MineQuest.log("Loading Ability Config - Warning: will not affect loaded abilities!!");
        ability_config = new AbilityConfigManager();
	}

	public void setSpawning(boolean b) {
		spawning = b;
	}

	public void setupEconomoyProperties() {
		PropertiesFile economy = new PropertiesFile("MineQuest/economy.properties");
		
		sell_percent = economy.getDouble("sell_return", .92);
		price_change = economy.getDouble("price_change", .009);
		
		money_names = economy.getString("money_names", "GC,MC,KC,C").split(",");
		money_amounts = SkillClassConfig.longList(economy.getString("money_amounts", "1000000000,1000000,1000,0"));

		cubonomy_enable = economy.getBoolean("cubonomy_enable", true);
	}

	public void setupExperienceProperties() {
		PropertiesFile experience = new PropertiesFile("MineQuest/experience.properties");

		destroy_class_exp = experience.getInt("destroy_class", 5);
		destroy_non_class_exp = experience.getInt("destroy_non_class", 2);
		destroy_block_exp = experience.getInt("destroy_block", 2);
		adjustment_multiplier = experience.getInt("adjustment_multiplier",
				1);
		exp_damage = experience.getInt("damage", 3);
		cast_ability_exp = experience.getInt("cast_ability", 5);
		exp_class_damage = experience.getInt("class_damage", 5);
	}
    
	public void setupGeneralProperties() {
		PropertiesFile general = new PropertiesFile("MineQuest/general.properties");
		
		town_enable = general.getBoolean("town_enable", true);
		npc_enabled = general.getBoolean("npc_enable", true);
		track_kills = general.getBoolean("track_kills", true);
		track_destroy = general.getBoolean("track_destroy", true);
		town_protect = general.getBoolean("town_protect", true);
		log_health_change = general.getBoolean("log_health_change", true);
		starting_classes = general.getString("starting_classes", "Warrior,Archer,WarMage,PeaceMage,Miner,Digger,Lumberjack,Farmer").split(",");
		skeleton_type = general.getString("skeleton_type", "WarMage");
		half_damage = general.getBoolean("half_damage", true);
		deny_non_class = general.getBoolean("deny_non_class", true);
		debug_enable = general.getBoolean("debug_enable", true);
		health_spawn_enable = general.getBoolean("health_spawn_enable", false);
		mana = general.getBoolean("mana_enable", false);
		spell_comp = general.getBoolean("spell_comp_enable", true);
		boolean slow_heal = general.getBoolean("slow_heal", false);
		if (slow_heal) {
			int amount = general.getInt("slow_heal_amount", 1);
			int delay = general.getInt("slow_heal_delay_ms", 1500);
			heal_event = MineQuest.getEventQueue().addEvent(new HealEvent(delay, amount));
		} else {
			if (heal_event != 0) {
				MineQuest.getEventQueue().cancel(heal_event);
				NormalEvent.count--;
			}
		}
		boolean slow_mana = general.getBoolean("slow_mana", true);
		if (slow_mana) {
			int amount = general.getInt("slow_mana_amount", 1);
			int delay = general.getInt("slow_mana_delay_ms", 1500);
			mana_event = MineQuest.getEventQueue().addEvent(new ManaEvent(delay, amount));
		} else {
			if (mana_event != 0) {
				MineQuest.getEventQueue().cancel(mana_event);
				NormalEvent.count--;
			}
		}
		server_owner = general.getString("mayor", "jmonk");
		disable_worlds = general.getString("disable_worlds", "").split(",");
		starting_health = general.getInt("starting_health", 10);
		level_health = general.getInt("level_health", 4);
		level_mana = general.getInt("level_mana", 4);
		starting_mana = general.getInt("starting_mana", 10);
		town_respawn = general.getBoolean("town_respawn", true);
		String exceptions = general.getString("town_edit_exception", "64,77");
		if (exceptions.contains(",")) {
			String[] split = exceptions.split(",");
			town_exceptions = new int[split.length];
			int i;
			for (i = 0; i < split.length; i++) {
				town_exceptions[i] = Integer.parseInt(split[i]);
			}
		} else {
			try {
				town_exceptions = new int[] {Integer.parseInt(exceptions)};
			} catch (Exception e) {
				town_exceptions = new int[0];
			}
		}
		mq_damage_system = general.getBoolean("mq_health_system", true);
		
		heal_item_config = new HealItemConfig();
	}

	public void setupMainProperties() throws Exception {
		PropertiesFile minequest = new PropertiesFile("MineQuest/main.properties");
		String url, port, db, user, pass;
		
		url = minequest.getString("url", "localhost");
		port = minequest.getString("port", "3306");
		db = minequest.getString("db", "MineQuest/minequest");
		user = minequest.getString("user", "root");
		pass = minequest.getString("pass", "1234");
		maxClass = minequest.getInt("max_classes", 4);
		boolean real = minequest.getBoolean("mysql", false);
		boolean nomobs_main = minequest.getBoolean("no_mobs_main_world", false);
		if (nomobs_main) {
			MineQuest.getEventQueue().addEvent(new NoMobs(5000, "world"));
			MineQuest.mobHandler.noMobs.add("world");
		} else {
			if (MineQuest.mobHandler.noMobs.contains("world")) {
				MineQuest.mobHandler.noMobs.remove("world");
			}
		}
		town_no_mobs = minequest.getBoolean("town_no_mobs", true);
		sql_server = new MysqlInterface(url, port, db, user, pass, minequest.getInt("silent", 1), real);
	}

	public void setupNPCProperties() {
		PropertiesFile npc = new PropertiesFile("MineQuest/npc.properties");

		npc_cost = npc.getInt("npc_cost_level", 1000);
		npc_cost_class = npc.getInt("npc_cost_class", 1000);
		npc_vulnerable_delay = npc.getInt("npc_vulnerable_delay", 30000);
		npc_attack_type = npc.getString("npc_attack_type", "Warrior");
	}

	public void setupPropertyProperties() {
		PropertiesFile property = new PropertiesFile("MineQuest/property.properties");
		mayor_restricted = property.getBoolean("town_mayor_restricted", true);
		op_restricted = property.getBoolean("town_op_restricted", false);
		is_claim_restricted = property.getBoolean("is_claim_restricted", false);
		is_village_restricted = property.getBoolean("is_village_restricted", false);
		town_cost = property.getInt("town_cost", 0);
		village_cost = property.getInt("village_cost", 0);
		claim_cost = property.getInt("claim_cost", 0);
	}
}
