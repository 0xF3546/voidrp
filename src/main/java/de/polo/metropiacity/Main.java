package de.polo.metropiacity;

import de.polo.metropiacity.listeners.*;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.playerUtils.*;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public final class Main extends JavaPlugin {
    public boolean isOnline = false;

    public static Plugin plugin = null;
    public static final String prefix = "§8[§6Metropia§8] §7";
    public static String debug_prefix = "§8[§7§lDEBUG§8] §cMetropia§8 » §7";
    public static final String admin_prefix = "§8[§cAdmin§8] §7";
    public static final String error_cantinteract = "§8[§cFehler§8] §7Du kannst gerade nicht interagieren.";
    public static final String PayDay_prefix = "§8[§aPayDay§8] §7";
    public static final String faction_prefix = "§8[§9Fraktion§8] §7";
    public static final String support_prefix = "§8[§3Support§8] §7";
    public static final String gamedesign_prefix = "§8[§9Gamedesign§8] §7";
    public static final String bank_prefix = "§8[§3Bank§8] §7";

    public static final String error_nopermission = "§8[§cFehler§8] §7Für den ausgeführten Befehl hast du keine Rechte.";
    public static final String error = "§8[§cFehler§8] §7";
    public static final String admin_error = "§8[§c§lADMIN§8] §cFehler§8 » §7";
    public static String admin_info = "§8[§9§lINFO§8] §cAdmin§8 » §7";
    public static final String business_prefix = "§8[§6Business§8]§7 ";

    public static final CooldownManager cooldownManager = new CooldownManager();

    private static Main instance;


    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        isOnline = true;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§cDer Server wurde reloaded.");
        }
        getLogger().info("§cMETROPIACITY ROLEPLAY STARTED.");
        registerListener();
        registerCommands();
        plugin = Bukkit.getPluginManager().getPlugin("MetropiaCity");
        try {
            Statement statement = MySQL.getStatement();
            statement.execute("DELETE FROM bank_logs WHERE datum < DATE_SUB(NOW(), INTERVAL 7 DAY)");
            statement.execute("DELETE FROM phone_messages WHERE datum < DATE_SUB(NOW(), INTERVAL 14 DAY)");
            Weapons.loadWeapons();
            Vehicles.loadVehicles();
            Vehicles.loadPlayerVehicles();
            PlayerManager.startTimeTracker();
            ServerManager.everySecond();
            ServerManager.loadShops();
            ServerManager.startTabUpdateInterval();
            Farming.loadData();

            ServerManager.loadRanks();
            LocationManager.loadLocations();
            FactionManager.loadFactions();
            BusinessManager.loadBusinesses();
            Shop.loadShopItems();
            StaatUtil.loadJail();
            Housing.loadHousing();
            FFAUtils.loadFFALobbys();
            GangwarUtils.loadGangwar();

            ServerManager.loadDBPlayer();
            ServerManager.loadContracts();

            TeamSpeak.loadConfig();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerListener() {

        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new HouseLockListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
        getServer().getPluginManager().registerEvents(new Weapons(), this);
        getServer().getPluginManager().registerEvents(new ItemDropListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerPickUpArrowListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerVoteListener(), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new TabletUtils(), this);
        getServer().getPluginManager().registerEvents(new PhoneUtils(), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(), this);
        getServer().getPluginManager().registerEvents(new ComputerUtils(), this);
        getServer().getPluginManager().registerEvents(new Vehicles(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractWithPlayerListener(), this);
        getServer().getPluginManager().registerEvents(new ExpPickupListener(), this);
        getServer().getPluginManager().registerEvents(new ItemPickUpListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerManager(), this);
        getServer().getPluginManager().registerEvents(new FFAUtils(), this);
        getServer().getPluginManager().registerEvents(new BankingUtils(), this);
        getServer().getPluginManager().registerEvents(new Tutorial(), this);
        getServer().getPluginManager().registerEvents(new NachrichtenCommand(), this);
        getServer().getPluginManager().registerEvents(new Navigation(), this);
        getServer().getPluginManager().registerEvents(new FishingListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryOpenListener(), this);
        getServer().getPluginManager().registerEvents(new Farming(), this);

    }

    private void registerCommands() {
        getCommand("aduty").setExecutor(new ADutyCommand());
        getCommand("setgroup").setExecutor(new SetTeamCommand());
        getCommand("geldbeutel").setExecutor(new GeldbeutelCommand());
        getCommand("personalausweis").setExecutor(new PersonalausweisCommand());
        getCommand("teamchat").setExecutor(new TeamChatCommand());
        getCommand("leadfrak").setExecutor(new LeadFrakCommand());
        getCommand("fraktionschat").setExecutor(new FraktionsChatCommand());
        getCommand("uninvite").setExecutor(new UninviteCommand());
        getCommand("setloc").setExecutor(new LocationCommand());
        getCommand("setfraktion").setExecutor(new SetFrakCommand());
        getCommand("adminuninvite").setExecutor(new AdminUnInviteCommand());
        getCommand("guidechat").setExecutor(new AssistentchatCommand());
        getCommand("support").setExecutor(new SupportCommand());
        getCommand("cancelsupport").setExecutor(new CancelSupportCommand());
        getCommand("acceptsupport").setExecutor(new AcceptTicketCommand());
        getCommand("closesupport").setExecutor(new CloseTicketCommand());
        getCommand("tpto").setExecutor(new TPToCommand());
        getCommand("adminrevive").setExecutor(new AdminReviveCommand());
        getCommand("playerinfo").setExecutor(new PlayerInfoCommand());
        getCommand("me").setExecutor(new MeCommand());
        getCommand("announce").setExecutor(new BroadcastCommand());
        getCommand("gov").setExecutor(new GovCommand());
        getCommand("tickets").setExecutor(new TicketsCommand());
        getCommand("team").setExecutor(new TeamCommand());
        getCommand("shop").setExecutor(new ShopCommand());
        getCommand("annehmen").setExecutor(new AnnehmenCommand());
        getCommand("ablehnen").setExecutor(new AblehnenVertrag());
        getCommand("invite").setExecutor(new InviteCommand());
        getCommand("rent").setExecutor(new RentCommand());
        getCommand("holzfäller").setExecutor(new LumberjackCommand());
        getCommand("apfelsammler").setExecutor(new ApfelplantageCommand());
        getCommand("minenarbeiter").setExecutor(new MineCommand());
        getCommand("arrest").setExecutor(new ArrestCommand());
        getCommand("tp").setExecutor(new TPCommand());
        getCommand("tphere").setExecutor(new TPHereCommand());
        getCommand("speed").setExecutor(new SpeedCommand());
        getCommand("bossmenu").setExecutor(new OpenBossMenuCommand());
        getCommand("kick").setExecutor(new KickCommand());
        getCommand("ooc").setExecutor(new OOCCommand());
        getCommand("plugins").setExecutor(new PluginCommand());
        getCommand("adminmenu").setExecutor(new AdminMenuCommand());
        getCommand("vote").setExecutor(new VoteCommand());
        getCommand("setrankname").setExecutor(new SetRankNameCommand());
        getCommand("setrankpayday").setExecutor(new SetRankPayDayCommand());
        getCommand("cp").setExecutor(new CPCommand());
        getCommand("sms").setExecutor(new SMSCommand());
        getCommand("call").setExecutor(new CallCommand());
        getCommand("auflegen").setExecutor(new AuflegenCommand());
        getCommand("jailtime").setExecutor(new JailtimeCommand());
        getCommand("drop").setExecutor(new DropCommand());
        getCommand("lebensmittellieferant").setExecutor(new LebensmittelLieferantCommand());
        getCommand("ban").setExecutor(new BanCommand());
        getCommand("unban").setExecutor(new UnbanCommand());
        getCommand("setblockvalue").setExecutor(new SetBlockValueCommand());
        getCommand("car").setExecutor(new Vehicles());
        getCommand("getveh").setExecutor(new GetVehCommand());
        getCommand("gotoveh").setExecutor(new GoToVehCommand());
        getCommand("navi").setExecutor(new Navigation());
        getCommand("einreise").setExecutor(new EinreiseCommand());
        getCommand("registerhouse").setExecutor(new RegisterHouseCommand());
        getCommand("reinforcement").setExecutor(new ReinforcementCommand());
        getCommand("buyhouse").setExecutor(new BuyHouseCommand());
        getCommand("mieters").setExecutor(new MietersCommand());
        getCommand("unrent").setExecutor(new UnrentCommand());
        getCommand("frisk").setExecutor(new FriskCommand());
        getCommand("blacklist").setExecutor(new BlacklistCommand());
        getCommand("ffa").setExecutor(new FFAUtils());
        getCommand("npc").setExecutor(new NPC());
        getCommand("redeem").setExecutor(new RedeemCommand());
        getCommand("link").setExecutor(new LinkCommand());
        getCommand("whistle").setExecutor(new WhistleCommand());
        getCommand("shout").setExecutor(new ShoutCommand());
        getCommand("setsecondaryteam").setExecutor(new SetSecondaryTeamCommand());
        getCommand("bauteamchat").setExecutor(new BauteamChat());
        getCommand("eventteamchat").setExecutor(new EventTeamChat());
        getCommand("prteamchat").setExecutor(new PRTeamChat());
        getCommand("stats").setExecutor(new StatsCommand());
        getCommand("revive").setExecutor(new ReviveCommand());
        getCommand("service").setExecutor(new ServiceCommand());
        getCommand("acceptservice").setExecutor(new AcceptServiceCommand());
        getCommand("services").setExecutor(new ServicesCommand());
        getCommand("cancelservice").setExecutor(new CancelServiceCommand());
        getCommand("closeservice").setExecutor(new CloseServiceCommand());
        getCommand("tslink").setExecutor(new TSLinkCommand());
        getCommand("verify").setExecutor(new TeamSpeak());
        getCommand("tsunlink").setExecutor(new TSUnlinkCommand());
        getCommand("orten").setExecutor(new OrtenCommand());
        getCommand("gangwar").setExecutor(new GangwarUtils());
        getCommand("youtube").setExecutor(new YoutubeCommand());
        getCommand("discord").setExecutor(new DiscordCommand());
        getCommand("departmentchat").setExecutor(new DepartmentChatCommand());
        getCommand("member").setExecutor(new MemberCommand());
        getCommand("farmer").setExecutor(new FarmerCommand());
        getCommand("müllmann").setExecutor(new MuellmannCommand());
        getCommand("postbote").setExecutor(new PostboteCommand());
        getCommand("contracts").setExecutor(new ContractsCommand());
        getCommand("contract").setExecutor(new ContractCommand());
        getCommand("nachrichten").setExecutor(new NachrichtenCommand());
        getCommand("businesschat").setExecutor(new BusinessChatCommand());
        getCommand("leadbusiness").setExecutor(new LeadBusinessCommand());
        getCommand("fraktionsinfo").setExecutor(new FrakInfoCommand());
        getCommand("beziehung").setExecutor(new BeziehungCommand());
        getCommand("trennen").setExecutor(new TrennenCommand());
        getCommand("antrag").setExecutor(new AntragCommand());
        getCommand("self").setExecutor(new SelfCommand());
        getCommand("garage").setExecutor(new GarageCommand());
        getCommand("akten").setExecutor(new AktenCommand());
        getCommand("spec").setExecutor(new SpecCommand());
        getCommand("msg").setExecutor(new MsgCommand());
        getCommand("leaderchat").setExecutor(new LeaderChatCommand());
        getCommand("frakstats").setExecutor(new FrakStatsCommand());
        getCommand("checkinv").setExecutor(new CheckInvCommand());
        getCommand("bizinvite").setExecutor(new BizInviteCommand());
        getCommand("shoprob").setExecutor(new ShopRobCommand());
        getCommand("respawn").setExecutor(new RespawnCommand());
        getCommand("dealer").setExecutor(new DealerCommand());
        getCommand("farming").setExecutor(new Farming());
        getCommand("gethead").setExecutor(new GetHeadCommand());
        getCommand("factions").setExecutor(new FactionsCommand());

        getCommand("reinforcement").setTabCompleter(new ReinforcementCommand());
        getCommand("blacklist").setTabCompleter(new BlacklistCommand());
        getCommand("gangwar").setTabCompleter(new GangwarUtils());
        getCommand("navi").setTabCompleter(new Navigation());
        getCommand("contracts").setTabCompleter(new ContractsCommand());
        getCommand("personalausweis").setTabCompleter(new PersonalausweisCommand());
        getCommand("fraktionsinfo").setTabCompleter(new FrakInfoCommand());
        getCommand("tpto").setTabCompleter(new TPToCommand());
        getCommand("farming").setTabCompleter(new Farming());
    }


    @Override
    public void onDisable() {
        TeamSpeak.getAPI().logout();
        TeamSpeak.getQuery().exit();
        System.out.println("Disabling MetroCity Roleplay");
        isOnline = false;
        try {
            ServerManager.savePlayers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static int random(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }

    public static char getRandomChar(String characters) {
        Random random = new Random();
        int index = random.nextInt(characters.length());
        return characters.charAt(index);
    }

    public static String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomInt = random.nextInt(75) + 48;
            if ((randomInt >= 48 && randomInt <= 57) || (randomInt >= 65 && randomInt <= 90) || (randomInt >= 97 && randomInt <= 122)) {
                sb.append((char) randomInt);
            } else {
                i--;
            }
        }
        return sb.toString();
    }

    public static Main getInstance() {
        return instance;
    }

    public static void waitSeconds(int seconds, Runnable runnable) {
        new BukkitRunnable() {
            public void run() {
                runnable.run();
            }
        }.runTaskLater(getInstance(), seconds * 20L);
    }

    public static String getTime(int seconds) {
        int minutes = seconds / 60;
        int sec = seconds % 60;
        return minutes + " Minuten & " + sec + " Sekunden";
    }
}
