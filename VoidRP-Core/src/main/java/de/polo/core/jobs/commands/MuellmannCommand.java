package de.polo.core.jobs.commands;

import de.polo.api.utils.inventorymanager.CustomItem;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.jobs.Job;
import de.polo.api.jobs.enums.MiniJob;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.handler.CommandBase;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.player.SoundManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CommandBase.CommandMeta(
        name = "müllmann",
        usage = "/müllmann"
)
public class MuellmannCommand extends CommandBase implements Job {
    public final String prefix = "§8[§9Müllmann§8] §7";
    private final List<Integer> array = new ObjectArrayList<>();

    public MuellmannCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (ServerManager.canDoJobs()) {
            LocationService locationService = VoidAPI.getService(LocationService.class);
            if (locationService.getDistanceBetweenCoords(player, "muellmann") <= 5) {
                InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §9Müllmann"), true, true);

                // Start Job Option
                if (!playerService.isInJobCooldown(player, MiniJob.WASTE_COLLECTOR) && player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.LIME_DYE, 1, 0, "§aMüllmann starten")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            startJob(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                } else {
                    if (player.getActiveJob() == null) {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mMüllmann starten", "§8 ➥§7 Warte noch " + Utils.getTime(playerService.getJobCooldown(player, MiniJob.WASTE_COLLECTOR)) + "§7.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                            }
                        });
                    } else {
                        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§a§mMüllmann starten", "§8 ➥§7 Du hast bereits den §f" + player.getMiniJob().getName() + "§7 Job angenommen.")) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                            }
                        });
                    }
                }

                // Quit Job Option
                if (player.getActiveJob() == null) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                        }
                    });
                } else if (!player.getMiniJob().equals(MiniJob.WASTE_COLLECTOR)) {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.GRAY_DYE, 1, 0, "§e§mJob beenden", "§8 ➥§7 Du hast den Job nicht angenommen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.YELLOW_DYE, 1, 0, "§eJob beenden", "§8 ➥ §7Du erhälst §a" +
                            (Utils.random(ServerManager.getPayout("muellmann"), ServerManager.getPayout("muellmann2")) * (int) player.getVariable("muellkg")) + "$")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            endJob(player);
                            player.getPlayer().closeInventory();
                        }
                    });
                }
            } else {
                player.sendMessage(Prefix.ERROR + "Du bist §cnicht§7 in der nähe der Mülldeponie§7!");
            }
        } else {
            player.sendMessage(ServerManager.error_cantDoJobs);
        }
    }

    public boolean canGet(int number) {
        return !array.contains(number);
    }

    public void startJob(VoidPlayer player) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (!playerService.isInJobCooldown(player, MiniJob.WASTE_COLLECTOR)) {
            player.setMiniJob(MiniJob.WASTE_COLLECTOR);
            player.setActiveJob(this);

            player.setVariable("muell", Utils.random(2, 5));
            player.setVariable("muellkg", 0);

            player.sendMessage(prefix + "Entleere den Müll verschiedener Häuser.");
            player.sendMessage("§8 ➥ §7Nutze §8[§6Rechtsklick§8]§7 auf die Hausschilder.");
        } else {
            player.sendMessage(prefix + "Du kannst den Job erst in §f" + Utils.getTime(playerService.getJobCooldown(player, MiniJob.WASTE_COLLECTOR)) + "§7 beginnen.");
        }
    }

    @Override
    public void endJob(VoidPlayer player) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        Main.beginnerpass.didQuest(player.getPlayer(), 5);
        Main.seasonpass.didQuest(player.getPlayer(), 2);

        int collectedTrash = (int) player.getVariable("muellkg");
        int payout = Utils.random(ServerManager.getPayout("muellmann"), ServerManager.getPayout("muellmann2")) * collectedTrash;

        if (collectedTrash > 0) {
            player.sendMessage(prefix + "Vielen Dank für die geleistete Arbeit. §a+" + payout + "$");
            player.getData().addBankMoney(payout, "Auszahlung Müllmann");
            SoundManager.successSound(player.getPlayer());
        } else {
            player.sendMessage(prefix + "Du hast keinen Müll eingesammelt.");
        }

        playerService.handleJobFinish(player, MiniJob.WASTE_COLLECTOR, 3600, Utils.random(12, 20));
        player.setMiniJob(null);
        player.setActiveJob(null);
        player.setVariable("muell", null);
        player.setVariable("muellkg", null);
        array.clear();
    }

    public void handleDrop(VoidPlayer player, int house) {
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        if (canGet(house)) {
            int remainingTrash = (int) player.getVariable("muell");
            int collectedTrash = (int) player.getVariable("muellkg");
            int trashAmount = Utils.random(1, 4);

            player.sendMessage(prefix + "Du hast den Müll von §6Haus " + house + "§7 entleert.");
            SoundManager.successSound(player.getPlayer());
            playerService.addExp(player.getPlayer(), Utils.random(1, 3));

            player.setVariable("muell", remainingTrash - 1);
            player.setVariable("muellkg", collectedTrash + trashAmount);

            array.add(house);
            Utils.waitSeconds(1800, () -> array.removeIf(number -> number == house));

            if (remainingTrash - 1 <= 0) {
                int payout = Utils.random(ServerManager.getPayout("muellmann"), ServerManager.getPayout("muellmann2")) * (collectedTrash + trashAmount);
                player.sendMessage(prefix + "Du hast alles eingesammelt. Danke! §a+" + payout + "$");
                player.getData().addBankMoney(payout, "Auszahlung Müllmann");
                endJob(player);
            }
        } else {
            player.sendMessage(prefix + "Dieser Müll wurde bereits eingesammelt!");
        }
    }
}