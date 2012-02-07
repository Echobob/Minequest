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
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.martin.bukkit.npclib.NPCEntity;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Ability.Ability;
import org.monksanctum.MineQuest.Economy.Store;
import org.monksanctum.MineQuest.Economy.StoreBlock;
import org.monksanctum.MineQuest.Event.NPCEvent;
import org.monksanctum.MineQuest.Event.Absolute.SpawnNPCEvent;
import org.monksanctum.MineQuest.Event.Relative.MessageEvent;
import org.monksanctum.MineQuest.Event.Target.StartQuestEvent;
import org.monksanctum.MineQuest.Quest.AreaTarget;
import org.monksanctum.MineQuest.Quest.QuestProspect;
import org.monksanctum.MineQuest.Quester.SkillClass.SkillClass;
import org.monksanctum.MineQuest.World.Town;


public class NPCQuester extends Quester {
	private static String getName(String name) {
		return name.replaceAll("_", " ");
	}
	private Location center;
	private long cost;
	private int count = 0;
	private int delay = 15000;
	private NPCEntity entity;
	private int fix;
	private Quester follow;
	private String follow_name;
	private Random generator;
	private ItemStack hand = null;
	private int heal_amount;
	private String[] hit_message;
    private List<Integer> ids1 = new ArrayList<Integer>();
	private List<Integer> idsStore;
	private ItemStack item_drop;
	private List<Integer> itemStore;
	private long last_attack;
	private long last_hit;
	private int message_delay;
	private boolean mob_protected = false;
	private LivingEntity attackTarget;
	private NPCMode mode;
	private String quest_file;
	private double rad;
	private int radius;
	private int reach_count;
	private boolean removed;
	private double speed = .6;
	private String start_quest;
	private double start_quest_radius;
	private int startle_task;
	private ItemStack steal;
	private Location target = null;
	private int task = -2;
	private int threshold = 0;
	private List<Long> times1 = new ArrayList<Long>();
	private String town;
	private String[] walk_message;

	
	private int wander_delay;
	
	public NPCQuester(String name) {
		super(getName(name));
		name = getName(name);
		this.entity = null;
		attackTarget = null;
		wander_delay = 30;
		MineQuest.getEventQueue().addEvent(new NPCEvent(100, this));
		if (mode == NPCMode.STORE) {
			delay = 2000;
		}
		idsStore = new ArrayList<Integer>();
		itemStore = new ArrayList<Integer>();
		last_attack = 0;
		last_hit = 0;
		removed = false;
		reach_count = 0;
		generator = new Random();
		cost = 0;
		start_quest_radius = 3;
	}

	public NPCQuester(String name, NPCMode mode, World world, Location location) {
		this.name = getName(name);
		this.mode = mode;
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		double pitch = location.getPitch();
		double yaw = location.getYaw();
		wander_delay = 30;
		if ((mode != NPCMode.QUEST_INVULNERABLE) && (mode != NPCMode.QUEST_VULNERABLE)) {
			create(mode, world, x, y, z, pitch, yaw);
			update();
		} else {
			makeNPC(world.getName(), x, y, z, (float)pitch, (float)yaw);
			health = max_health = 2000;
			startle_task = -2;
			classes = new ArrayList<SkillClass>();
			classes.add(SkillClass.newShell(MineQuest.config.npc_attack_type));
			classes.get(0).setQuester(this);
			kills = new CreatureType[0];
			destroyed = new HashMap<Material, Integer>();
		}
		distance = 0;
		entity = null;
		attackTarget = null;
		MineQuest.getEventQueue().addEvent(new NPCEvent(100, this));
		if (mode == NPCMode.STORE) {
			delay = 2000;
		}
		idsStore = new ArrayList<Integer>();
		itemStore = new ArrayList<Integer>();
		last_attack = 0;
		last_hit = 0;
		removed = false;
		reach_count = 0;
		generator = new Random();
		cost = 0;
		start_quest_radius = 3;
	}
	
