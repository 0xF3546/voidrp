package de.polo.core.player.commands;

import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.player.JobSkill;
import de.polo.api.player.VoidPlayer;
import de.polo.core.handler.CommandBase;
import de.polo.core.player.entities.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(
        name = "jobskills",
        usage = "/jobskills")
public class JobSkillsCommand extends CommandBase {
    public JobSkillsCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8» §3Job Skills"));
        int i = 0;
        for (JobSkill jobSkill : player.getData().getJobSkills()) {
            inventoryManager.setItem(new CustomItem(i, new ItemBuilder(jobSkill.getJob().getIcon())
                    .setName("§8» §3" + jobSkill.getJob().getName())
                    .setLore(
                            Arrays.asList("§7Level: §8" + jobSkill.getLevel(),
                                    "§7Exp: §8" + jobSkill.getExp() + "§8/§8" + (jobSkill.getLevel() * 2250))
                    )
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {

                }
            });
            i++;
        }
    }
}
