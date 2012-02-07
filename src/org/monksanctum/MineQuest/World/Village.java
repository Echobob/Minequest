package org.monksanctum.MineQuest.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.CheckMobEvent;

public class Village {
	protected Area area;
	protected String name;
	protected int center_x, center_z;
	private String owner;
	
	public Village(String name, World world) {
		ResultSet results;
		if (this instanceof Town) {
			results = MineQuest.getSQLServer().query("SELECT * from towns WHERE name='" + name + "'");
		} else {
			results = MineQuest.getSQLServer().query("SELECT * from villages WHERE name='" + name + "'");
		}
		this.name = name;
		
		try {
			if (results.next()) {
				int height = results.getInt("height");
				Location start = new Location(world, (double)results.getInt("x"), 
						(double)results.getInt("y"), (double)results.getInt("z"));
				Location end = new Location(world, (double)results.getInt("max_x"), 
						(double)results.getInt("y") + height, (double)results.getInt("max_z"));
				
				if (this instanceof Town) {
					area = new TownProperty((Town)this, results.getString("owner"), start, end, height > 0, 0);
					((Town)this).getSpawn(results, world);
				} else {
					this.owner = results.getString("owner");
					area = new VillageArea(start, end, height > 0);
				}
				center_x = area.getCenterX();
				center_z = area.getCenterZ();
			}
		} catch (SQLException e) {
			MineQuest.log("Error: could not initialize village " + name);
			e.printStackTrace();
		}
		
		if (MineQuest.config.town_no_mobs) {
			MineQuest.getEventQueue().addEvent(new CheckMobEvent(this));
		}
	}
	
	public String getOwner() {
		return owner;
	}
	
	public Village() {
	}

	private void checkMob(Monster livingEntity) {
		if (isWithin(livingEntity.getLocation()) && 
				((MineQuest.mobHandler.getMob(livingEntity) == null) || 
					!(MineQuest.mobHandler.getMob(livingEntity).isSpawned()))) {
			livingEntity.setHealth(0);
		}
	}

	public String getName() {
		return name;
	}
	
	public void checkMobs() {
		List<LivingEntity> livingEntities = MineQuest.getSServer().getWorlds().get(0).getLivingEntities();
		
		for (LivingEntity livingEntity : livingEntities) {
			if (livingEntity instanceof Monster) {
				checkMob((Monster)livingEntity);
			}
		}
	}
	
	public boolean isWithin(Location loc) {
		return area.isWithin(loc);
	}

	public boolean isWithin(Player player) {
		return isWithin(player.getLocation());
	}
}
