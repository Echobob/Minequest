package org.monksanctum.MineQuest.Quest.Idle;

import org.bukkit.Material;
import org.monksanctum.MineQuest.Configuration.SkillClassConfig;
import org.monksanctum.MineQuest.Quest.QuestProspect;
import org.monksanctum.MineQuest.Quester.Quester;

public class DestroyIdleTask extends IdleTask {

	private Material[] materials;
	private int[] counts;
	private int[] targets;

	public DestroyIdleTask(QuestProspect quest, int event_id, int id, Quester quester, 
			Material[] creatures, int[] counts, int[] targets) {
		super(quest, event_id, id, quester);
		this.materials = creatures;
		this.counts = counts;
		this.targets = targets;
		printStatus();
	}

	@Override
	public boolean isComplete() {
		boolean flag = true;
		boolean print_flag = false;
		int i;
		String last = quester.getLastDestroy();
		
		for (i = 0; i < materials.length; i++) {
			if (quester.getDestroyed(materials[i]) < targets[i]) {
				flag = false;
			}
			if (materials[i].name().equals(last)) {
				print_flag = true;
			}
		}
		if (print_flag) {
			printStatus();
		}
		
		return flag;
	}

	@Override
	public String getTarget() {
		String ret = targets[0] + "";
		int i;
		
		for (i = 1; i < targets.length; i++) {
			ret = ret + "," + targets[i];
		}
		
		return ret;
	}

	@Override
	public IdleType getType() {
		return IdleType.KILL;
	}

	@Override
	public int getTypeId() {
		return IdleType.KILL.getId();
	}

	@Override
	public void printStatus() {
		int i = 0;;
		if (quester.getPlayer() != null) {
			quester.sendMessage(quest.getName() + ":");
			for (Material material : materials) {
				quester.sendMessage("    "  + material.name() + " - " + (quester.getDestroyed(material) - targets[i] + counts[i]) + "/" + counts[i]);
				i++;
			}
		}
	}

	@Override
	public void setTarget(String target) {
		targets = SkillClassConfig.intList("Destroy Idle Task", target);
	}
}
