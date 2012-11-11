package me.blha303;

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

import couk.Adamki11s.SQL.SyncSQL;
import couk.Adamki11s.Updates.UpdatePackage;

public class PlayerNotes extends JavaPlugin implements Listener {

	private static final Logger log = Logger.getLogger("Minecraft");
	public PlayerNotes plugin;
	public static Permission perms = null;
	public final PlayerNotesSQLConfig pnConfig = new PlayerNotesSQLConfig(this);
	public final PlayerNotesSQLLib pnSql = new PlayerNotesSQLLib(this);
	public ResultSet result;
	ChatColor header = ChatColor.getByChar("2");
	ChatColor aboutc = ChatColor.getByChar("3");
	String col = ChatColor.GRAY + ":" + ChatColor.getByChar("a");
	String sep = ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + " |-| ";
//  &2Notes from johnkapsis&7: &3blha303&7: &anotes &e&l|-|&f &3blha303&7: &amore notes
//  &2Notes about blha303&7: &3johnkapsis&7: &anotes &e&l|-|&f &3johnkapsis&7: &amore notes
	public SyncSQL sql = null;
	UpdatePackage pack;

	@Override
	public void onDisable() {
		this.sql.closeConnection();
		log.info(String.format("[%s] Disabled Version %s", getDescription()
				.getName(), getDescription().getVersion()));
	}

