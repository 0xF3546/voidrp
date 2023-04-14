package de.polo.void_roleplay;

import de.polo.void_roleplay.Listener.*;
import de.polo.void_roleplay.PlayerUtils.Shop;
import de.polo.void_roleplay.Utils.*;
import de.polo.void_roleplay.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Random;

public final class Main extends JavaPlugin {
    public static Plugin plugin = null;
    public static String prefix = "§cVoid§8 » §7";
    public static String debug_prefix = "§8[§7§lDEBUG§8] §cVoid§8 » §7";
    public static String admin_prefix = "§cAdmin§8 » §7";
    public static String error_cantinteract = "§cFehler§8 » §7Du kannst gerade nicht interagieren.";
    public static String PayDay_prefix = "§3PayDay§8 » §7";
    public static String faction_prefix = "§9Fraktion§8 » §7";
    public static String support_prefix = "§3Support§8 » §7";
    public static String gamedesign_prefix = "§9Gamedesign§8 » §7";
    public static String bank_prefix = "§3Bank§8 » §7";

    public static String error_nopermission = "§cFehler§8 » §7Für den ausgeführten Befehl hast du keine Rechte.";
    public static String error = "§cFehler§8 » §7";
    public static String admin_error = "§8[§c§lADMIN§8] §cFehler§8 » §7";
    public static String admin_info = "§8[§9§lINFO§8] §cAdmin§8 » §7";

    private static Main instance;
    public void onLoad() {
        instance = this;
    }
    @Override
    public void onEnable() {
        getLogger().info("§cVOID ROLEPLAY STARTED.");
        registerListener();
        registerCommands();
        plugin = Bukkit.getPluginManager().getPlugin("Void_Roleplay");
        try {
            Weapons.loadWeapons();
            PlayerManager.startTimeTracker();
            LocationManager.loadLocations();
            FactionManager.loadFactions();
            Shop.loadShopItems();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerListener(){

        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new chatListener(), this);
        getServer().getPluginManager().registerEvents(new blockbreakListener(), this);
        getServer().getPluginManager().registerEvents(new blockplaceListener(), this);
        getServer().getPluginManager().registerEvents(new unknownCommandListener(), this);
        getServer().getPluginManager().registerEvents(new deathListener(), this);
        getServer().getPluginManager().registerEvents(new serverPingListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new houseLockListener(), this);
        getServer().getPluginManager().registerEvents(new inventoryCloseListener(), this);
        getServer().getPluginManager().registerEvents(new Weapons(), this);
        getServer().getPluginManager().registerEvents(new itemDropListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerPickUpArrowListener(), this);
        getServer().getPluginManager().registerEvents(new playerVoteListener(), this);
    }

    private void registerCommands(){
        getCommand("aduty").setExecutor(new aduty());
        getCommand("setgroup").setExecutor(new setteamCommand());
        getCommand("geldbeutel").setExecutor(new geldbeutelCommand());
        getCommand("personalausweis").setExecutor(new personalausweisCommand());
        getCommand("teamchat").setExecutor(new teamchatCommand());
        getCommand("leadfrak").setExecutor(new leadfrakCommand());
        getCommand("fraktionschat").setExecutor(new fraktionschatCommand());
        getCommand("uninvite").setExecutor(new uninviteCommand());
        getCommand("setloc").setExecutor(new LocationCommand());
        getCommand("bank").setExecutor(new bankCommand());
        getCommand("setfraktion").setExecutor(new setfrakCommand());
        getCommand("adminuninvite").setExecutor(new adminuninviteCommand());
        getCommand("assistentchat").setExecutor(new assistentchatCommand());
        getCommand("support").setExecutor(new supportCommand());
        getCommand("cancelsupport").setExecutor(new cancelsupportCommand());
        getCommand("acceptsupport").setExecutor(new acceptticketCommand());
        getCommand("closesupport").setExecutor(new closeticketCommand());
        getCommand("target-ip").setExecutor(new targetipCommand());
        getCommand("tpto").setExecutor(new tptoCommand());
        getCommand("adminrevive").setExecutor(new adminReviveCommand());
        getCommand("registeratm").setExecutor(new ATMLocationCommand());
        getCommand("playerinfo").setExecutor(new playerinfoCommand());
        getCommand("me").setExecutor(new meCommand());
        getCommand("broadcast").setExecutor(new broadcastCommand());
        getCommand("gov").setExecutor(new govCommand());
        getCommand("tickets").setExecutor(new ticketsCommand());
        getCommand("team").setExecutor(new teamCommand());
        getCommand("shop").setExecutor(new shopCommand());
        getCommand("annehmen").setExecutor(new annehmenCommand());
        getCommand("ablehnen").setExecutor(new ablehnenVertrag());
        getCommand("invite").setExecutor(new inviteCommand());
        getCommand("rent").setExecutor(new rentCommand());
        getCommand("holzfäller").setExecutor(new lumberjackCommand());
        getCommand("apfelsammler").setExecutor(new apfelplantageCommand());
        getCommand("minenarbeiter").setExecutor(new mineCommand());
        getCommand("arrest").setExecutor(new arrestCommand());
        getCommand("tp").setExecutor(new tpCommand());
        getCommand("tphere").setExecutor(new tphereCommand());
        getCommand("speed").setExecutor(new speedCommand());
        getCommand("bossmenu").setExecutor(new openBossMenuCommand());
        getCommand("kick").setExecutor(new kickCommand());
        getCommand("ooc").setExecutor(new oocCommand());
        getCommand("plugins").setExecutor(new pluginCommand());
        getCommand("adminmenu").setExecutor(new adminmenuCommand());
    }


    @Override
    public void onDisable() {
        try {
            ServerManager.savePlayers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static int random(int min, int max) {
        int randomNumber = min + (int) (Math.random() * ((max - min) + 1));
        return randomNumber;
    }
    public static char getRandomChar(String characters) {
        Random random = new Random();
        int index = random.nextInt(characters.length());
        return characters.charAt(index);
    }

    public static Main getInstance() {
        return instance;
    }
}
