package tk.blha303;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.MySQL;

public class PlayerNotesSQLLib {
	
	Logger log = Logger.getLogger("Minecraft");
	PlayerNotes plugin;
	
	private MySQL mysql;
	private String prefix = "PN";
	
	public PlayerNotesSQLLib(PlayerNotes plugin) {
		this.plugin = plugin;
	}

	public void openConnection() {
	
		this.mysql = new MySQL(
				this.log, 
				prefix, 
				plugin.pnConfig.getMySQLServer(), 
				plugin.pnConfig.getMySQLPort(), 
				plugin.pnConfig.getMySQLDatabase(), 
				plugin.pnConfig.getMySQLUsername(), 
				plugin.pnConfig.getMySQLPassword()
		);
		
		try {
			this.mysql.open();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if ( this.checkConnection() ) {
			if ( !tableExists(plugin.pnConfig.getMySQLTable()) ) {
				this.mysql.createTable("CREATE TABLE " + plugin.pnConfig.getMySQLTable()  +
						" (idplayernotes INT not null auto_increment, " +
						"PRIMARY KEY (idplayernotes), " + 
						"notes varchar(2000), " +
						"about varchar(45), " +
						"fromusr varchar(45))");
			}
		}
	}
	
/*	public void updateTableSchema() throws SQLException {
		log.info("Updating Schema information for table.");
		if ( ! columnExists(plugin.opConfig.getMySQLDatabase(), plugin.opConfig.getMySQLTable(), "online") ) {
			log.info("Creating additional 'online' column for table.");
			this.mysql.query("ALTER TABLE " + plugin.opConfig.getMySQLTable() + " ADD COLUMN online boolean default false;");
		}
	} */
	
	public void closeConnection() {
		this.mysql.close();
	}
	
	private boolean checkConnection() {
		return this.mysql.checkConnection();
	}
	
	public ResultSet SqlQuery(String query) {
		if ( this.checkConnection() ) {
			try {
				return this.mysql.query(query);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public boolean columnExists(String db, String tbl, String column) {
		ResultSet result = SqlQuery("SELECT * FROM Information_Schema.COLUMNS " +
				"WHERE Information_Schema.COLUMNS.COLUMN_NAME = '" + column + "' " +
				"AND Information_Schema.COLUMNS.TABLE_NAME = '" + tbl + "' " +
				"AND Information_Schema.COLUMNS.TABLE_SCHEMA = '" + db + "'");
				
		try {
			if ( ! result.isBeforeFirst() ) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean tableExists(String table) {
		return this.mysql.checkTable(table);
	}
	
}
