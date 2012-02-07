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
package org.monksanctum.MineQuest.Quester.SkillClass;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Ability.Ability;
import org.monksanctum.MineQuest.Ability.DefendingAbility;
import org.monksanctum.MineQuest.Ability.TargetDefendAbility;
import org.monksanctum.MineQuest.Configuration.SkillClassConfig;
import org.monksanctum.MineQuest.Quester.Quester;

/**
 * Holds information referring to a Questers specific class
 * such as Warrior. Handles the list of abilities associated
 * with the class and any damage/experience required.
 * 
 * @author jmonk
 */
public class SkillClass {
	public static SkillClass newClass(Quester quester, String type) {
		
		for (String name : MineQuest.getCombatConfig().getClassNames()) {
			if (name.equalsIgnoreCase(type)) {
				return new CombatClass(quester, name);
			}
		}
		
		for (String name : MineQuest.getResourceConfig().getClassNames()) {
			if (name.equalsIgnoreCase(type)) {
				return new ResourceClass(quester, name);
			}
		}
		
		MineQuest.log("Warning: SkillClass " + type + " could not be found");
		
		return new SkillClass(quester, type);
	}

	public static SkillClass newShell(String type) {
		
		for (String name : MineQuest.getCombatConfig().getClassNames()) {
			if (name.equalsIgnoreCase(type)) {
				return new CombatClass(name);
			}
		}
		
		for (String name : MineQuest.getResourceConfig().getClassNames()) {
			if (name.equalsIgnoreCase(type)) {
				return new ResourceClass(name);
			}
		}
		
		MineQuest.log("Warning: SkillClass " + type + " could not be found");
		
		return new SkillClass(type);
	}
	
	private int abil_list_id;
	protected Ability ability_list[];
	private int exp;
	protected Random generator;
	protected int level;
	protected Quester quester;
	
	protected String type;
	
	public SkillClass(String type) {
		//Shell
		this.type = type;
		generator = new Random();
		level = MineQuest.config.adjustment_multiplier * MineQuest.questerHandler.getAdjustment() * 10;
	}
	
	/**
	 * Load Class information from MineQuest DB.
	 * 
	 * @param quester Quester that has the class
	 * @param type Name of the class
	 */
	public SkillClass(Quester quester, String type) {
		this.type = type;
		generator = new Random();
		this.quester = quester;
		if (quester != null) {
			update();
		} else {
			level = MineQuest.config.adjustment_multiplier * MineQuest.questerHandler.getAdjustment() * 10;
		}
	}
	
	/**
	 * Reload the ability list from the MySQL database.
	 * Used on initial creation of class and upon modification
	 * of the ability list.
	 * 
	 * @param abilId ID of abilities in table
	 * @return List of Abilities for the abilId
	 * @throws SQLException Upon failure to Query the Database.
	 */
	private Ability[] abilListSQL(int abilId) throws SQLException {
		Ability list[];
		int i;
		int count = 0;
		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM abilities WHERE abil_list_id='" + abilId + "'");
		
		results.next();
		
		for (i = 0; i < 10; i++) {
			if (!results.getString("abil" + i).equals("0")) {
				count++;
			}
		}
		list = new Ability[count];
		
		count = 0;
		for (i = 0; i < 10; i++) {
			if (!results.getString("abil" + i).equals("0")) {
				list[count] = Ability.newAbility(results.getString("abil" + i), this);
				if (list[count++] == null) {
					MineQuest.getSQLServer().aupdate(
							"UPDATE abilities SET abil" + i
									+ "='0' WHERE abil_list_id='" + abilId
									+ "'");
					return abilListSQL(abilId);
				}
			}
		}
		
		return list;
	}

	public void addAbility(Ability ability) {
		Ability[] new_abil_list = new Ability[ability_list.length + 1];
		int i;
		for (i = 0; i < ability_list.length; i++) {
			new_abil_list[i] = ability_list[i];
		}
		new_abil_list[i] = ability;
		
		ability_list = new_abil_list;
	}
	
