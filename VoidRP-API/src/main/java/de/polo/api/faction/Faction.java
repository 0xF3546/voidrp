package de.polo.api.faction;

import lombok.Getter;
import lombok.Setter;

/**
 * Pure domain model for a faction.
 *
 * <p><b>Rules:</b>
 * <ul>
 *   <li>No Hibernate annotations</li>
 *   <li>No Bukkit imports</li>
 *   <li>No persistence logic</li>
 *   <li>No static references to Core managers</li>
 * </ul>
 *
 * <p>This class is the authoritative representation of faction state inside
 * the application layer.  The Core module maps it to/from {@code FactionEntity}
 * (Hibernate) and keeps an in-memory copy in {@code FactionCache} for fast reads.
 */
@Getter
@Setter
public class Faction implements IFaction {

    private int id;
    private String name;
    private String fullname;
    private FactionType factionType;

    // ── Finance ──────────────────────────────────────────────────────────────
    private int bank;
    private int payDay;

    // ── Visual ───────────────────────────────────────────────────────────────
    private String primaryColor;
    private String secondaryColor;
    private String motd;
    /** The name of the chat colour (e.g. {@code "GRAY"}), stored as a plain String to keep Bukkit out of the API. */
    private String chatColor;

    // ── Organisation ─────────────────────────────────────────────────────────
    private int maxMember;
    private boolean active;
    private boolean badFrak;

    // ── Integrations ─────────────────────────────────────────────────────────
    private int teamSpeakID;
    private int channelGroupID;
    private int forumID;
    private int forumIDLeader;

    // ── Game features ─────────────────────────────────────────────────────────
    private boolean hasBlacklist;
    private boolean doGangwar;
    private boolean hasLaboratory;
    private int laboratory;
    private int jointsMade;

    // ── Relations ────────────────────────────────────────────────────────────
    private int allianceFaction;
    private int subGroupId;
    private int cooperationPartner;

    // ── Equip ────────────────────────────────────────────────────────────────
    private int equipPoints;
    private int tookOut;
}
