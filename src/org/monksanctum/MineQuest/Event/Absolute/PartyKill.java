package org.monksanctum.MineQuest.Event.Absolute;

import org.bukkit.entity.CreatureType;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Quest.Party;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quester.Quester;

public class PartyKill extends QuestEvent {
	private Party party;
	private int[] count;
	private String[] kill_names;
	private int[] kills;

	public PartyKill(Quest quest, long delay, int task, Party party, String kill_names[], int kills[]) {
		super(quest, delay, task);
		this.party = party;
		this.kill_names = kill_names;
		this.kills = kills;
		count = new int[kill_names.length];
		int i;
		for (i = 0; i < count.length; i++) {
			count[i] = 0;
		}
	}

	@Override
	public void activate(EventParser eventParser) {
		int i;
		
		for (Quester quester : party.getQuesterArray()) {
			for (CreatureType type : quester.getKills()) {
				for (i = 0; i < kill_names.length; i++) {
					if (kill_names[i].equals(type.getName())) {
						count[i]++;
					}
				}
			}
			quester.clearKills();
		}
		
		boolean flag = true;
		for (i = 0; i < count.length; i++) {
			if (count[i] < kills[i]) {
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
