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
    
    public void newPlayerNote(String notes, Player about, Player from) {
    	this.pnSql.SqlQuery("INSERT INTO " + this.pnConfig.getMySQLTable() + " (notes,about,from) " + 
						"VALUES ('" + notes + "', '" + about.getName() + "', '" + from.getName() + "');");
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

        if ( sender instanceof Player) {
                player = (Player) sender;
                
                if ( args.length == 0 ) {
                        return false;
                }
                
                if ( command.getName().equalsIgnoreCase("notes")) {
                	notes = getPlayerNotes(getServer().getPlayer(args[0]));
                	if (notes!=null) {
                		player.sendMessage(notes.replace("; ", "\n"));
                	} else {
                		player.sendMessage("No notes / invalid player name.");
                	}
                }
                
                if (command.getName().equalsIgnoreCase("noteadd")) {
                	String range = null;
                	for (int i = 1; i < args.length; i++) {
                		range = range + " " + args[i];
                	}
                	newPlayerNote(range, player, player);
                }
        }
        
        return false;
}

}
