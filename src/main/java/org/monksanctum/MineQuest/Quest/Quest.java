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
package org.monksanctum.MineQuest.Quest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.CancelEvent;
import org.monksanctum.MineQuest.Event.CompleteQuestEvent;
import org.monksanctum.MineQuest.Event.Event;
import org.monksanctum.MineQuest.Event.ExperienceEvent;
import org.monksanctum.MineQuest.Event.MessageEvent;
import org.monksanctum.MineQuest.Event.QuestAvailableEvent;
import org.monksanctum.MineQuest.Event.TargetEvent;
import org.monksanctum.MineQuest.Event.Absolute.AdvancedBlockEvent;
import org.monksanctum.MineQuest.Event.Absolute.AreaEvent;
import org.monksanctum.MineQuest.Event.Absolute.ArrowEvent;
import org.monksanctum.MineQuest.Event.Absolute.BlockCDEvent;
import org.monksanctum.MineQuest.Event.Absolute.BlockDCEvent;
import org.monksanctum.MineQuest.Event.Absolute.BlockEvent;
import org.monksanctum.MineQuest.Event.Absolute.CanEditPattern;
import org.monksanctum.MineQuest.Event.Absolute.CreateBoatEvent;
import org.monksanctum.MineQuest.Event.Absolute.EntitySpawnerCompleteEvent;
import org.monksanctum.MineQuest.Event.Absolute.EntitySpawnerCompleteNMEvent;
import org.monksanctum.MineQuest.Event.Absolute.EntitySpawnerEvent;
import org.monksanctum.MineQuest.Event.Absolute.EntitySpawnerNoMove;
import org.monksanctum.MineQuest.Event.Absolute.ExplosionEvent;
import org.monksanctum.MineQuest.Event.Absolute.HealthEntitySpawn;
import org.monksanctum.MineQuest.Event.Absolute.LightningEvent;
import org.monksanctum.MineQuest.Event.Absolute.LockWorldTime;
import org.monksanctum.MineQuest.Event.Absolute.PartyDestroy;
import org.monksanctum.MineQuest.Event.Absolute.PartyHealthEvent;
import org.monksanctum.MineQuest.Event.Absolute.PartyKill;
import org.monksanctum.MineQuest.Event.Absolute.QuestEvent;
import org.monksanctum.MineQuest.Event.Absolute.SingleAreaEvent;
import org.monksanctum.MineQuest.Event.Absolute.WeatherEvent;
import org.monksanctum.MineQuest.Event.Idle.IdleEvent;
import org.monksanctum.MineQuest.Event.Relative.RelativeEvent;
import org.monksanctum.MineQuest.Event.Target.TargetedEvent;
import org.monksanctum.MineQuest.Quest.CanEdit.CanEdit;
import org.monksanctum.MineQuest.Quest.Instance.NewChunkRegionLoader;
import org.monksanctum.MineQuest.Quester.NPCMode;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class Quest {
	private List<Quester> questers;
	private List<QuestTask> tasks;
	private List<Event> events;
	private Location spawn;
	private Party party;
	private World world;
	private CanEdit[] edits;
	private double start_x;
	private double start_y;
	private double start_z;
	private double end_x;
	private double end_y;
	private double end_z;
	private List<NPCQuester> npcs;
	private List<Target> targets;
	private String name;
	private String filename;
	private boolean repeatable;
	private boolean reset;
	private String edit_message;
	private AreaPreserver areaPreserver;
	private boolean no_mobs;
	
	public Quest(String filename, Party party) {
		if (setupQuest(filename, party)) {
			for (Quester quester : party.getQuesters()) {
				quester.setQuest(this, world);
			}

			MineQuest.getEventQueue().addEvent(new QuestEvent(this, 100, 0));
		}
	}
	
	public Quest(String file, Party party, int id) {
		if (setupQuest(file, party)) {
			if (id != -1) {
				for (Quester quester : party.getQuesters()) {
					quester.setQuest(this, world);
				}
				MineQuest.getEventQueue().addEvent(new QuestEvent(this, 100, id));
			}
		}
	}
	
	protected boolean setupQuest(String filename, Party party) {
		this.questers = party.getQuesters();
		this.party = party;
		tasks = new ArrayList<QuestTask>();
		events = new ArrayList<Event>();
		edits = new CanEdit[0];
		npcs = new ArrayList<NPCQuester>();
		targets = new ArrayList<Target>();
		this.name = filename;
		this.filename = filename;
		this.repeatable = false;
		this.reset = true;
		this.no_mobs = false;

		edit_message = "A Mystical Force is keeping you from Modifying the world!";
		try {
			BufferedReader bis;
			if (filename.equals("MineQuest/main.script")) {
				bis = new BufferedReader(new FileReader(filename));
			} else {
				bis = new BufferedReader(new FileReader(filename + ".quest"));
			}
			
			String line = "";
			int number = 0;
			if ((questers.size() > 0) && (questers.get(0).getPlayer() != null)) {
				world = questers.get(0).getPlayer().getWorld();
			} else if (filename.equals("MineQuest/main.script")) {
				world = MineQuest.getSServer().getWorlds().get(0);
			}
			spawn = null;
			start_x = 0;
			areaPreserver = null;
			try {
				while ((line = bis.readLine()) != null) {
					number++;
					String split[] = line.split(":");
					if (split == null) split = new String[] {line};
					parseLine(split);
				}
			} catch (Exception e) {
				MineQuest.log("Unable to load Quest Problem on Line " + number);
				MineQuest.log("  " + line);
				try {
					issueNextEvents(-1);
				} catch (Exception e1) {
					MineQuest.log("Unable to unload events properly");
				}
				return false;
			}

			if (world == null) {
				world = MineQuest.getSServer().getWorlds().get(0);
			}
			if (spawn == null) {
				spawn = world.getSpawnLocation();
			}
			
//			for (QuestTask task : tasks) {
//				MineQuest.log("Task: " + task.getId());
//				for (Event event : task.getEvents()) {
//					MineQuest.log(event.getName());
//				}
//			}
			
			if (no_mobs) {
				MineQuest.mobHandler.noMobs(world);
			}
			
			return true;
		} catch (Exception e) {
			MineQuest.log("Unable to load Quest - Generic Error");
			e.printStackTrace();
			try {
				issueNextEvents(-1);
			} catch (Exception e1) {
				MineQuest.log("Unable to unload events properly");
			}
			
			return false;
		}
	}

	public QuestProspect getProspect() {
		return new QuestProspect(name, filename, repeatable);
	}
	
	private void parseLine(String[] split) throws Exception {
		if (split[0].equals("Event")) {
			if (split[2].equals("R")) {
				events.add(RelativeEvent.newRelative(split, this));
			} else if (split[2].equals("T")) {
				events.add(TargetedEvent.newTargeted(split, this));
			} else if (split[2].equals("I")) {
				events.add(IdleEvent.newIdleEvent(this, split));
			} else {
				createEvent(split);
			}
		} else if (split[0].equals("PartyMinMax")) {
			int min = Integer.parseInt(split[1]);
			int max = Integer.parseInt(split[2]);
			if (party.getQuesters().size() > max) {
				(new MessageEvent(10, party, "This quest cannot have more than " + max + " people in a party")).activate(null);
				MineQuest.log("Party was the wrong size");
				throw new Exception();
			}
			if (party.getQuesters().size() < min) {
				(new MessageEvent(10, party, "This quest cannot have less than " + min + " people in a party")).activate(null);
				MineQuest.log("Party was the wrong size");
				throw new Exception();
			}
		} else if (split[0].equals("Task")) {
			createTask(split, false);
		} else if (split[0].equals("RepeatingTask")) {
			createTask(split, true);
		} else if (split[0].contains("World") || split[0].contains("Instance")) {
			createWorld(split);
		} else if (split[0].equals("Spawn")) {
			double x = Double.parseDouble(split[1]);
			double y = Double.parseDouble(split[2]);
			double z = Double.parseDouble(split[3]);
			this.spawn = new Location(world, x, y, z);
		} else if (split[0].equals("QuestArea")) {
			start_x = Double.parseDouble(split[1]);
			start_y = Double.parseDouble(split[2]);
			start_z = Double.parseDouble(split[3]);
			end_x = Double.parseDouble(split[4]);
			end_y = Double.parseDouble(split[5]);
			end_z = Double.parseDouble(split[6]);
		} else if (split[0].equals("NPC")) {
			String name = split[1];
			Location location = new Location(world,
					Double.parseDouble(split[2]),
					Double.parseDouble(split[3]),
					Double.parseDouble(split[4]),
					Float.parseFloat(split[5]),
					Float.parseFloat(split[6]));
			npcs.add(new NPCQuester(name, NPCMode.QUEST_INVULNERABLE, world, location));
			MineQuest.questerHandler.addQuester(npcs.get(npcs.size() - 1));
			MineQuest.questerHandler.getQuester(name).setQuest(this, world);
		} else if (split[0].equals("NPCV")) {
			String name = split[1];
			Location location = new Location(world,
					Double.parseDouble(split[2]),
					Double.parseDouble(split[3]),
					Double.parseDouble(split[4]),
					Float.parseFloat(split[5]),
					Float.parseFloat(split[6]));
			npcs.add(new NPCQuester(name, NPCMode.QUEST_VULNERABLE, world, location));
			MineQuest.questerHandler.addQuester(npcs.get(npcs.size() - 1));
			MineQuest.questerHandler.getQuester(name).setQuest(this, world);
		} else if (split[0].equals("Target")) {
			targets.add(Target.newTarget(split, this));
		} else if (split[0].equals("Edit")) {
			addCanEdit(CanEdit.makeCanEdit(split, world));
		} else if (split[0].equals("Name")) {
			name = split[1];
		} else if (split[0].equals("Repeatable")) {
			repeatable = Boolean.parseBoolean(split[1]);
		} else if (split[0].equals("Reset")) {
			reset = Boolean.parseBoolean(split[1]);
		} else if (split[0].equals("NoMobs")) {
			no_mobs = Boolean.parseBoolean(split[1]);
		} else if (split[0].equals("EditMessage")) {
			edit_message = split[1];
		} else if (split[0].equals("AreaPreserve")) {
			start_x = Double.parseDouble(split[1]);
			start_y = Double.parseDouble(split[2]);
			start_z = Double.parseDouble(split[3]);
			end_x = Double.parseDouble(split[4]);
			end_y = Double.parseDouble(split[5]);
			end_z = Double.parseDouble(split[6]);
			Location start = new Location(world, start_x, start_y, start_z);
			Location end = new Location(world, end_x, end_y, end_z);
			areaPreserver = new AreaPreserver(world, start, end);
		}
	}

	@SuppressWarnings("deprecation")
	private void createWorld(String[] split) throws Exception {
		if (split[0].equals("World")) {
			World world = null;
			if (MineQuest.getSServer().getWorld(split[1]) == null) {
				if ((split.length == 2) || (split[2].equals("NORMAL"))) {
					world = MineQuest.getSServer().createWorld(split[1], Environment.NORMAL);
				} else {
					world = MineQuest.getSServer().createWorld(split[1], Environment.NETHER);
				}
			}
			
			teleport(party.getQuesterArray(), world);
		} else if (split[0].equals("LoadWorld")) {
			if (MineQuest.getSServer().getWorld(split[1]) == null) {
				deleteDir(new File(split[1]));
				copyDirectory(new File(split[2]), new File(split[1]));
				world = null;
				if (MineQuest.getSServer().getWorld(split[1]) == null) {
					if ((split.length == 3) || (split[3].equals("NORMAL"))) {
						world = MineQuest.getSServer().createWorld(split[1], Environment.NORMAL);
					} else {
						world = MineQuest.getSServer().createWorld(split[1], Environment.NETHER);
					}
				}
			} else {
				boolean flag = false;
				if ((split.length == 3) || (split[3].equals("NORMAL"))) {
					flag = true;
				}
				copyWorld(split[3], split[2], flag);
			}
		} else if (split[0].equals("Instance")) {
			int max = Integer.parseInt(split[1]);
			int i;
			for (i = 0; i < max; i++) {
				boolean flag = true;
				for (Quest quest : MineQuest.getQuests()) {
					if (quest.getWorld().getName().equals(split[2] + i)) {
						flag = false;
					}
				}
				if (flag) break;
			}
			if (i == max) {
				MineQuest.log("Instances Full - Unable to Start Quest");
				MineQuest.getEventQueue().addEvent(new MessageEvent(10, party, "Instances Full - Unable to Start Quest"));
			}
//			split[2] = split[2] + i;
//			if (MineQuest.getSServer().getWorld(split[2]) == null) {
//				deleteDir(new File(split[2]));
//				copyDirectory(new File(split[3]), new File(split[2]));
				world = null;
//				if (MineQuest.getSServer().getWorld(split[2]) == null) {
					if ((split.length == 4) || (split[4].equals("NORMAL"))) {
						world = NewChunkRegionLoader.createWorld(split[3], Environment.NORMAL, i);
//						world = MineQuest.getSServer().createWorld(split[2], Environment.NORMAL);
					} else {
						world = NewChunkRegionLoader.createWorld(split[3], Environment.NETHER, i);
//						world = MineQuest.getSServer().createWorld(split[2], Environment.NETHER);
					}
//				}
//			} else {
//				boolean flag = false;
//				if ((split.length == 4) || (split[4].equals("NORMAL"))) {
//					flag = true;
//				}
//				copyWorld(split[3], split[2], flag);
//			}
		}
	}

	@SuppressWarnings("deprecation")
	private void copyWorld(String orig, String cp, boolean normal) throws Exception {
		World world = MineQuest.getSServer().getWorld(orig);
		if (world == null) {
			if (normal) {
				world = MineQuest.getSServer().createWorld(orig, Environment.NORMAL);
			} else {
				world = MineQuest.getSServer().createWorld(orig, Environment.NETHER);
			}
		}
		World copy = MineQuest.getSServer().getWorld(cp);

		if (start_x == 0) {
			MineQuest.log("Instanced world without quest area defined - missing QuestArea?");
		}
		for (LivingEntity entity : copy.getLivingEntities()) {
			entity.remove();
		}

		int x,y,z;
		for (x = (int)start_x; x <= (int)end_x; x++) {
			for (z = (int)start_z; z <= (int)end_z; z++) {
				for (y = (int)end_y; y >= (int)start_y; y--) {
					Block original = world.getBlockAt(x, y, z);
					Block new_block = copy.getBlockAt(x, y, z);
					
					new_block.setType(original.getType());
					new_block.setData(original.getData());
					
					if (original.getType() == Material.CHEST) {
						copyChest(original, new_block);
					}
				}
			}
		}

		this.world = copy;
	}
	
	private void copyChest(Block original, Block new_block) {
		moveContents(getChest(new_block.getLocation()).getInventory(), 
				getChest(original.getLocation()).getInventory());
	}

	private Chest getChest(Location location) {
		Block block = location.getWorld().getBlockAt(location);
		if (block.getType() == Material.CHEST) {
			Chest chest = new CraftChest(block);

			return chest;
		} else {
			return null;
		}
	}

	private void moveContents(Inventory to, Inventory from) {
		int i;
		ItemStack[] new_contents = new ItemStack[from.getContents().length];
		
		for (i = 0; i < new_contents.length; i++) {
			ItemStack original = from.getContents()[i];
			if (original != null) {
				ItemStack item = new ItemStack(original.getType(), 
						original.getAmount());
				item.setDurability(original.getDurability());
				if (item.getData() != null) {
					item.setData(new MaterialData(original.getType(), 
							original.getData().getData()));
				}
				new_contents[i] = item;
			}
		}
		to.setContents(new_contents);
	}

	public World getWorld() {
		return world;
	}

	public void removeQuester(Quester quester) {
		party.remQuester(quester);
		
		quester.clearQuest(reset);
		
		if (party.getQuesters().size() == 0) {
			issueNextEvents(-1);
		}
	}
	
	public Location getSpawn() {
		if (!reset) return null;
		
		return spawn;
	}
	
	public static boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	}
	
	// If targetLocation does not exist, it will be created.
    public void copyDirectory(File sourceLocation , File targetLocation)
    throws IOException {
        
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            
            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            
            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
	
	private void teleport(Quester[] questers, World world) {
		if (world == null) {
			MineQuest.log("Null World!!");
			return;
		}
		for (Quester quester : questers) {
			Location location = quester.getPlayer().getLocation();
			location.setWorld(world);
			quester.getPlayer().teleport(location);
		}
	}

	public void createTask(String line[], boolean repeating) throws Exception {
		int id = Integer.parseInt(line[1]);
		Event[] events;
		if (line.length == 3) {
			events = new Event[line[2].split(",").length];
			int i = 0;
			
			for (String event : line[2].split(",")) {
				if (getEvent(Integer.parseInt(event)) == null) {
					MineQuest.log("Problem Getting Event: " + event);
					throw new Exception();
				}
				events[i++] = getEvent(Integer.parseInt(event));
			}
		} else {
			events = new Event[0];
		}
		
		if (repeating) {
			tasks.add(new RepeatingQuestTask(events, id));
		} else {
			tasks.add(new QuestTask(events, id));
		}
	}
	
	public void createEvent(String line[]) throws Exception {
		int id = Integer.parseInt(line[1]);
		String type = line[2];
		Event new_event;
		LivingEntity entities[] = new LivingEntity[questers.size()];
		int i = 0;
		for (Quester quester : questers) {
			entities[i++] = quester.getPlayer();
		}
		
		if (type.equals("AreaEvent")) {
			int delay = Integer.parseInt(line[3]);
			int index = Integer.parseInt(line[4]);
			
			Location loc = new Location(world, Double.parseDouble(line[5]), Double.parseDouble(line[6]), Double.parseDouble(line[7]));
			double radius = Double.parseDouble(line[8]);
			new_event = new AreaEvent(this, delay, index, party, loc, radius);
		} else if (type.equals("SingleAreaEvent")) {
			int delay = Integer.parseInt(line[3]);
			int index = Integer.parseInt(line[4]);
			
			Location loc = new Location(world, Double.parseDouble(line[5]), Double.parseDouble(line[6]), Double.parseDouble(line[7]));
			double radius = Double.parseDouble(line[8]);
			new_event = new SingleAreaEvent(this, delay, index, party, loc, radius);
		} else if (type.equals("parseDouble")) {
			int delay = Integer.parseInt(line[3]);
			double x = Double.parseDouble(line[4]);
			double y = Double.parseDouble(line[5]);
			double z = Double.parseDouble(line[6]);
			
			new_event = new CreateBoatEvent(delay, world, x, y, z);
		} else if (type.equals("MessageEvent")) {
			int delay = Integer.parseInt(line[3]);

			new_event = new MessageEvent(delay, party, line[4]);
		} else if (type.equals("BlockEvent")) {
			int delay = Integer.parseInt(line[3]);

			Block block = world.getBlockAt(Integer.parseInt(line[4]), Integer.parseInt(line[5]), Integer.parseInt(line[6]));
			
			int mat = Integer.parseInt(line[7]);
			
			new_event = new BlockEvent(delay, block, Material.getMaterial(mat));
		} else if (type.equals("AdvancedBlockEvent")) {
			int delay = Integer.parseInt(line[3]);

			Block block = world.getBlockAt(Integer.parseInt(line[4]), Integer.parseInt(line[5]), Integer.parseInt(line[6]));
			
			int mat = Integer.parseInt(line[7]);
			
			new_event = new AdvancedBlockEvent(delay, block, Material.getMaterial(mat), Byte.parseByte(line[8]));
		} else if (type.equals("QuestEvent")) {
			int delay = Integer.parseInt(line[3]);
			int index = Integer.parseInt(line[4]);
			
			new_event = new QuestEvent(this, delay, index);
		} else if (type.equals("EntitySpawnerEvent")) {
			int delay = Integer.parseInt(line[3]);
			String creature = line[7];
			Location location = new Location(world, Double.parseDouble(line[4]), Double.parseDouble(line[5]), Double.parseDouble(line[6]));
			boolean superm;
			if (line[8].equals("f")) {
				superm = false;
			} else {
				superm = true;
			}
			new_event = new EntitySpawnerEvent(delay, location, CreatureType.fromName(creature), superm);
		} else if (type.equals("EntitySpawnerNoMove")) {
			int delay = Integer.parseInt(line[3]);
			String creature = line[7];
			Location location = new Location(world, Double.parseDouble(line[4]), Double.parseDouble(line[5]), Double.parseDouble(line[6]));
			boolean superm;
			if (line[8].equals("f")) {
				superm = false;
			} else {
				superm = true;
			}
			new_event = new EntitySpawnerNoMove(delay, location, CreatureType.fromName(creature), superm);			
		} else if (type.equals("EntitySpawnerCompleteNMEvent")) {
			long delay = Integer.parseInt(line[3]);
			int index = Integer.parseInt(line[4]);
			i = 0;
			EntitySpawnerEvent[] eventss = new EntitySpawnerEvent[line[5].split(",").length];
			for (String s : line[5].split(",")) {
				if (getEvent(Integer.parseInt(s)) == null) {
					MineQuest.log("Cannot Find Event: " + Integer.parseInt(s));
					throw new Exception();
				}
				eventss[i++] = (EntitySpawnerEvent)getEvent(Integer.parseInt(s));
			}

			new_event = new EntitySpawnerCompleteNMEvent(this, delay, index, eventss);
		} else if (type.equals("EntitySpawnerCompleteEvent")) {
			int delay = Integer.parseInt(line[3]);
			i = 0;
			EntitySpawnerEvent[] eventss = new EntitySpawnerEvent[line[4].split(",").length];
			for (String s : line[4].split(",")) {
				if (getEvent(Integer.parseInt(s)) == null) {
					MineQuest.log("Cannot Find Event: " + Integer.parseInt(s));
					throw new Exception();
				}
				eventss[i++] = (EntitySpawnerEvent)getEvent(Integer.parseInt(s));
			}

			new_event = new EntitySpawnerCompleteEvent(delay, eventss);
		} else if (type.equals("ExperienceAdd") || type.equals("ExperienceEvent")) {
			long delay = Integer.parseInt(line[3]);
			int exp = Integer.parseInt(line[5]);
			int class_exp = Integer.parseInt(line[6]);
			int cubes = 0;
			if (line.length >= 8) {
				cubes = Integer.parseInt(line[7]);
			}
			if (!line[4].equals("all")) {
				MineQuest.log("Warning: Options other than all are not supported for ExperienceAdd");
			}
			
			new_event = new ExperienceEvent(delay, party, exp, class_exp, cubes);
		} else if (type.equals("LockWorldTime")) {
			long delay = Integer.parseInt(line[3]);
			long time = Integer.parseInt(line[4]);
			long time_2 = Integer.parseInt(line[5]);
			
			new_event = new LockWorldTime(delay, world, time, time_2);
		} else if (type.equals("BlockCDEvent")) {
			long delay = Integer.parseInt(line[3]);
			long second_delay = Integer.parseInt(line[4]);
			Location location = new Location(world,
					Integer.parseInt(line[5]), Integer.parseInt(line[6]), Integer.parseInt(line[7]));
			Block block = world.getBlockAt(location);
			int idd = Integer.parseInt(line[8]);
			
			new_event = new BlockCDEvent(delay, second_delay, block, Material.getMaterial(idd));
		} else if (type.equals("BlockDCEvent")) {
			long delay = Integer.parseInt(line[3]);
			long second_delay = Integer.parseInt(line[4]);
			Location location = new Location(world,
					Integer.parseInt(line[5]), Integer.parseInt(line[6]), Integer.parseInt(line[7]));
			Block block = world.getBlockAt(location);
			int idd = Integer.parseInt(line[8]);
			
			new_event = new BlockDCEvent(delay, second_delay, block, Material.getMaterial(idd));
		} else if (type.equals("ArrowEvent")) {
			long delay = Integer.parseInt(line[3]);
			Location start = new Location(world,
					Double.parseDouble(line[4]),
					Double.parseDouble(line[5]),
					Double.parseDouble(line[6])
					);
			Vector vector = new Vector(
					Double.parseDouble(line[7]),
					Double.parseDouble(line[8]),
					Double.parseDouble(line[9])
					);
			
			new_event = new ArrowEvent(delay, start, vector);
		} else if (type.equals("CanEdit")) {
			addCanEdit(CanEdit.makeCanEdit(line, world));
			
			return;
		} else if (type.equals("PartyHealthEvent")) {
			long delay = Integer.parseInt(line[3]);
			double percent = Double.parseDouble(line[4]);
			
			new_event = new PartyHealthEvent(delay, party, percent);
		} else if (type.equals("CancelEvent")) {
			long delay = Integer.parseInt(line[3]);
			String[] split_nums = new String[] {line[4]};
			if (line[4].contains(",")) {
				split_nums = line[4].split(",");
			}
			int[] nums = new int[split_nums.length];
			
			for (i = 0; i < split_nums.length; i++) {
				nums[i] = Integer.parseInt(split_nums[i]);
			}
			
			new_event = new CancelEvent(delay, this, nums);
		} else if (type.equals("CompleteQuestEvent")) {
			long delay = Integer.parseInt(line[3]);
			
			new_event = new CompleteQuestEvent(delay, this, party);
		} else if (type.equals("HealthEntitySpawn")) {
			long delay;
			int task;
			Location location;
			int health;
			CreatureType creature;
			try {
				creature = CreatureType.fromName(line[8]);
				delay = Integer.parseInt(line[3]);
				task = Integer.parseInt(line[4]);
				location = new Location(world,
						Double.parseDouble(line[5]),
						Double.parseDouble(line[6]),
						Double.parseDouble(line[7])
						);
				health = Integer.parseInt(line[9]);
			} catch (Exception e2) {
				MineQuest.log("Problem getting HealthEntitySpawner Parameters");
				throw new Exception();
			}
			boolean stay = true;
			if (line[10].equals("f")) {
				stay = false;
			}
			 
			new_event = new HealthEntitySpawn(this, delay, task, location, creature, health, stay);
		} else if (type.equals("ExplosionEvent")) {
			int delay = Integer.parseInt(line[3]);
			double x = Double.parseDouble(line[4]);
			double y = Double.parseDouble(line[5]);
			double z = Double.parseDouble(line[6]);
			double radius = Double.parseDouble(line[7]);
			int damage = Integer.parseInt(line[8]);
			
			new_event = new ExplosionEvent(delay, world, x, y, z, (float)radius, damage);
		} else if (type.equals("WeatherEvent")) {
			int delay = Integer.parseInt(line[3]);
			boolean hasStorm = Boolean.parseBoolean(line[4]);
			int duration = Integer.parseInt(line[5]);
			
			new_event = new WeatherEvent(delay, world, hasStorm, duration);
		} else if (type.equals("LightningEvent")) {
			int delay = Integer.parseInt(line[3]);
			double x = Double.parseDouble(line[4]);
			double y = Double.parseDouble(line[5]);
			double z = Double.parseDouble(line[6]);
			Location location = new Location(world, x, y, z);
			
			new_event = new LightningEvent(delay, location);
		} else if (type.equals("KillEvent")) {
			int delay = Integer.parseInt(line[3]);
			int task = Integer.parseInt(line[4]);
			String[] kill_names = line[5].split(",");
			int[] kills = new int[line[6].split(",").length];
			if (kill_names.length != kills.length) {
				MineQuest.log("Error: Unmatched Length of Names and Quantities");
				throw new Exception();
			}
			for (String kill_name : kill_names) {
				if (CreatureType.fromName(kill_name) == null) {
					MineQuest.log("Error: Invalid Creature Name " + kill_name);
					throw new Exception();
				}
			}
			i = 0;
			for (String count : line[6].split(",")) {
				kills[i++] = Integer.parseInt(count);
			}
			if (kill_names.length == 0) {
				MineQuest.log("Error: Cannot Have 0 Targets");
				throw new Exception();
			}
			
			new_event = new PartyKill(this, delay, task, party, kill_names, kills);
		} else if (type.equals("DestroyEvent")) {
			int delay = Integer.parseInt(line[3]);
			int task = Integer.parseInt(line[4]);
			String[] destroy_names = line[5].split(",");
			int[] destroys = new int[line[6].split(",").length];
			if (destroy_names.length != destroys.length) {
				MineQuest.log("Error: Unmatched Length of Names and Quantities");
				throw new Exception();
			}

			for (String destroy_name : destroy_names) {
				if (Material.getMaterial(destroy_name) == null) {
					MineQuest.log("Error: Invalid Creature Name " + destroy_name);
					throw new Exception();
				}
			}

			i = 0;
			for (String count : line[6].split(",")) {
				destroys[i++] = Integer.parseInt(count);
			}

			if (destroy_names.length == 0) {
				MineQuest.log("Error: Cannot Have 0 Targets");
				throw new Exception();
			}

			new_event = new PartyDestroy(this, delay, task, party, destroy_names, destroys);
		} else if (type.equals("CanEditPattern")) {
			int delay = Integer.parseInt(line[3]);
			int index = Integer.parseInt(line[4]);
			String edit_s[] = line[5].split(",");
			String flag_s[] = line[6].split(",");
			if (edit_s.length != flag_s.length) {
				MineQuest.log("Lengths of parameters must be equal");
				throw new Exception();
			}
			CanEdit[] editors = new CanEdit[edit_s.length];
			boolean[] flags = new boolean[edit_s.length];
			for (i = 0; i < edit_s.length; i++) {
				editors[i] = getCanEdit(Integer.parseInt(edit_s[i]));
				flags[i] = Boolean.parseBoolean(flag_s[i]);
			}
			
			new_event = new CanEditPattern(this, delay, index, editors, flags);
		} else if (type.equals("QuestAvailableEvent")) {
			int delay = Integer.parseInt(line[3]);
			String quest = line[4];
			
			new_event = new QuestAvailableEvent(delay, quest, party);
		} else {
			MineQuest.log("Unknown Event Type: " + type);
			throw new Exception();
		}
		
		
//		MineQuest.log("Added " + events.get(events.size() - 1).getName());
		new_event.setId(id);
		events.add(new_event);
	}
	
	private void addCanEdit(CanEdit new_edit) {
		int i;
		CanEdit new_edits[] = new CanEdit[edits.length + 1];
		for (i = 0; i < edits.length; i++) {
			new_edits[i] = edits[i];
		}
		new_edits[i] = new_edit;
		edits = new_edits;
	}
	
	private CanEdit getCanEdit(int id) {
		int i;
		
		for (i = 0; i < edits.length; i++) {
			if (edits[i].getId() == id) {
				return edits[i];
			}
		}
		
		return null;
	}

	public Event getEvent(int id) {
		for (Event event : events) {
			if (event.getId() == id) {
				return event;
			}
		}
		
		return null;
	}

	public void issueNextEvents(int index) {
		if (index == -1) {
			for (Quester quester : party.getQuesters()) {
				if (quester != null) {
					quester.clearQuest(reset);
				}
			}
			
			for (QuestTask task : tasks) {
				if (task != null) {
					task.clearEvents();
				}
			}
			
			for (Event event : events) {
				if (event != null) {
					event.cancelEvent();
				}
			}
			
			for (NPCQuester quester : npcs) {
				if ((quester != null) && (quester.getHealth() > 0)) {
					MineQuest.questerHandler.remQuester(quester);
					quester.damage(200000);
				}
			}
			
			MineQuest.remQuest(this);
			
			if (areaPreserver != null) {
				areaPreserver.resetArea();
			}
			
			if (no_mobs) {
				MineQuest.mobHandler.yesMobs(world);
			}
			
			return;
		} else if (index <= -2) {
			return;
		}
		
		for (QuestTask task : tasks) {
			if (task.getId() == index) {
				task.issueEvents();
			}
		}
	}

	public boolean canEdit(Quester quester, Block block) {
		int i;
		for (i = 0; i < edits.length; i++) {
			if (edits[i].canEdit(quester, block.getLocation())) {
				if (edits[i].getQuestIndex() >= -1) {
					issueNextEvents(edits[i].getQuestIndex());
				}
				return true;
			}
		}

		quester.notify(edit_message);

		return false;
	}

	public Target getTarget(int id) {
		for (Target target : targets) {
			if (target.getId() == id) {
				return target;
			}
		}
		return null;
	}

	public Party getParty() {
		return party;
	}

	public TargetEvent getEdit(int id) {
		for (CanEdit edit : edits) {
			if (edit.getId() == id) {
				return edit;
			}
		}
		
		return null;
	}

	public boolean isMainQuest() {
		return filename.equals("MineQuest/main.script");
	}
}
