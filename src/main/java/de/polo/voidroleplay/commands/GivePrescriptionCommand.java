package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.VoidAPI;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.handler.TabCompletion;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.Agreement;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.enums.Prescription;
import de.polo.voidroleplay.utils.player.ChatUtils;
import de.polo.voidroleplay.utils.player.PayDayUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static de.polo.voidroleplay.Main.agreementService;
import static de.polo.voidroleplay.Main.utils;

@CommandBase.CommandMeta(name = "giveprescription", faction = "Medic", usage = "/giverezept [Spieler] [Rezept]")
public class GivePrescriptionCommand extends CommandBase implements TabCompleter {
    public GivePrescriptionCommand(@NotNull CommandMeta meta) {
        super(meta);
        Main.addTabCompleter(meta.name(), this);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (playerData.getFactionGrade() < 3) {
            player.sendMessage(Component.text(Prefix.ERROR + "Dieser Befehl geht erst ab Rang 3!"));
            return;
        }
        if (args.length < 2) {
            showSyntax(player);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Spieler wurde nicht gefunden."));
            return;
        }
        if (target.getLocation().distance(player.getLocation()) > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler ist nicht in der nähe."));
            return;
        }
        Prescription prescription = null;
        for (Prescription p : Prescription.values()) {
            if (p.getName().equalsIgnoreCase(args[1])) {
                prescription = p;
            }
        }
        if (prescription == null) {
            player.sendMessage(Prefix.ERROR + "Das Rezept wurde nicht gefunden.");
            return;
        }
        player.sendMessage("§6Du hast " + target.getName() + " ein Rezept ausgestellt.");
        target.sendMessage("§6" + player.getName() + " ht dir ein Rezept ausgestellt (" + prescription.getName() + ").");
        utils.vertragUtil.sendInfoMessage(target);
        Prescription finalPrescription = prescription;
        ChatUtils.sendGrayMessageAtPlayer(player.getPlayer(), player.getName() + " stellt ein Rezept aus");
        VoidPlayer targetVoidPlayer = VoidAPI.getPlayer(target);
        Agreement agreement = new Agreement(player, targetVoidPlayer, "rezept",
                () -> {
                    PlayerData targetData = Main.getInstance().playerManager.getPlayerData(target);
                    if (targetData.getBargeld() < 200) {
                        player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                        return;
                    }
                    targetData.removeMoney(200, "Rezept - " + finalPrescription.getName());
                    PayDayUtils.PAYED_TAXES += 100;
                    ItemStack stack = ItemManager.createItem(Material.PAPER, 1, 0, "§c" + finalPrescription.getName());
                    target.getInventory().addItem(stack);
                    target.sendMessage(Component.text("§eDu hast ein " + finalPrescription.getName() + " Rezept gekauft."));
                    player.sendMessage(Component.text("§c" + target.getName() + " hat das Angebot angenommen."));
                    try {
                        Main.getInstance().factionManager.addFactionMoney("Medic", 100, "Rezept verkauf");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    target.sendMessage(Component.text("§cDu hast das Angebot abgelehnt."));
                    player.sendMessage(Component.text("§c" + target.getName() + " hat das Angebot abgelehnt."));
                });
        agreementService.setAgreement(player, targetVoidPlayer, agreement);
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, Bukkit.getOnlinePlayers().stream()
                        .filter(x -> x.getLocation().distance(player.getLocation()) < 10)
                        .map(Player::getName)
                        .toList())
                .addAtIndex(2, Arrays.stream(Prescription.values())
                        .map(Prescription::getName)
                        .toList())
                .build();
    }
}
