/*
 * MineQuest - Bukkit Plugin for adding RPG characteristics to minecraft
 * Copyright (C) 2011  Jason Monk
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.monksanctum.MineQuest.Quester;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityTracker;
import net.minecraft.server.Packet18ArmAnimation;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Ability.Ability;
import org.monksanctum.MineQuest.Ability.AbilityBinder;
import org.monksanctum.MineQuest.Ability.PassiveAbility;
import org.monksanctum.MineQuest.Economy.NPCSignShop;
import org.monksanctum.MineQuest.Event.Absolute.EntityTeleportEvent;
import org.monksanctum.MineQuest.Mob.MQMob;
import org.monksanctum.MineQuest.Quest.Party;
import org.monksanctum.MineQuest.Quest.Quest;
import org.monksanctum.MineQuest.Quest.QuestProspect;
import org.monksanctum.MineQuest.Quest.Idle.IdleTask;
import org.monksanctum.MineQuest.Quest.Idle.IdleType;
import org.monksanctum.MineQuest.Quester.SkillClass.CombatClass;
import org.monksanctum.MineQuest.Quester.SkillClass.SkillClass;
import org.monksanctum.MineQuest.World.Claim;
import org.monksanctum.MineQuest.World.Property;
import org.monksanctum.MineQuest.World.Town;

//Add Vault Imports Here


/**
 * This is a wrapper around bukkit's player class.
 * It manages the health for the player as well as
 * any other Player specific MineQuest data.
 * 
 * @author jmonk
 */
public class Quester {
	public static String getCreatureName(String name) {
		name = name.replace('.', '/');
		
		if (name.split("/").length > 0) {
			String type = name.split("/")[name.split("/").length - 1];
			type = type.replace("Craft", "");
			return type;
		} else {
			return null;
		}
	}

	protected List<QuestProspect> available;
	protected Location before_quest;
	protected ChestSet chests;
	protected int class_exp;
	protected List<SkillClass> classes;
	protected List<QuestProspect> completed;
	protected double cubes;
	protected boolean debug;
	protected Map<Material, Integer> destroyed;
	protected double distance;
	protected boolean enabled;
	protected int exp;
	protected int health;
	protected IdleTask[] idles;
	private List<Integer> ids = new ArrayList<Integer>();
	protected CreatureType[] kills;
	protected String last;
	private String last_destroy;
	private long last_msg;
	protected int level;
	protected int mana;
	protected int max_health;
	private int max_mana;
	protected boolean modded_client = false;
	protected String name;
	protected Party npcParty;
	protected Party party;
	protected Player player;
	protected int poison_timer;
	protected Quest quest;
	protected int rep;
	protected Map<String, Integer> reputation;
	private List<Long> times = new ArrayList<Long>();

	public Quester() {
	}

	/**
	 * Load player from MySQL Database.
	 * @param player
	 */
	public Quester(Player player) {
		this(player.getName());
		this.player = player;
	}

	/**
	 * Create a new Player in the MySQL Database.
	 * 
	 * @param player
	 * @param x
	 */
	public Quester(Player player, int x) {
		this(player.getName(), 0);
		this.player = player;
	}

	/**
	 * Load player from MySQL Database.
	 * @param name
	 */
	public Quester(String name) {
		this.name = name;
		npcParty = new Party();
		update();
		class_exp = 0;
	}

	/**
	 * Create a new Player in the MySQL Database
	 * 
	 * @param name
	 * @param x
	 */
	public Quester(String name, int x) {
		this.name = name;
		create();
		npcParty = new Party();
		update();
		distance = 0;
		class_exp = 0;
	}

	/**
	 * Binds an Ability to left of right click of the item
	 * in the players hand.
	 * 
	 * @param player Player
	 * @param name Name of Ability
	 * @param lr 1 for left, 0 for right
	 */
	public void addBinder(String ability, int item) {
		SkillClass my_class = getClassFromAbil(ability);
		if (my_class == null) {
			sendMessage("You do not have an ability named " + ability);
			return;
		}
		
		for (SkillClass skill : classes) {
			skill.unBind(player.getItemInHand());
		}
		
		Ability abil = new AbilityBinder(ability, item);
		abil.bind(this, player.getItemInHand());
		my_class.addAbility(abil);
		
		sendMessage("Binder added");
	}

	protected void addBinder(String ability, int item, ItemStack item_hand) {
		SkillClass my_class = getClassFromAbil(ability);
		if (my_class == null) {
			return;
		}

		for (SkillClass skill : classes) {
			if (skill != null) {
				skill.unBind(item_hand);
			}
		}
		
		Ability abil = new AbilityBinder(ability, item);
		my_class.addAbility(abil);
		
		abil.silentBind(this, item_hand);
	}

	public void addClass(String name) {
		if (name.equalsIgnoreCase("list")) {
			sendMessage("Available combat classes are:");
			for (String class_name : MineQuest.getCombatConfig().getClassNames()) {
				if (getClass(class_name) == null) {
					sendMessage("   " + class_name);
				}
			}
			return;
		}

		if (getClass(name) != null) {
			sendMessage("You already have the class " + name);
			return;
		}

		if (getCombatClasses().size() == MineQuest.config.maxClass) {
			sendMessage("You do not have space for any more combat classes");
			return;
		}

		List<String> names = MineQuest.getCombatConfig().getClassNames();
		boolean flag = false;
		for (String clazz : names) {
			if (name.equals(clazz)) {
				flag = true;
				break;
			}
		}

		if (!flag) {
			sendMessage(name + " is not a valid class currently");
			return;
		}

		save();
		try {
			ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM questers WHERE name='" + getSName() + "'");
			results.next();
			String classes = results.getString("classes");
			
			classes = classes + ", " + name;
			
			MineQuest.getSQLServer().update("UPDATE questers SET classes='" + classes + "' WHERE name='" + getSName() + "'");
			
			createClass(name, MineQuest.getNextAbilId());
			sendMessage(name + " class added!");
		} catch (SQLException e) {
			MineQuest.log("Unable to update class list");
		}
		update();
	}

	/**
	 * Adds to both health and maximum health of quester.
	 * Should be used on level up of character of 
	 * SkillClasses.
	 * 
	 * @param addition
	 */
	public void addHealth(int addition) {
		health += addition;
		max_health += addition;
	}
	
	public void addIdle(IdleTask idleTask) {
		boolean flag = false;
		
		for (IdleTask idle : idles) {
			if (idle.equals(idleTask)) {
				flag = true;
				break;
			}
		}
		
		if (flag) {
			return;
		}
		
		IdleTask[] new_idles = new IdleTask[idles.length + 1];
		int i = 0;
		for (IdleTask idle : idles) {
			new_idles[i++] = idle;
		}
		new_idles[i++] = idleTask;
		idles = new_idles;
		MineQuest.getSQLServer().aupdate("INSERT INTO idle (name, file, type, event_id, target)" + 
				"VALUES('" + name + "', '" + idleTask.getQuest().getFile() + "', '" + idleTask.getTypeId()
				+ "', '" + idleTask.getEventId() + "', '" + idleTask.getTarget() + "')");
	}
	
	private void addKill(CreatureType kill) {
		CreatureType[] new_kills = new CreatureType[kills.length + 1];
		int i;
		
		for (i = 0; i < kills.length; i++) {
			new_kills[i] = kills[i];
		}
		new_kills[i] = kill;
		
		kills = new_kills;
		
		checkIdle(IdleType.KILL);
	}
	
	public void addKill(MQMob mqMob) {
		LivingEntity monster = mqMob.getMonster();
		
		String name = monster.getClass().getName();
		if (CreatureType.fromName(getCreatureName(name)) != null) {
			addKill(CreatureType.fromName(getCreatureName(name)));
		}
	}
	
	public void addMana(int realManaCost) {
		mana += realManaCost;
		updateMana();
	}

	protected void addNewReputation(String type, int amount) {
		MineQuest.getSQLServer().aupdate(
				"INSERT INTO reps (name, type, amount) VALUES('" + name
						+ "', '" + type + "', '" + amount + "')");
		reputation.put(type, amount);
	}

	public void addNPC(NPCQuester quester) {
		npcParty.addQuester(quester);
		if (!equals(quester.getFollow())) {
			quester.setFollow(this);
		}
	}

	public void addQuestAvailable(QuestProspect quest) {
		if (isAvailable(quest)) return;
		if (quest.equals("")) {
			return;
		}
		available.add(quest);
		MineQuest.getSQLServer().aupdate("INSERT INTO quests (name, type, file) VALUES('" + getSName() + "', 'A', '" 
				+ quest.getFile() + "')");
		sendMessage("You now have access to the quest " + quest.getName());
		
		if (isModded()) {
			sendMessage("MQ:AA:" + quest.toString());
		}
	}
	
	public void addQuestCompleted(QuestProspect quest) {
		if (isCompleted(quest)) return;
		completed.add(quest);
		MineQuest.getSQLServer().aupdate("INSERT INTO quests (name, type, file) VALUES('" + getSName() + "', 'C', '" 
				+ quest.getFile() + "')");
		
		if (isModded()) {
			sendMessage("MQ:AC:" + quest.toString());
		}
	}

	public void addReputation(String type, int amount) {
		if (hasReputation(type)) {
			setReputation(type, getReputation(type) + amount);
		} else {
			addNewReputation(type, amount);
		}
		sendMessage("Congratulations your reputation with " + type + " is now " + getReputation(type));
	}

