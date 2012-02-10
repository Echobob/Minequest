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
package org.monksanctum.MineQuest.World;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

public class Property extends Area{
	protected String owner;
	private List<Quester> editors;
	private long price;
	
	public Property(String owner, Location start, Location end, boolean height, long price) {
		super(start, end, height);
		
		this.price = price;
		
		this.owner = owner;
		
		editors = new ArrayList<Quester>();
	}
	
	public Property() {
	}
	
	public boolean inProperty(Location loc) {
		return isWithin(loc);
	}
	
	public boolean inProperty(Player player) {
		return  isWithin(player.getLocation());
	}
	
	public boolean inProperty(Quester quester) {
		return  isWithin(quester.getPlayer());
	}
	
	public boolean inProperty(Block block) {
		return inProperty(block.getLocation());
	}
	
	
	public Quester getOwner() {
		return MineQuest.questerHandler.getQuester(owner);
	}
	
	public void setOwner(Quester quester) {
		if (quester != null) {	
			owner = quester.getName();
		} else {
			MineQuest.questerHandler.getQuester(owner).sendMessage("Invalid Quester");
		}
	}
	
	public boolean canEdit(Quester quester) {
		int i;
		
		if (quester.equals(owner)) {
			return true;
		}
		
		if (MineQuest.isPermissionsEnabled() && (quester.getPlayer() != null)) {
			if (MineQuest.permission.playerHas(quester.getPlayer(), "MineQuest.Property." + owner)) {
				return true;
			}
		}
		
		for (i = 0; i < editors.size(); i++) {
			if (editors.get(i).equals(quester)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void addEdit(Quester quester) {
		editors.add(quester);
	}
	
	public void remEdit(Quester quester) {
		editors.remove(quester);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Property) {
			Property other = (Property)obj;
			
			return super.equals(other);
		}
		return super.equals(obj);
	}

	public Location getLocation() {
		return new Location(MineQuest.getSServer().getWorlds().get(0), 
				(getX() + getMaxX()) / 2, getY(), 
				(getZ() + getMaxZ()) / 2);
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public Location getEdge(Location location) {
		Location ret = new Location(location.getWorld(), location.getX(),
				location.getY(), location.getZ());
		
		if (Math.abs(ret.getX() - getCenterX()) > Math.abs(ret.getZ() - getCenterZ())) {
			if (ret.getX() > getCenterX()) {
				ret.setX(getMaxX());
			} else {
				ret.setX(getX());
			}
		} else {
			if (ret.getZ() > getCenterZ()) {
				ret.setZ(getMaxZ());
			} else {
				ret.setZ(getZ());
			}
		}
		
		return ret;
	}

	public long getPrice() {
		return price;
	}
}
