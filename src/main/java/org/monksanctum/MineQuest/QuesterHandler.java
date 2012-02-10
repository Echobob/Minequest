package org.monksanctum.MineQuest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class QuesterHandler {
	private List<Quester> questers = new ArrayList<Quester>(); 
	
	public QuesterHandler() {
	}
	
	public void query() {
		ResultSet results = MineQuest.config.sql_server.query("SELECT * FROM questers");
		List<String> names = new ArrayList<String>();
		List<String> npcs = new ArrayList<String>();

		try {
			while (results.next()) {
				if (results.getString("mode").equals("Quester")) {
					names.add(results.getString("name"));
				} else {
					if (MineQuest.config.npc_enabled) {
						npcs.add(results.getString("name"));
					}
				}
			}
		} catch (SQLException e) {
			MineQuest.log("Error: Couldn't get list of questers");
		}
		
		for (String name : names) {
			questers.add(new Quester(name));
		}
		
		for (String name : npcs) {
			questers.add(new NPCQuester(name));
		}
	}
	
	/**
	 * Adds a Quester to the MineQuest Server.
	 * Does not modify mysql database.
	 * 
	 * @param quester Quester to be added
	 */
	public void addQuester(Quester quester) {
		questers.add(quester);
	}
    
    public void disable() {
		for (Quester quester : questers) {
			if (quester.getPlayer() != null) {
				quester.save();
			}
		}
	}
	
	/**
	 * This gets a list of all of the questers in the server presently. Meaning
	 * they have not null players.
	 * 
	 * @return List of Active Questers
	 */
	public Quester[] getActiveQuesters() {
		List<Quester> active = new ArrayList<Quester>();
		
		for (Quester quester : questers) {
			if (quester.getPlayer() != null) {
				active.add(quester);
			}
		}
		
		Quester[] questers = new Quester[active.size()];
		int i;
		for (i = 0; i < active.size(); i++) {
			questers[i] = active.get(i);
		}
		
		return questers;
	}

	/**
	 * Gets the difficulty adjustement of the MineQuest Server.
	 * As the level of players goes up the natural encounter
	 * of monsters gets harder to compensate.
	 * 
	 * @return Adjustment Factor to be used
	 */
	public int getAdjustment() {
        int avgLevel = 0;
        int size = 0;
        for (Quester quester : questers) {
            if (quester.getPlayer() != null) {
	            avgLevel += quester.getLevel();
	            size++;
            }
        }
        if (size == 0) return 0;
        avgLevel /= size;
        
        return (avgLevel / 10);
	}

	/**
	 * Gets a Quester of a specific Player
	 * 
	 * @param player Player that is a Quester
	 * @return Quester or NULL if none found
	 */
	public Quester getQuester(LivingEntity player) {
		if (!(player instanceof HumanEntity)) return null;
		
		return getQuester(((HumanEntity)player).getName());
	}
	
	/**
	 * Gets a Quester with a specific player name
	 * 
	 * @param name Name of Quester
	 * @return Quester with Name name or NULL
	 */
	public Quester getQuester(String name) {
		int i;
		
		for (i = 0; i < questers.size(); i++) {
			if (questers.get(i).equals(name)) {
				return questers.get(i);
			}
		}

		return null;
	}

	/**
	 * Returns lists of Questers within server.
	 * 
	 * @return List of Questers
	 */
	public List<Quester> getQuesters() {
		return questers;
	}

	public List<Quester> getRealQuesters() {
		List<Quester> questers = new ArrayList<Quester>();
		
		for (Quester quester : questers) {
			if ((!(quester instanceof NPCQuester)) && (quester.getPlayer() != null)) {
				questers.add(quester);
			}
		}
		
		return questers;
	}

	/**
	 * Removes a Quester from the MineQuest Server.
	 * Does not modify mysql database.
	 * 
	 * @param quester Quester to be removed
	 */
	public void remQuester(Quester quester) {
		if (quester.getPlayer() != null) {
			for (Quester npc : questers) {
				if (npc instanceof NPCQuester) {
					((NPCQuester)npc).clearTarget(quester.getPlayer());
				}
			}
		}
		questers.remove(quester);
	}
	
	/**
	 * Removes a Quester from the MineQuest Server.
	 * Does not modify mysql database.
	 * 
	 * @param name Name of Quester to be removed
	 */
	public void remQuester(String name) {
		questers.remove(getQuester(name));
	}

	/**
	 * Respawn all NPCs in case of them disappearing.
	 * 
	 */
	public void respawnNPCs() {
		for (Quester quester : questers) {
			if (quester instanceof NPCQuester) {
				((NPCQuester)quester).redo();
			}
		}
	}
}