	/**
	 * Adds to both health and maximum mana of quester.
	 * Should be used on level up of character of 
	 * SkillClasses.
	 * 
	 * @param addition
	 */
	public void addTotalMana(int addition) {
		mana += addition;
		max_mana += addition;
	}

	public void armSwing() {
		HashSet<Byte> transparent = new HashSet<Byte>();
		transparent.add((byte)Material.AIR.getId());
		transparent.add((byte)Material.GLASS.getId());
		transparent.add((byte)Material.FIRE.getId());
		transparent.add((byte)Material.WATER.getId());
		transparent.add((byte)Material.TORCH.getId());
		List<Block> block = player.getLineOfSight(transparent, 30);

		if (block.size() > 0) {
			for (SkillClass skill : classes) {
				if (skill.isLookAbilityItem(player.getItemInHand())) {
					skill.callLookAbility(block.get(block.size() - 1));
				}
			}
		}
	}

	/**
	 * Called whenever a Quester attacks any other entity.
	 * 
	 * @param entity
	 * @param event
	 */
	public void attackEntity(Entity entity, EntityDamageByEntityEvent event) {
		if (checkItemInHand()) return;
		if (!(entity instanceof LivingEntity)) return;
		
		if (MineQuest.questerHandler.getQuester((LivingEntity)entity) instanceof NPCQuester) {
			NPCQuester quester = (NPCQuester)MineQuest.questerHandler.getQuester((LivingEntity)entity);
			if (hasQuester(quester)) {
				return;
			}
			NPCMode mode = quester.getMode();
			if ((mode != NPCMode.PARTY) &&
					(mode != NPCMode.PARTY_STAND)) {
				return;
			}
		}

		for (SkillClass skill : classes) {
			if (skill.isAbilityItem(player.getItemInHand())) {
				if (entity instanceof LivingEntity) {
					if (skill instanceof CombatClass) {
						skill.callAbility(entity);
						expGain(MineQuest.config.cast_ability_exp);
					} else {
						skill.callAbility(entity);
					}
					break;
				}
			}
		}
		
		if (npcParty != null) {
			boolean flag = true;
			if (entity instanceof Player) {
				for (Quester quester : npcParty.getQuesters()) {
					if (quester.getName().equals(((Player)entity).getName())) {
						flag = false;
						break;
					}
				}
			}
			if (flag) {
				for (Quester quester : npcParty.getQuesterArray()) {
					NPCQuester npc = (NPCQuester)quester;
					
					npc.questerAttack((LivingEntity)entity);
				}
			}
		}

		if (entity instanceof LivingEntity) {
			for (SkillClass skill : classes) {
				if (skill instanceof CombatClass) {
					if (skill.isClassItem(player.getItemInHand())) {
						((CombatClass)skill).attack((LivingEntity)entity, event);
						expGain(MineQuest.config.exp_class_damage);
						return;
					}
				}
			}
			if (MineQuest.config.half_damage) {
				event.setDamage(event.getDamage() / 2);
				expGain(MineQuest.config.exp_class_damage / 3);
			}
		}
	}
	
	/**
	 * Binds an Ability to click of the item
	 * in the players hand.
	 * 
	 * @param player Player
	 * @param name Name of Ability
	 */
	public void bind(String name) {
		for (SkillClass skill : classes) {
			skill.unBind(player.getItemInHand());
		}

		for (SkillClass skill : classes) {
			if (skill.getAbility(name) != null) {
				if (skill.getAbility(name) instanceof PassiveAbility) {
					sendMessage("Passive Abilities cannot be bound, must be enabled");
				} else {
					skill.getAbility(name).bind(this, player.getItemInHand());
				}
				return;
			}
		}
		sendMessage(name + " is not a valid ability");
		return;
	}
	
	public void bind(String ability, ItemStack itemStack) {
		for (SkillClass skill : classes) {
			skill.silentUnBind(itemStack);
		}

		for (SkillClass skill : classes) {
			if (skill.getAbility(ability) != null) {
				skill.getAbility(ability).bind(this, itemStack);
			}
		}
	}
	
	/**
	 * Determines which resource class a given block belongs to.
	 * 
	 * @param block
	 * @return
	 */
	public int blockToClass(Block block) {
		int miner[] = {
			1,4,14,15,16,
			41,42,43,44,45,
			56, 57,
			73, 74, 48, 49
		};
		int lumber[] = {
			5,17,18,47,50,53,
			58,63,64,65,68
		};
		int digger[] = {
			2,3,6,12,13,60,78,
			82
		};
		int farmer[] = {
			37, 38, 39, 40,
			81, 83, 281, 282,
			295, 296, 297, 332
		};
		
		int i;
		
		for (i = 0; i < miner.length; i++) {
			if (block.getTypeId() == miner[i]) {
				return 0;
			}
		}
		for (i = 0; i < lumber.length; i++) {
			if (block.getTypeId() == lumber[i]) {
				return 1;
			}
		}
		for (i = 0; i < digger.length; i++) {
			if (block.getTypeId() == digger[i]) {
				return 2;
			}
		}
		for (i = 0; i < farmer.length; i++) {
			if (block.getTypeId() == farmer[i]) {
				return 3;
			}
		}
		return 4;
	}

	public void callAbility() {
		for (SkillClass skill : classes) {
			if (skill.isAbilityItem(player.getItemInHand())){
				skill.callAbility();
				return;
			}
		}
	}
	
	/**
	 * Casts a left click bound ability on the given block.
	 * 
	 * @param block
	 */
	public void callAbility(Block block) {
		for (SkillClass skill : classes) {
			if (skill.isAbilityItem(player.getItemInHand())){
				skill.callAbility(block);
				return;
			}
		}
	}
	
	/**
	 * Casts a left click bound ability on the given entity
	 * 
	 * @param entity
	 */
	public void callAbility(Entity entity) {
		for (SkillClass skill : classes) {
			if (skill.isAbilityItem(player.getItemInHand())){
				skill.callAbility(entity);
				return;
			}
		}
	}
	
	/**
	 * Checks if a Quester has the required spell components.
	 * to cast an ability. If the Quester has the components
	 * they are removed from the inventory.
	 * 
	 * @param list List of Components
	 * @param mana 
	 * @return true if cost paid
	 */
	public boolean canCast(List<ItemStack> list, int mana) {
		int i;
		if (MineQuest.config.mana) {
			if (this.mana < mana) {
				return false;
			}
			this.mana -= mana;
		}
		
		if (MineQuest.config.spell_comp) {
			PlayerInventory inven = player.getInventory();
			
			for (i = 0; i < list.size(); i++) {
				if (inven.contains(list.get(i).getType())) {
					inven.removeItem(list.get(i));
				} else {
					while (i-- > 0) {
						inven.addItem(list.get(i));
					}
					if (MineQuest.config.mana) {
						this.mana += mana;
					}
					return false;
				}
			}
		}

		if (MineQuest.config.mana) {
			updateMana();
		}
		
		return true;
	}
	
	public boolean canCommand(String string) {
		if (MineQuest.isPermissionsEnabled() && (player != null)) {
			if (!MineQuest.permission.playerHas((Player) player, "MineQuest.Command." + string)) {
				return false;
			}
		}
		
		
		return true;
	}

	public boolean canEdit(Block block) {
		Town town = null;
		if (block != null) {
			town = MineQuest.townHandler.getTown(block.getLocation());
		} else {
			return true;
		}

		if (inQuest()) {
			if (!quest.canEdit(this, block)) {
				return false;
			}
			if (!player.getWorld().getName().equals(MineQuest.getSServer().getWorlds().get(0).getName())) {
				return true;
			}
		}
		
		if (!MineQuest.config.town_enable) {
			return true;
		}
		
		if (!MineQuest.config.town_protect) {
			return true;
		}

		int id = block.getTypeId();
		boolean flag = false;
		for (int other_id : MineQuest.config.town_exceptions) {
			if (id == other_id) flag = true;
		}

		
		if (!flag) {
			if (town != null) {
				Property prop = town.getProperty(block.getLocation());
				
				for (NPCSignShop shop : town.getStores()) {
					if (shop.parseClick(this, block)) {
						return false;
					}
				}
				
				if (prop != null) {
					if (!prop.canEdit(this)) {
						sendMessage("You are not authorized to modify this property - please get the proper authorization");
						dropRep(20);
						return false;
					}
				} else {
					prop = town.getTownProperty();
					
					if (!prop.canEdit(this)) {
						sendMessage("You are not authorized to modify town - please get the proper authorization");
						dropRep(10);
						return false;
					}
				}
			} else {
				Claim claim = MineQuest.townHandler.getClaim(block.getLocation());
				
				if (claim != null) {
					if (!claim.canEdit(this)) {
						sendMessage("You are not authorized to modify this claim - please get the proper authorization");
						return false;
					}
				}
			}
		}
		
		if (MineQuest.getMainQuest() != null) {
			return MineQuest.getMainQuest().canEdit(this, block);
		}

		return true;
	}
	
	public boolean canPay(long cost) {
		if (MineQuest.economy != null)
		{
			if (getCubes() >= cost)
			{
				withdrawBalance(cost);
				return true;
			}
		}
		if (getCubes() >= cost) 
		{
			setCubes(getCubes() - cost);
			return true;
		}
		
		return false;
	}

