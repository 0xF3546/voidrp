package de.polo.metropiacity.dataStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ticket {
    private int id;
    private String reason;
    private UUID creator;
    private List<UUID> editors = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getCreator() {
        return creator;
    }

    public void setCreator(UUID creator) {
        this.creator = creator;
    }

    public List<UUID> getEditors() {
        return editors;
    }

    public void setEditors(List<UUID> editors) {
        this.editors = editors;
    }

    public void addEditor(UUID uuid) {
        this.editors.add(uuid);
    }
}
