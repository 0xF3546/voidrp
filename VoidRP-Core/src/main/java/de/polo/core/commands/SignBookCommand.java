package de.polo.core.commands;

import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */

@CommandBase.CommandMeta(name = "signbook", faction = "News", usage = "/signbook [Titel]")
public class SignBookCommand extends CommandBase {
    public SignBookCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        ItemStack stack = player.getPlayer().getInventory().getItemInMainHand();
        if (stack.getType() != Material.WRITTEN_BOOK) {
            player.sendMessage(Component.text(Prefix.ERROR + "Du musst ein Buch in der Hand halten."));
            return;
        }
        if (args.length < 1) {
            showSyntax(player);
            return;
        }
        BookMeta bookMeta = (BookMeta) stack.getItemMeta();
        bookMeta.setAuthor("Void News");
        String title = Utils.stringArrayToString(args).replace("&", "§");
        bookMeta.setTitle(title);
        bookMeta.setUnbreakable(true);
        stack.setItemMeta(bookMeta);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(title));
        stack.setItemMeta(meta);
    }
}
