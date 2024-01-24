package de.polo.metropiacity.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.metropiacity.Main;
import de.polo.metropiacity.dataStorage.RegisteredBlock;
import de.polo.metropiacity.database.MySQL;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BlockManager {
    private final MySQL mySQL;
    private final List<RegisteredBlock> registeredBlocks = new ArrayList<>();
    public BlockManager(MySQL mySQL) {
        this.mySQL = mySQL;
        init();
    }

    @SneakyThrows
    private void init() {
        Statement statement = mySQL.getStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM blocks");
        ObjectMapper mapper = new ObjectMapper();
        while (result.next()) {
            RegisteredBlock block = new RegisteredBlock();
            block.setInfo(result.getString("info"));
            block.setInfoValue(result.getString("infoValue"));
            block.setId(result.getInt("id"));
            block.setLocation(new Location(Bukkit.getWorld(result.getString("world")), result.getDouble("x"), result.getDouble("y"), result.getDouble("z")));
            String b = result.getString("block");
            if (b != null) {
                block.setMaterial(Material.valueOf(b));
            }
            registeredBlocks.add(block);
        }
    }

    public RegisteredBlock getBlockAtLocation(Location location) {
        for (RegisteredBlock block : registeredBlocks) {
            if (block.getLocation().getBlockX() == location.getBlockX() && block.getLocation().getBlockY() == location.getBlockY() && block.getLocation().getBlockZ() == location.getBlockZ()) {
                return block;
            }
        }
        return null;
    }

    @SneakyThrows
    public int addBlock(RegisteredBlock block) {
        registeredBlocks.add(block);
        Connection connection = mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO blocks (info, infoValue, x, y, z, world) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, block.getInfo());
        statement.setString(2, block.getInfoValue());
        statement.setDouble(3,block.getLocation().getX());
        statement.setDouble(4, block.getLocation().getY());
        statement.setDouble(5, block.getLocation().getZ());
        statement.setString(6, block.getLocation().getWorld().getName());

        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        }

        statement.close();
        connection.close();
        return -1;
    }

    @SneakyThrows
    public void removeBlock(RegisteredBlock block) {
        registeredBlocks.remove(block);

        Statement statement = mySQL.getStatement();
        statement.execute("DELETE FROM blocks WHERE id = " + block.getId());
    }

    public Collection<RegisteredBlock> getBlocks() {
        return registeredBlocks;
    }
}
