package net.xstudio.xcreative;

import org.bukkit.WorldCreator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.WorldType;
import org.bukkit.GameRule;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class main extends JavaPlugin implements CommandExecutor {

    private final HashMap<UUID, Location> previousLocations = new HashMap<>();
    // Add inventory storage for each world
    private final HashMap<UUID, ItemStack[]> survivalInventories = new HashMap<>();
    private final HashMap<UUID, ItemStack[]> creativeInventories = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("cr").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "[xCreative]" + ChatColor.WHITE + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // Get the creative world
        World creativeWorld = Bukkit.getWorld("creative");
        if (creativeWorld == null) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "[xCreative]" + ChatColor.WHITE + "Creative world not found! Creating it now...");
            WorldCreator creator = new WorldCreator("creative");
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            creativeWorld = Bukkit.createWorld(creator);
            if (creativeWorld == null) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[xCreative]" + ChatColor.WHITE + "Failed to create creative world!");
                return true;
            }
            // Disable mob spawning in the creative world
            creativeWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            creativeWorld.setGameRule(GameRule.DO_MOB_LOOT, false);
            creativeWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        }

        // Get the main world (assume it's called "world")
        World mainWorld = Bukkit.getWorld("world");
        if (mainWorld == null) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "[xCreative]" + ChatColor.WHITE + "Main world not found! Creating it now...");
            mainWorld = Bukkit.createWorld(new WorldCreator("world"));
            if (mainWorld == null) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[xCreative]" + ChatColor.WHITE + "Failed to create main world!");
                return true;
            }
        }

        // Toggle logic
        if (player.getWorld().getName().equalsIgnoreCase("creative")) {
            // Save creative inventory
            creativeInventories.put(player.getUniqueId(), player.getInventory().getContents());
            // Restore survival inventory (or clear if none)
            ItemStack[] survivalInv = survivalInventories.get(player.getUniqueId());
            if (survivalInv != null) {
                player.getInventory().setContents(survivalInv);
            } else {
                player.getInventory().clear();
            }
            Location previous = previousLocations.get(player.getUniqueId());
            if (previous != null) {
                player.teleport(previous);
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[xCreative]" + ChatColor.WHITE + "Returned to your previous location!");
                previousLocations.remove(player.getUniqueId());
            } else {
                player.teleport(mainWorld.getSpawnLocation());
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[xCreative]" + ChatColor.WHITE + "No previous location found. Teleported to the main world!");
            }
        } else {
            // Save survival inventory
            survivalInventories.put(player.getUniqueId(), player.getInventory().getContents());
            // Restore creative inventory (or clear if none)
            ItemStack[] creativeInv = creativeInventories.get(player.getUniqueId());
            if (creativeInv != null) {
                player.getInventory().setContents(creativeInv);
            } else {
                player.getInventory().clear();
            }
            previousLocations.put(player.getUniqueId(), player.getLocation());
            player.teleport(creativeWorld.getSpawnLocation());
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "[xCreative]" + ChatColor.WHITE + "Teleported to the creative world!");
        }
        return true;
    }
} 