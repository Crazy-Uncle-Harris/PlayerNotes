package me.blha303;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerNotes extends JavaPlugin implements Listener {

	public Logger log = Logger.getLogger("Minecraft");
	public PlayerNotes plugin;
	public static Permission perms = null;
	public PlayerNotesSQLConfig pnConfig;
	public PlayerNotesSQLLib pnSql;
	public ResultSet result;
	ChatColor header = ChatColor.getByChar("2");
	ChatColor aboutc = ChatColor.getByChar("3");
	String col = ChatColor.GRAY + ":" + ChatColor.getByChar("a");
	String sep = ChatColor.YELLOW.toString() + ChatColor.BOLD.toString()
			+ " |-| ";
	public Connection sql = null;

	@Override
	public void onDisable() {
		try {
			pnSql.SQLDisconnect();
		} catch (SQLException e) {
			error("Could not close connection", e);
		}
		log.info(String.format("[%s] Disabled Version %s", getDescription()
				.getName(), getDescription().getVersion()));
	}

	@Override
	public void onEnable() {

		// Checks for dependencies
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			log.severe(String.format("[%s] Disabled. Vault is missing!",
					getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Loads modified configuration
		String mySQLServer = "MySQLServer";
		String mySQLPort = "MySQLPort";
		String mySQLUsername = "MySQLUsername";
		String mySQLPassword = "MySQLPassword";
		String mySQLDatabase = "MySQLDatabase";
		String mySQLTable = "MySQLTable";
		getConfig().addDefault(mySQLServer, "localhost");
		getConfig().addDefault(mySQLPort, "3306");
		getConfig().addDefault(mySQLUsername, "root");
		getConfig().addDefault(mySQLPassword, "password");
		getConfig().addDefault(mySQLDatabase, "db");
		getConfig().addDefault(mySQLTable, "playernotes");
		getConfig().addDefault("showDebug", false);
		getConfig().options().copyDefaults(true);
		pnConfig = new PlayerNotesSQLConfig(this);

		saveConfig();

		if (pnConfig.isShowDebug()) {
			log.info("[PlayerNotes] Debug mode enabled!");
		}
		// Sets up SQL
		pnSql = new PlayerNotesSQLLib(this);
		try {
			this.sql = pnSql.SQLConnect();
		} catch (SQLException e1) {
			log.severe("[PlayerNotes] " + e1.toString());
			getServer().getPluginManager().disablePlugin(this);
		}

		// Checks if the notes table exists
		try {
			if (!pnSql.tableExists(this.pnConfig.getMySQLDatabase(),
					this.pnConfig.getMySQLTable())) {
				log.info("[PlayerNotes] Table '"
						+ this.pnConfig.getMySQLTable()
						+ "' does not exist! Creating table...");
				this.pnSql.createSqlTable();
				log.info("[PlayerNotes] Table '"
						+ this.pnConfig.getMySQLTable() + "' created!");
			}
		} catch (SQLException e1) {
			error("Unable to create table. Plugin disabled!", e1);
			getServer().getPluginManager().disablePlugin(this);
		}

		// Checks if the new datetime column exists. Doesn't work at the moment.
		// if (!this.pnSql.columnExists(this.pnConfig.getMySQLDatabase(),
		// this.pnConfig.getMySQLTable(), "datetime")) {
		// log.info("[PlayerNotes] Creating datetime column for table.");
		// try {
		// pnSql.runUpdateQuery("ALTER TABLE "
		// + plugin.pnConfig.getMySQLTable()
		// + " ADD COLUMN datetime INT;");
		// } catch (Exception e) {
		// if (!(e instanceof SQLException)) {
		// e.printStackTrace();
		// getServer().getPluginManager().disablePlugin(this);
		// }
		// log.severe("[PlayerNotes] " + e.toString());
		// log.severe("[PlayerNotes] Unable to create datetime column. Plugin disabled!");
		// getServer().getPluginManager().disablePlugin(this);
		// }
		// }

		// Disconnect from SQL. If this doesn't happen, it times out later on.
		try {
			pnSql.SQLDisconnect();
		} catch (SQLException e) {
			error("Could not close connection", e);
		}

		// Register listeners
		getServer().getPluginManager().registerEvents(this, this);

		// Init Vault Permissions
		setupPermissions();

		// Show Enabled message
		log.info(String.format("[%s] Enabled version %s", getDescription()
				.getName(), getDescription().getVersion()));
	}

	// Set up Vault Permissions
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer()
				.getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	// For inserting a new note.
	public boolean newPlayerNote(String notes, String about, String from) {
		String notesa = notes.replace("\\", "\\\\");
		String notesb = notesa.replace("'", "\\'");
		long datetime = System.currentTimeMillis() / 1000l;

		try {
			sql = pnSql.SQLConnect();
		} catch (SQLException e1) {
			error("Unable to connect to SQL database", e1);
			return false;
		}

		if (pnSql.insertInto(notesb, about, from, datetime)) {
			try {
				pnSql.SQLDisconnect();
			} catch (SQLException e) {
				error("Unable to disconnect from SQL database", e);
			}
			return true;
		} else {
			try {
				pnSql.SQLDisconnect();
			} catch (SQLException e) {
				error("Unable to disconnect from SQL database", e);
			}
			return false;
		}
	}

	// For getting a note
	public String getPlayerNotes(String about) {
		String notes = null;
		String abouta = about.replace("\\", "\\\\");
		String aboutb = abouta.replace("'", "\\'");
		try {
			sql = pnSql.SQLConnect();
		} catch (SQLException e1) {
			error("Unable to connect to SQL database", e1);
			return null;
		}
		result = pnSql.getInfo(false, aboutb);
		if (result != null) {
			pnSql.debug("Result: 1");
			// &3johnkapsis&7: &anotes &e&l|-|&f &3johnkapsis&7: &amore notes
			try {
				result.beforeFirst();
				pnSql.debug("Result: 2");
				while (result.next()) {
					pnSql.debug("Result: 3");
					if (notes != null) {
						pnSql.debug("Result: 5");
						notes = notes + sep + aboutc
								+ result.getString("fromusr") + col
								+ result.getString("notes");
					} else {
						pnSql.debug("Result: 6");
						notes = aboutc + result.getString("fromusr") + col
								+ result.getString("notes");
					}
					if (result.isAfterLast()) {
						pnSql.debug("Result: 7");
						break;
					}
				}
				pnSql.debug("Result: 8");
				pnSql.SQLDisconnect();
				return notes;
			} catch (SQLException e) {
				pnSql.debug("Result: 10");
				e.printStackTrace();
				return null;
			}
		} else {
			pnSql.debug("Result: 11");
			return null;
		}
	}

	public String getFromNotes(String from) {
		String notes = null;
		String froma = from.replace("\\", "\\\\");
		String fromb = froma.replace("'", "\\'");
		result = pnSql.getInfo(true, fromb);
		if (result != null) {
			try {
				result.beforeFirst();
				pnSql.debug("Result: 2");
				while (result.next()) {
					pnSql.debug("Result: 3");
					if (notes != null) {
						pnSql.debug("Result: 5");
							notes = notes + sep + aboutc
									+ result.getString("about") + col
									+ result.getString("notes");
					} else {
						pnSql.debug("Result: 6");
							notes = aboutc + result.getString("about") + col
									+ result.getString("notes");
					}
					if (result.isAfterLast()) {
						pnSql.debug("Result: 7");
						break;
					}
				}
				pnSql.debug("Result: 8");
				pnSql.SQLDisconnect();
				return notes;
			} catch (SQLException e) {
				pnSql.debug("Result: 10");
				e.printStackTrace();
				return null;
			}
		} else {
			pnSql.debug("Result: 11");
			return null;
		}
	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		Player player = null;
		String notes;
		String pnvnode = "playernotes.pnv";
		String pnpnode = "playernotes.pnp";
		String pnanode = "playernotes.pna";

		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			player = null;
		}

		if (args.length == 0) {
			return false;
		}

		if (command.getName().equalsIgnoreCase("pnotesview")) {
			boolean go = false;
			if (player != null) {
				if (perms.has(sender, pnvnode)) {
					go = true;
				} else {
					go = false;
				}
			} else {
				go = true;
			}

			if (go) {
				notes = getPlayerNotes(args[0]);
				if (notes != null) {
					if (player != null) {
						player.sendMessage(header + "Notes about " + args[0]
								+ ": " + notes);
						log.info(String.format("[%s] %s used /%s %s",
								getDescription().getName(), sender.getName(),
								command.getName(), args[0]));
					} else {
						log.info(header + "[PlayerNotes] Notes about " + args[0]
								+ ": " + notes);
					}
					return true;
				} else {
					if (player != null) {
						player.sendMessage(ChatColor.RED
								+ "No notes / invalid player name.");
						log.info(String.format("[%s] %s used /%s %s",
								getDescription().getName(), sender.getName(),
								command.getName(), args[0]));
					} else {
						log.info(ChatColor.RED
								+ "[PlayerNotes] No notes / invalid player name.");
					}
					return true;
				}
			} else {
				if (player != null) {
					player.sendMessage(ChatColor.RED
							+ "You don't have permission to use this command.");
				}
				return true;
			}
		}

		if (command.getName().equalsIgnoreCase("pnotesposted")) {
			boolean go = false;
			if (player != null) {
				if (perms.has(sender, pnpnode)) {
					go = true;
				} else {
					go = false;
				}
			} else {
				go = true;
			}

			if (go) {
				notes = getFromNotes(args[0]);
				if (notes != null) {
					if (player != null) {
						player.sendMessage(header + "Notes from " + args[0]
								+ ": " + notes);
						log.info(String.format("[%s] %s used /notes %s",
								getDescription().getName(), sender.getName(),
								args[0]));
					} else {
						log.info(header + "[PlayerNotes] Notes from " + args[0]
								+ ": " + notes);
					}
					return true;
				} else {
					if (player != null) {
						player.sendMessage(ChatColor.RED
								+ "No notes / invalid player name.");
						log.info(String.format("[%s] %s used /%s %s",
								getDescription().getName(), sender.getName(),
								command.getName(), args[0]));
					} else {
						log.info(ChatColor.RED
								+ "[PlayerNotes] No notes / invalid player name.");
					}
					return true;
				}
			} else {
				if (player != null) {
					player.sendMessage(ChatColor.RED
							+ "You don't have permission to use this command.");
				}
				return true;
			}
		}

		if (command.getName().equalsIgnoreCase("pnotesadd")) {
			boolean note;
			boolean go = false;
			if (player != null) {
				if (perms.has(sender, pnanode)) {
					go = true;
				} else {
					go = false;
				}
			} else {
				go = true;
			}

			if (go) {
				String range = null;
				for (int i = 1; i < args.length; i++) {
					if (range == null) {
						range = args[i];
					} else {
						range = range + " " + args[i];
					}
				}
				if (player != null) {
					note = newPlayerNote(range, args[0], player.getName());
					if (note) {
						player.sendMessage(ChatColor.GREEN + "Note added.");
						log.info(String.format("[%s] %s used /%s %s %s",
								getDescription().getName(), sender.getName(),
								command.getName(), args[0], range));
						return true;
					} else {
						player.sendMessage(ChatColor.RED
								+ "Note could not be added.");
						log.info(String.format("[%s] %s used /%s %s %s",
								getDescription().getName(), sender.getName(),
								command.getName(), args[0], range));
						return true;
					}
				} else {
					note = newPlayerNote(range, args[0], "<CONSOLE>");
					if (note) {
						log.info(ChatColor.GREEN + "[PlayerNotes] Note added.");
						return true;
					} else {
						log.info(ChatColor.RED
								+ "[PlayerNotes] Note could not be added.");
						return true;
					}
				}
			} else {
				if (player != null) {
					player.sendMessage(ChatColor.RED
							+ "You don't have permission to use this command.");
				}
				return true;
			}
		}

		
		return false;
	}

	public void error(String error, SQLException exception) {
		log.severe("[PlayerNotes] " + error);
		log.severe("[PlayerNotes] Debug: " + exception.getMessage());
	}

	public void error(String error, SQLException exception, String name) {
		log.severe("[PlayerNotes] " + error + "! Triggered by " + name);
		log.severe("[PlayerNotes] Debug: " + exception.getMessage());
	}

}