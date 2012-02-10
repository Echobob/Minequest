package org.monksanctum.MineQuest.Economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.PropertiesFile;
import org.monksanctum.MineQuest.Event.Relative.MessageEvent;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class NPCStringConfig {
	private List<String> air;
	private List<String> normal;
	private List<String> walk;
	private List<String> hit;
	private List<String> want;
	private List<String> notwant;
	private Random generator;
	
	public NPCStringConfig() {
		PropertiesFile npc_strings = new PropertiesFile(
				"MineQuest/npc_strings.properties");
		
		air = new ArrayList<String>();
		normal = new ArrayList<String>();
		walk = new ArrayList<String>();
		hit = new ArrayList<String>();
		want = new ArrayList<String>();
		notwant = new ArrayList<String>();
		int air_number = npc_strings.getInt("air_string_number", 1);
		int normal_number = npc_strings.getInt("normal_string_number", 1);
		int walk_number = npc_strings.getInt("walk_string_number", 1);
		int hit_number = npc_strings.getInt("hit_string_number", 1);
		int want_number = npc_strings.getInt("want_string_number", 1);
		int notwant_number = npc_strings.getInt("notwant_string_number", 1);
		
		int i;
		for (i = 0; i < air_number; i++) {
			air.add(npc_strings.getString("air_string_" + (i + 1), "I have a great price on %b"));
		}
		
		for (i = 0; i < normal_number; i++) {
			normal.add(npc_strings.getString("normal_string_" + (i + 1), "I am very interested in your %i"));
		}
		
		for (i = 0; i < walk_number; i++) {
			walk.add(npc_strings.getString("walk_string_" + (i + 1), "Hey you!"));
		}
		
		for (i = 0; i < hit_number; i++) {
			hit.add(npc_strings.getString("hit_string_" + (i + 1), "Go away!"));
		}
		
		for (i = 0; i < want_number; i++) {
			want.add(npc_strings.getString("want_string_" + (i + 1), "I will give you %c for your %a %i"));
		}
		
		for (i = 0; i < notwant_number; i++) {
			notwant.add(npc_strings.getString("notwant_string_" + (i + 1), "I am not interested in your %i"));
		}

		generator = new Random();
	}

	public void sendRandomMessage(NPCQuester npcQuester, Quester quester,
			Store store) {
		String message = getRandomMessage(quester, store);
		if (message != null) {
			quester.sendMessage("<" + npcQuester.getName() + "> " + message);
		} else {

		}
	}

	public void sendRandomWalkMessage(NPCQuester npcQuester, Quester quester, int delay) {
		String message = getRandomWalkMessage(quester);
		if (message != null) {
			MineQuest.getEventQueue().addEvent(new MessageEvent(delay, quester, "<" + npcQuester.getName() + "> " + message));
		} else {

		}
	}

	private String getRandomWalkMessage(Quester quester) {
		if (walk.size() == 0) {
			return null;
		}

		int index = generator.nextInt(walk.size());
		
		String ret = new String(walk.get(index));

		ret = processModifiers(ret, quester, null);
		
		return ret;
	}

	public void sendRandomHitMessage(NPCQuester npcQuester, Quester quester, int delay) {
		String message = getRandomHitMessage(quester);
		if (message != null) {
			MineQuest.getEventQueue().addEvent(new MessageEvent(delay, quester, "<" + npcQuester.getName() + "> " + message));
		} else {

		}
	}

	private String getRandomHitMessage(Quester quester) {
		if (hit.size() == 0) {
			return null;
		}

		int index = generator.nextInt(hit.size());
		
		String ret = new String(hit.get(index));

		ret = processModifiers(ret, quester, null);
		
		return ret;
	}

	private String getRandomMessage(Quester quester, Store store) {
		if (quester.getPlayer().getItemInHand().getType() == Material.AIR) {
			return getRandomAirMessage(quester, store);
		} else {
			return getRandomNormMessage(quester, store);
		}
	}

	private String getRandomAirMessage(Quester quester, Store store) {
		if (air.size() == 0) {
			return null;
		}
		
		int index = generator.nextInt(air.size());
		
		String ret = new String(air.get(index));

		ret = processModifiers(ret, quester, store);
		
		return ret;
	}
	
	private String processModifiers(String string, Quester quester, Store store) {
		String ret = string;

		if (quester != null) {
			ret = ret.replaceAll("%n", quester.getName());
			if ((quester.getPlayer() != null) && 
					(quester.getPlayer().getItemInHand() != null)) {
				ret = ret.replaceAll("%i", quester.getPlayer().getItemInHand()
						.getType() + "");
				ret = ret.replaceAll("%a", quester.getPlayer().getItemInHand()
						.getAmount() + "");
				if (store != null) {
					StoreBlock block = store.getBlock(quester.getPlayer().getItemInHand().getTypeId());
					if (block != null) {
						int cubes = block.blocksToCubes(quester.getPlayer().getItemInHand().getAmount(), false);
						ret = ret.replaceAll("%c", StoreBlock.convert(cubes));
					}
				}
			}
		}

		if (store != null) {
			ret = ret.replaceAll("%b", store.getBest().getType());
			ret = ret.replaceAll("%sb", store.getSecondBest().getType());
		}
		
		return ret;
	}

	private String getRandomNormMessage(Quester quester, Store store) {
		if (normal.size() == 0) {
			return null;
		}

		int index = generator.nextInt(normal.size());
		
		String ret = new String(normal.get(index));

		if (store.getBlock(quester.getPlayer().getItemInHand().getTypeId()) != null) {
			ret = processModifiers(ret, quester, store);
		} else {
			return getRandomAirMessage(quester, store);
		}
		
		return ret;
	}

	public void sendWantMessage(NPCQuester npcQuester, Quester quester, Store store) {
		String message = getRandomWantMessage(quester, store);
		if (message != null) {
			quester.sendMessage("<" + npcQuester.getName() + "> " + message);
		} else {

		}
	}

	private String getRandomWantMessage(Quester quester, Store store) {
		if (want.size() == 0) {
			return null;
		}

		int index = generator.nextInt(want.size());
		
		String ret = new String(want.get(index));

		ret = processModifiers(ret, quester, store);
		
		return ret;
	}

	public void sendNotWantMessage(NPCQuester npcQuester, Quester quester, Store store) {
		String message = getRandomNotWantMessage(quester, store);
		if (message != null) {
			quester.sendMessage("<" + npcQuester.getName() + "> " + message);
		} else {

		}
	}

	private String getRandomNotWantMessage(Quester quester, Store store) {
		if (notwant.size() == 0) {
			return null;
		}

		int index = generator.nextInt(notwant.size());
		
		String ret = new String(notwant.get(index));

		ret = processModifiers(ret, quester, store);
		
		return ret;
	}

}
