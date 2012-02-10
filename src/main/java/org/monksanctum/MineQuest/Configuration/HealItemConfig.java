package org.monksanctum.MineQuest.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.PropertiesFile;

public class HealItemConfig {
	private Map<Material, Integer> healing;
	private Map<Material, Integer> manaing;
	
	public HealItemConfig() {
		healing = new HashMap<Material, Integer>();
		manaing = new HashMap<Material, Integer>();
		
		if (!(new File("MineQuest/use_items.properties")).exists()) {
			MineQuest.log("Cannot find MineQuest/use_items.properties - downloading template!");
			try {
				MineQuest.downloadFile("http://www.theminequest.com/download/MineQuest/use_items.properties", "MineQuest/use_items.properties");
				MineQuest.log("Download Successful!");
			} catch (Exception e) {
				MineQuest.log("[Error] Download Failed!");
			}
		}
		
		PropertiesFile use_items = new PropertiesFile("MineQuest/use_items.properties");
		
		String[] heal_items = use_items.getString("heal_items", "GRILLED_PORK,PORK,MUSHROOM_SOUP,BREAD,CAKE,RAW_FISH,COOKED_FISH").split(",");
		List<Material> heals = new ArrayList<Material>();
		for (String heal_item : heal_items) {
			if (Material.getMaterial(heal_item) != null) {
				heals.add(Material.getMaterial(heal_item));
			} else {
				MineQuest.log("[WARNING] (Use Item Config) " + heal_item + " is not a valid item");
			}
		}

		for (Material mat : heals) {
			healing.put(mat, use_items.getInt(mat.name(), 3));
		}

		String[] mana_items = use_items.getString("mana_items", "MANA_POTION").split(",");
		List<Material> mana = new ArrayList<Material>();
		for (String mana_item : mana_items) {
			if (Material.getMaterial(mana_item) != null) {
				mana.add(Material.getMaterial(mana_item));
			} else {
				MineQuest.log("[WARNING] (Use Item Config) " + mana_item + " is not a valid item");
			}
		}
		
		for (Material mat : mana) {
			manaing.put(mat, use_items.getInt(mat.name(), 3));
		}
	}
	
	public boolean isHealingItem(Material mat) {
		return healing.containsKey(mat);
	}
	
	public boolean isManaItem(Material mat) {
		return manaing.containsKey(mat);
	}
	
	public int getHealAmount(Material mat) {
		return healing.get(mat);
	}
	
	public int getManaAmount(Material mat) {
		return manaing.get(mat);
	}

}
