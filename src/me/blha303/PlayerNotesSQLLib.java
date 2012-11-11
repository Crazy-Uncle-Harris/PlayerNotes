package me.blha303;

import couk.Adamki11s.SQL.SyncSQL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class PlayerNotesSQLLib
{
  Logger log = Logger.getLogger("Minecraft");
  PlayerNotes plugin;

  public PlayerNotesSQLLib(PlayerNotes plugin)
  {
    this.plugin = plugin;
  }
  
  public SyncSQL SQLConnection() throws SQLException {
	  return new SyncSQL(plugin.pnConfig.getMySQLServer(),
				plugin.pnConfig.getMySQLDatabase(),
				plugin.pnConfig.getMySQLUsername(),
				plugin.pnConfig.getMySQLPassword());
  }

  public void createSqlTable(SyncSQL sql) throws SQLException {
    sql.standardQuery("CREATE TABLE " + plugin.pnConfig.getMySQLTable()  +
			" (id INT not null auto_increment, " +
			"PRIMARY KEY (id), " + 
			"notes varchar(2000), " +
			"about varchar(45), " +
			"fromusr varchar(45))");
  }

  public boolean columnExists(SyncSQL sql, String db, String tbl, String column) {
    ResultSet result = null;
    try
    {
      result = sql.sqlQuery("SELECT * FROM Information_Schema.COLUMNS WHERE Information_Schema.COLUMNS.COLUMN_NAME = '" + 
        column + "' " + 
        "AND Information_Schema.COLUMNS.TABLE_NAME = '" + tbl + "' " + 
        "AND Information_Schema.COLUMNS.TABLE_SCHEMA = '" + db + "'");
    }
    catch (SQLException e1) {
      e1.printStackTrace();
    }

    try
    {
      return result.isBeforeFirst();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }

    return false;
  }
}