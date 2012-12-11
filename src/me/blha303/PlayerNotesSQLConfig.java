package me.blha303;

public class PlayerNotesSQLConfig {
	
	PlayerNotes plugin;
	String MySQLServer,MySQLPort,MySQLUsername,MySQLPassword,MySQLDatabase,MySQLTable;
	
	public void loadConfiguration(String server, String port, String username, String pass, String dbase, String table) {
		MySQLServer = server;
		MySQLPort = port;
		MySQLUsername = username;
		MySQLPassword = pass;
		MySQLDatabase = dbase;
		MySQLTable = table;
	}

	public PlayerNotesSQLConfig(PlayerNotes plugin) {
		this.plugin = plugin;
	}
	
	public String getMySQLServer() {
		return plugin.getConfig().getString("MySQLServer");
	}
	
	public String getMySQLPort() {
		return plugin.getConfig().getString("MySQLPort");
	}
	
	public String getMySQLUsername() {
		return plugin.getConfig().getString("MySQLUsername");
	}
	
	public String getMySQLPassword() {
		return plugin.getConfig().getString("MySQLPassword");
	}
	
	public String getMySQLDatabase() {
		return plugin.getConfig().getString("MySQLDatabase");
	}
	
	public String getMySQLTable() {
		return plugin.getConfig().getString("MySQLTable");
	}
	
	public void setMySQLServer(String inp) {
		MySQLServer = inp;
	}
	
	public void setMySQLPort(String inp) {
		MySQLPort = inp;
	}
	
	public void setMySQLUsername(String inp) {
		MySQLUsername = inp;
	}
	
	public void setMySQLPassword(String inp) {
		MySQLPassword = inp;
	}
	
	public void setMySQLDatabase(String inp) {
		MySQLDatabase = inp;
	}
	
	public void setMySQLTable(String inp) {
		MySQLTable = inp;
	}
	
	public boolean isShowDebug() {
		return plugin.getConfig().getBoolean("showDebug");
	}	
}
