package de.polo.core.commands;

import de.polo.core.Main;
import de.polo.core.handler.TabCompletion;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.*;

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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl verwenden.");
            return true;
        }

        if (playerManager.firstname(player) == null || playerManager.lastname(player) == null) {
            player.sendMessage(Prefix.ERROR + "Du besitzt noch keinen Personalausweis.");
            return true;
        }

        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());

        if (args.length >= 1 && args[0].equalsIgnoreCase("show")) {
            if (args.length < 2) {
                player.sendMessage(Prefix.ERROR + "Bitte gib einen Spielernamen an.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(Prefix.ERROR + "Es wurde kein Spieler mit diesem Namen gefunden.");
                return true;
            }

            if (player.getLocation().distance(target.getLocation()) > 5) {
                player.sendMessage(Prefix.ERROR + target.getName() + " ist nicht in der Nähe.");
                return true;
            }

            player.sendMessage(Prefix.MAIN + "Du hast §c" + target.getName() + "§7 deinen Personalausweis gezeigt.");
            sendIDCardToOther(player, target, playerData);
        } else {
            sendIDCardToSelf(player, playerData);
            utils.tutorial.usedAusweis(player);
        }

        return true;
    }

    private void sendIDCardToSelf(Player player, PlayerData data) {
        player.sendMessage("§8§m----------------------");
        player.sendMessage("§7     ===§8[§6PERSONALAUSWEIS§8]§7===");
        sendBasicData(player, data, player);
    }

    private void sendIDCardToOther(Player owner, Player target, PlayerData data) {
        target.sendMessage("§8§m----------------------");
        target.sendMessage("§7     ===§8[§6FREMDER PERSONALAUSWEIS§8]§7===");
        sendBasicData(target, data, owner);
    }

    private void sendBasicData(Player receiver, PlayerData data, Player owner) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.GERMANY);
        String birthDate = dateFormat.format(data.getBirthday());

        receiver.sendMessage("§8 ➥ §eVorname§8:§7 " + data.getFirstname());
        receiver.sendMessage("§8 ➥ §eNachname§8:§7 " + data.getLastname());
        receiver.sendMessage("§8 ➥ §eGeschlecht§8:§7 " + data.getGender().getTranslation());
        receiver.sendMessage("§8 ➥ §eGeburtsdatum§8:§7 " + birthDate);
        receiver.sendMessage("§8§m----------------------");
        receiver.sendMessage("§8 ➥ §eWohnort§8:§7 " + utils.houseManager.getHouseAccessAsString(data));
        receiver.sendMessage("§8 ➥ §eBeziehungsstatus§8:§7 " + formatRelationship(data));
        receiver.sendMessage("§8 ➥ §eVisumstufe§8:§7 " + data.getVisum());
        receiver.sendMessage("§8§m----------------------");

        receiver.playSound(receiver.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
    }

    private String formatRelationship(PlayerData data) {
        if (data.getRelationShip().isEmpty()) return "Ledig";

        for (Map.Entry<String, String> entry : data.getRelationShip().entrySet()) {
            OfflinePlayer partner = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
            String status = entry.getValue().toLowerCase(Locale.ROOT);

            return switch (status) {
                case "beziehung" -> "Ledig §o(Beziehung: " + partner.getName() + ")";
                case "verlobt" -> "Ledig §o(Verlobt: " + partner.getName() + ")";
                case "verheiratet" -> "Verheiratet (" + partner.getName() + ")";
                default -> "Ledig";
            };
        }

        return "Ledig";
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, "show")
                .build();
    }
}
