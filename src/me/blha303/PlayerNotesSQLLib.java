package me.blha303;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class PlayerNotesSQLLib {
	Logger log = Logger.getLogger("Minecraft");
	String url;
	Connection conn = null;
	PreparedStatement myQuery = null;
	PlayerNotes plugin;

	public PlayerNotesSQLLib(PlayerNotes plugin) {
		this.plugin = plugin;
		this.url = ("jdbc:mysql://" + plugin.pnConfig.getMySQLServer() + ":"
				+ plugin.pnConfig.getMySQLPort() + "/" + plugin.pnConfig
				.getMySQLDatabase());
	}

	// Starting SQL
	public Connection SQLConnect() throws SQLException {
		Connection conn = DriverManager.getConnection(this.url,
				this.plugin.pnConfig.getMySQLUsername(),
				this.plugin.pnConfig.getMySQLPassword());
		return conn;
	}

	// Closing SQL
	public void SQLDisconnect() throws SQLException {
		plugin.sql.close();
		this.myQuery.close();
		this.conn.close();
	}

	public void runUpdateQuery(String query) {
		try {
			this.conn = SQLConnect();
			this.myQuery = this.conn.prepareStatement(query);

			this.myQuery.executeUpdate();
			SQLDisconnect();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public ResultSet runSearchQuery(String query) throws SQLException {
		ResultSet result = null;
		this.conn = SQLConnect();
		this.myQuery = this.conn.prepareStatement(query);

		result = this.myQuery.executeQuery();

		return result;
	}

	public void createSqlTable() throws SQLException {
		runUpdateQuery("CREATE TABLE " + plugin.pnConfig.getMySQLTable()
				+ " (id INT not null auto_increment, " 
				+ "PRIMARY KEY (id), "
				+ "notes varchar(2000), " 
				+ "about varchar(45), "
				+ "fromusr varchar(45), " 
				+ "datetime INT)");
	}

	public boolean tableExists(String db, String tbl) {
		ResultSet result = null;
		Boolean recordExists = Boolean.valueOf(false);

		String query = "SELECT * FROM Information_Schema.TABLES WHERE Information_Schema.TABLES.TABLE_NAME = '"
				+ tbl
				+ "' "
				+ "AND Information_Schema.TABLES.TABLE_SCHEMA = '"
				+ db + "'";

		try {
			result = runSearchQuery(query);
		} catch (SQLException e1) {
			plugin.error("Unable to check if table exists", e1);
		}
		try {
			recordExists = Boolean.valueOf(result.isBeforeFirst());
			SQLDisconnect();

			return recordExists.booleanValue();
		} catch (SQLException e) {
			plugin.error("Unable to disconnect from SQL database", e);
		}

		return false;
	}

	public boolean columnExists(String db, String tbl, String column) {
		ResultSet result = null;
		Boolean recordExists = Boolean.valueOf(false);

		String query = "SELECT * FROM Information_Schema.COLUMNS WHERE Information_Schema.COLUMNS.COLUMN_NAME = '"
				+ column
				+ "' "
				+ "AND Information_Schema.COLUMNS.TABLE_NAME = '"
				+ tbl
				+ "' "
				+ "AND Information_Schema.COLUMNS.TABLE_SCHEMA = '" + db + "'";

		try {
			result = runSearchQuery(query);
		} catch (SQLException e1) {
			plugin.error("Unable to get ResultSet", e1);
		}
		try {
			this.log.info("Result of column " + column + " check: "
					+ result.isBeforeFirst());

			recordExists = Boolean.valueOf(result.isBeforeFirst());

			SQLDisconnect();

			return recordExists.booleanValue();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean insertInto(String notes, String about, String fromusr,
			long datetime) {
		String query = "INSERT INTO " + plugin.pnConfig.getMySQLTable()
				+ " (notes,about,fromusr,datetime) " + "VALUES ('" + notes
				+ "', '" + about + "', '" + fromusr + "', " + datetime + ");";
		debug(query);
		try {
			runUpdateQuery(query);
			SQLDisconnect();
			return true;
		} catch (SQLException e) {
			plugin.error("Unable to add Player Note", e);
			return false;
		}
	}

	public ResultSet getInfo(boolean from, String about) {
		String query = "";
		ResultSet result;
		if (from) {
			query = "SELECT * FROM " + plugin.pnConfig.getMySQLTable()
					+ " WHERE fromusr='" + about + "';";
		} else {
			query = "SELECT * FROM " + plugin.pnConfig.getMySQLTable()
					+ " WHERE about='" + about + "';";
		}
		try {
			plugin.sql = SQLConnect();
		} catch (SQLException e1) {
			plugin.error("Unable to connect to SQL", e1);
			return null;
		}

		debug(query);
		try {
			result = runSearchQuery(query);
			if (result != null) {
				debug("Result is something!");
			}
			return result;
		} catch (SQLException e) {
			plugin.error("Unable to get ResultSet", e);
			return null;
		}
	}

	public void debug(String debug) {
		if (plugin.pnConfig.isShowDebug()) {
			log.info("[PlayerNotes] DEBUG: " + debug);
		}
	}
}