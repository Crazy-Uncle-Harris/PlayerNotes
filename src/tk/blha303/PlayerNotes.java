package tk.blha303;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerNotes extends JavaPlugin implements Listener {

	private static final Logger log = Logger.getLogger("Minecraft");
	public static Permission perms = null;
	public static PlayerNotes plugin;
	public final PlayerNotesSQLConfig pnConfig = new PlayerNotesSQLConfig(this);
    public final PlayerNotesSQLLib pnSql = new PlayerNotesSQLLib(this);
    private ResultSet result;
    
    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }
    
	@Override
	public void onEnable() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			log.severe(String.format("[%s] Disabled. Vault is missing!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		pnConfig.loadConfiguration();
		pnSql.openConnection();
		
		getServer().getPluginManager().registerEvents(this, this);
		setupPermissions();
        log.info(String.format("[%s] Enabled version %s", getDescription().getName(), getDescription().getVersion()));
	}
	
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    public void newPlayerNote(String notes, Player about, String from) {
    	String query = "INSERT INTO " + this.pnConfig.getMySQLTable() + " (notes,about,fromusr) " + 
						"VALUES ('" + notes + "', '" + about.getName() + "', '" + from + "');";
    	if ( this.pnConfig.isShowDebug()) { log.info(query); }
    	this.pnSql.SqlQuery(query);
    }
    
    public void newPlayerNoteOffline(String notes, String about, String from) {
    	String query = "INSERT INTO " + this.pnConfig.getMySQLTable() + " (notes,about,fromusr) " + 
						"VALUES ('" + notes + "', '" + about + "', '" + from + "');";
    	if ( this.pnConfig.isShowDebug()) { log.info(query); }
    	this.pnSql.SqlQuery(query);
    }
    
    public String getPlayerNotes(Player about) {
    	String notes = null;
    	result = this.pnSql.SqlQuery("SELECT * FROM " + this.pnConfig.getMySQLTable() + " WHERE about='" + about.getName() + "';");
    	
    	try {
    		if ( result.isBeforeFirst() ) {
    			for (int i = 1; result.absolute(i); i++) {
    				if (notes!=null) {
    					notes = notes + "; " + result.getString("notes");
    				} else {
    					notes = result.getString("notes");
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
    		return null;
    	}
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;
        String notes;
        String node;

        if ( sender instanceof Player) {
                player = (Player) sender;
        } else {
        		player = null;
        }
                
                if ( args.length == 0 ) {
                        return false;
                }
                
                if ( command.getName().equalsIgnoreCase("notes")) {
                	node = "playernotes.notes";
                	if (perms.has(sender, node)) {
                		notes = getPlayerNotes(getServer().getPlayer(args[0]));
                		if (notes!=null) {
                			if (player != null) {
                				player.sendMessage("Notes for " + getServer().getPlayer(args[0]).getName() + ": " + notes);
                				log.info(String.format("[%s] %s used /notes %s", getDescription().getName(), sender.getName(), args[0]));
                			} else {
                				log.info("[PlayerNotes] Notes for " + getServer().getPlayer(args[0]).getName() + ": " + notes);
                			}
                			return true;
                		} else {
                			if (player != null) {
                				player.sendMessage("No notes / invalid player name.");
                				log.info(String.format("[%s] %s used /notes %s", getDescription().getName(), sender.getName(), args[0]));
                			} else {
                				log.info("[PlayerNotes] No notes / invalid player name.");
                			}
                			return true;
                		}
                	} else {
                		if (player != null) {
                			player.sendMessage("You don't have permission to use this command.");
                		}
                		return true;
                	}
                }
                
                if (command.getName().equalsIgnoreCase("noteadd")) {
                	node = "playernotes.noteadd";
                	if (perms.has(sender, node)) {
                	String range = null;
                	for (int i = 1; i < args.length; i++) {
                		if (range==null) {
                			range = args[i];
                		} else {
                			range = range + " " + args[i];
                		}
                	}
                	if (getServer().getPlayer(args[0]) == null) {
                		if (player != null) {
                			player.sendMessage("Player could not be found: " + args[0]);
                			player.sendMessage("If the player is not online, use /noteaddo.");
                			return false;
                		} else {
                			log.info("[PlayerNotes] Player could not be found: " + args[0]);
                			log.info("[PlayerNotes] If the player is not online, use /noteaddo.");
                			return false;
                		}
                	} else {
                		if (player != null) {
                			newPlayerNote(range, getServer().getPlayer(args[0]), player.getName());
                			player.sendMessage("Note added.");
                			log.info(String.format("[%s] %s used /noteadd %s %s", getDescription().getName(), sender.getName(), args[0], range));
                			return true;
                		} else {
                			newPlayerNote(range, getServer().getPlayer(args[0]), "<CONSOLE>");
                			log.info("[PlayerNotes] Note added.");
                			return true;
                		}
                	}
                } else {
                		if (player != null) {
                			player.sendMessage("You don't have permission to use this command.");
                		}
                		return true;
                }
                }
                
                if (command.getName().equalsIgnoreCase("noteaddo")) {
                	node = "playernotes.noteaddo";
                	if (perms.has(sender, node)) {
                	String range = null;
                	for (int i = 1; i < args.length; i++) {
                		if (range==null) {
                			range = args[i];
                		} else {
                			range = range + " " + args[i];
                		}
                	}
                	if (player != null) {
                    	newPlayerNoteOffline(range, args[0], player.getName());
                    	player.sendMessage("Note added.");
                    	log.info(String.format("[%s] %s used /noteadd %s %s", getDescription().getName(), sender.getName(), args[0], range));
                    	return true;
                	} else {
                		newPlayerNoteOffline(range, args[0], "<CONSOLE>");
                		log.info("[PlayerNotes] Note added.");
                		return true;
                	}
                } else {
                	if (player != null) {
                		player.sendMessage("You don't have permission to use this command.");
                	}
                	return true;
                }
                }
        
        return false;
    }

}
