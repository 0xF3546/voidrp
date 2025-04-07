package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.faction.entity.FactionData;
import de.polo.voidroleplay.faction.service.impl.FactionManager;
import de.polo.voidroleplay.storage.*;
import de.polo.voidroleplay.game.base.shops.ShopData;
import de.polo.voidroleplay.game.base.vehicle.PlayerVehicleData;
import de.polo.voidroleplay.game.base.vehicle.VehicleData;
import de.polo.voidroleplay.game.base.vehicle.Vehicles;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.manager.*;
import de.polo.voidroleplay.utils.inventory.CustomItem;
import de.polo.voidroleplay.utils.inventory.InventoryManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class TabletUtils implements Listener {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final Utils utils;
    private final CompanyManager companyManager;

    public TabletUtils(PlayerManager playerManager, FactionManager factionManager, Utils utils, CompanyManager companyManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.utils = utils;
        this.companyManager = companyManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onTabletUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.IRON_INGOT && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            openTablet(player);
        }
    }

    public void openTablet(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §eTablet", true, true);
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        int i = 2;
        inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.PLAYER_HEAD, 1, 0, "§cFraktionsapp")) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(1, ItemManager.createItem(Material.MINECART, 1, 0, "§6Fahrzeugübersicht")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openVehiclesApp(player, 1);
            }
        });
        if (playerData.getFaction().equalsIgnoreCase("FBI") || playerData.getFaction().equalsIgnoreCase("Polizei")) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.BLUE_DYE, 1, 0, "§1Aktenapp")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openAktenApp(player);
                }
            });
            inventoryManager.setItem(new CustomItem(i + 1, ItemManager.createItem(Material.ORANGE_DYE, 1, 0, "§6Gefängnisapp")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openJailApp(player, 1);
                }
            });
            i = i + 2;
        }
        if (playerData.getFaction().equalsIgnoreCase("FBI")) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.COMPASS, 1, 0, "§3Bürgerdatenbank")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCitizienDatabase(player, 1, null);
                }
            });
            i++;
        }
        if (playerData.getCompany() != null) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.EMERALD, 1, 0, "§6Firma")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCompanyApp(player);
                }
            });
            i++;
        }
    }

    public void openApp(Player player, String app) throws SQLException {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        switch (app) {
            case "aktenapp":
                openAktenApp(player);
                break;
            case "gefängnisapp":
                openJailApp(player, 1);
                break;
            case "vehiclesapp":
                openVehiclesApp(player, 1);
                break;
        }
    }

    private void openCitizienDatabase(Player player, int page, String search) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §3Bürgerdatenbank", true, true);
        int i = 0;
        int j = 0;
        for (PlayerData playersData : playerManager.getPlayers()) {
            if (i == 26 || i == 18 || i == 22) {
                i++;
            } else if (j >= (25 * (page - 1)) && j <= (25 * page)) {
                if (playersData.getLastname() == null || playersData.getFirstname() == null) {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItemHead(playersData.getUuid().toString(), 1, 0, "§cZu dieser Person sind keine Daten bekannt")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItemHead(playersData.getUuid().toString(), 1, 0, "§3" + playersData.getFirstname() + " " + playersData.getLastname())) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                }
            }
            i++;
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCitizienDatabase(player, page + 1, search);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCitizienDatabase(player, page - 1, search);
            }
        });
        inventoryManager.setItem(new CustomItem(21, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openTablet(player);
            }
        });
        if (search == null) {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Suchen...")) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        } else {
            inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Suche leeren")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCitizienDatabase(player, 1, null);
                }
            });
        }
    }

    public void openAktenApp(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (Objects.equals(playerData.getFaction(), "FBI") || Objects.equals(playerData.getFaction(), "Polizei")) {
            InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §1Aktenapp", true, true);
            inventoryManager.setItem(new CustomItem(0, ItemManager.createItem(Material.PAPER, 1, 0, "§9Aktenübersicht")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openAktenList(player, 1, null);
                    playerData.setVariable("targetakte", null);
                }
            });
            inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openTablet(player);
                }
            });
        }
    }

    public void openPlayerAktenList(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        playerData.setIntVariable("current_page", page);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §9Akten §8- §9Seite§8:§7 " + page, true, false);
        int i = 0;
        int j = 0;
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (i == 26 && i == 18 && i == 22) {
                i++;
            } else if (j >= (25 * (page - 1)) && j <= (25 * page)) {
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItemHead(players.getUniqueId().toString(), 1, 0, "§8» §6" + players.getName())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        editPlayerAkte(player, players.getUniqueId());
                    }
                });
                i++;
            }
            inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openPlayerAktenList(player, page + 1);
                }
            });
            inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cLetzte Seite")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openPlayerAktenList(player, page - 1);
                }
            });
            inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openAktenApp(player);
                }
            });
            j++;
        }
    }

    public void editPlayerAkte(Player player, UUID uuid) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        OfflinePlayer targetplayer = Bukkit.getOfflinePlayer(uuid);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §c" + targetplayer.getName(), true, true);
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItemHead(targetplayer.getUniqueId().toString(), 1, 0, "§8» §6" + targetplayer.getName())) {
            @Override
            public void onClick(InventoryClickEvent event) {
            }
        });
        inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(Material.BOOK, 1, 0, "§9Offene Akten")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPlayerAkte(player, targetplayer.getUniqueId(), 1);
            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§9Akte hinzufügen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("targetakte", targetplayer.getUniqueId());
                openAktenList(player, 1, null);
            }
        });
        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
        if (targetplayerData.isJailed()) {
            inventoryManager.setItem(new CustomItem(16, ItemManager.createItem(Material.BARRIER, 1, 0, "§cAus Gefängnis entlassen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    Player target = Bukkit.getPlayer(targetplayerData.getUuid());
                    utils.staatUtil.unarrestPlayer(target);
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        PlayerData playerData1 = playerManager.getPlayerData(players.getUniqueId());
                        if (Objects.equals(playerData1.getFaction(), "FBI") || Objects.equals(playerData1.getFaction(), "Polizei")) {
                            players.sendMessage("§8[§cGefängnis§8] §6" + factionManager.getTitle(player) + " " + player.getName() + "§7 hat §6" + target.getName() + "§7 entlassen.");
                        }
                    }
                    player.closeInventory();
                }
            });
        }
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPlayerAktenList(player, 1);
            }
        });
    }

    @SneakyThrows
    public void openAktenList(Player player, int page, String search) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §9Aktenübersicht §8- §9Seite§8:§7 " + page, true, false);
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = null;
        if (search == null) {
            result = statement.executeQuery("SELECT `id`, `akte`, `hafteinheiten`, `geldstrafe` FROM `akten`");
        } else {
            result = statement.executeQuery("SELECT `id`, `akte`, `hafteinheiten`, `geldstrafe` FROM `akten` WHERE LOWER(`akte`) LIKE LOWER('%" + search + "%') ");
        }
        int i = 0;
        while (result.next()) {
            if (result.getRow() >= (18 * (page - 1)) && result.getRow() <= (18 * page)) {
                int id = result.getInt(1);
                int value1 = result.getInt(3);
                String value2 = result.getString(2);
                int value3 = result.getInt(4);

                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§8» §3" + value2, Arrays.asList("§8 ➥ §bHafteinheiten§8:§7 " + value1, "§8 ➥ §bGeldstrafe§8:§7 " + value3 + "$"))) {
                    @SneakyThrows
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (playerData.isLeader()) {
                            if (event.isRightClick()) {
                                player.sendMessage("§aDu hast die Akte " + value2 + " gelöscht.");
                                statement.execute("DELETE FROM akten WHERE id = " + id);
                                player.closeInventory();
                            }
                        }
                        if (playerData.getVariable("targetakte") == null) {
                            return;
                        }
                        Player targetPlayer = Bukkit.getPlayer(UUID.fromString(playerData.getVariable("targetakte").toString()));
                        utils.staatUtil.addAkteToPlayer(player, targetPlayer, value1, value2, value3);
                    }
                });
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openAktenList(player, page + 1, search);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openAktenList(player, page - 1, search);
            }
        });
        inventoryManager.setItem(new CustomItem(21, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openAktenApp(player);
            }
        });
        if (playerData.isLeader())
            inventoryManager.setItem(new CustomItem(22, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aAkte einfügen", null)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    playerData.setIntVariable("input_hafteinheiten", 0);
                    playerData.setIntVariable("input_geldstrafe", 0);
                    playerData.setVariable("input_akte", null);
                    createAkte(player);
                }
            });
        inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.CLOCK, 1, 0, "§7Akte suchen...")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "aktensearch");
                player.sendMessage("§8[§9Akte§8]§7 Gib nun die Akte ein.");
                player.closeInventory();
            }
        });
        result.close();
    }

    @SneakyThrows
    public void openPlayerAkte(Player player, UUID target, int page) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §9Aktenübersicht §8- §9Seite§8:§7 " + page, true, false);
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT `id`, `akte`, `hafteinheiten`, `geldstrafe`, `vergebendurch`, DATE_FORMAT(datum, '%d.%m.%Y | %H:%i:%s') AS formatted_timestamp FROM `player_akten` WHERE `uuid` = '" + target + "'");
        Player targetplayer = Bukkit.getPlayer(target);
        int i = 0;
        while (result.next()) {
            if (i == 26 && i == 18 && i == 22) {
                i++;
            } else if (result.getRow() >= (25 * (page - 1)) && result.getRow() <= (25 * page)) {
                int id = result.getInt("id");
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.WRITTEN_BOOK, 1, 0, "§8» §3" + result.getString(2), Arrays.asList("§8 ➥ §bHafteinheiten§8:§7 " + result.getInt(3), "§8 ➥ §bGeldstrafe§8:§7 " + result.getInt(4) + "$", "§8 ➥ §bDurch§8:§7 " + result.getString(5), "§8 ➥ §bDatum§8:§7 " + result.getString("formatted_timestamp")))) {
                    @SneakyThrows
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        utils.staatUtil.removeAkteFromPlayer(player, id);
                        event.getCurrentItem().setType(Material.BLACK_STAINED_GLASS_PANE);
                        player.sendMessage("§8[§9Zentrale§8] §7Akte von " + targetplayer.getName() + " entfernt.");
                    }
                });
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPlayerAkte(player, target, page + 1);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPlayerAkte(player, target, page + 1);
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPlayerAktenList(player, 1);
            }
        });
        result.close();
    }

    public void openJailApp(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (!playerData.getFaction().equalsIgnoreCase("FBI") && !playerData.getFaction().equalsIgnoreCase("Polizei"))
            return;
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §6Gefängnis §8- §6Seite§8:§7 " + page, true, true);
        int i = 0;
        int j = 0;
        for (JailData jailData : StaatUtil.jailDataMap.values()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(jailData.getUuid()));
            if (offlinePlayer.isOnline()) {
                if (i == 26 && i == 18 && i == 22) {
                    i++;
                } else if (j >= (25 * (page - 1)) && j <= (25 * page)) {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItemHead(jailData.getUuid(), 1, 0, "§8» §6" + offlinePlayer.getName())) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            editPlayerAkte(player, offlinePlayer.getUniqueId());
                        }
                    });
                    i++;
                }
                j++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openJailApp(player, page + 1);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openJailApp(player, page - 1);
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openTablet(player);
            }
        });

    }

    public void openVehiclesApp(Player player, int page) {
        if (page <= 0) return;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8» §6Fahrzeuge §8- §6Seite§8:§7 " + page, true, false);
        int i = 0;
        int j = 0;
        for (PlayerVehicleData playerVehicleData : Vehicles.playerVehicleDataMap.values()) {
            if (playerVehicleData.getUuid().equals(player.getUniqueId().toString())) {
                if (i >= (18 * (page - 1)) && i < (18 * page)) {
                    VehicleData vehicleData = Vehicles.vehicleDataMap.get(playerVehicleData.getType());
                    int slotIndex = i % 9;
                    j++;
                    if (j > 9) {
                        slotIndex += 9;
                    }

                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.MINECART, 1, 0, "§6" + vehicleData.getName(), Arrays.asList("§8 ➥ §eID§8:§7 " + playerVehicleData.getId(), "§8 » §aOrten", "§8[§6Rechtsklick§8]§7 Verkaufen"))) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            if (event.isRightClick()) {
                                int price = (int) (vehicleData.getPrice() * 0.75);
                                InventoryManager sellInventory = new InventoryManager(player, 27, "§8 » §e" + vehicleData.getName() + " verkaufen");
                                sellInventory.setItem(new CustomItem(12, ItemManager.createItem(Material.GREEN_WOOL, 1, 0, "§aVerkaufen", "§8 ➥ §7" + Utils.toDecimalFormat(price) + "$")) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {
                                        player.closeInventory();
                                        try {
                                            player.sendMessage(Prefix.MAIN + "Du hast dein " + vehicleData.getName() + " verkauft.");
                                            playerData.addMoney(price, "Verkauf " + vehicleData.getName());
                                            Vehicles.deleteVehicleById(vehicleData.getId());
                                            Main.getInstance().vehicles.removeVehicleFromDatabase(playerVehicleData.getId());
                                            player.sendMessage(Component.text(Prefix.MAIN + "Du hast dein Fahrzeug verkauft."));
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });
                                sellInventory.setItem(new CustomItem(14, ItemManager.createItem(Material.RED_WOOL, 1, 0, "§cAbbrechen")) {
                                    @Override
                                    public void onClick(InventoryClickEvent event) {
                                        openVehiclesApp(player, page);
                                    }
                                });
                            } else {
                                player.closeInventory();
                                utils.navigationManager.createNaviByCord(player, playerVehicleData.getX(), playerVehicleData.getY(), playerVehicleData.getZ());
                                player.sendMessage("§8[§3Tablet§8]§7 Der Standort deines Fahrzeuges wurde dir markiert.");
                            }
                        }
                    });
                }
                i++;
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.GOLD_NUGGET, 1, 0, "§cNächste Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openVehiclesApp(player, page + 1);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cVorherige Seite")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openVehiclesApp(player, page - 1);
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openTablet(player);
            }
        });

    }

    public void createAkte(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §aAkte einfügen");
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.CHEST, 1, 0, "§aAkte", playerData.getVariable("input_reason") != null ? "§8 ➥ §e" + playerData.getVariable("input_reason") : "§8 ➥ §cNicht angegeben")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "createakte_reason");
                player.closeInventory();
                player.sendMessage("§8[§aAkte§8]§7 Gib nun den Namen der Akte an.");
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.CHEST, 1, 0, "§aWantedpunkte", playerData.getVariable("input_wanted") != null ? "§8 ➥ §e" + playerData.getVariable("input_wanted") : "§8 ➥ §cNicht angegeben")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "createakte_wanted");
                player.closeInventory();
                player.sendMessage("§8[§aAkte§8]§7 Gib nun die Wantedpunkte an.");
            }
        });
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aBestätigen")) {
            @SneakyThrows
            @Override
            public void onClick(InventoryClickEvent event) {
                createNewAkte(player);
            }
        });
    }

    public void createNewAkte(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        Main.getInstance().getMySQL().insertAndGetKeyAsync("INSERT INTO wantedreasons (reason, wanted) VALUES (?, ?)", playerData.getVariable("input_reason"), playerData.getVariable("input_wanted"))
                .thenApply(key -> {
                    if (key.isPresent()) {
                        WantedReason wantedReason = new WantedReason(key.get(), playerData.getVariable("input_reason"), playerData.getVariable("input_wanted"));
                        utils.staatUtil.addWantedReason(wantedReason);
                    }
                    return null;
                });
        player.sendMessage("§8[§aAkte§8]§7 Akte wurde hinzugefügt.");
        player.closeInventory();
    }

    public void openCompanyApp(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6" + playerData.getCompany().getName(), true, true);
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§6Rollen verwalten")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openRoles(player);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.PLAYER_HEAD, 1, 0, "§6Mitarbeiter verwalten")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCompanyMember(player);
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§6Assets verwalten")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openAssetApp(player, false);
            }
        });
        inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openTablet(player);
            }
        });
    }

    public void openAssetApp(Player player, boolean isAddingPermission) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Assets", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCompanyApp(player);
            }
        });
        int i = 0;
        for (ShopData shopData : ServerManager.shopDataMap.values()) {
            if (shopData.getCompany() == null) continue;
            if (shopData.getCompany().equals(playerData.getCompany())) {
                if (!isAddingPermission) {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§6" + shopData.getName(), "§8 ➥ §eID§8:§7 " + shopData.getId())) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    CompanyRole role = playerData.getVariable("tablet::company::role");
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§6" + shopData.getName(), "§8 ➥ §eVerwaltung für  " + role.getName() + " hinzufügen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            role.addPermission("manage_shop_" + shopData.getId());
                            editRole(player, role);
                            role.save();
                        }
                    });
                }
                i++;
            }
        }
        for (GasStationData gasStationData : LocationManager.gasStationDataMap.values()) {
            if (gasStationData.getCompany() == null) continue;
            if (gasStationData.getCompany().equals(playerData.getCompany())) {
                if (!isAddingPermission) {
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§6Tankstelle " + gasStationData.getName(), "§8 ➥ §eID§8:§7 " + gasStationData.getId())) {
                        @Override
                        public void onClick(InventoryClickEvent event) {

                        }
                    });
                } else {
                    CompanyRole role = playerData.getVariable("tablet::company::role");
                    inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§6" + gasStationData.getName(), "§8 ➥ §eVerwaltung für  " + role.getName() + " hinzufügen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            role.addPermission("manage_gas_" + gasStationData.getId());
                            editRole(player, role);
                            role.save();
                        }
                    });
                }
                i++;
            }
        }
    }

    @SneakyThrows
    public void openCompanyMember(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Mitarbeiter", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCompanyApp(player);
            }
        });
        Connection connection = Main.getInstance().mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT player_name, companyRole, uuid FROM players WHERE company = ?");
        statement.setInt(1, playerData.getCompany().getId());
        ResultSet resultSet = statement.executeQuery();
        int i = 0;
        while (resultSet.next()) {
            String roleName = "Keine Rolle";
            CompanyRole role = companyManager.getCompanyRoleById(resultSet.getInt("companyRole"));
            if (role != null) {
                roleName = role.getName();
            }
            String uuid = resultSet.getString("uuid");
            String player_name = resultSet.getString("player_name");
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItemHead(uuid, 1, 0, "§6" + player_name, "§8 ➥ §e" + roleName)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    editCompanyPlayer(player, uuid, player_name);
                }
            });
            i++;
        }
        statement.close();
        connection.close();
    }

    private void editCompanyPlayer(Player player, String uuid, String player_name) {
        PlayerData pData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Mitarbeiter (" + player_name + ")", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCompanyMember(player);
            }
        });
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItemHead(uuid, 1, 0, "§6" + player_name)) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.PAPER, 1, 0, "§eRolle setzen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openPlayerSetRole(player, uuid, player_name);
            }
        });
        if (pData.getCompanyRole().hasPermission("*") || pData.getCompany().getOwner().equals(player.getUniqueId()) || pData.getCompanyRole().hasPermission("manage_employees")) {
            inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.RED_DYE, 1, 0, "§cEntlassen")) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.sendMessage("§8[§6" + pData.getCompany().getName() + "§8]§c Du wurdest hast " + player_name + " aus der Firma entlassen.");
                    for (PlayerData playerData : playerManager.getPlayers()) {
                        if (playerData.getUuid().toString().equals(uuid)) {
                            playerData.getPlayer().sendMessage("§8[§6" + playerData.getCompany().getName() + "§8]§c Du wurdest von " + player.getName() + " aus der Firma entlassen.");
                            playerData.setCompany(null);
                            playerData.setCompanyRole(null);
                        }
                    }
                    Connection connection = Main.getInstance().mySQL.getConnection();
                    PreparedStatement statement = connection.prepareStatement("UPDATE players SET company = 0, companyRole = 0 WHERE uuid = ?");
                    statement.setString(1, uuid);
                    statement.execute();
                    statement.close();
                    connection.close();
                }
            });
        }
    }

    private void openPlayerSetRole(Player player, String uuid, String player_name) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Mitarbeiter Rolle (" + player_name + ")", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                editCompanyPlayer(player, uuid, player_name);
            }
        });
        int i = 0;
        for (CompanyRole role : playerData.getCompany().getRoles()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.PAPER, 1, 0, "§e" + role.getName())) {
                @SneakyThrows
                @Override
                public void onClick(InventoryClickEvent event) {
                    Connection connection = Main.getInstance().mySQL.getConnection();
                    PreparedStatement statement = connection.prepareStatement("UPDATE players SET companyRole = ? WHERE uuid = ?");
                    statement.setInt(1, role.getId());
                    statement.setString(2, uuid);
                    statement.execute();
                    statement.close();
                    connection.close();
                    for (PlayerData pd : playerManager.getPlayers()) {
                        if (pd.getUuid().toString().equals(uuid)) {
                            pd.setCompanyRole(role);
                        }
                    }
                    editCompanyPlayer(player, uuid, player_name);
                }
            });
            i++;
        }
    }

    public void openRoles(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Rollen verwalten (" + playerData.getCompany().getRoles().size() + ")", true, true);
        int i = 0;
        for (CompanyRole role : playerData.getCompany().getRoles()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§6" + role.getName())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getCompanyRole() == null) return;
                    if (!playerData.getCompanyRole().hasPermission("*") && !playerData.getCompanyRole().hasPermission("manage_assets") && playerData.getCompany().getOwner().equals(player.getUniqueId())) {
                        return;
                    }
                    if (event.isLeftClick()) {
                        editRole(player, role);
                    } else {
                        playerData.getCompany().deleteRole(role);
                        openRoles(player);
                    }
                }
            });

            i++;
        }
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCompanyApp(player);
            }
        });
        if (playerData.getCompanyRole() == null) return;
        if (playerData.getCompanyRole().hasPermission("*") || playerData.getCompany().getOwner().equals(player.getUniqueId())) {
            inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§2Neu erstellen")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    CompanyRole role = new CompanyRole();
                    role.setName("Neue Rolle");
                    role.setPermissions(new ObjectArrayList<>());
                    playerData.getCompany().createRole(role);
                    openRoles(player);
                }
            });
        }
    }

    private void editRole(Player player, CompanyRole role) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§8 » §6Rolle bearbeiten (" + role.getName() + ")", true, true);
        inventoryManager.setItem(new CustomItem(4, ItemManager.createItem(Material.PAPER, 1, 0, "§6" + role.getName())) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "tablet::company::role::setname");
                player.closeInventory();
                player.sendMessage(Prefix.MAIN + "Gib nun den Namen der Rolle an.");
                playerData.setVariable("temp_role", role);
            }
        });
        int i = 9;
        for (String permission : role.getPermissions()) {
            if (permission.contains("manage_shop")) {
                ShopData shop = null;
                int shopId = Integer.parseInt(permission.replace("manage_shop_", ""));
                for (ShopData shopData : ServerManager.shopDataMap.values()) {
                    if (shopData.getId() == shopId) {
                        shop = shopData;
                    }
                }
                if (shop == null) continue;
                inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(Material.OAK_SIGN, 1, 0, "§eVerwaltung von Shop " + shop.getName())) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        role.removePermission(permission);
                        editRole(player, role);
                        role.save();
                    }
                });
            }
        }
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openRoles(player);
            }
        });
        if (playerData.getCompanyRole().hasPermission("*") || playerData.getCompany().getOwner().equals(player.getUniqueId())) {
            if (role.hasPermission("*")) {
                inventoryManager.setItem(new CustomItem(20, ItemManager.createItem(Material.PAPER, 1, 0, "§6Prokura", "§8 ➥ §aAktiviert")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        role.removePermission("*");
                        editRole(player, role);
                        role.save();
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(20, ItemManager.createItem(Material.PAPER, 1, 0, "§6Prokura", "§8 ➥ §cDeaktiviert")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        role.addPermission("*");
                        editRole(player, role);
                        role.save();
                    }
                });
            }
            if (role.hasPermission("manage_employees")) {
                inventoryManager.setItem(new CustomItem(21, ItemManager.createItem(Material.PAPER, 1, 0, "§6Rollen verwalten", "§8 ➥ §aAktiviert")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        role.removePermission("manage_employees");
                        editRole(player, role);
                        role.save();
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(21, ItemManager.createItem(Material.PAPER, 1, 0, "§6Rollen verwalten", "§8 ➥ §cDeaktiviert")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        role.addPermission("manage_employees");
                        editRole(player, role);
                        role.save();
                    }
                });
            }
            if (role.hasPermission("manage_assets")) {
                inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.PAPER, 1, 0, "§6Assets verwalten", "§8 ➥ §aAktiviert")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        role.removePermission("manage_assets");
                        editRole(player, role);
                        role.save();
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(22, ItemManager.createItem(Material.PAPER, 1, 0, "§6Assets verwalten", "§8 ➥ §cDeaktiviert")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        role.addPermission("manage_assets");
                        editRole(player, role);
                        role.save();
                    }
                });
            }
            if (role.hasPermission("manage_bank")) {
                inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.PAPER, 1, 0, "§6Vermögen verwalten", "§8 ➥ §aAktiviert")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        role.removePermission("manage_bank");
                        editRole(player, role);
                        role.save();
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(23, ItemManager.createItem(Material.PAPER, 1, 0, "§6Vermögen verwalten", "§8 ➥ §cDeaktiviert")) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        role.addPermission("manage_bank");
                        editRole(player, role);
                        role.save();
                    }
                });
            }
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§2Hinzufügen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                /*playerData.setVariable("chatblock", "tablet::company::role::addpermission");
                player.closeInventory();
                player.sendMessage(Main.prefix + "Gib nun die Permission an");*/
                playerData.setVariable("tablet::company::role", role);
                openAssetApp(player, true);
            }
        });
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) throws SQLException {
        if (event.getSubmitTo().equalsIgnoreCase("tablet::company::role::setname")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            CompanyRole role = event.getPlayerData().getVariable("temp_role");
            role.setName(event.getMessage());
            editRole(event.getPlayer(), role);
            role.save();
        }
        if (event.getSubmitTo().equalsIgnoreCase("tablet::company::role::addpermission")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            CompanyRole role = event.getPlayerData().getVariable("temp_role");
            role.addPermission(event.getMessage());
            editRole(event.getPlayer(), role);
            role.save();
        }
        if (event.getSubmitTo().equals("aktensearch")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            openAktenList(event.getPlayer(), 1, event.getMessage());
            event.end();
        }
        if (event.getSubmitTo().equals("createakte_reason")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            event.getPlayerData().setVariable("input_reason", event.getMessage());
            createAkte(event.getPlayer());
            event.end();
        }
        if (event.getSubmitTo().equals("createakte_wanted")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int input = 0;
            try {
                input = Integer.parseInt(event.getMessage());
            } catch (IllegalArgumentException e) {
                event.getPlayer().sendMessage(Prefix.ERROR + "Du hast keine gültige Zahl angegeben");
                event.end();
                return;
            }
            event.getPlayerData().setVariable("input_wanted", input);
            createAkte(event.getPlayer());
            event.end();
        }
    }
}
