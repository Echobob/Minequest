package org.monksanctum.MineQuest.Economy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.monksanctum.MineQuest.MineQuest;

public class Bank {
	private List<Account> accounts;
	
	public Bank() {
		accounts = new ArrayList<Account>();
		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM bank");
		
		try {
			while (results.next()) {
				accounts.add(new Account(results.getString("owner"), results.getDouble("balance")));
			}
		} catch (SQLException e) {
		}
	}
	
	public boolean hasAccount(String name) {
		int i;
		
		for (i = 0; i < accounts.size(); i++) {
			if (accounts.get(i).getOwner().equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Account getAccount(String name) {
		int i;
		
		for (i = 0; i < accounts.size(); i++) {
			if (accounts.get(i).getOwner().equalsIgnoreCase(name)) {
				return accounts.get(i);
			}
		}
		
		return null;
	}

	public Account createAccount(String name, double balance) {
		Account created = getAccount(name);
		
		if (created != null) {
			created.setBalance(balance);
		} else {
			MineQuest.getSQLServer().update("INSERT INTO bank (owner, balance) VALUES('" + name + "', '" + balance + "')");
			created = new Account(name, balance);
		}
		
		return created;
	}
}
