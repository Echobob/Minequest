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
package org.monksanctum.MineQuest.Quest.CanEdit;

import org.bukkit.Location;
import org.bukkit.World;
import org.monksanctum.MineQuest.Event.TargetEvent;
import org.monksanctum.MineQuest.Quester.Quester;

public abstract class CanEdit implements TargetEvent {
	public static CanEdit makeCanEdit(String[] line, World world) {
		CanEdit can_edit = null;
		if (line[2].equals("CanEdit")) {
			Location new_loc = new Location(world,
					Integer.parseInt(line[3]),
					Integer.parseInt(line[4]),
					Integer.parseInt(line[5])
					);
			int next_task = Integer.parseInt(line[6]);
			
			can_edit = new CanEditBlock(new_loc, next_task);
		} else if (line[2].equals("CanEditArea")) {
			int x = Integer.parseInt(line[3]);
			int y = Integer.parseInt(line[4]);
			int z = Integer.parseInt(line[5]);
			int max_x = Integer.parseInt(line[6]);
			int max_y = Integer.parseInt(line[7]);
			int max_z = Integer.parseInt(line[8]);
			int next_task = Integer.parseInt(line[9]);

			can_edit = new CanEditArea(next_task, x, y, z, max_x, max_y, max_z);
		} else if (line[2].equals("CanEditOutsideArea")) {
			int x = Integer.parseInt(line[3]);
			int y = Integer.parseInt(line[4]);
			int z = Integer.parseInt(line[5]);
			int max_x = Integer.parseInt(line[6]);
			int max_y = Integer.parseInt(line[7]);
			int max_z = Integer.parseInt(line[8]);
			int next_task = Integer.parseInt(line[9]);

			can_edit = new CanEditOutsideArea(next_task, x, y, z, max_x, max_y, max_z);
		} else if (line[2].equals("CanEditTypesInHand")) {
			int index = Integer.parseInt(line[3]);
			String[] split_nums = new String[] {line[4]};
			if (line[4].contains(",")) {
				split_nums = line[4].split(",");
			}
			int[] nums = new int[split_nums.length];
			
			for (int i = 0; i < split_nums.length; i++) {
				nums[i] = Integer.parseInt(split_nums[i]);
			}
			
			can_edit = new CanEditTypesInHand(index, nums);
		} else if (line[2].equals("CanEditTypes")) {
			int index = Integer.parseInt(line[3]);
			String[] split_nums = new String[] {line[4]};
			if (line[4].contains(",")) {
				split_nums = line[4].split(",");
			}
			int[] nums = new int[split_nums.length];
			
			for (int i = 0; i < split_nums.length; i++) {
				nums[i] = Integer.parseInt(split_nums[i]);
			}
			
			can_edit = new CanEditTypes(index, nums);
		} else {
			return null;
		}
		
		can_edit.setId(Integer.parseInt(line[1]));
		
		return can_edit;
	}
	
	public abstract int getId();
	
	public abstract void setId(int id);
	
	public abstract boolean canEdit(Quester quester, Location loc);
	
	public abstract int getQuestIndex();
	
	public abstract boolean isActive();
}
