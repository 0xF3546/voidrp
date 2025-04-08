package de.polo.core.faction.commands;

import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.core.handler.TabCompletion;
import de.polo.core.faction.entity.Faction;
import de.polo.api.player.VoidPlayer;
import de.polo.core.player.entities.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@CommandBase.CommandMeta(name = "equiplog", leader = true, usage = "/equiplog [<calculate>]")
public class EquiplogCommand extends CommandBase implements TabCompleter {
    public EquiplogCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        Faction factionData = Main.getInstance().factionManager.getFactionData(playerData.getFaction());
        if (args.length < 1) {
            Main.getInstance().getCoreDatabase().executeQueryAsync("SELECT * FROM faction_equip_logs WHERE factionId = ?", factionData.getId())
                    .thenAccept(result -> {
                        for (Map<String, Object> res : result) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString((String) res.get("player")));
                            player.sendMessage("§8 - §7" + offlinePlayer.getName() + " " + res.get("item"));
                        }
                    });
        } else if (args[0].equalsIgnoreCase("calculate")) {
            Main.getInstance().getCoreDatabase().executeQueryAsync(
                    "SELECT player, SUM(itemPoints) as totalPoints FROM faction_equip_logs WHERE factionId = ? GROUP BY player",
                    factionData.getId()
            ).thenAccept(result -> {
                for (Map<String, Object> res : result) {
                    UUID playerUUID = UUID.fromString((String) res.get("player"));
                    int totalPoints = Integer.parseInt(res.get("totalPoints").toString());

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                    player.sendMessage("§8 - §7" + offlinePlayer.getName() + ": " + totalPoints + " Punkte");
                }
            });
        } else if (args[0].equalsIgnoreCase("reset")) {
            Main.getInstance().factionManager.sendCustomLeaderMessageToFactions("§8[§6Equip§8]§c " + player.getName() + " hat den Equiplog geleert.");
            Main.getInstance().getCoreDatabase().deleteAsync("DELETE FROM faction_equip_logs WHERE factionId = ?", factionData.getId());
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return TabCompletion.getBuilder(strings)
                .addAtIndex(1, List.of("calculate", "reset"))
                .build();
    }
}
