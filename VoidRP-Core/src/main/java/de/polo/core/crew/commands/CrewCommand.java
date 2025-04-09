package de.polo.core.crew.commands;

import de.polo.api.Utils.ItemBuilder;
import de.polo.api.Utils.inventorymanager.CustomItem;
import de.polo.api.Utils.inventorymanager.InventoryManager;
import de.polo.api.VoidAPI;
import de.polo.api.crew.Crew;
import de.polo.api.crew.CrewRank;
import de.polo.api.crew.enums.CrewPermission;
import de.polo.api.player.VoidPlayer;
import de.polo.core.Main;
import de.polo.core.agreement.services.AgreementService;
import de.polo.core.crew.dto.CreateCrewDto;
import de.polo.core.crew.dto.CreateCrewRankDto;
import de.polo.core.crew.dto.CrewMemberDto;
import de.polo.core.crew.services.CrewService;
import de.polo.core.game.events.SubmitChatEvent;
import de.polo.core.handler.CommandBase;
import de.polo.core.handler.TabCompletion;
import de.polo.core.player.entities.PlayerData;
import de.polo.core.player.services.PlayerService;
import de.polo.core.storage.Agreement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static de.polo.core.Main.*;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
@CommandBase.CommandMeta(name = "crew")
public class CrewCommand extends CommandBase implements Listener {
    public CrewCommand(@NotNull CommandMeta meta) {
        super(meta);
        Main.registerListener(this);
    }

    @Override
    public void execute(@NotNull VoidPlayer player, @NotNull PlayerData playerData, @NotNull String[] args) throws Exception {
        openMain(player);
    }

