package org.monksanctum.MineQuest.Quest;

import java.util.List;

import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

public class FullParty extends Party {
	public FullParty() {
	}
	
	@Override
	public void addQuester(Quester quester) {
		MineQuest.log("[WARNING] Cannot add quester to Main Party!");
	}
	
	@Override
	public void remQuester(Quester quester) {
		MineQuest.log("[WARNING] Cannot add quester to Main Party!");
	}
	
	@Override
	public Quester[] getQuesterArray() {
		Quester[] ret = new Quester[MineQuest.questerHandler.getRealQuesters().size()];
		int i = 0;
		
		for (Quester quester : MineQuest.questerHandler.getRealQuesters()) {
			ret[i++] = quester;
		}
		
		return ret;
	}
	
	@Override
	public List<Quester> getQuesters() {
		return MineQuest.questerHandler.getRealQuesters();
	}

}
