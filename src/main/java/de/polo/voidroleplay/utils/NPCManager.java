package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.manager.PlayerManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class NPCManager implements CommandExecutor {
    private final PlayerManager playerManager;

    public NPCManager(PlayerManager playerManager) {
        this.playerManager = playerManager;
        Main.registerCommand("npc", this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());

        if (playerData.getPermlevel() >= 90) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("remove")) {
                    if (args.length >= 2) {
                        player.sendMessage(Main.gamedesign_prefix + "Trying to remove NPC...");
                        deleteNPC(player, args[1]);
                    } else {
                        player.sendMessage(Main.gamedesign_prefix + "Syntax error: /npc remove [Name]");
                    }
                } else if (args[0].equalsIgnoreCase("create")) {
                    if (args.length >= 3) {
                        String npcName = args[1];
                        String skinName = args[2];
                        String command = String.join(" ", Arrays.copyOfRange(args, 3, args.length));

                        player.sendMessage(Main.gamedesign_prefix + "Trying to create NPC...");
                        createNPC(player, npcName, skinName, command);
                    } else {
                        player.sendMessage(Main.gamedesign_prefix + "Syntax error: /npc create [Name] [Skin] [Command]");
                    }
                } else {
                    player.sendMessage(Main.gamedesign_prefix + "Invalid subcommand. Use /npc [create/remove].");
                }
            } else {
                player.sendMessage(Main.gamedesign_prefix + "Syntax error: /npc [create/remove] [Name] [Skin] [Command]");
            }
        } else {
            player.sendMessage(Main.error_nopermission);
        }

        return true;
    }

    public void createNPC(Player player, String name, String skinName, String command) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(org.bukkit.entity.EntityType.PLAYER, name);

        npc.spawn(player.getLocation());
        npc.setName(name);
        npc.data().setPersistent("command", command);

        if (skinName != null && !skinName.isEmpty()) {
            SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
            skinTrait.setSkinName(skinName); // Set the custom skin
        }

        player.sendMessage(Main.gamedesign_prefix + "NPC " + name + " created with skin " + skinName + ".");
    }

    public void deleteNPC(Player player, String name) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        boolean found = false;

        for (NPC npc : registry) {
            if (npc.getName().equalsIgnoreCase(name)) {
                npc.despawn();
                registry.deregister(npc);
                player.sendMessage(Main.gamedesign_prefix + "NPC " + name + " has been removed.");
                found = true;
                break;
            }
        }

        if (!found) {
            player.sendMessage(Main.gamedesign_prefix + "No NPC found with the name " + name + ".");
        }
    }
}