    private void openMain(VoidPlayer player) {
        if (player.getData().getCrew() == null) {
            InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §cCrew gründen"));
            inventoryManager.setItem(new CustomItem(13, new ItemBuilder(Material.PAPER)
                    .setName("§8» §cCrew gründen")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCreateCrew(player);
                }
            });
        } else {
            InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §c" + player.getData().getCrew().getName()));
            inventoryManager.setItem(new CustomItem(12, new ItemBuilder(Material.SKELETON_SKULL)
                    .setName("§8» §6Mitglieder")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCrewMembers(player, 1, () -> {
                        openMain(player);
                    });
                }
            });
            inventoryManager.setItem(new CustomItem(13, new ItemBuilder(Material.PAPER)
                    .setName("§8» §6Ränge")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCrewRanks(player, 1, () -> {
                        openMain(player);
                    });
                }
            });
            inventoryManager.setItem(new CustomItem(18, new ItemBuilder(Material.BARRIER)
                    .setName("§8» §cCrew verlassen")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCrewLeave(player, () -> {
                        openMain(player);
                    });
                }
            });
        }
    }

    private void openCrewRanks(VoidPlayer player, int page, final Runnable onBack) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 54, Component.text("§8 » §cCrew Ränge"));

        List<CrewRank> crewRanks = player.getData().getCrew().getRanks();
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) crewRanks.size() / itemsPerPage);

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, crewRanks.size());

        for (int i = startIndex; i < endIndex; i++) {
            CrewRank rank = crewRanks.get(i);
            int slot = i - startIndex;

            int finalPage2 = page;
            inventoryManager.setItem(new CustomItem(slot, new ItemBuilder(Material.NAME_TAG)
                    .setName(Component.text(rank.getColor() + rank.getName()))
                    .setLore(List.of("§7Stufe: §c" + rank.getRank()))
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (player.getData().getCrewRank().hasPermission(CrewPermission.RANK)) {
                        openCrewRank(player, rank, () -> {
                            openCrewRanks(player, finalPage2, onBack);
                        });
                    }
                }
            });
        }

        // Navigation
        if (page > 1) {
            int finalPage1 = page;
            inventoryManager.setItem(new CustomItem(45, new ItemBuilder(Material.ARROW)
                    .setName("§8« §7Vorherige Seite")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCrewRanks(player, finalPage1 - 1, onBack);
                }
            });
        }

        inventoryManager.setItem(new CustomItem(49, new ItemBuilder(Material.PAPER)
                .setName("§7Seite §c" + page + "§7 von §c" + totalPages)
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                // No-op
            }
        });

        if (page < totalPages) {
            int finalPage = page;
            inventoryManager.setItem(new CustomItem(53, new ItemBuilder(Material.ARROW)
                    .setName("§7Nächste Seite §8»")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCrewRanks(player, finalPage + 1, onBack);
                }
            });
        }

        inventoryManager.setItem(new CustomItem(46, new ItemBuilder(Material.BARRIER)
                .setName("§cZurück")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                onBack.run();
            }
        });

        if (player.getData().getCrewRank().hasPermission(CrewPermission.RANK)) {
            int finalPage3 = page;
            inventoryManager.setItem(new CustomItem(47, new ItemBuilder(Material.NAME_TAG)
                    .setName("§8» §aRang erstellen")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCreateCrewRank(player, 0, () -> {
                        openCrewRanks(player, finalPage3, onBack);
                    });
                }
            });
        }
    }

    private void openCreateCrewRank(VoidPlayer player, final int grade, Runnable onBack) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §cRang erstellen"));
        String name = (String) player.getVariable("crewRankName");
        inventoryManager.setItem(new CustomItem(12, new ItemBuilder(Material.PAPER)
                .setName(name == null ? "§8» §cRang name" : "§8» §c " + name)
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.getData().setVariable("chatblock", "crewRankName");
                player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Bitte gib den Namen des Ranges ein."));
                player.getPlayer().closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(14, new ItemBuilder(Material.PAPER)
                .setName("§8» §cRang " + grade)
                .setLore(Arrays.asList(
                        "§8 ➥ §8[§6Rechtsklick§8]§7 -1",
                        "§8 ➥ §8[§6Linksklick§8]§7 +1"))
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.isLeftClick()) {
                    openCreateCrewRank(player, grade + 1, onBack);
                } else if (event.isRightClick()) {
                    if (grade <= 0) {
                        player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Der Rang kann nicht niedriger als 0 sein."));
                        return;
                    }
                    openCreateCrewRank(player, grade - 1, onBack);
                }
                player.getPlayer().closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(26, new ItemBuilder(Material.EMERALD)
                .setName("§8» §aErstellen")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (name == null) return;
                CrewService crewService = VoidAPI.getService(CrewService.class);
                CreateCrewRankDto createCrewRankDto = new CreateCrewRankDto(name, TextColor.fromHexString("#ffffff"), grade, player.getData().getCrew().getId(), false, false);
                crewService.addCrewRank(createCrewRankDto);
                player.getData().setVariable("crewRankName", null);
                player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du hast den Rang §c" + name + " §7 erstellt."));
                onBack.run();
            }
        });
    }

    private void openCrewRank(VoidPlayer player, CrewRank crewRank, final Runnable onBack) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §c" + crewRank.getName()));
        int i = 13;
        if (player.getData().getCrewRank().hasPermission(CrewPermission.PERMISSION)) {
            inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.PAPER)
                    .setName("§8» §cBerechtigungen")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (crewRank.isBoss()) {
                        player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du kannst den Boss nicht bearbeiten."));
                        return;
                    }
                    openManagePermissions(player, crewRank, () -> {
                        openCrewRank(player, crewRank, onBack);
                    });
                }
            });
        }
        if (player.getData().getCrewRank().hasPermission(CrewPermission.RANK)) {
            inventoryManager.setItem(new CustomItem(i + 1, new ItemBuilder(Material.NAME_TAG)
                    .setName("§8» §cName bearbeiten")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                }
            });
        }
    }

    private void openManagePermissions(VoidPlayer player, CrewRank crewRank, Runnable onBack) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §c" + crewRank.getName()));
        int i = 0;
        for (CrewPermission crewPermission : CrewPermission.values()) {
            boolean isPermitted = crewRank.getPermissions().contains(crewPermission);
            inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.PAPER)
                    .setName(crewPermission.getDisplayName())
                    .setLore(List.of("§7Berechtigung: §c" + (isPermitted ? "Aktiv" : "Inaktiv")))
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (isPermitted) {
                        crewRank.removePermission(crewPermission);
                        player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Die Berechtigung " + crewPermission.getName() + " wurde entfernt."));
                    } else {
                        crewRank.addPermission(crewPermission);
                        player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Die Berechtigung " + crewPermission.getName() + " wurde hinzugefügt."));
                    }
                    openManagePermissions(player, crewRank, onBack);
                }
            });
            i++;
        }
    }


    private void openCrewMembers(VoidPlayer player, int page, Runnable onBack) {
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 54, Component.text("§8 » §cCrew Mitglieder"));
        CrewService crewService = VoidAPI.getService(CrewService.class);
        List<CrewMemberDto> crewMembers = crewService.getCrewMembers(player.getData().getCrew());
        int itemsPerPage = 45; // 5 Zeilen à 9 Slots
        int totalPages = (int) Math.ceil((double) crewMembers.size() / itemsPerPage);

        // Grenzen prüfen
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, crewMembers.size());

        // Prüfen, ob crewMembers leer ist
        if (crewMembers.isEmpty()) {
            player.sendMessage(Component.text("§cEs gibt keine Crew-Mitglieder."));
            return; // Verlasse die Methode, wenn keine Mitglieder vorhanden sind.
        }

        // Inhalte setzen
        for (int i = startIndex; i < endIndex; i++) {
            CrewMemberDto member = crewMembers.get(i);
            int slot = i - startIndex;

            inventoryManager.setItem(new CustomItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§a" + member.getName())
                    .setOwner(member.getUuid().toString())
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCrewMember(player, member);
                }
            });
        }

        // Navigation
        if (page > 1) {
            int finalPage = page;
            inventoryManager.setItem(new CustomItem(45, new ItemBuilder(Material.ARROW)
                    .setName("§8« §7Vorherige Seite")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCrewMembers(player, finalPage - 1, onBack);
                }
            });
        }

        // Seitenanzeige (Mitte unten)
        inventoryManager.setItem(new CustomItem(49, new ItemBuilder(Material.PAPER)
                .setName("§7Seite §c" + page + "§7 von §c" + totalPages)
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                // No-op
            }
        });

        if (page < totalPages) {
            int finalPage1 = page;
            inventoryManager.setItem(new CustomItem(53, new ItemBuilder(Material.ARROW)
                    .setName("§7Nächste Seite §8»")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openCrewMembers(player, finalPage1 + 1, onBack);
                }
            });
        }

        inventoryManager.setItem(new CustomItem(46, new ItemBuilder(Material.BARRIER)
                .setName("§cZurück")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                onBack.run();
            }
        });
    }

    private void openCrewMember(VoidPlayer voidPlayer, CrewMemberDto crewMember) {
        InventoryManager inventoryManager = new InventoryManager(voidPlayer.getPlayer(), 27, Component.text("§8 » §c" + crewMember.getName()));
        inventoryManager.setItem(new CustomItem(4, new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§8» §c" + crewMember.getName())
                .setOwner(crewMember.getUuid().toString())
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
            }
        });
        inventoryManager.setItem(new CustomItem(12, new ItemBuilder(Material.PAPER)
                .setName("§8» §cRang bearbeiten")
                .setLore("§8 ➥ §c" + crewMember.getCrewRank().getName())
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (crewMember.getUuid().equals(voidPlayer.getUuid())) {
                    voidPlayer.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du kannst deinen eigenen Rang nicht bearbeiten."));
                    return;
                }
                openEditPlayerRank(voidPlayer, crewMember, 1, () -> {
                    openCrewMembers(voidPlayer, 1, () -> {
                        openCrewMember(voidPlayer, crewMember);
                    });
                });
            }
        });
        inventoryManager.setItem(new CustomItem(13, new ItemBuilder(Material.BARRIER)
                .setName("§8» §cKick")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (crewMember.getCrewRank().isBoss()) {
                    voidPlayer.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du kannst den Boss nicht kicken."));
                    return;
                }
                if (voidPlayer.getData().getCrewRank().hasPermission(CrewPermission.KICK)) {
                    openKickPlayer(voidPlayer, crewMember, () -> {
                        openCrewMembers(voidPlayer, 1, () -> {
                            openCrewMember(voidPlayer, crewMember);
                        });
                    });
                } else {
                    voidPlayer.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du hast keine Berechtigung dazu."));
                }
            }
        });

        inventoryManager.setItem(new CustomItem(26, new ItemBuilder(Material.BARRIER)
                .setName("§8» §cZurück")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                openCrewMembers(voidPlayer, 1, () -> {
                    openCrewMember(voidPlayer, crewMember);
                });
            }
        });
        inventoryManager.setItem(new CustomItem(14, new ItemBuilder(Material.PAPER)
                .setName("§8» §cEinladen")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (voidPlayer.getData().getCrewRank().hasPermission(CrewPermission.INVITE)) {
                    openInviteMenu(voidPlayer, () -> {
                        openCrewMembers(voidPlayer, 1, () -> {
                            openCrewMember(voidPlayer, crewMember);
                        });
                    });
                } else {
                    voidPlayer.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du hast keine Berechtigung dazu."));
                }
            }
        });
    }

    private void openInviteMenu(VoidPlayer voidPlayer, Runnable onBack) {
        InventoryManager inventoryManager = new InventoryManager(voidPlayer.getPlayer(), 27, Component.text("§8 » §cMitglied einladen"));
        CrewService crewService = VoidAPI.getService(CrewService.class);
        PlayerService playerService = VoidAPI.getService(PlayerService.class);
        AgreementService agreementService = VoidAPI.getService(AgreementService.class);
        int i = 0;
        for (VoidPlayer player : playerService.getPlayersInRange(voidPlayer.getLocation(), 10)) {
            if (player.getData().getCrew() != null) continue;
            inventoryManager.setItem(new CustomItem(i, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§8» §c" + player.getName())
                    .setOwner(player.getUuid().toString())
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    voidPlayer.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du hast " + player.getName() + " in die Crew eingeladen."));
                    player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du wurdest in die Crew eingeladen."));
                    Agreement agreement = new Agreement(voidPlayer, player, "CrewInvite",
                            () -> {
                                crewService.setPlayerCrew(player.getUuid(), voidPlayer.getData().getCrew().getId());
                                crewService.sendMessageToMembers(voidPlayer.getData().getCrew(), player.getName() + " ist der Crew beigetreten.");
                                voidPlayer.sendMessage(Component.text("§cCrew §8┃ §c➜ §7" + player.getName() + " ist der Crew beigetreten."));
                                player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du bist der Crew beigetreten."));
                            },
                            () -> {
                                voidPlayer.sendMessage(Component.text("§cCrew §8┃ §c➜ §7" + player.getName() + " hat die Einladung abgelehnt."));
                                player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du hast die Einladung abgelehnt."));
                            });
                    agreementService.setAgreement(voidPlayer, player, agreement);
                    agreementService.sendInfoMessage(player);
                }
            });
            i++;
        }
    }

    private void openKickPlayer(VoidPlayer voidPlayer, CrewMemberDto crewMember, Runnable onBack) {
        CrewService crewService = VoidAPI.getService(CrewService.class);
        InventoryManager inventoryManager = new InventoryManager(voidPlayer.getPlayer(), 27, Component.text("§8 » §c" + crewMember.getName()));
        inventoryManager.setItem(new CustomItem(13, new ItemBuilder(Material.BARRIER)
                .setName("§8» §cKick")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                crewService.removePlayerFromCrew(crewMember.getUuid());
                voidPlayer.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du hast " + crewMember.getName() + " gekickt."));
            }
        });

        inventoryManager.setItem(new CustomItem(26, new ItemBuilder(Material.BARRIER)
                .setName("§8» §cAbbrechen")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                onBack.run();
            }
        });
    }

    private void openEditPlayerRank(VoidPlayer voidPlayer, CrewMemberDto crewMember, int page, Runnable onBack) {
        CrewService crewService = VoidAPI.getService(CrewService.class);
        InventoryManager inventoryManager = new InventoryManager(voidPlayer.getPlayer(), 27, Component.text("§8 » §c" + crewMember.getName()));
        Crew crew = voidPlayer.getData().getCrew();
        List<CrewRank> ranks = crew.getRanks();

        int itemsPerPage = 18;
        int totalPages = (int) Math.ceil((double) ranks.size() / itemsPerPage);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, ranks.size());
        List<CrewRank> sublist = ranks.subList(start, end);

        int slot = 0;
        for (CrewRank rank : sublist) {
            boolean isCurrent = crewMember.getCrewRank().getId() == rank.getId();

            int finalPage2 = page;
            inventoryManager.setItem(new CustomItem(slot, new ItemBuilder(Material.NAME_TAG)
                    .setName(Component.text(rank.getColor() + rank.getName()))
                    .setLore(List.of(
                            "§7Stufe: §c" + rank.getRank(),
                            isCurrent ? "§aAktueller Rang" : "§7Klicken zum Zuweisen"
                    ))
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (isCurrent) {
                        voidPlayer.sendMessage(Component.text("§7" + crewMember.getName() + " hat diesen Rang bereits."));
                        return;
                    }

                    crewService.setPlayerCrewRank(crewMember.getUuid(), rank.getId());
                    crewService.sendMessageToMembers(crew, "§7" + crewMember.getName() + " ist jetzt §c" + rank.getName());
                    voidPlayer.sendMessage(Component.text("§7Du hast §c" + crewMember.getName() + " §7den Rang §c" + rank.getName() + " §7zugewiesen."));
                    openEditPlayerRank(voidPlayer, crewMember, finalPage2, onBack); // refresh
                }
            });
            slot++;
        }

        // Seitenanzeige (Slot 21)
        inventoryManager.setItem(new CustomItem(21, new ItemBuilder(Material.PAPER)
                .setName("§7Seite §c" + page + "§7 von §c" + totalPages)
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                // no-op
            }
        });

        // Vorherige Seite
        if (page > 1) {
            int finalPage1 = page;
            inventoryManager.setItem(new CustomItem(18, new ItemBuilder(Material.ARROW)
                    .setName("§8« §7Vorherige Seite")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openEditPlayerRank(voidPlayer, crewMember, finalPage1 - 1, onBack);
                }
            });
        }

        // Nächste Seite
        if (page < totalPages) {
            int finalPage = page;
            inventoryManager.setItem(new CustomItem(26, new ItemBuilder(Material.ARROW)
                    .setName("§7Nächste Seite §8»")
                    .build()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    openEditPlayerRank(voidPlayer, crewMember, finalPage + 1, onBack);
                }
            });
        }

        // Zurück zur Mitgliederliste (Slot 22)
        inventoryManager.setItem(new CustomItem(22, new ItemBuilder(Material.BARRIER)
                .setName("§cZurück")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                onBack.run();
            }
        });
    }



    private void openCrewLeave(VoidPlayer player, Runnable onBack) {
        CrewService crewService = VoidAPI.getService(CrewService.class);
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §cCrew verlassen"));
        inventoryManager.setItem(new CustomItem(13, new ItemBuilder(Material.SKELETON_SKULL)
                .setName("§8» §cCrew verlassen")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                crewService.sendMessageToMembers(player.getData().getCrew(), player.getName() + " hat die Crew verlassen.");
                crewService.removePlayerFromCrew(player.getUuid());
                player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du hast die Crew verlassen."));
            }
        });

        inventoryManager.setItem(new CustomItem(26, new ItemBuilder(Material.BARRIER)
                .setName("§8» §cAbbrechen")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                onBack.run();
            }
        });
    }

    private void openCreateCrew(VoidPlayer player) {
        CrewService crewService = VoidAPI.getService(CrewService.class);
        InventoryManager inventoryManager = new InventoryManager(player.getPlayer(), 27, Component.text("§8 » §cCrew gründen"));
        String name = (String) player.getVariable("crewName");
        inventoryManager.setItem(new CustomItem(13, new ItemBuilder(Material.PAPER)
                .setName(name == null ? "§8» §cCrew name" : "§8» §c " + name)
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                player.getData().setVariable("chatblock", "crewName");
                player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Bitte gib den Namen deiner Crew ein."));
                player.getPlayer().closeInventory();
            }
        });
        inventoryManager.setItem(new CustomItem(26,new ItemBuilder(Material.EMERALD)
                .setName("§8» §aErstellen")
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (name == null) return;
                CreateCrewDto createCrewDto = new CreateCrewDto(name, player.getUuid());
                crewService.createCrew(createCrewDto);
                player.getData().setVariable("crewName", null);
                player.sendMessage(Component.text("§cCrew §8┃ §c➜ §7Du hast die Crew §c" + name + " §7 gegründet."));
                openMain(player);
            }
        });
    }

    @EventHandler
    public void onChatSubmit(SubmitChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (event.getSubmitTo().equalsIgnoreCase("crewName")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            if (message.length() >= 20) {
                event.getPlayer().sendMessage(Component.text("§cCrew §8┃ §c➜ §7Der Name deiner Crew ist zu lang!"));
                event.end();
                return;
            }
            event.getPlayerData().setVariable("crewName", message);
            openCreateCrew(VoidAPI.getPlayer(player));
            event.end();
        }
        if (event.getSubmitTo().equalsIgnoreCase("crewRankName")) {
            if (event.isCancel()) {
                event.sendCancelMessage();
                event.end();
                return;
            }
            if (message.length() >= 20) {
                event.getPlayer().sendMessage(Component.text("§cCrew §8┃ §c➜ §7Der Name deines Ranges ist zu lang!"));
                event.end();
                return;
            }
            event.getPlayerData().setVariable("crewRankName", message);
            openCreateCrewRank(VoidAPI.getPlayer(player), 0, () -> {
                openMain(VoidAPI.getPlayer(player));
            });
            event.end();
        }
    }
}
