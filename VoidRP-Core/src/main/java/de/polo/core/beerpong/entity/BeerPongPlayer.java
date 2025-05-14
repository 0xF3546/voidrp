package de.polo.core.beerpong.entity;

import de.polo.api.player.VoidPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BeerPongPlayer {
    @Getter
    private final VoidPlayer player;
    @Getter
    @Setter
    private BeerPongTeam team;
    public BeerPongPlayer(VoidPlayer player) {
        this.player = player;
        player.setVariable("beerpongInventory", player.getPlayer().getInventory().getContents());
        player.getPlayer().getInventory().clear();
    }

    public void equip() {
        player.getPlayer().getInventory().addItem(new ItemStack(Material.SNOWBALL, 1));
    }
    public void unequip() {
        player.getPlayer().getInventory().clear();
        if (player.getVariable("beerpongInventory") == null) return;
        ItemStack[] items = (ItemStack[]) player.getVariable("beerpongInventory");
        if (items != null) {
            player.getPlayer().getInventory().setContents(items);
        }
    }
}
