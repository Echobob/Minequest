package org.monksanctum.MineQuest.Quest.Idle;

import java.util.HashMap;
import java.util.Map;

public enum IdleType {
	AREA(0),
	KILL(1),
	DESTROY(2);
	
	protected static Map<String, Integer> ids = new HashMap<String, Integer>();
	protected int id;
	
	private IdleType(int id) {
		this.id = id;
	}
	
	public static IdleType getIdle(int id) {
		for (IdleType idle : values()) {
			if (ids.get(idle.name()) == id) {
				return idle;
			}
		}
		
		return null;
	}
	
	public int getId() {
		return id;
	}
	
	static {
		for (IdleType idle : values()) {
			ids.put(idle.name(), idle.getId());
		}
	}
}
