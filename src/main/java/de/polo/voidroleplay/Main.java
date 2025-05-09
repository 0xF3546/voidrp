package de.polo.voidroleplay;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import de.polo.api.nametags.INameTagProvider;
import de.polo.voidroleplay.admin.commands.*;
import de.polo.voidroleplay.admin.services.AdminService;
import de.polo.voidroleplay.admin.services.InvSeeCommand;
import de.polo.voidroleplay.admin.services.SupportService;
import de.polo.voidroleplay.admin.services.impl.AdminManager;
import de.polo.voidroleplay.admin.services.impl.CoreAdminService;
import de.polo.voidroleplay.admin.services.impl.CoreSupportService;
import de.polo.voidroleplay.agreement.commands.AblehnenVertrag;
import de.polo.voidroleplay.agreement.commands.AnnehmenCommand;
import de.polo.voidroleplay.agreement.commands.VertragCommand;
import de.polo.voidroleplay.agreement.services.AgreementService;
import de.polo.voidroleplay.agreement.services.VertragUtil;
import de.polo.voidroleplay.agreement.services.impl.CoreAgreementService;
import de.polo.voidroleplay.base.commands.*;
import de.polo.voidroleplay.commands.*;
import de.polo.voidroleplay.commands.BizInviteCommand;
import de.polo.voidroleplay.commands.BusinessChatCommand;
import de.polo.voidroleplay.commands.BusinessCommand;
import de.polo.voidroleplay.commands.GeworbenCommand;
import de.polo.voidroleplay.commands.HealthInsuranceCommand;
import de.polo.voidroleplay.commands.InvCommand;
import de.polo.voidroleplay.commands.JailCommand;
import de.polo.voidroleplay.commands.JailtimeCommand;
import de.polo.voidroleplay.database.Database;
import de.polo.voidroleplay.database.impl.CoreDatabase;
import de.polo.voidroleplay.events.christmas.commands.AdventskalenderCommand;
import de.polo.voidroleplay.events.collect.commands.WarpEventCommand;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.faction.commands.*;
import de.polo.voidroleplay.faction.service.FactionService;
import de.polo.voidroleplay.faction.service.impl.CoreFactionService;
import de.polo.voidroleplay.game.base.CustomTabAPI;
import de.polo.voidroleplay.game.base.extra.beginnerpass.Beginnerpass;
import de.polo.voidroleplay.game.base.extra.seasonpass.Seasonpass;
import de.polo.voidroleplay.game.base.farming.Farming;
import de.polo.voidroleplay.game.base.housing.HouseManager;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.game.faction.streetwar.Streetwar;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.housing.commands.AusziehenCommand;
import de.polo.voidroleplay.housing.commands.RentCommand;
import de.polo.voidroleplay.housing.commands.SellHouseCommand;
import de.polo.voidroleplay.housing.services.HouseService;
import de.polo.voidroleplay.housing.services.impl.CoreHouseService;
import de.polo.voidroleplay.jobs.commands.*;
import de.polo.voidroleplay.listeners.*;
import de.polo.voidroleplay.location.services.LocationService;
import de.polo.voidroleplay.location.services.NavigationService;
import de.polo.voidroleplay.location.services.impl.CoreLocationService;
import de.polo.voidroleplay.location.services.impl.CoreNavigationService;
import de.polo.voidroleplay.location.services.impl.LocationManager;
import de.polo.voidroleplay.manager.*;
import de.polo.voidroleplay.news.services.NewsService;
import de.polo.voidroleplay.news.services.impl.CoreNewsService;
import de.polo.voidroleplay.news.services.impl.NewsManager;
import de.polo.voidroleplay.phone.commands.AuflegenCommand;
import de.polo.voidroleplay.phone.commands.CallCommand;
import de.polo.voidroleplay.player.commands.NeulingschatCommand;
import de.polo.voidroleplay.player.commands.OOCCommand;
import de.polo.voidroleplay.player.commands.SellBookCommand;
import de.polo.voidroleplay.player.commands.SellDrugCommand;
import de.polo.voidroleplay.player.services.PlayerService;
import de.polo.voidroleplay.player.services.impl.CorePlayerService;
import de.polo.voidroleplay.player.services.impl.PlayerManager;
import de.polo.voidroleplay.utils.inventory.InventoryApiRegister;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.gameplay.GamePlay;
import de.polo.voidroleplay.utils.player.ScoreboardAPI;
import de.polo.voidroleplay.utils.player.ScoreboardManager;
import dev.vansen.singleline.SingleLineOptions;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public final class Main extends JavaPlugin {

    public static Database database;
    public static PlayerService playerService;
    public static AdminService adminService;
    public static SupportService supportService;
    public static FactionService factionService;
    public static HouseService houseService;
    public static NewsService newsService;
    public static AgreementService agreementService;
    public static LocationService locationService;
    public static NavigationService navigationService;
    @Getter
    public static PlayerManager playerManager;
    @Getter
    public static Utils utils;
    public static AdminManager adminManager;
    public static FactionManager factionManager;
    public static ServerManager serverManager;
    public static LocationManager locationManager;
    public static VertragUtil vertragUtil;
    public static SupportManager supportManager;
    public static ComputerUtils computerUtils;
    @Getter
    public static HouseManager houseManager;
    public static BusinessManager businessManager;
    public static Vehicles vehicles;
    @Getter
    public static WeaponManager weaponManager;
    public static BlockManager blockManager;
    public static GamePlay gamePlay;
    public static CompanyManager companyManager;
    public static Seasonpass seasonpass;
    public static Beginnerpass beginnerpass;
    public static INameTagProvider nameTagProvider;
    public static CustomTabAPI customTabAPI;
    public static NewsManager newsManager;
    @Getter
    private static Main instance;
    @Getter
    public CoreDatabase coreDatabase;
    @Getter
    public CooldownManager cooldownManager;
    public TeamSpeak teamSpeak;
    public Commands commands;
    public Streetwar streetwar;
    @Getter
    private ScoreboardAPI scoreboardAPI;
    private ScoreboardManager scoreboardManager;

    public static void registerCommand(String command, CommandExecutor c) {
        org.bukkit.command.PluginCommand cmd = instance.getCommand(command);
        if (cmd != null) {
            cmd.setExecutor(c);
        }
    }

    public static void addTabCompleter(String command, TabCompleter c) {
        org.bukkit.command.PluginCommand cmd = instance.getCommand(command);
        if (cmd != null) {
            cmd.setTabCompleter(c);
        }
    }

    public static void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, instance);
    }

    @Override
    public void onLoad() {
        instance = this;
        VoidAPI.setPlugin(this);
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {

        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("ProtocolLib not found!");
            getLogger().severe("Please install ProtocolLib to use this plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        coreDatabase = new CoreDatabase();
        database = coreDatabase;
        factionService = new CoreFactionService();
        playerService = new CorePlayerService();
        houseService = new CoreHouseService();
        newsService = new CoreNewsService();
        supportService = new CoreSupportService();
        adminService = new CoreAdminService();
        agreementService = new CoreAgreementService();
        locationService = new CoreLocationService();
        navigationService = new CoreNavigationService();

        customTabAPI = new CustomTabAPI();
        scoreboardManager = new ScoreboardManager();
        scoreboardAPI = new ScoreboardAPI(scoreboardManager);
        companyManager = new CompanyManager(coreDatabase);
        supportManager = new SupportManager(coreDatabase);
        playerManager = new PlayerManager(coreDatabase);
        cooldownManager = new CooldownManager();
        locationManager = new LocationManager(coreDatabase);
        adminManager = new AdminManager(playerManager, scoreboardAPI);
        factionManager = new FactionManager(playerManager);
        blockManager = new BlockManager(coreDatabase);
        houseManager = new HouseManager(playerManager, blockManager, locationManager);
        utils = new Utils(playerManager, adminManager, factionManager, locationManager, houseManager, companyManager);
        vehicles = new Vehicles(playerManager, locationManager);
        vertragUtil = new VertragUtil(playerManager, factionManager, adminManager);
        serverManager = new ServerManager(playerManager, factionManager, utils, locationManager);
        computerUtils = new ComputerUtils(playerManager, factionManager);
        businessManager = new BusinessManager(playerManager);
        streetwar = new Streetwar(playerManager, factionManager, utils);
        weaponManager = new WeaponManager(utils, playerManager);
        newsManager = new NewsManager();
        //laboratory = new Laboratory(playerManager, factionManager, locationManager);
        gamePlay = new GamePlay(playerManager, utils, coreDatabase, factionManager, locationManager);
        commands = new Commands(this, playerManager, adminManager, locationManager, supportManager, vehicles, gamePlay, businessManager, weaponManager, companyManager);
        seasonpass = new Seasonpass(playerManager, factionManager);
        beginnerpass = new Beginnerpass(playerManager, factionManager);
        new InventoryApiRegister(this);
        GlobalStats.load();

        PacketEvents.getAPI().getEventManager().registerListener(new PacketSendListener(playerManager, factionManager), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("§cDer Server wurde reloaded.");
        }
        registerAnnotatedCommands();
        getLogger().info("§VOIDROLEPLAY ROLEPLAY STARTED.");
        try {
            Statement statement = coreDatabase.getStatement();
            statement.execute("DELETE FROM bank_logs WHERE datum < DATE_SUB(NOW(), INTERVAL 7 DAY)");
            statement.execute("DELETE FROM money_logs WHERE datum < DATE_SUB(NOW(), INTERVAL 7 DAY)");
            statement.execute("DELETE FROM phone_messages WHERE datum < DATE_SUB(NOW(), INTERVAL 14 DAY)");
            statement.execute("DELETE FROM housebans WHERE until > NOW()");
            //teamSpeak = new TeamSpeak(playerManager, factionManager, utils);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        SingleLineOptions.USE_COMPONENT_LOGGER.enabled(true);
        SingleLineOptions.USE_NORMAL_LOGGER_INSTEAD_OF_PRINT.enabled(false);
    }

    private void registerListener(Commands commands) {

        new JoinListener(playerManager, adminManager, utils, locationManager, serverManager);
        new QuitListener(playerManager, adminManager, utils, commands, serverManager, supportManager, scoreboardAPI);
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
        new ItemDropListener(weaponManager, playerManager);
        new PlayerPickUpArrowListener();
        new PlayerVoteListener(playerManager, adminManager);
        new RespawnListener(playerManager);
        new PlayerMoveListener(playerManager, utils);
        new PlayerLoginListener();
        new PlayerInteractListener(playerManager, utils, commands, blockManager, factionManager);
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
        new PlayerSpawnpointListener();
        new GameModeChangeEvent();
        new EntitySpawnListener();
        new EntityDamageByEntityListener(playerManager);
        new ExplosionListener();
        new ConsumeListener(playerManager);
        new EntityToggleGlideListener();
        new JumpListener(playerManager);

    }

    private void registerCommands() {
        getCommand("aduty").setExecutor(adminManager);
    }

    @Override
    public void onDisable() {
        if (gamePlay != null && gamePlay.activeDrop != null) {
            gamePlay.activeDrop.cleanup();
        }
        gamePlay.plant.cleanup();
        Main.getInstance().utils.deathUtil.cleanUpCorpses();
        System.out.println("Disabling VoidRoleplay");
        try {
            serverManager.savePlayers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        coreDatabase.close();
        PacketEvents.getAPI().terminate();
    }

    private void registerAnnotatedCommands() {
        Set<Class<? extends CommandBase>> commands = Set.of(
                InvCommand.class,
                HochseefischerCommand.class,
                CatchFishCommand.class,
                AdventskalenderCommand.class,
                EquiplogCommand.class,
                TreuebonusCommand.class,
                SellDrugCommand.class,
                GivePrescriptionCommand.class,
                UnarrestCommand.class,
                NeulingschatCommand.class,
                SignBookCommand.class,
                PremiumCommand.class,
                UranMineCommand.class,
                SellBookCommand.class,
                BibliothekCommand.class,
                MegaphoneCommand.class,
                CheckResultCommand.class,
                WarpEventCommand.class,
                BottletransportCommand.class,
                ModifyWantedsCommand.class,
                GeldlieferantCommand.class,
                HealthInsuranceCommand.class,
                LicensesCommand.class,
                GiveLicenseCommand.class,
                FahrschuleCommand.class,
                TakeLicenseCommand.class,
                AnwaltCommand.class,
                MaklerCommand.class,
                SellHouseCommand.class,
                VertragCommand.class,
                SewerCleanerCommand.class
        );


        for (Class<? extends CommandBase> commandClass : commands) {
            CommandBase.CommandMeta meta = commandClass.getAnnotation(CommandBase.CommandMeta.class);

            if (meta == null) {
                getLogger().warning("Command " + commandClass.getName() + " hat keine @CommandMeta Annotation.");
                continue;
            }

            try {
                CommandBase command = commandClass.getDeclaredConstructor(CommandBase.CommandMeta.class)
                        .newInstance(meta);
                getLogger().info("Befehl registriert: /" + meta.name());
            } catch (Exception e) {
                getLogger().severe("Fehler beim Registrieren des Befehls " + commandClass.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public class Commands {
        private final Main voidAPI;
        private final PlayerManager playerManager;
        private final AdminManager adminManager;
        private final LocationManager locationManager;
        private final SupportManager supportManager;
        private final Vehicles vehicles;
        private final GamePlay gamePlay;
        private final BusinessManager businessManager;
        private final WeaponManager weaponManager;
        private final CompanyManager companyManager;
        public SetTeamCommand setTeamCommand;
        public GeldbeutelCommand geldbeutelCommand;
        public PersonalausweisCommand personalausweisCommand;
        public TeamChatCommand teamChatCommand;
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
        public MineCommand mineCommand;
        public ArrestCommand arrestCommand;
        public TPCommand tpCommand;
        public TPHereCommand tpHereCommand;
        public SpeedCommand speedCommand;
        public KickCommand kickCommand;
        public OOCCommand oocCommand;
        public PluginCommand pluginCommand;
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
        public GetVehCommand getVehCommand;
        public GoToVehCommand goToVehCommand;
        public EinreiseCommand einreiseCommand;
        public RegisterHouseCommand registerHouseCommand;
        public ReinforcementCommand reinforcementCommand;
        public BuyHouseCommand buyHouseCommand;
        public MietersCommand mietersCommand;
        public UnrentCommand unrentCommand;
        public FriskCommand friskCommand;
        public BlacklistCommand blacklistCommand;
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
        public WantedCommand aktenCommand;
        public SpecCommand specCommand;
        public MsgCommand msgCommand;
        public LeaderChatCommand leaderChatCommand;
        public FrakStatsCommand frakStatsCommand;
        public CheckInvCommand checkInvCommand;
        public BizInviteCommand bizInviteCommand;
        public ShopRobCommand shopRobCommand;
        public RespawnCommand respawnCommand;
        //public DealerCommand dealerCommand;
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
        public BusinessCommand businessCommand;
        public EquipCommand equipCommand;
        public TSLinkCommand tsLinkCommand;
        public TSUnlinkCommand tsUnlinkCommand;
        public TeamSpeak teamSpeak;
        public FDoorCommand fDoorCommand;
        public ChangeSpawnCommand changeSpawnCommand;
        //
        // public FindLaboratoryCommand findLaboratoryCommand;
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
        public SubGroupCommand subGroupCommand;
        public SubGroupChatCommand subGroupChatCommand;
        public PermbanCommand permbanCommand;
        public FlyCommand flyCommand;
        public WorkstationCommand workstationCommand;
        public WeaponcrafterCommand weaponcrafterCommand;
        public SperrzoneCommand sperrzoneCommand;
        public SperrzonenCommand sperrzonenCommand;
        public FAQCommand faqCommand;
        public KarmaCommand karmaCommand;
        public ForceDropCommand forceDropCommand;
        public BlacklistsCommand blacklistsCommand;
        public JailCommand jailCommand;
        public TicketCommand ticketCommand;
        public AkteCommand akteCommand;
        public GottesdienstCommand gottesdienstCommand;
        public ChurchEventCommand eventCommand;
        public JesusKreuzCommand jesusKreuzCommand;
        public MarryCommand marryCommand;
        public SegenCommand segenCommand;
        public KirchensteuerCommand kirchensteuerCommand;
        public TaufeCommand taufeCommand;
        public WüfelnCommand wüfelnCommand;
        public OpenInvCommand openInvCommand;
        public GeworbenCommand geworbenCommand;
        public KickSecondaryTeam kickSecondaryTeam;
        public AuktionCommand auktionCommand;
        public FixBlockCommand fixBlockCommand;
        public PfeifenTransport pfeifenTransport;
        public BlacklistReasonsCommand blacklistReasonsCommand;
        public ModifyBlacklistCommand modifyBlacklistCommand;
        public AutoBlacklistCommand autoBlacklistCommand;
        public AFKCommand afkCommand;
        public SettingsCommand settingsCommand;
        public SecondaryTeamInfoCommand secondaryTeamInfoCommand;
        public SubTeamCommand subTeamCommand;
        public SubTeamChat subTeamChat;
        public SetMOTDCommand setMOTDCommand;
        public VoteShopCommand voteShopCommand;
        public SprengguertelCommand sprengguertelCommand;
        public BombeCommand bombeCommand;
        public CookCommand cookCommand;
        public RegisterFactionBanner registerFactionBanner;
        public SperrinfoCommand sperrinfoCommand;
        public RoadBlockCommand roadBlockCommand;
        public SetFactionChatColorCommand setFactionChatColorCommand;
        public CheckoutWebshopCommand checkoutWebshopCommand;
        public UndertakerCommand undertakerCommand;
        public MinerJobCommand minerJobCommand;
        public ClearCommand clearCommand;
        public WeaponInfoCommand weaponInfoCommand;
        public AsuCommand asuCommand;
        public WantedInfoCommand wantedInfoCommand;
        public EquipTransportCommand equipTransportCommand;
        public GiveLeaderRechteCommand giveLeaderRechteCommand;
        public RemoveLeaderRechteCommand removeLeaderRechteCommand;
        public GwdCommand gwdCommand;
        public ZDCommand zdCommand;

        public Commands(Main voidAPI, PlayerManager playerManager, AdminManager adminManager, LocationManager locationManager, SupportManager supportManager, Vehicles vehicles, GamePlay gamePlay, BusinessManager businessManager, WeaponManager weaponManager, CompanyManager companyManager) {
            this.voidAPI = voidAPI;
            this.playerManager = playerManager;
            this.adminManager = adminManager;
            this.locationManager = locationManager;
            this.supportManager = supportManager;
            this.vehicles = vehicles;
            this.gamePlay = gamePlay;
            this.businessManager = businessManager;
            this.weaponManager = weaponManager;
            this.companyManager = companyManager;
            Init();
        }

        private void Init() {
            minerJobCommand = new MinerJobCommand(playerManager, gamePlay, locationManager, factionManager);
            undertakerCommand = new UndertakerCommand(playerManager, locationManager);
            checkoutWebshopCommand = new CheckoutWebshopCommand(playerManager);
            setFactionChatColorCommand = new SetFactionChatColorCommand(playerManager, factionManager);
            roadBlockCommand = new RoadBlockCommand(factionManager, playerManager);
            sperrinfoCommand = new SperrinfoCommand(playerManager);
            cookCommand = new CookCommand(playerManager);
            registerFactionBanner = new RegisterFactionBanner(playerManager, factionManager);
            bombeCommand = new BombeCommand(playerManager, utils, factionManager);
            sprengguertelCommand = new SprengguertelCommand(playerManager, utils);
            setTeamCommand = new SetTeamCommand(playerManager, adminManager);
            geldbeutelCommand = new GeldbeutelCommand(playerManager);
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
            acceptTicketCommand = new AcceptTicketCommand(playerManager, adminManager, supportManager, utils);
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
            mineCommand = new MineCommand(playerManager, locationManager);
            arrestCommand = new ArrestCommand(playerManager, factionManager, utils);
            tpCommand = new TPCommand(playerManager, adminManager);
            tpHereCommand = new TPHereCommand(playerManager, adminManager);
            speedCommand = new SpeedCommand(playerManager);
            kickCommand = new KickCommand(playerManager);
            oocCommand = new OOCCommand();
            pluginCommand = new PluginCommand(playerManager);
            voteCommand = new VoteCommand();
            setRankNameCommand = new SetRankNameCommand(playerManager, adminManager, factionManager);
            setRankPayDayCommand = new SetRankPayDayCommand(playerManager, adminManager, factionManager);
            cpCommand = new CPCommand();
            smsCommand = new SMSCommand(playerManager, utils);
            callCommand = new CallCommand(playerManager, utils);
            auflegenCommand = new AuflegenCommand(utils);
            jailtimeCommand = new JailtimeCommand(playerManager);
            dropCommand = new DropCommand(playerManager, factionManager);
            lebensmittelLieferantCommand = new LebensmittelLieferantCommand(playerManager, locationManager);
            banCommand = new BanCommand(playerManager, adminManager);
            unbanCommand = new UnbanCommand(playerManager, adminManager);
            getVehCommand = new GetVehCommand(playerManager);
            goToVehCommand = new GoToVehCommand(playerManager);
            einreiseCommand = new EinreiseCommand(playerManager, locationManager);
            registerHouseCommand = new RegisterHouseCommand(playerManager);
            reinforcementCommand = new ReinforcementCommand(playerManager, factionManager);
            buyHouseCommand = new BuyHouseCommand(playerManager, blockManager);
            mietersCommand = new MietersCommand(playerManager, utils);
            unrentCommand = new UnrentCommand(utils);
            friskCommand = new FriskCommand(playerManager, weaponManager, factionManager);
            blacklistCommand = new BlacklistCommand(playerManager, factionManager);
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
            departmentChatCommand = new DepartmentChatCommand(playerManager, factionManager);
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
            aktenCommand = new WantedCommand(playerManager, utils);
            specCommand = new SpecCommand(playerManager, adminManager);
            msgCommand = new MsgCommand(playerManager);
            leaderChatCommand = new LeaderChatCommand(playerManager, utils);
            frakStatsCommand = new FrakStatsCommand(playerManager, factionManager, utils);
            checkInvCommand = new CheckInvCommand(playerManager);
            bizInviteCommand = new BizInviteCommand(playerManager, utils, businessManager);
            shopRobCommand = new ShopRobCommand(playerManager, locationManager, factionManager);
            respawnCommand = new RespawnCommand(playerManager, adminManager, utils, locationManager);
            //dealerCommand = new DealerCommand(playerManager, gamePlay, locationManager, factionManager);
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
            registerblockCommand = new RegisterblockCommand(playerManager, blockManager);
            registerATMCommand = new RegisterATMCommand(playerManager, adminManager);
            apothekeCommand = new ApothekeCommand(playerManager, locationManager, gamePlay);
            apothekenCommand = new ApothekenCommand(playerManager, gamePlay, factionManager);
            businessCommand = new BusinessCommand(playerManager, businessManager);
            equipCommand = new EquipCommand(playerManager, factionManager, locationManager, weaponManager);
            tsLinkCommand = new TSLinkCommand(playerManager);
            tsUnlinkCommand = new TSUnlinkCommand(playerManager);
            //teamSpeak = new TeamSpeak(playerManager, factionManager, utils);
            fDoorCommand = new FDoorCommand(playerManager, blockManager, locationManager);
            changeSpawnCommand = new ChangeSpawnCommand(playerManager, utils);
            //findLaboratoryCommand = new FindLaboratoryCommand(playerManager, utils, locationManager);
            invSeeCommand = new InvSeeCommand(playerManager);
            useCommand = new UseCommand(gamePlay);
            ausziehenCommand = new AusziehenCommand(utils);
            companyCommand = new CompanyCommand(playerManager, companyManager, locationManager);
            dailyBonusCommand = new DailyBonusCommand(playerManager, locationManager);
            winzerCommand = new WinzerCommand();
            rebstockCommand = new RebstockCommand(playerManager);
            muschelSammlerCommand = new MuschelSammlerCommand(playerManager, locationManager);
            gasStationCommand = new GasStationCommand(playerManager, locationManager, companyManager);
            resetBonusEntryCommand = new ResetBonusEntryCommand(playerManager, adminManager);
            dutyCommand = new DutyCommand(playerManager, factionManager, locationManager);
            deleteBrokenEntitiesCommand = new DeleteBrokenEntitiesCommand();
            adminGiveRankCommand = new AdminGiveRankCommand(playerManager);
            subGroupCommand = new SubGroupCommand(playerManager, factionManager, locationManager);
            subGroupChatCommand = new SubGroupChatCommand(playerManager, factionManager);
            permbanCommand = new PermbanCommand(playerManager, adminManager);
            flyCommand = new FlyCommand(playerManager, adminManager);
            workstationCommand = new WorkstationCommand(playerManager);
            weaponcrafterCommand = new WeaponcrafterCommand(locationManager, playerManager);
            sperrzoneCommand = new SperrzoneCommand();
            sperrzonenCommand = new SperrzonenCommand(playerManager);
            faqCommand = new FAQCommand();
            karmaCommand = new KarmaCommand(playerManager);
            forceDropCommand = new ForceDropCommand(playerManager);
            blacklistsCommand = new BlacklistsCommand(factionManager);
            jailCommand = new JailCommand(playerManager);
            ticketCommand = new TicketCommand(playerManager, supportManager);
            akteCommand = new AkteCommand(playerManager);
            gottesdienstCommand = new GottesdienstCommand(playerManager, factionManager);
            eventCommand = new ChurchEventCommand(playerManager, factionManager);
            jesusKreuzCommand = new JesusKreuzCommand(playerManager, factionManager);
            marryCommand = new MarryCommand(playerManager);
            segenCommand = new SegenCommand(playerManager, factionManager);
            kirchensteuerCommand = new KirchensteuerCommand(playerManager);
            taufeCommand = new TaufeCommand(playerManager, factionManager);
            wüfelnCommand = new WüfelnCommand();
            openInvCommand = new OpenInvCommand(playerManager);
            geworbenCommand = new GeworbenCommand();
            kickSecondaryTeam = new KickSecondaryTeam(playerManager);
            auktionCommand = new AuktionCommand(playerManager, factionManager, locationManager);
            fixBlockCommand = new FixBlockCommand(playerManager, blockManager);
            pfeifenTransport = new PfeifenTransport(playerManager, factionManager, locationManager);
            blacklistReasonsCommand = new BlacklistReasonsCommand(playerManager, factionManager);
            modifyBlacklistCommand = new ModifyBlacklistCommand(playerManager, factionManager);
            autoBlacklistCommand = new AutoBlacklistCommand(playerManager, factionManager);
            afkCommand = new AFKCommand(utils);
            settingsCommand = new SettingsCommand(playerManager);
            secondaryTeamInfoCommand = new SecondaryTeamInfoCommand();
            subTeamCommand = new SubTeamCommand(playerManager, factionManager);
            subTeamChat = new SubTeamChat(playerManager);
            setMOTDCommand = new SetMOTDCommand(playerManager, factionManager);
            voteShopCommand = new VoteShopCommand(playerManager);
            clearCommand = new ClearCommand(playerManager, factionManager, utils);
            asuCommand = new AsuCommand(playerManager, utils);
            weaponInfoCommand = new WeaponInfoCommand(playerManager);
            wantedInfoCommand = new WantedInfoCommand(playerManager, utils);
            equipTransportCommand = new EquipTransportCommand(playerManager);
            giveLeaderRechteCommand = new GiveLeaderRechteCommand(playerManager, factionManager, adminManager);
            removeLeaderRechteCommand = new RemoveLeaderRechteCommand(playerManager, factionManager, adminManager);
            gwdCommand = new GwdCommand(playerManager, adminManager, factionManager);
            zdCommand = new ZDCommand(playerManager, adminManager, factionManager);

            voidAPI.registerCommands();
            voidAPI.registerListener(this);
        }
    }
}