	public void activate() {
		if (health <= 0) return;
		if (player == null) return;

		if (attackTarget == null) {
			if ((follow != null) && (follow.getPlayer() != null)) {
				if (mode != NPCMode.PARTY_STAND) {
					if (MineQuest.distance(follow.getPlayer().getLocation(), player.getLocation()) > 4) {
						setTarget(follow.getPlayer().getLocation(), 4, 0);
					}
				}
			}
		} else {
			double distance = MineQuest.distance(player.getLocation(), attackTarget.getLocation());
			if (distance > 1.3) {
				if (MineQuest.mobHandler.getMob(attackTarget) != null) {
					if (MineQuest.mobHandler.getMob(attackTarget).getHealth() <= 0) {
						attackTarget = null;
						return;
					}
				} else {
					if (attackTarget.getHealth() <= 0) {
						attackTarget = null;
						return;
					}
				}
				setTarget(attackTarget.getLocation(), 1.25, 0);
			} else if (distance < 1.2) {
				attack(attackTarget);
			}
		}

		if (target != null) {
			if (reach_count > 30) {
				teleport(target);
				target = null;
				reach_count = 0;
				return;
			} else {
				if (MineQuest.distance(player.getLocation(), target) < speed) {
					double move_x = (target.getX() - player.getLocation().getX());
					double move_z = (target.getZ() - player.getLocation().getZ());
					float yaw = 0;
					yaw = (float)(-180 * Math.atan2(move_x , move_z) / Math.PI);
					entity.setLocation(target.getX(), target.getY(), target.getZ(), yaw, target.getPitch());
					newLocation(player.getLocation());
					reach_count = 0;
	
					target = null;
				} else {
					double distance = MineQuest.distance(player.getLocation(), target);
					double move_x = (speed * (target.getX() - player.getLocation().getX()) / distance);
					double move_y = (speed * (target.getY() - player.getLocation().getY()) / distance);
					double move_z = (speed * (target.getZ() - player.getLocation().getZ()) / distance);
					move_x += (new Random()).nextDouble() * .15;
					move_z += (new Random()).nextDouble() * .15;
					move_y = Ability.getNearestY(player.getWorld(), (int)(player.getLocation().getBlockX() + move_x),
							(int)player.getLocation().getBlockY(), 
							(int)(player.getLocation().getBlockZ() + move_z)) - player.getLocation().getY();
					if ((move_x < .1) && (move_z < .1)) {
						reach_count++;
					}
					if (move_y > 4) {
						move_y = 1;
					}
					float yaw = 0;
					yaw = (float)(-180 * Math.atan2(move_x , move_z) / Math.PI);
					entity.setLocation(
						player.getLocation().getX() + move_x,
						player.getLocation().getY() + move_y,
						player.getLocation().getZ() + move_z,
						yaw, target.getPitch());
				}
			}
		}


		if (rad != 0) {
			count++;
			if (count == wander_delay) {
				setTarget(center, rad, 0);
				count = 0;
			}
		}

		if (walk_message != null) {
			for (Player entity : MineQuest.getSServer().getOnlinePlayers()) {
				if (entity == null) continue;
				if (MineQuest.distance(player.getLocation(), entity.getLocation()) < radius) {
					if ((quest_file == null) || (!MineQuest.questerHandler.getQuester(entity).isCompleted(new QuestProspect(quest_file)))) {
						if (!checkMessage(entity.getEntityId())) {
							int delay = message_delay;
							for (String message : walk_message) {
								if (message != null) {
									if (message.equals("random")) {
										MineQuest.getNPCStringConfiguration().sendRandomWalkMessage(this, MineQuest.questerHandler.getQuester(entity), delay);
									} else {
										MineQuest.getEventQueue().addEvent(
												new MessageEvent(delay, MineQuest
														.questerHandler.getQuester(entity), "<"
														+ name + "> "
														+ message));
									}
									delay += message_delay;
								}
							}
						}
					}
				}
			}
		}

		if (mode == NPCMode.STORE) {
			List<Integer> these_ids = new ArrayList<Integer>();
			if (MineQuest.townHandler.getTown(player) != null) {
				Store store = MineQuest.townHandler.getTown(player).getStore(player);
				
				for (Player player : MineQuest.getSServer().getOnlinePlayers()) {
					if ((MineQuest.townHandler.getTown(player) != null) && store.equals(MineQuest.townHandler.getTown(player).getStore(player))) {
						if (!checkMessageStore(player.getEntityId(), player.getItemInHand().getTypeId())) {
							MineQuest.getNPCStringConfiguration().sendRandomMessage(this, MineQuest.questerHandler.getQuester(player), store);
						}
						these_ids.add(player.getEntityId());
					}
				}
	
				int i;
				for (i = 0; i < idsStore.size(); i++) {
					if (!these_ids.contains(idsStore.get(i))) {
						itemStore.set(i, -1);
					}
				}
			}
		}
	}

    private void attack(LivingEntity attackTarget) {
//		entity.attackLivingEntity(mobTarget);
    	long now = Calendar.getInstance().getTimeInMillis();

    	if (now - last_attack > 500) {
    		last_attack = now;
	    	entity.animateArmSwing();
	    	if (isDead(attackTarget)) {
	    		if (center != null) {
	    			target = center;
	    		}
	    		attackTarget = null;
	    	} else {
				((CraftHumanEntity)player).getHandle().d(((CraftLivingEntity)attackTarget).getHandle());
	    	}
    	}
	}

	private boolean isDead(LivingEntity attackTarget) {
		if (MineQuest.questerHandler.getQuester(attackTarget) != null) {
			if (MineQuest.questerHandler.getQuester(attackTarget).getHealth() <= 0) {
				return true;
			}
		} else if (attackTarget.getHealth() <= 0) {
			return true;
		}
		return false;
	}

	@Override
	public void bind(String name) {
		if ((getAbility(name) != null)) {
			super.bind(name);
		} else {
			sendMessage("I can only cast warrior abilities");
		}
	}
	
	public void buyNPC(Quester quester) {
		if (quester.getCubes() > getCost()) {
			if (MineQuest.economy != null)
			{
				quester.withdrawBalance(cubes);
			}
			else
			{
				quester.setCubes(quester.getCubes() - cubes);
			}
			quester.addNPC(this);
			setMode(NPCMode.PARTY);
			quester.sendMessage(name + " joined your party!");
		} else {
			quester.sendMessage("You don't have enough cubes");
		}
	}
	
	@Override
	public boolean canCast(List<ItemStack> list, int mana) {
		if (follow != null) {
			return follow.canCast(list, mana);
		}
		
		return false;
	}
	
