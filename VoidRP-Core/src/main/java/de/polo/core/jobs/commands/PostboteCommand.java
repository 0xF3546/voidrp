package de.polo.core.jobs.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.Job;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.utils.Utils;
import de.polo.core.manager.ItemManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.enums.EXPType;
import de.polo.core.utils.player.SoundManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.polo.core.Main.locationManager;

@CommandBase.CommandMeta(
        name = "postbote",
        usage = "/postbote"
)
public class PostboteCommand extends CommandBase implements Job {
    private final List<Integer> array = new ObjectArrayList<>();
    public final String prefix = "§8[§ePostbote§8] §7";

    public PostboteCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (ServerManager.canDoJobs()) {
            if (locationManager.getDistanceBetweenCoords(player, "postbote") <= 5) {
                InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §ePostbote"), true, true);

                // Start Job Option
                if (!playerService.isInJobCooldown(player, MiniJob.POSTMAN) && player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aPostbote starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startJob(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                } else {
                    if (player.getActiveJob() == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mPostbote starten", "§8 ➥§7 Warte noch " + Utils.getTime(playerService.getJobCooldown(player, MiniJob.POSTMAN)) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {}
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mPostbote starten", "§8 ➥§7 Du hast bereits den §f" + player.getMiniJob().getName() + "§7 Job angenommen.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {}
                        });
                    }
                }

                // Quit Job Option
                if (player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {}
                    });
                } else if (!player.getMiniJob().equals(MiniJob.POSTMAN)) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {}
                    });
                } else {
                    int remainingDeliveries = (int)player.getVariable("post");
                    String payoutText = remainingDeliveries > 0 ?
                            "§8 ➥ §7Noch " + remainingDeliveries + " Briefe abzugeben" :
                            "§8 ➥ §7Alle Briefe abgegeben";
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", payoutText)) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            endJob(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist §cnicht§7 in der Nähe des Nachrichtengebäudes§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
    }

    public boolean canGive(int number) {
        return !array.contains(number);
    }

    @Override
    public void startJob(VoidPlayer player) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (!playerService.isInJobCooldown(player, MiniJob.POSTMAN)) {
            player.setMiniJob(MiniJob.POSTMAN);
            player.setActiveJob(this);

            int deliveries = Utils.random(2, 5);
            player.setVariable("post", deliveries);
            player.sendMessage(prefix + "Bringe die Post zu verschiedenen Häusern.");
            player.sendMessage("§8 ➥ §7Nutze §8[§6Rechtsklick§8]§7 auf die Hausschilder.");
            player.sendMessage(prefix + "Du hast §e" + deliveries + " Briefe§7 abzugeben.");
        } else {
            player.sendMessage(prefix + "Du kannst den Job erst in §f" + Utils.getTime(playerService.getJobCooldown(player, MiniJob.POSTMAN)) + "§7 beginnen.");
        }
    }

    @Override
    public void endJob(VoidPlayer player) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        Main.getInstance().beginnerpass.didQuest(player.getPlayer(), 5);

        player.sendMessage(prefix + "Du hast den Job beendet.");
        SoundManager.successSound(player.getPlayer());

        playerService.handleJobFinish(player, MiniJob.POSTMAN, 3600, Utils.random(12, 20));
        player.setMiniJob(null);
        player.setActiveJob(null);
        player.setVariable("post", null);
        array.clear();
    }

    public void handleDrop(VoidPlayer player, int house) {
        if (canGive(house)) {
            PlayerService playerService = VoidAPI.getService(PlayerService.class);
            int remainingDeliveries = (int)player.getVariable("post");
            if (remainingDeliveries <= 0) {
                player.sendMessage(prefix + "Du hast keine Post mehr zum Abgeben!");
                return;
            }

            int payout = Utils.random(ServerManager.getPayout("postbote"), ServerManager.getPayout("postbote2"));
            player.sendMessage(prefix + "Du hast Post bei §6Haus " + house + "§7 abgeliefert. §a+" + payout + "$");
            SoundManager.successSound(player.getPlayer());
            playerService.addExp(player.getPlayer(), Utils.random(1, 3));
            Main.getInstance().seasonpass.didQuest(player.getPlayer(), 3);

            player.getData().addBankMoney(payout, "Auszahlung Postbote");
            player.setVariable("post", remainingDeliveries - 1);

            array.add(house);
            Utils.waitSeconds(1800, () -> array.removeIf(number -> number == house));

            if (remainingDeliveries - 1 <= 0) {
                player.sendMessage(prefix + "Du hast alles abgegeben. Danke!");
                endJob(player);
            }
        } else {
            player.sendMessage(prefix + "Dieses Haus hat bereits Post erhalten!");
        }
    }
}