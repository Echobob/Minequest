package org.monksanctum.MineQuest.Event.Target;

import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.TargetEvent;
import org.monksanctum.MineQuest.Event.Absolute.QuestEvent;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quest.Target;
import org.monksanctum.MineQuest.Quester.Quester;

public class ReputationCheckEvent extends QuestEvent implements TargetEvent {
	private String reputation;
	private boolean above;
	private int amount;
	private int task_fail;
	private Quester fail;
	private Target target;
	private int task_pass;

	public ReputationCheckEvent(Quest quest, long delay, Target target, String reputation, boolean above, int amount, int task_pass, int task_fail) {
		super(quest, delay, task_pass);
		this.target = target;
		this.reputation = reputation;
		this.above = above;
		this.amount = amount;
		this.task_pass = task_pass;
		this.task_fail = task_fail;
	}
	
	@Override
	public void reset(long time) {
		this.index = this.task_pass;
		super.reset(time);
	}
	
	@Override
	public void activate(EventParser eventParser) {
		boolean flag = true;
		
		for (Quester quester : target.getTargets()) {
			if (above) {
				if (!quester.hasReputation(reputation) || (quester.getReputation(reputation) < amount)) {
					flag = false;
					this.fail = quester;
					break;
				}
			} else {
				if (quester.hasReputation(reputation) && (quester.getReputation(reputation) > amount)) {
					flag = false;
					this.fail = quester;
					break;
				}
			}
		}
		
		if (flag) {
			super.activate(eventParser);
		} else {
			this.index = task_fail;
			super.activate(eventParser);
		}
	}

	@Override
	public String getName() {
		return "Reputation Check Event";
	}

	@Override
	public Quester getTarget() {
		return fail;
	}

}
