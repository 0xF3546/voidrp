package de.polo.voidroleplay.dataStorage;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;

import java.time.LocalDateTime;
import java.util.Random;

public class Bomb {

    @Getter
    private final LocalDateTime placed;

    @Getter
    @Setter
    private int minutes;

    @Getter
    private final Block block;


    @Getter
    private final String color;


    private final String[] colors = {"Rot", "Blau", "Grün"};
    public Bomb(LocalDateTime placed, Block block, int minutes) {
        this.placed = placed;
        this.block = block;
        this.minutes = minutes;
        color = colors[new Random().nextInt(colors.length)];
    }
}