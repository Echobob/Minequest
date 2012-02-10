package org.monksanctum.MineQuest.World;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.MineQuest;

public class Claim extends Property {
	protected String name;
	protected int center_x, center_z;

	public Claim(String name, World world) {
		ResultSet results;
		results = MineQuest.getSQLServer().query("SELECT * from claims WHERE name='" + name + "'");
		this.name = name;
		
		try {
			if (results.next()) {
				int height = results.getInt("height");
				Location start = new Location(world, (double)results.getInt("x"), 
						(double)results.getInt("y"), (double)results.getInt("z"));
				Location end = new Location(world, (double)results.getInt("max_x"), 
						(double)results.getInt("y") + height, (double)results.getInt("max_z"));
				
				parseLocations(start, end, height > 0);
				this.owner = results.getString("owner");
				center_x = getCenterX();
				center_z = getCenterZ();
			}
		} catch (SQLException e) {
			MineQuest.log("Error: could not initialize claim " + name);
			e.printStackTrace();
		}
	}
	
	public boolean isWithin(Location loc) {
		return isWithin(loc);
	}

	public boolean isWithin(Player player) {
		return isWithin(player.getLocation());
	}

	public String getName() {
		return name;
	}

	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setX(int x) {
		this.x = x;
		
		MineQuest.getSQLServer().update("UPDATE claims SET x='" + x + " WHERE name='" + getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setMaxX(int x) {
		this.max_x = x;
		
		MineQuest.getSQLServer().update("UPDATE claims SET max_x='" + max_x + " WHERE name='" + getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setZ(int z) {
		this.z = z;
		
		MineQuest.getSQLServer().update("UPDATE claims SET z='" + z + " WHERE name='" + getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setMaxZ(int z) {
		this.max_z = z;
		
		MineQuest.getSQLServer().update("UPDATE claims SET max_z='" + max_z + " WHERE name='" + getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setY(int y) {
		this.y = y;
		
		MineQuest.getSQLServer().update("UPDATE claims SET y='" + y + " WHERE name='" + getName() + "'");
	}
	
	/**
	 * FOR USE WITH VILLAGE PROPERTY ONLY!!
	 */
	public void setHeight(int h) {
		this.height = h;
		Town claim = MineQuest.townHandler.getTown(new Location(null, x + 1, y, z + 1));
		if (claim == null) {
			MineQuest.log("[ERROR] Property outside of claim...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE claims SET height='" + h + " WHERE name='" + claim.getName() + "'");
	}

}
