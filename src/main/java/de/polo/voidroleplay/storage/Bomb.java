package de.polo.voidroleplay.storage;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;

import java.time.LocalDateTime;
import java.util.Random;

public class Bomb {

    @Getter
    private final LocalDateTime placed;
    @Getter
    private final Block block;
    @Getter
    private final String color;
    private final String[] colors = {"Rot", "Blau", "Grün"};
    @Getter
    @Setter
    private int minutes;

    public Bomb(LocalDateTime placed, Block block, int minutes) {
        this.placed = placed;
        this.block = block;
        this.minutes = minutes;
        color = colors[new Random().nextInt(colors.length)];
    }
}