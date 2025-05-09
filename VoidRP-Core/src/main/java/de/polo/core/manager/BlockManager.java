package de.polo.core.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.polo.core.Main;
import de.polo.core.database.impl.CoreDatabase;
import de.polo.core.faction.entity.Faction;
import de.polo.core.storage.RegisteredBlock;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.polo.core.Main.database;

public class BlockManager {
    private final CoreDatabase coreDatabase;
    private final List<RegisteredBlock> registeredBlocks = new ObjectArrayList<>();

    public BlockManager(CoreDatabase coreDatabase) {
        this.coreDatabase = coreDatabase;
        init();
    }

    @SneakyThrows
    private void init() {
        // Versuche, die Verbindung und die PreparedStatement Objekte zu verwalten
        try (Statement statement = coreDatabase.getStatement();
             ResultSet result = statement.executeQuery("SELECT * FROM blocks")) {

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
        } // Alle Ressourcen wie Statement und ResultSet werden automatisch geschlossen
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
        Main.getInstance().getCoreDatabase().insertAndGetKeyAsync("INSERT INTO blocks (info, infoValue, x, y, z, world) VALUES (?, ?, ?, ?, ?, ?)",
                        block.getInfo(),
                        block.getInfoValue(),
                        block.getLocation().getX(),
                        block.getLocation().getY(),
                        block.getLocation().getZ(),
                        block.getLocation().getWorld().getName())
                .thenApply(key -> {
                    key.ifPresent(block::setId);
                    return null;
                });

        return block.getId();
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

        Statement statement = coreDatabase.getStatement();
        statement.execute("DELETE FROM blocks WHERE id = " + block.getId());
    }

    public List<Block> getNearbyBlocks(Location location, double radius) {
        List<Block> blocks = new ObjectArrayList<>();
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

    public void updateBlocksAtScenario(String scenario, Faction faction) {
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

    public void deleteBlock(int blockId) {
        RegisteredBlock block = getBlockById(blockId);
        if (block != null) {
            block.getLocation().getBlock().setType(Material.AIR);
            registeredBlocks.remove(block);
            database.deleteAsync("DELETE FROM blocks WHERE id = ?", blockId);
        }
    }

    public void updateBlock(RegisteredBlock registeredBlock) {
        database.updateAsync("UPDATE blocks SET info = ?, infoValue = ?, x = ?, y = ?, z = ?, world = ? WHERE id = ?",
                registeredBlock.getInfo(),
                registeredBlock.getInfoValue(),
                registeredBlock.getLocation().getX(),
                registeredBlock.getLocation().getY(),
                registeredBlock.getLocation().getZ(),
                registeredBlock.getLocation().getWorld().getName(),
                registeredBlock.getId());
    }
}
