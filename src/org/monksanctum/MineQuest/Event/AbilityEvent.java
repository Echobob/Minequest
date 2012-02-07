package org.monksanctum.MineQuest.Event;

import org.monksanctum.MineQuest.Ability.Ability;

public class AbilityEvent extends NormalEvent {
	private Ability ability;

	public AbilityEvent(long delay, Ability ability) {
		super(delay);
		this.ability = ability;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		ability.eventActivate();
	}
	
	@Override
	public String getName() {
		return "Ability Event";
	}

}
