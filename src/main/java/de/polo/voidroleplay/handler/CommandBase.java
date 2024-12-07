package de.polo.voidroleplay.handler;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.utils.Prefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class CommandBase implements CommandExecutor {

    private final CommandMeta meta;

    public CommandBase(@NotNull CommandMeta meta) {
        this.meta = meta;
    }

    /**
     * Diese Methode wird von spezifischen Befehlen überschrieben, um ihre Logik zu implementieren.
     */
    public abstract void execute(@NotNull Player player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception;

    /**
     * Bukkit-Methode zur Befehlsausführung.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Prefix.ERROR + "Dieser Befehl kann nur von Spielern ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = Main.getInstance().getPlayerManager().getPlayerData(player);

        if (playerData == null) {
            player.sendMessage(Prefix.ERROR + "Spielerdaten konnten nicht geladen werden.");
            return true;
        }

        if (playerData.getPermlevel() < meta.permissionLevel()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return true;
        }

        try {
            execute(player, playerData, args);
        } catch (Exception e) {
            player.sendMessage(Prefix.ERROR + "Ein Fehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Zeigt die Syntax des Befehls an.
     */
    protected void showSyntax(@NotNull CommandSender sender) {
        sender.sendMessage(Prefix.ERROR + "Syntax: " + meta.usage());
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandMeta {
        String name();

        int permissionLevel() default 0;

        String usage() default "/<command>";
    }
}
