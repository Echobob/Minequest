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

import java.util.ArrayList;
import java.util.List;

import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

public class AreaTargetQuester extends Target {
	private double radius;
	private Target target;

	public AreaTargetQuester(Target target, double radius) {
		this.target = target;
		this.radius = radius;
	}

	@Override
	public List<Quester> getTargets() {
		List<Quester> questers = new ArrayList<Quester>();
		if (target.getTargets().size() != 0) {
			Quester quester = target.getTargets().get(0);
			
			for (Quester q : MineQuest.questerHandler.getQuesters()) {
				if (MineQuest.distance(q.getPlayer().getLocation(), quester.getPlayer().getLocation()) < radius) {
					questers.add(q);
				}
			}
		}
		
		return questers;
	}
}
