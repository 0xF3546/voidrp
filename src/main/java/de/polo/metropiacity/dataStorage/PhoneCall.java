package de.polo.metropiacity.dataStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PhoneCall {
    private UUID caller;
    private List<UUID> participants = new ArrayList<>();


    public UUID getCaller() {
        return caller;
    }

    public void setCaller(UUID caller) {
        this.caller = caller;
    }

    public List<UUID> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UUID> participants) {
        this.participants = participants;
    }

    public void addParticipant(UUID uuid) {
        this.participants.add(uuid);
    }
}
