package de.polo.voidroleplay.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.voidroleplay.Main;
import de.polo.voidroleplay.dataStorage.FactionData;
import de.polo.voidroleplay.dataStorage.RegisteredBlock;
import de.polo.voidroleplay.database.MySQL;
import de.polo.voidroleplay.game.base.housing.House;
import de.polo.voidroleplay.game.faction.SprayableBanner;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
            if (block == null) continue;
            if (block.getLocation().getBlockX() == location.getBlockX() && block.getLocation().getBlockY() == location.getBlockY() && block.getLocation().getBlockZ() == location.getBlockZ()) {
                return block;
            }
        }
        return null;
    }

    public RegisteredBlock getBlockById(int id) {
        return registeredBlocks.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
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

    public RegisteredBlock getNearestBlockOfType(Location location, String type) {
        RegisteredBlock nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (RegisteredBlock data : registeredBlocks) {
            if (!data.getInfo().equalsIgnoreCase(type)) continue;
            if (data.getLocation() == null) continue;
            double distance = data.getLocation().distance(location);

            if (nearest == null || distance < nearestDistance) {
                nearest = data;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    @SneakyThrows
    public void removeBlock(RegisteredBlock block) {
        registeredBlocks.remove(block);

        Statement statement = mySQL.getStatement();
        statement.execute("DELETE FROM blocks WHERE id = " + block.getId());
    }

    public List<Block> getNearbyBlocks(Location location, double radius) {
        List<Block> blocks = new ArrayList<>();
        int bx = location.getBlockX();
        int by = location.getBlockY();
        int bz = location.getBlockZ();
        int r = (int) radius;

        for (int x = bx - r; x <= bx + r; x++) {
            for (int y = by - r; y <= by + r; y++) {
                for (int z = bz - r; z <= bz + r; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    public Collection<RegisteredBlock> getBlocks() {
        return registeredBlocks;
    }

    public void updateBlocksAtScenario(String scenario, FactionData faction) {
        List<RegisteredBlock> blocks = registeredBlocks.stream().filter(b -> b.getInfo().equalsIgnoreCase("gangzone") && b.getInfoValue().equalsIgnoreCase(scenario)).collect(Collectors.toList());
        for (RegisteredBlock block : blocks) {
            BlockState state = block.getLocation().getBlock().getState();
            if (state instanceof Banner) {
                //block.getLocation().getBlock().setType(faction.getBannerColor());
                ((Banner) state).setPatterns(faction.getBannerPattern());
                state.update();
            }
        }
    }
}