	public boolean canUse(ItemStack item) {
		boolean class_item = false;
		boolean can_use = false;
		for (String type : MineQuest.config.getFullClassNames()) {
			SkillClass skill = getClass(type);
			if (skill != null) {
				if (skill.isClassItem(item)) {
					if (skill.canUse(item)) {
						class_item = true;
						can_use = true;
					} else {
						if (!class_item) {
							class_item = true;
							can_use = false;
						}
					}
				}
			} else {
				SkillClass shell = SkillClass.newShell(type);
				if (shell.isClassItem(item)) {
					if (MineQuest.config.deny_non_class && !class_item) {
						class_item = true;
						can_use = false;
					}
				}
			}
		}
		
		return !class_item || can_use;
	}
    
    public boolean canWear(ItemStack item) {
		boolean class_armor = false;
		boolean can_use = false;
		for (String type : MineQuest.config.getFullClassNames()) {
			SkillClass skill = getClass(type);
			if (skill != null) {
				if (skill.isArmor(item)) {
					if (skill.canWear(item)) {
						class_armor = true;
						can_use = true;
					} else {
						if (!class_armor) {
							class_armor = true;
							can_use = false;
						}
					}
				}
			} else {
				SkillClass shell = SkillClass.newShell(type);
				if (shell.isArmor(item)) {
					if (MineQuest.config.deny_non_class && !class_armor) {
						class_armor = true;
						can_use = false;
					}
				}
			}
		}
		
		return !class_armor || can_use;
    }
	
	private boolean checkDamage(DamageCause cause) {
        if (cause == DamageCause.FIRE) {
        	return checkDamage(24040);
        } else if (cause == DamageCause.FIRE_TICK) {
        	return checkDamage(24041);
        } else if (cause == DamageCause.SUFFOCATION) {
        	return checkDamage(24042);
        } else if (cause == DamageCause.DROWNING) {
        	return checkDamage(24043);
        } else if (cause == DamageCause.LAVA) {
        	return checkDamage(24044);
        } else {
        	return checkDamage(24045);
        }
	}

	/**
     * This function will check if the damager in the event passed
     * has damaged the quester too recently to damage again.
     * This implements individual cool down for every entity.
     * 
     * @param event Event that holds the attacker
     * @return True if damage should be cancelled
     */
    private boolean checkDamage(int id) {
	    Calendar now = Calendar.getInstance();
	    int i;
	    int delay = 500;

    	if ((id == 24040) || (id == 24041)) {
    		if ((getAbility("Fire Resistance") != null) && getAbility("Fire Resistance").isEnabled()) {
	    		delay = 1000;
	    	}
	    }
	    
		for (i = 0; i < ids.size(); i++) {
			if (ids.get(i) == id) {
				if ((now.getTimeInMillis() - times.get(i)) > delay) {
					times.set(i, now.getTimeInMillis());
					return false;
				} else {
					return true;
				}
			}
		}
	    
	    ids.add(id);
	    times.add(now.getTimeInMillis());
	    
	    return false;
    }

	/**
	 * Checks if a Quester can use all of the equipment
	 * that they are wearing. It checks with each class
	 * individually, and they all handle their own removal.
	 * 
	 * @param player
	 */
	public void checkEquip() {
		if (!enabled) return;
		
		PlayerInventory inven = player.getInventory();
		if (!canWear(inven.getBoots())) {
			if (inven.firstEmpty() != -1) {
				inven.addItem(new ItemStack(inven.getBoots().getTypeId(), 1));
			} else {
				player.getWorld().dropItem(player.getLocation(), new ItemStack(inven.getBoots().getTypeId(), 1));
			}
			inven.setBoots(null);
			sendMessage("You are not proficient in the use of those boots");
		}
		if (!canWear(inven.getChestplate())) {
			if (inven.firstEmpty() != -1) {
				inven.addItem(new ItemStack(inven.getChestplate().getTypeId(), 1));
			} else {
				player.getWorld().dropItem(player.getLocation(), new ItemStack(inven.getChestplate().getTypeId(), 1));
			}
			inven.setChestplate(null);
			sendMessage("You are not proficient in the use of that chestplate");
		}
		if (!canWear(inven.getHelmet())) {
			if (inven.firstEmpty() != -1) {
				inven.addItem(new ItemStack(inven.getHelmet().getTypeId(), 1));
			} else {
				player.getWorld().dropItem(player.getLocation(), new ItemStack(inven.getHelmet().getTypeId(), 1));
			}
			inven.setHelmet(null);
			sendMessage("You are not proficient in the use of that helmet");
		}
		if (!canWear(inven.getLeggings())) {
			if (inven.firstEmpty() != -1) {
				inven.addItem(new ItemStack(inven.getLeggings().getTypeId(), 1));
			} else {
				player.getWorld().dropItem(player.getLocation(), new ItemStack(inven.getLeggings().getTypeId(), 1));
			}
			inven.setLeggings(null);
			sendMessage("You are not proficient in the use of those leggings");
		}
	}

	public void checkIdle(IdleType type) {
		for (IdleTask task : idles) {
			if (task.getType() == type) {
				task.checkTask();
			}
		}
	}
	
	/**
	 * Checks if a Quester is allowed to use the item
	 * in hand. The item is checked with each class
	 * but the removal is handled here.
	 * 
	 * @return
	 */
	public boolean checkItemInHand() {
		PlayerInventory inven = player.getInventory();
		ItemStack item = player.getItemInHand();
		
		if ((item == null) || (item.getTypeId() == 0)) return false;

		if (!canUse(item)) {
			if (inven.firstEmpty() != -1) {
				inven.addItem(item);
			} else {
				player.getWorld().dropItem(player.getLocation(), item);
			}
			
			inven.setItemInHand(null);
			if (player instanceof Player) {
				((Player)player).sendMessage("You are not proficient in the use of that item");
			}
			return true;
		}

		return false;
	}
	
	/**
	 * Checks if the item in the Questers hand is bound
	 * to any abilities. 
	 * @return true if bound to an ability
	 */
	public boolean checkItemInHandAbil() {
//		if ((player.getItemInHand().getTypeId() == 261) || (player.getItemInHand().getTypeId() == 332)) {
//			return false;
//		}

		for (SkillClass skill : classes) {
			if (skill.isAbilityItem(player.getItemInHand())) {
				return true;
			}
		}
		
		return false;
	}
	
	public void clearDestroyed() {
		if (MineQuest.config.track_destroy) {
			Map<CreatureType, Integer> kill_map = new HashMap<CreatureType, Integer>();
			
			ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM kills WHERE name='" + getSName() + "'");
			
			try {
				while (results.next()) {
					if (CreatureType.fromName(results.getString("type")) != null) {
						kill_map.put(CreatureType.fromName(results.getString("type")), results.getInt("count"));
					}
					if (Material.getMaterial(results.getString("type")) != null) {
						if (destroyed.get(Material.getMaterial(results.getString("type"))) == null) {
							destroyed.put(Material.getMaterial(results.getString("type")), results.getInt("count"));
						} else {
							destroyed.put(Material.getMaterial(results.getString("type")), results.getInt("count") + destroyed.get(Material.getMaterial(results.getString("type"))));
						}
					}
				}
			} catch (Exception e) {
			}
			
			MineQuest.getSQLServer().update("DELETE FROM kills WHERE name='" + getSName() + "'");
			
			for (CreatureType creature : kill_map.keySet()) {
				MineQuest.getSQLServer().aupdate(
						"INSERT INTO kills (name, type, count) VALUES('"
								+ getSName() + "', '"
								+ creature.getName() + "', '"
								+ kill_map.get(creature) + "')");
			}
			
			for (Material material : destroyed.keySet()) {
				MineQuest.getSQLServer().aupdate(
						"INSERT INTO kills (name, type, count) VALUES('"
								+ getSName() + "', '"
								+ material.name() + "', '"
								+ destroyed.get(material) + "')");
			}
		}
		
		destroyed.clear();
	}
	
	public void clearKills() {
		if (MineQuest.config.track_kills) {
			Map<CreatureType, Integer> kill_map = new HashMap<CreatureType, Integer>();
			Map<Material, Integer> destroyed = new HashMap<Material, Integer>();
			
			ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM kills WHERE name='" + getSName() + "'");
			
			try {
				while (results.next()) {
					if (CreatureType.fromName(results.getString("type")) != null) {
						kill_map.put(CreatureType.fromName(results.getString("type")), results.getInt("count"));
					}
					if (Material.getMaterial(results.getString("type")) != null) {
						if (destroyed.get(results.getString("type")) == null) {
							destroyed.put(Material.getMaterial(results.getString("type")), results.getInt("count"));
						} else {
							destroyed.put(Material.getMaterial(results.getString("type")), results.getInt("count") + destroyed.get(results.getString("type")));
						}
					}
				}
			} catch (Exception e) {
			}
			
			for (CreatureType creature : kills) {
				if (creature != null) {
					if (kill_map.get(creature) == null) {
						kill_map.put(creature, 1);
					} else {
						kill_map.put(creature, kill_map.get(creature) + 1);
					}
				}
			}
			
			MineQuest.getSQLServer().update("DELETE FROM kills WHERE name='" + getSName() + "'");
			
			for (CreatureType creature : kill_map.keySet()) {
				MineQuest.getSQLServer().aupdate(
						"INSERT INTO kills (name, type, count) VALUES('"
								+ getSName() + "', '"
								+ creature.getName() + "', '"
								+ kill_map.get(creature) + "')");
			}
			
			for (Material material : destroyed.keySet()) {
				MineQuest.getSQLServer().aupdate(
						"INSERT INTO kills (name, type, count) VALUES('"
								+ getSName() + "', '"
								+ material.name() + "', '"
								+ destroyed.get(material) + "')");
			}
		}
		
		kills = new CreatureType[0];
	}
	
