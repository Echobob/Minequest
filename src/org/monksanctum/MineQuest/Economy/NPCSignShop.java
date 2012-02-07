package org.monksanctum.MineQuest.Economy;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.monksanctum.MineQuest.MineQuest;
import org.monksanctum.MineQuest.Event.UpdateSignEvent;
import org.monksanctum.MineQuest.Quester.NPCQuester;
import org.monksanctum.MineQuest.Quester.Quester;

public class NPCSignShop extends Store {
	private Sign next;
	private Sign last;
	private Sign buy_64;
	private Sign buy_1;
	private Sign sell_64;
	private Sign sell_1;
	private Sign display_1;
	private Sign display_2;
	private NPCQuester keeper;
	private int selected;
	private int initialization = -1;

	public NPCSignShop(String storeName, Location start, Location end) {
		super(storeName, start, end); 
		selected = 0;
	}
	
	public NPCSignShop(String name, String town) {
		super(name, town);
		selected = 0;
	}
	
	@Override
	public void queryData() {
		super.queryData();

		MineQuest.getSQLServer().aupdate(
				"CREATE TABLE IF NOT EXISTS " + getName()
						+ "_signs (name VARCHAR(30), x INT, y INT, z INT)");
		
		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM " + getName() + "_signs");
		
		try {
			while (results.next()) {
				String name = results.getString("name");
				int x = results.getInt("x");
				int y = results.getInt("y");
				int z = results.getInt("z");
				if (name.equals("next")) {
					next = (Sign)MineQuest.getSServer().getWorlds().get(0).getBlockAt(x, y, z).getState();
				} else if (name.equals("last")) {
					last = (Sign)MineQuest.getSServer().getWorlds().get(0).getBlockAt(x, y, z).getState();
				} else if (name.equals("buy_1")) {
					buy_1 = (Sign)MineQuest.getSServer().getWorlds().get(0).getBlockAt(x, y, z).getState();
				} else if (name.equals("buy_64")) {
					buy_64 = (Sign)MineQuest.getSServer().getWorlds().get(0).getBlockAt(x, y, z).getState();
				} else if (name.equals("sell_1")) {
					sell_1 = (Sign)MineQuest.getSServer().getWorlds().get(0).getBlockAt(x, y, z).getState();
				} else if (name.equals("sell_64")) {
					sell_64 = (Sign)MineQuest.getSServer().getWorlds().get(0).getBlockAt(x, y, z).getState();
				} else if (name.equals("display_1")) {
					display_1 = (Sign)MineQuest.getSServer().getWorlds().get(0).getBlockAt(x, y, z).getState();
				} else if (name.equals("display_2")) {
					display_2 = (Sign)MineQuest.getSServer().getWorlds().get(0).getBlockAt(x, y, z).getState();
				} 
			}
		} catch (SQLException e) {
			MineQuest.log("Problem with database for shop " + getName());
		}
	}
	
	public void intialize(Quester quester) {
		initialization = 0;
		quester.sendMessage("Set next");
	}
	
	public void skip(Quester quester) {
		if (initialization >= 0) {
			switch (initialization) {
			case 0:
				quester.sendMessage("Set last");
				break;
			case 1:
				quester.sendMessage("Set buy_1");
				break;
			case 2:
				quester.sendMessage("Set buy_64");
				break;
			case 3:
				quester.sendMessage("Set sell_1");
				break;
			case 4:
				quester.sendMessage("Set sell_64");
				break;
			case 5:
				quester.sendMessage("Set display_1");
				break;
			case 6:
				quester.sendMessage("Set display_2");
				break;
			case 7:
				quester.sendMessage("Done!");
				save();
				initialization = -1;
				return;
			}
			initialization++;
		}
	}
	