	/**
	 * Adds an ability to the class in the DB. Then
	 * reloads the ability list for the class.
	 * 
	 * @param string Name of ability to be added.
	 */
	protected void addAbility(String string) {
		try {
			int i;
			ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM abilities WHERE abil_list_id='" + abil_list_id + "'");
			results.next();
			
			for (i = 0; i < 10; i++) {
				if (results.getString("abil" + i).equals("0")) {
					MineQuest.getSQLServer().aupdate("UPDATE abilities SET abil" + i + "='" + string
							+ "' WHERE abil_list_id='" + abil_list_id + "'");
					break;
				}
			}
			ability_list = abilListSQL(abil_list_id);
			quester.updateBinds();
		} catch (SQLException e) {
			MineQuest.log("Failed to add ability " + string + " to mysql server");
		}
		
		quester.sendMessage("Gained ability " + string);
		
		return;
	}

	public void blockBreak(BlockBreakEvent event) {
		for (Ability abil : ability_list) {
			if (abil.isBound(quester.getPlayer().getItemInHand())) {
				abil.parseClick(quester, event.getBlock());
				return;
			}
		}
	}


	/**
	 * Called any time a player is destroying a block of this
	 * classes type or if the block has no class type then when
	 * the player is using this classes weapon/tool. Handles
	 * parsing of activated abilities.
	 * 
	 * @param event.getBlock() Block being destroyed
	 */
	public void blockDamage(BlockDamageEvent event) {
		for (Ability abil : ability_list) {
			if (abil.isBound(quester.getPlayer().getItemInHand())) {
				abil.parseClick(quester, event.getBlock());
				return;
			}
		}
	}

	public void callAbility() {
		getAbility(quester.getPlayer().getItemInHand()).useAbility(quester, quester.getPlayer().getLocation(), null);
	}

	/**
	 * Will activate any abilities that are left bound on
	 * the quester's item in hand.
	 * 
	 * @param block Block selected for Casting
	 */
	public void callAbility(Block block) {
		getAbility(quester.getPlayer().getItemInHand()).useAbility(quester, block.getLocation(), null);
	}

	/**
	 * Will activate any abilities that are left bound on
	 * the quester's item in hand.
	 * 
	 * @param block Block selected for Casting
	 */
	public void callLookAbility(Block block) {
		List<LivingEntity> lentities = Ability.getEntities(block.getLocation(), 2);
		if (lentities.size() > 0) {
			getAbility(quester.getPlayer().getItemInHand()).useAbility(quester, block.getLocation(), lentities.get(0));
		} else {
			getAbility(quester.getPlayer().getItemInHand()).useAbility(quester, block.getLocation(), null);
		}
	}

	/**
	 * Will activate any abilities that are left bound on
	 * the quester's item in hand.
	 * 
	 * @param entity Entity selected for Casting
	 */
	public void callAbility(Entity entity) {
		if (entity instanceof LivingEntity) {
			getAbility(quester.getPlayer().getItemInHand()).useAbility(quester, entity.getLocation(), (LivingEntity)entity);
		}
	}
	
