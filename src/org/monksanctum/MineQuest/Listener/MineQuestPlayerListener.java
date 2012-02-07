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
package org.monksanctum.MineQuest.Listener;


import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Economy.NPCSignShop;
import org.monksanctum.MineQuest.Economy.Store;
import org.monksanctum.MineQuest.Economy.StoreBlock;
import org.monksanctum.MineQuest.Event.NormalEvent;
import org.monksanctum.MineQuest.Event.Absolute.EntityTeleportEvent;
import org.monksanctum.MineQuest.Quest.QuestProspect;
import org.monksanctum.MineQuest.Quester.NPCMode;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;
import org.monksanctum.MineQuest.Quester.SkillClass.SkillClass;
import org.monksanctum.MineQuest.World.Property;
import org.monksanctum.MineQuest.World.Town;

public class MineQuestPlayerListener extends PlayerListener {

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!MineQuest.isMQEnabled(event.getPlayer())) return;
		Quester quester = MineQuest.questerHandler.getQuester(event.getPlayer());
		
		if (!MineQuest.questerHandler.getQuester(event.getPlayer()).healthIncrease(event)) {
			event.setCancelled(!quester.canEdit(event.getClickedBlock()));
		}
		
		if (event.getClickedBlock() != null) {
			if (event.getClickedBlock().getType() == Material.CHEST) {
				quester.getChestSet().clicked(event.getPlayer(), event.getClickedBlock());
			}
		}
		
		super.onPlayerInteract(event);
	}
	
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		if (!MineQuest.isMQEnabled(event.getPlayer())) return;
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			Quester quester = MineQuest.questerHandler.getQuester(event.getPlayer());
			
			quester.armSwing();
		}
		
		super.onPlayerAnimation(event);
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!MineQuest.isMQEnabled(event.getPlayer())) return;
		MineQuest.questerHandler.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		MineQuest.questerHandler.getQuester(event.getPlayer()).move(event.getFrom(), event.getTo());
		super.onPlayerMove(event);
	}

	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (MineQuest.questerHandler.getQuester(event.getPlayer()) == null) {
			MineQuest.questerHandler.addQuester(new Quester(event.getPlayer(), 0));
		}
		MineQuest.questerHandler.getQuester(event.getPlayer()).update(event.getPlayer());
