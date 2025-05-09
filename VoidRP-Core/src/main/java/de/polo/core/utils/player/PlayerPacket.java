package de.polo.core.utils.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerPacket {

    private final Player sender;

    public PlayerPacket(Player sender){
        this.sender = sender;
    }

    public void renewPacket(){
        for(Player viewer : Bukkit.getOnlinePlayers()){
            WrapperPlayServerPlayerInfoUpdate.PlayerInfo data = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(sender.getUniqueId());
            List datalist = new ArrayList<>();
            datalist.add(data);
            WrapperPlayServerPlayerInfoUpdate packet = new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME, datalist);
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
        }
    }

}
