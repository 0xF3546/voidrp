package de.polo.core.web.dto;

public class FactionDto {
    public final int id;
    public final String name;
    public final String fullname;

    public FactionDto(final int id, final String name, final String fullname) {
        this.id = id;
        this.name = name;
        this.fullname = fullname;
    }
}
