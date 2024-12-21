package de.polo.voidroleplay.storage;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * @author Mayson1337
 * @version 1.0.0
 * @since 1.0.0
 */
public class ShopBook {

    @Getter
    private final int id;

    @Getter
    private final Component title;

    @Getter
    private final String author;

    @Getter
    private final String type;

    @Getter
    private final List<Component> content;

    @Getter
    @Setter
    private int price;

    public ShopBook(int id, Component title, String author, String type, @Unmodifiable @NotNull List<Component> content) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.type = type;
        this.content = content;
    }
}
