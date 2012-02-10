package org.monksanctum.MineQuest.Event.Absolute;

import org.bukkit.Material;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Party;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quester.Quester;

public class PartyDestroy extends QuestEvent {
	private Party party;
	private int[] count;
	private String[] destroy_names;
	private int[] destroys;

	public PartyDestroy(Quest quest, long delay, int task, Party party, String destroy_names[], int destroys[]) {
		super(quest, delay, task);
		this.party = party;
		this.destroy_names = destroy_names;
		this.destroys = destroys;
		count = new int[destroy_names.length];
		int i;
		for (i = 0; i < count.length; i++) {
			count[i] = 0;
		}
	}

	@Override
	public void activate(EventParser eventParser) {
		int i;
		
		for (Quester quester : party.getQuesterArray()) {
			for (Material type : quester.getDestroyed().keySet()) {
				for (i = 0; i < destroy_names.length; i++) {
					if (destroy_names[i].equals(type.toString())) {
						count[i] += quester.getDestroyed().get(type);
					}
				}
			}

			quester.clearDestroyed();
		}
		
		boolean flag = true;
		for (i = 0; i < count.length; i++) {
			if (count[i] < destroys[i]) {
				flag = false;
			}
		}

		if (flag) {
			super.activate(eventParser);
		} else {
			eventParser.setComplete(false);
		}
	}

}
