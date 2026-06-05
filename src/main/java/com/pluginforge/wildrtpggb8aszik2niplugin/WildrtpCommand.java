package com.pluginforge.wildrtpggb8aszik2niplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class WildrtpCommand implements CommandExecutor, TabCompleter, Listener {
    private static final String GUI_TITLE = "Wildrtp Hub";
    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Random random = new Random();

    public WildrtpCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("wildrtp.use")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }
        long cooldownSeconds = plugin.getConfig().getLong("cooldown-seconds", 54L);
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null) {
            long remaining = cooldownSeconds - ((now - last) / 1000L);
            if (remaining > 0) {
                player.sendMessage("Slow down — " + remaining + " seconds before using this again.");
                return true;
            }
        }
        cooldowns.put(player.getUniqueId(), now);
        int warmupSeconds = 0;
        if (warmupSeconds > 0) {
            player.sendMessage("Starting warmup...");
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) performMainAction(player);
                }
            }.runTaskLater(plugin, warmupSeconds * 20L);
        } else {
            performMainAction(player);
        }
        return true;
    }

    private void performMainAction(Player player) {
        int min = plugin.getConfig().getInt("radius-min", 667);
        int max = plugin.getConfig().getInt("radius-max", 2707);
        Location safe = findSafeLocation(player.getWorld(), Math.max(1, min), Math.max(min + 1, max));
        if (safe == null) {
            player.sendMessage("Could not find a safe random teleport location. Try again.");
            return;
        }
        player.teleport(safe);
        player.sendMessage("Teleported to a safe random location.");
    }

    private Location findSafeLocation(World world, int min, int max) {
        int range = Math.max(1, max - min + 1);
        for (int i = 0; i < 32; i++) {
            int distance = min + random.nextInt(range);
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int x = (int) Math.round(Math.cos(angle) * distance);
            int z = (int) Math.round(Math.sin(angle) * distance);
            int y = world.getHighestBlockYAt(x, z);
            Location location = new Location(world, x + 0.5D, y + 1.0D, z + 0.5D);
            if (isSafe(location)) return location;
        }
        return null;
    }

    private boolean isSafe(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block ground = feet.getRelative(0, -1, 0);
        Material groundType = ground.getType();
        return feet.getType().isAir()
                && head.getType().isAir()
                && groundType.isSolid()
                && !ground.isLiquid()
                && groundType != Material.LAVA
                && groundType != Material.MAGMA_BLOCK
                && groundType != Material.CACTUS;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
