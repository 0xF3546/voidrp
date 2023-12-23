package de.polo.metropiacity.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.metropiacity.dataStorage.RegisteredBlock;
import de.polo.metropiacity.database.MySQL;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    private List<RegisteredBlock> registeredBlocks = new ArrayList<>();
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
            registeredBlocks.add(block);
        }
    }

    @SneakyThrows
    public void addBlock(RegisteredBlock block) {
        registeredBlocks.add(block);
        Connection connection = mySQL.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO blocks (info, x, y, z, world) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, block.getInfo());
        statement.setDouble(2,block.getLocation().getX());
        statement.setDouble(3, block.getLocation().getY());
        statement.setDouble(4, block.getLocation().getZ());
        statement.setString(5, block.getLocation().getWorld().getName());

        statement.execute();

        statement.close();
        connection.close();
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
