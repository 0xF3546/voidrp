package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.handler.TabCompletion;
import de.polo.voidroleplay.storage.Agreement;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.VertragUtil;
import de.polo.voidroleplay.utils.enums.Drug;
import de.polo.voidroleplay.utils.player.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.polo.voidroleplay.Main.utils;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "selldrug", usage = "/selldrug [Spieler] [Item] [Anzahl] [Preis]")
public class SellDrugCommand extends CommandBase implements TabCompleter {
    public SellDrugCommand(@NotNull CommandMeta meta) {
        super(meta);
        Main.addTabCompeter(meta.name(), this);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 4) {
            showSyntax(player);
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler wurde nicht gefunden."));
            return;
        }
        if (target.getLocation().distance(player.getLocation()) > 5) {
            player.sendMessage(Component.text(Prefix.ERROR + "Der Spieler ist nicht in der nähe."));
            return;
        }
        Drug drug = null;
        for (Drug d : Drug.values()) {
            if (d.name().equalsIgnoreCase(args[1]) || d.getItem().getClearName().equalsIgnoreCase(args[1]) || d.getItem().name().equalsIgnoreCase(args[1])) {
                drug = d;
            }
        }
        if (drug == null) {
            player.sendMessage(Component.text(Prefix.ERROR + "Das Item wurde nicht gefunden."));
            return;
        }
        try {
            int amount = Integer.parseInt(args[2]);
            int price = Integer.parseInt(args[3]);
            if (amount < 1 || price < 1) {
                return;
            }
            if (playerData.getInventory().getByTypeOrEmpty(drug.getItem()).getAmount() < amount) {
                player.sendMessage(Component.text(Prefix.ERROR + "Du hast davon nicht genug dabei."));
                return;
            }
            Drug finalDrug = drug;
            PlayerData targetData = Main.getInstance().playerManager.getPlayerData(target);
            target.sendMessage(Prefix.MAIN + player.getName() + " biete dir " + finalDrug.getItem().getClearName() + " für " + price + "$ an.");
            utils.vertragUtil.sendInfoMessage(target);
            player.sendMessage(Prefix.MAIN + "Du hast " + target.getName() + " " + finalDrug.getItem().getClearName() + " für " + price + "$ angeboten.");
            Agreement agreement = new Agreement(player, target, "selldrug", () -> {
                if (targetData.getBargeld() < price) {
                    target.sendMessage(Component.text(Prefix.ERROR + "Du hast nicht genug Geld dabei."));
                    return;
                }
                if (targetData.getInventory().addItem(finalDrug.getItem(), amount)) {
                    playerData.addMoney(price, "Verkauf drogen");
                    targetData.removeMoney(price, "Ankauf drogen");
                    playerData.getInventory().removeItem(finalDrug.getItem(), amount);
                    ChatUtils.sendGrayMessageAtPlayer(player, player.getName() + " handelt mit " + target.getName());
                } else {
                    player.sendMessage(Prefix.ERROR + target.getName() + " hat nicht genug Inventarplatz.");
                    target.sendMessage(Prefix.ERROR +"Du hast nicht genug Inventarplatz.");
                }
            }, () -> {
                player.sendMessage(Prefix.MAIN + target.getName() + " hat das Angebot abgelehnt.");
                player.sendMessage(Prefix.MAIN + "Du hast das Angebot abgelehnt.");
            });
            utils.vertragUtil.setAgreement(player, target, agreement);
        } catch (Exception ex) {
            player.sendMessage(Component.text(Prefix.ERROR + "Die Anzahl & der Preis müssen numerisch sein."));
            return;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, Bukkit.getOnlinePlayers().stream()
                        .filter(x -> x.getLocation().distance(player.getLocation()) < 10)
                        .map(Player::getName)
                        .toList())
                .addAtIndex(2, "[Droge]")
                .addAtIndex(3, "[Anzahl]")
                .addAtIndex(4, "[Preis]")
                .build();
    }
}
