package org.monksanctum.MineQuest.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.monksanctum.MineQuest.PropertiesFile;

public class ResourceClassConfig extends SkillClassConfig {
	protected List<int[]> blocks;

	protected void setupProperties() {
		blocks = new ArrayList<int[]>();
		
		if (!(new File("MineQuest/resource_classes.properties")).exists()) {
			createFile("MineQuest/resource_classes.properties");
		}

		properties = new PropertiesFile("MineQuest/resource_classes.properties");
	}
	
	@Override
	protected void parseConfig(String name) {
		blocks.add(intList(name + "_block_types", properties.getString(name + "_block_types", "")));
		
		super.parseConfig(name);
	}

	public int[] getBlocks(String type) {
		int i;
		
		for (i = 0; i < names.size(); i++) {
			if (names.get(i).equals(type)) {
				return blocks.get(i);
			}
		}
		
		return null;
	}

}