	public void clearQuest(boolean reset) {
		this.quest = null;
		poison_timer = 0;
		if (reset && (before_quest != null)) {
			try {
//				player.teleport(before_quest.getWorld().getSpawnLocation());
			} catch (Exception e) {
			}
			MineQuest.getEventQueue().addEvent(new EntityTeleportEvent(1000, this, before_quest.getWorld().getSpawnLocation()));
			MineQuest.getEventQueue().addEvent(new EntityTeleportEvent(2000, this, before_quest));
		}
		if (isModded()) {
			sendMessage("MQ:ClearQuest");
		}
	}

	public void completeQuest(QuestProspect quest) {
		addQuestCompleted(quest);
		if (!quest.isRepeatable()) {
			remQuestAvailable(quest);
		}
	}

    /**
	 * Creates database entry for this quester with starting
	 * classes and health.
	 */
	public void create() {
		int num;
		String[] class_names = MineQuest.config.starting_classes;
		
		String update_string = "INSERT INTO questers (name, selected_chest, cubes, exp, level, last_town, classes, health, max_health, mana, max_mana) VALUES('"
			+ getSName() + "', '" + getSName() + "', '500000', '0', '0', 'Bitville', '";
		if ((class_names != null) && (class_names.length > 0)) {
			update_string = update_string + class_names[0];
			for (String name : class_names) {
				if (!name.equals(class_names[0])) {
					update_string = update_string + ", " + name;
				}
			}
		}
		update_string = update_string + "', '" + MineQuest.config.starting_health + "', '" + MineQuest.config.starting_health + "', '" + MineQuest.config.starting_mana + "', '" + MineQuest.config.starting_mana + "')";

		MineQuest.getSQLServer().update(update_string);
		
		num = MineQuest.getNextAbilId();
		
		for (String clazz : class_names) {
			createClass(clazz, num++);
		}
	}

	public void createClass(String clazz, int abil_list_id) {
		String update_string = "INSERT INTO classes (name, class, exp, level, abil_list_id) VALUES('"
			+ getSName() + "', '" + clazz + "', '0', '0', '" + abil_list_id + "')";

		MineQuest.getSQLServer().update(update_string);
		MineQuest.getSQLServer().update("INSERT INTO abilities (abil_list_id) VALUES('" + abil_list_id + "')");
	}

	public void createParty() {
		if (party != null) {
			party.remQuester(this);
		}
		party = null;
		(new Party()).addQuester(this);
	}

	/**
	 * Cures all poison for the Quester.
	 */
	public void curePoison() {
		poison_timer = 0;
	}

	public void damage(int i) {
		setHealth(getHealth() - i);
	}

	/**
	 * Called anytime a Player is destroying a block. Checks to
	 * see if it is bound to any abilities then attributes experience
	 * to given class if required.
	 * 
	 * @param event being destroyed
	 * @return false
	 */
	public void damageBlock(BlockDamageEvent event) {
		if (!enabled) return;

		for (SkillClass skill : classes) {
			if (skill.isAbilityItem(player.getItemInHand())) {
				skill.blockDamage(event);
				return;
			}
		}
		
		return;
	}

	public void debug() {
		debug = !debug;
	}

	/**
	 * Called any time there is a generic damage event on 
	 * the Quester.
	 * 
	 * @param event
	 */
	public void defend(EntityDamageEvent event) {
		healthChange(event.getDamage(), event);
	}
	
	/**
	 * Called any time there is a damaged by block event
	 * on the Quester.
	 * @param event
	 */
	public void defendBlock(EntityDamageByBlockEvent event) {
		healthChange(event.getDamage(), event);
	}

	/**
	 * Called any time the Quester is defending against an
	 * attack from another entity.
	 * 
	 * @param entity Attacker
	 * @param event
	 */
	public void defendEntity(Entity entity, EntityDamageByEntityEvent event) {
		int amount;
		if ((classes != null) && (classes.get(0) != null) && (classes.get(0).getGenerator() != null)) {
			amount = classes.get(0).getGenerator().nextInt(10);
		} else {
			amount = 5;
		}
		int levelAdj = MineQuest.questerHandler.getAdjustment();
		boolean flags[] = new boolean[] {false, false, false, false};
		if (levelAdj == 0) {
			levelAdj = 1;
		} else {
			amount *= levelAdj * 1;
		}
		
		amount /= 2;
		
		if ((event.getDamager() != null) && 
				(!(event.getDamager() instanceof Player) && 
				 checkDamage(event.getDamager().getEntityId())) &&
				 MineQuest.mqDamageEnabled(this)) {
            event.setCancelled(true);
            return;
        }
		
		if (entity instanceof LivingEntity) {
			if (entity != null) {
				if (MineQuest.mobHandler.getMob((LivingEntity)entity) != null) {
					amount = MineQuest.mobHandler.getMob((LivingEntity)entity).attack(amount, player);
				}
			}
		}
		
		if (npcParty != null) {
			for (Quester quester : npcParty.getQuesterArray()) {
				NPCQuester npc = (NPCQuester)quester;
				
				if (entity instanceof LivingEntity) {
					npc.questerAttack((LivingEntity)entity);
				}
			}
		}
		
		int sum = 0;
		
		if (classes != null) {
			for (SkillClass sclass : classes) {
				if (entity instanceof LivingEntity) {
					sum += sclass.defend((LivingEntity)entity, amount, flags);
				}
			}
		}
		
		amount -= sum;

		if (amount < 0) {
			amount = 0;
		}
		
		healthChange(amount, event);
		
		return;
	}

	public void destroyBlock(BlockBreakEvent event) {
		if (!enabled) return;

		if (destroyed.get(event.getBlock().getType()) == null) {
			destroyed.put(event.getBlock().getType(), 1);
		} else {
			destroyed.put(event.getBlock().getType(), destroyed.get(event.getBlock().getType()) + 1);
		}
		last_destroy = event.getBlock().getType().name();
		
		checkIdle(IdleType.DESTROY);

		for (SkillClass skill : classes) {
			if (skill.isAbilityItem(player.getItemInHand())) {
				skill.blockBreak(event);
				return;
			}
		}
		
		for (SkillClass skill : classes) {
			if (skill.isClassItem(player.getItemInHand())) {
				skill.blockBreak(event);
				expGain(MineQuest.config.destroy_block_exp);
				return;
			}
		}
		
		for (SkillClass skill : classes) {
			if (skill.isClassBlock(event.getBlock())) {
				skill.blockBreak(event);
				expGain(MineQuest.config.destroy_block_exp);
				return;
			}
		}
		
//		switch (blockToClass(event.getBlock())) {
//		case 0: // Miner
//			getClass("Miner").blockBreak(event);
//			expGain(MineQuest.getDestroyBlockExp());
//			return;
//		case 1: // Lumberjack
//			getClass("Lumberjack").blockBreak(event);
//			expGain(MineQuest.getDestroyBlockExp());
//			return;
//		case 2: // Digger
//			getClass("Digger").blockBreak(event);
//			expGain(MineQuest.getDestroyBlockExp());
//			return;
//		case 3: // Farmer
//			getClass("Farmer").blockBreak(event);
//			expGain(MineQuest.getDestroyBlockExp());
//			return;
//		}
		
		return;
	}

	/**
	 * Disable an ability with the given name.
	 * 
	 * @param string Name of Ability
	 */
	public void disableabil(String string) {
		for (SkillClass skill : classes) {
			if (skill.getAbility(string) != null) {
				skill.getAbility(string).disable();
			}
		}
	}

	/**
	 * Drops a Questers reputation by i.
	 * 
	 * @param i
	 */
	public void dropRep(int i) {
		rep -= i;
	}
	
