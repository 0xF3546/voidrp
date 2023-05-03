package de.polo.void_roleplay.commands;

import de.polo.void_roleplay.Main;
import de.polo.void_roleplay.PlayerUtils.BankingUtils;
import de.polo.void_roleplay.Utils.FactionManager;
import de.polo.void_roleplay.Utils.LocationManager;
import de.polo.void_roleplay.Utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class bankCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        String syntax_error = Main.error + "Syntax-Fehler: /bank [Info/Einzahlen/Abbuchen/Überweisen] [Betrag] [<Spieler>]";
        if (LocationManager.nearATM(player)) {
            if (args.length == 0) {
                player.sendMessage(syntax_error);
            } else if (args.length >= 2) {
                int amount = Integer.parseInt(args[1]);
                if (amount >= 1) {
                    try {
                        if (args[0].equalsIgnoreCase("info")) {
                            BankingUtils.sendKontoauszug(player);
                        } else if (args[0].equalsIgnoreCase("einzahlen")) {
                            if (PlayerManager.money(player) >= amount) {
                                player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
                                player.sendMessage(" ");
                                player.sendMessage("§8 ➜ §3Kontoveränderung§8:§b Auszahlung");
                                player.sendMessage("§8 ➥ §aAlter Kontostand§8:§7 " + PlayerManager.bank(player) + "$");
                                PlayerManager.removeMoney(player, amount, "Bankauszahlung");
                                PlayerManager.addBankMoney(player, amount);
                                player.sendMessage("§8§l ➲ §a§l" + amount + " eingezahlt.");
                                player.sendMessage("§8 ➥ §aNeuer Kontostand§8:§7 " + PlayerManager.bank(player) + "$");
                            }
                        } else if (args[0].equalsIgnoreCase("abbuchen")) {
                            if (PlayerManager.bank(player) >= amount) {
                                player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
                                player.sendMessage(" ");
                                player.sendMessage("§8 ➜ §3Kontoveränderung§8:§b Auszahlung");
                                player.sendMessage("§8 ➥ §aAlter Kontostand§8:§7 " + PlayerManager.bank(player) + "$");
                                PlayerManager.removeMoney(player, amount, "Bankauszahlung");
                                PlayerManager.removeBankMoney(player, amount, "Abbuchung");
                                PlayerManager.addMoney(player, amount);
                                player.sendMessage("§8§l ➲ §a§l" + amount + " eingezahlt.");
                                player.sendMessage("§8 ➥ §aNeuer Kontostand§8:§7 " + PlayerManager.bank(player) + "$");
                            }
                        } else if (args[0].equalsIgnoreCase("überweisen")) {
                            if (args.length >= 3) {
                                Player targetplayer = Bukkit.getPlayer(args[2]);
                                assert targetplayer != null;
                                if (targetplayer.isOnline()) {
                                    if (PlayerManager.bank(player) >= amount) {
                                        player.sendMessage("§7     ===§8[§2KONTOAUSZUG§8]§7===");
                                        player.sendMessage(" ");
                                        player.sendMessage("§8 ➜ §3Kontoveränderung§8:§b Auszahlung");
                                        player.sendMessage("§8 ➥ §aAlter Kontostand§8:§7 " + PlayerManager.bank(player) + "$");
                                        PlayerManager.removeMoney(player, amount, "Bankauszahlung");
                                        PlayerManager.removeBankMoney(player, amount, "Überweisung " + targetplayer.getName());
                                        PlayerManager.addBankMoney(targetplayer, amount);
                                        targetplayer.sendMessage(Main.bank_prefix + "§c" + player.getName() + "§7 hat dir §c" + amount + "$§7 überwiesen");
                                        player.sendMessage("§8§l ➲ §a§l" + amount + " überwiesen an " + targetplayer.getName() + ".");
                                        player.sendMessage("§8 ➥ §aNeuer Kontostand§8:§7 " + PlayerManager.bank(player) + "$");
                                    }
                                } else {
                                    player.sendMessage(Main.error + "§c" + args[2] + "§7 ist nicht online.");
                                }
                            } else {
                                player.sendMessage(syntax_error);
                            }
                        } else {
                            player.sendMessage(syntax_error);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    player.sendMessage(Main.error + "Die Bankautomaten können nicht mit Negativen Zahlen umgehen...");
                }
            } else {
                player.sendMessage(syntax_error);
            }
        } else if(FactionManager.faction(player) != null && LocationManager.getDistanceBetweenCoords(player, FactionManager.faction(player) + "_" + "bank") <= 5) {
            String faction = FactionManager.faction(player);
            if (args.length == 0) {
                player.sendMessage(syntax_error);
            } else if (args.length >= 2) {
                int amount = Integer.parseInt(args[1]);
                if (amount >= 1) {
                    try {
                        if (args[0].equalsIgnoreCase("info")) {
                            BankingUtils.sendKontoauszug(player);
                        } else if (args[0].equalsIgnoreCase("einzahlen")) {
                            if (PlayerManager.money(player) >= amount) {
                                PlayerManager.removeMoney(player, amount, "Bankauszahlung");
                                FactionManager.addFactionMoney(faction, amount, "Bankeinzahlung " + player.getName());
                                player.sendMessage(Main.bank_prefix + "Du hast §c" + amount + "§7$ auf dein Fraktionskonto eingezahlt.");
                                BankingUtils.sendKontoauszug(player);
                            }
                        } else if (args[0].equalsIgnoreCase("abbuchen")) {
                            if (FactionManager.faction_grade(player) >= 6) {
                                if (FactionManager.factionBank(faction) >= amount) {
                                    FactionManager.removeFactionMoney(faction, amount, "Abbuchung durch " + player.getName());
                                    PlayerManager.addMoney(player, amount);
                                    player.sendMessage(Main.bank_prefix + "Du hast §c" + amount + "§7$ von deinem Fraktionskonto abgebucht.");
                                    BankingUtils.sendKontoauszug(player);
                                }
                            }
                        } else {
                            player.sendMessage(syntax_error);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    player.sendMessage(Main.error + "Die Bankautomaten können nicht mit Negativen Zahlen umgehen...");
                }
            } else {
                player.sendMessage(syntax_error);
            }
        } else {
            player.sendMessage(Main.error + "Du bist nicht in der nähe eines Bankautomaten.");
        }
        return false;
    }
}
