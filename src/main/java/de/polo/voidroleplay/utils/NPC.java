package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.dataStorage.PlayerData;
import de.polo.voidroleplay.Main;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;

public class NPC implements CommandExecutor {
    private final PlayerManager playerManager;
    public NPC(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("npc", this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
        if (playerData.getPermlevel() >= 90) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("remove")) {
                    player.sendMessage(Main.gamedesign_prefix + "Versuche NPC zu löschen...");
                    deleteNPC(player, args[1]);
                }
                if (args[0].equalsIgnoreCase("create")) {
                    if (args.length >= 3) {
                        player.sendMessage(Main.gamedesign_prefix + "Versuche NPC zu erstellen...");
                        String command = args[3];
                        for (int i = 4; i < args.length; i++) {
                            command = command + " " + args[i];
                        }
                        spawnNPC(player.getLocation(), args[1], args[2], command);
                    }
                }
            } else {
                player.sendMessage(Main.gamedesign_prefix + "Syntax-Fehler: /npc [create/remove] [Name] [Display-Name] [Command]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }
        return false;
    }
    public void spawnNPC(Location location, String name, String displayname, String command) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        NamespacedKey cmd = new NamespacedKey(Main.getInstance(), "command");
        villager.getPersistentDataContainer().set(cmd, PersistentDataType.STRING, command);
        NamespacedKey id_name = new NamespacedKey(Main.getInstance(), "name");
        villager.getPersistentDataContainer().set(id_name, PersistentDataType.STRING, name);
        villager.setAI(false);
        villager.setCustomName(displayname.replace("&", "§"));
        villager.setCustomNameVisible(true);
        villager.setCanPickupItems(false);
        villager.setCollidable(false);
        villager.setInvulnerable(true);
        villager.setProfession(Villager.Profession.NONE);
        villager.setSilent(true);
        villager.setAdult();
        villager.setBreed(false);
        villager.setAgeLock(true);
        villager.setRemoveWhenFarAway(false);
        villager.setTicksLived(Integer.MAX_VALUE);
    }

    public void deleteNPC(Player player, String name) {
        System.out.println("delete npc: " + name);
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Villager) {
                Villager villager = (Villager) entity;
                String villager_name = villager.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "name"), PersistentDataType.STRING);
                if (villager_name == null) continue;
                System.out.println("villager: " + villager.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "name"), PersistentDataType.STRING));
                if (villager.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "name"), PersistentDataType.STRING).equalsIgnoreCase(name)) {
                    villager.remove();
                    player.sendMessage(Main.gamedesign_prefix + "Der Villager wurde entfernt.");
                }
            }
        }
    }

    public void deleteNPC(Location location, String name) {
        System.out.println("delete npc: " + name);
        for (Entity entity : location.getWorld().getEntities()) {
            if (entity instanceof Villager) {
                Villager villager = (Villager) entity;
                String villager_name = villager.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "name"), PersistentDataType.STRING);
                if (villager_name == null) continue;
                System.out.println("villager: " + villager.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "name"), PersistentDataType.STRING));
                if (villager.getPersistentDataContainer().get(new NamespacedKey(Main.getInstance(), "name"), PersistentDataType.STRING).equalsIgnoreCase(name)) {
                    villager.remove();
                }
            }
        }
    }
}
