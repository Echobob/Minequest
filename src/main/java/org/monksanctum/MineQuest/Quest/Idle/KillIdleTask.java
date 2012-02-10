package org.monksanctum.MineQuest.Quest.Idle;

import org.bukkit.entity.CreatureType;
import org.monksanctum.MineQuest.Configuration.SkillClassConfig;
import org.monksanctum.MineQuest.Quest.QuestProspect;
import org.monksanctum.MineQuest.Quester.Quester;

public class KillIdleTask extends IdleTask {

	private CreatureType[] creatures;
	private int[] counts;
	private int[] targets;

	public KillIdleTask(QuestProspect quest, int event_id, int id, Quester quester, 
			CreatureType[] creatures, int[] counts, int[] targets) {
		super(quest, event_id, id, quester);
		this.creatures = creatures;
		this.counts = counts;
		this.targets = targets;
		printStatus();
	}

	@Override
	public boolean isComplete() {
		boolean flag = true;
		boolean print_flag = false;
		int i;
		String last = quester.getKills()[quester.getKills().length - 1].getName();
		
		for (i = 0; i < creatures.length; i++) {
			if (quester.getKills(creatures[i]) < targets[i]) {
				flag = false;
			}
			if (creatures[i].getName().equals(last)) {
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
			for (CreatureType creature : creatures) {
				quester.sendMessage("    "  + creature.getName() + " - " + (quester.getKills(creature) - targets[i] + counts[i]) + "/" + counts[i]);
				i++;
			}
		}
	}

	@Override
	public void setTarget(String target) {
		targets = SkillClassConfig.intList("Kill Idle Task", target);
	}
}
