package org.monksanctum.MineQuest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.monksanctum.MineQuest.Mob.MQMob;
import org.monksanctum.MineQuest.Mob.SpecialMob;

public class MobHandler {
	List<String> noMobs;
	private MQMob mobs[];
	
	public MobHandler() {
		mobs = new MQMob[128];
        
        noMobs = new ArrayList<String>();
	}
	
	/**
	 * This adds a minequest wrapper to an existing mob.
	 * This function should not be called outside of MineQuest.
	 * Instead setMQMob should be used. This function handles
	 * the random control of whether a mob is a special mob or
	 * not.
	 * 
	 * @param entity Living entity to add
	 */
	public void addMob(LivingEntity entity) {
		Random generator = new Random();
		MQMob newMob;
		
		if (getMob(entity) != null) return;
		
		if (generator.nextDouble() < (MineQuest.questerHandler.getAdjustment() / 100.0)) {
			newMob = new SpecialMob(entity);
		} else {
			newMob = new MQMob(entity);
		}
		
		addMQMob(newMob);
	}
	
	/**
	 * This inserts a already created mob warpper into the list
	 * of other mobs. This function should not be called outside of MineQuest.
	 * Instead setMQMob should be used.
	 * 
	 * @param newMob
	 */
	public void addMQMob(MQMob newMob) {
		int i;
		for (i = 0; i < mobs.length; i++) {
			if (mobs[i] == null) {
				mobs[i] = newMob;
				return;
			}
		}
		
		MQMob newList[] = new MQMob[mobs.length*2];
		i = 0;
		for (MQMob mob : mobs) {
			newList[i++] = mob;
		}
		newList[i++] = newMob;
		while (i < newList.length){
			newList[i++] = null;
		}
		
		mobs = newList;
	}

	/**
	 * This is used in the mob control system to determine if a mob should be
	 * allowed to spawn in any given world.
	 * 
	 * @param entity Mob contained in world of question
	 * @return true if mobs are allowed in the entities world, false if not.
	 */
	public boolean canCreate(Entity entity) {
		String name = entity.getWorld().getName();
		
		if (MineQuest.config.spawning) return true;
		
		if (noMobs.contains(name)) {
			return false;
		}
		
		return true;
	}

	/**
	 * Forces a check of all of the mobs in every world to check for a mob that
	 * exists in MC but is not known about by MQ.
	 */
	public void checkAllMobs() {
    	for (World world : MineQuest.getSServer().getWorlds()) {
    		for (LivingEntity entity : world.getLivingEntities()) {
    			if ((entity instanceof Monster) || (entity instanceof Ghast)) {
    				if (getMob(entity) == null) {
    					addMob(entity);
    				}
    			}
    		}
    	}
	}
	
	/**
	 * This checks all MQ related mobs for death to see if any death actions 
	 * should be taken. Used for kill tracking.
	 */
	public void checkMobs() {
    	int i;
    	
    	for (i = 0; i < mobs.length; i++) {
    		if ((mobs[i] != null) && ((mobs[i].getHealth() <= 0) || (mobs[i].isDead()) || notExists(mobs[i].getId()))) {
    			mobs[i].dropLoot();
    			if (mobs[i].getLastAttacker() != null) {
    				mobs[i].getLastAttacker().addKill(mobs[i]);
    			}
    			mobs[i] = null;
    		}
    	}
    }

	private boolean notExists(int id) {
		for (World world : MineQuest.getSServer().getWorlds()) {
			net.minecraft.server.World mworld = ((CraftWorld) world).getHandle();
			for (Object obj : mworld.entityList) {
				if (obj instanceof net.minecraft.server.Entity) {
					net.minecraft.server.Entity entity = (net.minecraft.server.Entity) obj;

					if (entity.id == id) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public void printMobs() {
    	int i;
    	
    	for (i = 0; i < mobs.length; i++) {
    		if (mobs[i] != null) {
    			MineQuest.log("Mob:" + mobs[i].getId());
    		}
    	}
	}
	
	public void killMob(MQMob mob) {
    	int i;
    	
    	for (i = 0; i < mobs.length; i++) {
    		if ((mobs[i] != null) && (mobs[i].getId() == mob.getId())) {
    			mobs[i].dropLoot();
    			if (mobs[i].getLastAttacker() != null) {
    				mobs[i].getLastAttacker().addKill(mobs[i]);
    			}
    			mobs[i] = null;
    		}
    	}
	}
	
	public void unloadMob(MQMob mob) {
    	int i;
    	
    	for (i = 0; i < mobs.length; i++) {
    		if ((mobs[i] != null) && (mobs[i].getId() == mob.getId())) {
    			if (mobs[i].getLastAttacker() != null) {
    				mobs[i].getLastAttacker().addKill(mobs[i]);
    			}
    			mobs[i] = null;
    		}
    	}
	}
	
	/**
	 * Gets the wrapper for a specific mob.
	 * 
	 * @param entity Living entity of the mob
	 * @return MQ Wrapper for Mob
	 */
	public MQMob getMob(LivingEntity entity) {
		for (MQMob mob : mobs) {
			if (mob != null) {
				if (mob.getId() == entity.getEntityId()) {
					return mob;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Determines the number of Mobs that MQ is tracking right now.
	 * @return
	 */
	public int getMobSize() {
		int i = 0;
		
		for (MQMob mob : mobs) {
			if (mob != null) i++;
		}
		
		return i;
	}

	/**
	 * Removes all mobs from the designated world and adds it to the MQ no spawn
	 * list.
	 * 
	 * @param world World to remove Mobs from.
	 */
	public void noMobs(World world) {
		for (LivingEntity entity : world.getLivingEntities()) {
			if (!(entity instanceof HumanEntity)) {
				entity.setHealth(0);
			}
		}

		noMobs.add(world.getName());
	}

	/**
	 * This can be used to set the wrapper for a specific. This can be used to
	 * change a mob from special to normal or from any type to any other type.
	 * It will replace the old wrapper based on id of the mob it is wrapping.
	 * If the mob does not already have a wrapper, it will simply be added to 
	 * the list.
	 * 
	 * @param newMob New wrapper to set.
	 */
	public void setMQMob(MQMob newMob) {
		int i;
		
		for (i = 0; i < mobs.length; i++) {
			if (mobs[i] != null) {
				if (mobs[i].getId() == newMob.getId()) {
					mobs[i].cancel();
					mobs[i] = newMob;
				}
			}
		}
		
		addMQMob(newMob);
	}

	/**
	 * Removes a mob spawn restriction from the specified world.
	 * @param world
	 */
	public void yesMobs(World world) {
		noMobs.remove(world.getName());
		
		return;
	}

	public void unloadMobs(Chunk chunk) {
		for (MQMob mob : mobs) {
			if (mob != null) {
				if (mob.inChunk(chunk)) {
					unloadMob(mob);
				}
			}
		}
	}

}
