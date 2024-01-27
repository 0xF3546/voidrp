package de.polo.metropiacity.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum RoleplayItem {
    JOINT("§2Joint", Material.BAMBOO),
    MARIHUANA("§aMarihuana", Material.KELP),
    BOX_WITH_JOINTS("§7Kiste mit Joints", Material.CHEST),
    COCAINE("§fKokain", Material.SUGAR),
    NOBLE_JOINT("§2Veredelter Joint", Material.BAMBOO),
    SCHWEIßGERÄT("§cSchweißgerät", Material.BLAZE_ROD),
    CUFF("§6Handschellen", Material.LEAD),
    SWAT_SHIELD("§6Einsatzschild", Material.SHIELD),
    ANTIBIOTIKUM("§cAntibiotikum", Material.RED_DYE);

    private final String displayName;
    private final Material material;

    private void Add() {

    }
}
