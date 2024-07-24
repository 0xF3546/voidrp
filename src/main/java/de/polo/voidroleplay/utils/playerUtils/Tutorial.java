package de.polo.voidroleplay.utils.playerUtils;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.game.events.NaviReachEvent;
import de.polo.voidroleplay.utils.Navigation;
import de.polo.voidroleplay.utils.PlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.sql.Statement;

public class Tutorial implements Listener {
    private final PlayerManager playerManager;
    private final Navigation navigation;

    public Tutorial(PlayerManager playerManager, Navigation navigation) {
        this.playerManager = playerManager;
        this.navigation = navigation;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    public void start(Player player) {
        player.sendMessage("§8[§9Tutorial§8]§7 Willkommen im Tutorial.");
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFirstname() != null && playerData.getLastname() != null) {
            createdAusweis(player);
            return;
        }
        ;
        PlayerTutorial playerTutorial = PlayerTutorial.getPlayerTutorial(playerData);
        if (playerTutorial == null) return;
        playerTutorial.setStage(1);
        Main.waitSeconds(4, () -> {
            player.sendMessage("§8[§9Tutorial§8]§7 Als erstes musst du dir einen Personalausweis erstellen. Gehe dazu in die Stadthalle, folge dazu einfach der Route!");
            navigation.createNaviByCord(player, 133, 72, 157);
        });
    }

    public void createdAusweis(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        PlayerTutorial playerTutorial = PlayerTutorial.getPlayerTutorial(playerData);
        if (playerTutorial == null) return;
        if (playerTutorial.getStage() != 1) {
            usedAusweis(player);
            return;
        };
        playerTutorial.setStage(2);
        player.sendMessage("§8[§9Tutorial§8]§7 Sehr gut gemacht!");
        Main.waitSeconds(1, () -> player.sendMessage("§8[§9Tutorial§8]§7 Nutze §8/§epersonalausweis§7 um deinen Personalausweis anzusehen."));
    }

    public void usedAusweis(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        PlayerTutorial playerTutorial = PlayerTutorial.getPlayerTutorial(playerData);
        if (playerTutorial == null) return;
        if (playerTutorial.getStage() != 2) {
            supportMessage(player);
            return;
        }
        playerTutorial.setStage(3);
        player.sendMessage("§8[§9Tutorial§8]§7 Ich denke es ist alles verständlich, außer die §eVisumstufe§7?");
        Main.waitSeconds(4, () -> {
            player.sendMessage("§8[§9Tutorial§8]§7 Dein §eVisum§7 gibt an wie lang du schon auf Void Roleplay bist.");
            Main.waitSeconds(4, () -> {
                TextComponent one = new TextComponent("§8[§9Tutorial§8]§7 Mehr über das §eVisumsystem§7 findest du ");
                TextComponent mcserverlist = new TextComponent("§ehier§7.");
                mcserverlist.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/forum/main/index.php?thread/4-visum-levelsystem/"));
                player.spigot().sendMessage(one, mcserverlist);
                Main.waitSeconds(3, () -> {
                    player.sendMessage("§b   Info:§f Wenn du nun Shift + F drückst an der Stadthalle (Sneak und Item-Wechsel), öffnet sich ein Lager in welchem du Wertgegenstände lagern kannst.");
                    Main.waitSeconds(3, () -> {
                        navigation.createNaviByLocation(player, "Shop-1");
                        player.sendMessage("§8[§9Tutorial§8]§7 Begib dich nun zum Shop. (/navi [Shop] Stadthalle)");
                    });
                });
            });
        });

    }

