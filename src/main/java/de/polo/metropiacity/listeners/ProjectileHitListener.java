package de.polo.metropiacity.listeners;

import de.polo.metropiacity.Main;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitListener implements Listener {
    public ProjectileHitListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile instanceof Arrow) {
            Arrow arrow = (Arrow) projectile;
            Block hitBlock = arrow.getLocation().getBlock();
            arrow.remove();
            /*new BukkitRunnable() {
                @Override
                public void run() {
                    hitBlock.breakNaturally();
                    hitBlock.getState().update();
                }
            }.runTask(Main.getInstance());*/
        }
    }
}
