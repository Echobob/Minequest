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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Quester.Quester;

public class AbilityBinder extends Ability {

	private String ability;
	private int bind_to;

	public AbilityBinder(String ability, int bind) {
		this.ability = ability;
		this.bind_to = bind;
	}
	
	public AbilityBinder() {
		// Shell
	}
	
	@Override
	public void bind(Quester quester, ItemStack item) {
		if (bind != item.getTypeId()) {
			bind = item.getTypeId();
			MineQuest.getSQLServer().update("INSERT INTO binds (name, abil, bind, bind_2) VALUES('" + quester.getSName() + "', '" + getName() + "', '" + bind + "', '" + bind_to + "')");
			quester.sendMessage(getName() + " is now bound to " + item.getTypeId());
		}
	}
	
	@Override
	public String getName() {
		return "Binder:" + ability;
	}
	
	@Override
	public void unBind(Quester quester) {
		super.unBind(quester);
		myclass.remAbility(this);
		quester.sendMessage(getName() + " removed");
	}
	
	@Override
	public void useAbility(Quester quester, Location location,
			LivingEntity entity) {
		quester.bind(ability, new ItemStack(bind_to, 1));
	}
	
	@Override
	public void silentBind(Quester quester, ItemStack itemStack) {
		bind = itemStack.getTypeId();
	}

	@Override
	public void castAbility(Quester quester, Location location,
			LivingEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ItemStack> getSpellComps() {
		return new ArrayList<ItemStack>();
	}

	@Override
	public int getReqLevel() {
		return 0;
	}

	@Override
	public String getSkillClass() {
		return null;
	}

	@Override
	public int getIconLoc() {
		return 255;
	}
}
