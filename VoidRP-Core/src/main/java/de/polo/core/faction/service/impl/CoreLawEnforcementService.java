package de.polo.core.faction.service.impl;

import de.polo.api.VoidAPI;
import de.polo.api.faction.CharacterRecord;
import de.polo.api.player.PlayerWanted;
import de.polo.api.player.VoidPlayer;
import de.polo.api.player.enums.WantedVariation;
import de.polo.core.Main;
import de.polo.core.faction.service.FactionService;
import de.polo.core.faction.service.LawEnforcementService;
import de.polo.core.location.services.LocationService;
import de.polo.core.manager.ServerManager;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.storage.JailInfo;
import de.polo.core.storage.WantedReason;
import de.polo.core.utils.Service;
import de.polo.core.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;

import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CoreLawEnforcementService implements LawEnforcementService {
    private final LawEnforcementRepository repository;

    public CoreLawEnforcementService() {
        this.repository = new LawEnforcementRepository();
    }

    @Override
    public CharacterRecord getCharacterRecord(UUID target) {
        return repository.getCharacterRecord(target);
    }

    @Override
    public void setCharacterRecord(UUID target, CharacterRecord record) {
        repository.setCharacterRecord(target, record);
    }

    @Override
    public void addWantedLog(UUID criminal, PlayerWanted playerWanted) {
        repository.addWantedLog(criminal, playerWanted);
    }

    @SneakyThrows
    @Override
    public boolean arrestPlayer(VoidPlayer player, VoidPlayer target, boolean isDeathArrest) {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (target.getData().getWanted() == null) return false;
        target.getPlayer().teleport(locationService.getLocation("gefaengnis"));
        PlayerWanted wanted = target.getData().getWanted();
        WantedReason wantedReason = repository.getWantedReason(wanted.getWantedId());
        if (!isDeathArrest) {
            locationService.useLocation(target.getPlayer(), "gefaengnis");
        }
        int wanteds = getActualArrestWanteds(wanted);
        if (isDeathArrest) {
            target.sendMessage("§8[§6Gefängnis§8] §7Du wurdest für " + wanteds / 3 + " Minuten inhaftiert.");
        } else {
            target.sendMessage("§8[§6Gefängnis§8] §7Du bist nun für " + wanteds / 3 + "  Minuten im Gefängnis..");
        }
        target.getData().setJailed(true);
        target.getData().setHafteinheiten(wanteds / 3);
        FactionService factionService = VoidAPI.getService(FactionService.class);
        factionService.addFactionMoney(player.getData().getFaction(), ServerManager.getPayout("arrest"), "Inhaftierung von " + target.getName() + ", durch " + player.getName());
        if (isDeathArrest) {
            factionService.sendCustomMessageToFactions("§9HQ: " + target.getName() + " wurde von " + player.getName() + " getötet.", "FBI", "Polizei");
        } else {
            factionService.sendCustomMessageToFactions("§9HQ: " + factionService.getTitle(player.getPlayer()) + " " + player.getName() + " hat " + target.getName() + " in das Gefängnis inhaftiert.");
        }
        factionService.sendCustomMessageToFactions("§9HQ: Fahndungsgrund: " + wantedReason.getReason() + " | Fahndungszeit: " + calculateManhuntTime(wanted));

        repository.removeWanteds(target.getUuid());
        repository.addPlayerArrest(player.getUuid(), target.getUuid(), wanted.getWantedId(), wanteds);
        addWantedLog(target.getUuid(), wanted);
        return false;
    }

    @Override
    public void unarrestPlayer(VoidPlayer player) {
        LocationService locationService = VoidAPI.getService(LocationService.class);
        if (locationService.getDistanceBetweenCoords(player.getPlayer(), "gefaengnis") < 200) {
            locationService.useLocation(player.getPlayer(), "gefaengnis_out");
        }
        player.getData().setJailed(false);
        player.getData().setHafteinheiten(0);
        player.sendMessage("§8[§cGefängnis§8] §7Du wurdest entlassen.");
        repository.removeJail(player.getUuid());
        // loadParole(player);
    }

    @Override
    public List<WantedReason> getWantedReasons() {
        return repository.getWantedReasons();
    }

    @Override
    public WantedReason getWantedReason(int id) {
        return repository.getWantedReason(id);
    }

    @Override
    public WantedReason getWantedReason(String reason) {
        return getWantedReasons()
                .stream()
                .filter(wantedReason -> wantedReason.getReason().equalsIgnoreCase(reason))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addWantedReason(WantedReason reason) {
        repository.getWantedReasons().add(reason);
    }

    private int getActualArrestWanteds(PlayerWanted wanted) {
        int wanteds = repository.getWantedReason(wanted.getWantedId()).getWanted();
        for (WantedVariation variation : wanted.getVariations()) {
            wanteds += variation.getWantedAmount();
        }
        return wanteds;
    }

    private String calculateManhuntTime(PlayerWanted wanted) {
        LocalDateTime now = Utils.getTime();
        Duration diff = Duration.between(wanted.getIssued(), now);

        if (diff.isNegative()) {
            diff = diff.abs();
        }

        long minutes = diff.toMinutes();
        if (minutes < 60) {
            return minutes + " Minute" + (minutes != 1 ? "n" : "");
        }

        long hours = diff.toHours();
        if (hours < 24) {
            return hours + " Stunde" + (hours != 1 ? "n" : "");
        }

        long days = diff.toDays();
        return days + " Tag" + (days != 1 ? "e" : "");
    }
}
