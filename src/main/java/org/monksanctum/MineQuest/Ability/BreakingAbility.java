package org.monksanctum.MineQuest.Ability;

import org.bukkit.block.Block;
import org.monksanctum.MineQuest.Quester.Quester;

public interface BreakingAbility {
	public void blockBreak(Quester quester, Block block);
}
