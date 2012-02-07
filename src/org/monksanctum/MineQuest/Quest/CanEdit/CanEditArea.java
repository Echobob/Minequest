package org.monksanctum.MineQuest.Quest.CanEdit;

import java.util.Calendar;

import org.bukkit.Location;
import org.monksanctum.MineQuest.Quester.Quester;

public class CanEditArea extends CanEdit {
	protected int id;
	protected boolean active;
	protected int index;
	protected Quester quester;
	protected int x;
	protected int y;
	protected int z;
	protected int max_x;
	protected int max_y;
	protected int max_z;
	private long last;
	
	public CanEditArea(int index, int x, int y, int z, int max_x, int max_y, int max_z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.max_x = max_x;
		this.max_y = max_y;
		this.max_z = max_z;
		this.index = index;
		active = false;
		quester = null;
	}

	@Override
	public boolean canEdit(Quester quester, Location loc) {
		if (within(loc)) {
			this.quester = quester;
			Calendar now = Calendar.getInstance();
			if (now.getTimeInMillis() - last > 100) {
				this.active = !active;
			}
			last = now.getTimeInMillis();
			return true;
		}
		
		return false;
	}

	public boolean within(Location loc) {
		if (loc.getX() < x) {
			return false;
		}
		if (loc.getX() > max_x) {
			return false;
		}
		if (loc.getY() < y) {
			return false;
		}
		if (loc.getY() > max_y) {
			return false;
		}
		if (loc.getZ() < z) {
			return false;
		}
		if (loc.getZ() > max_z) {
			return false;
		}
		return true;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getQuestIndex() {
		return index;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public Quester getTarget() {
		return quester;
	}

}
