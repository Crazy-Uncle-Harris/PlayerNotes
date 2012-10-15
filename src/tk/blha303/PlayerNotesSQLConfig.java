package tk.blha303;

public class PlayerNotesSQLConfig {
	
	PlayerNotes plugin;

	public PlayerNotesSQLConfig(PlayerNotes plugin) {
		this.plugin = plugin;
	}

	public void loadConfiguration() {
		String mySQLServer = "MySQLServer";
		String mySQLPort = "MySQLPort";
		String mySQLUsername = "MySQLUsername";
		String mySQLPassword = "MySQLPassword";
		String mySQLDatabase = "MySQLDatabase";
		String mySQLTable = "MySQLTable";
		
		plugin.getConfig().addDefault(mySQLServer, "localhost");
		plugin.getConfig().addDefault(mySQLPort,  "3306");
		plugin.getConfig().addDefault(mySQLUsername, "root");
		plugin.getConfig().addDefault(mySQLPassword, "");
		plugin.getConfig().addDefault(mySQLDatabase, "db");
		plugin.getConfig().addDefault(mySQLTable,  "playernotes");
		plugin.getConfig().addDefault("showDebug", false);
		
		plugin.getConfig().options().copyDefaults(true);
		
		plugin.saveConfig();
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
	
	public boolean isShowDebug() {
		return plugin.getConfig().getBoolean("showDebug");
	}	
}
