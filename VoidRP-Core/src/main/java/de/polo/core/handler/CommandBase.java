package de.polo.core.handler;

import de.polo.core.Main;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.utils.Prefix;
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

import static de.polo.core.Main.playerService;

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

        VoidPlayer player = VoidAPI.getPlayer((Player) sender);

        if (player.getData() == null) {
            player.sendMessage(Prefix.ERROR + "Spielerdaten konnten nicht geladen werden.");
            return true;
        }

        if (player.getData().getPermlevel() < meta.permissionLevel()) {
            player.sendMessage(Prefix.ERROR_NOPERMISSION);
            return true;
        }
        if (meta.adminDuty() && !player.isAduty()) {
            player.sendMessage(Prefix.ERROR + "Du bist nicht im Admindienst.");
            return true;
        }
        PlayerData playerData = playerService.getPlayerData(player.getUuid());
        if (!Objects.equals(meta.faction(), "")) {

            if (playerData.getFaction() == null || !playerData.getFaction().equalsIgnoreCase(meta.faction())) {
                player.sendMessage(Prefix.ERROR_NOPERMISSION);
                return true;
            }
        }
        if (meta.leader() && !player.getData().isLeader()) {
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
