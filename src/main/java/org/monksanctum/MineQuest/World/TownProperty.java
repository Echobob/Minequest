package org.monksanctum.MineQuest.World;

import org.bukkit.Location;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

public class TownProperty extends Property {

	private Town town;

	public TownProperty(Town town, String owner, Location start, Location end,
			boolean height, long price) {
		super(owner, start, end, height, price);
		this.town = town;
	}
	
	@Override
	public boolean canEdit(Quester quester) {
		if (MineQuest.isPermissionsEnabled() && (quester.getPlayer() != null)) {
			if (MineQuest.permission.playerHas(quester.getPlayer(), "MineQuest.Town." + town.getName())) {
				return true;
			}
		}
		
		return super.canEdit(quester);
	}

}