	/**
	 * Enable an ability with the given name.
	 * 
	 * @param string Name of Ability
	 */
	public void enableabil(String string) {
		for (SkillClass skill : classes) {
			if (skill.getAbility(string) != null) {
				if (!skill.getAbility(string).isEnabled()) {
					skill.getAbility(string).enable(this);
				} else {
					sendMessage(string + " already enabled!");
				}
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Quester) {
			return name.equalsIgnoreCase(((Quester)obj).getName());
		}
		if (obj instanceof HumanEntity) {
			return name.equalsIgnoreCase(((HumanEntity)obj).getName());
		}
		if (obj instanceof String) {
			return name.equalsIgnoreCase((String) obj) || getSName().equalsIgnoreCase((String) obj);
		}
		return super.equals(obj);
	}
	
	public void expClassGain(int class_exp) {
		this.class_exp += class_exp;
	}

	/**
	 * Adds i experience to Quester and checks for level
	 * up.
	 * 
	 * @param i Experience to Add
	 */
	public void expGain(int i) {
		exp += i;
		if (exp > 400 * (level + 1)) {
			levelUp();
		}
	}

    public Ability getAbility(String ability) {
		for (SkillClass skill : classes) {
			if (skill != null) {
				if (skill.getAbility(ability) != null) {
					return skill.getAbility(ability);
				}
			}
		}
		return null;
	}
	
	public List<QuestProspect> getAvailableQuests() {
		return available;
	}

	/**
	 * Gets the ChestSet for given player
	 * 
	 * @return ChestSet
	 */
	public ChestSet getChestSet() {
		return chests;
	}

	/**
	 * Gets the SkillClass with given name for this
	 * Quester. Returns NULL if no class exists with
	 * given name.
	 * 
	 * @param string Name of SkillClass
	 * @return SkillClass
	 */
	public SkillClass getClass(String string) {
		for (SkillClass skill : classes) {
			if (skill.getType().equalsIgnoreCase(string)) {
				return skill;
			}
		}
		
		return null;
	}

	/**
	 * Gets a list of SkillClasses that this Quester has.
	 * @return
	 */
	public List<SkillClass> getClasses() {
		return classes;
	}

	public int getClassExp() {
		return class_exp;
	}

	public SkillClass getClassFromAbil(String ability) {
		for (SkillClass skill : classes) {
			if (skill != null) {
				if (skill.getAbility(ability) != null) {
					return skill;
				}
			}
		}
		return null;
	}

	public List<SkillClass> getCombatClasses() {
		List<SkillClass> ret = new ArrayList<SkillClass>();
		for (SkillClass skill : classes) {
			if (skill instanceof CombatClass) {
				ret.add(skill);
			}
		}
		
		return ret;
	}

	public List<QuestProspect> getCompletedQuests() {
		return completed;
	}

	/**
	 * Returns amount of cubes the Quester has.
	 * 
	 * @return
	 */

	public double getCubes() {
		if (MineQuest.economy != null) {
				double balance = MineQuest.economy.getBalance(getSName());
				return balance;
			}
		return cubes;
	}
	
	public Map<Material, Integer> getDestroyed() {
		return destroyed;
	}

	public int getDestroyed(Material material) {
		int count = 0;
		
		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM kills WHERE name='" + getSName() + "' AND type='" + material.toString() + "'");

		try {
			while (results.next()) {
				count += results.getInt("count");	
			}
		} catch (Exception e) {
			MineQuest.log("[WARNING] Problem getting " + name + "'s kills");
		}
		
		if (destroyed.get(material) != null) {
			count += destroyed.get(material);
		}
		
		return count;
	}

	/**
	 * Returns amount of experience for Quester.
	 * @return
	 */
	public int getExp() {
		return exp;
	}

	/**
	 * Returns health of Quester.
	 * @return
	 */
	public int getHealth() {
		if (MineQuest.mqDamageEnabled(this)) {
			return health;
		} else {
			if (player != null) {
				return player.getHealth();
			} else {
				return 0;
			}
		}
	}

	public List<QuestProspect> getIdleQuests() {
		List<QuestProspect> ret = new ArrayList<QuestProspect>();
		
		for (IdleTask idle : idles) {
			ret.add(idle.getQuest());
		}
		
		return ret;
	}

	public CreatureType[] getKills() {
		return kills;
	}

	public int getKills(CreatureType creature) {
		int count = 0;
		
		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM kills WHERE name='" + getSName() + "' AND type='" + creature.toString() + "'");

		try {
			while (results.next()) {
				count += results.getInt("count");	
			}
		} catch (Exception e) {
			MineQuest.log("[WARNING] Problem getting " + name + "'s kills");
		}
		for (CreatureType kill : kills) {
			if (kill.toString().equals(creature.toString())) {
				count++;
			}
		}
		
		return count;
	}

	public String getLastDestroy() {
		return last_destroy;
	}

	/**
	 * Returns level of Quester.
	 * 
	 * @return
	 */
	public int getLevel() {
		return level;
	}

	public int getMana() {
		return mana;
	}

	/**
	 * Returns maximum health of Quester.
	 * 
	 * @return
	 */
	public int getMaxHealth() {
		if (MineQuest.mqDamageEnabled(this)) {
			return max_health;
		} else {
			return 20;
		}
	}
	
	public int getMaxMana() {
		return max_mana;
	}
	
	/**
	 * Returns name of Quester.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public Party getParty() {
		return party;
	}

	/**
	 * Returns player that the Quester wraps.
	 * 
	 * @return
	 */
	public Player getPlayer() {
		return player;
	}
	
	public Quest getQuest() {
		return quest;
	}
	
	private QuestProspect getQuestProspect(String string) {
		for (QuestProspect qp : available) {
			if (qp.equals(string)) {
				return qp;
			}
		}
		for (QuestProspect qp : completed) {
			if (qp.equals(string)) {
				return qp;
			}
		}
		
		return null;
	}

	public int getReputation(String type) {
		return reputation.get(type);
	}

	/**
	 * Returns SQL name of Quester.
	 * @return
	 */
	public String getSName() {
		return name;
	}
	
	/**
	 * Returns last town player was near.
	 * 
	 * @return
	 */
	public Town getTown() {
		return MineQuest.townHandler.getTown(last);
	}
	
	public boolean hasQuester(Quester quester) {
		return npcParty.getQuesters().contains(quester);
	}

	public boolean hasReputation(String type) {
		return (reputation.get(type) != null);
	}

	/**
	 * Called any time a Quester is taking damage of any time
	 * it adjusts the Quester's health accordingly and sets
	 * the damage of the event as required.
	 * 
	 * @param change Amount of Damage Done
	 * @param event Event causing Damage
	 * @return false
	 */
	public boolean healthChange(int change, EntityDamageEvent event) {
		if (!MineQuest.mqDamageEnabled(this)) return false;
		if (event.isCancelled()) return false;
		if (player == null) return false;
		int newHealth;
                
        if (checkDamage(event.getCause()) && !(event instanceof EntityDamageByEntityEvent)) {
        	event.setCancelled(true);
        	return false;
        }

        if (MineQuest.config.log_health_change) {
        	MineQuest.log(change + " damage to " + name);
        }
        health -= change;
        
        newHealth = 20 * health / max_health;
        
        if ((newHealth == 0) && (health > 0)) {
        	newHealth = 1;
        }
        
        if (health > max_health) {
        	health = max_health;
        }
        
        if (newHealth < 0) {
        	newHealth = 0;
        }
        
        EntityPlayer pl = ((CraftPlayer)player).getHandle();
        EntityTracker entitytracker = pl.server.getTracker(pl.dimension);

        entitytracker.a(pl, new Packet18ArmAnimation(pl, 2));
        pl.world.a(pl, (byte) 2);
        
        if (player.getHealth() > newHealth) {
        	event.setDamage(player.getHealth() - newHealth);
        	MineQuest.log("Difference damage set - " + (player.getHealth() - newHealth));
        } else {
        	if (player.getHealth() < 20) {
        		player.setHealth(player.getHealth() + 1);
        		event.setDamage(1);
        		MineQuest.log("Damage set to 1");
        	} else {
        		event.setDamage(0);
        		MineQuest.log("Damage set to 0");
        	}
        }

        if (MineQuest.config.log_health_change) {
        	MineQuest.log("[INFO] " + name + " - " + health + "/" + max_health);
        }

        return false;
    }
	
	public boolean healthIncrease(PlayerInteractEvent event) {
		if (!MineQuest.mqDamageEnabled(this)) return false;
		if (event.getItem() == null) return false;
		Material type = event.getItem().getType();
		
		if (type == null) return false;
		
		switch (type) {
		case GOLDEN_APPLE:
			health = max_health;
			break;
		case APPLE:
			health += (int)(.15 * max_health);
			break;
		default:
			break;
		}

		if (MineQuest.getHealthConfiguration().isHealingItem(type)) {
			setHealth(getHealth() + MineQuest.getHealthConfiguration().getHealAmount(type));
		} else if (MineQuest.getHealthConfiguration().isManaItem(type)) {
			mana += MineQuest.getHealthConfiguration().getManaAmount(type);
		} else {
			return false;
		}
		
		event.setCancelled(true);
		if (!(this instanceof NPCQuester)) {
			if (getPlayer().getItemInHand().getAmount() == 1) {
				getPlayer().setItemInHand(null);
			} else {
				getPlayer().getItemInHand().setAmount(
						getPlayer().getItemInHand().getAmount() - 1);
			}
		}
		
		if (health > max_health) health = max_health;
		if (mana > max_mana) mana = max_mana;
		
		updateHealth();
		updateMana();
		
		return true;
	}

	public boolean inQuest() {
		return quest != null;
	}

	public boolean inVisible() {
		if (getAbility("Temporary Invisibility") != null) {
			return getAbility("Temporary Invisibility").isActive();
		}
		
		return false;
	}

	public boolean isAvailable(QuestProspect quest) {
		for (QuestProspect qp : available) {
			if (qp.equals(quest)) {
				return true;
			}
		}
		return false;
	}

	public boolean isAvailable(String quest) {
		for (QuestProspect qp : available) {
			if (qp.equals(quest)) {
				return true;
			}
		}
		return false;
	}

	public boolean isCompleted(QuestProspect quest) {
		for (QuestProspect qp : completed) {
			if (qp.equals(quest)) {
				return true;
			}
		}
		return false;
	}

	public boolean isCompleted(String quest) {
		for (QuestProspect qp : completed) {
			if (qp.equals(quest)) {
				return true;
			}
		}
		return false;
	}

	public boolean isDebug() {
		return debug;
	}

	/**
	 * Returns true if MineQuest is enabled for this
	 * Quester.
	 * 
	 * @deprecated
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	private boolean isIdle(String quest) {
		for (IdleTask idle : idles) {
			if (idle.getQuest().equals(quest)) {
				return true;
			}
		}

		return false;
	}
	
	public boolean isModded() {
		return modded_client;
	}
	
	/**
	 * Returns true if Quester is poisoned currently.
	 * 
	 * @return
	 */
	public boolean isPoisoned() {
		return (poison_timer > 0);
	}
	
	public void journal() {
		for (IdleTask idle : idles) {
			idle.printStatus();
		}
	}
	
	public void journal(String quest) {
		for (IdleTask idle : idles) {
			if (idle.getQuest().equals(quest)) {
				idle.printStatus();
			}
		}
	}
	
	/**
	 * Called whenever a Quester's character goes up
	 * a level. Handles experience changes and health
	 * additions.
	 */
	private void levelUp() {
		Random generator = new Random();
		level++;
		exp -= (400 * level);
		int add_health = 0;
		if (MineQuest.config.getLevelHealth() > 0) {
			add_health = generator.nextInt(MineQuest.config.getLevelHealth()) + 1;
		} else if (MineQuest.config.getLevelHealth() == 0) {
			add_health = 1;
		}
		max_health += add_health;
		health += add_health;
		if (add_health > 0) {
			updateHealth();
		}
		
		getPlayer().sendMessage("Congratulations on reaching character level " + level);
	}
	
	/**
	 * Display list of abilities that Quester has.
	 */
	public void listAbil() {
		for(SkillClass skill : classes) {
			skill.listAbil();
		}
	}
	
	public void listMercs() {
		if (npcParty.getQuesters().size() == 0) {
			sendMessage("You have no mercenaries");
		} else {
			sendMessage("Your mercenaries are:");
			for (Quester quester : npcParty.getQuesters()) {
				sendMessage(quester.getName());
			}
		}
	}
	
	public void listProf() {
		int id = 0;
		if (player.getItemInHand() != null) {
			id = player.getItemInHand().getTypeId();
		}
		listProf(id);
	}
	
	public void listProf(int id) {
		if (id <= 0) {
			sendMessage("You are not holding anything");
			return;
		}
		boolean flag = false;
		
		for (String class_name : MineQuest.getCombatConfig().getClassNames()) {
			int i;
			int ids[] = MineQuest.getCombatConfig().getTypes(class_name);
			int levels[] = MineQuest.getCombatConfig().getLevels(class_name);
			if ((ids != null) && (levels != null)) {
				for (i = 0; i < ids.length; i++) {
					if (ids[i] == id) {
						flag = true;
						String send = class_name + " - Lvl:" + levels[i];
						send = send + " - Dmg:(" + MineQuest.getCombatConfig().getBaseDamage(class_name)[i];
						send = send + "-" + MineQuest.getCombatConfig().getMaxDamage(class_name)[i];
						send = send + ")+ChrLvl/" + MineQuest.getCombatConfig().getCharLevelDmgAdj(class_name)[i];
						send = send + "+ClsLvl/" + MineQuest.getCombatConfig().getClassLevelDmgAdj(class_name)[i];
						send = send + " - Crit:" + MineQuest.getCombatConfig().getCritChance(class_name)[i];
						sendMessage(send);
					}
				}
			}
		}
		
		for (String class_name : MineQuest.getResourceConfig().getClassNames()) {
			int i;
			int ids[] = MineQuest.getResourceConfig().getTypes(class_name);
			int levels[] = MineQuest.getResourceConfig().getLevels(class_name);
			if ((ids != null) && (levels != null)) {
				for (i = 0; i < ids.length; i++) {
					if (ids[i] == id) {
						flag = true;
						String send = class_name + " - Lvl:" + levels[i];
						
						sendMessage(send);
					}
				}
			}
		}
		
		for (String class_name : MineQuest.getCombatConfig().getClassNames()) {
			int i;
			int ids[] = MineQuest.getCombatConfig().getArmor(class_name);
			int levels[] = MineQuest.getCombatConfig().getArmorLevels(class_name);
			int blocks[] = MineQuest.getCombatConfig().getArmorBlocks(class_name);
			double defends[] = MineQuest.getCombatConfig().getArmorDefends(class_name);
			if ((ids != null) && (levels != null)) {
				for (i = 0; i < ids.length; i++) {
					if (ids[i] == id) {
						flag = true;
						String send = class_name + " - Lvl:" + levels[i];
						send = send + " - Chance: " + defends[i];
						send = send + " - Blocks: " + blocks[i];
						sendMessage(send);
					}
				}
			}
		}
		
		for (String class_name : MineQuest.getResourceConfig().getClassNames()) {
			int i;
			int ids[] = MineQuest.getResourceConfig().getArmor(class_name);
			int levels[] = MineQuest.getResourceConfig().getArmorLevels(class_name);
			int blocks[] = MineQuest.getResourceConfig().getArmorBlocks(class_name);
			double defends[] = MineQuest.getResourceConfig().getArmorDefends(class_name);
			if ((ids != null) && (levels != null)) {
				for (i = 0; i < ids.length; i++) {
					if (ids[i] == id) {
						flag = true;
						String send = class_name + " - Lvl:" + levels[i];
						send = send + " - Chance: " + defends[i];
						send = send + " - Blocks: " + blocks[i];
						sendMessage(send);
					}
				}
			}
		}
		
		
		if (!flag) {
			sendMessage("No proficiency for this item found");
		}
	}
	
	public void lookBind(String name) {
		for (SkillClass skill : classes) {
			skill.unBind(player.getItemInHand());
		}

		for (SkillClass skill : classes) {
			if (skill.getAbility(name) != null) {
				if (skill.getAbility(name) instanceof PassiveAbility) {
					sendMessage("Passive Abilities cannot be bound, must be enabled");
				} else {
					skill.getAbility(name).lookBind(this, player.getItemInHand());
				}
				return;
			}
		}
		sendMessage(name + " is not a valid ability");
		return;
	}
	
	public void lookBind(String ability, ItemStack itemStack) {
		for (SkillClass skill : classes) {
			skill.silentUnBind(itemStack);
		}

		for (SkillClass skill : classes) {
			if (skill.getAbility(ability) != null) {
				skill.getAbility(ability).lookBind(this, itemStack);
			}
		}
	}
	
	/**
	 * Called every time a player moves. It makes sure that players
	 * respawn in the proper town. Handles any poison damage required
	 * and updates the health to fix any inconsistencies that may have
	 * arisen.
	 * 
	 * @param from Quester's old Location
	 * @param to Quester's new Location
	 */
	public void move(Location from, Location to) {
		checkEquip();
		
		updateHealth();
		
		if (MineQuest.config.town_enable) {
			Town last_town = MineQuest.townHandler.getNearestTown(to);
			if (last_town != null) {
				if (!last_town.getName().equals(last)) {
					last = last_town.getName();
					MineQuest.getSQLServer().aupdate("UPDATE questers SET last_town='" + last + "'");
				}
			} else {
				last = null;
			}
		}
		
		if (poison_timer > 0) {
			distance += MineQuest.distance(from, to);
		}
		
		while ((distance > 5) && (poison_timer > 0)) {
			distance -= 5;
			poison_timer -= 1;
			setHealth(getHealth() - 1);
	        
	        EntityPlayer pl = ((CraftPlayer)player).getHandle();
	        EntityTracker entitytracker = pl.server.getTracker(pl.dimension);

	        entitytracker.a(pl, new Packet18ArmAnimation(pl, 3));
		}
		
		checkIdle(IdleType.AREA);
	}
	
	public void notify(String message) {
		Calendar now = Calendar.getInstance();
		
		if ((now.getTimeInMillis() - last_msg) > 2000) {
			last_msg = now.getTimeInMillis();
			sendMessage(message);
		}		
	}
	
	/**
	 * Called each time the player is poisoned.
	 * Updates the poison counter appropriately.
	 */
	public void poison() {
		sendMessage("Poisoned!");
		poison_timer += 10;
	}

	public void priorClass(String string) {
		String list;
		if (getClass(string) == null) {
			sendMessage("You do not have any class " + string);
			return;
		}
		string = getClass(string).getType();
		list = string;
		
		for (SkillClass sclass : classes) {
			if (!sclass.getType().equals(string)) {
				list = list + ", " + sclass.getType();
			}
		}

		MineQuest.getSQLServer().update("UPDATE questers SET classes='" + list + "' WHERE name='" + getSName() + "'");
		save();
		update();
		
		sendMessage("Class Priority:");
		for (SkillClass sclass : classes) {
			sendMessage("  " + sclass.getType());
		}
	}

	public int recalculateHealth() {
		health = MineQuest.config.starting_health;
		Random generator = new Random();
		
		for (int i = 0; i < level; i++) {
			int add_health = generator.nextInt(MineQuest.config.getLevelHealth()) + 1;
			health += add_health;
		}
		
		for (SkillClass skill : classes) {
			if (skill instanceof CombatClass) {
				CombatClass cclass = (CombatClass)skill;
				for (int i = 0; i < cclass.getLevel(); i++) {
					int size = cclass.getSize();
					if (size > 0) {
						int add_health = generator.nextInt(size) + 1;
		
						health += add_health;
					} else if (size == 0) {
						health++;
					}
				}
			}
		}
		
		max_health = health;
		
		return health;
	}

	public int recalculateMana() {
		mana = MineQuest.config.starting_mana;
		Random generator = new Random();
		
		for (int i = 0; i < level; i++) {
			int add_health = generator.nextInt(MineQuest.config.getLevelMana()) + 1;
			health += add_health;
		}
		
		for (SkillClass skill : classes) {
			if (skill instanceof CombatClass) {
				CombatClass cclass = (CombatClass)skill;
				for (int i = 0; i < cclass.getLevel(); i++) {
					int size = cclass.getManaSize();
					if (size > 0) {
						int add = generator.nextInt(size) + 1;
	
						mana += add;
					} else if (size == 0) {
						mana++;
					}
				}
			}
		}
		
		max_mana = mana;
		
		updateMana();
		
		return mana;
	}

	public void regain(EntityRegainHealthEvent event) {
		health += event.getAmount();
		
		updateHealth();
		
		event.setAmount(0);
	}
	
	public void regroup() {
		for (Quester quester : npcParty.getQuesterArray()) {
			((NPCQuester)quester).setTarget((LivingEntity)null);
		}

		sendMessage("Regrouping Mercenaries!");
	}

	public void remIdle(IdleTask idleTask) {
		boolean flag = false;
		
		for (IdleTask idle : idles) {
			if (idle.equals(idleTask)) {
				flag = true;
				break;
			}
		}
		
		if (!flag) {
			return;
		}
		
		IdleTask[] new_idles = new IdleTask[idles.length - 1];
		int i = 0;
		for (IdleTask idle : idles) {
			if (!idle.equals(idleTask)) {
				new_idles[i++] = idle;
			}
		}
		idles = new_idles;
		MineQuest.getSQLServer().update("DELETE FROM idle WHERE name='" + name 
				+ "' AND file='" + idleTask.getQuest().getFile() 
				+ "' AND event_id='" + idleTask.getEventId() + "'");
	}

	public void remNPC(NPCQuester quester) {
		npcParty.remQuester(quester);
	}

	public void remQuestAvailable(QuestProspect quest) {
		available.remove(quest);
		MineQuest.getSQLServer().update("DELETE FROM quests WHERE type='A' AND name='" + getSName() + "' AND file='" + quest.getFile() + "'");
		
		if (isModded()) {
			sendMessage("MQ:RA:" + quest.toString());
		}
	}

	public void remQuestComplete(QuestProspect quest) {
		completed.remove(quest);
		MineQuest.getSQLServer().update("DELETE FROM quests WHERE type='C' AND name='" + getSName() + "' AND file='" + quest.getFile() + "'");
		
		if (isModded()) {
			sendMessage("MQ:RC:" + quest.toString());
		}
	}

	public void respawn(PlayerRespawnEvent event) {
		health = max_health;
		mana = max_mana;
		poison_timer = 0;
		if (quest != null) {
			if (quest.getSpawn() != null) {
				if (!quest.getSpawn().getWorld().getName().equals(event.getRespawnLocation().getWorld().getName())) {
					CraftWorld cworld = (CraftWorld)quest.getWorld();
					WorldServer world = cworld.getHandle();
					world.manager.removePlayer(((CraftPlayer)player).getHandle());
				}
				try {
					event.setRespawnLocation(quest.getSpawn());
				} catch (Exception e) {
				}
				MineQuest.getEventQueue().addEvent(new EntityTeleportEvent(500, this, quest.getSpawn()));
			} else if (MineQuest.getSServer().getWorlds().get(0).getName().equals(quest.getWorld().getName())) {
//				if (MineQuest.townRespawn() && (MineQuest.getTown(last) != null)) {
//					event.setRespawnLocation(MineQuest.getTown(last).getSpawn());
//				}
			}
		} else {
			if (MineQuest.config.town_respawn && (MineQuest.townHandler.getTown(last) != null)) {
				event.setRespawnLocation(MineQuest.townHandler.getTown(last).getSpawn());
			}
		}
		return;
	}

	public void rightClick(Block block) {
		for (SkillClass skill : classes) {
			if (skill.isClassItem(player.getItemInHand())) {
				skill.rightClick(block);
				expGain(3);
				return;
			}
		}
		
		switch (blockToClass(block)) {
		case 0: // Miner
			getClass("Miner").rightClick(block);
			return;
		case 1: // Lumberjack
			getClass("Lumberjack").rightClick(block);
			return;
		case 2: // Digger
			getClass("Digger").rightClick(block);
			return;
		case 3: // Farmer
			getClass("Farmer").rightClick(block);
			return;
		default:
			break;
		}
		
		return;
	}

	/**
	 * Saves any changes in the Quester to the MySQL
	 * Database.
	 */
	public void save() {
		if (MineQuest.getSQLServer().update("UPDATE questers SET exp='" + exp + "', level='" + level + "', health='" 
				+ health + "', max_health='" + max_health + "', mana='" 
				+ mana + "', max_mana='" + max_mana + "', enabled='" + 1
				+ "', cubes='" + (long)cubes + "' WHERE name='" + getSName() + "'") == -1) {
			if (player instanceof Player) {
				((Player)player).sendMessage("May not have saved properly, please try again");
			} else {
				MineQuest.log("May not have saved properly, please try again");
			}
		}
		for(SkillClass skill : classes) {
			skill.save();
		}
		enabled = false;
		
		if (npcParty != null) {
			for (Quester quester : npcParty.getQuesters()) {
				if (((NPCQuester)quester).getMode() == NPCMode.PARTY) {
					((NPCQuester)quester).setMode(NPCMode.PARTY_STAND);
				}
			}
		}
		
		clearKills();
		clearDestroyed();
	}

	/**
	 * Sends a message to the Quester's Player.
	 * 
	 * @param string Message
	 */
	public void sendMessage(String string) {
		if ((player != null) && (player instanceof Player)) {
			((Player)player).sendMessage(string);
		} else {
			MineQuest.log("[WARNING] Quester " + name + " doesn't have player, not logged in?");
			MineQuest.log("[WARNING] Message: " + string);
		}
	}

	/**
	 * Sets the Cubes of the Quester.
	 * 
	 * @param d New Cubes
	 */

	public void setCubes(double d) {
		cubes = d;
		}
	
	/**
	 * This code is to add money to the specified player when using vault's
	 * economy system. This requires a double to be supplied to work.
	 */
	
	public void addBalance(double money)
	{
		MineQuest.economy.depositPlayer(getSName(), money);
	}
	
	/**
	 * This withdraws the given amount from the player. 
	 */
	
	public void withdrawBalance(double cost)
	{
		EconomyResponse Amount = MineQuest.economy.bankHas(getSName(), cost);
		if (Amount != null){
			MineQuest.economy.bankWithdraw(getSName(), cost);
		}
	}
	
	/**
	 * Sets the Health of the Quester.
	 * 
	 * @param i New Health
	 */
	public void setHealth(int i) {
		if (i > getMaxHealth()) {
			i = getMaxHealth();
		}
		if (MineQuest.mqDamageEnabled(this)) {
			health = i;
			
			updateHealth();
		} else {
			if (player != null) {
				player.setHealth(i);
			}
		}
	}

	public void setMana(int i) {
		if (i > max_mana) {
			i = max_mana;
		}
		if (i < 0) {
			i = 0;
		}
		mana = i;
		updateMana();
	}

	public void setModded() {
		modded_client = true;
		updateMana();
		updateQuests();
		updateAbilities();
		if (MineQuest.config.mana) {
			sendMessage("MQ:ManaSystem");
		}
		if (MineQuest.config.spell_comp) {
			sendMessage("MQ:SpellCompSystem");
		}
	}

	public void setParty(Party party) {
		if (this.party != null) {
			Party party1 = this.party;
			this.party = null;
			party1.remQuester(this);
		}
		if (npcParty != null) {
			if (party != null) {
				for (Quester quester : npcParty.getQuesters()) {
					party.addQuester(quester);
				}
			}
			if (this.party != null) {
				for (Quester quester : npcParty.getQuesters()) {
					this.party.remQuester(quester);
				}
			}
		}
		this.party = party;
	}

	/**
	 * Sets the reference to the Player.
	 * 
	 * @param player New Reference
	 */
	public void setPlayer(Player player) {
		if (player == null) {
			if (!(this instanceof NPCQuester)) {
				if (party != null) party.remQuester(this);
				if (quest != null) {
					quest.removeQuester(this);
				}
			}
			modded_client = false;
		}
		this.player = player;
	}
	
	public void setQuest(Quest quest, World world) {
		this.quest = quest;

		if (player != null) {
			before_quest = player.getLocation();
			
			if (quest.getSpawn() != null) {
//				try {
//					player.teleport(quest.getSpawn());
//				} catch (Exception e) {
//				}
				MineQuest.getEventQueue().addEvent(new EntityTeleportEvent(1, this, quest.getSpawn()));
			}
			clearKills();
			clearDestroyed();
		}
		if ((quest != null) && isModded()) {
			sendMessage("MQ:Quest:" + quest.getProspect().getName());
		}
	}
	
	public void setReputation(String type, int amount) {
		MineQuest.getSQLServer().aupdate(
				"UPDATE reps SET amount='" + amount + "' WHERE name='" + name
						+ "' AND type='" + type + "'");
		reputation.put(type, amount);
	}

	public void spendClassExp(String type, int amount) {
		if (amount > class_exp) {
			sendMessage("You only have " + class_exp + " available");
			return;
		}
		if (getClass(type) == null) {
			sendMessage(type + " is not a valid class for you");
			return;
		}
		
		class_exp -= amount;
		
		getClass(type).expAdd(amount);
		sendMessage(amount + " experience spent on " + type);
	}

	public void startled() {
		sendMessage("Someone is attempting to steal form you!");
	}

	public void startQuest(String string) {
		if (!isAvailable(string)) {
			sendMessage("You have no quest named " + string + " available");
			return;
		}

		if (!getQuestProspect(string).isRepeatable()) {
			for (Quester quester : party.getQuesterArray()) {
				if (quester.isCompleted(string)) {
					sendMessage(quester.getName() + " has already completed " + string);
					quester.sendMessage("You have already completed " + string);
					return;
				}
			}
		}
		for (Quester quester : party.getQuesterArray()) {
			if (quester.isIdle(string)) {
				if (!equals(quester)) {
					sendMessage(quester.getName() + " has " + string + " in idle status");
				}
				quester.sendMessage("You already have " + string + " in idle status");
				return;
			}
		}

		MineQuest.addQuest(new Quest(getQuestProspect(string).getFile(), party));
	}

	@SuppressWarnings("deprecation")
	public ItemStack stolen() {
		if (player == null) {
			return null;
		}
		Inventory inven = player.getInventory();
		if (inven == null) {
			return null;
		}
		List<ItemStack> possibilities = new ArrayList<ItemStack>();
		
		for (ItemStack item : inven.getContents()) {
			if (item != null) {
				possibilities.add(item);
			}
		}
		
		if (possibilities.size() > 0) {
			Random gen = new Random();
			int index = gen.nextInt(possibilities.size());
			ItemStack item = possibilities.get(index);
			
			if (item.getAmount() > 1) {
				int amount = gen.nextInt(item.getAmount()) + 1;
				if (amount == item.getAmount()) {
					inven.remove(item);
					getPlayer().updateInventory();
					return item;
				} else {
					ItemStack ret = new ItemStack(item.getTypeId(), amount);
					item.setAmount(item.getAmount() - amount);
					getPlayer().updateInventory();
					return ret;
				}
			} else {
				inven.remove(item);
				getPlayer().updateInventory();
				return item;
			}
		}
		
		return null;
	}

	public void targeted(EntityTargetEvent event) {
		for (SkillClass sclass : classes) {
			sclass.targeted(event);
		}
	}

	/**
	 * Called whenever a Quester is teleported to check for
	 * respawning.
	 * 
	 * @deprecated
	 * @param event Teleport Event
	 */
	public void teleport(PlayerMoveEvent event) {
//		if ((health <= 0) && (player.getHealth() == 20)) {
//			event.setTo(MineQuest.getTown(last).getSpawn());
//			setRespawn(true);
//		}
	}
	
	/**
	 * Unbinds the Item that the Quester is holding
	 * from all abilities.
	 * 
	 * @param itemInHand
	 */
	public void unBind(ItemStack itemInHand) {
		for(SkillClass skill : classes) {
			skill.unBind(itemInHand);
		}
	}

	/**
	 * Loads all of the Quester's status from the MySQL
	 * database.
	 */
	public void update() {
		ResultSet results;
		String split[];
		int i;
		
		try {
			results = MineQuest.getSQLServer().query("SELECT * FROM questers WHERE name='" + getSName() + "'");
			results.next();
		} catch (SQLException e) {
			MineQuest.log("Issue querying name");
			return;
		}
		try {
			split = results.getString("classes").split(", ");
			exp = results.getInt("exp");
			level = results.getInt("level");
			health = results.getInt("health");
			max_health = results.getInt("max_health");
			mana = results.getInt("mana");
			max_mana = results.getInt("max_mana");
			if (max_mana == 0) max_mana++;
			enabled = results.getInt("enabled") > 0;
			
			cubes = results.getDouble("cubes");
			last = results.getString("last_town");
			chests = new ChestSet(this, results.getString("selected_chest"));
		} catch (SQLException e) {
			MineQuest.log("Issue getting parameters");
			return;
		}
		
		classes = new ArrayList<SkillClass>();
		for (i = 0; i < split.length; i++) {
			classes.add(SkillClass.newClass(this, split[i]));
		}
		
		results = MineQuest.getSQLServer().query("SELECT * FROM quests WHERE type='C' AND name='" + getSName() + "'");
		completed = new ArrayList<QuestProspect>();
		
		try {
			while (results.next()) {
				completed.add(new QuestProspect(results.getString("file")));
			}
		} catch (SQLException e) {
			MineQuest.log("Unable to load completed quests for " + name);
		}
		
		results = MineQuest.getSQLServer().query("SELECT * FROM quests WHERE type='A' AND name='" + getSName() + "'");
		available = new ArrayList<QuestProspect>();
		
		try {
			while (results.next()) {
				available.add(new QuestProspect(results.getString("file")));
			}
		} catch (SQLException e) {
			MineQuest.log("Unable to load completed quests for " + name);
		}
		
		updateBinds();
		kills = new CreatureType[0];
		destroyed = new HashMap<Material, Integer>();
		if (npcParty != null) {
			for (Quester quester : MineQuest.questerHandler.getQuesters()) {
				if (quester instanceof NPCQuester) {
					NPCQuester nquester = (NPCQuester)quester;
					if ((nquester.getMode() == NPCMode.PARTY) 
						|| (nquester.getMode() == NPCMode.PARTY_STAND)) {
						if (nquester.getFollow() != null) {
							if ((nquester.getFollow().equals(this)) 
								&& !npcParty.getQuesters().contains(nquester)) {
								npcParty.addQuester(nquester);
							}
						}
					}
				}
			}
		}

		results = MineQuest.getSQLServer().query("SELECT * FROM idle WHERE name='" + name + "'");
		List<String> files = new ArrayList<String>();
		List<Integer> event_ids = new ArrayList<Integer>();
		List<String> targets = new ArrayList<String>();
		try {
			while (results.next()) {
				files.add(results.getString("file"));
				event_ids.add(results.getInt("event_id"));
				targets.add(results.getString("target"));
			}
			
			idles = new IdleTask[files.size()];
			for (i = 0; i < files.size(); i++) {
				idles[i] = IdleTask.newIdle(this, files.get(i), event_ids.get(i), targets.get(i));
			}
		} catch (SQLException e) {
			MineQuest.log("Unable to load idle quests for " + name);
		}
		reputation = new HashMap<String, Integer>();
		results = MineQuest.getSQLServer().query("SELECT * FROM reps WHERE name='" + name + "'");
		try {
			while (results.next()) {
				reputation.put(results.getString("type"), results.getInt("amount"));
			}
		} catch (SQLException e) {
			MineQuest.log("Unable to load reputations for " + name);
		}
		
//		if (npcParty != null) {
//			for (Quester quester : npcParty.getQuesterArray()) {
//			}
//		}
	}

	/**
	 * Sets the reference to the Player and updates
	 * the Quester's status information from the db.
	 * @param player
	 */
	public void update(Player player) {
		this.player = player;
		update();
		updateHealth();
	}

	private void updateAbilities() {
		for (SkillClass skill : classes) {
			skill.updateAbilities();
		}
	}

	public void updateBinds() {
		ResultSet results;
		try {
			results = MineQuest.getSQLServer().query("SELECT * FROM binds WHERE name='" + getSName() + "'");
			while (results.next()) {
				String name = results.getString("abil");
				if (name.contains(":") && name.split(":")[0].equals("Binder")) {
					if (getAbility(name) == null) {
						addBinder(name.split(":")[1], results.getInt("bind_2"), new ItemStack(results.getInt("bind")));
					}
				} else if (name.contains(":") && name.split(":")[0].equals("LOOK")) {
					Ability ability = getAbility(name.split(":")[1]);
					if (ability != null) {
						ability.silentLookBind(this, new ItemStack(results.getInt("bind")));
					}
				} else {
					Ability ability = getAbility(name);
					if (ability != null) {
						ability.silentBind(this, new ItemStack(results.getInt("bind")));
					}
				}
			}
		} catch (SQLException e) {
			MineQuest.log("Could not update binds for quester " + name);
		}
	}

	/**
	 * Updates the Players health to show a percentage of
	 * the Questers health.
	 * 
	 * @param player
	 */
	public void updateHealth() {
		if (!MineQuest.mqDamageEnabled(this)) return;
		int newValue;
		
		newValue = (int)((20 * (double)health) / max_health);
		
		if ((newValue == 0) && (health > 0)) {
			newValue++;
		}

		if (newValue < 0) {
			newValue = 0;
		}
		
		if (player != null) {
			if (newValue == 0) {
				player.damage(5000);
			} else {
				if (newValue < player.getHealth()) {
					player.damage(player.getHealth() - newValue);
				} else if (newValue != player.getHealth()) {
					player.setHealth(newValue);
				}
			}
		}
	}

	private void updateMana() {
		if (MineQuest.config.mana && isModded()) {
			sendMessage("MQ:Mana-" + mana + "/" + max_mana);
		} else if (isModded()) {
			sendMessage("MQ:Mana--1/1");
		}
	}

	private void updateQuests() {
		for (QuestProspect quest : available) {
			if (isModded()) {
				sendMessage("MQ:AA:" + quest.toString());
			}
		}
		for (QuestProspect quest : completed) {
			if (isModded()) {
				sendMessage("MQ:AC:" + quest.toString());
			}
		}
	}
}
