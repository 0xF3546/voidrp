package de.polo.metropiacity.Utils;

import de.polo.metropiacity.DataStorage.PlayerData;
import de.polo.metropiacity.Main;
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
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        PlayerData playerData = PlayerManager.playerDataMap.get(player.getUniqueId().toString());
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
                        spawnNPC(player, args[1], args[2], command);
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
    public static void spawnNPC(Player player, String name, String displayname, String command) {
        System.out.println("name: " + name);
        System.out.println("displayname: " + displayname);
        System.out.println("command: " + command);
        Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
        NamespacedKey cmd = new NamespacedKey(Main.plugin, "command");
        villager.getPersistentDataContainer().set(cmd, PersistentDataType.STRING, command);
        NamespacedKey id_name = new NamespacedKey(Main.plugin, "name");
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
        player.sendMessage(Main.gamedesign_prefix + "Du hast einen Villager erstellt.");
    }

    public static void deleteNPC(Player player, String name) {
        System.out.println("delete npc: " + name);
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Villager) {
                Villager villager = (Villager) entity;
                System.out.println("villager: " + villager.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "name"), PersistentDataType.STRING));
                if (villager.getPersistentDataContainer().get(new NamespacedKey(Main.plugin, "name"), PersistentDataType.STRING).equalsIgnoreCase(name)) {
                    villager.remove();
                    player.sendMessage(Main.gamedesign_prefix + "Der Villager wurde entfernt.");
                }
            }
        }
    }
}
