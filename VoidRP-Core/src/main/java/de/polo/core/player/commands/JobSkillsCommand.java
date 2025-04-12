package de.polo.core.player.commands;

import de.polo.api.Utils.ApiUtils;
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
@CommandBase.CommandMeta(name = "jobskills", usage = "/jobskills")
public class JobSkillsCommand extends CommandBase {

    public JobSkillsCommand(@NotNull CommandMeta meta) {
        super(meta);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        // Stilvoller Inventar-Titel
        InventoryManager inventoryManager = new InventoryManager(
                player.getPlayer(),
                27,
                Component.text("§7[§3Job Skills§7]")
        );

        int slot = 0;
        for (JobSkill jobSkill : player.getData().getJobSkills()) {
            inventoryManager.setItem(createJobSkillItem(slot, jobSkill));
            slot++;
        }
    }

    private CustomItem createJobSkillItem(int slot, JobSkill jobSkill) {
        int maxExp = jobSkill.getLevel() * 2250;

        return new CustomItem(slot, new ItemBuilder(jobSkill.getJob().getIcon())
                .setName("§3" + jobSkill.getJob().getName())
                .setLore(Arrays.asList(
                        "§7Level: §f" + jobSkill.getLevel(),
                        "§7Exp: §f" + jobSkill.getExp() + " §7/ §f" + maxExp,
                        "§8[§7" + ApiUtils.getProgressBar(jobSkill.getExp(), maxExp, 10) + "§8]"
                ))
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
            }
        };
    }
}