package de.polo.voidroleplay.utils.player;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.ATM;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.game.events.SubmitChatEvent;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.inventory.CustomItem;
import de.polo.voidroleplay.manager.inventory.InventoryManager;
import de.polo.voidroleplay.manager.ItemManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.manager.ServerManager;
import de.polo.voidroleplay.utils.Prefix;
import de.polo.voidroleplay.utils.Utils;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BankingUtils implements Listener {
    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final List<ATM> atmList = new ObjectArrayList<>();

    @SneakyThrows
    public BankingUtils(PlayerManager playerManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
        Statement statement = Main.getInstance().mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM atm LEFT JOIN blocks ON atm.blockId = blocks.id");
        while (result.next()) {
            ATM atm = new ATM();
            atm.setId(result.getInt("atm.id"));
            atm.setName(result.getString("atm.name"));
            atm.setLocation(new Location(Bukkit.getWorld(result.getString("blocks.world")), result.getDouble("blocks.x"), result.getDouble("blocks.y"), result.getDouble("blocks.z")));
            atmList.add(atm);
        }
    }

    public Collection<ATM> getATMs() {
        return atmList;
    }

    public void sendKontoauszug(Player player) {
        player.sendMessage(" ");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §aBankguthaben§8:§7 " + playerManager.bank(player) + "$");
        player.sendMessage(" ");
    }

    public void sendBankChangeReason(Player player, String reason) {
        player.sendMessage(" ");
        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
        player.sendMessage(" ");
        player.sendMessage("§8 ➜ §3Kontoveränderung§8:§b " + reason);
        player.sendMessage(" ");
        player.sendMessage("§8 ➥ §aBankguthaben§8:§7 " + playerManager.bank(player) + "$");
        player.sendMessage(" ");
    }

    public void openBankMenu(Player player, ATM atm) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        InventoryManager inventoryManager = new InventoryManager(player, 45, "§8 » §aBankautomat " + atm.getId(), true, true);
        playerData.setVariable("atm", atm);
        playerData.setVariable("atm_name", atm.getName());
        inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNWEzOGQ2YmZjYTU5Nzg2NDE3MzM2M2QyODRhOGQzMjljYWFkOTAxOGM2MzgxYjFiNDI5OWI4YjhiOTExYyJ9fX0=", 1, 0, "§cAuszahlen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "atm_auszahlen");
                player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmZmFkMzNkMjkzYjYxNzY1ZmM4NmFiNTU2MDJiOTU1YjllMWU3NTdhOGU4ODVkNTAyYjNkYmJhNTQyNTUxNyJ9fX0=", 1, 0, "§bKontostand", Collections.singletonList("§8 ➥ §a" + new DecimalFormat("#,###").format(playerData.getBank()) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aEinzahlen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "atm_einzahlen");
                player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(29, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNmZjkxZGM5OWQ1ODI4MDIzZWVkZjg3Mzc5OWQyNTUzNWRhZGU2NGEyZTE2YTNiNDk4YjQxMTNlYWZkNDk2NiJ9fX0=", 1, 0, "§cAlles auszahlen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.sendMessage("§8[§aATM§8]§a Du hast " + playerData.getBank() + "$ ausgezahlt.");
                playerData.addMoney(playerData.getBank(), "Bankauszahlung");
                playerData.removeBankMoney(playerData.getBank(), "Bankauszahlung");
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(33, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI1MGIzY2NlNzY2MzVlZjRjN2E4OGIyYzU5N2JkMjc0OTg2OGQ3OGY1YWZhNTY2MTU3YzI2MTJhZTQxMjAifX19", 1, 0, "§aAlles einzahlen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.sendMessage("§8[§aATM§8]§a Du hast " + playerData.getBargeld() + "$ eingezahlt.");
                playerData.addBankMoney(playerData.getBargeld(), "Bankeinzahlung");
                playerData.removeMoney(playerData.getBargeld(), "Bankeinzahlung");
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(31, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjc2ZGNiMjc0ZTJlMjMxNDlmNDcyNzgxNjA1YjdjNmY4Mzk5MzFhNGYxZDJlZGJkMWZmNTQ2M2FiN2M0MTI0NiJ9fX0=", 1, 0, "§7Geld überweisen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "atm_transfer_player");
                player.closeInventory();
                player.sendMessage("§8[§aATM§8]§7 Gib den Spieler an, an wen das Geld überwiesen werden soll.");
            }
        });
        if (atm.getLastTimeBlown() == null || Duration.between(atm.getLastTimeBlown(), LocalDateTime.now()).toHours() >= 1) {
            if (playerData.getAtmBlown() < 3) {
                if (ItemManager.getCustomItemCount(player, RoleplayItem.EXPLOSION_DEVICE) >= 1) {
                    inventoryManager.setItem(new CustomItem(36, ItemManager.createItem(Material.TNT, 1, 0, "§cAutomat sprengen")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                            if (!ServerManager.canDoJobs()) {
                                return;
                            }
                            Bukkit.getWorld("world").spawnParticle(Particle.EXPLOSION_HUGE, player.getLocation(), 3);
                            Bukkit.getWorld("world").playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                            ItemManager.removeCustomItem(player, RoleplayItem.EXPLOSION_DEVICE, 1);
                            openRobInventory(player, atm);
                            atm.setLastTimeBlown(LocalDateTime.now());
                            playerData.setAtmBlown(playerData.getAtmBlown() + 1);
                            playerData.save();
                            boolean isRandom = Utils.isRandom(2);
                            if (isRandom) {
                                factionManager.sendCustomMessageToFaction("Polizei", "§8[§cATM-Sicherheitssystem§8]§c Der Bankautomat " + atm.getName() + " meldet Alarm aufgrund einer Sprengung. Die Kameras zeigen " + player.getName() + " als Täter.");
                            } else {
                                factionManager.sendCustomMessageToFaction("Polizei", "§8[§cATM-Sicherheitssystem§8]§c Der Bankautomat " + atm.getName() + " meldet Alarm aufgrund einer Sprengung.");
                            }
                        }
                    });
                } else {
                    inventoryManager.setItem(new CustomItem(36, ItemManager.createItem(Material.TNT, 1, 0, "§c§mAutomat sprengen", "§8 ➥ §7Dafür benötigst du einen Sprengsatz.")) {
                        @Override
                        public void onClick(InventoryClickEvent event) {
                        }
                    });
                }
            }
        }

        if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
            inventoryManager.setItem(new CustomItem(44, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGVmMzU2YWQyYWE3YjE2NzhhZWNiODgyOTBlNWZhNWEzNDI3ZTVlNDU2ZmY0MmZiNTE1NjkwYzY3NTE3YjgifX19", 1, 0, "§aFraktionskonto", null)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.getFaction() != null && !playerData.getFaction().equals("Zivilist")) {
                        openFactionBankMenu(player);
                        Main.getInstance().getCooldownManager().setCooldown(player, "atm", 1);
                    }
                }
            });
        }
        if (playerData.getCompany() != null) {
            inventoryManager.setItem(new CustomItem(35, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGVmMzU2YWQyYWE3YjE2NzhhZWNiODgyOTBlNWZhNWEzNDI3ZTVlNDU2ZmY0MmZiNTE1NjkwYzY3NTE3YjgifX19", 1, 0, "§aFirmenkonto", null)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCompanyBankMenu(player);
                    Main.getInstance().getCooldownManager().setCooldown(player, "atm", 1);
                }
            });
        }
    }

    private void openCompanyBankMenu(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 45, "§8 » §6Firmenkonto", true, true);
        inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmZmFkMzNkMjkzYjYxNzY1ZmM4NmFiNTU2MDJiOTU1YjllMWU3NTdhOGU4ODVkNTAyYjNkYmJhNTQyNTUxNyJ9fX0=", 1, 0, "§bKontostand", Collections.singletonList("§8 ➥ §a" + new DecimalFormat("#,###").format(playerData.getCompany().getBank()) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aEinzahlen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "atm_company_einzahlen");
                player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(33, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI1MGIzY2NlNzY2MzVlZjRjN2E4OGIyYzU5N2JkMjc0OTg2OGQ3OGY1YWZhNTY2MTU3YzI2MTJhZTQxMjAifX19", 1, 0, "§aAlles einzahlen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.sendMessage("§8[§aATM§8]§a Du hast " + playerData.getBargeld() + "$ eingezahlt.");
                Main.getInstance().companyManager.sendCompanyMessage(playerData.getCompany(), "§8[§6" + playerData.getCompany().getName() + "§8]§e " + player.getName() + " hat " + Utils.toDecimalFormat(playerData.getBargeld()) + "$ auf das Firmenkonto eingezahlt.");
                playerData.getCompany().addBank(playerData.getBargeld());
                playerData.removeMoney(playerData.getBargeld(), "Bankeinzahlung auf " + playerData.getCompany().getName());
                player.closeInventory();
            }
        });
        if (playerData.getCompanyRole().hasPermission("*") || playerData.getCompany().getOwner().equals(player.getUniqueId()) || playerData.getCompanyRole().hasPermission("manage_bank")) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNWEzOGQ2YmZjYTU5Nzg2NDE3MzM2M2QyODRhOGQzMjljYWFkOTAxOGM2MzgxYjFiNDI5OWI4YjhiOTExYyJ9fX0=", 1, 0, "§cAuszahlen", null)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.isLeader()) {
                        playerData.setVariable("chatblock", "atm_company_auszahlen");
                        player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                        player.closeInventory();
                    }
                }
            });
            inventoryManager.setItem(new CustomItem(29, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNmZjkxZGM5OWQ1ODI4MDIzZWVkZjg3Mzc5OWQyNTUzNWRhZGU2NGEyZTE2YTNiNDk4YjQxMTNlYWZkNDk2NiJ9fX0=", 1, 0, "§cAlles auszahlen", null)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.isLeader()) {
                        Main.getInstance().companyManager.sendCompanyMessage(playerData.getCompany(), "§8[§6" + playerData.getCompany().getName() + "§8]§e " + player.getName() + " hat " + Utils.toDecimalFormat(playerData.getCompany().getBank()) + "$ vom Firmenkonto ausgezahlt.");
                        playerData.addMoney(playerData.getCompany().getBank(), "Company-Auszwahlung - " + playerData.getCompany().getId());
                        playerData.getCompany().removeBank(playerData.getCompany().getBank());
                        player.closeInventory();
                    }
                }
            });
        }
        inventoryManager.setItem(new CustomItem(44, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjUzNDc0MjNlZTU1ZGFhNzkyMzY2OGZjYTg1ODE5ODVmZjUzODlhNDU0MzUzMjFlZmFkNTM3YWYyM2QifX19", 1, 0, "§aPrivates Konto", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "atm"))
                    openBankMenu(player, playerData.getVariable("atm"));
            }
        });
    }

    private void openRobInventory(Player player, ATM atm) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 54, "§cATM-Raub " + atm.getName(), true, true);
        playerData.setVariable("atm::rob::collected", 0);
        for (int i = 0; i < Main.random(12, 20); i++) {
            int cash = Main.random(15, 25);
            inventoryManager.setItem(new CustomItem(Main.random(0, 53), ItemManager.createItem(Material.GREEN_DYE, 1, 0, "§2+" + cash + "$")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    ItemStack item = event.getCurrentItem();
                    item.setType(Material.BLACK_STAINED_GLASS_PANE);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("§2");
                    item.setItemMeta(meta);
                    event.setCurrentItem(item);
                    int amount = 0;
                    for (ItemStack inventoryItem : event.getInventory().getContents()) {
                        if (inventoryItem.getType() == Material.GREEN_DYE) {
                            amount++;
                        }
                    }
                    playerData.setVariable("atm::rob::collected", (int) playerData.getVariable("atm::rob::collected") + cash);
                    if (amount > 0) {
                        SoundManager.openSound(player);
                        return;
                    }
                    player.closeInventory();
                    SoundManager.successSound(player);
                    int money = playerData.getVariable("atm::rob::collected");
                    player.sendMessage("§8[§2ATM§8]§a +" + money + "$");
                    playerData.addMoney(money, "ATM-Sprengung");
                }
            });
        }
    }

    public void openFactionBankMenu(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        FactionData factionData = factionManager.getFactionData(playerData.getFaction());
        InventoryManager inventoryManager = new InventoryManager(player, 45, "§8 » §" + factionData.getPrimaryColor() + "Fraktionskonto", true, true);
        inventoryManager.setItem(new CustomItem(13, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBmZmFkMzNkMjkzYjYxNzY1ZmM4NmFiNTU2MDJiOTU1YjllMWU3NTdhOGU4ODVkNTAyYjNkYmJhNTQyNTUxNyJ9fX0=", 1, 0, "§bKontostand", Collections.singletonList("§8 ➥ §a" + new DecimalFormat("#,###").format(factionData.getBank()) + "$"))) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19", 1, 0, "§aEinzahlen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                playerData.setVariable("chatblock", "atm_frak_einzahlen");
                player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                player.closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(33, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI1MGIzY2NlNzY2MzVlZjRjN2E4OGIyYzU5N2JkMjc0OTg2OGQ3OGY1YWZhNTY2MTU3YzI2MTJhZTQxMjAifX19", 1, 0, "§aAlles einzahlen", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.sendMessage("§8[§aATM§8]§a Du hast " + playerData.getBargeld() + "$ eingezahlt.");
                factionManager.sendMessageToFaction(factionData.getName(), player.getName() + " hat " + playerData.getBargeld() + "$ auf das Fraktionskonto eingezahlt.");
                factionData.addBankMoney(playerData.getBargeld(), "Bankeinzahlung " + player.getName());
                playerData.removeMoney(playerData.getBargeld(), "Bankeinzahlung auf " + factionData.getName());
                player.closeInventory();
            }
        });
        if (playerData.isLeader()) {
            inventoryManager.setItem(new CustomItem(11, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhkNWEzOGQ2YmZjYTU5Nzg2NDE3MzM2M2QyODRhOGQzMjljYWFkOTAxOGM2MzgxYjFiNDI5OWI4YjhiOTExYyJ9fX0=", 1, 0, "§cAuszahlen", null)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.isLeader()) {
                        playerData.setVariable("chatblock", "atm_frak_auszahlen");
                        player.sendMessage("§8[§aATM§8]§7 Gib nun einen Wert ein.");
                        player.closeInventory();
                    }
                }
            });
            inventoryManager.setItem(new CustomItem(29, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNmZjkxZGM5OWQ1ODI4MDIzZWVkZjg3Mzc5OWQyNTUzNWRhZGU2NGEyZTE2YTNiNDk4YjQxMTNlYWZkNDk2NiJ9fX0=", 1, 0, "§cAlles auszahlen", null)) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.isLeader()) {
                        player.sendMessage("§8[§aATM§8]§a Du hast " + factionData.getBank() + "$ ausgezahlt.");
                        factionManager.sendMessageToFaction(factionData.getName(), player.getName() + " hat " + factionData.getBank() + "$ vom Fraktionskonto ausgezahlt.");
                        playerData.addMoney(factionData.getBank(), "Fraktionsbank-Auszahlung - " + factionData.getName());
                        factionData.removeFactionMoney(factionData.getBank(), "Bankauszahlung " + player.getName());
                        player.closeInventory();
                    }
                }
            });
        }
        inventoryManager.setItem(new CustomItem(44, ItemManager.createCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjUzNDc0MjNlZTU1ZGFhNzkyMzY2OGZjYTg1ODE5ODVmZjUzODlhNDU0MzUzMjFlZmFkNTM3YWYyM2QifX19", 1, 0, "§aPrivates Konto", null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!Main.getInstance().getCooldownManager().isOnCooldown(player, "atm"))
                    openBankMenu(player, playerData.getVariable("atm"));
            }
        });
    }

    private void openTransactionMenu(Player player, Player target) {
        openTransactionMenu(player, target, -1);
    }

    @SneakyThrows
    private void openTransactionMenu(Player player, Player target, int amount) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7Transaktion");
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItemHead(target.getUniqueId().toString(), 1, 0, "§7" + target.getName())) {
            @Override
            public void onClick(InventoryClickEvent event) {

            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.PAPER, 1, 0, "§7Betrag angeben")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.closeInventory();
                player.sendMessage("§8[§aATM§8]§7 Gib nun an, wie viel Geld " + target.getName() + " erhalten soll.");
                playerData.setVariable("chatblock", "atm_transfer_amount");
                playerData.setVariable("transfer_player", target.getName());
            }
        });
        if (amount >= 1) {
            playerData.setVariable("transfer_player", null);
            if (playerManager.bank(player) >= amount) {
                inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aTransaktion bestätigen", "§8 ➥ §7" + amount + "$")) {
                    @SneakyThrows
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        player.closeInventory();
                        if (playerData.getBank() < amount) {
                            player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld auf der Bank.");
                            return;
                        }
                        playerManager.removeBankMoney(player, amount, "Überweisung an " + target.getName());
                        playerManager.addBankMoney(target, amount, "Überweisung von " + player.getName());
                        player.sendMessage("§8[§aATM§8]§a Du hast " + amount + "$ an " + target.getName() + " überwiesen.");
                        target.sendMessage("§8[§6Bank§8]§a " + player.getName() + " hat dir " + amount + "$ überwiesen.");
                        Main.getInstance().adminManager.send_message("§6" + player.getName() + " hat " + target.getName() + " " + amount + "$ überwiesen.", ChatColor.GOLD);
                    }
                });
            } else {
                inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§a§mTransaktion bestätigen", "§8 ➥ §cDu hast nicht genug Geld auf der Bank.")) {
                    @SneakyThrows
                    @Override
                    public void onClick(InventoryClickEvent event) {

                    }
                });
            }
        }

    }

    @EventHandler
    public void onSubmit(SubmitChatEvent event) throws SQLException {
        System.out.println("submit event trigger");
        Player player = event.getPlayer();
        if (event.getSubmitTo() == null) return;
        if (event.getSubmitTo().equals("atm_einzahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (playerManager.money(player) >= amount) {
                    playerManager.removeMoney(player, amount, "Bankeinzahlung(" + event.getPlayerData().getVariable("atm_name") + ")");
                    playerManager.addBankMoney(player, amount, "Bankeinzahlung(" + event.getPlayerData().getVariable("atm_name") + ")");
                    player.sendMessage("§8[§aATM§8]§a Du hast " + amount + "$ eingezahlt.");
                } else {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                }
            }
            event.end();
        }
        if (event.getSubmitTo().equals("atm_auszahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (playerManager.bank(player) >= amount) {
                    playerManager.removeBankMoney(player, amount, "Bankauszahlung (" + event.getPlayerData().getVariable("atm_name") + ")");
                    playerManager.addMoney(player, amount, "Bankauszahlung (" + event.getPlayerData().getVariable("atm_name") + ")");
                    player.sendMessage("§8[§aATM§8]§a Du hast " + amount + "$ ausgezahlt.");
                } else {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld auf der Bank.");
                }
            }
            event.end();
        }
        if (event.getSubmitTo().equals("atm_transfer_player")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            Player targetplayer = Bukkit.getPlayer(event.getMessage());
            if (!targetplayer.isOnline()) {
                player.sendMessage(Prefix.ERROR + "Der Spieler konnte nicht gefunden werden.");
                event.end();
            } else {
                event.end();
                openTransactionMenu(player, targetplayer);
            }
        }
        if (event.getSubmitTo().equals("atm_transfer_amount")) {
            System.out.println("transfer is raus");
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            if (event.getMessage().equals(event.getPlayerData().getVariable("transfer_player"))) return;
            Player targetplayer = Bukkit.getPlayer(event.getPlayerData().getVariable("transfer_player").toString());
            try {
                int amount = Integer.parseInt(event.getMessage());
                openTransactionMenu(player, targetplayer, amount);
            } catch (Exception ex) {
                player.sendMessage(Prefix.ERROR + "Die Zahl muss numerisch sein.");
            }
            event.end();
        }
        if (event.getSubmitTo().equals("atm_frak_einzahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (playerManager.money(player) >= amount) {
                    playerManager.removeMoney(player, amount, "Bankauszahlung");
                    factionManager.addFactionMoney(event.getPlayerData().getFaction(), amount, "Bankeinzahlung " + player.getName());
                    player.sendMessage("§8[§aATM§8]§a Du hast " + Utils.toDecimalFormat(amount) + "$ eingezahlt.");
                    factionManager.sendMessageToFaction(event.getPlayerData().getFaction(), player.getName() + " hat " + Utils.toDecimalFormat(amount) + "$ auf das Fraktionskonto eingezahlt.");
                } else {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                }
            }
            event.end();
        }
        if (event.getSubmitTo().equals("atm_frak_auszahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (factionManager.factionBank(event.getPlayerData().getFaction()) >= amount) {
                    factionManager.removeFactionMoney(event.getPlayerData().getFaction(), amount, "Bankauszahlung " + player.getName());
                    playerManager.addMoney(player, amount, "Bankauszahlung " + event.getPlayerData().getFaction());
                    player.sendMessage("§8[§aATM§8]§a Du hast " + Utils.toDecimalFormat(amount) + "$ ausgezahlt.");
                    factionManager.sendMessageToFaction(event.getPlayerData().getFaction(), player.getName() + " hat " + Utils.toDecimalFormat(amount) + "$ vom Fraktionskonto ausgezahlt.");
                } else {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld auf der Bank.");
                }
            }
            event.end();
        }

        if (event.getSubmitTo().equals("atm_company_einzahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (event.getPlayerData().getBargeld() >= amount) {
                    playerManager.removeMoney(player, amount, "Bankauszahlung");
                    event.getPlayerData().getCompany().addBank(amount);
                    player.sendMessage("§8[§aATM§8]§a Du hast " + Utils.toDecimalFormat(amount) + "$ eingezahlt.");
                    Main.getInstance().companyManager.sendCompanyMessage(event.getPlayerData().getCompany(), "§8[§6" + event.getPlayerData().getCompany().getName() + "§8]§e " + player.getName() + " hat " + Utils.toDecimalFormat(amount) + "$ auf das Firmenkonto eingezahlt.");
                } else {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld dabei.");
                }
            }
            event.end();
        }
        if (event.getSubmitTo().equals("atm_company_auszahlen")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            int amount = Integer.parseInt(event.getMessage());
            if (amount >= 1) {
                if (event.getPlayerData().getCompany().getBank() >= amount) {
                    event.getPlayerData().getCompany().removeBank(amount);
                    playerManager.addMoney(player, amount, "Bankauszahlung (Company-" + event.getPlayerData().getCompany().getId() + ")");
                    player.sendMessage("§8[§aATM§8]§a Du hast " + Utils.toDecimalFormat(amount) + "$ ausgezahlt.");
                    Main.getInstance().companyManager.sendCompanyMessage(event.getPlayerData().getCompany(), "§8[§6" + event.getPlayerData().getCompany().getName() + "§8]§e " + player.getName() + " hat " + Utils.toDecimalFormat(amount) + "$ vom Firmenkonto ausgezahlt.");
                } else {
                    player.sendMessage(Prefix.ERROR + "Du hast nicht genug Geld auf der Bank.");
                }
            }
            event.end();
        }
    }
}
