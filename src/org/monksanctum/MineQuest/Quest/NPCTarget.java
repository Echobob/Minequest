package org.monksanctum.MineQuest.Quest;

import java.util.ArrayList;
import java.util.List;

import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

public class NPCTarget extends Target{
	private String[] names;

	public NPCTarget(String[] names) {
		this.names = names;
	}

	@Override
	public List<Quester> getTargets() {
		List<Quester> questers = new ArrayList<Quester>();
		
		int i;
		for (i = 0; i < names.length; i++) {
			if (MineQuest.questerHandler.getQuester(names[i]) != null) {
				questers.add(MineQuest.questerHandler.getQuester(names[i]));
			}
		}
		
		return questers;
	}
}
