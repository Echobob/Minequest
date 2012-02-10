package org.monksanctum.MineQuest.World;

import org.bukkit.Location;
import org.monksanctum.MineQuest.MineQuest;

public class VillageArea extends Area {
	
	public VillageArea(Location start, Location end, boolean b) {
		super(start, end, b);
	}

	/**
	 * FOR USE WITH TOWN PROPERTY ONLY!!
	 */
	public void setX(int x) {
		this.x = x;
		Village village = MineQuest.townHandler.getVillage(new Location(null, x + 1, y, z + 1));
		if (village == null) {
			MineQuest.log("[ERROR] Property outside of Village...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE villages SET x='" + x + " WHERE name='" + village.getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setMaxX(int x) {
		this.max_x = x;
		Village village = MineQuest.townHandler.getVillage(new Location(null, x + 1, y, z + 1));
		if (village == null) {
			MineQuest.log("[ERROR] Property outside of Village...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE villages SET max_x='" + max_x + " WHERE name='" + village.getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setZ(int z) {
		this.z = z;
		Village village = MineQuest.townHandler.getVillage(new Location(null, x + 1, y, z + 1));
		if (village == null) {
			MineQuest.log("[ERROR] Property outside of Village...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE villages SET z='" + z + " WHERE name='" + village.getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setMaxZ(int z) {
		this.max_z = z;
		Village village = MineQuest.townHandler.getVillage(new Location(null, x + 1, y, z + 1));
		if (village == null) {
			MineQuest.log("[ERROR] Property outside of Village...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE villages SET max_z='" + max_z + " WHERE name='" + village.getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setY(int y) {
		this.y = y;
		Village village = MineQuest.townHandler.getVillage(new Location(null, x + 1, y, z + 1));
		if (village == null) {
			MineQuest.log("[ERROR] Property outside of Village...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE villages SET y='" + y + " WHERE name='" + village.getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setHeight(int h) {
		this.height = h;
		Village village = MineQuest.townHandler.getVillage(new Location(null, x + 1, y, z + 1));
		if (village == null) {
			MineQuest.log("[ERROR] Property outside of Village...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE villages SET height='" + h + " WHERE name='" + village.getName() + "'");
	}

}
