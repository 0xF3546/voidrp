package de.polo.voidroleplay;

import de.polo.voidroleplay.listeners.*;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.commands.*;
import de.polo.voidroleplay.utils.Game.*;
import de.polo.voidroleplay.utils.GamePlay.GamePlay;
import de.polo.voidroleplay.utils.InventoryManager.InventoryApiRegister;
import de.polo.voidroleplay.utils.playerUtils.Shop;
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
    public static final String prefix = "§8[§6Void§8] §7";
    public static final String infoPrefix = "§b    Info§f: ";
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
    public GamePlay gamePlay;
    public Laboratory laboratory;
    public CompanyManager companyManager;

    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        mySQL = new MySQL();
        companyManager = new CompanyManager(mySQL);
        supportManager = new SupportManager(mySQL);
        playerManager = new PlayerManager(mySQL);
        cooldownManager = new CooldownManager();
        locationManager = new LocationManager(mySQL);
        adminManager = new AdminManager(playerManager);
        factionManager = new FactionManager(playerManager);
        housing = new Housing(playerManager);
        utils = new Utils(playerManager, adminManager, factionManager, locationManager, housing, new Navigation(playerManager), companyManager);
        vehicles = new Vehicles(playerManager ,locationManager);
        vertragUtil = new VertragUtil(playerManager, factionManager, adminManager);
        serverManager = new ServerManager(playerManager, factionManager, utils, locationManager);
        computerUtils = new ComputerUtils(playerManager, factionManager);
        businessManager = new BusinessManager(playerManager);
        streetwar = new Streetwar(playerManager, factionManager, utils);
        weapons = new Weapons(utils, playerManager);
        blockManager = new BlockManager(mySQL);
        isOnline = true;
        laboratory = new Laboratory(playerManager, factionManager, locationManager);
        gamePlay = new GamePlay(playerManager, utils, mySQL, factionManager, locationManager);
        commands = new Commands(this, playerManager, adminManager, locationManager, supportManager, vehicles, gamePlay, businessManager, weapons, companyManager);
        new InventoryApiRegister(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§cDer Server wurde reloaded.");
        }
        getLogger().info("§VOIDROLEPLAY ROLEPLAY STARTED.");
        plugin = Bukkit.getPluginManager().getPlugin("VoidRoleplay");
        try {
            Statement statement = mySQL.getStatement();
            statement.execute("DELETE FROM bank_logs WHERE datum < DATE_SUB(NOW(), INTERVAL 7 DAY)");
            statement.execute("DELETE FROM phone_messages WHERE datum < DATE_SUB(NOW(), INTERVAL 14 DAY)");

            Shop.loadShopItems();

            //teamSpeak = new TeamSpeak(playerManager, factionManager, utils);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerListener(Commands commands) {
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
        new ItemDropListener(weapons, playerManager);
        new PlayerPickUpArrowListener();
        new PlayerVoteListener(playerManager, adminManager);
        new RespawnListener(playerManager);
        new PlayerMoveListener(playerManager, utils);
        new PlayerLoginListener();
        new PlayerInteractListener(playerManager, utils, commands, blockManager, factionManager, laboratory);
        new PlayerInteractWithPlayerListener(playerManager);
        new ExpPickupListener();
        new ItemPickUpListener();
        new FishingListener(playerManager);
        new ProjectileHitListener();
        new WorldListener();
        new InventoryOpenListener();
        new HungerListener(playerManager);
        new AntiCheat();
        new PlayerSwapHandItemsListener(playerManager, utils);
        new ArmorStandExitListener();
        new GameModeChangeEvent();
        new EntitySpawnListener();
        new EntityDamageByEntityListener(playerManager);
        new ExplosionListener();
    }

    public static void registerCommand(String command, CommandExecutor c) {
        instance.getCommand(command).setExecutor(c);
    }

    public static void addTabCompeter(String command, TabCompleter c) {
        instance.getCommand(command).setTabCompleter(c);
    }

    private void registerCommands() {
        getCommand("aduty").setExecutor(adminManager);
    }

    @Override
    public void onDisable() {
        teamSpeak.shutdown();
        System.out.println("Disabling VoidRoleplay");
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
        private GamePlay gamePlay;
        private BusinessManager businessManager;
        private Weapons weapons;
        private CompanyManager companyManager;
        public Commands(Main main, PlayerManager playerManager, AdminManager adminManager, LocationManager locationManager, SupportManager supportManager, Vehicles vehicles, GamePlay gamePlay, BusinessManager businessManager, Weapons weapons, CompanyManager companyManager) {
            this.main = main;
            this.playerManager = playerManager;
            this.adminManager = adminManager;
            this.locationManager = locationManager;
            this.supportManager = supportManager;
            this.vehicles = vehicles;
            this.gamePlay = gamePlay;
            this.businessManager = businessManager;
            this.weapons = weapons;
            this.companyManager = companyManager;
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
        //public GetSkinCommand getSkinCommand;
        public ApothekeCommand apothekeCommand;
        public ApothekenCommand apothekenCommand;
        public PlantCommand plantCommand;
        public PlantagenCommand plantagenCommand;
        public BusinessCommand businessCommand;
        public EquipCommand equipCommand;
        public TSLinkCommand tsLinkCommand;
        public TSUnlinkCommand tsUnlinkCommand;
        public TeamSpeak teamSpeak;
        public FDoorCommand fDoorCommand;
        public ChangeSpawnCommand changeSpawnCommand;
        public FindLaboratoryCommand findLaboratoryCommand;
        public InvSeeCommand invSeeCommand;
        public UseCommand useCommand;
        public AusziehenCommand ausziehenCommand;
        public CompanyCommand companyCommand;
        public DailyBonusCommand dailyBonusCommand;
        public WinzerCommand winzerCommand;
        public RebstockCommand rebstockCommand;
        public MuschelSammlerCommand muschelSammlerCommand;
        public GasStationCommand gasStationCommand;
        public ResetBonusEntryCommand resetBonusEntryCommand;
        public DutyCommand dutyCommand;
        public DeleteBrokenEntitiesCommand deleteBrokenEntitiesCommand;
        public AdminGiveRankCommand adminGiveRankCommand;
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
            shopCommand = new ShopCommand(playerManager, locationManager, companyManager);
            annehmenCommand = new AnnehmenCommand(utils);
            ablehnenVertrag = new AblehnenVertrag(utils);
            inviteCommand = new InviteCommand(playerManager, factionManager, utils, businessManager);
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
            dropCommand = new DropCommand(playerManager);
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
            buyHouseCommand = new BuyHouseCommand(playerManager, blockManager);
            mietersCommand = new MietersCommand(playerManager, utils);
            unrentCommand = new UnrentCommand(utils);
            friskCommand = new FriskCommand(playerManager, weapons, factionManager);
            blacklistCommand = new BlacklistCommand(playerManager, factionManager);
            npc = new NPC(playerManager);
            redeemCommand = new RedeemCommand(playerManager, utils);
            whistleCommand = new WhistleCommand(utils, playerManager);
            shoutCommand = new ShoutCommand(utils, playerManager);
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
            businessChatCommand = new BusinessChatCommand(playerManager, businessManager);
            leadBusinessCommand = new LeadBusinessCommand(playerManager, businessManager);
            frakInfoCommand = new FrakInfoCommand(factionManager);
            beziehungCommand = new BeziehungCommand(playerManager, utils);
            trennenCommand = new TrennenCommand(playerManager);
            antragCommand = new AntragCommand(playerManager, utils);
            garageCommand = new GarageCommand(locationManager, vehicles);
            aktenCommand = new AktenCommand(playerManager);
            specCommand = new SpecCommand(playerManager, adminManager);
            msgCommand = new MsgCommand(playerManager);
            leaderChatCommand = new LeaderChatCommand(playerManager, utils);
            frakStatsCommand = new FrakStatsCommand(playerManager, factionManager, utils);
            checkInvCommand = new CheckInvCommand(playerManager);
            bizInviteCommand = new BizInviteCommand(playerManager, utils, businessManager);
            shopRobCommand = new ShopRobCommand(playerManager, locationManager, factionManager);
            respawnCommand = new RespawnCommand(playerManager, adminManager, utils, locationManager);
            dealerCommand = new DealerCommand(playerManager, gamePlay, locationManager, factionManager);
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
            registerblockCommand = new RegisterblockCommand(playerManager, mySQL, blockManager);
            registerATMCommand = new RegisterATMCommand(playerManager, adminManager, mySQL);
            apothekeCommand = new ApothekeCommand(playerManager, locationManager, gamePlay);
            apothekenCommand = new ApothekenCommand(playerManager, gamePlay, factionManager);
            plantCommand = new PlantCommand(playerManager, locationManager, gamePlay);
            plantagenCommand = new PlantagenCommand(gamePlay, utils, factionManager, locationManager);
            businessCommand = new BusinessCommand(playerManager, businessManager);
            equipCommand = new EquipCommand(playerManager, factionManager, locationManager, weapons);
            tsLinkCommand = new TSLinkCommand(playerManager);
            tsUnlinkCommand = new TSUnlinkCommand(playerManager);
            //teamSpeak = new TeamSpeak(playerManager, factionManager, utils);
            fDoorCommand = new FDoorCommand(playerManager, blockManager, locationManager);
            changeSpawnCommand = new ChangeSpawnCommand(playerManager, utils);
            findLaboratoryCommand = new FindLaboratoryCommand(playerManager, utils, locationManager);
            invSeeCommand = new InvSeeCommand(playerManager);
            useCommand = new UseCommand(gamePlay);
            ausziehenCommand = new AusziehenCommand(utils);
            companyCommand = new CompanyCommand(playerManager, companyManager, locationManager);
            dailyBonusCommand = new DailyBonusCommand(playerManager, locationManager);
            winzerCommand = new WinzerCommand(playerManager, locationManager, navigation);
            rebstockCommand = new RebstockCommand(playerManager, navigation);
            muschelSammlerCommand = new MuschelSammlerCommand(playerManager, locationManager);
            gasStationCommand = new GasStationCommand(playerManager, locationManager, companyManager);
            resetBonusEntryCommand = new ResetBonusEntryCommand(playerManager, adminManager);
            dutyCommand = new DutyCommand(playerManager, factionManager, locationManager);
            deleteBrokenEntitiesCommand = new DeleteBrokenEntitiesCommand();
            adminGiveRankCommand = new AdminGiveRankCommand(playerManager);

            main.registerCommands();
            main.registerListener(this);
        }
    }
}
