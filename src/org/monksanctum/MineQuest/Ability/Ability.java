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

import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftAnimals;
import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.craftbukkit.entity.CraftSkeleton;
import org.bukkit.craftbukkit.entity.CraftSpider;
import org.bukkit.craftbukkit.entity.CraftZombie;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;
import org.monksanctum.MineQuest.Quester.SkillClass.SkillClass;

/**
 * This is the base class for all abilities in MineQuest.
 * 
 * @author jmonk
 *
 */
public abstract class Ability {
	@SuppressWarnings({ "rawtypes" })
	private static List<Class> abil_classes;
// http://www.devx.com/tips/Tip/38975
	public static Class<?> getClass(String the_class) throws Exception {
		try {
			URL url = new URL("file:MineQuest/abilities.jar");
			URLClassLoader ucl = new URLClassLoader(new URL[] {url}, (new AbilityBinder()).getClass().getClassLoader());
			return Class.forName(the_class.replaceAll(".class", ""), true, ucl);
		} catch (Exception e) {
			URL url = new URL("file:abilities.jar");
			URLClassLoader ucl = new URLClassLoader(new URL[] {url}, (new AbilityBinder()).getClass().getClassLoader());
			return Class.forName(the_class.replaceAll(".class", ""), true, ucl);
		}
	}

	//following code came from http://snippets.dzone.com/posts/show/4831
	public static List<String> getClasseNamesInPackage(String jarName,
			String packageName) {
		boolean debug = false;
		ArrayList<String> classes = new ArrayList<String>();

		packageName = packageName.replaceAll("\\.", "/");
		if (debug)
			MineQuest.log("Jar " + jarName + " looking for " + packageName);
		try {
			JarInputStream jarFile = new JarInputStream(new FileInputStream(
					jarName));
			JarEntry jarEntry;

			while (true) {
				jarEntry = jarFile.getNextJarEntry();
				if (jarEntry == null) {
					break;
				}
				if ((jarEntry.getName().startsWith(packageName))
						&& (!jarEntry.getName().contains("Version"))
						&& (jarEntry.getName().endsWith(".class"))) {
					if (debug)
						MineQuest.log("Found "
								+ jarEntry.getName().replaceAll("/", "\\."));
					classes.add((String)jarEntry.getName().replaceAll("/", "\\."));
				}
			}
		} catch (Exception e) {
			MineQuest.log("Couldn't get Ability Classes - Missing abilities.jar?");
		}
		return classes;
	}
	
	/**
	 * Gets the entities within a area of a player. name
	 * 
	 * Not Implemented in bukkit yet!
	 * 
	 * @param player
	 * @param radius
	 * @return List of Entities within the area
	 */
	public static List<LivingEntity> getEntities(LivingEntity entity, int radius) {
		List<LivingEntity> entities = new ArrayList<LivingEntity>(0);
		List<LivingEntity> serverList = entity.getWorld().getLivingEntities();
		int i;
		
		for (i = 0; i < serverList.size(); i++) {
			if ((MineQuest.distance(entity.getLocation(), serverList.get(i).getLocation()) <= radius) 
				&& (serverList.get(i).getEntityId() != entity.getEntityId())) {
				entities.add(serverList.get(i));
			}
		}
		
		return entities;
	}
	
	/**
	 * Gets the entities within a area of a player. name
	 * 
	 * Not Implemented in bukkit yet!
	 * 
	 * @param player
	 * @param radius
	 * @return List of Entities within the area
	 */
	public static List<LivingEntity> getEntities(Location location, int radius) {
		List<LivingEntity> entities = new ArrayList<LivingEntity>(0);
		List<LivingEntity> serverList = location.getWorld().getLivingEntities();
		int i;
		
		for (i = 0; i < serverList.size(); i++) {
			if ((MineQuest.distance(location, serverList.get(i).getLocation()) <= radius)) {
				entities.add(serverList.get(i));
			}
		}
		
		return entities;
	}
	
