package org.monksanctum.MineQuest.Event;

import org.bukkit.block.Sign;

public class UpdateSignEvent extends NormalEvent {
	private Sign sign;
	private String[] lines;

	public UpdateSignEvent(long delay, Sign sign, String lines[]) {
		super(delay);
		this.sign = sign;
		this.lines = lines;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		sign.setLine(0, lines[0]);
		sign.setLine(1, lines[1]);
		sign.setLine(2, lines[2]);
		sign.setLine(3, lines[3]);
		sign.update(true);
	}

	@Override
	public String getName() {
		return "Update Sign Event";
	}
}
