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
package org.monksanctum.MineQuest.Event.Absolute;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.monksanctum.MineQuest.Event.EventParser;
import org.monksanctum.MineQuest.Event.NormalEvent;

public class MassBlockEvent extends NormalEvent {
	protected Material newType;
	protected Block block;
	protected int type;
	protected byte data;
	protected boolean passed;
	
	public MassBlockEvent(long delay, World world, int x, int y,
			int z, int max_x, int max_y, int max_z, Material newType) {
		this(delay, world.getBlockAt(x, y, z), newType);
	}

	public MassBlockEvent(long delay, Block block, Material newType) {
		super(delay);
		this.block = block;
		this.newType = newType;
		passed = false;
	}
	
	@Override
	public void activate(EventParser eventParser) {
		type = block.getTypeId();
		data = block.getData();
		block.setType(newType);
		passed = true;
	}

	@Override
	public String getName() {
		return "Generic Block Type Event";
	}

	@Override
	public void cancelEvent() {
		super.cancelEvent();
		
		if (passed) {
			block.setTypeId(type);
			block.setData(data);
		}
	}
}
