package org.monksanctum.MineQuest.Economy;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.monksanctum.MineQuest.MineQuest;

public class Account {
	private String owner;
	private double balance;
	
	public Account(String owner) {
		this.owner = owner;
	}
	
	public Account(String owner, double balance) {
		this.owner = owner;
		this.balance = balance;
	}
	
	public void query() {
		ResultSet results = MineQuest.getSQLServer().query("SELECT * FROM bank WHERE owner='" + owner + "'");
		
		try {
			if (results.next()) {
				balance = results.getDouble("balance");
			}
		} catch (SQLException e) {
			balance = 0;
		}
	}
	
	public String getOwner() {
		return owner;
	}
	
	public double getBalance() {
		return balance;
	}
	
	public void setBalance(double balance) {
		this.balance = balance;
	}

	public boolean canPayAndPay(double amount) {
		if (balance >= amount) {
			balance -= amount;
			update();
			return true;
		}
		return false;
	}

	public void update() {
		MineQuest.getSQLServer().aupdate("UPDATE bank SET balance='" + balance + "' WHERE ownder='" + owner + "'");
	}

	public void addMoney(double amount) {
		balance += amount;
		update();
	}
}
