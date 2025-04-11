package de.polo.core.jobs.commands;

import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.core.Main;
import de.polo.core.faction.service.impl.FactionManager;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ItemManager;
import de.polo.core.player.services.impl.PlayerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.RegisteredBlock;
import de.polo.core.utils.Prefix;
import de.polo.core.utils.Utils;
import de.polo.core.utils.enums.EXPType;
import de.polo.core.utils.enums.MinerBlockType;
import de.polo.core.utils.enums.MinerItem;
import de.polo.core.utils.enums.PickaxeType;
import de.polo.core.utils.gameplay.GamePlay;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class MinerJobCommand implements CommandExecutor {

    private final PlayerManager playerManager;
    private final GamePlay gamePlay;
    private final FactionManager factionManager;

    private final List<RegisteredBlock> registeredBlocks;

    public MinerJobCommand(PlayerManager playerManager, GamePlay gamePlay, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.gamePlay = gamePlay;
        this.factionManager = factionManager;


        registeredBlocks = Main.getInstance().blockManager.getBlocks().stream().filter(x -> x.getInfo() != null && x.getInfo().equalsIgnoreCase("mine")).collect(Collectors.toList());
        Main.registerCommand("miner", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player);
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (locationService.getDistanceBetweenCoords(player, "Miner") < 5) {
            open(player);
        }
        return false;
    }

    private void open(Player player) {
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§7Miner"));
        inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(Material.STONE_PICKAXE, 1, 0, "§7Spitzhacken")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openBuyMenu(player);
            }
        });
        inventoryManager.setItem(new CustomItem(12, ItemManager.createItem(Material.EMERALD, 1, 0, "§aVerkauf")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openVerkauf(player);
            }
        });
    }

    private void openBuyMenu(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§7Miner"));
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
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                open(player);
            }
        });
    }

    private void openVerkauf(Player player) {
        PlayerData playerData = playerManager.getPlayerData(player);
        InventoryManager inventoryManager = new InventoryManager(player, 27, Component.text("§7Verkauf"));
        inventoryManager.setItem(new CustomItem(18, ItemManager.createItem(Material.NETHER_WART, 1, 0, "§cZurück")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                open(player);
            }
        });
        int i = 10;
        for (MinerItem minerItem : MinerItem.values()) {
            inventoryManager.setItem(new CustomItem(i, ItemManager.createItem(minerItem.getMaterial(), 1, 0, minerItem.getDisplayName(), "§8 ➥ §a" + minerItem.getPrice() + "$")) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    player.closeInventory();
                    int count = ItemManager.getItem(player, minerItem.getMaterial());
                    playerData.addBankMoney(count * minerItem.getPrice(), "Verkauf " + minerItem.name());
                    player.sendMessage("§8[§7Miner§8]§7 Du hast §a" + Utils.toDecimalFormat(count * minerItem.getPrice()) + "$§7 erhalten.");
                    ItemManager.removeItem(player, minerItem.getMaterial(), count);
                    playerManager.addExp(player, count / 2);
                }
            });
            i++;
        }
    }

    public void blockBroke(Player player, Block brokenBlock) {
        RegisteredBlock block = getBlock(brokenBlock);
        if (block == null) return;
        PickaxeType type = getType(player.getInventory().getItemInMainHand());

        if (type == null) return;
        MinerBlockType minerBlockType = MinerBlockType.valueOf(block.getInfoValue());
        if (!brokenBlock.getType().equals(minerBlockType.getBlock())) return;

        if (minerBlockType.getOrder() > type.getOrder()) {
            player.sendMessage(Prefix.ERROR + "Deine Spitzhacke reicht dafür leider nicht aus. Du benötigst: " + type.getDisplayName());
            return;
        }

        player.getInventory().addItem(ItemManager.createItem(minerBlockType.getOutputItem().getMaterial(), 1, 0, minerBlockType.getOutputItem().getDisplayName()));
        Material blockType = brokenBlock.getType();
        brokenBlock.setType(Material.STONE);
        int amount = Utils.random(1, 10);
        int rand = Utils.random(1, 100);
        if (Utils.random(1, 100) == 1) {
            amount = amount * 4;
            player.sendMessage("§8[§eMiner-drop§8]§a Du hast 4x XP bekommen!");
        } else if (rand >= 5 && rand <= 10) {
            player.sendMessage("§8[§eMiner-drop§8]§a Du kannst 30 Sekunden schneller abbauen!");
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 30 * 20, 1));
        } else if (rand < 4) {
            int am = Utils.random(2, 4);
            player.sendMessage("§8[§eMiner-drop§8]§a Du hast " + am + " mehr " + minerBlockType.getDisplayName() + " §a erhalten.");
            player.getInventory().addItem(ItemManager.createItem(minerBlockType.getOutputItem().getMaterial(), am, 0, minerBlockType.getOutputItem().getDisplayName()));
        }
        playerManager.addExp(player, EXPType.SKILL_MINER, amount);
        rolloutBlocks(blockType);
    }

    private ItemStack getEquippedItem(Inventory inventory) {
        for (PickaxeType type : PickaxeType.values()) {
            for (ItemStack stack : inventory.getContents()) {
                if (stack == null || stack.getItemMeta() == null) continue;
                if (stack.getItemMeta().getDisplayName().equalsIgnoreCase(type.getDisplayName())) {
                    return stack;
                }
            }
        }
        return null;
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
        List<RegisteredBlock> blocks = new ObjectArrayList<>();
        for (RegisteredBlock block : registeredBlocks) {
            if (block.getInfoValue() == null) continue;
            if (!MinerBlockType.valueOf(block.getInfoValue()).getBlock().equals(blockType)) continue;
            blocks.add(block);
        }

        if (blocks.stream().filter(x -> x.getMaterial() == blockType).count() >= 20) return;

        List<Block> newBlocks = blocks.stream()
                .map(RegisteredBlock::getBlock)
                .filter(block -> block.getType() == Material.STONE)
                .collect(Collectors.toList());
        List<Block> nb = new ObjectArrayList<>();
        for (RegisteredBlock b : blocks) {
            if (!b.getBlock().getType().equals(Material.STONE)) continue;
            nb.add(b.getBlock());
        }

        if (newBlocks.isEmpty()) {
            System.out.println("Found no block");
            return;
        }

        Block block = newBlocks.get(Utils.random(0, newBlocks.size() - 1));
        block.setType(blockType);
    }

}