	@Override
	public boolean checkItemInHand() {
		if (player == null) return false;
		if (mode == NPCMode.QUEST_VULNERABLE) return false;
		if (mode == NPCMode.QUEST_INVULNERABLE) return false;
		if (mode == NPCMode.VULNERABLE) return false;
		PlayerInventory inven = null;
		if ((follow != null) && (follow.getPlayer() != null)) {
			inven = follow.getPlayer().getInventory();
		}
		ItemStack item = player.getItemInHand();

		if (!canUse(item)) {
			if ((inven != null) && (inven.firstEmpty() != -1)) {
				inven.addItem(item);
			} else {
				player.getWorld().dropItem(player.getLocation(), item);
			}
			
			setProperty("item", "0,0,0");
			
			sendMessage("You are not proficient with that item");
			
			return true;
		}

		return false;
	}
	
	private boolean checkMessage(int id) {
	    Calendar now = Calendar.getInstance();
	    int i;
	    
		for (i = 0; i < ids1.size(); i++) {
			if (ids1.get(i) == id) {
				if ((now.getTimeInMillis() - times1.get(i)) > delay) {
					times1.set(i, now.getTimeInMillis());
					return false;
				} else {
					return true;
				}
			}
		}
	    
	    ids1.add(id);
	    times1.add(now.getTimeInMillis());
	    
	    return false;
    }
	
	private boolean checkMessageStore(int id, int item) {
	    int i;
	    
		for (i = 0; i < idsStore.size(); i++) {
			if (idsStore.get(i) == id) {
				if (itemStore.get(i) != item) {
					itemStore.set(i, item);
					return false;
				} else {
					return true;
				}
			}
		}
	    
	    idsStore.add(id);
	    itemStore.add(item);
	    
	    return false;
    }
	
	public void clearTarget(Player player) {
		if (player == null) return;
		if (attackTarget == null) return;
		if (attackTarget.getEntityId() == player.getEntityId()) {
			attackTarget = null;
		}
	}
	
	public void create(NPCMode mode, World world, double x, double y, double z, double pitch, double yaw) {
		if (mode != NPCMode.QUEST_INVULNERABLE) {
			super.create();
	
			MineQuest.getSQLServer().aupdate("UPDATE questers SET x='" + 
					x + "', y='" + 
					y + "', z='" + 
					z + "', pitch='" + 
					pitch + "', yaw='" + 
					yaw + "', mode='" + 
					mode + "', world='" + 
					world.getName() + "' WHERE name='"
					+ getSName() + "'");
		}
	}
	
	public void despawn() {
		if (entity == null) return;
		Location location = player.getLocation();
		this.center = new Location(location.getWorld(), location.getX(), 
				location.getY(), location.getZ(), location.getYaw(), 
				location.getPitch());

		if ((player != null) && (player.getItemInHand() != null) && (player.getItemInHand().getType() != Material.AIR)) {
			hand = player.getItemInHand();
			player.setItemInHand(null);
		} else {
			hand = null;
		}
		player.setHealth(0);
		MineQuest.getNPCManager().despawn(name);
		entity = null;
		player = null;
	}
	
	@Override
	public void expClassGain(int class_exp) {
		if (getClass("Warrior") != null) {
			getClass("Warrior").expAdd(class_exp);
		}
	}
	
	public int getCost() {
		int cost = (level * level + 1) * MineQuest.config.npc_cost;
		
		if (getClass("Warrior") != null) {
			cost += (getClass("Warrior").getLevel() * getClass("Warrior").getLevel() + 1) * MineQuest.config.npc_cost_class;
		}
		if (getClass("Archer") != null) {
			cost += (getClass("Archer").getLevel() * getClass("Archer").getLevel() + 1) * MineQuest.config.npc_cost_class;
		}
		if (getClass("WarMage") != null) {
			cost += (getClass("WarMage").getLevel() * getClass("WarMage").getLevel() + 1) * MineQuest.config.npc_cost_class;
		}
		if (getClass("PeaceMage") != null) {
			cost += (getClass("PeaceMage").getLevel() * getClass("PeaceMage").getLevel() + 1) * MineQuest.config.npc_cost_class;
		}
		
		return cost;
	}
	
	public NPCEntity getEntity() {
		return entity;
	}

	public Quester getFollow() {
		return follow;
	}
	
	public NPCMode getMode() {
		return mode;
	}
	
	public Town getNPCTown() {
		return MineQuest.townHandler.getTown(town);
	}

	@Override
	public String getSName() {
		return super.getSName().replaceAll(" ", "_");
	}

	public LivingEntity getTarget() {
		return attackTarget;
	}

	public void giveItem(Quester quester) {
		ItemStack spare = null;
		if (player.getItemInHand() != null) {
			if (player.getItemInHand().getType() != Material.AIR) {
				spare = new ItemStack(player.getItemInHand().getType(), player.getItemInHand().getAmount());
				if (player.getItemInHand().getData() != null) {
					spare.setData(player.getItemInHand().getData());
				}
				spare.setDurability(player.getItemInHand().getDurability());
			}
		}
		
		ItemStack item = quester.getPlayer().getItemInHand();
		String value = item.getTypeId() + "," + item.getAmount() + "," + item.getDurability();
		if (item.getData() != null) {
			value = value + "," + item.getData().getData();
		}
		setProperty("item", value);
		quester.getPlayer().setItemInHand(null);
		
		if (spare != null) {
			quester.getPlayer().getInventory().addItem(spare);
		}
	}

