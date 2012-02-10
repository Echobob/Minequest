package org.monksanctum.MineQuest.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.monksanctum.MineQuest.PropertiesFile;
import org.monksanctum.MineQuest.Ability.Ability;

public class AbilityConfigManager {
	private Map<String, Integer> casting_times;
	private Map<String, Integer> required_levels;
	private Map<String, Integer> experience;
	private Map<String, Integer> mana;
	private Map<String, Integer> icons;
	private Map<String, String> cost;
	private Map<String, String> classes;
	private Map<String, int[]> config;
	
	public AbilityConfigManager() {
		PropertiesFile cast;
		PropertiesFile required;
		PropertiesFile exper;
		PropertiesFile cost_config;
		PropertiesFile mana_config;
		PropertiesFile class_config;
		PropertiesFile abil_config;
		casting_times = new HashMap<String, Integer>();
		required_levels = new HashMap<String, Integer>();
		experience = new HashMap<String, Integer>();
		mana = new HashMap<String, Integer>();
		icons = new HashMap<String, Integer>();
		cost = new HashMap<String, String>();
		classes = new HashMap<String, String>();
		config = new HashMap<String, int[]>();
		
		for (Ability ability : Ability.newAbilities(null)) {
			casting_times.put(ability.getName(), ability.getCastTime());
			required_levels.put(ability.getName(), ability.getReqLevel());
			experience.put(ability.getName(), ability.getExp());
			mana.put(ability.getName(), ability.getMana());
			cost.put(ability.getName(), ability.getRealSpellCompsString());
			icons.put(ability.getName(), ability.getIconLoc());
			classes.put(ability.getName(), ability.getSkillClass());
			config.put(ability.getName(), ability.getConfig());
		}

		cast = new PropertiesFile("MineQuest/casting_times.properties");
		required = new PropertiesFile("MineQuest/required_levels.properties");
		exper = new PropertiesFile("MineQuest/experience_given.properties");
		cost_config = new PropertiesFile("MineQuest/cost.properties");
		PropertiesFile icon_config = new PropertiesFile("MineQuest/icons.properties");
		mana_config = new PropertiesFile("MineQuest/mana.properties");
		class_config = new PropertiesFile("MineQuest/abil_classes.properties");
		abil_config = new PropertiesFile("MineQuest/abil_config.properties");
		
		for (String abil : casting_times.keySet()) {
			casting_times.put(abil, cast.getInt(abil, casting_times.get(abil)));
			required_levels.put(abil, required.getInt(abil, required_levels.get(abil)));
			experience.put(abil, exper.getInt(abil, experience.get(abil)));
			mana.put(abil, mana_config.getInt(abil, mana.get(abil)));
			cost.put(abil, cost_config.getString(abil, cost.get(abil)));
			icons.put(abil, icon_config.getInt(abil, icons.get(abil)));
			classes.put(abil, class_config.getString(abil, classes.get(abil)));
			config.put(abil, SkillClassConfig.intList(abil, abil_config.getString(abil, getConfigString(config.get(abil)))));
		}
	}
	
	protected String getConfigString(int[] config) {
		String ret = "";
		
		if (config != null) {
			int i;
			ret = config[0] + "";
			for (i = 1; i < config.length; i++) {
				ret = ret + "," + config[i];
			}
		}
		
		return ret;
	}

	public int getCastingTime(String ability) {
		return casting_times.get(ability);
	}

	public int getRequiredLevel(String ability) {
		return required_levels.get(ability);
	}

	public int getExperience(String ability) {
		return experience.get(ability);
	}

	public int getMana(String ability) {
		return mana.get(ability);
	}

	public List<ItemStack> getCost(String name) {
		int[] types = SkillClassConfig.intList("Spell Cost String " + name, cost.get(name));
		List<ItemStack> ret = new ArrayList<ItemStack>();
		
		if (types != null) {
			for (int type : types) {
				ret.add(new ItemStack(type, 1));
			}
		}
		
		return ret;
	}

	public String getSkillClass(String ability) {
		return classes.get(ability);
	}

	public int[] getConfig(String ability) {
		return config.get(ability);
	}

	public Integer getIconLocation(String ability) {
		return icons.get(ability);
	}
}
