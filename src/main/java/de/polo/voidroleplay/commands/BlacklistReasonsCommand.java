package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.game.faction.blacklist.BlacklistReason;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.Prefix;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlacklistReasonsCommand implements CommandExecutor, TabCompleter {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public BlacklistReasonsCommand(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;

        Main.registerCommand("blacklistreasons", this);
        Main.addTabCompeter("blacklistreasons", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (playerData.getFaction() == null) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return false;
        }
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        if (!factionData.hasBlacklist()) {
            player.sendMessage(Prefix.ERROR + "Deine Fraktion hat keine Blacklist!");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage("§7   ===§8[§cBlacklist§8]§7===");
            for (BlacklistReason blacklistReason : factionData.getBlacklistReasons()) {
                player.sendMessage("§8 ➥ §e" + blacklistReason.getId() + " §8✗ " + " §e" + blacklistReason.getReason() + "§8 | §e" + blacklistReason.getKills() + " Kills §8| §e" + blacklistReason.getPrice() + "$");
            }
            return false;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (!playerData.isLeader()) {
                player.sendMessage(Prefix.ERROR + "Das geht als Leader!");
                return false;
            }
            if (args.length < 4) {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /blacklistreasons add [Preis] [Kills] [Grund]");
                return false;
            }
            try {
                int price = Integer.parseInt(args[1]);
                int kills = Integer.parseInt(args[2]);
                StringBuilder reason = new StringBuilder(args[3]);
                for (int i = 4; i < args.length; i++) {
                    reason.append(" ").append(args[i]);
                }
                BlacklistReason blacklistReason = new BlacklistReason(reason.toString(), price, kills);
                blacklistReason.setFactionId(factionData.getId());
                player.sendMessage("§8[§cBlacklist§8]§a Du hast den Grund erstellt.");
                factionData.addBlacklistReason(blacklistReason, true);
            } catch (Exception ex) {
                player.sendMessage(Prefix.ERROR + "Der Preis/Kills müssen numerisch sein.");
            }
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (!playerData.isLeader()) {
                player.sendMessage(Prefix.ERROR + "Das geht nur als Leader!");
                return false;
            }
            if (args.length < 2) {
                player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /blacklistreasons remove [ID]");
                return false;
            }
            try {
                int id = Integer.parseInt(args[1]);
                BlacklistReason reason = factionData.getBlacklistReasonById(id);
                if (reason == null) {
                    player.sendMessage(Prefix.ERROR + "Der Blacklistgrund wurde nicht gefunden.");
                    return false;
                }
                player.sendMessage("§8[§cBlacklist§8]§a Du hast den Grund gelöscht.");
                factionData.removeBlacklistReason(reason, true);
            } catch (Exception ex) {
                player.sendMessage(Prefix.ERROR + "Die ID muss numerisch sein.");
            }
        } else {
            player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /blacklistreasons [add/remove]");
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> texts = new ObjectArrayList<>();
        if (args.length == 1) {
            texts.add("add");
            texts.add("remove");
        }
        return null;
    }
}
