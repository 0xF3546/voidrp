package de.polo.core.commands;

import de.polo.api.utils.enums.Prefix;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.handler.TabCompletion;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Discord;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@CommandBase.CommandMeta(
        name = "discord",
        usage = "/discord [link/reload] [<Discord-Id>]"
)
public class DiscordCommand extends CommandBase implements TabCompleter {

    public DiscordCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        if (args.length < 1) {
            TextComponent c = new TextComponent(Prefix.DISCORD.getPrefix() + "Klicke um den Discord-Server zu joinen.");
            c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/void-roleplay"));
            player.getPlayer().spigot().sendMessage(c);
            return;
        }
        if (args[0].equalsIgnoreCase("link")) {
            if (args.length < 2) {
                showSyntax(player);
                return;
            }
            Discord.verifyUser(player.getPlayer(), args[1]);
            player.sendMessage("Unser Discord Bot hat dir eine Nachricht gesendet. Bitte öffne Discord und folge den Anweisungen, um deinen Account zu verifizieren.", Prefix.DISCORD);
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (player.getData().getDiscordId() == null) {
                player.sendMessage("Du hast keinen Discord-Account verknüpft.", Prefix.ERROR);
                return;
            }
            Discord.reloadPlayer(player.getPlayer().getUniqueId());
            player.sendMessage("Dein Discord-Account wurde erfolgreich aktualisiert.", Prefix.DISCORD);
        } else {
            showSyntax(player);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, List.of("link", "reload"))
                .build();
    }
}
