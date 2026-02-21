package de.polo.core.beerpong.handler;
import de.polo.api.utils.enums.Prefix;
import de.polo.api.VoidAPI;
import de.polo.api.player.VoidPlayer;
import de.polo.api.zone.Zone;
import de.polo.core.beerpong.services.BeerPongService;
import de.polo.core.beerpong.entity.BeerPongPlayer;
import de.polo.core.beerpong.entity.BeerPongTeam;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static de.polo.core.Main.blockManager;

public class BeerPongHandler {
    @Getter
    private final List<BeerPongPlayer> players = new ArrayList<>();
    @Getter
    private final Zone zone;
    @Getter
    private boolean started = false;

    @Getter
    @Setter
    private BeerPongPlayer currentTurn;

    public BeerPongHandler(Zone zone) {
        this.zone = zone;
    }

    public boolean addPlayer(BeerPongPlayer player) {
        if (players.size() >= 2) return false;
        if (players.contains(player)) return false;
        players.add(player);
        tryStartGame();
        return true;
    }

    public void tryStartGame() {
        if (!started && players.size() >= 2) {
            this.started = true;
            this.currentTurn = players.get(0);
            players.forEach(p -> {
                p.getPlayer().sendMessage("Das BeerPong Spiel beginnt!", Prefix.BEERPONG);
            });
            giveBallToCurrentPlayer();
        }
    }

    public void giveBallToCurrentPlayer() {
        players.forEach(p -> p.getPlayer().getPlayer().getInventory().clear());
        currentTurn.getPlayer().getPlayer().getInventory().addItem(new ItemStack(Material.SNOWBALL, 1));
        currentTurn.getPlayer().sendMessage("Du bist dran!", Prefix.BEERPONG);
    }

    public void switchTurn() {
        int index = players.indexOf(currentTurn);
        int nextIndex = (index + 1) % players.size();
        currentTurn = players.get(nextIndex);
        giveBallToCurrentPlayer();
    }


    public List<BeerPongPlayer> getRedTeam() {
        return players.stream().filter(p -> p.getTeam().isRed()).toList();
    }

    public List<BeerPongPlayer> getBlueTeam() {
        return players.stream().filter(p -> !p.getTeam().isRed()).toList();
    }

    public void endGame() {
        BeerPongService beerPongService = VoidAPI.getService(BeerPongService.class);
        players.forEach(BeerPongPlayer::unequip);
        players.forEach(p -> p.getPlayer().sendMessage("Das BeerPong Spiel ist vorbei.", Prefix.BEERPONG));
        players.clear();
        started = false;
        beerPongService.removeGame(this);

        blockManager.getBlocks()
                .stream()
                .filter(x -> x.getInfo().equalsIgnoreCase("beerpongcup"))
                .filter(x -> x.getInfoValue().contains(zone.getName()))
                .forEach(x -> {
                    x.getLocation().getBlock().setType(Material.FLOWER_POT);
                });
    }

    public void increaseScore(VoidPlayer player) {
        BeerPongPlayer pongPlayer = getBeerPongPlayer(player);
        if (pongPlayer == null) return;

        BeerPongTeam team = pongPlayer.getTeam();
        team.setScore(team.getScore() + 1);

        if (team.getScore() >= 9) {
            endGame();
        }
    }

    public BeerPongPlayer getBeerPongPlayer(VoidPlayer player) {
        return players.stream()
                .filter(p -> p.getPlayer().equals(player))
                .findFirst()
                .orElse(null);
    }

}