	/**
	 * Returns the nearest empty block to location. Location is not
	 * Guaranteed to be closest to location. If the block is solid
	 * it looks upward until it finds air. If the block is air it
	 * looks downward until it finds ground.
	 * 
	 * @param x X Location
	 * @param y Y Location
	 * @param z Z Location
	 * @return Nearest empty height above ground to Location
	 */
	static public int getNearestY(World world, int x, int y, int z) {
		int i = y;
		
		if (world.getBlockAt(x, y, z).getTypeId() != 0) {
			do {
				i++;
			} while (((world.getBlockAt(x, i, z).getType() != Material.SNOW) 
					&& (world.getBlockAt(x, i, z).getType() != Material.FIRE) 
					&& (world.getBlockAt(x, i, z).getType() != Material.TORCH) 
					&& (world.getBlockAt(x, i, z).getType() != Material.SIGN) 
					&& (world.getBlockAt(x, i, z).getType() != Material.WALL_SIGN) 
					&& (world.getBlockAt(x, i, z).getType() != Material.AIR)) && (i < 1000));
			if (i == 1000) i = 0;
		} else {
			do {
				i--;
			} while (((world.getBlockAt(x, i, z).getType() == Material.SNOW) 
					|| (world.getBlockAt(x, i, z).getType() == Material.FIRE) 
					|| (world.getBlockAt(x, i, z).getType() == Material.TORCH) 
					|| (world.getBlockAt(x, i, z).getType() == Material.SIGN) 
					|| (world.getBlockAt(x, i, z).getType() == Material.WALL_SIGN) 
					|| (world.getBlockAt(x, i, z).getType() == Material.AIR)) && (i > -100));
			if (i == -100) i = 0;
			i++;
		}
		
		return i;
	}
	
	public static int getVersion() {
		try {
			@SuppressWarnings("rawtypes")
			Class this_class;
			try {
				URL url = new URL("file:MineQuest/abilities.jar");
				URLClassLoader ucl = new URLClassLoader(new URL[] {url}, (new AbilityBinder()).getClass().getClassLoader());
				this_class = Class.forName("org.monk.MineQuest.Ability.Version.Version", true, ucl);
			} catch (Exception e) {
				URL url = new URL("file:abilities.jar");
				URLClassLoader ucl = new URLClassLoader(new URL[] {url}, (new AbilityBinder()).getClass().getClassLoader());
				this_class = Class.forName("org.monk.MineQuest.Ability.Version.Version", true, ucl);
			}
			MineQuestVersion version = (MineQuestVersion)this_class.newInstance();
			
			return version.getVersion();
		} catch (Exception e) {
			return -1;
		}
	}
	
	@SuppressWarnings("rawtypes")
	static public List<Ability> newAbilities(SkillClass myclass) {
		List<Ability> abilities = new ArrayList<Ability>();
		if (abil_classes == null) {
			List<String> classes = new ArrayList<String>();
			
			abil_classes = new ArrayList<Class>();
			try {
				try {
					classes = getClasseNamesInPackage("MineQuest/abilities.jar", "org.monk.MineQuest.Ability");
				} catch (Exception e) {
					classes = getClasseNamesInPackage("abilities.jar", "org.monk.MineQuest.Ability");
					MineQuest.log("Please move abilities.jar to MineQuest/abilities.jar");
				}
			} catch (Exception e) {
				MineQuest.log("Unable to get Abilities");
			}
			for (String this_class : classes) {
				try {
					abil_classes.add(getClass(this_class));
				} catch (Exception e) {
					MineQuest.log("Could not load Ability: " + this_class);
				}
			}
		}
		
		String type = null;
		if (myclass != null) {
			type = myclass.getType();
		}
		for (Class abil : abil_classes) {
			try {
				Ability ability = (Ability) abil.newInstance();
				ability.setSkillClass(myclass);
				if ((myclass == null) || (type.equals(MineQuest.getAbilityConfiguration().getSkillClass(ability.getName())))) {
					for (@SuppressWarnings("unused") Class ability_class : abil_classes) {
						abilities.add(ability);
					}
				}
			} catch (Exception e) {
				MineQuest.log("Could not load Ability: " + myclass.getType());
				e.printStackTrace();
			}
		}

		return abilities;
	}
	
