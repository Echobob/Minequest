package org.monksanctum.MineQuest.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.monksanctum.MineQuest.PropertiesFile;

public class CombatClassConfig extends SkillClassConfig {
	protected List<int[]> char_dmg_adj;
	protected List<int[]> class_dmg_adj;
	protected List<int[]> base_dmg;
	protected List<int[]> max_dmg;
	protected List<double[]> crit_chance;
	
	public CombatClassConfig() {
		super();
	}

	protected void setupProperties() {
		char_dmg_adj = new ArrayList<int[]>();
		class_dmg_adj = new ArrayList<int[]>();
		base_dmg = new ArrayList<int[]>();
		max_dmg = new ArrayList<int[]>();
		crit_chance = new ArrayList<double[]>();
		
		if (!(new File("MineQuest/combat_classes.properties")).exists()) {
			createFile("MineQuest/combat_classes.properties");
		}

		properties = new PropertiesFile("MineQuest/combat_classes.properties");
	}

	@Override
	protected void parseConfig(String name) {
		super.parseConfig(name);
		
		char_dmg_adj.add(intList(name + "_char_dmg_adj", properties.getString(name + "_char_dmg_adj", "")));
		
		class_dmg_adj.add(intList(name + "_class_dmg_adj", properties.getString(name + "_class_dmg_adj", "")));
		
		base_dmg.add(intList(name + "_base_dmg", properties.getString(name + "_base_dmg", "")));
		
		max_dmg.add(intList(name + "_max_dmg", properties.getString(name + "_max_dmg", "")));
		
		crit_chance.add(doubleList(name + "_crit_chance", properties.getString(name + "_crit_chance", "")));
	}
	
	public int[] getCharLevelDmgAdj(String type) {
		int i;
		
		for (i = 0; i < names.size(); i++) {
			if (names.get(i).equals(type)) {
				return char_dmg_adj.get(i);
			}
		}
		
		return null;
	}

	public int[] getClassLevelDmgAdj(String type) {
		int i;
		
		for (i = 0; i < names.size(); i++) {
			if (names.get(i).equals(type)) {
				return class_dmg_adj.get(i);
			}
		}
		
		return null;
	}

	public int[] getBaseDamage(String type) {
		int i;
		
		for (i = 0; i < names.size(); i++) {
			if (names.get(i).equals(type)) {
				return base_dmg.get(i);
			}
		}
		
		return null;
	}

	public double[] getCritChance(String type) {
		int i;
		
		for (i = 0; i < names.size(); i++) {
			if (names.get(i).equals(type)) {
				return crit_chance.get(i);
			}
		}

		return null;
	}

	public int[] getMaxDamage(String type) {
		int i;
		
		for (i = 0; i < names.size(); i++) {
			if (names.get(i).equals(type)) {
				return max_dmg.get(i);
			}
		}
		
		return null;
	}

}
