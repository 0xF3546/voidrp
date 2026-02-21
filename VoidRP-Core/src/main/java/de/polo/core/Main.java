package de.polo.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import de.polo.api.Server;
import de.polo.api.utils.inventorymanager.InventoryApiRegister;
import de.polo.api.VoidAPI;
import de.polo.api.nametags.INameTagProvider;
import de.polo.core.admin.commands.*;
import de.polo.core.admin.services.InvSeeCommand;
import de.polo.core.admin.utils.ServerStats;
import de.polo.core.agreement.commands.AblehnenVertrag;
import de.polo.core.agreement.commands.AnnehmenCommand;
import de.polo.core.agreement.services.VertragUtil;
import de.polo.core.base.commands.*;
import de.polo.core.commands.*;
import de.polo.core.commands.BizInviteCommand;
import de.polo.core.commands.BusinessChatCommand;
import de.polo.core.commands.BusinessCommand;
import de.polo.core.commands.GeworbenCommand;
import de.polo.core.commands.JailtimeCommand;
import de.polo.core.database.Database;
import de.polo.core.database.impl.CoreDatabase;
import de.polo.core.infrastructure.cache.FactionCache;
import de.polo.core.infrastructure.cache.PlayerDataCache;
import de.polo.core.infrastructure.persistence.FactionFlushService;
import de.polo.core.infrastructure.persistence.FlushService;
import de.polo.core.infrastructure.persistence.HibernateConfig;
import de.polo.core.infrastructure.persistence.HibernateFactionRepository;
import de.polo.core.infrastructure.persistence.HibernatePlayerRepository;
import de.polo.core.faction.commands.*;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.game.base.CustomTabAPI;
import de.polo.core.game.base.extra.beginnerpass.Beginnerpass;
import de.polo.core.game.base.extra.seasonpass.Seasonpass;
import de.polo.core.game.base.housing.HouseManager;
import de.polo.core.game.faction.streetwar.Streetwar;
import de.polo.core.handler.CommandBase;
import de.polo.core.housing.commands.AusziehenCommand;
import de.polo.core.housing.commands.RentCommand;
import de.polo.core.jobs.commands.*;
import de.polo.core.listeners.PacketSendListener;
import de.polo.core.manager.*;
import de.polo.core.news.services.impl.NewsManager;
import de.polo.core.phone.commands.AuflegenCommand;
import de.polo.core.phone.commands.CallCommand;
import de.polo.core.player.commands.JailCommand;
import de.polo.core.player.commands.OOCCommand;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.*;
import de.polo.core.utils.gameplay.GamePlay;
import de.polo.core.utils.player.ScoreboardAPI;
import de.polo.core.utils.player.ScoreboardManager;
import dev.vansen.singleline.SingleLineOptions;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Main extends JavaPlugin implements Server {
    public static Database database;
    @Getter
    public static PlayerManager playerManager;
    @Getter
    public static Utils utils;
    public static FactionManager factionManager;
    public static ServerManager serverManager;
    public static VertragUtil vertragUtil;
    public static SupportManager supportManager;
    public static ComputerUtils computerUtils;
    @Getter
    public static HouseManager houseManager;
    public static BusinessManager businessManager;
    public static Streetwar streetwar;
    @Getter
    public static WeaponManager weaponManager;
    public static BlockManager blockManager;
    public static GamePlay gamePlay;
    public static CompanyManager companyManager;
    public static Seasonpass seasonpass;
    public static Beginnerpass beginnerpass;
    @Getter
    public static ScoreboardAPI scoreboardAPI;
    public static INameTagProvider nameTagProvider;
    public static CustomTabAPI customTabAPI;
    public static NewsManager newsManager;
    /** Shared player repository using Hibernate + Caffeine dirty-tracking. */
    public static HibernatePlayerRepository playerRepository;
    /** Shared faction repository using Hibernate + Caffeine dirty-tracking. */
    public static HibernateFactionRepository factionRepository;
    @Getter
    private static Main instance;
    private final Map<Class<? extends CommandBase>, CommandBase> commandInstances = new HashMap<>();
    private final Map<Class<?>, Object> eventInstances = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    @Getter
    public CoreDatabase coreDatabase;
    private HibernateConfig hibernateConfig;
    private FlushService flushService;
    private FactionFlushService factionFlushService;
    @Getter
    public CooldownManager cooldownManager;
    public TeamSpeak teamSpeak;
    public Commands commands;
    private ConfigurableApplicationContext springContext;
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
        startSpringBoot();
        ServerStats.setStartTime(Utils.getTime());

        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("ProtocolLib not found!");
            getLogger().severe("Please install ProtocolLib to use this plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        coreDatabase = new CoreDatabase();
        database = coreDatabase;

        // ── Hibernate + Caffeine persistence infrastructure ─────────────────
        hibernateConfig = new HibernateConfig(coreDatabase.getDataSource());
        PlayerDataCache playerDataCache = new PlayerDataCache();
        playerRepository = new HibernatePlayerRepository(hibernateConfig.getSessionFactory(), playerDataCache);
        flushService = new FlushService(playerRepository, playerDataCache, this);
        flushService.start();

        FactionCache factionDataCache = new FactionCache();
        factionRepository = new HibernateFactionRepository(hibernateConfig.getSessionFactory(), factionDataCache);
        factionFlushService = new FactionFlushService(factionRepository, factionDataCache, this);
        factionFlushService.start();

        customTabAPI = new CustomTabAPI();
        scoreboardManager = new ScoreboardManager();
        scoreboardAPI = new ScoreboardAPI(scoreboardManager);
        companyManager = new CompanyManager(coreDatabase);
        supportManager = new SupportManager(coreDatabase);
        playerManager = new PlayerManager(coreDatabase);
        cooldownManager = new CooldownManager();
        factionManager = new FactionManager(playerManager);
        blockManager = new BlockManager(coreDatabase);
        houseManager = new HouseManager(playerManager, blockManager);
        utils = new Utils(playerManager, factionManager, houseManager, companyManager);
        vertragUtil = new VertragUtil(playerManager, factionManager);
        serverManager = new ServerManager(playerManager, factionManager, utils);
        computerUtils = new ComputerUtils(playerManager, factionManager);
        businessManager = new BusinessManager(playerManager);
        streetwar = new Streetwar(playerManager, factionManager, utils);
        weaponManager = new WeaponManager(utils, playerManager);
        newsManager = new NewsManager();
        //laboratory = new Laboratory(playerManager, factionManager, locationManager);
        gamePlay = new GamePlay(playerManager, utils, coreDatabase, factionManager);
        commands = new Commands(this, playerManager, supportManager, gamePlay, businessManager, weaponManager, companyManager);
        seasonpass = new Seasonpass(playerManager, factionManager);
        beginnerpass = new Beginnerpass(playerManager, factionManager);

        initServices("de.polo.core");

        InventoryApiRegister.register(this);
        GlobalStats.load();

        PacketEvents.getAPI().getEventManager().registerListener(new PacketSendListener(playerManager, factionManager), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(Component.text("§cDer Server wurde reloaded."));
        }
        registerAnnotatedCommands();
        registerAnnotatedEvents();
        getLogger().info("§VOIDROLEPLAY ROLEPLAY STARTED.");
        try {
            Statement statement = coreDatabase.getStatement();
            statement.execute("DELETE FROM players_online");
            statement.execute("DELETE FROM bank_logs WHERE datum < DATE_SUB(NOW(), INTERVAL 7 DAY)");
            statement.execute("DELETE FROM money_logs WHERE datum < DATE_SUB(NOW(), INTERVAL 7 DAY)");
            statement.execute("DELETE FROM players_online_log WHERE time < DATE_SUB(NOW(), INTERVAL 7 DAY)");
            statement.execute("DELETE FROM phone_messages WHERE datum < DATE_SUB(NOW(), INTERVAL 14 DAY)");
            statement.execute("DELETE FROM housebans WHERE until < NOW()");
            statement.execute("DELETE FROM player_bans WHERE date < NOW() AND isPermanent = 0");
            //teamSpeak = new TeamSpeak(playerManager, factionManager, utils);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        SingleLineOptions.USE_COMPONENT_LOGGER.enabled(true);
        SingleLineOptions.USE_NORMAL_LOGGER_INSTEAD_OF_PRINT.enabled(false);
    }

    private void startSpringBoot() {
        new Thread(() -> {
            springContext = SpringApplication.run(VoidSpringApplication.class);
        }).start();
    }

    @Override
    public void onDisable() {
        if (springContext != null) {
            springContext.close();
        }
        if (gamePlay != null && gamePlay.activeDrop != null) {
            gamePlay.activeDrop.cleanup();
        }
        gamePlay.plant.cleanup();
        utils.deathUtil.cleanUpCorpses();
        System.out.println("Disabling VoidRoleplay");
        try {
            serverManager.savePlayers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (flushService != null) {
            flushService.stop();
            flushService.flushAllSync();
        }
        if (factionFlushService != null) {
            factionFlushService.stop();
            factionFlushService.flushAllSync();
        }
        if (hibernateConfig != null) {
            hibernateConfig.close();
        }
        coreDatabase.close();
        PacketEvents.getAPI().terminate();
    }

    private void registerAnnotatedCommands() {
        Set<Class<?>> allClasses = ClassScanner.findClassesWithAnnotation("de.polo.core", CommandBase.CommandMeta.class);

        // Nur die CommandBase-Klassen extrahieren
        Set<Class<? extends CommandBase>> commandClasses = new HashSet<>();
        for (Class<?> clazz : allClasses) {
            if (CommandBase.class.isAssignableFrom(clazz)) {
                // Typumwandlung in CommandBase
                commandClasses.add((Class<? extends CommandBase>) clazz);
            }
        }

        for (Class<? extends CommandBase> clazz : commandClasses) {
            CommandBase.CommandMeta meta = clazz.getAnnotation(CommandBase.CommandMeta.class);
            if (meta == null) {
                getLogger().warning("Command " + clazz.getName() + " hat keine @CommandMeta Annotation.");
                continue;
            }

            if (commandInstances.containsKey(clazz)) {
                getLogger().warning("Command " + clazz.getName() + " ist bereits registriert.");
                continue;
            }

            try {
                CommandBase command = clazz.getDeclaredConstructor(CommandBase.CommandMeta.class).newInstance(meta);
                commandInstances.put(clazz, command);

                // Befehl registrieren
                getLogger().info("Command registriert: /" + meta.name());

                if (TabCompleter.class.isAssignableFrom(clazz)) {
                    TabCompleter tabCompleter = (TabCompleter) command;
                    addTabCompleter(meta.name(), tabCompleter);
                    getLogger().info("TabCompleter für Command " + meta.name() + " registriert.");
                }

            } catch (Exception e) {
                getLogger().severe("Fehler beim Registrieren des Commands " + clazz.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public <T extends CommandBase> T getCommandInstance(Class<T> commandClass) {
        return commandClass.cast(commandInstances.get(commandClass));
    }

    private void registerAnnotatedEvents() {
        // Suche nach Klassen mit der @Event Annotation
        Set<Class<?>> eventClasses = ClassScanner.findClassesWithAnnotation("de.polo.core", Event.class);

        for (Class<?> clazz : eventClasses) {
            // Überprüfen, ob die Klasse von Listener erbt (und somit ein Event-Listener ist)
            if (!Listener.class.isAssignableFrom(clazz)) {
                getLogger().warning(clazz.getName() + " ist kein Listener.");
                continue;
            }

            if (eventInstances.containsKey(clazz)) {
                getLogger().warning(clazz.getName() + " ist bereits registriert.");
                continue;
            }

            try {
                // Instanziiere den Event-Listener
                Object eventInstance = clazz.getDeclaredConstructor().newInstance();

                // Wenn der Listener instanziiert ist, registriere ihn
                if (eventInstance instanceof Listener) {
                    Main.getInstance().getServer().getPluginManager().registerEvents((Listener) eventInstance, Main.getInstance());
                    eventInstances.put(clazz, eventInstance);
                    getLogger().info("Event registriert: " + clazz.getName());
                }

            } catch (Exception e) {
                getLogger().severe("Fehler beim Registrieren des Events " + clazz.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public <T> T getEvent(Class<T> eventClass) {
        return (T) eventInstances.get(eventClass);
    }

    @Override
    public @Nullable <T> T getBean(@NotNull final Class<T> clazz) {
        if (springContext == null) {
            return null;
        }
        return springContext.getBean(clazz);
    }

    public void initServices(String basePackage) {
        Set<Class<?>> classes = ClassScanner.findClassesWithAnnotation(basePackage, Service.class);

        for (Class<?> implClass : classes) {
            try {
                if (!Modifier.isAbstract(implClass.getModifiers())) {
                    if (services.containsKey(implClass)) {
                        System.out.println("Service " + implClass.getName() + " ist bereits registriert.");
                        continue;
                    }
                    Object instance = implClass.getDeclaredConstructor().newInstance();

                    Class<?>[] interfaces = implClass.getInterfaces();
                    if (interfaces.length == 0) {
                        services.put(implClass, instance);
                    } else {
                        for (Class<?> iface : interfaces) {
                            services.put(iface, instance);
                        }
                    }

                    System.out.println("Service registriert: " + implClass.getName());

                    services.put(implClass, instance);
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Instanziieren von Service: " + implClass.getName());
                e.printStackTrace();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(@NotNull Class<T> clazz) {
        return (T) services.get(clazz);
    }

    public class Commands {
        private final Main voidAPI;
        private final PlayerManager playerManager;
        private final SupportManager supportManager;
        private final GamePlay gamePlay;
        private final BusinessManager businessManager;
        private final WeaponManager weaponManager;
        private final CompanyManager companyManager;
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
        public ArrestCommand arrestCommand;
        public TPCommand tpCommand;
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
        public DepartmentChatCommand departmentChatCommand;
        public MemberCommand memberCommand;
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
        public MsgCommand msgCommand;
        public FrakStatsCommand frakStatsCommand;
        public CheckInvCommand checkInvCommand;
        public BizInviteCommand bizInviteCommand;
        public RespawnCommand respawnCommand;
        //public DealerCommand dealerCommand;
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

        public Commands(Main voidAPI, PlayerManager playerManager, SupportManager supportManager, GamePlay gamePlay, BusinessManager businessManager, WeaponManager weaponManager, CompanyManager companyManager) {
            this.voidAPI = voidAPI;
            this.playerManager = playerManager;
            this.supportManager = supportManager;
            this.gamePlay = gamePlay;
            this.businessManager = businessManager;
            this.weaponManager = weaponManager;
            this.companyManager = companyManager;
            Init();
        }

        private void Init() {
            minerJobCommand = new MinerJobCommand(playerManager, gamePlay, factionManager);
            undertakerCommand = new UndertakerCommand(playerManager);
            checkoutWebshopCommand = new CheckoutWebshopCommand(playerManager);
            setFactionChatColorCommand = new SetFactionChatColorCommand(playerManager, factionManager);
            roadBlockCommand = new RoadBlockCommand(factionManager, playerManager);
            sperrinfoCommand = new SperrinfoCommand(playerManager);
            cookCommand = new CookCommand(playerManager);
            registerFactionBanner = new RegisterFactionBanner(playerManager, factionManager);
            bombeCommand = new BombeCommand(playerManager, utils, factionManager);
            sprengguertelCommand = new SprengguertelCommand(playerManager, utils);
            personalausweisCommand = new PersonalausweisCommand(playerManager, utils);
            teamChatCommand = new TeamChatCommand(playerManager, utils);
            fraktionsChatCommand = new FraktionsChatCommand(playerManager, factionManager, utils);
            uninviteCommand = new UninviteCommand(playerManager, factionManager);
            locationCommand = new LocationCommand();
            setFrakCommand = new SetFrakCommand(playerManager, factionManager);
            adminUnInviteCommand = new AdminUnInviteCommand(playerManager, factionManager, utils);
            assistentchatCommand = new AssistentchatCommand(playerManager, utils);
            supportCommand = new SupportCommand(playerManager, supportManager);
            cancelSupportCommand = new CancelSupportCommand(supportManager);
            acceptTicketCommand = new AcceptTicketCommand(playerManager, supportManager, utils);
            closeTicketCommand = new CloseTicketCommand(playerManager, supportManager, utils);
            tpToCommand = new TPToCommand(playerManager);
            adminReviveCommand = new AdminReviveCommand(playerManager, utils);
            playerInfoCommand = new PlayerInfoCommand(playerManager, factionManager);
            meCommand = new MeCommand();
            broadcastCommand = new BroadcastCommand(playerManager, utils);
            govCommand = new GovCommand(playerManager, factionManager, utils);
            ticketsCommand = new TicketsCommand(playerManager, supportManager);
            teamCommand = new TeamCommand(playerManager);
            shopCommand = new ShopCommand(playerManager, companyManager);
            annehmenCommand = new AnnehmenCommand(utils);
            ablehnenVertrag = new AblehnenVertrag(utils);
            inviteCommand = new InviteCommand(playerManager, factionManager, utils, businessManager);
            rentCommand = new RentCommand(playerManager, utils);
            arrestCommand = new ArrestCommand(playerManager, factionManager, utils);
            tpCommand = new TPCommand(playerManager);
            speedCommand = new SpeedCommand(playerManager);
            kickCommand = new KickCommand(playerManager);
            oocCommand = new OOCCommand();
            pluginCommand = new PluginCommand(playerManager);
            voteCommand = new VoteCommand();
            setRankNameCommand = new SetRankNameCommand(playerManager, factionManager);
            setRankPayDayCommand = new SetRankPayDayCommand(playerManager, factionManager);
            cpCommand = new CPCommand();
            smsCommand = new SMSCommand(playerManager, utils);
            callCommand = new CallCommand(playerManager, utils);
            auflegenCommand = new AuflegenCommand(utils);
            jailtimeCommand = new JailtimeCommand(playerManager);
            dropCommand = new DropCommand(playerManager, factionManager);
            banCommand = new BanCommand(playerManager);
            unbanCommand = new UnbanCommand(playerManager);
            getVehCommand = new GetVehCommand(playerManager);
            goToVehCommand = new GoToVehCommand(playerManager);
            einreiseCommand = new EinreiseCommand(playerManager);
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
            departmentChatCommand = new DepartmentChatCommand(playerManager, factionManager);
            memberCommand = new MemberCommand(playerManager, factionManager);
            contractsCommand = new ContractsCommand(playerManager, factionManager);
            contractCommand = new ContractCommand(playerManager, factionManager);
            nachrichtenCommand = new NachrichtenCommand(playerManager);
            businessChatCommand = new BusinessChatCommand(playerManager, businessManager);
            leadBusinessCommand = new LeadBusinessCommand(playerManager, businessManager);
            frakInfoCommand = new FrakInfoCommand(factionManager);
            beziehungCommand = new BeziehungCommand(playerManager, utils);
            trennenCommand = new TrennenCommand(playerManager);
            antragCommand = new AntragCommand(playerManager, utils);
            aktenCommand = new WantedCommand(playerManager, utils);
            msgCommand = new MsgCommand(playerManager);
            frakStatsCommand = new FrakStatsCommand(playerManager, factionManager, utils);
            checkInvCommand = new CheckInvCommand(playerManager);
            bizInviteCommand = new BizInviteCommand(playerManager, utils, businessManager);
            respawnCommand = new RespawnCommand(playerManager, utils);
            //dealerCommand = new DealerCommand(playerManager, gamePlay, locationManager, factionManager);
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
            registerATMCommand = new RegisterATMCommand(playerManager);
            apothekeCommand = new ApothekeCommand(playerManager, gamePlay);
            apothekenCommand = new ApothekenCommand(playerManager, gamePlay, factionManager);
            businessCommand = new BusinessCommand(playerManager, businessManager);
            equipCommand = new EquipCommand(playerManager, factionManager, weaponManager);
            tsLinkCommand = new TSLinkCommand(playerManager);
            tsUnlinkCommand = new TSUnlinkCommand(playerManager);
            //teamSpeak = new TeamSpeak(playerManager, factionManager, utils);
            fDoorCommand = new FDoorCommand(playerManager, blockManager);
            changeSpawnCommand = new ChangeSpawnCommand(playerManager, utils);
            //findLaboratoryCommand = new FindLaboratoryCommand(playerManager, utils, locationManager);
            invSeeCommand = new InvSeeCommand(playerManager);
            useCommand = new UseCommand(gamePlay);
            ausziehenCommand = new AusziehenCommand(utils);
            companyCommand = new CompanyCommand(playerManager, companyManager);
            dailyBonusCommand = new DailyBonusCommand(playerManager);
            winzerCommand = new WinzerCommand();
            rebstockCommand = new RebstockCommand(playerManager);
            muschelSammlerCommand = new MuschelSammlerCommand(playerManager);
            gasStationCommand = new GasStationCommand(playerManager, companyManager);
            resetBonusEntryCommand = new ResetBonusEntryCommand(playerManager);
            dutyCommand = new DutyCommand(playerManager, factionManager);
            deleteBrokenEntitiesCommand = new DeleteBrokenEntitiesCommand();
            adminGiveRankCommand = new AdminGiveRankCommand(playerManager);
            subGroupCommand = new SubGroupCommand(playerManager, factionManager);
            subGroupChatCommand = new SubGroupChatCommand(playerManager, factionManager);
            permbanCommand = new PermbanCommand(playerManager);
            flyCommand = new FlyCommand(playerManager);
            workstationCommand = new WorkstationCommand(playerManager);
            weaponcrafterCommand = new WeaponcrafterCommand(playerManager);
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
            auktionCommand = new AuktionCommand(playerManager, factionManager);
            fixBlockCommand = new FixBlockCommand(playerManager, blockManager);
            pfeifenTransport = new PfeifenTransport(playerManager, factionManager);
            blacklistReasonsCommand = new BlacklistReasonsCommand(playerManager, factionManager);
            modifyBlacklistCommand = new ModifyBlacklistCommand(playerManager, factionManager);
            autoBlacklistCommand = new AutoBlacklistCommand(playerManager, factionManager);
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
            giveLeaderRechteCommand = new GiveLeaderRechteCommand(playerManager, factionManager);
            removeLeaderRechteCommand = new RemoveLeaderRechteCommand(playerManager, factionManager);
            gwdCommand = new GwdCommand(playerManager, factionManager);
            zdCommand = new ZDCommand(playerManager, factionManager);
        }
    }
}
