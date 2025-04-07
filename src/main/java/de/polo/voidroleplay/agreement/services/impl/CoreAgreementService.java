package de.polo.voidroleplay.agreement.services.impl;

import de.polo.voidroleplay.agreement.services.AgreementService;
import de.polo.voidroleplay.agreement.services.VertragUtil;
import de.polo.voidroleplay.player.entities.VoidPlayer;
import de.polo.voidroleplay.storage.Agreement;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import static de.polo.voidroleplay.Main.utils;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class CoreAgreementService implements AgreementService {

    @Override
    public boolean setVertrag(VoidPlayer player, VoidPlayer target, String type, Object vertrag) {
        return VertragUtil.setVertrag(player.getPlayer(), target.getPlayer(), type, vertrag);
    }

    @Override
    public void deleteVertrag(VoidPlayer player) {
        VertragUtil.deleteVertrag(player.getPlayer());
    }

    @Override
    public void setAgreement(VoidPlayer player, VoidPlayer target, Agreement agreement) {
        utils.vertragUtil.setAgreement(player.getPlayer(), target.getPlayer(), agreement);
    }

    @SneakyThrows
    @Override
    public void acceptVertrag(VoidPlayer player) {
        utils.vertragUtil.acceptVertrag(player.getPlayer());
    }

    @Override
    public void denyVertrag(VoidPlayer player) {
        utils.vertragUtil.denyVertrag(player.getPlayer());
    }

    @Override
    public void sendInfoMessage(VoidPlayer player) {
        utils.vertragUtil.sendInfoMessage(player.getPlayer());
    }
}
