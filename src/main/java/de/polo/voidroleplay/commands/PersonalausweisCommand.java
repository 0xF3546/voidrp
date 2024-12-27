package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.TabCompletion;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PersonalausweisCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final Utils utils;

    public PersonalausweisCommand(PlayerManager playerManager, Utils utils) {
        this.playerManager = playerManager;
        this.utils = utils;
        Main.registerCommand("personalausweis", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Locale locale = new Locale("de", "DE");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        String formattedDate = dateFormat.format(playerData.getBirthday());
        if (playerManager.firstname(player) != null && playerManager.lastname(player) != null) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("show")) {
                    Player targetplayer = Bukkit.getPlayer(args[1]);
                    if (targetplayer != null) {
                        if (player.getLocation().distance(targetplayer.getLocation()) <= 5) {
                            player.sendMessage(Prefix.MAIN + "Du hast §c" + targetplayer.getName() + "§7 deinen Personalausweis gezeigt.");
                            targetplayer.sendMessage("");
                            targetplayer.sendMessage("§7     ===§8[§6FREMDER PERSONALAUSWEIS§8]§7===");
                            targetplayer.sendMessage(" ");
                            targetplayer.sendMessage("§8 ➥ §eVorname§8:§7 " + playerManager.firstname(player));
                            targetplayer.sendMessage("§8 ➥ §eNachname§8:§7 " + playerManager.lastname(player));
                            targetplayer.sendMessage("§8 ➥ §eGeschlecht§8:§7 " + playerData.getGender().getTranslation());
                            targetplayer.sendMessage("§8 ➥ §eGeburtsdatum§8:§7 " + formattedDate);
                            targetplayer.sendMessage(" ");
                            targetplayer.sendMessage("§8 ➥ §eWohnort§8:§7 " + utils.houseManager.getHouseAccessAsString(playerData));
                            if (!playerData.getRelationShip().isEmpty()) {
                                for (Map.Entry<String, String> entry : playerData.getRelationShip().entrySet()) {
                                    if (entry.getValue().equalsIgnoreCase("beziehung")) {
                                        OfflinePlayer tplayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                                        targetplayer.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Ledig §o(Beziehung: " + tplayer.getName() + ")");
                                    } else if (entry.getValue().equalsIgnoreCase("verlobt")) {
                                        OfflinePlayer tplayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                                        targetplayer.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Ledig §o(Verlobt: " + tplayer.getName() + ")");
                                    } else if (entry.getValue().equalsIgnoreCase("verheiratet")) {
                                        OfflinePlayer tplayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                                        targetplayer.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Verheiratet (" + tplayer.getName() + ")");
                                    } else {
                                        targetplayer.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Ledig");
                                    }
                                }
                            } else {
                                targetplayer.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Ledig");
                            }
                            targetplayer.sendMessage("§8 ➥ §eVisumstufe§8:§7 " + playerManager.visum(player));
                        } else {
                            player.sendMessage(Prefix.ERROR + targetplayer.getName() + " ist nicht in der nähe.");
                        }
                    } else {
                        player.sendMessage(Prefix.ERROR + "Es wurde kein Spieler mit diesem Namen gefunden.");
                    }
                }
            } else {
                player.sendMessage("");
                player.sendMessage("§7     ===§8[§6PERSONALAUSWEIS§8]§7===");
                player.sendMessage(" ");
                player.sendMessage("§8 ➥ §eVorname§8:§7 " + playerManager.firstname(player));
                player.sendMessage("§8 ➥ §eNachname§8:§7 " + playerManager.lastname(player));
                player.sendMessage("§8 ➥ §eGeschlecht§8:§7 " + playerData.getGender().getTranslation());
                player.sendMessage("§8 ➥ §eGeburtsdatum§8:§7 " + formattedDate);
                player.sendMessage(" ");
                player.sendMessage("§8 ➥ §eWohnort§8:§7 " + utils.houseManager.getHouseAccessAsString(playerData));
                if (!playerData.getRelationShip().isEmpty()) {
                    for (Map.Entry<String, String> entry : playerData.getRelationShip().entrySet()) {
                        if (entry.getValue().equalsIgnoreCase("beziehung")) {
                            OfflinePlayer targetplayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                            player.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Ledig §o(Beziehung: " + targetplayer.getName() + ")");
                        } else if (entry.getValue().equalsIgnoreCase("verlobt")) {
                            OfflinePlayer targetplayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                            player.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Ledig §o(Verlobt: " + targetplayer.getName() + ")");
                        } else if (entry.getValue().equalsIgnoreCase("verheiratet")) {
                            OfflinePlayer targetplayer = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                            player.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Verheiratet (" + targetplayer.getName() + ")");
                        } else {
                            player.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Ledig");
                        }
                    }
                } else {
                    player.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 Ledig");
                }
                player.sendMessage("§8 ➥ §eVisumstufe§8:§7 " + playerManager.visum(player));
                utils.tutorial.usedAusweis(player);
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Du besitzt noch keinen Personalausweis.");
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, "show")
                .build();
    }
}
