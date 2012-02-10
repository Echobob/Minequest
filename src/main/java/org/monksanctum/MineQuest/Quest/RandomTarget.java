package org.monksanctum.MineQuest.Quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.monksanctum.MineQuest.Quester.Quester;

public class RandomTarget extends Target {
	private Target other;
	private Random generator;

	public RandomTarget(Target other) {
		this.other = other;
		generator = new Random();
	}

	@Override
	public List<Quester> getTargets() {
		List<Quester> ret = new ArrayList<Quester>();
		List<Quester> other_list = other.getTargets();
		
		if (other_list.size() != 0) {
			int index = generator.nextInt(other_list.size());
			ret.add(other_list.get(index));
		}
		
		return ret;
	}
}
