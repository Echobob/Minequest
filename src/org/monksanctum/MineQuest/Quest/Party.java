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
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class Party {
	protected List<Quester> questers;
	
	public Party() {
		questers = new ArrayList<Quester>();
	}
	
	public void addQuester(Quester quester) {
		if (!questers.contains(quester)) {
			questers.add(quester);
		} else {
			MineQuest.log("[WARNING] Tried to add quester " + quester.getName() + " that was already part of party");
		}
		
		quester.setParty(this);
	}
	
	public void remQuester(Quester quester) {
		if (questers.contains(quester)) {
			questers.remove(quester);
		} else {
			MineQuest.log("[WARNING] Tried to remove quester " + quester.getName() + " that was not part of party");
		}
		
		quester.setParty(null);
	}
	
	public List<Quester> getQuesters() {
		return questers;
	}
	
	public Quester[] getQuesterArray() {
		Quester[] ret = new Quester[questers.size()];
		int i = 0;
		
		for (Quester quester : questers) {
			ret[i++] = quester;
		}
		
		return ret;
	}
	
	public List<Quester> getRealQuesters() {
		List<Quester> ret = new ArrayList<Quester>();
		
		for (Quester quester : questers) {
			if (!(quester instanceof NPCQuester)) {
				ret.add(quester);
			}
		}
		
		return ret;
	}
}
