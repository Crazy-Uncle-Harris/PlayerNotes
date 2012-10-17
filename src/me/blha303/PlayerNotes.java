package me.blha303;

//import java.io.File;
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

//import couk.Adamki11s.Exceptions.MultipleUpdatePackageException;
import couk.Adamki11s.SQL.SyncSQL;
import couk.Adamki11s.Updates.UpdatePackage;
//import couk.Adamki11s.Updates.UpdateService;

public class PlayerNotes extends JavaPlugin implements Listener {

	private static final Logger log = Logger.getLogger("Minecraft");
	public PlayerNotes plugin;
	public static Permission perms = null;
	public final PlayerNotesSQLConfig pnConfig = new PlayerNotesSQLConfig(this);
	public final PlayerNotesSQLLib pnSql = new PlayerNotesSQLLib(this);
	public ResultSet result;
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
		// Checking dependencies
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			log.severe(String.format("[%s] Disabled. Vault is missing!",
					getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		if (getServer().getPluginManager().getPlugin("Sync") == null) {
			log.severe(String.format("[%s] Disabled. Sync is missing!",
					getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		// Loading config.yml
		pnConfig.loadConfiguration();
		// Setting up the SQL connection
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
		
/*		//Sync auto-updates
		String websiteURL = "http://blha303.com.au/PlayerNotes.html";
	    boolean autoDownloadUpdates = true;
	    boolean reloadAfterUpdate = false;
	    File downloadLocation = new File("plugins" + File.separator + "PlayerNotes" + File.separator + "PlayerNotes.jar");
	    pack = new UpdatePackage(plugin, websiteURL, autoDownloadUpdates, reloadAfterUpdate, downloadLocation);
	    try {
	        UpdateService.registerUpdateService(pack); //try and register our update service. Only 1 allowed per plugin
	        //Sync will handle the rest.
	    } catch (MultipleUpdatePackageException e1) {
	        e1.printStackTrace();
	    } */

		// Registering the command listener
		getServer().getPluginManager().registerEvents(this, this);
		// Setting up Vault permissions
		setupPermissions();
		// Complete
		log.info(String.format("[%s] Enabled version %s", getDescription()
				.getName(), getDescription().getVersion()));
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer()
				.getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	public boolean newPlayerNote(String notes, Player about, String from) {
		String notesa = notes.replace("\\", "\\\\");
		String notesb = notesa.replace("'", "\\'");
		
		String query = "INSERT INTO " + this.pnConfig.getMySQLTable()
				+ " (notes,about,fromusr) " + "VALUES ('" + notesb + "', '"
				+ about.getName() + "', '" + from + "');";
		if (this.pnConfig.isShowDebug()) {
			log.info("[PlayerNames] DEBUG: " + query);
		}
		try {
			this.sql.standardQuery(query);
			return true;
		} catch (SQLException e) {
			log.info("[PlayerNotes] " + e.toString());
			return false;
		}
	}

	public String getPlayerNotes(String about) {
		String notes = null;
		String abouta = about.replace("\\", "\\\\");
		String aboutb = abouta.replace("'", "\\'");
		String query = "SELECT * FROM " + this.pnConfig.getMySQLTable()
				+ " WHERE about='" + aboutb + "';";
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
						notes = notes + ChatColor.WHITE + "; "
								+ ChatColor.GREEN + result.getString("notes");
					} else {
						notes = ChatColor.GREEN + result.getString("notes");
					}
					if (result.isAfterLast()) {
						break;
					}
				}
				return notes;
			} else {
				return null;
			}
		} catch (SQLException e) {
			log.severe("[PlayerNotes] " + e.toString());
			return null;
		}
	}
	
	public String getFromNotes(String from) {
		String notes = null;
		String froma = from.replace("\\", "\\\\");
		String fromb = froma.replace("'", "\\'");
		String query = "SELECT * FROM " + this.pnConfig.getMySQLTable()
				+ " WHERE fromusr='" + fromb + "';";
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
						notes = notes + ChatColor.WHITE + "; "
								+ ChatColor.GREEN + result.getString("notes") + "(about " + result.getString("about") + ")";
					} else {
						notes = ChatColor.GREEN + result.getString("notes") + "(about " + result.getString("about") + ")";
					}
					if (result.isAfterLast()) {
						break;
					}
				}
				return notes;
			} else {
				return null;
			}
		} catch (SQLException e) {
			log.severe("[PlayerNotes] " + e.toString());
			return null;
		}
	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		Player player = null;
		String notes;
		String node;

		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			player = null;
		}

		if (args.length == 0) {
			return false;
		}

		if (command.getName().equalsIgnoreCase("pnotesview")) {
			node = "playernotes.pnv";
			boolean go = false;
			if (player != null) {
				if (perms.has(sender, node)) {
					go = true;
				} else if (sender.isOp()) {
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
						player.sendMessage(ChatColor.DARK_GREEN + "Notes for "
								+ args[0] + ": " + notes);
						log.info(String.format("[%s] %s used /notes %s",
								getDescription().getName(), sender.getName(),
								args[0]));
					} else {
						log.info(ChatColor.DARK_GREEN
								+ "[PlayerNotes] Notes for " + args[0] + ": "
								+ notes);
					}
					return true;
				} else {
					if (player != null) {
						player.sendMessage(ChatColor.RED
								+ "No notes / invalid player name.");
						log.info(String.format("[%s] %s used /notes %s",
								getDescription().getName(), sender.getName(),
								args[0]));
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
			node = "playernotes.pnp";
			boolean go = false;
			if (player != null) {
				if (perms.has(sender, node)) {
					go = true;
				} else if (sender.isOp()) {
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
						player.sendMessage(ChatColor.DARK_GREEN + "Notes from "
								+ args[0] + ": " + notes);
						log.info(String.format("[%s] %s used /notes %s",
								getDescription().getName(), sender.getName(),
								args[0]));
					} else {
						log.info(ChatColor.DARK_GREEN
								+ "[PlayerNotes] Notes from " + args[0] + ": "
								+ notes);
					}
					return true;
				} else {
					if (player != null) {
						player.sendMessage(ChatColor.RED
								+ "No notes / invalid player name.");
						log.info(String.format("[%s] %s used /notes %s",
								getDescription().getName(), sender.getName(),
								args[0]));
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
			node = "playernotes.pna";
			boolean note;
			boolean go = false;
			if (player != null) {
				if (perms.has(sender, node)) {
					go = true;
				} else if (sender.isOp()) {
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
				if (getServer().getPlayer(args[0]) == null) {
					if (player != null) {
						player.sendMessage(ChatColor.RED
								+ "Player could not be found: "
								+ ChatColor.WHITE + args[0]);
						player.sendMessage(ChatColor.RED
								+ "If the player is not online, use /noteaddo.");
						return false;
					} else {
						log.info(ChatColor.RED
								+ "[PlayerNotes] Player could not be found: "
								+ ChatColor.WHITE + args[0]);
						log.info(ChatColor.RED
								+ "[PlayerNotes] If the player is not online, use /noteaddo.");
						return false;
					}
				} else {
					if (player != null) {
						note = newPlayerNote(range,
								getServer().getPlayer(args[0]),
								player.getName());
						if (note) {
							player.sendMessage(ChatColor.GREEN + "Note added.");
							log.info(String.format(
									"[%s] %s used /noteadd %s %s",
									getDescription().getName(),
									sender.getName(), args[0], range));
							return true;
						} else {
							player.sendMessage(ChatColor.RED
									+ "Note could not be added.");
							player.sendMessage(ChatColor.RED
									+ "Don't use ', \" or \\ in your note.");
							log.info(String.format(
									"[%s] %s used /noteadd %s %s",
									getDescription().getName(),
									sender.getName(), args[0], range));
							return true;
						}
					} else {
						note = newPlayerNote(range,
								getServer().getPlayer(args[0]), "<CONSOLE>");
						if (note) {
							log.info(ChatColor.GREEN
									+ "[PlayerNotes] Note added.");
							return true;
						} else {
							log.info(ChatColor.RED
									+ "[PlayerNotes] Note could not be added.");
							return true;
						}
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
