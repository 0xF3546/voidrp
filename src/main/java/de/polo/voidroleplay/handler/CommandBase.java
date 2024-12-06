package de.polo.voidroleplay.handler;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class CommandBase implements CommandExecutor {

    private final Command command;

    public CommandBase(@NotNull Command command) {
        this.command = command;
    }

    public abstract void execute(@NotNull Player player, @NotNull PlayerData context, @NotNull String[] args) throws Exception;

    protected void showSyntax(@NotNull CommandSender sender) {
        sender.sendMessage(Prefix.ERROR + "Syntax-Fehler: " + command.usage());
    }

    //@Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = Main.getInstance().getPlayerManager().getPlayerData(player);
        if (playerData == null) {
            return true;
        }
        if (playerData.getPermlevel() < command.permissionLevel()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return true;
        }
        try {
            execute(player, playerData, args);
        } catch (Exception e) {
            player.sendMessage("An error occurred: " + e.getMessage());
        }
        return true;
    }

    protected PlayerData getContext(@NotNull Player player) {
        return Main.getInstance().getPlayerManager().getPlayerData(player);
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Command {
        String name();

        int permissionLevel() default 0;

        String usage() default "/<command>";
    }

}