	/**
	 * Determines if the Quester is high enough level in
	 * this class to use a specific item. if the item
	 * does not relate to this class then it will return
	 * true.
	 * 
	 * @param itemStack
	 * @return Boolean False if Quester cannot use item. True otherwise
	 */
	public boolean canUse(ItemStack item) {
		int item_ids[];
		int levels[];
		int i;
		item_ids = getSkillConfig().getTypes(type);
		levels = getSkillConfig().getLevels(type);
		if (levels == null) return false;
		if (item_ids == null) return false;

		for (i = 0; i < item_ids.length; i++) {
			if ((item.getTypeId() == item_ids[i]) && (level >= levels[i])) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean canWear(ItemStack item) {
		int item_ids[];
		int levels[];
		int i;
		item_ids = getSkillConfig().getArmor(type);
		levels = getSkillConfig().getArmorLevels(type);
		if (levels == null) return false;
		if (item_ids == null) return false;

		for (i = 0; i < item_ids.length; i++) {
			if ((item.getTypeId() == item_ids[i]) && (level >= levels[i])) {
				return true;
			}
		}
		
		return false;
	}

	public SkillClassConfig getSkillConfig() {
		MineQuest.log("[WARNING] SkillClass with no type - " + type);
		return null;
	}

	/**
	 * Called when a Quester left clicks on something.
	 * 
	 * @param itemInHand Item that the quester clicked with
	 * @param block Block that was clicked
	 * @return True if left clicked parsed and is complete
	 */
	public boolean click(Block block, int itemInHand) {
		for (Ability ability : ability_list) {
			if (ability.isBound(quester.getPlayer().getItemInHand())) {
				ability.parseClick(quester, block);
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Called when a Quester left clicks on something.
	 * 
	 * @param itemInHand Item that the quester clicked with
	 * @return True if left clicked parsed and is complete
	 */
	public boolean click(ItemStack itemInHand) {
		for (Ability ability : ability_list) {
			if (ability.isBound(quester.getPlayer().getItemInHand())) {
				ability.parseClick(quester, null);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Called whenever the Quester that has this class is
	 * being attacked.
	 * 
	 * @param entity Entity that is attacker
	 * @param amount Amount of damage being dealt
	 * @return The amount of damage negated by this class
	 */
	public int defend(LivingEntity entity, int amount, boolean flags[]) {
		int i;
		int armor[] = getSkillConfig().getArmorLevels(type);
		double armor_defends[] = getSkillConfig().getArmorDefends(type);
		int armor_blocks[] = getSkillConfig().getArmorBlocks(type);
		int sum = 0;
		
		if (armor != null) {
			for (i = 0; i < armor.length; i++) {
				if (isWearing(armor[i], flags)) {
					if (generator.nextDouble() < armor_defends[i]) {
						sum += armor_blocks[i];
					}
				}
			}
		}
		
		if (ability_list != null) {
			for (i = 0; i < ability_list.length; i++) {
				if (ability_list[i] instanceof DefendingAbility) {
					sum += ((DefendingAbility)ability_list[i]).parseDefend(quester, entity, amount - sum);
				}
			}
		}
		
		return sum;
	}
	
	/**
	 * Disables a specific Ability.
	 * 
	 * @param abil_name Name of ability to disable
	 */
	public void disableAbility(String abil_name) {
		Ability abil = getAbility(abil_name);
		if (abil != null) {
			abil.disable();
		}
	}

	/**
	 * Displays the status of this class
	 * 
	 */
	public void display() {
		int num = 400;
		quester.sendMessage(" " + type + ": " + level + " - " + exp + "/" + (num*(level+1)));
		
		return;
	}
	
	/**
	 * Enables a specific Ability.
	 * 
	 * @param abil_name Name of Ability to enable
	 */
	public void enableAbility(String abil_name) {
		Ability abil = getAbility(abil_name);
		if (abil != null) {
			if (!abil.isEnabled()) {
				abil.enable(quester);
			} else {
				quester.sendMessage(abil_name + " already enabled!");
			}
		}
	}
	
	/**
	 * Adds experience to the class and checks for a level
	 * up.
	 * 
	 * @param expNum Experience to be added
	 */
	public void expAdd(int expNum) {
		int num = 400;
		exp += expNum;
		if (exp >= num * (level + 1)) {
			levelUp();
		}
	}
	
	private void fillAbilities() {
		try {
			ability_list = abilListSQL(abil_list_id);
			
			List<Ability> available = Ability.newAbilities(this);
			
			for (Ability ability : available) {
				if (ability_list.length < 10) {
					if (getAbility(ability.getName()) == null) {
						if (level >= ability.getRealRequiredLevel()) {
							addAbility(ability.getName());
							ability_list = abilListSQL(abil_list_id);
						}
					}
				}
			}
			
		} catch (SQLException e) {
			
		}
		quester.updateBinds();
	}
	
	/**
	 * Gets an ability based on an item that it is
	 * bound to.
	 * 
	 * @param itemInHand Item that ability is bound to
	 * @return Ability that is bound
	 */
	public Ability getAbility(ItemStack itemInHand) {
		int i;
		
		for (i = 0; i < ability_list.length; i++) {
			if (ability_list[i] != null) {
				if (ability_list[i].isBound(itemInHand)) {
					return ability_list[i];
				}
			}
		}
		
		for (i = 0; i < ability_list.length; i++) {
			if (ability_list[i] != null) {
				if (ability_list[i].isLookBound(itemInHand)) {
					return ability_list[i];
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Gets an ability based on name.
	 * 
	 * @param name Name of Ability
	 * @return Ability requested
	 */
	public Ability getAbility(String name) {
		int i;
		
		for (i = 0; i < ability_list.length; i++) {
			if (ability_list[i] != null) {
				if (ability_list[i].getName().equalsIgnoreCase(name)) {
					return ability_list[i];
				}
			}
		}
		
		return null;
	}

	/**
	 * Gets the effective caster level for Mage classes.
	 * 
	 * @return Effective Caster Level
	 */
	public int getCasterLevel() {
		return (level + 1) / 2;
	}

	/**
	 * Gets a List of Armor IDs that are related
	 * to the current class.
	 * 
	 * @return List of Armor IDs
	 */
	public int[] getClassArmorIds() {
		return null;
	}

	/**
	 * Get the random number generator for this class.
	 * 
	 * @return Random number generator
	 */
	public Random getGenerator() {
		return generator;
	}

	/**
	 * Get the level of this class
	 * 
	 * @return Current Level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Get the name of the Quester that owns
	 * this class.
	 * 
	 * @return Name of Quester
	 */
	public String getName() {
		return quester.getPlayer().getName();
	}

	public Quester getQuester() {
		return quester;
	}

	/**
	 * Get the name of this class.
	 * 
	 * @return Name of Class
	 */
	public String getType() {
		return type;
	}

	/**
	 * Check if the given itemstack is bound to an 
	 * ability that this class is holding.
	 * 
	 * @param itemStack Itemstack to check
	 * @return Boolean True if bound to an Ability.
	 */
	public boolean isAbilityItem(ItemStack itemStack) {
		int i;
		
		for (i = 0; i < ability_list.length; i++) {
			if (ability_list[i] != null) {
				if (ability_list[i].isBound(itemStack)) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Check if the given itemstack is bound to an 
	 * ability that this class is holding.
	 * 
	 * @param itemStack Itemstack to check
	 * @return Boolean True if bound to an Ability.
	 */
	public boolean isLookAbilityItem(ItemStack itemStack) {
		int i;
		
		for (i = 0; i < ability_list.length; i++) {
			if (ability_list[i] != null) {
				if (ability_list[i].isLookBound(itemStack)) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Checks if a block type is a type that cannot be
	 * destroyed without better tools.
	 * 
	 * @param i Block Type
	 * @return True for gold, diamond, and redstone
	 */
	protected boolean isBlockGiveType(int i) {
		switch (i) {
		case 14: //gold
		case 56: //diamond
		case 73: //red stone
		case 74: //more red stone
			return true;
		default:		
			return false;

		}
	}

	/**
	 * Determines if an item is a tool specific to this
	 * class. 
	 * 
	 * @param item Item to Check
	 * @return True if item is a class item.
	 */
	public boolean isClassItem(ItemStack item) {
		if (item == null) return false;
		if (getSkillConfig().getTypes(type) == null) return false;
		for (int id : getSkillConfig().getTypes(type)) {
			if (id == item.getTypeId()) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Determines if an item is a tool specific to this
	 * class. 
	 * 
	 * @param type Item to Check
	 * @return True if item is a class item.
	 */
	protected boolean isClassItem(Material type) {
		return isClassItem(new ItemStack(type.getId()));
	}

	/**
	 * Determines if the Quester is wearing the item as
	 * a piece of armor.
	 * 
	 * @param i ItemId of Item to check
	 * @param flags 
	 * @return True if wearing the Item
	 */
	protected boolean isWearing(int i, boolean[] flags) {
		if (quester.getPlayer() == null) return false;

		PlayerInventory equip = quester.getPlayer().getInventory();

		for (ItemStack itemStack : equip.getArmorContents()) {
			if (itemStack.getTypeId() == i) {
				if (!flags[i]) {
					flags[i] = true;
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Called when a class gains enough experience to level up.
	 * Handles adding abilities and modification of health.
	 * 
	 * @param quester
	 */
	public void levelUp() {
		int num = 400;
		level++;
		exp -= num * (level);
		quester.sendMessage("Congratulations on becoming a level " + level + " " + type);
		
		fillAbilities();
		
		int add;
		int size;
		
		size = getSize();
		
		if (size > 0) {
			add = generator.nextInt(size) + 1;
			
			quester.addHealth(add);
		} else if (size == 0) {
			quester.addHealth(1);
		}
		
		size = getManaSize();
		
		if (size > 0) {
			add = generator.nextInt(size) + 1;
			
			quester.addTotalMana(add);
		} else if (size == 0) {
			quester.addTotalMana(1);
		}
	}

	public int getSize() {
		return getSkillConfig().getLevelHealth(type) - 1;
	}

	public int getManaSize() {
		return getSkillConfig().getLevelMana(type) - 1;
	}

	/**
	 * Called to display a list of abilities that this class
	 * holds to the player.
	 * 
	 */
	public void listAbil() {
		for (Ability ability : ability_list) {
			if (ability != null) {
				quester.sendMessage(ability.getName());
			}
		}
	}

	public void remAbility(Ability ability) {
		Ability[] new_list = new Ability[ability_list.length - 1];
		int i = 0;
		for (Ability abil : ability_list) {
			if (abil != null) {
				if (!abil.getName().equals(ability.getName())) {
					new_list[i++] = abil;
				}
			}
		}
		
		ability_list = new_list;
	}

	public void replaceAbil(String old, String new_abil) {
		int i;
		for (i = 0; i < ability_list.length; i++) {
			if (ability_list[i] != null) {
				if (ability_list[i].getName().equals(old)) {
					break;
				}
			}
		}
		
		Ability new_ability = Ability.newAbility(new_abil, this);
		
		if (new_ability == null) {
			quester.sendMessage(new_abil + " is not a valid ability");
			return;
		}
		
		if (level < new_ability.getRealRequiredLevel()) {
			quester.sendMessage("You are not high enough level to cast " + new_abil);
			return;
		}
		
		ability_list[i] = new_ability;
		MineQuest.getSQLServer().aupdate("UPDATE abilities SET abil" + i + "='" + new_ability.getName()
				+ "' WHERE abil_list_id='" + abil_list_id + "'");
		quester.sendMessage(old + " Ability has been replaced with " + new_ability.getName() + " Ability in your spellbook");
	}

	public void rightClick(Block block) {
		expAdd(5);
	}

	/**
	 * Save any changes in this class to the MySQL Database.
	 */
	public void save() {
		MineQuest.getSQLServer().update("UPDATE classes SET exp='" + exp + "', level='" + level + 
								"' WHERE name='" + quester.getSName() + "' AND class='" + type + "'");
	}

	public void silentUnBind(ItemStack itemStack) {
		for (Ability ability : ability_list) {
			if (ability != null) {
				if (ability.isBound(itemStack)) {
					ability.silentUnBind(quester);
				}
			}
		}
	}

	/***
	 * Unbind an item from any abilities it might be bound to.
	 * @param itemInHand 
	 */
	public void unBind(ItemStack itemInHand) {
		for (Ability ability : ability_list) {
			if (ability != null) {
				if (ability.isBound(itemInHand) || ability.isLookBound(itemInHand)) {
					ability.unBind(quester);
				}
			}
		}
	}

	/**
	 * Load class data from database overwriting any unsaved
	 * changes.
	 */
	public void update() {
		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM classes WHERE name='" + quester.getSName() + "' AND class='" + type + "'");
		
		try {
			if (results.next()) {
				exp = results.getInt("exp");
				level = results.getInt("level");
				abil_list_id = results.getInt("abil_list_id");
				
				if (MineQuest.isPermissionsEnabled() && (quester.getPlayer() != null)) {
					if (MineQuest.permission.playerHas(quester.getPlayer(), "MineQuest.Abilities")) {
						ability_list = abilListSQL(abil_list_id);
					} else {
						ability_list = new Ability[0];
					}
				} else if (!MineQuest.isPermissionsEnabled()) {
					ability_list = abilListSQL(abil_list_id);
				}
				quester.updateBinds();
			} else {
				abil_list_id = -1;
				ability_list = new Ability[0];
			}
		} catch (SQLException e) {
			MineQuest.log("Problem reading Ability");
		}
		
		if (MineQuest.isPermissionsEnabled() && (quester.getPlayer() != null)) {
			if (MineQuest.permission.playerHas(quester.getPlayer(), "MineQuest.Abilities")) {
				fillAbilities();
			}
		} else if (!MineQuest.isPermissionsEnabled()) {
			fillAbilities();
		}
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public void setQuester(Quester quester) {
		this.quester = quester;
	}

	public boolean isArmor(ItemStack boots) {
		if (getSkillConfig().getArmor(type) == null) return false;
		for (int type_id : getSkillConfig().getArmor(type)) {
			if (type_id == boots.getTypeId()) {
				return true;
			}
		}
		
		return false;
	}

	public boolean isClassBlock(Block block) {
		return false;
	}

	public void targeted(EntityTargetEvent event) {
		if (ability_list == null) return;
		for (Ability ability : ability_list) {
			if (ability instanceof TargetDefendAbility) {
				if (ability.isEnabled()) {
					((TargetDefendAbility)ability).targeted(event);
				}
			}
		}
	}

	public void updateAbilities() {
		for (Ability ability : ability_list) {
			if (ability != null) {
				quester.sendMessage("MQ:Ability:" + ability.toString());
				quester.sendMessage(ability.toBindString());
			}
		}
	}

}
