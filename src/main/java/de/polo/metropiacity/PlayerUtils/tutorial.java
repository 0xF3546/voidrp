package de.polo.metropiacity.PlayerUtils;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.MySQl.MySQL;
import de.polo.metropiacity.Utils.NaviReachEvent;
import de.polo.metropiacity.Utils.Navigation;
import de.polo.metropiacity.Utils.PlayerManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.sql.Statement;

public class tutorial implements Listener {
    public static void start(Player player) {
        player.sendMessage("§8[§9Tutorial§8]§7 Willkommen im Tutorial.");
        Main.waitSeconds(4, () -> {
            player.sendMessage("§8[§9Tutorial§8]§7 Als erstes musst du dir einen Personalausweis erstellen. Gehe dazu ins Rathaus, folge dazu einfach der Route!");
            Navigation.createNavi(player, "einreise", true);
        });
    }
    public static boolean isInTutorial(Player player) {
        return PlayerManager.playerDataMap.get(player.getUniqueId().toString()).getVariable("tutorial") != null;
    }

    public static void createdAusweis(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getVariable("tutorial") != null) {
            player.sendMessage("§8[§9Tutorial§8]§7 Sehr gut gemacht!");
            Main.waitSeconds(1, () -> {
                player.sendMessage("§8[§9Tutorial§8]§7 Nutze §8/§epersonalausweis§7 um deinen Personalausweis anzusehen.");
            });
        }
    }

    public static void usedAusweis(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        if (playerData.getVariable("tutorial") != null) {
            player.sendMessage("§8[§9Tutorial§8]§7 Ich denke es ist alles verständlich, außer die §eVisumstufe§7?");
            Main.waitSeconds(4, () -> {
                player.sendMessage("§8[§9Tutorial§8]§7 Dein §eVisum§7 gibt an wie lang du schon auf Void Roleplay bist.");
                Main.waitSeconds(4, () -> {
                    TextComponent one = new TextComponent("§8[§9Tutorial§8]§7 Mehr über das §eVisumsystem§7 findest du ");
                    TextComponent mcserverlist = new TextComponent("§ehier§7.");
                    mcserverlist.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/forum/main/index.php?thread/4-visum-levelsystem/"));
                    player.spigot().sendMessage(one, mcserverlist);
                    Main.waitSeconds(3, () -> {
                        Navigation.createNavi(player, "Shop-1", true);
                        player.sendMessage("§8[§9Tutorial§8]§7 Begib dich nun zum Shop.");
                    });
                });
            });
        }
    }

    public void supportMessage(Player player) {
        TextComponent one = new TextComponent("§8[§9Tutorial§8]§7 Es ist wichtig, dass du dir das ");
        TextComponent mcserverlist = new TextComponent("§eRegelwerk§7 ");
        TextComponent two = new TextComponent("§7durchliest.");
        mcserverlist.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/forum/main/index.php?thread/3-ingame-regelwerk/&postID=3#post3"));
        player.spigot().sendMessage(one, mcserverlist);
        Main.waitSeconds(4, () -> {
            player.sendMessage("§8[§9Tutorial§8]§7 Solltest du noch §eFragen §7oder andere §eAnliegen§7 haben, nutze §8/§esupport [Anliegen]§7.");
            Main.waitSeconds(4, () -> {
                player.sendMessage("§8[§9Tutorial§8]§7 Beachte jedoch, dass auch ein §eTeammitglied§7 anwesend sein muss, um dein §eTicket§7 zu bearbeiten. (§8/§eteam§7)");
                Main.waitSeconds(4, () -> {
                    TextComponent a = new TextComponent("§8[§9Tutorial§8]§7 Sollte keins anwesend sein, reiche dein §eTicket§7 im ");
                    TextComponent b = new TextComponent("§eForum§7 ");
                    TextComponent c = new TextComponent("§7ein.");
                    b.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://voidroleplay.de/forum/main/"));
                    player.spigot().sendMessage(a,b,c);
                    Main.waitSeconds(4, () -> {
                        endTutorial(player);
                    });
                });
            });
        });
    }

    public void endTutorial(Player player) {
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
        playerData.setVariable("tutorial", null);
        player.sendMessage("§8[§9Tutorial§8]§7 Du hast das §9Tutorial§a erfolgreich§7 beendeet.");
        player.sendMessage("§8[§bInfo§8]§7 Solltest du noch mehr über den Server wissen wollen, komm auf unseren §9Discord§7 oder schau auf §cYouTube§7 vorbei.");
        player.sendMessage("§8       ➥ §8/§9discord§7 & §8/§cyoutube");
        PlayerManager.addExp(player, 30);
        try {
            Statement statement = MySQL.getStatement();
            statement.executeUpdate("UPDATE `players` SET `tutorial` = false WHERE `uuid` = '" + player.getUniqueId().toString() + "'");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onNaviReach(NaviReachEvent event) {
        if (event.getPlayerData().getVariable("tutorial") != null) {
            Player player = event.getPlayer();
            if (event.getNavi().equals("einreise")) {
                player.sendMessage("§8[§9Tutorial§8]§7 Erstelle dir nun einen Personalausweis!");
            }
            if (event.getNavi().equals("Shop-1")) {
                player.sendMessage("§8[§9Tutorial§8]§7 Kaufe dir nun ein Handy und schau dich ein bisschen um. Das Tutorial geht in 15 Sekunden weiter");
                Main.waitSeconds(15, () -> {
                    player.sendMessage("§8[§9Tutorial§8]§7 Deine Nummer lautet §e" + event.getPlayerData().getNumber() + "§7, mit §8/§esms§7 kannst du SMS an Freunde schreiben.");
                    Main.waitSeconds(4, () -> {
                        player.sendMessage("§8[§9Tutorial§8]§7 Mit §8/§ecall§7 kannst du Sie auch Anrufen.");
                        Main.waitSeconds(6, () -> {
                            player.sendMessage("§8[§9Tutorial§8]§7 Das Tutorial ist fast durch, nur noch ein bisschen was um zu vermeiden, dass du gebannt wirst.");
                            Main.waitSeconds(4, () -> {
                                supportMessage(player);
                            });
                        });
                    });
                });
            }
        }
    }
}
