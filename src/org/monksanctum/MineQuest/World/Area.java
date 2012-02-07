package org.monksanctum.MineQuest.World;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

public class Area {
	protected int x, max_x;
	protected int z, max_z;
	protected int height, y;
	protected World world;
	
	public Area(Location start, Location end, boolean height) {
		parseLocations(start, end, height);
	}
	
	public Area() {
	}
	
	public void parseLocations(Location start, Location end, boolean height) {
		if (start.getX() < end.getX()) {
			x = (int)start.getX();
			max_x = (int)end.getX();
		} else {
			x = (int)end.getX();
			max_x = (int)start.getX();
		}
		if (start.getZ() < end.getZ()) {
			z = (int)start.getZ();
			max_z = (int)end.getZ();
		} else {
			z = (int)end.getZ();
			max_z = (int)start.getZ();
		}
		y = (int)start.getY();
		if (height) {
			this.height = (int)(start.getY() - end.getY());
		}
		this.world = start.getWorld();
	}
	
	public boolean isWithin(Location loc) {
		if (loc.getX() < x) {
			return false;
		}
		if (loc.getX() > max_x) {
			return false;
		}
		if (loc.getZ() < z) {
			return false;
		}
		if (loc.getZ() > max_z) {
			return false;
		}
		if (height > 0) {
			if (loc.getY() < y) {
				return false;
			}
			if (loc.getY() > (y + height)) {
				return false;
			}
		}
		if (world != null) {
			if (!world.getName().equals(loc.getWorld().getName())) {
				return false;
			}
		}
		
		return true;
	}

	public int getY() {
		return y;
	}

	public int getMaxZ() {
		return max_z;
	}

	public int getZ() {
		return z;
	}

	public int getMaxX() {
		return max_x;
	}

	public int getX() {
		return x;
	}

	public int getCenterX() {
		return (x + max_x) / 2;
	}

	public int getCenterZ() {
		return (z + max_z) / 2;
	}
	
	public boolean isWithin(Player player) {
		return isWithin(player.getLocation());
	}
	
	public boolean isWithin(Quester quester) {
		return isWithin(quester.getPlayer());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Area) {
			Area other = (Area)obj;
			if (other.getX() != getX()) {
				return false;
			}
			if (other.getMaxX() != getMaxX()) {
				return false;
			}
			if (other.getZ() != getZ()) {
				return false;
			}
			if (other.getMaxZ() != getMaxZ()) {
				return false;
			}
			if (other.getY() != getY()) {
				return false;
			}
			
			return true;
		}
		return super.equals(obj);
	}
	
	/**
	 * FOR USE WITH TOWN PROPERTY ONLY!!
	 */
	public void setX(int x) {
		this.x = x;
		Town town = MineQuest.townHandler.getTown(new Location(null, x + 1, y, z + 1));
		if (town == null) {
			MineQuest.log("[ERROR] Property outside of town...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE towns SET x='" + x + " WHERE name='" + town.getName() + "'");
	}
	
	/**
	 * FOR USE WITH TOWN PROPERTY ONLY!!
	 */
	public void setMaxX(int x) {
		this.max_x = x;
		Town town = MineQuest.townHandler.getTown(new Location(null, x + 1, y, z + 1));
		if (town == null) {
			MineQuest.log("[ERROR] Property outside of town...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE towns SET max_x='" + max_x + " WHERE name='" + town.getName() + "'");
	}
	
	/**
	 * FOR USE WITH TOWN PROPERTY ONLY!!
	 */
	public void setZ(int z) {
		this.z = z;
		Town town = MineQuest.townHandler.getTown(new Location(null, x + 1, y, z + 1));
		if (town == null) {
			MineQuest.log("[ERROR] Property outside of town...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE towns SET z='" + z + " WHERE name='" + town.getName() + "'");
	}
	
	/**
	 * FOR USE WITH TOWN PROPERTY ONLY!!
	 */
	public void setMaxZ(int z) {
		this.max_z = z;
		Town town = MineQuest.townHandler.getTown(new Location(null, x + 1, y, z + 1));
		if (town == null) {
			MineQuest.log("[ERROR] Property outside of town...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE towns SET max_z='" + max_z + " WHERE name='" + town.getName() + "'");
	}
	
	/**
	 * FOR USE WITH TOWN PROPERTY ONLY!!
	 */
	public void setY(int y) {
		this.y = y;
		Town town = MineQuest.townHandler.getTown(new Location(null, x + 1, y, z + 1));
		if (town == null) {
			MineQuest.log("[ERROR] Property outside of town...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE towns SET y='" + y + " WHERE name='" + town.getName() + "'");
	}
	
	/**
	 * FOR USE WITH TOWN PROPERTY ONLY!!
	 */
	public void setHeight(int h) {
		this.height = h;
		Town town = MineQuest.townHandler.getTown(new Location(null, x + 1, y, z + 1));
		if (town == null) {
			MineQuest.log("[ERROR] Property outside of town...");
			return;
		}
		
		MineQuest.getSQLServer().update("UPDATE towns SET height='" + h + " WHERE name='" + town.getName() + "'");
	}

}
