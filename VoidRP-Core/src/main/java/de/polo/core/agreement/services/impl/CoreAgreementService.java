package de.polo.core.agreement.services.impl;

import de.polo.api.player.VoidPlayer;
import de.polo.core.agreement.services.AgreementService;
import de.polo.core.agreement.services.VertragUtil;
import de.polo.core.storage.Agreement;
import de.polo.core.utils.Service;
import lombok.SneakyThrows;

import static de.polo.core.Main.utils;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
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
        System.out.println("setAgreement " + player.getPlayer().getName() + " " + target.getPlayer().getName());
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
