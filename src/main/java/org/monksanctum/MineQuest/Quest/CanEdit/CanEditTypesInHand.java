package org.monksanctum.MineQuest.Quest.CanEdit;

import org.bukkit.Location;
import org.monksanctum.MineQuest.Quester.Quester;

public class CanEditTypesInHand extends CanEditBlock {
	private int[] ids;

	public CanEditTypesInHand(int index, int ids[]) {
		super(null, index);
		this.ids = ids;
	}
	
	@Override
	public boolean canEdit(Quester quester, Location loc) {
		if (quester.getPlayer().getItemInHand() != null) {
			for (int id : ids) {
				if (quester.getPlayer().getItemInHand().getTypeId() == id) {
					return true;
				}
			}
		}
		
		return false;
	}
}