	/**
	 * Creates an instance of the proper ability based on name
	 * and returns it as an Ability.
	 * 
	 * @param name Name of Ability
	 * @param myclass Instance of SkillClass holding the Ability
	 * @return new Ability created
	 */
	static public Ability newAbility(String name, SkillClass myclass) {
		for (Ability ability : newAbilities(myclass)) {
			if (name.equalsIgnoreCase(ability.getName())) {
				return ability;
			}
		}
		MineQuest.log("Warning: Could not find ability " + name + " for class " + myclass.getType());
		
		return null;
	}
	
	protected int bind;
	//	private static List<Class> abil_classes;
	protected int config[];
	private List<ItemStack> cost;
	protected int count;
	protected boolean enabled;
	protected long last_msg;
	private int lookBind;
	protected SkillClass myclass;
	
	protected long time;
	
	/**
	 * Creates an Ability
	 * 
	 * @param name Name of Ability
	 * @param myclass SkillClass that holds Ability
	 */
	public Ability() {
		Calendar now = Calendar.getInstance();
		enabled = true;
		if (this instanceof PassiveAbility) enabled = false;
		count = 0;
		bind = -1;
		lookBind = -1;
		time = now.getTimeInMillis();
		last_msg = 0;
		config = null;
	}
	
	/**
	 * Bind to left click of item.
	 * 
	 * @param player Player binding Ability
	 * @param item Item to be bound
	 */
	public void bind(Quester quester, ItemStack item) {
		if (bind != item.getTypeId()) {
			silentUnBind(quester);
			bind = item.getTypeId();
			MineQuest.getSQLServer().update(
					"INSERT INTO binds (name, abil, bind, bind_2) VALUES('"
							+ quester.getSName() + "', '" + getName() + "', '"
							+ bind + "', '" + bind + "')");
			quester.sendMessage(getName() + " is now bound to "
					+ item.getTypeId());
			if (quester.isModded()) {
				quester.sendMessage(toBindString());
			}
		}
	}
	
	/**
	 * Checks if the Ability has been cast too recently based on
	 * casting time.
	 * 
	 * @return Boolean true if can cast now
	 */
	protected boolean canCast() {
		Calendar now = Calendar.getInstance();
		if ((now.getTimeInMillis() - time) > getRealCastingTime()) {
			time = now.getTimeInMillis();
			return true;
		}
		return false;
	}
	
	/**
	 * This is called when non-passive abilities are activated.
	 * Must be overloaded for all non-passive abilities. Binding
	 * and casting cost have already been checked.
	 * 
	 * @param quester Caster
	 * @param location Location of Target
	 * @param entity Target
	 */
	public abstract void castAbility(Quester quester, Location location,
			LivingEntity entity);
	
	/**
	 * Disable the ability
	 */
	public void disable() {
		enabled = false;
	}
	
	/**
	 * Enable the ability.
	 * 
	 * @param quester Quester enabling the ability
	 */
	public void enable(Quester quester) {
		if (quester.canCast(getConfigSpellComps(), getRealManaCost())) {
			enabled = true;
			quester.sendMessage(getName() + " enabled");
//			MineQuest.log("Can cast " + getName() + " with config :");
//			for (int i : config) {
//				MineQuest.log("        " + i);
//			}
		} else {
			notify(quester, "You do not have the materials to enable that - try /spellcomp " + getName());
		}
	}
	
	public boolean equals(String name) {
		return name.equals(getName());
	}
	
	public void eventActivate() {
		
	}
	
	/**
	 * Get the casting time of the spell that restricts
	 * how often it can be cast.
	 * @return
	 */
	public int getCastTime() {
		return 0;
	}
	
	public int[] getConfig() {
		return config;
	}

	public List<ItemStack> getConfigSpellComps() {
		return cost;
	}
	
	/**
	 * Gets the distance between a Player and the entity.
	 * 
	 * @param player
	 * @param entity
	 * @return distance between player and entity
	 */
	protected int getDistance(Player player, LivingEntity entity) {
		return (int)MineQuest.distance(player.getLocation(), entity.getLocation());
	}
	