	public void handleProperty(String property, String value) {
		if ((value != null) && value.equals("null")) {
			value = null;
		}
		if (property.equals("radius")) {
			this.radius = Integer.parseInt(value);
		} else if (property.startsWith("hit_message")) {
			if (hit_message == null) {
				hit_message = new String[10];
			}
			int index;
			try {
				index = Integer.parseInt(property.replaceAll("hit_message", ""));
			} catch (Exception e) {
				MineQuest.delayUpdate("INSERT INTO npc " + 
						" (name, property, value) VALUES('" + getSName() + "', '" + "hit_message0" + "', '" + value + "')");
				MineQuest.delayUpdate("DELETE FROM npc WHERE name='" + getSName() + "' AND property='hit_message'");
				return;
			}
			if (index > 9) {
				MineQuest.log("NPC Hit Message limited to 10");
			}
			this.hit_message[index] = value;
		} else if (property.startsWith("walk_message")) {
			if (walk_message == null) {
				walk_message = new String[10];
			}
			int index;
			try {
				index = Integer.parseInt(property.replaceAll("walk_message", ""));
			} catch (Exception e) {
				MineQuest.delayUpdate("INSERT INTO npc " + 
						" (name, property, value) VALUES('" + getSName() + "', '" + "walk_message0" + "', '" + value + "')");
				MineQuest.delayUpdate("DELETE FROM npc WHERE name='" + getSName() + "' AND property='walk_message'");
				return;
			}
			if (index > 9) {
				MineQuest.log("NPC Walk Message limited to 10");
			}
			this.walk_message[index] = value;
		} else if (property.equals("health_threshold")) {
			this.threshold  = Integer.parseInt(value);
		} else if (property.equals("start_quest_radius")) {
			this.start_quest_radius  = Integer.parseInt(value);
		} else if (property.equals("start_quest")) {
			this.start_quest = value;
			MineQuest.log("Start quest " + value);
		} else if (property.equals("next_task")) {
			this.task = Integer.parseInt(value);
		} else if (property.equals("fix_amount")) {
			this.fix = Integer.parseInt(value);
		} else if (property.equals("heal_amount")) {
			this.heal_amount = Integer.parseInt(value);
		} else if (property.equals("startle_task")) {
			this.startle_task = Integer.parseInt(value);
		} else if (property.equals("message_delay")) {
			this.message_delay = Integer.parseInt(value);
		} else if (property.equals("health")) {
			health = max_health = Integer.parseInt(value);
		} else if (property.equals("cost")) {
			cost = Long.parseLong(value);
		} else if (property.equals("mob_protected")) {
			mob_protected = Boolean.parseBoolean(value);
		} else if (property.equals("item_in_hand")) {
			ItemStack item = null; 
			try {
				int id = Integer.parseInt(value);
				if (id > 0) {
					item = new ItemStack(id, 1);
				}
			} catch (Exception e) {
			}
			hand = item;
			if (player != null) {
				if (item.getTypeId() <= 0) {
					player.setItemInHand(null);
				} else {
					player.setItemInHand(item);
				}
			}
		} else if (property.equals("steal")) {
			String split[] = value.split(",");
			steal = new ItemStack(Integer.parseInt(split[0]), 
					Integer.parseInt(split[1]));
		} else if (property.equals("item_drop")) {
			if (value.contains(":")) {
				String split[] = value.split(":");
				item_drop = new ItemStack(Integer.parseInt(split[0]), 1);
				item_drop.setData(new MaterialData(Integer.parseInt(split[0]), Byte.parseByte(split[1])));
			} else {
				item_drop = new ItemStack(Integer.parseInt(value), 1);
			}
		} else if (property.equals("quest")) {
			if ((value == null) || (value.equals("null"))) {
				this.quest_file = null;
			}
			this.quest_file = value;
		} else if (property.equals("follow")) {
			this.follow_name = value;
			if ((value == null) || (value.equals("null"))) {
				this.follow_name = null;
			}
			if (follow != null) {
				follow.remNPC(this);
				follow = null;
			}
			if (follow_name != null) {
				this.follow = MineQuest.questerHandler.getQuester(value);
				if (follow != null) {
					follow.addNPC(this);
				}
			}
		} else if (property.equals("town")) {
			this.town = value;
		} else if (property.equals("wander_radius")) {
			this.rad = Integer.parseInt(value);
		} else if (property.equals("wander_delay")) {
			this.wander_delay = Integer.parseInt(value);
		} else if (property.equals("item")) {
			String split[] = value.split(",");
			ItemStack item = new ItemStack(Integer.parseInt(split[0]), 
					Integer.parseInt(split[1]));
			
			item.setDurability(Short.parseShort(split[2]));
			if (split.length > 3) {
				MaterialData md = new MaterialData(Integer.parseInt(split[0]));
				md.setData(Byte.parseByte(split[3]));
				item.setData(md);
			}
			if (item.getTypeId() > 0) {
				hand = item;
			}
			if (player != null) {
				if (item.getTypeId() <= 0) {
					player.setItemInHand(null);
				} else {
					player.setItemInHand(item);
				}
			}
		} else {
			MineQuest.log("Warning: Invalid NPC Property: " + property);
		}
	}

