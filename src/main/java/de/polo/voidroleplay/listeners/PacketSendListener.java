package de.polo.voidroleplay.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.storage.BlacklistData;
import de.polo.voidroleplay.storage.FactionData;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.WantedReason;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.MessageFormat;

import static de.polo.voidroleplay.Main.utils;

public class PacketSendListener implements PacketListener {

    private final PlayerManager playerManager;
    private final FactionManager factionManager;

    public PacketSendListener(PlayerManager playerManager, FactionManager factionManager){
        this.playerManager = playerManager;
        this.factionManager = factionManager;
    }

    @Override
    public void onPacketSend(PacketSendEvent event){
        if(event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE){
            Player target = event.getPlayer();
            WrapperPlayServerPlayerInfoUpdate playerInfoUpdate = new WrapperPlayServerPlayerInfoUpdate(event);
            for(var entry : playerInfoUpdate.getEntries()){
                Player sender = Bukkit.getPlayer(entry.getProfileId());
                if(target != sender) {
                    PlayerData targetData = playerManager.getPlayerData(target.getUniqueId());
                    FactionData factionData = factionManager.getFactionData(targetData.getFaction());
                    PlayerData senderData = playerManager.getPlayerData(sender.getUniqueId());
                    //WPS für Goodfraks
                    if(factionManager.isPlayerInGoodFaction(target)){
                        if(!targetData.getFaction().equalsIgnoreCase("Medic")){
                            if(senderData.getWanted() != null){
                                WantedReason wantedReason = utils.staatUtil.getWantedReason(senderData.getWanted().getWantedId());
                                if(wantedReason.getWanted() >= 55){
                                    entry.setDisplayName(Component.text(MessageFormat.format("{0} {1} WPS", sender.getName(), wantedReason.getWanted())).color(NamedTextColor.DARK_RED));
                                }
                                else{
                                    entry.setDisplayName(Component.text(MessageFormat.format("{0} {1} WPS", sender.getName(), wantedReason.getWanted())).color(NamedTextColor.RED   ));
                                }
                            }
                        }
                    }
                    //Blacklists für Badfraks
                    else{
                        for(BlacklistData blacklistData : factionManager.getBlacklists()){
                            //maybe always false IDE zeigt irwas an sieht aber wie fehler aus
                            if(blacklistData.getUuid().equals(senderData.getUuid()) && blacklistData.getFaction().equals(targetData.getFaction())){
                                entry.setDisplayName(Component.text(sender.getName()).color(NamedTextColor.DARK_RED));
                            }
                        }
                    }
                    //Wenn Spieler in gleicher Frak dann Namen in Primary Color Anzeigen
                    if(targetData.getFaction().equals(senderData.getFaction())) {
                        entry.setDisplayName(Component.text("§" + factionData.getPrimaryColor() +sender.getName()));
                    }
                    //Verlobt / Verheiratet?
                    if(!targetData.getRelationShip().isEmpty() && !senderData.getRelationShip().isEmpty()){
                        if(targetData.getRelationShip().get(sender.getUniqueId().toString()) != null){
                            entry.setDisplayName(Component.text(sender.getName()).color(NamedTextColor.LIGHT_PURPLE));
                        }
                    }
                }
            }
        }
    }

}
