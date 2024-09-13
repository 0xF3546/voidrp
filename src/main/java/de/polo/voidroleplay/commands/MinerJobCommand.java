package de.polo.voidroleplay.commands;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.dataStorage.RegisteredBlock;
import de.polo.voidroleplay.utils.*;
import de.polo.voidroleplay.utils.GamePlay.GamePlay;
import de.polo.voidroleplay.utils.InventoryManager.CustomItem;
import de.polo.voidroleplay.utils.InventoryManager.InventoryManager;
import de.polo.voidroleplay.utils.enums.EXPType;
import de.polo.voidroleplay.utils.enums.MinerBlockType;
import de.polo.voidroleplay.utils.enums.PickaxeType;
import de.polo.voidroleplay.utils.enums.RoleplayItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MinerJobCommand implements CommandExecutor {

    private final PlayerManager playerManager;
    private final GamePlay gamePlay;
    private final LocationManager locationManager;
    private final FactionManager factionManager;

    private final List<RegisteredBlock> registeredBlocks;

    public MinerJobCommand(PlayerManager playerManager, GamePlay gamePlay, LocationManager locationManager, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.gamePlay = gamePlay;
        this.locationManager = locationManager;
        this.factionManager = factionManager;


        registeredBlocks = Main.getInstance().blockManager.getBlocks().stream().filter(x -> x.getInfo() != null && x.getInfo().equalsIgnoreCase("mine")).collect(Collectors.toList());
        Main.registerCommand("miner", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        if (locationManager.getDistanceBetweenCoords(player, "Miner") < 5) {
            open(player);
        }
        return false;
    }

    private void open(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7Miner");
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.STONE_PICKAXE, 1, 0, "§7Spitzhacken")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openBuyMenu(player);
            }
        });
    }

    private void openBuyMenu(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7Miner");
        int i = 10;
        for (PickaxeType type : PickaxeType.values()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(type.getMaterial(), 1, 0, type.getDisplayName(), "§8 ➥ §cLevel " + type.getMinLevel())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (playerData.addonXP.getMinerLevel() < type.getMinLevel()) {
                        player.sendMessage(Prefix.ERROR + "Du musst mindestens Miner-Level " + type.getMinLevel() + " sein.");
                        return;
                    }
                    for (ItemStack stack : player.getInventory().getContents()) {
                        if (stack == null || stack.getItemMeta() == null) continue;
                        if (stack.getItemMeta().getDisplayName().equalsIgnoreCase(type.getDisplayName())) {
                            player.getInventory().remove(stack);
                        }
                    }
                    player.getInventory().addItem(ItemManager.createItem(type.getMaterial(), 1, 0, type.getDisplayName()));
                    player.closeInventory();
                }
            });
            i++;
        }
        inventoryManager.setItem(new CustomItem(26, ItemManager.createItem(Material.EMERALD, 1, 0, "§aVerkauf")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openVerkauf(player);
            }
        });
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                open(player);
            }
        });
    }

    private void openVerkauf(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, "§7Verkauf", true, true);
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.REDSTONE, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                open(player);
            }
        });
        inventoryManager.setItem(new CustomItem(10, ItemManager.createItem(Material.COAL, 1, 0, "§7Kohle")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!player.getInventory().containsAtLeast(new ItemStack(Material.COAL), 1)) {
                    player.sendMessage(Main.error + "Du hast nicht genug Kohle dabei!");
                    return;
                }

                int coalAmount = 0;

                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.COAL) {
                        coalAmount += item.getAmount();
                    }
                }

                playerData.addMoney(coalAmount, "Kohle verkauft");
                ItemManager.removeItem(player, Material.COAL, coalAmount);
            }
        });
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.IRON_INGOT, 1, 0, "§7Eisen")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!player.getInventory().containsAtLeast(new ItemStack(Material.IRON_INGOT), 1)) {
                    player.sendMessage(Main.error + "Du hast nicht genug Eisen dabei!");
                    return;
                }

                int ironAmount = 0;

                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.IRON_INGOT) {
                        ironAmount += item.getAmount();
                    }
                }

                playerData.addMoney(ironAmount, "Eisen verkauft");
                ItemManager.removeItem(player, Material.COAL, ironAmount);
            }
        });
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.GOLD_INGOT, 1, 0, "§7Gold")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!player.getInventory().containsAtLeast(new ItemStack(Material.GOLD_INGOT), 1)) {
                    player.sendMessage(Main.error + "Du hast nicht genug Gold dabei!");
                    return;
                }

                int goldAmount = 0;

                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.GOLD_INGOT) {
                        goldAmount += item.getAmount();
                    }
                }

                playerData.addMoney(goldAmount, "Gold verkauft");
                ItemManager.removeItem(player, Material.COAL, goldAmount);
            }
        });
        inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(Material.LAPIS_LAZULI,1 ,0, "§7Lapis")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!player.getInventory().containsAtLeast(new ItemStack(Material.LAPIS_LAZULI), 1)) {
                    player.sendMessage(Main.error + "Du hast nicht genug Lapis dabei!");
                    return;
                }

                int lapisAmount = 0;

                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.LAPIS_LAZULI) {
                        lapisAmount += item.getAmount();
                    }
                }

                playerData.addMoney(lapisAmount, "lapisAmount verkauft");
                ItemManager.removeItem(player, Material.COAL, lapisAmount);
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.REDSTONE, 1, 0, "§7Redstone")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!player.getInventory().containsAtLeast(new ItemStack(Material.REDSTONE), 1)) {
                    player.sendMessage(Main.error + "Du hast nicht genug Redstone dabei!");
                    return;
                }

                int redstoneAmount = 0;

                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.REDSTONE) {
                        redstoneAmount += item.getAmount();
                    }
                }

                playerData.addMoney(redstoneAmount, "Redstone verkauft");
                ItemManager.removeItem(player, Material.COAL, redstoneAmount);
            }
        });
        inventoryManager.setItem(new CustomItem(14, ItemManager.createItem(Material.DIAMOND, 1, 0, "§7Diamant")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!player.getInventory().containsAtLeast(new ItemStack(Material.DIAMOND), 1)) {
                    player.sendMessage(Main.error + "Du hast nicht genug Kohle dabei!");
                    return;
                }

                int diamondAmount = 0;

                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.DIAMOND) {
                        diamondAmount += item.getAmount();
                    }
                }

                playerData.addMoney(diamondAmount, "Diamanten verkauft");
                ItemManager.removeItem(player, Material.COAL, diamondAmount);
            }
        });
        inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(Material.EMERALD, 1, 0, "§7Emerald")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!player.getInventory().containsAtLeast(new ItemStack(Material.COAL), 1)) {
                    player.sendMessage(Main.error + "Du hast nicht genug Kohle dabei!");
                    return;
                }

                int emeraldAmount = 0;

                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == Material.EMERALD) {
                        emeraldAmount += item.getAmount();
                    }
                }

                playerData.addMoney(emeraldAmount, "Emerald verkauft");
                ItemManager.removeItem(player, Material.COAL, emeraldAmount);
            }
        });
    }

    public void blockBroke(Player player, Block brokenBlock) {
        RegisteredBlock block = getBlock(brokenBlock);
        System.out.println(block);
        if (block == null) return;
        PickaxeType type = getType(player.getInventory().getItemInMainHand());
        System.out.println(type);
        if (type == null) return;
        MinerBlockType minerBlockType = MinerBlockType.valueOf(block.getInfoValue());
        System.out.println(minerBlockType);
        if (minerBlockType.getOrder() > type.getOrder()) {
            player.sendMessage(Prefix.ERROR + "Deine Spitzhacke reicht dafür leider nicht aus. Du benötigst: " + type.getDisplayName());
            return;
        }

        player.getInventory().addItem(ItemManager.createItem(minerBlockType.getOutputItem().getMaterial(), 1, 0, minerBlockType.getOutputItem().getDisplayName()));
        brokenBlock.setType(Material.STONE);
        playerManager.addExp(player, EXPType.SKILL_MINER, Main.random(1, 10));
        rolloutBlocks(brokenBlock.getType());
    }

    private PickaxeType getType(ItemStack itemStack) {
        for (PickaxeType type : PickaxeType.values()) {
            if (type.getMaterial().equals(itemStack.getType())) return type;
        }
        return null;
    }

    private RegisteredBlock getBlock(Block block) {
        Location location = block.getLocation();
        for (RegisteredBlock b : registeredBlocks) {
            if (b == null) continue;
            if (b.getLocation().getBlockX() == location.getBlockX() && b.getLocation().getBlockY() == location.getBlockY() && b.getLocation().getBlockZ() == location.getBlockZ()) {
                return b;
            }
        }
        return null;
    }

    private void rolloutBlocks(Material blockType) {
        List<RegisteredBlock> blocks = registeredBlocks.stream().filter(x -> MinerBlockType.valueOf(x.getInfoValue()).getBlock().equals(blockType)).collect(Collectors.toList());
        if (blocks.stream().filter(x -> x.getMaterial() == blockType).count() >= 20) return;
        List<Block> newBlocks = new ArrayList<>();
        for (RegisteredBlock registeredBlock : blocks.stream().filter(x -> x.getBlock().getType().equals(Material.AIR)).collect(Collectors.toList())) {
            newBlocks.add(registeredBlock.getBlock());
        }
        Block block = newBlocks.get(Main.random(0, newBlocks.size() - 1));

        block.setType(blockType);
    }
}