	@Override
	public boolean healthChange(int change, EntityDamageEvent event) {
		boolean ret = false;
		Calendar now = Calendar.getInstance();
		
		if (now.getTimeInMillis() - last_hit <= 10) {
			return false;
		}
		last_hit = now.getTimeInMillis();
		
		if (isInvulnerable()) {
			health = max_health;
			event.setDamage(0);
			event.setCancelled(true);

			if (mode == NPCMode.STORE) {
				LivingEntity entity = null;
				if (event instanceof EntityDamageByEntityEvent) {
					if (((EntityDamageByEntityEvent)event).getDamager() instanceof Projectile) {
						entity = ((Projectile)((EntityDamageByEntityEvent)event).getDamager()).getShooter();
					} else {
						entity = (LivingEntity) ((EntityDamageByEntityEvent)event).getDamager();
					}
				}
				
				if (entity instanceof HumanEntity) {
					HumanEntity human = (HumanEntity)entity;
					ItemStack hand = human.getItemInHand();
					MineQuest.townHandler.getTown(player).getStore(player).setKeeper(this);
					if (checkMessage(entity.getEntityId()) && checkMessageStore(human.getEntityId(), human.getItemInHand().getTypeId())) {
						MineQuest.townHandler.getTown(player).getStore(player).sell(MineQuest.questerHandler.getQuester(human), hand.getTypeId(), hand.getAmount());
					} else {
						StoreBlock block = MineQuest.townHandler.getTown(player).getStore(player).getBlock(hand.getTypeId());
						checkMessageStore(human.getEntityId(), hand.getTypeId());
						if (block != null) {
							MineQuest.getNPCStringConfiguration().sendWantMessage(this, MineQuest.questerHandler.getQuester(human), MineQuest.townHandler.getTown(player).getStore(player));
						} else {
							MineQuest.getNPCStringConfiguration().sendNotWantMessage(this, MineQuest.questerHandler.getQuester(human), MineQuest.townHandler.getTown(player).getStore(player));
						}
					}
				}
			}
			
			if ((event instanceof EntityDamageByEntityEvent) && 
					(((EntityDamageByEntityEvent)event).getDamager() instanceof Player)) {
				Player player = (Player)((EntityDamageByEntityEvent)event).getDamager();
				Quester quester = MineQuest.questerHandler.getQuester(player);
				
				if (quester.isDebug()) {
					quester.sendMessage("My name is " + getName());
				}
				if ((cost == 0) || (quester.canPay(cost))) {
					paidHit(quester);
				}
			}
		} else {
			LivingEntity entity = null;
			if (event instanceof EntityDamageByEntityEvent) {
				if (((EntityDamageByEntityEvent)event).getDamager() instanceof Projectile) {
					entity = ((Projectile)((EntityDamageByEntityEvent)event).getDamager()).getShooter();
				} else {
					if (((EntityDamageByEntityEvent)event).getDamager() instanceof LivingEntity) {
						entity = (LivingEntity) ((EntityDamageByEntityEvent)event).getDamager();
					}
				}
			}
			if ((follow != null) && (mode != NPCMode.QUEST_INVULNERABLE) && (mode != NPCMode.QUEST_VULNERABLE)) {
				if ((entity instanceof Player) && ((Player)entity).getName().equals(follow.getName())) {
					PlayerInteractEvent pie = new PlayerInteractEvent(getPlayer(), null, ((Player)entity).getItemInHand(), null, null);

					if (!healthIncrease(pie)) {
						if (attackTarget != null) {
							attackTarget = null;
							sendMessage("Regrouping!");
						} else {
							if ((mode == NPCMode.PARTY_STAND)) {
								mode = NPCMode.PARTY;
								sendMessage("I'll follow your lead");
							} else {
								mode = NPCMode.PARTY_STAND;
								sendMessage("I'll stay here");
							}
						}
					} else {
						Player player = (Player)entity;
						if ((player.getItemInHand() != null) &&
								(player.getItemInHand().getAmount() > 1)) {
							player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
						} else {
							player.setItemInHand(null);
						}
						sendMessage("Health: " + health + "/" + max_health);
					}
					event.setCancelled(true);
				} else {
					if (attackTarget == null) {
						if (!(entity instanceof Player)
								|| (!((Player) entity).getName().equals(name))) {
							if (NPCMode.FOR_SALE != mode) {
								attackTarget = entity;

								if (MineQuest.questerHandler.getQuester(attackTarget) instanceof NPCQuester) {
									NPCQuester npc = (NPCQuester)MineQuest.questerHandler.getQuester(attackTarget);
									if (npc.isMerc()) {
										sendMessage("Its not good for business to attack other mercenaries");
									}
									attackTarget = null;
								}
							}
						}
					}
				}
			}
			if (mode == NPCMode.VULNERABLE) {
				if (attackTarget == null) {
					if (!((Player) entity).getName().equals(name)) {
						attackTarget = entity;
					}
				}
			}
			ret = super.healthChange(change, event);


			if (mode == NPCMode.QUEST_VULNERABLE) {
				 if (getHealth() <= threshold) {
					 if (quest != null) {
						 quest.issueNextEvents(task);
					 }
				 }
			}
			if (getHealth() <= 0) {
				setPlayer(null);
				if ((mode != NPCMode.QUEST_VULNERABLE) && (mode != NPCMode.VULNERABLE)) {
					if ((mode != NPCMode.PARTY) && (mode != NPCMode.FOR_SALE) && (mode != NPCMode.PARTY_STAND)) {
						removeSql();
						MineQuest.questerHandler.remQuester(this);
						MineQuest.getNPCManager().despawn(name);
						player = null;
						entity = null;
					} else {
						Location location = MineQuest.townHandler.getTown(town).getNPCSpawn();
	
						sendMessage("Died!");
						setProperty("follow", null);
						mode = NPCMode.FOR_SALE;
						attackTarget = null;
						target = null;
						makeNPC(location.getWorld().getName(), location.getX(),
								location.getY(), location.getZ(), location
										.getPitch(), location.getYaw());
					}
					health = max_health;
				} else {
					setHealth(0);
				}
			}
		}
		
		return ret;
	}

