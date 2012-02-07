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

import java.util.HashMap;
import java.util.Map;

public enum NPCMode {
	GENERIC, 
	PARTY, 
	FOR_SALE, 
	STORE,
	PARTY_STAND,
	QUEST_INVULNERABLE, 
	QUEST_VULNERABLE, 
	VULNERABLE;
	
    private static final Map<String, NPCMode> lookupName = new HashMap<String, NPCMode>();

	public static NPCMode getNPCMode(String name) {
        return lookupName.get(name);
	}

    static {
        for (NPCMode mode : values()) {
            lookupName.put(mode.name(), mode);
        }
    }
}