	private void save() {
		if (next != null) {
			MineQuest.log("Saving Next");
			MineQuest.getSQLServer().update("DELETE FROM " + getName() + "_signs WHERE name='next'");
			MineQuest.getSQLServer().update(
					"INSERT INTO " + getName() + "_signs (name, x, y, z) "
							+ "VALUES('next', '" + next.getX() + "', '"
							+ next.getY() + "', '" + next.getZ() + "')");
		}
		if (last != null) {
			MineQuest.log("Saving Last");
			MineQuest.getSQLServer().update("DELETE FROM " + getName() + "_signs WHERE name='last'");
			MineQuest.getSQLServer().update(
					"INSERT INTO " + getName() + "_signs (name, x, y, z) "
							+ "VALUES('last', '" + last.getX() + "', '"
							+ last.getY() + "', '" + last.getZ() + "')");
		}
		if (buy_64 != null) {
			MineQuest.log("Saving Buy 64");
			MineQuest.getSQLServer().update("DELETE FROM " + getName() + "_signs WHERE name='buy_64'");
			MineQuest.getSQLServer().update(
					"INSERT INTO " + getName() + "_signs (name, x, y, z) "
							+ "VALUES('buy_64', '" + buy_64.getX() + "', '"
							+ buy_64.getY() + "', '" + buy_64.getZ() + "')");
		}
		if (buy_1 != null) {
			MineQuest.log("Saving Buy 1");
			MineQuest.getSQLServer().update("DELETE FROM " + getName() + "_signs WHERE name='buy_1'");
			MineQuest.getSQLServer().update(
					"INSERT INTO " + getName() + "_signs (name, x, y, z) "
							+ "VALUES('buy_1', '" + buy_1.getX() + "', '"
							+ buy_1.getY() + "', '" + buy_1.getZ() + "')");
		}
		if (sell_64 != null) {
			MineQuest.log("Saving sell 64");
			MineQuest.getSQLServer().update("DELETE FROM " + getName() + "_signs WHERE name='sell_64'");
			MineQuest.getSQLServer().update(
					"INSERT INTO " + getName() + "_signs (name, x, y, z) "
							+ "VALUES('sell_64', '" + sell_64.getX() + "', '"
							+ sell_64.getY() + "', '" + sell_64.getZ() + "')");
		}
		if (sell_1 != null) {
			MineQuest.log("Saving Sell 1");
			MineQuest.getSQLServer().update("DELETE FROM " + getName() + "_signs WHERE name='sell_1'");
			MineQuest.getSQLServer().update(
					"INSERT INTO " + getName() + "_signs (name, x, y, z) "
							+ "VALUES('sell_1', '" + sell_1.getX() + "', '"
							+ sell_1.getY() + "', '" + sell_1.getZ() + "')");
		}
		if (display_1 != null) {
			MineQuest.log("Saving Display 1");
			MineQuest.getSQLServer().update("DELETE FROM " + getName() + "_signs WHERE name='display_1'");
			MineQuest.getSQLServer().update(
					"INSERT INTO " + getName() + "_signs (name, x, y, z) "
							+ "VALUES('display_1', '" + display_1.getX() + "', '"
							+ display_1.getY() + "', '" + display_1.getZ() + "')");
		}
		if (display_2 != null) {
			MineQuest.log("Saving Display 2");
			MineQuest.getSQLServer().update("DELETE FROM " + getName() + "_signs WHERE name='display_2'");
			MineQuest.getSQLServer().update(
					"INSERT INTO " + getName() + "_signs (name, x, y, z) "
							+ "VALUES('display_2', '" + display_2.getX() + "', '"
							+ display_2.getY() + "', '" + display_2.getZ() + "')");
		}
	}

	public void setKeeper(NPCQuester keeper) {
		this.keeper = keeper;
	}

	@Override
	public void sell(Quester quester, int item_id, int quantity) {
		if (sell(quester, getBlock(item_id), quantity)) {
			quester.sendMessage("<" + keeper.getName() + "> I am not interested in your " + Material.getMaterial(item_id));
		}
	}

	@Override
	public void sell(Quester quester, String name, int quantity) {
		if (sell(quester, getBlock(name), quantity)) {
			quester.sendMessage("<" + keeper.getName() + "> I am not interested in your " + name);
		}
	}
	