	public boolean inChunk(Chunk chunk) {
		if ((player == null) && (center == null)) return false;
		if (chunk == null) return false;
		if (player == null) {
			if (center.getWorld() == null) return false;
		} else {
			if (player.getWorld() == null) return false;
		}
		Chunk my_chunk;
		if (player != null) {
			my_chunk = player.getWorld().getChunkAt(player.getLocation());
		} else {
			my_chunk = center.getWorld().getChunkAt(center);
		}
		
		if (my_chunk.getWorld().getName().equals(chunk.getWorld().getName())) {
			if (my_chunk.getX() == chunk.getX()) {
				if (my_chunk.getZ() == chunk.getZ()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean isInvulnerable() {
		if ((mode != NPCMode.FOR_SALE) && 
				(mode != NPCMode.PARTY) && 
				(mode != NPCMode.QUEST_VULNERABLE) && 
				(mode != NPCMode.VULNERABLE) && 
				(mode != NPCMode.PARTY_STAND)) {
			return true;
		}
		return false;
	}

	private boolean isMerc() {
		if ((mode == NPCMode.FOR_SALE) || (mode == NPCMode.PARTY) || (mode == NPCMode.PARTY_STAND)) {
			return true;
		}
		return false;
	}

	public boolean isProtected() {
		return mob_protected;
	}

	public boolean isRemoved() {
		return removed;
	}

	private void makeNPC(String world, double x, double y, double z,
			float pitch, float yaw) {
		if (entity != null) {
			if ((player != null) && (player.getItemInHand() != null) && (player.getItemInHand().getType() != Material.AIR)) {
				hand = player.getItemInHand();
				player.setItemInHand(null);
			} else {
				hand = null;
			}
			if (player != null) {
				player.setHealth(0);
			}
			MineQuest.getNPCManager().despawn(name);
			entity = null;
			player = null;
		}
		MineQuest.getEventQueue().addEvent(new SpawnNPCEvent(200, this, world, x, y, z, (float)pitch, (float)yaw));
	}
	
	private Location newLocation(Location location) {
		Location loc = new Location(location.getWorld(), location.getX(),
				location.getX(), location.getZ(), location.getYaw(), location
						.getPitch());
		return loc;
	}
	
	private void paidHit(Quester quester) {
		int delay = message_delay;
		if (hit_message != null) {
			for (String message : hit_message) {
				if (message != null) {
					if (hit_message.equals("random")) {
						MineQuest.getNPCStringConfiguration().sendRandomHitMessage(this, quester, delay);
					} else {
						MineQuest.getEventQueue().addEvent(
								new MessageEvent(delay, quester, "<" + name + "> " + message));
					}
				}
				delay += message_delay;
			}
		}
		if (quest_file != null) {
			if (!quester.isCompleted(new QuestProspect(quest_file))) {
				quester.addQuestAvailable(new QuestProspect(quest_file));
			}
		}
		if (start_quest != null) {
			MineQuest.getEventQueue().addEvent(new StartQuestEvent(10, new AreaTarget(player.getLocation(), start_quest_radius), start_quest));
		}
		if (fix > 0) {
			Player player = quester.getPlayer();
			if (player.getItemInHand() != null) {
				short newDurability = (short) (player.getItemInHand().getDurability() - fix);
				if (newDurability < 0) {
					newDurability = 0;
				}
				ItemStack item = player.getItemInHand();
				item.setDurability((short)newDurability);
				player.setItemInHand(item);
			}
		}
		if (steal != null) {
			quester.getPlayer().getInventory().removeItem(steal);
		}
		if (item_drop != null) {
			Player player = quester.getPlayer();
			player.getWorld().dropItemNaturally(player.getLocation(), item_drop.clone());
		}
		quester.setHealth(quester.getHealth() + heal_amount);
	}

	public void questerAttack(LivingEntity entity) {
		if (NPCMode.PARTY_STAND != mode) {
			if ((attackTarget == null) || (attackTarget.getHealth() <= 0)) {
				attackTarget = entity;

				if (MineQuest.questerHandler.getQuester(attackTarget) instanceof NPCQuester) {
					NPCQuester npc = (NPCQuester)MineQuest.questerHandler.getQuester(attackTarget);
					if (npc.isMerc()) {
						sendMessage("Its not good for business to attack other mercenaries");
					}
					attackTarget = null;
				}
			}
		}
	}

	public void redo() {
		if (player != null) {
			teleport(player.getLocation());
		}
	}

	public void remNPC() {
		if ((mode != NPCMode.QUEST_INVULNERABLE) && (mode != NPCMode.QUEST_VULNERABLE)) {
			removeSql();
		}
		MineQuest.questerHandler.remQuester(this);
		MineQuest.getNPCManager().despawn(name);
//		NpcSpawner.RemoveBasicHumanNpc(this.entity);
//		MineQuest.log("NPC Died");
		entity = null;
		player = null;
	}
	
	public void removeSql() {
		removed = true;
		try {
			MineQuest.getSQLServer().aupdate("DELETE FROM questers WHERE name='" + getSName() + "'");
		} catch (Exception e) {
		}
		try {
			MineQuest.getSQLServer().aupdate("DELETE FROM binds WHERE name='" + getSName() + "'");
		} catch (Exception e) {
		}
		try {
			MineQuest.getSQLServer().aupdate("DELETE FROM chests WHERE name='" + getSName() + "'");
		} catch (Exception e) {
		}
		try {
			MineQuest.getSQLServer().aupdate("DELETE FROM kills WHERE name='" + getSName() + "'");
		} catch (Exception e) {
		}
		try {
			MineQuest.getSQLServer().aupdate("DELETE FROM npc WHERE name='" + getSName() + "'");
		} catch (Exception e) {
		}
		try {
			MineQuest.getSQLServer().aupdate("DELETE FROM quests WHERE name='" + getSName() + "'");
		} catch (Exception e) {
		}
	}
	
	public void respawn() {
		if (center == null) return;
		teleport(center);
	}
	
	@Override
	public void save() {
		if (mode == NPCMode.QUEST_INVULNERABLE) return;
		if (mode == NPCMode.QUEST_VULNERABLE) return;
		super.save();
		if (mode == null) return;
		
		if (entity == null) return;
		Location loc = entity.getBukkitEntity().getLocation();
		if ((mode != NPCMode.PARTY) && (mode != NPCMode.PARTY_STAND)) {
			loc = center;
		}
		
		MineQuest.getSQLServer().aupdate("UPDATE questers SET x='" + 
				loc.getX() + "', y='" + 
				loc.getY() + "', z='" + 
				loc.getZ() + "', mode='" + 
				mode + "', world='" + 
				entity.getBukkitEntity().getWorld().getName() + "' WHERE name='"
				+ getSName() + "'");
	}
	
	@Override
	public void sendMessage(String string) {
		if ((NPCMode.PARTY == mode) || (mode == NPCMode.PARTY_STAND)) {
			if (follow != null) {
				follow.sendMessage(name + " : " + string);
			}
		}
	}
	
	public void setEntity(NPCEntity entity) {
		if (removed) return;
		if (this.entity != null) {
			MineQuest.getNPCManager().despawn(name);
			this.entity = null;
			player = null;
		}
		this.entity = entity;
		setPlayer((Player)entity.getBukkitEntity());
		if (hand != null) {
			player.setItemInHand(hand);
			hand = null;
		}
		newLocation(player.getLocation());
	}
	
	public void setFollow(Quester quester) {
		setProperty("follow", quester == null?null:quester.getName());
		target = null;
	}

	@Override
	public void setHealth(int i) {
		super.setHealth(i);

		if (mode == NPCMode.QUEST_VULNERABLE) {
			 if (getHealth() <= threshold) {
				 if (quest != null) {
					 quest.issueNextEvents(task);
				 }
			 }
		}
		if (getHealth() <= 0) {
			setPlayer(null);
			if (mode == NPCMode.VULNERABLE) {
				if ((player != null) && (player.getItemInHand() != null) && (player.getItemInHand().getType() != Material.AIR)) {
					hand = player.getItemInHand();
					player.setItemInHand(null);
				} else {
					hand = null;
				}
				if (player != null) {
					player.setHealth(0);
				}
				entity = null;
				MineQuest.getNPCManager().despawn(name);
				MineQuest.getEventQueue().addEvent(new SpawnNPCEvent(MineQuest.config.npc_vulnerable_delay, this, center.getWorld().getName(), center.getX(), center.getY(), 
						center.getZ(), center.getPitch(), center.getYaw()));
//				makeNPC(center.getWorld().getName(), center.getX(), center.getY(), 
//						center.getZ(), center.getPitch(), center.getYaw());
			} else if ((mode != NPCMode.PARTY_STAND) && (mode != NPCMode.PARTY) && (mode != NPCMode.FOR_SALE)) {
				if ((mode != NPCMode.QUEST_INVULNERABLE) && (mode != NPCMode.QUEST_VULNERABLE)) {
					removeSql();
				}
				MineQuest.questerHandler.remQuester(this);
				MineQuest.getNPCManager().despawn(name);
//				NpcSpawner.RemoveBasicHumanNpc(this.entity);
//				MineQuest.log("NPC Died");
				entity = null;
				player = null;
			} else {
				Location location = MineQuest.townHandler.getTown(town).getNPCSpawn();

				sendMessage("Died!");
				mode = NPCMode.FOR_SALE;
				setProperty("follow", null);
				attackTarget = null;
				target = null;
				makeNPC(location.getWorld().getName(), location.getX(),
						location.getY(), location.getZ(), location
								.getPitch(), location.getYaw());
			}
			health = max_health;
			return;
		}
		if (isInvulnerable()) {
			health = max_health;
		}
	}

	public void setMode(NPCMode mode) {
		this.mode = mode;
		target = null;
	}
	
	public void setProperty(String property, String value) {
		handleProperty(property, value);

		if ((mode != NPCMode.QUEST_INVULNERABLE) && (mode != NPCMode.QUEST_VULNERABLE)) {
			MineQuest.getSQLServer().update("DELETE FROM npc WHERE property='" + property + "' AND name='" + getSName() + "'");
			MineQuest.getSQLServer().aupdate("INSERT INTO npc " + 
					" (name, property, value) VALUES('" + getSName() + "', '" + property + "', '" + value + "')");
		}
	}

	public void setTarget(LivingEntity entity) {
		attackTarget = entity;

		if (MineQuest.questerHandler.getQuester(attackTarget) instanceof NPCQuester) {
			NPCQuester npc = (NPCQuester)MineQuest.questerHandler.getQuester(attackTarget);
			if (npc.isMerc()) {
				sendMessage("Its not good for business to attack other mercenaries");
			}
			attackTarget = null;
		}
		if (entity == null) {
			if ((follow != null) && (follow.getPlayer() != null) && (follow.getPlayer().getLocation() != null)) {
				if ((mode == NPCMode.PARTY_STAND) || (mode == NPCMode.PARTY)) {
					if ((player != null) || (MineQuest.distance(follow.getPlayer().getLocation(), player.getLocation()) > 100)) {
							Player player = follow.getPlayer();
							teleport(player.getLocation());
					}
				}
			}
		}
	}
	
	public void setTarget(Location location) {
		target = location;
		attackTarget = null;
	}

	private void setTarget(Location location, double rad, int call) {
//		Location self = entity.getBukkitEntity().getLocation();
//		double distance = MineQuest.distance(location, self);
		double angle = generator.nextDouble() * Math.PI * 2;
		double length = generator.nextDouble() * rad;
		double x = length * Math.cos(angle);
		double z = length * Math.sin(angle);
		target = new Location(location.getWorld(), 
				location.getX() + x,
				Ability.getNearestY(location.getWorld(), 
						(int)(location.getX() + x), (int)location.getY(), 
						(int)(location.getZ() + z)),
				location.getZ() + z,
				location.getYaw(),
				location.getPitch());
		if (MineQuest.distance(target, location) > 10) {
			target = null;
		} else {
			if (Math.abs(target.getY() - location.getY()) > 5) {
				target.setY(location.getY());
			}
			if ((target.getWorld().getBlockAt(target).getType() != Material.AIR) && 
					(target.getWorld().getBlockAt(target).getType() != Material.FIRE) &&
					(target.getWorld().getBlockAt(target).getType() != Material.TORCH) &&
					(target.getWorld().getBlockAt(target).getType() != Material.SIGN) &&
					(target.getWorld().getBlockAt(target).getType() != Material.WALL_SIGN) &&
					(target.getWorld().getBlockAt(target).getType() != Material.SNOW)) {
				target = null;
				if (call < 20) {
					setTarget(location, rad, call + 1);
				} else {
					reach_count++;
				}
			}
		}
	}

	public void setTown(String town) {
		this.town = town;
		MineQuest.getSQLServer().update("DELETE FROM npc WHERE property='town' AND name='" + getSName() + "'");
		MineQuest.getSQLServer().aupdate("INSERT INTO npc " + 
				" (name, property, value) VALUES('" + getSName() + "', 'town', '" + town + "')");
	}
	
	@Override
	public void startled() {
		quest.issueNextEvents(startle_task);
	}
	
	@Override
	public ItemStack stolen() {
		if (steal != null) {
			return steal;
		} else {
			return null;
		}
	}

	@Override
	public void targeted(EntityTargetEvent event) {
		super.targeted(event);
		
		if (isProtected() || isInvulnerable()) {
			event.setCancelled(true);
			if (event.getEntity() instanceof Creature) {
				((Creature)event.getEntity()).setTarget(null);
			}
		}
	}

	public void teleport(Location location) {
		makeNPC(location.getWorld().getName(), location.getX(), location.getY(), 
				location.getZ(), location.getPitch(), location.getYaw());
	}

	public void update() {
		if (mode == NPCMode.QUEST_INVULNERABLE) {
			return;
		}
		if (mode == NPCMode.QUEST_VULNERABLE) {
			return;
		}
		super.update();

		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM questers WHERE name='" + getSName() + "'");

		try {
			if (!results.next()) return;
			String mode_string = results.getString("mode");
			this.mode = NPCMode.getNPCMode(mode_string);
			double x = results.getDouble("x");
			double y = results.getDouble("y");
			double z = results.getDouble("z");
			float pitch = (float)results.getDouble("pitch");
			float yaw = (float)results.getDouble("yaw");
			String world = results.getString("world");
			center = new Location(MineQuest.getSServer().getWorld(world), x, y, z, yaw, pitch);
			makeNPC(world, x, y, z, (float)pitch, (float)yaw);
		} catch (SQLException e) {
			MineQuest.log("Unable to add NPCQuester");
		}
		
		results = MineQuest.getSQLServer().query("SELECT * FROM npc WHERE name='" + getSName() + "'");
		
		this.radius = 0;
		this.hit_message = null;
		this.walk_message = null;
		this.quest_file = null;
		this.town = null;
		this.rad = 0;
			
		try {
			while (results.next()) {
				String property = results.getString("property");
				String value = results.getString("value");
				handleProperty(property, value);
			}
		} catch (Exception e) {
			MineQuest.log("Problem getting NPC Properties");
		}
	}
}