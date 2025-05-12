package de.polo.core.game.base.shops;

import de.polo.api.VoidAPI;
import de.polo.api.crew.Crew;
import de.polo.core.Main;
import de.polo.core.crew.services.CrewService;
import de.polo.core.storage.CoreCompany;
import de.polo.core.utils.enums.ShopType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShopData {
    private int id;
    private String name;
    private int x;
    private int y;
    private int z;
    private World welt;
    private float yaw;
    private float pitch;
    private ShopType type;
    private int company;
    private int bank;
    @Setter
    private int crewHolder;
    private List<ShopItem> items = new ObjectArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public World getWelt() {
        return welt;
    }

    public void setWelt(World welt) {
        this.welt = welt;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public ShopType getType() {
        return type;
    }

    public void setType(ShopType type) {
        this.type = type;
    }

    public CoreCompany getCompany() {
        return Main.companyManager.getCompanyById(company);
    }

    public void setCompany(int company) {
        this.company = company;
    }

    @SneakyThrows
    public void save() {
        Main.getInstance().getCoreDatabase().updateAsync("UPDATE shops SET company = ? WHERE id = ?", getCompany().getId(), getId());
    }

    public int getBank() {
        return bank;
    }

    public void setBank(int bank) {
        this.bank = bank;
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public void setItems(List<ShopItem> items) {
        this.items = items;
    }

    public void removeItem(ShopItem shopItem) {
        items.remove(shopItem);
    }

    public void addItem(ShopItem shopItem) {
        items.add(shopItem);
    }

    @Nullable
    public Crew getCrewHolder() {
        CrewService crewService = VoidAPI.getService(CrewService.class);
        return crewService.getCrew(crewHolder);
    }

    public Location getLocation() {
        return new Location(welt, x, y, z, yaw, pitch);
    }
}