	public boolean parseClick(Quester quester, Block clicked) {
		if (initialization == -1) {
			if (next != null) {
				if (equals(next.getBlock(), clicked)) {
					next(quester, clicked);
					return true;
				}
			}
			if (last != null) {
				if (equals(last.getBlock(), clicked)) {
					last(quester, clicked);
					return true;
				}
			}
			if (buy_64 != null) {
				if (equals(buy_64.getBlock(), clicked)) {
					buy_64(quester, clicked);
					updateDisplay();
					return true;
				}
			}
			if (buy_1 != null) {
				if (equals(buy_1.getBlock(), clicked)) {
					buy_1(quester, clicked);
					updateDisplay();
					return true;
				}
			}
			if (sell_64 != null) {
				if (equals(sell_64.getBlock(), clicked)) {
					sell_64(quester, clicked);
					updateDisplay();
					return true;
				}
			}
			if (sell_1 != null) {
				if (equals(sell_1.getBlock(), clicked)) {
					sell_1(quester, clicked);
					updateDisplay();
					return true;
				}
			}
		} else {
			if ((clicked.getType() != Material.SIGN) && (clicked.getType() != Material.WALL_SIGN)) {
				return false;
			}
			switch (initialization) {
			case 0:
				next = (Sign)clicked.getState();
				quester.sendMessage("Set last");
				break;
			case 1:
				last = (Sign)clicked.getState();
				quester.sendMessage("Set buy_1");
				break;
			case 2:
				buy_1 = (Sign)clicked.getState();
				quester.sendMessage("Set buy_64");
				break;
			case 3:
				buy_64 = (Sign)clicked.getState();
				quester.sendMessage("Set sell_1");
				break;
			case 4:
				sell_1 = (Sign)clicked.getState();
				quester.sendMessage("Set sell_64");
				break;
			case 5:
				sell_64 = (Sign)clicked.getState();
				quester.sendMessage("Set display_1");
				break;
			case 6:
				display_1 = (Sign)clicked.getState();
				quester.sendMessage("Set display_2");
				break;
			case 7:
				display_2 = (Sign)clicked.getState();
				quester.sendMessage("Done!");
				save();
				initialization = -1;
				updateDisplay();
				return true;
			default:
				return false;
			}
			initialization++;
			return true;
		}
		
		return false;
	}
	
	public void updateDisplay() {
		if (blocks.size() == 0) return;
		StoreBlock block = blocks.get(selected);
		String lines[] = new String [] {
				"Material Type:",
				block.getType(),
				"Material Id:",
				"" + block.getId()
		};

		if (display_1 != null) {
			MineQuest.getEventQueue().addEvent(new UpdateSignEvent(100, display_1, lines));
		}
		
		lines = new String [] {
				"Quantity:",
				block.getQuantity() + "",
				"Price:",
				block.getPriceString() + ""
		};
		
		if (display_2 != null) {
			MineQuest.getEventQueue().addEvent(new UpdateSignEvent(200, display_2, lines));
		}
		
//		if (!display_2.update()) {
//			display_2.update(true);
//		}
		
		
//		display_2.update(true);
	}

	private void sell_1(Quester quester, Block clicked) {
		StoreBlock block = blocks.get(selected);
		
		block.sell(quester, 1);
	}

	private void sell_64(Quester quester, Block clicked) {
		StoreBlock block = blocks.get(selected);
		
		block.sell(quester, 64);
	}

	private void buy_1(Quester quester, Block clicked) {
		StoreBlock block = blocks.get(selected);
		
		block.buy(quester, 1);
	}

	private void buy_64(Quester quester, Block clicked) {
		StoreBlock block = blocks.get(selected);
		
		block.buy(quester, 64);
	}

	private void last(Quester quester, Block clicked) {
		selected--;
		
		if (selected < 0) {
			selected = blocks.size() - 1;
		}
		
		updateDisplay();
	}

	private void next(Quester quester, Block clicked) {
		selected++;
		
		if (selected == blocks.size()) {
			selected = 0;
		}
		
		updateDisplay();
	}

	private boolean equals(Block block, Block clicked) {
		if (block.getX() != clicked.getX()) {
			return false;
		}
		if (block.getY() != clicked.getY()) {
			return false;
		}
		if (block.getZ() != clicked.getZ()) {
			return false;
		}
		return true;
	}
	
	@Override
	public void delete() {
		super.delete();
		
		MineQuest.getSQLServer().aupdate("DROP TABLE " + getName() + "_signs");
	}

}
