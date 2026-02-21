package de.polo.core.player.commands;

import de.polo.api.utils.ApiUtils;
import de.polo.api.utils.ItemBuilder;
import de.polo.api.utils.enums.Prefix;
import de.polo.api.utils.inventorymanager.CustomItem;
import de.polo.api.utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

@CommandBase.CommandMeta(
        name = "navicolor",
        permissionLevel = 20
)
public class NaviColorCommand extends CommandBase {
    public NaviColorCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("Navi Farbe"));
        int i = 0;
        for (Color color : ApiUtils.getColors()) {
            inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.PAPER)
                    .setName(ApiUtils.getColorString(color))
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    PlayerService playerService = VoidAPI.getService(PlayerService.class);
                    playerService.setNaviColor(player, color);
                    player.sendMessage("Du hast deine Navi Farbe auf " + ApiUtils.getColorString(color) + " gesetzt.", Prefix.MAIN);
                }
            });
            i++;
        }
    }
}