    public void supportMessage(Player player) {
        TextComponent one = new TextComponent("§8[§9Tutorial§8]§7 Es ist wichtig, dass du dir das ");
        TextComponent mcserverlist = new TextComponent("§eRegelwerk§7 ");
        TextComponent two = new TextComponent("§7durchliest.");
        mcserverlist.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/forum/main/index.php?thread/3-ingame-regelwerk/&postID=3#post3"));
        player.spigot().sendMessage(one, mcserverlist);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        PlayerTutorial playerTutorial = PlayerTutorial.getPlayerTutorial(playerData);
        if (playerTutorial == null) return;
        if (playerTutorial.getStage() != 3) {
            endTutorial(player);
            return;
        }
        ;
        playerTutorial.setStage(4);
        Main.waitSeconds(4, () -> {
            player.sendMessage("§8[§9Tutorial§8]§7 Solltest du noch §eFragen §7oder andere §eAnliegen§7 haben, nutze §8/§esupport [Anliegen]§7.");
            Main.waitSeconds(4, () -> {
                player.sendMessage("§8[§9Tutorial§8]§7 Beachte jedoch, dass auch ein §eTeammitglied§7 anwesend sein muss, um dein §eTicket§7 zu bearbeiten. (§8/§eteam§7)");
                Main.waitSeconds(4, () -> {
                    TextComponent a = new TextComponent("§8[§9Tutorial§8]§7 Sollte keins anwesend sein, reiche dein §eTicket§7 im ");
                    TextComponent b = new TextComponent("§eForum§7 ");
                    TextComponent c = new TextComponent("§7ein.");
                    b.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/forum/main/"));
                    player.spigot().sendMessage(a, b, c);
                    Main.waitSeconds(4, () -> endTutorial(player));
                });
            });
        });
    }

    public void endTutorial(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        PlayerTutorial playerTutorial = PlayerTutorial.getPlayerTutorial(playerData);
        playerData.setVariable("tutorial", null);
        player.sendMessage("§8[§9Tutorial§8]§7 Du hast das §9Tutorial§a erfolgreich§7 beendeet.");
        player.sendMessage("§8[§bInfo§8]§7 Solltest du noch mehr über den Server wissen wollen, komm auf unseren §9Discord§7 oder schau auf §cYouTube§7 vorbei.");
        player.sendMessage("§8       ➥ §8/§9discord§7 & §8/§cyoutube");
        player.sendMessage("§b   Info:§f Nutze §e/neulingspass§f um mehr Einblick in den Server zu erhalten!");
        playerManager.addExp(player, 30);
        if (playerTutorial == null) return;
        playerTutorial.end();
    }

    @EventHandler
    public void onNaviReach(NaviReachEvent event) {
        if (event.getPlayerData().getVariable("tutorial") != null) {
            Player player = event.getPlayer();
            if (event.getNavi().equals("einreise")) {
                player.sendMessage("§8[§9Tutorial§8]§7 Erstelle dir nun einen Personalausweis!");
            }
            if (event.getNavi().equals("Shop-1")) {
                player.sendMessage("§8[§9Tutorial§8]§7 Hier kannst du Gegenstände kaufen oder Verkaufen.");
                Main.waitSeconds(2, () -> {
                    player.sendMessage("§8[§9Tutorial§8]§7 Wenn du eine §6Firma§7 besitzt, kannst du diese Läden kaufen und verwalten. Firmen kannst du in der §eStadthalle gründen.");
                    Main.waitSeconds(3, () -> {
                        player.sendMessage("§8[§9Tutorial§8]§7 Du kannst dir ein Handy & Tablet kaufen oder mit Shift + F deine Tasche öffnen und darüber diese benutzen.");
                        Main.waitSeconds(4, () -> {
                            player.sendMessage("§8[§9Tutorial§8]§7 Mit §8/§ecall§7 kannst du Sie auch Anrufen.");
                            Main.waitSeconds(6, () -> {
                                player.sendMessage("§8[§9Tutorial§8]§7 Das Tutorial ist fast durch, nur noch ein bisschen was um zu vermeiden, dass du gebannt wirst.");
                                Main.waitSeconds(4, () -> supportMessage(player));
                            });
                        });
                    });
                });
            }
        }
    }
}
