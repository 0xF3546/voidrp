package de.polo.voidroleplay.handler;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.VoidAPI;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.PlayerData;
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
import java.util.Objects;

public abstract class CommandBase implements CommandExecutor {

    private final CommandMeta meta;

    public CommandBase(@NotNull CommandMeta meta) {
        this.meta = meta;

        Main.getInstance().getCommand(meta.name()).setExecutor(this);

    }

    /**
     * Diese Methode wird von spezifischen Befehlen überschrieben, um ihre Logik zu implementieren.
     */
    public abstract void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception;

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
        if (meta.adminDuty() && !playerData.isAduty()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst.");
            return true;
        }
        if (!Objects.equals(meta.faction(), "")) {
            if (playerData.getFaction() == null || !playerData.getFaction().equalsIgnoreCase(meta.faction())) {
                player.sendMessage(Prefix.ERROR_NOPERMISSION);
                return true;
            }
        }
        if (meta.leader() && !playerData.isLeader()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return true;
        }
        try {
            VoidPlayer voidPlayer = VoidAPI.getPlayer(player);
            execute(voidPlayer, playerData, args);
        } catch (Exception e) {
            player.sendMessage(Prefix.ERROR + "Ein Fehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Zeigt die Syntax des Befehls an.
     */
    protected void showSyntax(@NotNull VoidPlayer sender) {
        sender.sendMessage(Prefix.ERROR + "Syntax: " + meta.usage());
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandMeta {
        String name();

        int permissionLevel() default 0;

        String usage() default "/<command>";

        boolean adminDuty() default false;

        boolean leader() default false;

        String faction() default "";
    }
}
