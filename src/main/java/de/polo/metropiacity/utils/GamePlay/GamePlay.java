package de.polo.metropiacity.utils.GamePlay;

import de.polo.metropiacity.dataStorage.FactionData;
import de.polo.metropiacity.dataStorage.PlayerData;
import de.polo.metropiacity.dataStorage.PlayerLaboratory;
import de.polo.metropiacity.database.MySQL;
import de.polo.metropiacity.utils.FactionManager;
import de.polo.metropiacity.utils.InventoryManager.CustomItem;
import de.polo.metropiacity.utils.InventoryManager.InventoryManager;
import de.polo.metropiacity.utils.ItemManager;
import de.polo.metropiacity.utils.PlayerManager;
import de.polo.metropiacity.utils.Utils;
import de.polo.metropiacity.utils.enums.RoleplayItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GamePlay {
    public final ApothekeFunctions apotheke;
    private final PlayerManager playerManager;
    private final Utils utils;
    private final MySQL mySQL;
    private final FactionManager factionManager;
    public final Drugstorage drugstorage;
    public GamePlay(PlayerManager playerManager, Utils utils, MySQL mySQL, FactionManager factionManager) {
        this.playerManager = playerManager;
        this.utils = utils;
        this.mySQL = mySQL;
        this.factionManager = factionManager;
        drugstorage = new Drugstorage(playerManager, factionManager);
        apotheke = new ApothekeFunctions(mySQL, utils, factionManager, playerManager);
    }
    public class Drugstorage {
        private final PlayerManager playerManager;
        private final FactionManager factionManager;
        public Drugstorage(PlayerManager playerManager, FactionManager factionManager) {
            this.playerManager = playerManager;
            this.factionManager = factionManager;
        };
        public void open(Player player) {
            PlayerData playerData = playerManager.getPlayerData(player);
            FactionData factionData = factionManager.getFactionData(playerData.getFaction());
            InventoryManager inventoryManager = new InventoryManager(player,27, "§8 » §2Drogenlager (" + factionData.getName() + ")", true, true);
            inventoryManager.setItem(new CustomItem(11, ItemManager.createItem(RoleplayItem.MARIHUANA.getMaterial(), 1, 0, RoleplayItem.MARIHUANA.getDisplayName(), "§8 ➥ §7" + factionData.storage.getWeed())) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
            inventoryManager.setItem(new CustomItem(13, ItemManager.createItem(RoleplayItem.JOINT.getMaterial(), 1, 0, RoleplayItem.JOINT.getDisplayName(), "§8 ➥ §7" + factionData.storage.getJoint())) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
            inventoryManager.setItem(new CustomItem(15, ItemManager.createItem(RoleplayItem.COCAINE.getMaterial(), 1, 0, RoleplayItem.COCAINE.getDisplayName(), "§8 ➥ §7" + factionData.storage.getCocaine())) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
        }
    }
}