	/**
	 * Get the experience gained from using this ability.
	 * 
	 * @return
	 */
	public int getExp() {
		return 30;
	}
	
	public int getMana() {
		return 1 + getReqLevel();
	}
	
	/**
	 * Get the name of the Ability
	 * 
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Gets a random entity within the radius of the entity
	 * 
	 * @param entity
	 * @param radius
	 * @return
	 */
	protected LivingEntity getRandomEntity(LivingEntity entity, int radius) {
		List<LivingEntity> entities = getEntities(entity, radius);
		int i = myclass.getGenerator().nextInt(entities.size());
		
		return entities.get(i);
	}
	
	public int getRealCastingTime() {
		return MineQuest.getAbilityConfiguration().getCastingTime(getName());
	}
	
	public int getRealExperience() {
		return MineQuest.getAbilityConfiguration().getExperience(getName());
	}
	
	public int getRealManaCost() {
		return MineQuest.getAbilityConfiguration().getMana(getName());
	}
	
	public int getRealRequiredLevel() {
		return MineQuest.getAbilityConfiguration().getRequiredLevel(getName());
	}
	
	public List<ItemStack> getRealSpellComps() {
		List<ItemStack> cost = getSpellComps();

		int i;
		for (i = 0; i < (getReqLevel() / 4); i++) {
			cost.add(new ItemStack(Material.REDSTONE, 1));
		}

		return cost;
	}
	
	public String getRealSpellCompsString() {
		List<Integer> cost = new ArrayList<Integer>();
		String ret = "";
		int i;
		
		for (ItemStack item : getRealSpellComps()) {
			for (i = 0; i < item.getAmount(); i++) {
				cost.add(item.getTypeId());
			}
		}
		if (cost.size() > 0) {
			ret = ret + cost.get(0);
			for (i = 1; i < cost.size(); i++) {
				ret = ret + "," + cost.get(i);
			}
		}
		
		return ret;
	}
	
	public abstract int getReqLevel();
	
	public abstract String getSkillClass();
	
	/**
	 * Get the spell components of casting the ability.
	 * Must be overloaded by all abilities that have 
	 * components.
	 * 
	 * @return
	 */
	public abstract List<ItemStack> getSpellComps();
	
	/**
	 * Gives the casting cost back to the player.
	 * 
	 * @param player
	 */
	protected void giveCost(Player player) {
		giveCostNoExp(player);
		
		myclass.expAdd(-getRealExperience());
	}

	/**
	 * Gives the casting cost back to the player.
	 * 
	 * @param player
	 */
	protected void giveCostNoExp(Player player) {
		if (MineQuest.config.spell_comp) {
			giveSpellComps(player);
		}
		if (MineQuest.config.mana) {
			giveManaCost(player);
		}
	}

	private void giveManaCost(Player player) {
		MineQuest.questerHandler.getQuester(player).addMana(getRealManaCost());
	}
	
	@SuppressWarnings("deprecation")
	protected void giveSpellComps(Player player) {
		List<ItemStack> cost = getConfigSpellComps();
		int i;
		
		for (i = 0; i < cost.size(); i++) {
			player.getInventory().addItem(cost.get(i));
		}
		player.updateInventory();
	}
	
	public boolean isActive() {
		return enabled;
	}
	
	/**
	 * Checks if the itemStack is bound to this ability.
	 * 
	 * @param itemStack
	 * @return true if bound
	 */
	public boolean isBound(ItemStack itemStack) {
		return (bind == itemStack.getTypeId());
	}
	
	/**
	 * Checks if ability is enabled.
	 * 
	 * @return true if enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	public boolean isLookBound(ItemStack itemStack) {
		return (lookBind == itemStack.getTypeId());
	}
	
	/**
	 * This was used to determine if entities are part of type
	 * for purge spells.
	 * 
	 * @param livingEntity
	 * @param type
	 * @return
	 */
	private boolean isType(LivingEntity livingEntity, PurgeType type) {
		switch (type) {
		case ZOMBIE:
			return livingEntity instanceof CraftZombie;
		case SPIDER:
			return livingEntity instanceof CraftSpider;
		case SKELETON:
			return livingEntity instanceof CraftSkeleton;
		case CREEPER:
			return livingEntity instanceof CraftCreeper;
		case ANIMAL:
			return livingEntity instanceof CraftAnimals;
		default:
			return true;
		}
	}

