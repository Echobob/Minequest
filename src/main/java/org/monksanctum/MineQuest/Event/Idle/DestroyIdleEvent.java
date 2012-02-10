package org.monksanctum.MineQuest.Event.Idle;

import org.bukkit.Material;
import org.monksanctum.MineQuest.Quest.Party;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quest.Idle.DestroyIdleTask;
import org.monksanctum.MineQuest.Quest.Idle.IdleTask;
import org.monksanctum.MineQuest.Quester.Quester;

public class DestroyIdleEvent extends IdleEvent {

	private Material[] materials;
	private int[] counts;

	public DestroyIdleEvent(long delay, Party party, Quest quest, int task_id, Material[] materials, int[] counts) throws Exception {
		super(delay, party, quest, task_id);
		this.materials = materials;
		this.counts = counts;
	}

	@Override
	public String getName() {
		return "Destroy Idle Event";
	}

	@Override
	public IdleTask createEvent() {
		Quester quester = party.getQuesters().get(0);
		int[] target = new int[materials.length];
		int i;
		
		for (i = 0; i < materials.length; i++) {
			target[i] = counts[i] + quester.getDestroyed(materials[i]);
		}
		
		return new DestroyIdleTask(quest.getProspect(), id, task_id, quester, materials, counts, target);
	}

}
