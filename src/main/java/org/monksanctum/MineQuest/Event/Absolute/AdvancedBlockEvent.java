package org.monksanctum.MineQuest.Event.Absolute;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.monksanctum.MineQuest.Event.EventParser;

public class AdvancedBlockEvent extends BlockEvent {
	private byte new_data;

	public AdvancedBlockEvent(long delay, Block block, Material newType, byte data) {
		super(delay, block, newType);
		this.new_data = data;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		super.activate(eventParser);
		
		block.setData(new_data, false);
		if (block.getState() != null) {
			block.getState().update(true);
		}
	}
}