	/**
	 * Determines if player and baseEntity are within radius distance
	 * of each other.
	 * 
	 * @param player
	 * @param baseEntity
	 * @param radius
	 * @return true if within radius
	 */
	protected boolean isWithin(LivingEntity player, LivingEntity baseEntity, int radius) {
		return MineQuest.distance(player.getLocation(), baseEntity.getLocation()) < radius;
	}
	
	/**
	 * Bind to left click of item.
	 * 
	 * @param player Player binding Ability
	 * @param item Item to be bound
	 */
	public void lookBind(Quester quester, ItemStack item) {
		if (lookBind != item.getTypeId()) {
			silentUnBind(quester);
			lookBind = item.getTypeId();
			MineQuest.getSQLServer().update(
					"INSERT INTO binds (name, abil, bind, bind_2) VALUES('"
							+ quester.getSName() + "', 'LOOK:" + getName()
							+ "', '" + lookBind + "', '" + lookBind + "')");
			quester.sendMessage(getName() + " is now look bound to "
					+ item.getTypeId());
			if (quester.isModded()) {
				quester.sendMessage(toBindString());
			}
		}
	}

	/**
	 * Moves the entity other so that it is not within the distance
	 * specified of the player. It keeps the direction of location
	 * other with respect to player.
	 * 
	 * @param player
	 * @param other
	 * @param distance
	 */
	private void moveOut(LivingEntity player, LivingEntity other,
			int distance) {
		double x, z;
		double unit_fix;
		
		x = other.getLocation().getX() - player.getLocation().getX();
		z = other.getLocation().getZ() - player.getLocation().getZ();
		unit_fix = Math.sqrt(x*x + z*z);
		
		x *= distance / unit_fix;
		z *= distance / unit_fix;
		
		other.teleport(new Location(other.getWorld(), x + player.getLocation().getX(), 
				(double)getNearestY(player.getWorld(), (int)(x + player.getLocation().getX()), 
				(int)other.getLocation().getY(), (int)(z + player.getLocation().getZ())), 
				z + player.getLocation().getZ()));
		
		return;
	}

	public void notify(Quester quester, String message) {
		Calendar now = Calendar.getInstance();
		
		if ((now.getTimeInMillis() - last_msg) > 2000) {
			last_msg = now.getTimeInMillis();
			quester.sendMessage(message);
		}		
	}

	/**
	 * Parse any affects of the ability being activated as part
	 * of an attack motion.
	 * 
	 * @param quester
	 * @param defend
	 * @return true if attack damage should be negated
	 */
	public boolean parseAttack(Quester quester, LivingEntity defend) {
		useAbility(quester, defend.getLocation(), defend);
		
		if ((getName().equals("Fire Arrow") || getName().equals("PowerStrike"))) {
			return false;
		}
		
		return true;
	}

	/**
	 * Parse any affects of the ability being activated
	 * as part of a left click.
	 * 
	 * @param quester
	 * @param block
	 */
	public void parseClick(Quester quester, Block block) {
		useAbility(quester, block.getLocation(), null);
	}

	/**
	 * Moves all entities of given type outside of the distance specified
	 * from the entity passed. 
	 * 
	 * @param player
	 * @param distance
	 * @param type
	 */
	protected void purgeEntities(LivingEntity player, int distance, PurgeType type) {
		List<LivingEntity> entities = getEntities(player, distance);
		
		int i;
		
		for (i = 0; i < entities.size(); i++) {
			if (isType(entities.get(i), type)) {
				moveOut(player, entities.get(i), distance);
				MineQuest.damage(entities.get(i), 1, MineQuest.questerHandler.getQuester(player));
			}
		}
		
	}
	
	public void setActive(boolean active) {
		this.enabled = active;
	}