//		if (MineQuest.getSServer().getOnlinePlayers().length == 1) {
//			MineQuest.respawnNPCs();
//		}
	}
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (!MineQuest.isMQEnabled(event.getPlayer())) return;
		MineQuest.questerHandler.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		MineQuest.questerHandler.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		if (MineQuest.questerHandler.getQuester(event.getPlayer()) != null) {
			MineQuest.questerHandler.getQuester(event.getPlayer()).save();
			MineQuest.questerHandler.getQuester(event.getPlayer()).setPlayer(null);
		}
	}
	
	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (!MineQuest.isMQEnabled(event.getPlayer())) return;
		MineQuest.questerHandler.getQuester(event.getPlayer()).respawn(event);
		MineQuest.questerHandler.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (!MineQuest.isMQEnabled(event.getPlayer())) return;
		MineQuest.questerHandler.getQuester(event.getPlayer()).setPlayer(event.getPlayer());
		String split[] = event.getMessage().split(" ");
		Player player = event.getPlayer();
		Quester quester = MineQuest.questerHandler.getQuester(player);
		
		split[0] = split[0].toLowerCase().replaceAll("_", "");
		try {
			if (quester.canCommand("Quester")) {
				processQuester(split, player, event);
			}
			if (event.isCancelled()) return;
			if (MineQuest.config.cubonomy_enable) {
				if (quester.canCommand("Store")) {
					processStore(split, player, event);
				}
				if (event.isCancelled()) return;
			}
			if (quester.canCommand("Quest")) {
				processQuest(split, player, event);
			}
			if (event.isCancelled()) return;
			if (MineQuest.config.town_enable) {
				if (quester.canCommand("Town")) {
					processTown(split, player, event);
				}
				if (event.isCancelled()) return;
			}
			if (MineQuest.config.npc_enabled) {
				if (quester.canCommand("Merc")) {
					processMerc(split, player, event);
				}
				if (event.isCancelled()) return;
			}
			if (MineQuest.config.debug_enable) {
				if (quester.canCommand("Debug")) {
					processDebug(split, player, event);
				}
				if (event.isCancelled()) return;
			}
			processHelp(split, player, event);
		} catch (Exception e) {
			player.sendMessage("Congratulations! You found a bug!");
			player.sendMessage("Please contact help@theminequest.com");
			e.printStackTrace();
		}
	}
	
	private void processHelp(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/help")) {
			player.sendMessage("To get started with the leveling, abilities");
			player.sendMessage("and experience system type /minequest");
			player.sendMessage("To get started with the economy system try");
			player.sendMessage("/cubonomy");
			player.sendMessage("To get started with the questing system try ");
			player.sendMessage("/quest");
		} else if (split[0].equals("/minequest")) {
			player.sendMessage("Minequest Commands:");
			player.sendMessage("    /char - information about your character level");
			player.sendMessage("    /class <classname> - information about a specific class");
			player.sendMessage("    /health - display your health");
			player.sendMessage("    /abillist [classname] - display all abilities or for a specific class");
			player.sendMessage("    /enableabil <ability name> - enable an ability (enabled by default)");
			player.sendMessage("    /disableabil <ability name> - disable an ability");
			player.sendMessage("    /bind <ability name> - bind an ability to current item");
			player.sendMessage("    /unbind - unbind current item from all abilities");
			player.sendMessage("    /spellcomp <ability name> - list the components required for an ability");
			event.setCancelled(true);
		} else if (split[0].equals("/cubonomy")) {
        	if ((split.length == 2) && (Integer.parseInt(split[1]) == 2)) {
				player.sendMessage("Available Commands (2 of 2):");
				player.sendMessage("    /costb <store_index or name> <amount>");
				player.sendMessage("          - cost of buying amount of name");
				player.sendMessage("    /costs <store_index or name> <amount>");
				player.sendMessage("          - cost of selling amount of name");
				player.sendMessage("    /cubes - money you have");
        	} else {
				player.sendMessage("Available Commands (1 of 2):");
				player.sendMessage("    /store <page_num> - list contents");
				player.sendMessage("    /buy <name> <amount> - buy amount of name");
				player.sendMessage("    /sell <name> <amount> - sell amount of name");
				player.sendMessage("    /buyi <item_index> <amount> - buy amount of index");
				player.sendMessage("    /selli <item_index> <amount> - sell amount of index");
				player.sendMessage("for more type: /cubonomy 2");
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/quest")) {
        	player.sendMessage("Quest Related Commands:");
			player.sendMessage("    /create_party - create a party");
			player.sendMessage("    /list_party - list users in your party");
			player.sendMessage("    /join_party <username> - join username's party");
			player.sendMessage("    /start_quest <name of quest> - start a quest with party");
			player.sendMessage("    /quit_quest - quit the instance of quest, lose current exp");
			player.sendMessage("    /class_exp - list amount of unassigned exp");
			player.sendMessage("    /assign_exp <class> <amount> - assign amount exp to class");
			event.setCancelled(true);
        }else if (split[0].equals("/new_binder")) {
        	player.sendMessage("Item binding is now restricted to one binding per item.");
        	player.sendMessage("This is because it was too confusing for right click binding");
        	player.sendMessage("when it only works on some items. Now spells are bound to both.");
        	player.sendMessage("either right click or left click will activate.");
        	player.sendMessage("Rebinders have been added to the Server and can be created using");
        	player.sendMessage("/binder Ability_Name item_id_to_bind");
        	player.sendMessage("while holding the item to create it for.");
        	event.setCancelled(true);
        } 
	}
	
	private void processQuest(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/startquest") || split[0].equals("/start_quest")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /start_quest filename");
        	} else {
        		if (MineQuest.questerHandler.getQuester(player).getParty() == null) {
        			MineQuest.questerHandler.getQuester(player).createParty();
        		}
        		String qname = split[1];
        		int i;
        		for (i = 2; i < split.length; i++) qname = qname + " " + split[i];
        		MineQuest.questerHandler.getQuester(player).startQuest(qname);
        	}
           	event.setCancelled(true);
        } else if (split[0].equals("/classexp")) {
        	MineQuest.questerHandler.getQuester(player).sendMessage("You have " + MineQuest.questerHandler.getQuester(player).getClassExp() + " unassigned experience");
        	event.setCancelled(true);
        } else if (split[0].equals("/assignexp")) {
        	if (split.length < 3) {
        		player.sendMessage("Usage: /assign_exp class_name amount");
        	} else {
        		MineQuest.questerHandler.getQuester(player).spendClassExp(split[1], Integer.parseInt(split[2]));
        	}
        	event.setCancelled(true);
        }  else if (split[0].equals("/createparty")) {
        	MineQuest.questerHandler.getQuester(player).createParty();
        	player.sendMessage("Party Created");
        	event.setCancelled(true);
        } else if (split[0].equals("/joinparty")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /join_party player_name");
        	} else {
        		if (MineQuest.questerHandler.getQuester(split[1]) == null) {
        			player.sendMessage(split[1] + " is not a valid quester");
        		} else if (MineQuest.questerHandler.getQuester(split[1]).getParty() == null) {
        			player.sendMessage(split[1] + " is not in a party");
        		} else {
        			MineQuest.questerHandler.getQuester(split[1]).getParty().addQuester(MineQuest.questerHandler.getQuester(player));
        		}
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/quitquest")) {
        	if (MineQuest.questerHandler.getQuester(player).getQuest() != null) {
        		MineQuest.questerHandler.getQuester(player).getQuest().removeQuester(MineQuest.questerHandler.getQuester(player));
        	} else {
        		player.sendMessage("You are not in a quest...");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/listparty")) {
        	if (MineQuest.questerHandler.getQuester(player).getParty() != null) {
            	for (Quester quester : MineQuest.questerHandler.getQuester(player).getParty().getQuesters()) {
            		player.sendMessage(quester.getName());
            	}
        	} else {
        		player.sendMessage("You are not in a party");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/listquest")) {
        	for (QuestProspect qp : MineQuest.questerHandler.getQuester(player).getAvailableQuests()) {
        		player.sendMessage(qp.getName());
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/listcompletequest")) {
        	for (QuestProspect qp : MineQuest.questerHandler.getQuester(player).getCompletedQuests()) {
        		player.sendMessage(qp.getName());
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/journal")) {
        	if (split.length == 1) {
        		MineQuest.questerHandler.getQuester(player).journal();
        	} else {
    			String quest = split[1];
    			int i;
    			for (i = 2; i < split.length; i++) quest = quest + " " + split[i];
        		MineQuest.questerHandler.getQuester(player).journal(quest);
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/listidlequest")) {
        	for (QuestProspect qp : MineQuest.questerHandler.getQuester(player).getIdleQuests()) {
        		player.sendMessage(qp.getName());
        	}
        	event.setCancelled(true);
        }
	}
	
	private void processQuester(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/mystash")) {
			MineQuest.questerHandler.getQuester(player).getChestSet().add(player);
			event.setCancelled(true);
		} else if (split[0].equals("/cancel")) {
			MineQuest.questerHandler.getQuester(player).getChestSet().cancelAdd(player);
			event.setCancelled(true);
		} else if (split[0].equals("/dropstash")) {
			Town town = MineQuest.townHandler.getTown(player);
			MineQuest.questerHandler.getQuester(player).getChestSet().rem(player, town);
			event.setCancelled(true);
		} else if (split[0].equals("/moddedclient")) {
			MineQuest.questerHandler.getQuester(player).setModded();
			event.setCancelled(true);
		} else if (split[0].equals("/char")) {
			Quester quester = MineQuest.questerHandler.getQuester(player);
			player.sendMessage("You are level " + quester.getLevel() + " with " + quester.getExp() + "/" + (400 * (quester.getLevel() + 1)) + " Exp");

			for (SkillClass skill : quester.getClasses()) {
				skill.display();
			}
			event.setCancelled(true);
		} else if (split[0].equals("/save")) {
			MineQuest.questerHandler.getQuester(player).save();
			event.setCancelled(true);
		} else if (split[0].equals("/load")) {
			MineQuest.questerHandler.getQuester(player).update();
			event.setCancelled(true);
		} else if (split[0].equals("/abillist")) {
			if (split.length < 2) {
				MineQuest.questerHandler.getQuester(player).listAbil();
			} else {
				if (MineQuest.questerHandler.getQuester(player).getClass(split[1]) != null) {
					MineQuest.questerHandler.getQuester(player).getClass(split[1]).listAbil();
				} else {
					player.sendMessage(split[1] + " is not a valid class");
				}
			}
			event.setCancelled(true);
		} else if (split[0].equals("/unbind")) {
			MineQuest.questerHandler.getQuester(player).unBind(player.getItemInHand());
			event.setCancelled(true);
		} else if (split[0].equals("/enableabil")) {
			if (split.length < 2) return;
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			MineQuest.questerHandler.getQuester(player).enableabil(abil);
			event.setCancelled(true);
		} else if (split[0].equals("/disableabil")) {
			if (split.length < 2) return;
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			MineQuest.questerHandler.getQuester(player).disableabil(abil);
			event.setCancelled(true);
		} else if (split[0].equals("/bind")) {
			if (split.length < 2) {
				player.sendMessage("Usage: /bind <ability>");
				event.setCancelled(true);
				return;
			}
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			MineQuest.questerHandler.getQuester(player).bind(abil);
			event.setCancelled(true);
		} else if (split[0].equals("/lookbind")) {
			if (split.length < 2) {
				player.sendMessage("Usage: /look_bind <ability>");
				event.setCancelled(true);
				return;
			}
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			MineQuest.questerHandler.getQuester(player).lookBind(abil);
			event.setCancelled(true);
		} else if (split[0].equals("/class")) {
			if (split.length < 2) {
				player.sendMessage("Usage: /class <class_name>");
				event.setCancelled(true);
			}
			MineQuest.questerHandler.getQuester(player).getClass(split[1]).display();
			event.setCancelled(true);
		} else if (split[0].equals("/health")) {
			player.sendMessage("Your health is " + MineQuest.questerHandler.getQuester(player).getHealth() + "/" + MineQuest.questerHandler.getQuester(player).getMaxHealth());
			event.setCancelled(true);
		} else if (split[0].equals("/mana")) {
			player.sendMessage("Your mana is " + MineQuest.questerHandler.getQuester(player).getMana() + "/" + MineQuest.questerHandler.getQuester(player).getMaxMana());
			event.setCancelled(true);
		} else if (split[0].equals("/spellcomp")) {
			if (split.length < 2) {
				return;
			}
			String abil = split[1];
			int i;
			for (i = 2; i < split.length; i++) abil = abil + " " + split[i];
			player.sendMessage(MineQuest.listSpellComps(abil));
			event.setCancelled(true);
		}  else if (split[0].equals("/binder")) {
        	if (split.length < 3) {
        		player.sendMessage("Usage: /binder Ability_Name item_id");
        	} else {
    			String abil = split[1];
    			int i;
    			for (i = 2; i < split.length - 1; i++) abil = abil + " " + split[i];
            	int item = Integer.parseInt(split[i]);
            	
            	MineQuest.questerHandler.getQuester(player).addBinder(abil, item);
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/itemid")) {
        	ItemStack item = player.getItemInHand();
        	if (item != null) {
        		player.sendMessage(item.getType().name() + " is item id " + item.getTypeId());
        	} else {
        		player.sendMessage("You are not holding anything");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/replace")) {
        	if (split.length < 3) {
        		player.sendMessage("Usage: /replace old_ability_name with new_ability_name");
        	} else {
        		int divider;
        		for (divider = 1; divider < split.length; divider++) {
        			if (split[divider].equals("with")) {
        				break;
        			}
        		}
        		if (divider < split.length) {
            		int i;
            		String first = split[1];
            		for (i = 2; i < divider; i++) {
            			first = first + " " + split[i];
            		}
            		String second = split[divider + 1];
            		for (i = divider + 2; i < split.length; i++) {
            			second = second + " " + split[i];
            		}
	        		SkillClass skill = MineQuest.questerHandler.getQuester(player).getClassFromAbil(first);
	        		if (skill == null) {
	        			player.sendMessage(split[1] + " is not a valid ability");
	        		} else {
	        			if (MineQuest.config.town_enable && (MineQuest.townHandler.getTown(player) == null)) {
	        				player.sendMessage("Must be in a town to modify spellbook");
	        			} else {
	        				skill.replaceAbil(first, second);
	        			}
	        		}
        		} else {
            		player.sendMessage("Usage: /replace old_ability_name with new_ability_name");
        		}
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/addclass")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /addclass <combat_class>");
        	} else {
        		MineQuest.questerHandler.getQuester(player).addClass(split[1]);
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/prof") || split[0].equals("/proficiency")) {
        	if (split.length < 2) {
	        	MineQuest.questerHandler.getQuester(player).listProf();
        	} else {
        		int id = Integer.parseInt(split[1]);
        		MineQuest.questerHandler.getQuester(player).listProf(id);
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/priorityclass")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /priority_class <class_name>");
        		return;
        	}
        	MineQuest.questerHandler.getQuester(player).priorClass(split[1]);
        	event.setCancelled(true);
        }
	}
	
	private void processStore(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/store")) {
    		int page;
    		if (split.length > 1) {
    			page = Integer.parseInt(split[1]);
    		} else {
    			page = 0;
    		}
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.displayPage(MineQuest.questerHandler.getQuester(player), page);
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        } else if (split[0].equals("/buy")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.buy(MineQuest.questerHandler.getQuester(player), split[1], Integer.parseInt(split[2]));
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/buyi")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.buy(MineQuest.questerHandler.getQuester(player), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/costb")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.cost(MineQuest.questerHandler.getQuester(player), split[1], Integer.parseInt(split[2]), true);
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/costs")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.cost(MineQuest.questerHandler.getQuester(player), split[1], Integer.parseInt(split[2]), false);
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/sell")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.sell(MineQuest.questerHandler.getQuester(player), split[1], Integer.parseInt(split[2]));
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/selli")) {
        	if (split.length < 3) {
        		return;
        	}
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);
    			
    			if (store != null) {
    				store.sell(MineQuest.questerHandler.getQuester(player), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town - stores are only found in towns");
    		}
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/cubes") || split[0].equals("/mqmoney")) {
        	String cubes_string = StoreBlock.convert((long)MineQuest.questerHandler.getQuester(player).getCubes());
	    	
			player.sendMessage("You have " + cubes_string);
			event.setCancelled(true);
        	return;
        } else if (split[0].equals("/addblock")) {
        	if (split.length < 4) {
        		player.sendMessage("Usage: /add_block type price item_id");
        		return;
        	}
        	
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);

    			if (store != null) {
    				Block block = player.getWorld().getBlockAt(player.getLocation());
    				if (MineQuest.questerHandler.getQuester(player).canEdit(block)) {
        				store.addBlock(split[1], split[2], split[3]);
        				player.sendMessage(split[1] + " added to store");
    				} else {
    					player.sendMessage("You are not permitted to edit this store");
    				}
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town");
    		}
			event.setCancelled(true);
        } else if (split[0].equals("/remblock")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /rem_block type");
        		return;
        	}
        	
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);

    			if (store != null) {
    				Block block = player.getWorld().getBlockAt(player.getLocation());
    				if (MineQuest.questerHandler.getQuester(player).canEdit(block)) {
        				store.remBlock(split[1]);
        				player.sendMessage(split[1] + " removed from store");
    				} else {
    					player.sendMessage("You are not permitted to edit this store");
    				}
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town");
    		}
			event.setCancelled(true);
        } else if (split[0].equals("/setstorequantity")) {
        	if (split.length < 3) {
        		player.sendMessage("Usage: /set_store_quantity type amount");
        		return;
        	}
        	int amount;
        	try {
        		amount = Integer.parseInt(split[2]);
        	} catch (Exception e) {
        		player.sendMessage("Usage: /set_store_quantity type amount");
        		return;
        	}
        	
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);

    			if (store != null) {
    				Block block = player.getWorld().getBlockAt(player.getLocation());
    				if (MineQuest.questerHandler.getQuester(player).canEdit(block)) {
        				store.setBlockQuant(split[1], amount);
        				player.sendMessage(split[1] + " removed from store");
    				} else {
    					player.sendMessage("Item " + split[1] + " quantity set to: " + split[2]);
    				}
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town");
    		}
			event.setCancelled(true);
        } else if (split[0].equals("/initstore")) {
        	if (MineQuest.townHandler.getTown(player) == null) {
        		player.sendMessage("You are not in a town");
    			event.setCancelled(true);
    			return;
        	}
        	if (MineQuest.townHandler.getTown(player).getStore(player) == null) {
        		player.sendMessage("You are not in a store");
    			event.setCancelled(true);
    			return;
        	}
        	if (!MineQuest.townHandler.getTown(player).getTownProperty().canEdit(MineQuest.questerHandler.getQuester(player))) {
        		player.sendMessage("You do not have permission to edit town");
    			event.setCancelled(true);
    			return;
        	}
        	
        	NPCSignShop shop = MineQuest.townHandler.getTown(player).getStore(player);
        	shop.intialize(MineQuest.questerHandler.getQuester(player));
			event.setCancelled(true);
        } else if (split[0].equals("/spawnstorenpc")) {
        	if (MineQuest.townHandler.getTown(player) == null) {
        		player.sendMessage("You are not in a town");
    			event.setCancelled(true);
    			return;
        	}
        	if (MineQuest.townHandler.getTown(player).getStore(player) == null) {
        		player.sendMessage("You are not in a store");
    			event.setCancelled(true);
    			return;
        	}
        	if (!MineQuest.townHandler.getTown(player).getTownProperty().canEdit(MineQuest.questerHandler.getQuester(player))) {
        		player.sendMessage("You do not have permission to edit town");
    			event.setCancelled(true);
    			return;
        	}
        	
        	Location location = player.getLocation();
    		if (MineQuest.questerHandler.getQuester(split[1]) == null) {
    			if (nameCheck(split[1], player)) {
    				MineQuest.questerHandler.addQuester(new NPCQuester(split[1], NPCMode.STORE, player.getWorld(), location));
    			}
    		} else {
    			player.sendMessage("A quester with that name already exists!");
    		}
        	event.setCancelled(true);
        } else if (split[0].equals("/deletestore")) {
    		Town town = MineQuest.townHandler.getTown(player);
    		if (town != null) {
    			Store store = town.getStore(player);

    			if (store != null) {
    				Block block = player.getWorld().getBlockAt(player.getLocation());
    				if (MineQuest.questerHandler.getQuester(player).canEdit(block)) {
	    				store.delete();
	    				town.remove(store);
	    				player.sendMessage("Store deleted");
    				}
    			} else {
    				player.sendMessage("You are not in a store");
    			}
    		} else {
    			player.sendMessage("You are not in a town");
    		}
			event.setCancelled(true);
        }
	}
	
	private void processTown(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/townspawn")) {
			int index, i;
			double distance;
			List<Town> towns = MineQuest.townHandler.getTowns();
			
			if (MineQuest.questerHandler.getQuester(player).inQuest()) {
				player.sendMessage("You cannot do that inside a quest");
				event.setCancelled(true);
				return;
			}
			if (towns.size() == 0) {
				player.sendMessage("There are no spawns - Contact your administrator");
				event.setCancelled(true);
				return;
			}
			index = 0;
			distance = towns.get(0).calcDistance(player);
			for (i = 1; i < towns.size(); i++) {
				if (towns.get(i).calcDistance(player) < distance) {
					distance = towns.get(i).calcDistance(player);
					index = i;
				}
			}
			player.sendMessage("Welcome to " + towns.get(index).getName());
			player.teleport(towns.get(index).getSpawn());
			if (MineQuest.config.health_spawn_enable) {
				Quester quester = MineQuest.questerHandler.getQuester(player);
				quester.setHealth(quester.getMaxHealth());
			}
			event.setCancelled(true);
		} else if (split[0].equals("/townloc")) {
			player.sendMessage("You are at " + player.getLocation().getX() + " " + player.getLocation().getY() + " " + 
					player.getLocation().getZ() + " P:" + player.getLocation().getPitch() + " Y:" + player.getLocation().getYaw());
			event.setCancelled(true);
		}else if (split[0].equals("/startcreate")) {
        	MineQuest.townHandler.startCreate(player);
			event.setCancelled(true);
        } else if (split[0].equals("/finishtown")) {
        	if (split.length <= 1) {
        		player.sendMessage("Usage: /finishtown <name>");
        	} else {
        		MineQuest.townHandler.finishTown(player, split[1]);
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/finishvillage")) {
        	if (split.length <= 1) {
        		player.sendMessage("Usage: /finishvillage <name>");
        	} else {
        		MineQuest.townHandler.finishVillage(player, split[1]);
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/finishclaim")) {
        	if (split.length <= 1) {
        		player.sendMessage("Usage: /finishclaim <name>");
        	} else {
        		MineQuest.townHandler.finishClaim(player, split[1]);
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/setmayor")) {
        	if ((MineQuest.townHandler.getTown(player) != null) && (MineQuest.townHandler.getTown(player).getTownProperty().getOwner().equals(MineQuest.questerHandler.getQuester(player)))) {
        		MineQuest.townHandler.getTown(player).setOwner(split[1]);
        	} else {
        		player.sendMessage("You are not in a town or you are not the mayor");
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/createproperty")) {
        	if (MineQuest.townHandler.getTown(player) != null) {
        		MineQuest.townHandler.getTown(player).createProperty(player);
        	} else {
        		player.sendMessage("You are not in a town");
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/finishproperty")) {
        	if (split.length <= 0) {
        		player.sendMessage("Usage: /finishproperty [set-height]");
        	} else {
	        	if (MineQuest.townHandler.getTown(player) != null) {
	        		MineQuest.townHandler.getTown(player).finishProperty(player, split.length > 1);
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/createstore")) {
        	if (MineQuest.townHandler.getTown(player) != null) {
        		MineQuest.townHandler.getTown(player).createStore(player);
        	} else {
        		player.sendMessage("You are not in a town");
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/finishstore")) {
        	if (split.length <= 1) {
        		player.sendMessage("Usage: /finishstore <unique name>");
        	} else {
	        	if (MineQuest.townHandler.getTown(player) != null) {
	        		MineQuest.townHandler.getTown(player).finishStore(player, split[1]);
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/setprice")) {
        	if (split.length <= 1) {
        		player.sendMessage("Usage: /setprice <price>");
        	} else {
	        	if (MineQuest.townHandler.getTown(player) != null) {
	        		MineQuest.townHandler.getTown(player).setPrice(player, Long.parseLong(split[1]));
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
        	}
			event.setCancelled(true);
        } else if (split[0].equals("/mqtown")) {
			if (MineQuest.townHandler.getTown(player) != null) {
				player.sendMessage("You are in " + MineQuest.townHandler.getTown(player).getName());
			} else {
				player.sendMessage("You are not in a town");
			}
			event.setCancelled(true);
        } else if (split[0].equals("/setspawn")) {
			if (MineQuest.townHandler.getTown(player) != null) {
				MineQuest.townHandler.getTown(player).setSpawn(player.getLocation());
				player.sendMessage("Spawn location set");
			} else {
				player.sendMessage("You are not in a town");
			}
			event.setCancelled(true);
        } else if (split[0].equals("/price")) {
        	if ((MineQuest.townHandler.getTown(player) != null) && (MineQuest.townHandler.getTown(player).getProperty(player) != null)) {
        		Property prop = MineQuest.townHandler.getTown(player).getProperty(player);
        		if (prop.getOwner() == null) {
        			player.sendMessage("The price of this property is " + StoreBlock.convert(prop.getPrice()));
        		} else {
        			player.sendMessage("This property is not for sale");
        		}
        	} else {
        		player.sendMessage("You are not on a property");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/buyprop")) {
        	Town town = MineQuest.townHandler.getTown(player);
        	if (town == null) {
        		player.sendMessage("You are not in a town");
            	event.setCancelled(true);
            	return;
        	}
        	Property prop = town.getProperty(player);
        	Quester quester = MineQuest.questerHandler.getQuester(player);
        	if (prop != null) {
	        	if (prop.getOwner() == null) {
	        		if (quester.getCubes() > prop.getPrice()) {
	        			town.buy(quester, prop);
	        		} else {
	        			player.sendMessage("You cannot afford this property");
	        		}
	        	} else {
	        		player.sendMessage("This Property is not for sale");
	        	}
        	} else {
        		player.sendMessage("You are not on a property");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/remprop")) {
        	Town town = MineQuest.townHandler.getTown(player);
        	if (town == null) {
        		player.sendMessage("You are not in a town");
            	event.setCancelled(true);
            	return;
        	}
        	Property prop = town.getProperty(player);
        	Quester quester = MineQuest.questerHandler.getQuester(player);
        	if (prop != null) {
	        	if (town.getTownProperty().canEdit(quester)) {
	        		town.remove(prop);
	        	} else {
	        		player.sendMessage("You are not authorized to modify town");
	        	}
        	} else {
        		player.sendMessage("You are not on a property");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/addedit")) {
        	event.setCancelled(true);
	    	if (MineQuest.questerHandler.getQuester(player).canEdit(player.getWorld().getBlockAt(player.getLocation()))) {
	        	Town town = MineQuest.townHandler.getTown(player);
	        	if (town != null) {
	        		Property prop = town.getProperty(player);
	        		if (prop == null) prop = town.getTownProperty();
	        		
	        		if ((split.length < 2) || (MineQuest.questerHandler.getQuester(split[1]) == null)) {
	        			player.sendMessage("Usage: /addedit <username>");
	        		} else {
	        			prop.addEdit(MineQuest.questerHandler.getQuester(split[1]));
	        			player.sendMessage("Editor " + split[1] + " added");
	        		}
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
	    	}
        } else if (split[0].equals("/remedit")) {
        	event.setCancelled(true);
	    	if (MineQuest.questerHandler.getQuester(player).canEdit(player.getWorld().getBlockAt(player.getLocation()))) {
	        	Town town = MineQuest.townHandler.getTown(player);
	        	if (town != null) {
	        		Property prop = town.getProperty(player);
	        		if (prop == null) prop = town.getTownProperty();
	        		
	        		if ((split.length < 2) || (MineQuest.questerHandler.getQuester(split[1]) == null)) {
	        			player.sendMessage("Usage: /addedit <username>");
	        		} else {
	        			prop.remEdit(MineQuest.questerHandler.getQuester(split[1]));
	        			player.sendMessage("Editor " + split[1] + " removed");
	        		}
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
	    	}
        } else if (split[0].equals("/deletetown")) {
        	event.setCancelled(true);
	    	if (MineQuest.questerHandler.getQuester(player).canEdit(player.getWorld().getBlockAt(player.getLocation()))) {
	        	Town town = MineQuest.townHandler.getTown(player);
	        	if (town != null) {
	        		town.delete();
	        		MineQuest.townHandler.remTown(town);
	        		player.sendMessage("Town deleted");
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
	    	}
        } else if (split[0].equals("/expandtown")) {
        	event.setCancelled(true);
        	if (split.length < 2) {
        		player.sendMessage("Usage: /expand_town <town_name>");
        		return;
        	}
        	Town town = MineQuest.townHandler.getTown(split[1]);
        	
        	if (town == null) {
        		player.sendMessage(split[1] + " is not a valid town");
        	} else {
        		town.expand(MineQuest.questerHandler.getQuester(player));
        	}
        } else if (split[0].equals("/settowny")) {
        	event.setCancelled(true);
        	if (split.length < 3) {
        		player.sendMessage("Usage: /set_town_y <town_name> <y>");
        		return;
        	}
        	Town town = MineQuest.townHandler.getTown(split[1]);
        	
        	if (town == null) {
        		player.sendMessage(split[1] + " is not a valid town");
        	} else {
        		town.setMinY(MineQuest.questerHandler.getQuester(player), Integer.parseInt(split[2]));
        	}
        } else if (split[0].equals("/settownheight")) {
        	event.setCancelled(true);
        	if (split.length < 3) {
        		player.sendMessage("Usage: /set_town_height <town_name> <height>");
        		return;
        	}
        	Town town = MineQuest.townHandler.getTown(split[1]);
        	
        	if (town == null) {
        		player.sendMessage(split[1] + " is not a valid town");
        	} else {
        		town.setHeight(MineQuest.questerHandler.getQuester(player), Integer.parseInt(split[2]));
        	}
        }
	}
	
	private void processMerc(String[] split, Player player, PlayerChatEvent event) {
	    if (split[0].equals("/regroup")) {
	    	MineQuest.questerHandler.getQuester(player).regroup();
	    	event.setCancelled(true);
	    } else if (split[0].equals("/npcproperty")) {
	    	if (split.length < 4) {
	    		player.sendMessage("Usage: /npc_property <npc_name> <property_name> <property_value>");
	    		event.setCancelled(true);
	    		return;
	    	}
    		String value = split[3];
    		int i;
    		for (i = 4; i < split.length; i++) value = value + " " + split[i];
    		if (MineQuest.questerHandler.getQuester(split[1].replaceAll("_", " ")) instanceof NPCQuester) {
    			((NPCQuester)MineQuest.questerHandler.getQuester(split[1].replaceAll("_", " "))).setProperty(split[2], value);
    		} else {
    			player.sendMessage(split[1].replaceAll("_", " ") + " is not a valid NPC");
    		}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/listmercs")) {
	    	if (MineQuest.townHandler.getTown(player) != null) {
	    		player.sendMessage("Available in " + MineQuest.townHandler.getTown(player).getName() + ":");
	    		for (NPCQuester quester : MineQuest.townHandler.getTown(player).getAvailableNPCs()) {
	    			player.sendMessage(quester.getName() + " : " + quester.getCost());
	    		}
	    	} else {
	    		player.sendMessage("You are not in a town");
	    	}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/setmercspawn")) {
	    	if (MineQuest.townHandler.getTown(player) != null) {
	    		if (MineQuest.townHandler.getTown(player).getTownProperty().canEdit(MineQuest.questerHandler.getQuester(player))) {
	        		MineQuest.townHandler.getTown(player).setMERCSpawn(player.getLocation());
	        		player.sendMessage("Mercenary Spawn Set");
	    		} else {
	    			player.sendMessage("You do not have permission to edit town");
	    		}
	    	} else {
	    		player.sendMessage("You are not in a town");
	    	}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/spawnmerc")) {
	    	if (split.length > 1) {
	        	if (MineQuest.townHandler.getTown(player) != null) {
	        		if (MineQuest.questerHandler.getQuester(split[1]) == null) {
	        			if (nameCheck(split[1], player)) {
	        				MineQuest.townHandler.getTown(player).addMerc(split[1], MineQuest.questerHandler.getQuester(player));
	        			}
	        		} else {
	        			player.sendMessage("A quester with that name already exists!");
	        		}
	        	} else {
	        		player.sendMessage("You are not in a town");
	        	}
	    	} else {
	    		player.sendMessage("Usage: /spawn_merc name");
	    	}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/buymerc")) {
	    	if (split.length < 2) {
	    		player.sendMessage("Usage: /buy_merc <npc_name>");
				event.setCancelled(true);
				return;
			}
			if ((MineQuest.questerHandler.getQuester(split[1]) instanceof NPCQuester)
					&& (NPCMode.FOR_SALE == ((NPCQuester) MineQuest
							.questerHandler.getQuester(split[1])).getMode())) {
				((NPCQuester) MineQuest.questerHandler.getQuester(split[1])).buyNPC(MineQuest
						.questerHandler.getQuester(player));
			} else {
    			player.sendMessage(split[1] + " is not a mercenary for hire");
    		}
	    	event.setCancelled(true);
	    } else if (split[0].equals("/setmercitem")) {
	    	if (split.length < 2) {
	    		player.sendMessage("Usage: /set_merc_item <npc_name>");
				event.setCancelled(true);
				return;
	    	}
			Quester quester = MineQuest.questerHandler.getQuester(split[1]);
			if (!(quester instanceof NPCQuester) || !(MineQuest.questerHandler.getQuester(player).hasQuester(quester))) {
	    		player.sendMessage(split[1] + " is not one of your mercenaries");
				event.setCancelled(true);
				return;
			}
	    	((NPCQuester)quester).giveItem(MineQuest.questerHandler.getQuester(player));
	    	event.setCancelled(true);
	    } else if (split[0].equals("/npcchar") || split[0].equals("/mercchar")) {
	    	if (split.length < 2) {
	    		player.sendMessage("Usage: /merc_char <merc_name>");
				event.setCancelled(true);
				return;
	    	}
			Quester quester = MineQuest.questerHandler.getQuester(split[1]);
			if (!(quester instanceof NPCQuester) || !(MineQuest.questerHandler.getQuester(player).hasQuester(quester))) {
	    		player.sendMessage(split[1] + " is not one of your mercenaries");
				event.setCancelled(true);
				return;
			}
			player.sendMessage(split[1] + " is a level " + quester.getLevel() + " with " + quester.getExp() + "/" + (400 * (quester.getLevel() + 1)) + " Exp");
	
			quester.getClass("Warrior").display();
			quester.sendMessage(" Health: " + quester.getHealth() + "/" + quester.getMaxHealth());
			event.setCancelled(true);
	    } else if (split[0].equals("/mymercs")) {
	    	MineQuest.questerHandler.getQuester(player).listMercs();
	    	event.setCancelled(true);
	    }
	}
	
	private boolean nameCheck(String string, Player player) {
		String[] bad_chars = new String[] {"-", "(", ")", "[", "]", " ", ",", "."};
		for (String chars : bad_chars) {
			if (string.contains(chars)) {
				player.sendMessage("NPC Names cannot contain " + chars);
				return false;
			}
		}
		
		return true;
	}

	@SuppressWarnings("deprecation")
	private void processDebug(String[] split, Player player, PlayerChatEvent event) {
		if (split[0].equals("/goto")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /goto world_name type");
        	} else {
        		World world = MineQuest.getSServer().getWorld(split[1]);
        		if (world == null) {
        			if ((split.length < 3) || !split[2].equalsIgnoreCase("Nether")) {
        				world = MineQuest.getSServer().createWorld(split[1], Environment.NORMAL);
        			} else {
        				world = MineQuest.getSServer().createWorld(split[1], Environment.NETHER);
        			}
        		}
        		MineQuest.getEventQueue().addEvent(new EntityTeleportEvent(10, 
        				MineQuest.questerHandler.getQuester(player), world.getSpawnLocation()));
        		event.setCancelled(true);
        	}
        } else if (split[0].equals("/setworldtime")) {
        	World world = MineQuest.getSServer().getWorld(split[1]);
        	world.setTime(Long.parseLong(split[2]));
        	event.setCancelled(true);
        } else if (split[0].equals("/debug")) {
        	MineQuest.questerHandler.getQuester(player).debug();
        	event.setCancelled(true);
        } else if (split[0].equals("/nomobs")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /nomobs <world_name>");
            	event.setCancelled(true);
            	return;
        	}
        	World world = MineQuest.getSServer().getWorld(split[1]);
        	
        	if (world != null) {
        		MineQuest.mobHandler.noMobs(world);
        		player.sendMessage("No mobs activated for world: " + world.getName());
        	} else {
        		player.sendMessage(split[1] + " is not a valid world");
        	}
        	
        	event.setCancelled(true);
        } else if (split[0].equals("/mobs")) {
        	if (split.length < 2) {
        		player.sendMessage("Usage: /mobs <world_name>");
            	event.setCancelled(true);
            	return;
        	}
        	World world = MineQuest.getSServer().getWorld(split[1]);
        	
        	if (world != null) {
        		MineQuest.mobHandler.yesMobs(world);
        		player.sendMessage("Yes mobs activated for world: " + world.getName());
        	} else {
        		player.sendMessage(split[1] + " is not a valid world");
        	}
        	
        	event.setCancelled(true);
        } else if (split[0].equals("/mobss")) {
        	if (player.getWorld().getLivingEntities() == null) {
        		player.sendMessage("No Living Entities List");
        	} else {
        		int count = 0;
        		int total = 0;
        		for (World world : MineQuest.getSServer().getWorlds()) {
	        		for (LivingEntity entity : world.getLivingEntities()) {
	        			if (entity instanceof Monster) {
	        				count++;
	        			}
	        		}
	        		total += world.getLivingEntities().size();
        		}
        		player.sendMessage("There are " + MineQuest.mobHandler.getMobSize() + " " + count + " " + total);
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/recalculatehealth")) {
        	if (player.isOp()) {
        		player.sendMessage("Recalculating all Health");
        		for (Quester quester : MineQuest.questerHandler.getQuesters()) {
        			player.sendMessage(quester.getName() + " - " + quester.recalculateHealth());
        			quester.save();
        		}
        		player.sendMessage("Recalculated!");
        	} else {
        		player.sendMessage("Only an op can do that");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/recalculatemana")) {
        	if (player.isOp()) {
        		player.sendMessage("Recalculating all Mana");
        		for (Quester quester : MineQuest.questerHandler.getQuesters()) {
        			player.sendMessage(quester.getName() + " - " + quester.recalculateMana());
        			quester.save();
        		}
        		player.sendMessage("Recalculated!");
        	} else {
        		player.sendMessage("Only an op can do that");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/howmany")) {
        	player.sendMessage("There are " + MineQuest.getSServer().getOnlinePlayers().length + " online players");
        	event.setCancelled(true);
        } else if (split[0].equals("/reloadconfig")) {
        	if (player.isOp()) {
        		player.sendMessage("Reloading...");
        		MineQuest.config.reloadConfig();
        		player.sendMessage("MineQuest Configuration Reloaded");
        	} else {
        		player.sendMessage("Only OPs are allowed to reload config");
        	}
        	event.setCancelled(true);
        } else if (split[0].equals("/setmana")) {
        	int mana  = Integer.parseInt(split[1]);
        	int max = Integer.parseInt(split[2]);
        	player.sendMessage("MQ:Mana-" + mana + "/" + max);
        	event.setCancelled(true);
        } else if (split[0].equals("/spawnnpc")) {
        	event.setCancelled(true);
        	if (split.length < 2) {
        		player.sendMessage("Usage: /spawn_npc <npc_name>");
        		return;
        	}
        	if (MineQuest.questerHandler.getQuester(player).canEdit(player.getLocation().getBlock())) {
            	Location location = player.getLocation();
            	MineQuest.questerHandler.addQuester(new NPCQuester(split[1], NPCMode.GENERIC, player.getWorld(), location));
        	} else {
        		player.sendMessage("You don't have permission to edit this area");
        	}
        } else if (split[0].equals("/spawnnpcv")) {
        	event.setCancelled(true);
        	if (split.length < 2) {
        		player.sendMessage("Usage: /spawn_npcv <npc_name>");
        		return;
        	}
        	if (MineQuest.questerHandler.getQuester(player).canEdit(player.getLocation().getBlock())) {
            	Location location = player.getLocation();
            	MineQuest.questerHandler.addQuester(new NPCQuester(split[1], NPCMode.VULNERABLE, player.getWorld(), location));
        	} else {
        		player.sendMessage("You don't have permission to edit this area");
        	}
        } else if (split[0].equals("/removenpc")) {
        	event.setCancelled(true);
        	if (split.length < 2) {
        		player.sendMessage("Usage: /remove_npc <npc_name>");
        		return;
        	}
        	if (MineQuest.questerHandler.getQuester(split[1]) instanceof NPCQuester) {
        		NPCQuester quester = (NPCQuester)MineQuest.questerHandler.getQuester(split[1]);
	        	if (MineQuest.questerHandler.getQuester(player).canEdit(quester.getPlayer().getLocation().getBlock())) {
	        		quester.remNPC();
	        	} else {
	        		player.sendMessage("You don't have permission to edit their area");
	        	}
        	} else {
        		player.sendMessage(split[1] + " is not a valid NPC to remove");
        	}
        } else if (split[0].equals("/getevents")) {
        	player.sendMessage("I think there are " + NormalEvent.count + " Events");
        	event.setCancelled(true);
        } else if (split[0].equals("/printmobs")) {
        	MineQuest.mobHandler.printMobs();
        	event.setCancelled(true);
        }
	}
}
