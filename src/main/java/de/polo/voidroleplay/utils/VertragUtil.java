package de.polo.voidroleplay.utils;

import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.storage.Agreement;
import de.polo.voidroleplay.storage.Company;
import de.polo.voidroleplay.storage.PlayerData;
import de.polo.voidroleplay.storage.SubGroup;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.game.base.housing.HouseManager;
import de.polo.voidroleplay.manager.AdminManager;
import de.polo.voidroleplay.manager.FactionManager;
import de.polo.voidroleplay.manager.PlayerManager;
import de.polo.voidroleplay.utils.player.ChatUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class VertragUtil {
    public static final HashMap<String, String> vertrag_type = new HashMap<>();
    public static final HashMap<String, Object> current = new HashMap<>();

    private final PlayerManager playerManager;
    private final FactionManager factionManager;
    private final AdminManager adminManager;

    private final List<Agreement> agreements = new ObjectArrayList<>();

    public VertragUtil(PlayerManager playerManager, FactionManager factionManager, AdminManager adminManager) {
        this.playerManager = playerManager;
        this.factionManager = factionManager;
        this.adminManager = adminManager;
    }

    public static boolean setVertrag(Player player, Player target, String type, Object vertrag) {
        if (current.get(target.getUniqueId().toString()) == null) {
            current.remove(target.getUniqueId().toString(), vertrag);
            vertrag_type.put(target.getUniqueId().toString(), type);
        }
        vertrag_type.put(target.getUniqueId().toString(), type);
        current.put(target.getUniqueId().toString(), vertrag);
        Statement statement = null;
        Main.getInstance().getMySQL().insertAsync("INSERT INTO verträge (first_person, second_person, type, vertrag, date) VAUES (?, ?, ?, ?, ?)", player.getUniqueId().toString(), target.getUniqueId().toString(), type, vertrag, new Date().toString());
        return true;
    }

    public static void deleteVertrag(Player player) {
        if (current.get(player.getUniqueId().toString()) != null) {
            current.remove(player.getUniqueId().toString());
            vertrag_type.remove(player.getUniqueId().toString());
        }
    }

    private Agreement getActiveAgreement(Player player) {
        return agreements.stream().filter(x -> x.getContractor() == player || x.getContracted() == player).findFirst().orElse(null);
    }

    public void setAgreement(Player player, Player target, Agreement agreement) {
        agreements.remove(getActiveAgreement(player));
        agreements.remove(getActiveAgreement(target));
        agreements.add(agreement);
        Main.getInstance().getMySQL().insertAsync("INSERT INTO player_agreements (contractor, contracted, type, agreement) VALUES (?, ?, ?, ?)",
                player.getUniqueId(),
                target.getUniqueId(),
                agreement.getType(),
                "");
    }

    public void acceptVertrag(Player player) throws SQLException {
        Object curr = current.get(player.getUniqueId().toString());
        Agreement agreement = getActiveAgreement(player);
        if (agreement != null) {
            agreement.accept();
            agreements.remove(agreement);
            return;
        }
        if (curr != null) {
            Player targetplayer = null;
            try {
                targetplayer = Bukkit.getPlayer(UUID.fromString(curr.toString()));
            } catch (IllegalArgumentException e) {
            }
            PlayerData playerData = playerManager.getPlayerData(player.getUniqueId());
            switch (vertrag_type.get(player.getUniqueId().toString())) {
                case "faction_invite":
                    factionManager.setPlayerInFrak(player, curr.toString(), 0, true);
                    factionManager.sendMessageToFaction(curr.toString(), player.getName() + " ist der Fraktion beigetreten");
                    adminManager.send_message(player.getName() + " ist der Fraktion " + curr + " beigetreten.", ChatColor.DARK_PURPLE);
                    Main.getInstance().beginnerpass.didQuest(player, 1);
                    break;
                case "business_invite":
                    playerData.setBusiness(Integer.parseInt(curr.toString()));
                    player.sendMessage("§8[§6Business§8]§a Du bist einem Business beigetreten.");
                    playerData.save();
                    break;
                case "company_invite":
                    Company company = Main.getInstance().companyManager.getCompanyById(Integer.parseInt(curr.toString()));
                    playerData.setCompany(company);
                    playerData.save();
                    player.sendMessage("§6Du bist " + company.getName() + " beigetreten.");
                    break;
                case "subgroup_invite":
                    SubGroup group = Main.getInstance().factionManager.subGroups.getSubGroup(Integer.parseInt(curr.toString()));
                    playerData.setSubGroupId(group.getId());
                    playerData.save();
                    player.sendMessage("§6Du bist " + group.getName() + " beigetreten.");
                    break;
                case "rental":
                    String[] args = curr.toString().split("_");
                    int haus = Integer.parseInt(args[0]);
                    int preis = Integer.parseInt(args[1]);
                    House houseData = HouseManager.houseDataMap.get(haus);
                    houseData.addRenter(player.getUniqueId().toString(), preis);
                    Main.getInstance().utils.houseManager.updateRenter(haus);
                    Player player1 = Bukkit.getPlayer(UUID.fromString(houseData.getOwner()));
                    player1.sendMessage("§8[§6Haus§8]§a " + player.getName() + " Mietet nun in Haus " + houseData.getNumber() + " für " + preis + "$.");
                    player.sendMessage("§8[§6Haus§8]§a Du mietest nun in Haus " + houseData.getNumber() + " für " + preis + "$/PayDay.");
                    Main.getInstance().beginnerpass.didQuest(player, 10);
                    break;
                case "phonecall":
                    PhoneUtils.acceptCall(player, curr.toString());
                    break;
                case "beziehung":
                    if (targetplayer.isOnline()) {
                        player.sendMessage("§aDu und " + targetplayer.getName() + " sind jetzt zusammen.");
                        targetplayer.sendMessage("§aDu und " + player.getName() + " sind jetzt zusammen.");
                        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                        HashMap<String, String> hmap1 = new HashMap<>();
                        hmap1.put(player.getUniqueId().toString(), "beziehung");
                        targetplayerData.setRelationShip(hmap1);

                        HashMap<String, String> hmap2 = new HashMap<>();
                        hmap2.put(targetplayer.getUniqueId().toString(), "beziehung");
                        playerData.setRelationShip(hmap2);
                        JSONObject object = new JSONObject(playerData.getRelationShip());
                        Main.getInstance().getMySQL().updateAsync("UPDATE players SET relationShip = ? WHERE uuid = ?", object.toString(), player.getUniqueId().toString());

                        JSONObject object2 = new JSONObject(targetplayerData.getRelationShip());
                        Main.getInstance().getMySQL().updateAsync("UPDATE players SET relationShip = ? WHERE uuid = ?", object2.toString(), targetplayer.getUniqueId().toString());
                    } else {
                        player.sendMessage(Prefix.ERROR + "Spieler konnte nicht gefunden werden.");
                    }
                    break;
                case "verlobt":
                    if (targetplayer.isOnline()) {
                        player.sendMessage("§aDu und " + targetplayer.getName() + " sind jetzt verlobt.");
                        targetplayer.sendMessage("§aDu und " + player.getName() + " sind jetzt verlobt.");
                        PlayerData targetplayerData = playerManager.getPlayerData(targetplayer.getUniqueId());
                        HashMap<String, String> hmap1 = new HashMap<>();
                        hmap1.put(player.getUniqueId().toString(), "verlobt");
                        targetplayerData.getRelationShip().clear();
                        targetplayerData.setRelationShip(hmap1);

                        HashMap<String, String> hmap2 = new HashMap<>();
                        hmap2.put(targetplayer.getUniqueId().toString(), "verlobt");
                        playerData.getRelationShip().clear();
                        playerData.setRelationShip(hmap2);
                        JSONObject object = new JSONObject(playerData.getRelationShip());
                        Main.getInstance().getMySQL().updateAsync("UPDATE players SET relationShip = ? WHERE uuid = ?", object.toString(), player.getUniqueId().toString());

                        JSONObject object2 = new JSONObject(targetplayerData.getRelationShip());
                        Main.getInstance().getMySQL().updateAsync("UPDATE players SET relationShip = ? WHERE uuid = ?", object2.toString(), targetplayer.getUniqueId().toString());
                    } else {
                        player.sendMessage(Prefix.ERROR + "Spieler konnte nicht gefunden werden.");
                    }
                    break;
                case "blutgruppe":
                    if (targetplayer.isOnline()) {
                        ChatUtils.sendGrayMessageAtPlayer(targetplayer, targetplayer.getName() + " testet eine Blutgruppe im Labor.");
                        targetplayer.sendMessage("§8[§cLabor§8]§e Prüfe Ergebnisse...");
                        Player finalTargetplayer = targetplayer;
                        Main.waitSeconds(7, () -> {
                            if (!finalTargetplayer.isOnline() || !player.isOnline()) {
                                return;
                            }
                            String[] blutgruppen = {"A-", "A+", "B-", "B+", "AB-", "AB+", "0+", "0-"};
                            String random = blutgruppen[ThreadLocalRandom.current().nextInt(blutgruppen.length + 1)];
                            finalTargetplayer.sendMessage("§8[§cLabor§8]§e Die Blutgruppe ist " + random + "!");
                            player.sendMessage("§eDeine Blutgruppe ist " + random + "!");
                            playerData.setBloodType(random);
                            Main.getInstance().beginnerpass.didQuest(player, 21);
                            Main.getInstance().getMySQL().updateAsync("UPDATE players SET bloodtype = ? WHERE uuid = ?", random, player.getUniqueId().toString());
                            try {
                                playerManager.removeMoney(player, 200, "Untersuchung (Blutgruppe)");
                                factionManager.addFactionMoney("Medic", 200, "Untersuchung durch " + finalTargetplayer.getName() + " (Blutgruppe)");
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        player.sendMessage(Prefix.ERROR + "Spieler konnte nicht gefunden werden.");
                    }
                    break;
                case "streetwar":
                    Main.getInstance().streetwar.acceptStreetwar(player, curr.toString());
                    break;
                case "alliance":
                    Main.getInstance().gamePlay.alliance.accept(player, curr.toString());
                    break;
                case "vertrag":
                    player.sendMessage(Prefix.MAIN + "Du hast den Vertrag angenommen!");
                    targetplayer.sendMessage(Prefix.MAIN + player.getName() + " hat den Vertrag angeommen!");
                    break;
            }
            deleteVertrag(player);
        } else {
            player.sendMessage(Prefix.ERROR + "Dir wird nichts angeboten.");
        }
    }

    public void denyVertrag(Player player) {
        String curr = current.get(player.getUniqueId().toString()).toString();
        Agreement agreement = getActiveAgreement(player);
        if (agreement != null) {
            agreement.deny();
            agreements.remove(agreement);
            return;
        }
        if (curr != null) {
            Player targetplayer = null;
            PlayerData playerData = playerManager.getPlayerData(player);
            try {
                targetplayer = Bukkit.getPlayer(UUID.fromString(curr));
            } catch (IllegalArgumentException e) {
            }
            switch (vertrag_type.get(player.getUniqueId().toString())) {
                case "faction_invite":
                    factionManager.sendMessageToFaction(curr, player.getName() + " wurde eingeladen und ist nicht beigetreten.");
                    break;
                case "business_invite":
                    player.sendMessage("§6Du hast die einladung zu abgelehnt.");
                    break;
                case "company_invite":
                    Company company = Main.getInstance().companyManager.getCompanyById(Integer.parseInt(curr));
                    player.sendMessage("§6Du hast die einladung zu " + company.getName() + " abgelehnt.");
                    break;
                case "subgroup_invite":
                    SubGroup subGroup = Main.getInstance().factionManager.subGroups.getSubGroup(Integer.parseInt(curr));
                    player.sendMessage("§6Du hast die einladung zu " + subGroup.getName() + " abgelehnt.");
                    break;
                case "phonecall":
                    PhoneUtils.denyCall(player, curr);
                    break;
                case "rental":
                    String[] args = curr.split("_");
                    Integer haus = Integer.valueOf(args[0]);
                    Integer preis = Integer.valueOf(args[1]);
                    House houseData = HouseManager.houseDataMap.get(haus);
                    Player player1 = Bukkit.getPlayer(UUID.fromString(houseData.getOwner()));
                    player1.sendMessage("§8[§6Haus§8]§a " + player.getName() + " hat den Mietvertrag für Haus " + houseData.getNumber() + " abgelehnt.");
                    player.sendMessage("§8[§6Haus§8]§c Du hast den Mietvertrag abgelehnt.");
                    break;
                case "beziehung":
                case "verlobt":
                    if (targetplayer.isOnline()) {
                        player.sendMessage("§cDu hast die Anfrage abgelehnt.");
                        targetplayer.sendMessage("§c" + player.getName() + " hat die Anfrage abgelehnt.");
                    } else {
                        player.sendMessage(Prefix.ERROR + "Spieler konnte nicht gefunden werden.");
                    }
                    break;
                case "blutgruppe":
                    if (targetplayer.isOnline()) {
                        player.sendMessage("§cDu hast die Anfrage abgelehnt.");
                        targetplayer.sendMessage("§e" + player.getName() + " hat die Anfrage abgelehnt.");
                    } else {
                        player.sendMessage(Prefix.ERROR + "Spieler konnte nicht gefunden werden.");
                    }
                case "streetwar":
                    Main.getInstance().streetwar.denyStreetwar(player, curr);
                    break;
                case "alliance":
                    Main.getInstance().gamePlay.alliance.deny(player, curr);
                    break;
                case "vertrag":
                    player.sendMessage(Prefix.MAIN + "Du hast den Vertrag abgelehnt!");
                    targetplayer.sendMessage(Prefix.MAIN + player.getName() + " hat den Vertrag abgelehnt!");
                    break;
            }
            deleteVertrag(player);
        } else {
            player.sendMessage(Prefix.ERROR + "Dir wird nichts angeboten.");
        }
    }

    public void sendInfoMessage(Player player) {
        TextComponent annehmen = new TextComponent("§8/§aannehmen");
        annehmen.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/annehmen"));
        annehmen.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§a§oAnnehmen")));

        TextComponent ablehnen = new TextComponent("§8/§cablehnen");
        ablehnen.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ablehnen"));
        ablehnen.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§c§oAblehnen")));

        TextComponent message = new TextComponent("§8 ➥§7 Nutze ");
        message.addExtra(annehmen);
        message.addExtra(new TextComponent("§7 oder "));
        message.addExtra(ablehnen);
        message.addExtra(new TextComponent("§7."));
        player.spigot().sendMessage(message);
    }
}
