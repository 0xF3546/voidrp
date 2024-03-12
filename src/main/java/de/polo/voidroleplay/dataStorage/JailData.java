package de.polo.voidroleplay.dataStorage;

public class JailData {
    private String uuid;
    private int id;
    private int hafteinheiten;
    private String reason;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHafteinheiten() {
        return hafteinheiten;
    }

    public void setHafteinheiten(int hafteinheiten) {
        this.hafteinheiten = hafteinheiten;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
