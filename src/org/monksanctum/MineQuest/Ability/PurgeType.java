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
package org.monksanctum.MineQuest.Ability;

import java.util.HashMap;
import java.util.Map;

public enum PurgeType {
	ZOMBIE(0),
	SPIDER(1),
	SKELETON(2),
	CREEPER(3),
	GHAST(6),
	PIGZOMBIE(7),
	ANIMAL(4),
	ALL(5);
	
	private int id;
	private static int max;
    private static final Map<Integer, PurgeType> lookupId = new HashMap<Integer, PurgeType>();
    private static final Map<String, PurgeType> lookupName = new HashMap<String, PurgeType>();
	
	PurgeType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

    public static PurgeType getMaterial(final int id) {
        return lookupId.get(id);
    }

    public static PurgeType getMaterial(final String name) {
        return lookupName.get(name);
    }
    
    public static int getMaxId() {
    	return max;
    }

    static {
        for (PurgeType purgeType : values()) {
            lookupId.put(purgeType.getId(), purgeType);
            lookupName.put(purgeType.name(), purgeType);
            if (purgeType.getId() > max) {
            	max = purgeType.getId();
            }
        }
    }
}
