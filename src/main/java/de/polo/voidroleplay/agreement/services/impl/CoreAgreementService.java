package de.polo.voidroleplay.agreement.services.impl;

import de.polo.voidroleplay.agreement.services.AgreementService;
import de.polo.voidroleplay.agreement.services.VertragUtil;
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
    public boolean setVertrag(Player player, Player target, String type, Object vertrag) {
        return VertragUtil.setVertrag(player, target, type, vertrag);
    }

    @Override
    public void deleteVertrag(Player player) {
        VertragUtil.deleteVertrag(player);
    }

    @Override
    public void setAgreement(Player player, Player target, Agreement agreement) {
        utils.vertragUtil.setAgreement(player, target, agreement);
    }

    @SneakyThrows
    @Override
    public void acceptVertrag(Player player) {
        utils.vertragUtil.acceptVertrag(player);
    }

    @Override
    public void denyVertrag(Player player) {
        utils.vertragUtil.denyVertrag(player);
    }

    @Override
    public void sendInfoMessage(Player player) {
        utils.vertragUtil.sendInfoMessage(player);
    }
}