	@Override
	public void onEnable() {
		if (getServer().getPluginManager().getPlugin("Vault") == null
				|| getServer().getPluginManager().getPlugin("Sync") == null) {
			log.severe(String.format(
					"[%s] Disabled. Vault or Sync is missing!",
					getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		pnConfig.loadConfiguration();
		try {
			this.sql = pnSql.SQLConnection();
		} catch (SQLException e1) {
			log.severe("[PlayerNotes] " + e1.toString());
			getServer().getPluginManager().disablePlugin(this);
		}
		this.sql.initialise();
		try {
			if (!this.sql.doesTableExist(this.pnConfig.getMySQLTable())) {
				System.out.println("Table '" + this.pnConfig.getMySQLTable()
						+ "' does not exist! Creating table...");
				this.pnSql.createSqlTable(this.sql);
				System.out.println("Table '" + this.pnConfig.getMySQLTable()
						+ "' created!");
			}
		} catch (SQLException e1) {
			log.severe("[PlayerNotes] " + e1.toString());
		}
		this.sql.closeConnection();

		getServer().getPluginManager().registerEvents(this, this);

		setupPermissions();

		log.info(String.format("[%s] Enabled version %s", getDescription()
				.getName(), getDescription().getVersion()));
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer()
				.getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	public boolean newPlayerNote(String notes, String about, String from) {
		String notesa = notes.replace("\\", "\\\\");
		String notesb = notesa.replace("'", "\\'");

		try {
			this.sql = pnSql.SQLConnection();
		} catch (SQLException e1) {
			log.severe("[PlayerNotes] " + e1.toString());
		}

		String query = "INSERT INTO " + this.pnConfig.getMySQLTable()
				+ " (notes,about,fromusr) " + "VALUES ('" + notesb + "', '"
				+ about + "', '" + from + "');";
		if (this.pnConfig.isShowDebug()) {
			log.info("[PlayerNames] DEBUG: " + query);
		}
		try {
			this.sql.standardQuery(query);
			this.sql.closeConnection();
			return true;
		} catch (SQLException e) {
			log.info("[PlayerNotes] " + e.toString());
			this.sql.closeConnection();
			return false;
		}
	}

	public String getPlayerNotes(String about) {
		String notes = null;
		String abouta = about.replace("\\", "\\\\");
		String aboutb = abouta.replace("'", "\\'");
		String query = "SELECT * FROM " + this.pnConfig.getMySQLTable()
				+ " WHERE about='" + aboutb + "';";
		try {
			this.sql = pnSql.SQLConnection();
		} catch (SQLException e1) {
			log.severe("[PlayerNotes] " + e1.toString());
		}

		if (this.pnConfig.isShowDebug()) {
			log.info("[PlayerNames] DEBUG: " + query);
		}
		try {
			result = this.sql.sqlQuery(query);
		} catch (SQLException e) {
			log.severe("[PlayerNotes] " + e.toString());
			return null;
		}
	//  &3johnkapsis&7: &anotes &e&l|-|&f &3johnkapsis&7: &amore notes
		try {
			if (result.isBeforeFirst()) {
				for (int i = 1; result.absolute(i); i++) {
					if (notes != null) {
						notes = notes + sep + aboutc + result.getString("fromusr") + col + result.getString("notes");
					} else {
						notes = aboutc + result.getString("fromusr") + col + result.getString("notes");
					}
					if (result.isAfterLast()) {
						break;
					}
				}
				this.sql.closeConnection();
				return notes;
			} else {
				this.sql.closeConnection();
				return null;
			}
		} catch (SQLException e) {
			log.severe("[PlayerNotes] " + e.toString());
			this.sql.closeConnection();
			return null;
		}
	}

	public String getFromNotes(String from) {
		String notes = null;
		String froma = from.replace("\\", "\\\\");
		String fromb = froma.replace("'", "\\'");
		String query = "SELECT * FROM " + this.pnConfig.getMySQLTable()
				+ " WHERE fromusr='" + fromb + "';";
		try {
			this.sql = pnSql.SQLConnection();
		} catch (SQLException e1) {
			log.severe("[PlayerNotes] " + e1.toString());
		}

		if (this.pnConfig.isShowDebug()) {
			log.info("[PlayerNames] DEBUG: " + query);
		}
		try {
			result = this.sql.sqlQuery(query);
		} catch (SQLException e) {
			log.severe("[PlayerNotes] " + e.toString());
			return null;
		}

		try {
			if (result.isBeforeFirst()) {
				for (int i = 1; result.absolute(i); i++) {
					if (notes != null) {
						notes = notes + sep + aboutc + result.getString("about") + col + result.getString("notes");
					} else {
						notes = aboutc + result.getString("about") + col + result.getString("notes");
					}
					if (result.isAfterLast()) {
						break;
					}
				}
				this.sql.closeConnection();
				return notes;
			} else {
				this.sql.closeConnection();
				return null;
			}
		} catch (SQLException e) {
			log.severe("[PlayerNotes] " + e.toString());
			this.sql.closeConnection();
			return null;
		}
	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		Player player = null;
		String notes;
		String pnvnode = "playernotes.pnv";
		// String pnhnode = "playernotes.pnh";
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

		/*
		 * if (command.getName().equalsIgnoreCase("pnoteshelp")) { boolean pnv =
		 * perms.has(player, pnvnode); boolean pnp = perms.has(player, pnpnode);
		 * boolean pna = perms.has(player, pnanode); boolean isplayer = false;
		 * boolean go = false; if (player != null) { isplayer = true; if
		 * (perms.has(sender, pnhnode)) { go = true; } else { go = false; } }
		 * else { go = true; }
		 * 
		 * if (go) { ChatColor red = ChatColor.RED; ChatColor white =
		 * ChatColor.WHITE; ChatColor aqua = ChatColor.AQUA; if (isplayer) {
		 * player.sendMessage(String.format(ChatColor.GREEN + "%s v%s",
		 * getDescription().getName(), getDescription().getVersion())); if (pnv
		 * || pnp || pna) { player.sendMessage(String.format(ChatColor.GOLD +
		 * "Commands:")); if (pnv) { player.sendMessage(String.format( red +
		 * "/pnotesview <PLAYER>" + white + " - " + aqua +
		 * "List all notes about a player")); } if (pnp) {
		 * player.sendMessage(String.format( red + "/pnotesposted <PLAYER>" +
		 * white + " - " + aqua + "List all notes from a player")); } if (pna) {
		 * player.sendMessage(String.format( red + "/pnotesadd <PLAYER> <NOTE>"
		 * + white + " - " + aqua + "Add a note")); } return true; } else {
		 * player.sendMessage(red +
		 * "You don't have access to any PlayerNotes commands"); return true; }
		 * } else { log.info(String.format(ChatColor.GREEN + "%s v%s",
		 * getDescription().getName(), getDescription().getVersion()));
		 * log.info(String.format(ChatColor.GOLD + "Commands:"));
		 * log.info(String.format( red + "/pnotesview <PLAYER>" + white + " - "
		 * + aqua + "List all notes about a player")); log.info(String.format(
		 * red + "/pnotesposted <PLAYER>" + white + " - " + aqua +
		 * "List all notes from a player")); log.info(String.format( red +
		 * "/pnotesadd <PLAYER> <NOTE>" + white + " - " + aqua + "Add a note"));
		 * return true; } } else { player.sendMessage(ChatColor.RED +
		 * "You can't use this command."); return true; } }
		 */

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
						player.sendMessage(header + "Notes for " + args[0] + ": " + notes);
						log.info(String.format("[%s] %s used /%s %s",
								getDescription().getName(), sender.getName(),
								command.getName(), args[0]));
					} else {
						log.info(header + "[PlayerNotes] Notes for " + args[0] + ": " + notes);
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
						player.sendMessage(header + "Notes from "
								+ args[0] + ": " + notes);
						log.info(String.format("[%s] %s used /notes %s",
								getDescription().getName(), sender.getName(),
								args[0]));
					} else {
						log.info(header
								+ "[PlayerNotes] Notes from " + args[0] + ": "
								+ notes);
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

}