package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.handler.CommandBase;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static de.polo.voidroleplay.Main.newsManager;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "sellbook", faction = "News", usage = "/sellbook [Textart] [Preis]")
public class SellBookCommand extends CommandBase {
    public SellBookCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (playerData.getFactionGrade() < 4) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return;
        }
        ItemStack stack = player.getPlayer().getInventory().getItemInMainHand();
        if (stack.getType() != Material.WRITTEN_BOOK) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du musst ein Buch in der Hand halten."));
            return;
        }
        if (args.length < 2) {
            showSyntax(player);
            return;
        }
        try {
            int price = Integer.parseInt(args[1]);
            newsManager.addBookToStore(stack, args[0], price);
            player.sendMessage(Component.text(Prefix.MAIN + "Du hast ein Buch zum verkauf gestellt."));
        } catch (Exception ex) {
            player.sendMessage(Component.text(Prefix.ERROR + "Die Zahl muss numerisch sein."));
        }
    }
}