	protected void setConfig(int[] config) {
		this.config = config;
	}

	public void setConfigSpellComps(List<ItemStack> cost) {
		this.cost = cost;
	}

	public void setSkillClass(SkillClass skillclass) {
		this.myclass = skillclass;
		if (skillclass != null) {
			cost = MineQuest.getAbilityConfiguration().getCost(getName());
			config = MineQuest.getAbilityConfiguration().getConfig(getName());
		}
	}
	
	public void silentBind(Quester quester, ItemStack itemStack) {
		bind = itemStack.getTypeId();
		lookBind = -1;
	}

	public void silentLookBind(Quester quester, ItemStack itemStack) {
		lookBind = itemStack.getTypeId();
		bind = -1;
	}

	/**
	 * Clears bindings for this ability.
	 * @param player
	 */
	public void silentUnBind(Quester quester) {
		bind = -1;
		lookBind = -1;
		MineQuest.getSQLServer().update("DELETE FROM binds WHERE abil='" + getName() + "' AND name='" + quester.getSName() + "'");
		MineQuest.getSQLServer().update("DELETE FROM binds WHERE abil='LOOK:" + getName() + "' AND name='" + quester.getSName() + "'");
	}

	@Override
	public String toString() {
		String spellComps = new String();
		List<ItemStack> reduced = MineQuest.reduce(getConfigSpellComps());
		
		if (reduced.size() > 0) {
			spellComps = reduced.get(0).getTypeId() + "-" + reduced.get(0).getAmount();
			int i;
			
			for (i = 1; i < reduced.size(); i++) {
				spellComps = spellComps + "," + 
								reduced.get(i).getTypeId() + "-" + 
								reduced.get(i).getAmount();
			}
		}
		
		if (spellComps.length() > 0) {
			return getName() + ":" + spellComps + ":" + getRealManaCost();
		} else {
			return getName() + ":" + getRealManaCost();
		}
	}
	
	public String toBindString() {
		return "MQ:Bind:" + getName() + ":" + bind + ":" + lookBind + ":" + MineQuest.getAbilityConfiguration().getIconLocation(getName());
	}
	
	/**
	 * Clears bindings for this ability.
	 * @param player
	 */
	public void unBind(Quester quester) {
		bind = -1;
		lookBind = -1;
		MineQuest.getSQLServer().update("DELETE FROM binds WHERE abil='" + getName() + "' AND name='" + quester.getSName() + "'");
		MineQuest.getSQLServer().update("DELETE FROM binds WHERE abil='LOOK:" + getName() + "' AND name='" + quester.getSName() + "'");
		quester.sendMessage(getName() + " is now unbound");
		if (quester.isModded()) {
			quester.sendMessage(toBindString());
		}
	}

	/**
	 * This activates non-passive abilities. First makes sure that
	 * the ability is enabled, can be cast, and is bound. Then will
	 * call castAbility.
	 * 
	 * @param quester Caster
	 * @param location Location of Target
	 * @param l 1 for left click, 0 for right click
	 * @param entity Target
	 */
	public void useAbility(Quester quester, Location location, LivingEntity entity) {
		Player player = quester.getPlayer();
		
		if (this instanceof PassiveAbility) {
			notify(quester, getName() + " is a passive ability");
			return;
		}
		
		if (!enabled) {
			notify(quester, getName() + " is not enabled");
			return;
		}
		
		if ((quester == null) || quester.canCast(getConfigSpellComps(), getRealManaCost())) {
			if (canCast() || (player == null)) {
//				MineQuest.log("Can cast " + getName() + " with config :");
//				for (int i : config) {
//					MineQuest.log("        " + i);
//				}
				notify(quester, "Casting " + getName());
				castAbility(quester, location, entity);
				if (myclass != null) {
					myclass.expAdd(getRealExperience());
				}
			} else {
				if (player != null) {
					giveCostNoExp(player);
					notify(quester, "You cast that too recently");
				}
			}
		} else {
			if (player != null) {
				notify(quester, "You do not have the materials to cast that - try /spellcomp " + getName());
			}
		}
	}

	public abstract int getIconLoc();
}
