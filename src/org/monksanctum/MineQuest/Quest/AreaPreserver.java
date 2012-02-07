package org.monksanctum.MineQuest.Quest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.UpdateSignEvent;

public class AreaPreserver {
	private Material type[][][];
	private byte data[][][];
	private List<ItemStack[]> contents;
	private List<String> strings;
	private Location start;
	private Location end;
	private World world;
	
	public AreaPreserver(World world, Location start, Location end) {
		type = new Material[end.getBlockX() - start.getBlockX()]
		                    [end.getBlockY() - start.getBlockY()]
		                     [end.getBlockZ() - start.getBlockZ()];
		data = new byte[end.getBlockX() - start.getBlockX()]
		                    [end.getBlockY() - start.getBlockY()]
		                     [end.getBlockZ() - start.getBlockZ()];
		this.start = start;
		this.end = end;
		this.world = world;
		contents = new ArrayList<ItemStack[]>();
		strings = new ArrayList<String>();
		
		int x,y,z;
		for (x = start.getBlockX(); x < end.getBlockX(); x++) {
			for (y = start.getBlockY(); y < end.getBlockY(); y++) {
				for (z = start.getBlockZ(); z < end.getBlockZ(); z++) {
					type[x - start.getBlockX()]
					     [y - start.getBlockY()]
					      [z - start.getBlockZ()] = 
					    	  world.getBlockAt(x, y, z).getType();
					data[x - start.getBlockX()]
					     [y - start.getBlockY()]
					      [z - start.getBlockZ()] = 
					    	  world.getBlockAt(x, y, z).getData();
					if (world.getBlockAt(x, y, z).getType() == Material.CHEST) {
						contents.add(moveContents(getChest(
								world.getBlockAt(x, y, z).getLocation())
								.getInventory()));
					}
					if ((world.getBlockAt(x, y, z).getType() == Material.SIGN) || 
						(world.getBlockAt(x, y, z).getType() == Material.WALL_SIGN)) {
						Sign sign = (Sign)world.getBlockAt(x, y, z).getState();
						for (int i = 0; i < 4; i++) {
							strings.add(sign.getLine(i));
						}
					}
				}
			}
		}
	}
	
	public void resetArea() {
		int x,y,z;
		int i = 0;
		int line = 0;
		for (x = start.getBlockX(); x < end.getBlockX(); x++) {
			for (y = start.getBlockY(); y < end.getBlockY(); y++) {
				for (z = start.getBlockZ(); z < end.getBlockZ(); z++) {
					world.getBlockAt(x, y, z).setType(type[x - start.getBlockX()]
					     [y - start.getBlockY()]
					      [z - start.getBlockZ()]);
					 world.getBlockAt(x, y, z).setData(data[x - start.getBlockX()]
					     [y - start.getBlockY()]
					      [z - start.getBlockZ()]);
					if (world.getBlockAt(x, y, z).getType() == Material.CHEST) {
						getChest(world.getBlockAt(x, y, z).getLocation())
								.getInventory().setContents(contents.get(i++));
					}
					if ((world.getBlockAt(x, y, z).getType() == Material.SIGN) || 
						(world.getBlockAt(x, y, z).getType() == Material.WALL_SIGN)) {
						Sign sign = (Sign)world.getBlockAt(x, y, z).getState();
						for (int in = 0; in < 4; in++) {
							sign.setLine(in, strings.get(line++));
						}
						MineQuest.getEventQueue().addEvent(new UpdateSignEvent(100, sign, sign.getLines()));
					}
					if (world.getBlockAt(x, y, z).getState() != null) {
						world.getBlockAt(x, y, z).getState().update(true);
					}
				}
			}
		}
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

	private ItemStack[] moveContents(Inventory from) {
		int i;
		ItemStack[] new_contents = new ItemStack[from.getContents().length];
		
		for (i = 0; i < new_contents.length; i++) {
			ItemStack original = from.getContents()[i];
			ItemStack item = new ItemStack(original.getType(), 
					original.getAmount());
			item.setDurability(original.getDurability());
			if (item.getData() != null) {
				item.setData(new MaterialData(original.getType(), 
						original.getData().getData()));
			}
			new_contents[i] = item;
		}
		
		return new_contents;
	}
}
