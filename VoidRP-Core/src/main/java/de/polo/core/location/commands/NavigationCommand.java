package de.polo.core.location.commands;

import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.core.handler.TabCompletion;
import de.polo.core.location.services.LocationService;
import de.polo.core.location.services.NavigationService;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.NaviData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "navigation",
        usage = "/navigation"
)
public class NavigationCommand extends CommandBase implements TabCompleter {
    public NavigationCommand(@NotNull CommandMeta meta) {
        super(meta);
        Main.addTabCompleter("navi", this);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        NavigationService navigationService = VoidAPI.getService(NavigationService.class);
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (player.getVariable("navi") == null) {
            if (args.length >= 1) {
                if (args[0].contains("haus:")) {
                    try {
                        int number = Integer.parseInt(args[0].replace("haus:", "").replace(" ", ""));
                        for (RegisteredBlock registeredBlock : Main.getInstance().blockManager.getBlocks()) {
                            if (registeredBlock.getInfo() == null) {
                                System.out.println("Info is null");
                                continue;
                            }
                            if (!registeredBlock.getInfo().equalsIgnoreCase("house")) {
                                continue;
                            }
                            if (registeredBlock.getInfoValue() == null) {
                                System.out.println("InfoValue is null");
                                continue;
                            }
                            try {
                                if (Integer.parseInt(registeredBlock.getInfoValue()) != number) {
                                    // ISSUE VRP-10004: fixed to much log spam
                                    continue;
                                }
                            } catch (NumberFormatException e) {
                                // ISSUE VRP-10004: fixed to much log spam
                                continue;
                            }
                            navigationService.createNaviByCord(player.getPlayer(), (int) registeredBlock.getLocation().getX(), (int) registeredBlock.getLocation().getY(), (int) registeredBlock.getLocation().getZ());
                            player.sendMessage("§8[§eGPS§8]§7 Du hast ein Navi zu Haus " + number + " gemacht");
                            return;
                        }
                        player.sendMessage(Prefix.ERROR + "Hausnummer nicht gefunden");
                    } catch (NumberFormatException ex) {
                        player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /navi haus:[NUMMER]");
                    }
                }

                if (args[0].contains("atm")) {
                    try {
                        double closestDistance = Double.MAX_VALUE;  // Initialisiert mit einem sehr großen Wert
                        RegisteredBlock closestAtm = null;

                        for (RegisteredBlock registeredBlock : Main.getInstance().blockManager.getBlocks()) {
                            if (registeredBlock.getInfo() == null) {
                                continue;
                            }
                            if (!registeredBlock.getInfo().equalsIgnoreCase("atm")) {
                                continue;
                            }

                            double distance = player.getLocation().distance(registeredBlock.getLocation());

                            if (distance < closestDistance) {
                                closestDistance = distance;
                                closestAtm = registeredBlock;
                            }
                        }

                        if (closestAtm != null) {
                            navigationService.createNaviByCord(player.getPlayer(), (int) closestAtm.getLocation().getX(), (int) closestAtm.getLocation().getY(), (int) closestAtm.getLocation().getZ());
                            player.sendMessage("§8[§eGPS§8]§7 Du hast den nächsten ATM markiert");
                        } else {
                            player.sendMessage(Prefix.ERROR + "Kein ATM gefunden.");
                        }
                    } catch (NumberFormatException ex) {
                        player.sendMessage(Prefix.ERROR + "Syntax-Fehler: /navi haus:[NUMMER]");
                    }
                }

                if (args.length >= 3) {
                    navigationService.createNaviByCord(player.getPlayer(), Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                } else {
                    String nav = Utils.stringArrayToString(args);
                    NaviData data = locationService.getNavis().stream()
                            .filter(x -> !x.isGroup() && x.getClearName().equalsIgnoreCase(nav))
                            .findFirst()
                            .orElse(null);
                    if (data == null) {
                        player.sendMessage(Component.text(Prefix.ERROR + "Der Punkte wurde nicht gefunden."));
                        return;
                    }
                    navigationService.createNavi(player.getPlayer(), data.getLocation(), false);
                }
            } else {
                navigationService.openNavi(player.getPlayer(), null);
            }
        } else {
            player.setVariable("navi", null);
            player.sendMessage("§8[§6GPS§8]§e Du hast deine Route gelöscht.");
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        return TabCompletion.getBuilder(args)
                .addAtIndex(1, locationService.getNavis().stream().filter(x -> !x.isGroup())
                        .map(NaviData::getClearName).toList())
                .build();
    }
}
