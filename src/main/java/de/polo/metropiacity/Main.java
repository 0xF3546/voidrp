package de.polo.metropiacity;

import de.polo.metropiacity.listeners.*;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.playerUtils.*;
import de.polo.metropiacity.utils.*;
import de.polo.metropiacity.commands.*;
import de.polo.metropiacity.utils.Game.*;
import de.polo.metropiacity.utils.InventoryManager.InventoryApiRegister;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
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
    public static final String admin_prefix = "§8[§cAdmin§8] §7";
    public static final String error_cantinteract = "§8[§cFehler§8] §7Du kannst gerade nicht interagieren.";
    public static final String PayDay_prefix = "§8[§aPayDay§8] §7";
    public static final String faction_prefix = "§8[§9Fraktion§8] §7";
    public static final String support_prefix = "§8[§3Support§8] §7";
    public static final String gamedesign_prefix = "§8[§9Gamedesign§8] §7";

    public static final String error_nopermission = "§8[§cFehler§8] §7Für den ausgeführten Befehl hast du keine Rechte.";
    public static final String error = "§8[§cFehler§8] §7";
    public static final String admin_error = "§8[§c§lADMIN§8] §cFehler§8 » §7";
    public static final String business_prefix = "§8[§6Business§8]§7 ";



    private static Main instance;
    @Getter
    public MySQL mySQL;
    @Getter
    public CooldownManager cooldownManager;
    public TeamSpeak teamSpeak;
    public PlayerManager playerManager;
    public Utils utils;
    public AdminManager adminManager;
    public Commands commands;
    public FactionManager factionManager;
    public ServerManager serverManager;
    public LocationManager locationManager;
    public VertragUtil vertragUtil;
    public SupportManager supportManager;
    public ComputerUtils computerUtils;
    public Housing housing;
    public BusinessManager businessManager;
    public Vehicles vehicles;
    public Streetwar streetwar;
    public Weapons weapons;
    public BlockManager blockManager;

    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        mySQL = new MySQL();
        supportManager = new SupportManager(mySQL);
        playerManager = new PlayerManager(mySQL);
        cooldownManager = new CooldownManager();
        locationManager = new LocationManager(mySQL);
        adminManager = new AdminManager(playerManager);
        factionManager = new FactionManager(playerManager);
        housing = new Housing(playerManager);
        utils = new Utils(playerManager, adminManager, factionManager, locationManager, housing, new Navigation(playerManager));
        vehicles = new Vehicles(playerManager ,locationManager);
        vertragUtil = new VertragUtil(playerManager, factionManager, adminManager);
        serverManager = new ServerManager(playerManager, factionManager, utils, locationManager);
        computerUtils = new ComputerUtils(playerManager, factionManager);
        businessManager = new BusinessManager(playerManager);
        streetwar = new Streetwar(playerManager, factionManager, utils);
        weapons = new Weapons(utils);
        blockManager = new BlockManager(mySQL);
        isOnline = true;
        commands = new Commands(this, playerManager, adminManager, locationManager, supportManager, vehicles);

        new InventoryApiRegister(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§cDer Server wurde reloaded.");
        }
        getLogger().info("§cMETROPIACITY ROLEPLAY STARTED.");
        plugin = Bukkit.getPluginManager().getPlugin("MetropiaCity");
        try {
            Statement statement = mySQL.getStatement();
            statement.execute("DELETE FROM bank_logs WHERE datum < DATE_SUB(NOW(), INTERVAL 7 DAY)");
            statement.execute("DELETE FROM phone_messages WHERE datum < DATE_SUB(NOW(), INTERVAL 14 DAY)");

            Shop.loadShopItems();

            teamSpeak = new TeamSpeak(playerManager, factionManager, utils);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerListener() {
        new JoinListener(playerManager, adminManager, utils, locationManager, serverManager);
        new QuitListener(playerManager, adminManager, utils, commands, serverManager, supportManager);
        new DamageListener(playerManager);
        new ChatListener(playerManager, supportManager, utils);
        new BlockBreakListener(playerManager, commands);
        new BlockPlaceListener();
        new CommandListener(playerManager, utils);
        new DeathListener(playerManager, adminManager, factionManager, utils, streetwar);
        new ServerPingListener(factionManager);
        new InventoryClickListener(playerManager, factionManager, utils, locationManager);
        new HouseLockListener(playerManager, utils);
        new InventoryCloseListener(playerManager);
        new ItemDropListener(weapons);
        new PlayerPickUpArrowListener();
        new PlayerVoteListener(playerManager, adminManager);
        new RespawnListener(playerManager);
        new PlayerMoveListener(playerManager, utils);
        new PlayerLoginListener();
        new PlayerInteractListener(playerManager, utils, commands);
        new PlayerInteractWithPlayerListener(playerManager);
        new ExpPickupListener();
        new ItemPickUpListener();
        new FishingListener(playerManager);
        new ProjectileHitListener();
        new WorldListener();
        new InventoryOpenListener();
        new HungerListener(playerManager);
        new AntiCheat();
        new PlayerSwapHandItemsListener(playerManager);
        new GameModeChangeEvent();
        new EntitySpawnListener();
        new EntityDamageByEntityListener(playerManager);
    }

    public static void registerCommand(String command, CommandExecutor c) {
        instance.getCommand(command).setExecutor(c);
    }

    public static void addTabCompeter(String command, TabCompleter c) {
        instance.getCommand(command).setTabCompleter(c);
    }

    private void registerCommands() {
        getCommand("aduty").setExecutor(adminManager);
        /*getCommand("setgroup").setExecutor(commands.setTeamCommand);
        getCommand("geldbeutel").setExecutor(commands.geldbeutelCommand);
        getCommand("personalausweis").setExecutor(commands.personalausweisCommand);
        getCommand("teamchat").setExecutor(commands.teamChatCommand);
        getCommand("leadfrak").setExecutor(commands.leadFrakCommand);
        getCommand("fraktionschat").setExecutor(commands.fraktionsChatCommand);
        getCommand("uninvite").setExecutor(commands.uninviteCommand);
        getCommand("setloc").setExecutor(commands.locationCommand);
        getCommand("setfraktion").setExecutor(commands.setFrakCommand);
        getCommand("adminuninvite").setExecutor(commands.adminUnInviteCommand);
        getCommand("guidechat").setExecutor(commands.assistentchatCommand);
        getCommand("support").setExecutor(commands.supportCommand);
        getCommand("cancelsupport").setExecutor(commands.cancelSupportCommand);
        getCommand("acceptsupport").setExecutor(commands.acceptTicketCommand);
        getCommand("closesupport").setExecutor(commands.closeTicketCommand);
        getCommand("tpto").setExecutor(commands.tpToCommand);
        getCommand("adminrevive").setExecutor(commands.adminReviveCommand);
        getCommand("playerinfo").setExecutor(commands.playerInfoCommand);
        getCommand("me").setExecutor(commands.meCommand);
        getCommand("announce").setExecutor(commands.broadcastCommand);
        getCommand("gov").setExecutor(commands.govCommand);
        getCommand("tickets").setExecutor(commands.ticketsCommand);
        getCommand("team").setExecutor(commands.teamCommand);
        getCommand("shop").setExecutor(commands.shopCommand);
        getCommand("annehmen").setExecutor(commands.annehmenCommand);
        getCommand("ablehnen").setExecutor(commands.ablehnenVertrag);
        getCommand("invite").setExecutor(commands.inviteCommand);
        getCommand("rent").setExecutor(commands.rentCommand);
        getCommand("holzfäller").setExecutor(commands.lumberjackCommand);
        getCommand("apfelsammler").setExecutor(commands.apfelplantageCommand);
        getCommand("minenarbeiter").setExecutor(commands.mineCommand);
        getCommand("arrest").setExecutor(commands.arrestCommand);
        getCommand("tp").setExecutor(commands.tpCommand);
        getCommand("tphere").setExecutor(commands.tpHereCommand);
        getCommand("speed").setExecutor(commands.speedCommand);
        getCommand("bossmenu").setExecutor(commands.openBossMenuCommand);
        getCommand("kick").setExecutor(commands.kickCommand);
        getCommand("ooc").setExecutor(commands.oocCommand);
        getCommand("plugins").setExecutor(commands.pluginCommand);
        getCommand("adminmenu").setExecutor(commands.adminMenuCommand);
        getCommand("vote").setExecutor(commands.voteCommand);
        getCommand("setrankname").setExecutor(commands.setRankNameCommand);
        getCommand("setrankpayday").setExecutor(commands.setRankPayDayCommand);
        getCommand("cp").setExecutor(commands.cpCommand);
        getCommand("sms").setExecutor(commands.smsCommand);
        getCommand("call").setExecutor(commands.callCommand);
        getCommand("auflegen").setExecutor(commands.auflegenCommand);
        getCommand("jailtime").setExecutor(commands.jailtimeCommand);
        getCommand("drop").setExecutor(commands.dropCommand);
        getCommand("lebensmittellieferant").setExecutor(commands.lebensmittelLieferantCommand);
        getCommand("ban").setExecutor(commands.banCommand);
        getCommand("unban").setExecutor(commands.unbanCommand);
        getCommand("setblockvalue").setExecutor(commands.setBlockValueCommand);
        getCommand("car").setExecutor(vehicles);
        getCommand("getveh").setExecutor(commands.getVehCommand);
        getCommand("gotoveh").setExecutor(commands.goToVehCommand);
        getCommand("navi").setExecutor(commands.navigation);
        //getCommand("einreise").setExecutor(commands.einreiseCommand);
        getCommand("registerhouse").setExecutor(commands.registerHouseCommand);
        getCommand("reinforcement").setExecutor(commands.reinforcementCommand);
        getCommand("buyhouse").setExecutor(commands.buyHouseCommand);
        getCommand("mieters").setExecutor(commands.mietersCommand);
        getCommand("unrent").setExecutor(commands.unrentCommand);
        getCommand("frisk").setExecutor(commands.friskCommand);
        getCommand("blacklist").setExecutor(commands.blacklistCommand);
        getCommand("ffa").setExecutor(utils.ffaUtils);
        getCommand("npc").setExecutor(commands.npc);
        getCommand("redeem").setExecutor(commands.redeemCommand);
        getCommand("whistle").setExecutor(commands.whistleCommand);
        getCommand("shout").setExecutor(commands.shoutCommand);
        getCommand("setsecondaryteam").setExecutor(commands.setSecondaryTeamCommand);
        getCommand("bauteamchat").setExecutor(commands.bauteamChat);
        getCommand("eventteamchat").setExecutor(commands.eventTeamChat);
        getCommand("prteamchat").setExecutor(commands.prTeamChat);
        getCommand("stats").setExecutor(commands.statsCommand);
        getCommand("revive").setExecutor(commands.reviveCommand);
        getCommand("service").setExecutor(commands.serviceCommand);
        getCommand("acceptservice").setExecutor(commands.acceptServiceCommand);
        getCommand("services").setExecutor(commands.servicesCommand);
        getCommand("cancelservice").setExecutor(commands.cancelServiceCommand);
        getCommand("closeservice").setExecutor(commands.closeServiceCommand);
        getCommand("orten").setExecutor(commands.ortenCommand);
        getCommand("gangwar").setExecutor(gangwarUtils);
        getCommand("youtube").setExecutor(commands.youtubeCommand);
        getCommand("discord").setExecutor(commands.discordCommand);
        getCommand("departmentchat").setExecutor(commands.departmentChatCommand);
        getCommand("member").setExecutor(commands.memberCommand);
        getCommand("farmer").setExecutor(commands.farmerCommand);
        getCommand("müllmann").setExecutor(commands.muellmannCommand);
        getCommand("postbote").setExecutor(commands.postboteCommand);
        getCommand("contracts").setExecutor(commands.contractsCommand);
        getCommand("contract").setExecutor(commands.contractCommand);
        getCommand("nachrichten").setExecutor(commands.nachrichtenCommand);
        getCommand("businesschat").setExecutor(commands.businessChatCommand);
        getCommand("leadbusiness").setExecutor(commands.leadBusinessCommand);
        getCommand("fraktionsinfo").setExecutor(commands.frakInfoCommand);
        getCommand("beziehung").setExecutor(commands.beziehungCommand);
        getCommand("trennen").setExecutor(commands.trennenCommand);
        getCommand("antrag").setExecutor(commands.antragCommand);
        getCommand("self").setExecutor(commands.selfCommand);
        getCommand("garage").setExecutor(commands.garageCommand);
        getCommand("akten").setExecutor(commands.aktenCommand);
        getCommand("spec").setExecutor(commands.specCommand);
        getCommand("msg").setExecutor(commands.msgCommand);
        getCommand("leaderchat").setExecutor(commands.leaderChatCommand);
        getCommand("frakstats").setExecutor(commands.frakStatsCommand);
        getCommand("checkinv").setExecutor(commands.checkInvCommand);
        getCommand("bizinvite").setExecutor(commands.bizInviteCommand);
        getCommand("shoprob").setExecutor(commands.shopRobCommand);
        getCommand("respawn").setExecutor(commands.respawnCommand);
        getCommand("dealer").setExecutor(commands.dealerCommand);
        getCommand("farming").setExecutor(commands.farming);
        getCommand("gethead").setExecutor(commands.getHeadCommand);
        getCommand("factions").setExecutor(commands.factionsCommand);
        getCommand("serverreload").setExecutor(commands.serverReloadCommand);
        getCommand("serverstop").setExecutor(commands.serverStopCommand);
        getCommand("giverank").setExecutor(commands.giveRankCommand);
        getCommand("forum").setExecutor(commands.forumCommand);
        getCommand("autoban").setExecutor(commands.autoBanCommand);
        getCommand("banlist").setExecutor(commands.banListCommand);
        getCommand("gm").setExecutor(commands.gmCommand);
        getCommand("note").setExecutor(commands.noteCommand);
        getCommand("streetwar").setExecutor(streetwar);
        //getCommand("getskin").setExecutor(commands.getSkinCommand);

        getCommand("tslink").setExecutor(new TSLinkCommand(playerManager));
        getCommand("verify").setExecutor(teamSpeak);
        getCommand("tsunlink").setExecutor(new TSUnlinkCommand(playerManager));

        getCommand("reinforcement").setTabCompleter(commands.reinforcementCommand);
        getCommand("blacklist").setTabCompleter(commands.blacklistCommand);
        getCommand("gangwar").setTabCompleter(gangwarUtils);
        getCommand("navi").setTabCompleter(commands.navigation);
        getCommand("contracts").setTabCompleter(commands.contractsCommand);
        getCommand("personalausweis").setTabCompleter(commands.personalausweisCommand);
        getCommand("fraktionsinfo").setTabCompleter(commands.frakInfoCommand);
        getCommand("tpto").setTabCompleter(commands.tpToCommand);
        getCommand("farming").setTabCompleter(commands.farming);
        getCommand("aduty").setTabCompleter(adminManager);*/
    }


    @Override
    public void onDisable() {
        TeamSpeak.getTeamSpeak().shutdown();
        System.out.println("Disabling MetroCity Roleplay");
        isOnline = false;
        try {
            serverManager.savePlayers();
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

    public class Commands {
        private Main main;
        private PlayerManager playerManager;
        private AdminManager adminManager;
        private LocationManager locationManager;
        private SupportManager supportManager;
        private Vehicles vehicles;
        public Commands(Main main, PlayerManager playerManager, AdminManager adminManager, LocationManager locationManager, SupportManager supportManager, Vehicles vehicles) {
            this.main = main;
            this.playerManager = playerManager;
            this.adminManager = adminManager;
            this.locationManager = locationManager;
            this.supportManager = supportManager;
            this.vehicles = vehicles;
            Init();
        }
        public SetTeamCommand setTeamCommand;
        public GeldbeutelCommand geldbeutelCommand ;
        public PersonalausweisCommand personalausweisCommand;
        public  TeamChatCommand teamChatCommand;
        public LeadFrakCommand leadFrakCommand;
        public FraktionsChatCommand fraktionsChatCommand;
        public UninviteCommand uninviteCommand;
        public LocationCommand locationCommand;
        public SetFrakCommand setFrakCommand;
        public AdminUnInviteCommand adminUnInviteCommand;
        public AssistentchatCommand assistentchatCommand;
        public SupportCommand supportCommand;
        public CancelSupportCommand cancelSupportCommand;
        public AcceptTicketCommand acceptTicketCommand;
        public CloseTicketCommand closeTicketCommand;
        public TPToCommand tpToCommand;
        public AdminReviveCommand adminReviveCommand;
        public PlayerInfoCommand playerInfoCommand;
        public MeCommand meCommand;
        public BroadcastCommand broadcastCommand;
        public GovCommand govCommand;
        public TicketsCommand ticketsCommand;
        public TeamCommand teamCommand;
        public ShopCommand shopCommand;
        public AnnehmenCommand annehmenCommand;
        public AblehnenVertrag ablehnenVertrag;
        public InviteCommand inviteCommand;
        public RentCommand rentCommand;
        public LumberjackCommand lumberjackCommand;
        public ApfelplantageCommand apfelplantageCommand;
        public MineCommand mineCommand;
        public ArrestCommand arrestCommand;
        public TPCommand tpCommand;
        public TPHereCommand tpHereCommand;
        public SpeedCommand speedCommand;
        public OpenBossMenuCommand openBossMenuCommand;
        public KickCommand kickCommand;
        public OOCCommand oocCommand;
        public PluginCommand pluginCommand;
        public AdminMenuCommand adminMenuCommand;
        public VoteCommand voteCommand;
        public SetRankNameCommand setRankNameCommand;
        public SetRankPayDayCommand setRankPayDayCommand;
        public CPCommand cpCommand;
        public SMSCommand smsCommand;
        public CallCommand callCommand;
        public AuflegenCommand auflegenCommand;
        public JailtimeCommand jailtimeCommand;
        public DropCommand dropCommand;
        public LebensmittelLieferantCommand lebensmittelLieferantCommand;
        public BanCommand banCommand;
        public UnbanCommand unbanCommand;
        public SetBlockValueCommand setBlockValueCommand;
        public GetVehCommand getVehCommand;
        public GoToVehCommand goToVehCommand;
        public Navigation navigation;
        public EinreiseCommand einreiseCommand;
        public RegisterHouseCommand registerHouseCommand;
        public ReinforcementCommand reinforcementCommand;
        public BuyHouseCommand buyHouseCommand;
        public MietersCommand mietersCommand;
        public UnrentCommand unrentCommand;
        public FriskCommand friskCommand;
        public BlacklistCommand blacklistCommand;
        public NPC npc;
        public RedeemCommand redeemCommand;
        public WhistleCommand whistleCommand;
        public ShoutCommand shoutCommand;
        public SetSecondaryTeamCommand setSecondaryTeamCommand;
        public BauteamChat bauteamChat;
        public EventTeamChat eventTeamChat;
        public PRTeamChat prTeamChat;
        public StatsCommand statsCommand;
        public ReviveCommand reviveCommand;
        public ServiceCommand serviceCommand;
        public AcceptServiceCommand acceptServiceCommand;
        public ServicesCommand servicesCommand;
        public CancelServiceCommand cancelServiceCommand;
        public CloseServiceCommand closeServiceCommand;
        public OrtenCommand ortenCommand;
        public YoutubeCommand youtubeCommand;
        public DiscordCommand discordCommand;
        public DepartmentChatCommand departmentChatCommand;
        public MemberCommand memberCommand;
        public FarmerCommand farmerCommand;
        public MuellmannCommand muellmannCommand;
        public PostboteCommand postboteCommand;
        public ContractsCommand contractsCommand;
        public ContractCommand contractCommand;
        public NachrichtenCommand nachrichtenCommand;
        public BusinessChatCommand businessChatCommand;
        public LeadBusinessCommand leadBusinessCommand;
        public FrakInfoCommand frakInfoCommand;
        public BeziehungCommand beziehungCommand;
        public TrennenCommand trennenCommand;
        public AntragCommand antragCommand;
        public SelfCommand selfCommand;
        public GarageCommand garageCommand;
        public AktenCommand aktenCommand;
        public SpecCommand specCommand;
        public MsgCommand msgCommand;
        public LeaderChatCommand leaderChatCommand;
        public FrakStatsCommand frakStatsCommand;
        public CheckInvCommand checkInvCommand;
        public BizInviteCommand bizInviteCommand;
        public ShopRobCommand shopRobCommand;
        public RespawnCommand respawnCommand;
        public DealerCommand dealerCommand;
        public Farming farming;
        public GetHeadCommand getHeadCommand;
        public FactionsCommand factionsCommand;
        public ServerReloadCommand serverReloadCommand;
        public ServerStopCommand serverStopCommand;
        public GiveRankCommand giveRankCommand;
        public ForumCommand forumCommand;
        public AutoBanCommand autoBanCommand;
        public BanListCommand banListCommand;
        public GMCommand gmCommand;
        public NoteCommand noteCommand;
        public RegisterblockCommand registerblockCommand;
        public RegisterATMCommand registerATMCommand;
        public Laboratory laboratory;
        //public GetSkinCommand getSkinCommand;
        private void Init() {
            setTeamCommand = new SetTeamCommand(playerManager, adminManager);
            geldbeutelCommand  = new GeldbeutelCommand(playerManager);
            personalausweisCommand = new PersonalausweisCommand(playerManager, utils);
            teamChatCommand = new TeamChatCommand(playerManager, utils);
            leadFrakCommand = new LeadFrakCommand(playerManager, adminManager, factionManager);
            fraktionsChatCommand = new FraktionsChatCommand(playerManager, factionManager, utils);
            uninviteCommand = new UninviteCommand(playerManager, adminManager, factionManager);
            locationCommand = new LocationCommand(locationManager);
            setFrakCommand = new SetFrakCommand(playerManager, adminManager, factionManager);
            adminUnInviteCommand = new AdminUnInviteCommand(playerManager, adminManager, factionManager, utils);
            assistentchatCommand = new AssistentchatCommand(playerManager, utils);
            supportCommand = new SupportCommand(playerManager, supportManager);
            cancelSupportCommand = new CancelSupportCommand(supportManager);
            acceptTicketCommand = new AcceptTicketCommand(playerManager, adminManager, supportManager, utils, mySQL);
            closeTicketCommand = new CloseTicketCommand(playerManager, supportManager, adminManager, utils);
            tpToCommand = new TPToCommand(playerManager, locationManager);
            adminReviveCommand = new AdminReviveCommand(playerManager, utils);
            playerInfoCommand = new PlayerInfoCommand(playerManager, factionManager);
            meCommand = new MeCommand();
            broadcastCommand = new BroadcastCommand(playerManager, utils);
            govCommand = new GovCommand(playerManager, factionManager, utils);
            ticketsCommand = new TicketsCommand(playerManager, supportManager);
            teamCommand = new TeamCommand(playerManager);
            shopCommand = new ShopCommand(playerManager, locationManager);
            annehmenCommand = new AnnehmenCommand(utils);
            ablehnenVertrag = new AblehnenVertrag(utils);
            inviteCommand = new InviteCommand(playerManager, factionManager, utils);
            rentCommand = new RentCommand(playerManager, locationManager, utils);
            lumberjackCommand = new LumberjackCommand(playerManager, locationManager);
            apfelplantageCommand = new ApfelplantageCommand(playerManager, locationManager);
            mineCommand = new MineCommand(playerManager, locationManager);
            arrestCommand = new ArrestCommand(playerManager, factionManager, utils);
            tpCommand = new TPCommand(playerManager, adminManager);
            tpHereCommand = new TPHereCommand(playerManager, adminManager);
            speedCommand = new SpeedCommand(playerManager);
            openBossMenuCommand = new OpenBossMenuCommand(playerManager, factionManager);
            kickCommand = new KickCommand(playerManager);
            oocCommand = new OOCCommand();
            pluginCommand = new PluginCommand(playerManager);
            adminMenuCommand = new AdminMenuCommand(playerManager, factionManager);
            voteCommand = new VoteCommand();
            setRankNameCommand = new SetRankNameCommand(playerManager, adminManager, factionManager);
            setRankPayDayCommand = new SetRankPayDayCommand(playerManager, adminManager, factionManager);
            cpCommand = new CPCommand();
            smsCommand = new SMSCommand(playerManager, utils);
            callCommand = new CallCommand(playerManager, utils);
            auflegenCommand = new AuflegenCommand(utils);
            jailtimeCommand = new JailtimeCommand(playerManager);
            dropCommand = new DropCommand(playerManager, commands);
            lebensmittelLieferantCommand = new LebensmittelLieferantCommand(playerManager, locationManager);
            banCommand = new BanCommand(playerManager);
            unbanCommand = new UnbanCommand(playerManager, adminManager);
            setBlockValueCommand = new SetBlockValueCommand(playerManager);
            getVehCommand = new GetVehCommand(playerManager);
            goToVehCommand = new GoToVehCommand(playerManager);
            navigation = new Navigation(playerManager);
            einreiseCommand = new EinreiseCommand(playerManager, locationManager);
            registerHouseCommand = new RegisterHouseCommand(playerManager);
            reinforcementCommand = new ReinforcementCommand(playerManager, factionManager);
            buyHouseCommand = new BuyHouseCommand(playerManager);
            mietersCommand = new MietersCommand(playerManager, utils);
            unrentCommand = new UnrentCommand(utils);
            friskCommand = new FriskCommand(playerManager);
            blacklistCommand = new BlacklistCommand(playerManager, factionManager);
            npc = new NPC(playerManager);
            redeemCommand = new RedeemCommand(playerManager, utils);
            whistleCommand = new WhistleCommand(utils);
            shoutCommand = new ShoutCommand(utils);
            setSecondaryTeamCommand = new SetSecondaryTeamCommand(playerManager);
            bauteamChat = new BauteamChat(playerManager, utils);
            eventTeamChat = new EventTeamChat(playerManager, utils);
            prTeamChat = new PRTeamChat(playerManager, utils);
            statsCommand = new StatsCommand(playerManager);
            reviveCommand = new ReviveCommand(playerManager, utils, factionManager);
            serviceCommand = new ServiceCommand(playerManager, utils);
            acceptServiceCommand = new AcceptServiceCommand(playerManager, utils);
            servicesCommand = new ServicesCommand(playerManager);
            cancelServiceCommand = new CancelServiceCommand(playerManager, utils);
            closeServiceCommand = new CloseServiceCommand(playerManager);
            ortenCommand = new OrtenCommand(playerManager, utils);
            youtubeCommand = new YoutubeCommand();
            discordCommand = new DiscordCommand();
            departmentChatCommand = new DepartmentChatCommand(playerManager);
            memberCommand = new MemberCommand(playerManager, factionManager);
            farmerCommand = new FarmerCommand(playerManager, locationManager, utils);
            muellmannCommand = new MuellmannCommand(playerManager, locationManager);
            postboteCommand = new PostboteCommand(playerManager, locationManager);
            contractsCommand = new ContractsCommand(playerManager, factionManager);
            contractCommand = new ContractCommand(playerManager, factionManager);
            nachrichtenCommand = new NachrichtenCommand(playerManager, locationManager);
            businessChatCommand = new BusinessChatCommand(playerManager);
            leadBusinessCommand = new LeadBusinessCommand(playerManager, businessManager);
            frakInfoCommand = new FrakInfoCommand(factionManager);
            beziehungCommand = new BeziehungCommand(playerManager, utils);
            trennenCommand = new TrennenCommand(playerManager);
            antragCommand = new AntragCommand(playerManager, utils);
            selfCommand = new SelfCommand(playerManager);
            garageCommand = new GarageCommand(locationManager, vehicles);
            aktenCommand = new AktenCommand(playerManager);
            specCommand = new SpecCommand(playerManager, adminManager);
            msgCommand = new MsgCommand(playerManager);
            leaderChatCommand = new LeaderChatCommand(playerManager, utils);
            frakStatsCommand = new FrakStatsCommand(playerManager, factionManager, utils);
            checkInvCommand = new CheckInvCommand(playerManager);
            bizInviteCommand = new BizInviteCommand(playerManager, utils);
            shopRobCommand = new ShopRobCommand(playerManager, locationManager, factionManager);
            respawnCommand = new RespawnCommand(playerManager, adminManager, utils, locationManager);
            dealerCommand = new DealerCommand();
            farming = new Farming(playerManager, locationManager, utils);
            getHeadCommand = new GetHeadCommand();
            factionsCommand = new FactionsCommand(playerManager, factionManager);
            serverReloadCommand = new ServerReloadCommand();
            serverStopCommand = new ServerStopCommand();
            giveRankCommand = new GiveRankCommand(playerManager, factionManager);
            forumCommand = new ForumCommand(playerManager, factionManager, utils);
            autoBanCommand = new AutoBanCommand(playerManager);
            banListCommand = new BanListCommand(playerManager);
            gmCommand = new GMCommand(playerManager);
            noteCommand = new NoteCommand(playerManager, utils);
            //GetSkinCommand getSkinCommand = new GetSkinCommand(playerManager);
            registerblockCommand = new RegisterblockCommand(playerManager, mySQL, blockManager);
            registerATMCommand = new RegisterATMCommand(playerManager, adminManager, mySQL);
            laboratory = new Laboratory(playerManager, factionManager, locationManager);

            main.registerCommands();
            main.registerListener();
        }
    }
}
