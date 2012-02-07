package org.monksanctum.MineQuest.Event.Idle;

import org.bukkit.entity.CreatureType;
import org.monksanctum.MineQuest.Quest.Party;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quest.Idle.IdleTask;
import org.monksanctum.MineQuest.Quest.Idle.KillIdleTask;
import org.monksanctum.MineQuest.Quester.Quester;

public class KillIdleEvent extends IdleEvent {

	private CreatureType[] creatures;
	private int[] counts;

	public KillIdleEvent(long delay, Party party, Quest quest, int task_id, CreatureType[] creatures, int[] counts) throws Exception {
		super(delay, party, quest, task_id);
		this.creatures = creatures;
		this.counts = counts;
	}

	@Override
	public String getName() {
		return "Kill Idle Event";
	}

	@Override
	public IdleTask createEvent() {
		Quester quester = party.getQuesters().get(0);
		int[] target = new int[creatures.length];
		int i;
		
		for (i = 0; i < creatures.length; i++) {
			target[i] = counts[i] + quester.getKills(creatures[i]);
		}
		
		return new KillIdleTask(quest.getProspect(), id, task_id, quester, creatures, counts, target);
	}

}
