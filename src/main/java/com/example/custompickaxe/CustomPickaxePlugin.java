package com.example.custompickaxe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CustomPickaxePlugin extends JavaPlugin implements Listener {
    
    private static final int MORPH_COOLDOWN = 100; // milliseconds
    private NamespacedKey customPickaxeKey;
    private NamespacedKey eggLauncherKey;
    private FileConfiguration playerData;
    private File playerDataFile;
    private Map<UUID, Long> lastMorphTime = new HashMap<>();
    private final Set<UUID> specialEggs = new HashSet<>();
    
    // Tool morphing system
    
    @Override
    public void onEnable() {
        // Initialize the custom key for persistent data
        customPickaxeKey = new NamespacedKey(this, "starter_pickaxe");
        eggLauncherKey = new NamespacedKey(this, "egg_launcher");
        
        // Set up player data file
        setupPlayerData();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Save default config
        saveDefaultConfig();
        
        getLogger().info("Custom Pickaxe Plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (playerData != null) {
            savePlayerData();
        }
        getLogger().info("Custom Pickaxe Plugin has been disabled!");
    }
    
    private void setupPlayerData() {
        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        
        if (!playerDataFile.exists()) {
            getDataFolder().mkdirs();
            try {
                // Try to save the resource, if it doesn't exist, create an empty one
                saveResource("playerdata.yml", false);
            } catch (IllegalArgumentException e) {
                // Resource doesn't exist, create an empty file
                try {
                    playerDataFile.createNewFile();
                } catch (IOException ex) {
                    getLogger().warning("Could not create playerdata.yml file: " + ex.getMessage());
                }
            }
        }
        
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
        
        // Ensure the players section exists
        if (!playerData.contains("players")) {
            playerData.createSection("players");
            savePlayerData();
        }
    }
    
    private void savePlayerData() {
        if (playerData != null && playerDataFile != null) {
            try {
                playerData.save(playerDataFile);
            } catch (IOException e) {
                getLogger().warning("Could not save playerdata.yml file: " + e.getMessage());
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if this is the player's first time joining
        if (!playerData.contains("players." + playerId.toString())) {
            // Mark player as having joined before
            playerData.set("players." + playerId.toString() + ".first_join", System.currentTimeMillis());
            savePlayerData();
            
            // Give the custom pickaxe
            giveCustomPickaxe(player);
            
            // Send welcome message
            player.sendMessage(Component.text("Welcome to the server! ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text("You've received a special wooden pickaxe!")
                            .color(NamedTextColor.GOLD)));
        }
    }
    
    public ItemStack createCustomPickaxe() {
        ItemStack pickaxe = new ItemStack(Material.WOODEN_PICKAXE);
        ItemMeta meta = pickaxe.getItemMeta();
        
        if (meta != null) {
            meta.displayName(Component.text("Adaptive Magical Tool").color(NamedTextColor.GOLD));
            meta.lore(Arrays.asList(
                Component.text("A magical tool that adapts to your needs!").color(NamedTextColor.GRAY),
                Component.text("Changes form based on what you're doing!").color(NamedTextColor.BLUE).decorate(TextDecoration.ITALIC),
                Component.text(""),
                Component.text("⚡ Lightning Miner III").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD),
                Component.text("Mines in a 3x3 area with lightning!").color(NamedTextColor.DARK_PURPLE),
                Component.text(""),
                Component.text("🔄 Adaptive Tool").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD),
                Component.text("Morphs into the perfect tool!").color(NamedTextColor.DARK_PURPLE),
                Component.text(""),
                Component.text("Unbreakable").color(NamedTextColor.GREEN).decorate(TextDecoration.ITALIC)
            ));
            meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
            meta.addEnchant(Enchantment.FORTUNE, 2, true);
            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(customPickaxeKey, PersistentDataType.STRING, "starter_pickaxe");
            pickaxe.setItemMeta(meta);
        }
        
        return pickaxe;
    }
    
    public ItemStack createEggLauncher() {
        ItemStack eggLauncher = new ItemStack(Material.STICK);
        ItemMeta meta = eggLauncher.getItemMeta();
        
        if (meta != null) {
            meta.displayName(Component.text("Egg Launcher Supreme").color(NamedTextColor.GOLD));
            meta.lore(Arrays.asList(
                Component.text("A magical stick that launches explosive snowballs!").color(NamedTextColor.GRAY),
                Component.text("Left click to fire devastating snowball projectiles!").color(NamedTextColor.BLUE).decorate(TextDecoration.ITALIC),
                Component.text(""),
                Component.text("❄️ Snowball Barrage").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
                Component.text("Fires high-damage snowball projectiles!").color(NamedTextColor.DARK_PURPLE),
                Component.text(""),
                Component.text("⚡ Enchanted Weapon").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD),
                Component.text("Magical properties enhance damage!").color(NamedTextColor.DARK_PURPLE),
                Component.text(""),
                Component.text("Unbreakable").color(NamedTextColor.GREEN).decorate(TextDecoration.ITALIC)
            ));
            
            // Add enchantments for visual effect and power
            meta.addEnchant(Enchantment.FLAME, 1, true);
            meta.addEnchant(Enchantment.INFINITY, 1, true);
            meta.addEnchant(Enchantment.POWER, 5, true);
            meta.addEnchant(Enchantment.PUNCH, 2, true);
            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            meta.getPersistentDataContainer().set(eggLauncherKey, PersistentDataType.STRING, "egg_launcher");
            eggLauncher.setItemMeta(meta);
        }
        
        return eggLauncher;
    }
    
    private void giveCustomPickaxe(Player player) {
        ItemStack customPickaxe = createCustomPickaxe();
        
        // Try to add to inventory, drop if full
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(customPickaxe);
        } else {
            player.getWorld().dropItem(player.getLocation(), customPickaxe);
            player.sendMessage(Component.text("Your inventory is full! The pickaxe was dropped at your feet.")
                    .color(NamedTextColor.YELLOW));
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("custompickaxe")) {
            return false;
        }
        
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /custompickaxe <give|egglauncher|reload>")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "give":
                if (!sender.hasPermission("custompickaxe.give")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("This command can only be used by players!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                Player player = (Player) sender;
                giveCustomPickaxe(player);
                player.sendMessage(Component.text("You've been given a custom pickaxe!")
                        .color(NamedTextColor.GREEN));
                break;
                
            case "egglauncher":
                if (!sender.hasPermission("custompickaxe.give")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("This command can only be used by players!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                Player eggPlayer = (Player) sender;
                ItemStack eggLauncher = createEggLauncher();
                
                if (eggPlayer.getInventory().firstEmpty() != -1) {
                    eggPlayer.getInventory().addItem(eggLauncher);
                } else {
                    eggPlayer.getWorld().dropItem(eggPlayer.getLocation(), eggLauncher);
                    eggPlayer.sendMessage(Component.text("Your inventory is full! The egg launcher was dropped at your feet.")
                            .color(NamedTextColor.YELLOW));
                }
                
                eggPlayer.sendMessage(Component.text("❄️ You've been given the Egg Launcher Supreme!")
                        .color(NamedTextColor.GOLD));
                break;
                
            case "reload":
                if (!sender.hasPermission("custompickaxe.reload")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command!")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                reloadConfig();
                playerData = YamlConfiguration.loadConfiguration(playerDataFile);
                sender.sendMessage(Component.text("Plugin configuration reloaded!")
                        .color(NamedTextColor.GREEN));
                break;
                
            default:
                sender.sendMessage(Component.text("Unknown subcommand. Usage: /custompickaxe <give|egglauncher|reload>")
                        .color(NamedTextColor.RED));
                break;
        }
        
        return true;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        // Check if player is using our custom tool
        if (!isCustomTool(tool)) {
            return;
        }
        
        // Don't allow egg launcher to break blocks or morph
        if (isEggLauncher(tool)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("❄️ The Egg Launcher is for combat, not mining!")
                    .color(NamedTextColor.YELLOW));
            return;
        }
        
        Block brokenBlock = event.getBlock();
        Material blockType = brokenBlock.getType();
        
        // Check if this is a tree log and we're using an axe
        if (isLog(blockType) && tool.getType().name().contains("AXE")) {
            // Cancel the event to prevent normal drops
            event.setCancelled(true);
            
            // Perform tree felling
            fellTree(player, brokenBlock, tool);
            return;
        }
        
        // Only work on blocks that can be mined with current tool
        if (!canMineWithPickaxe(blockType) && !canMineWithShovel(blockType) && !canMineWithAxe(blockType)) {
            return;
        }
        
        // Completely cancel the original event to prevent any drops
        event.setCancelled(true);
        
        // Get the 3x3 area around the broken block
        Location center = brokenBlock.getLocation();
        World world = center.getWorld();
        
        // Strike lightning at the center (visual effect only)
        world.strikeLightningEffect(center);
        
        // Mine 3x3 area including the original block
        int itemsCollected = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    Block block = world.getBlockAt(blockLoc);
                    
                    // Only mine blocks that can be mined with current tool
                    if ((canMineWithPickaxe(block.getType()) || canMineWithShovel(block.getType()) || canMineWithAxe(block.getType())) && !block.getType().isAir()) {
                        // Get drops before breaking
                        Collection<ItemStack> drops = block.getDrops(tool);
                        
                        // Break the block manually
                        block.setType(Material.AIR);
                        
                        // Add drops to player inventory with better handling
                        itemsCollected += addItemsToInventory(player, drops);
                    }
                }
            }
        }
        
        // Add some visual flair with count
        player.sendMessage(Component.text("⚡ Lightning Miner activated! Collected " + itemsCollected + " items.")
                .color(NamedTextColor.AQUA));
    }
    
    private int addItemsToInventory(Player player, Collection<ItemStack> items) {
        int itemCount = 0;
        boolean inventoryFullMessageShown = false;
        
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            
            // Try to add to inventory
            var remaining = player.getInventory().addItem(item);
            
            // Count successfully added items
            if (remaining.isEmpty()) {
                itemCount += item.getAmount();
            } else {
                // Some items couldn't fit, add partial count
                int originalAmount = item.getAmount();
                int remainingAmount = remaining.values().stream().mapToInt(ItemStack::getAmount).sum();
                itemCount += (originalAmount - remainingAmount);
                
                // Drop remaining items at player's feet
                for (ItemStack remainingItem : remaining.values()) {
                    player.getWorld().dropItem(player.getLocation(), remainingItem);
                }
                
                // Show inventory full message only once
                if (!inventoryFullMessageShown) {
                    player.sendMessage(Component.text("⚠ Inventory full! Some items dropped at your feet.")
                            .color(NamedTextColor.YELLOW));
                    inventoryFullMessageShown = true;
                }
            }
        }
        
        return itemCount;
    }
    
    private void fellTree(Player player, Block startLog, ItemStack tool) {
        Set<Block> treeLogs = new HashSet<>();
        Set<Block> treeLeaves = new HashSet<>();
        
        // Find all connected logs
        findConnectedLogs(startLog, treeLogs, 0, 100); // Max 100 logs to prevent infinite loops
        
        // Find all leaves connected to these logs
        for (Block log : treeLogs) {
            findConnectedLeaves(log, treeLeaves, 0, 200); // Max 200 leaves
        }
        
        // Break all logs and collect drops
        int itemsCollected = 0;
        for (Block log : treeLogs) {
            Collection<ItemStack> drops = log.getDrops(tool);
            log.setType(Material.AIR);
            itemsCollected += addItemsToInventory(player, drops);
        }
        
        // Break all leaves and collect drops
        for (Block leaf : treeLeaves) {
            Collection<ItemStack> drops = leaf.getDrops(tool);
            leaf.setType(Material.AIR);
            itemsCollected += addItemsToInventory(player, drops);
        }
        
        // Visual effects
        World world = startLog.getWorld();
        world.strikeLightningEffect(startLog.getLocation());
        
        // Send message
        player.sendMessage(Component.text("🌳 Tree felled! Collected " + itemsCollected + " items from " + 
                treeLogs.size() + " logs and " + treeLeaves.size() + " leaves.")
                .color(NamedTextColor.GREEN));
    }
    
    private void findConnectedLogs(Block startBlock, Set<Block> foundLogs, int depth, int maxDepth) {
        if (depth >= maxDepth || foundLogs.size() >= maxDepth) {
            return;
        }
        
        if (!isLog(startBlock.getType()) || foundLogs.contains(startBlock)) {
            return;
        }
        
        foundLogs.add(startBlock);
        
        // Check all 26 surrounding blocks (3x3x3 minus center)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Block neighbor = startBlock.getRelative(x, y, z);
                    if (isLog(neighbor.getType()) && !foundLogs.contains(neighbor)) {
                        findConnectedLogs(neighbor, foundLogs, depth + 1, maxDepth);
                    }
                }
            }
        }
    }
    
    private void findConnectedLeaves(Block logBlock, Set<Block> foundLeaves, int depth, int maxDepth) {
        if (depth >= maxDepth || foundLeaves.size() >= maxDepth) {
            return;
        }
        
        // Check in a 5x5x5 area around each log for leaves
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block neighbor = logBlock.getRelative(x, y, z);
                    if (isLeaf(neighbor.getType()) && !foundLeaves.contains(neighbor)) {
                        foundLeaves.add(neighbor);
                        // Recursively find more leaves connected to this leaf
                        findMoreLeaves(neighbor, foundLeaves, depth + 1, maxDepth);
                    }
                }
            }
        }
    }
    
    private void findMoreLeaves(Block leafBlock, Set<Block> foundLeaves, int depth, int maxDepth) {
        if (depth >= maxDepth || foundLeaves.size() >= maxDepth) {
            return;
        }
        
        // Check in a 3x3x3 area around each leaf for more leaves
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Block neighbor = leafBlock.getRelative(x, y, z);
                    if (isLeaf(neighbor.getType()) && !foundLeaves.contains(neighbor)) {
                        foundLeaves.add(neighbor);
                        findMoreLeaves(neighbor, foundLeaves, depth + 1, maxDepth);
                    }
                }
            }
        }
    }
    
    private boolean isLog(Material material) {
        switch (material) {
            case OAK_LOG:
            case SPRUCE_LOG:
            case BIRCH_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
            case MANGROVE_LOG:
            case CHERRY_LOG:
            case CRIMSON_STEM:
            case WARPED_STEM:
            case STRIPPED_OAK_LOG:
            case STRIPPED_SPRUCE_LOG:
            case STRIPPED_BIRCH_LOG:
            case STRIPPED_JUNGLE_LOG:
            case STRIPPED_ACACIA_LOG:
            case STRIPPED_DARK_OAK_LOG:
            case STRIPPED_MANGROVE_LOG:
            case STRIPPED_CHERRY_LOG:
            case STRIPPED_CRIMSON_STEM:
            case STRIPPED_WARPED_STEM:
                return true;
            default:
                return false;
        }
    }
    
    private boolean isLeaf(Material material) {
        switch (material) {
            case OAK_LEAVES:
            case SPRUCE_LEAVES:
            case BIRCH_LEAVES:
            case JUNGLE_LEAVES:
            case ACACIA_LEAVES:
            case DARK_OAK_LEAVES:
            case MANGROVE_LEAVES:
            case CHERRY_LEAVES:
            case NETHER_WART_BLOCK:
            case WARPED_WART_BLOCK:
                return true;
            default:
                return false;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        
        // Check if the dropped item is our custom tool
        if (isCustomTool(item)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("You cannot drop this special tool!")
                    .color(NamedTextColor.RED));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        // Protect players from lightning damage when using our pickaxe
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            Player player = (Player) event.getEntity();
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            
            if (isCustomTool(mainHand)) {
                event.setCancelled(true);
                player.sendMessage(Component.text("⚡ Your magical tool protects you from lightning!")
                        .color(NamedTextColor.YELLOW));
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        // Check if player is using our custom tool
        if (!isCustomTool(tool)) {
            return;
        }
        
        // Handle egg launcher left-click shooting
        if (isEggLauncher(tool) && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
            event.setCancelled(true);
            
            // Launch snowball projectile
            Snowball snowball = player.launchProjectile(Snowball.class);
            snowball.setVelocity(player.getLocation().getDirection().multiply(2.5)); // Fast projectile
            
            // Add custom data to track this as our special snowball
            snowball.getPersistentDataContainer().set(eggLauncherKey, PersistentDataType.STRING, "launcher_snowball");
            specialEggs.add(snowball.getUniqueId());
            
            // Visual and sound effects
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 0.8f);
            player.sendMessage(Component.text("❄️ Snowball fired!").color(NamedTextColor.YELLOW));
            
            return;
        }
        
        // Don't morph the egg launcher - it stays as a stick
        if (isEggLauncher(tool)) {
            return;
        }
        
        // Handle adaptive tool morphing (existing code)
        Block targetBlock = event.getClickedBlock();
        if (targetBlock == null) {
            return;
        }
        
        Material blockType = targetBlock.getType();
        Material newToolType = getBestToolForBlock(blockType);
        
        if (newToolType != null && !tool.getType().equals(newToolType)) {
            if (!isOnCooldown(player)) {
                morphTool(player, newToolType, "block_interaction");
                lastMorphTime.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        // Check if player is using our custom tool
        if (!isCustomTool(tool)) {
            return;
        }
        
        // Don't morph the egg launcher - it stays as a stick
        if (isEggLauncher(tool)) {
            return;
        }
        
        // Check cooldown for adaptive tool morphing
        if (isOnCooldown(player)) {
            return;
        }
        
        // Morph to sword for combat
        if (!tool.getType().equals(Material.WOODEN_SWORD)) {
            morphTool(player, Material.WOODEN_SWORD, "combat");
            lastMorphTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
        
        // Lightning combat effect for adaptive tool sword form
        if (tool.getType().equals(Material.WOODEN_SWORD)) {
            Entity target = event.getEntity();
            World world = target.getWorld();
            
            // Strike lightning at the target's location (visual effect only)
            world.strikeLightningEffect(target.getLocation());
            
            // Add some extra damage for the lightning effect
            double extraDamage = 2.0; // 1 heart of extra lightning damage
            event.setDamage(event.getDamage() + extraDamage);
            
            // Send message to player
            player.sendMessage(Component.text("⚡ Lightning Strike! +" + (extraDamage/2) + " lightning damage!")
                    .color(NamedTextColor.YELLOW));
        }
    }
    
    private boolean isOnCooldown(Player player) {
        return lastMorphTime.containsKey(player.getUniqueId()) && 
               System.currentTimeMillis() - lastMorphTime.get(player.getUniqueId()) < MORPH_COOLDOWN;
    }
    
    private void morphTool(Player player, Material newToolType, String reason) {
        ItemStack oldTool = player.getInventory().getItemInMainHand();
        
        // Create new tool with same properties
        ItemStack newTool = createMorphedTool(newToolType);
        
        // Replace the tool
        player.getInventory().setItemInMainHand(newTool);
        
        // Visual feedback
        String toolName = getToolDisplayName(newToolType);
        player.sendMessage(Component.text("🔄 Tool morphed to " + toolName + "!")
                .color(NamedTextColor.LIGHT_PURPLE));
        
        // Sound effect (if you want to add one later)
        // player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
    }
    
    private Material getBestToolForBlock(Material blockType) {
        // Blocks best mined with pickaxe
        if (canMineWithPickaxe(blockType)) {
            return Material.WOODEN_PICKAXE;
        }
        
        // Blocks best mined with shovel
        if (canMineWithShovel(blockType)) {
            return Material.WOODEN_SHOVEL;
        }
        
        // Blocks best mined with axe
        if (canMineWithAxe(blockType)) {
            return Material.WOODEN_AXE;
        }
        
        return null; // No specific tool needed
    }
    
    private String getToolDisplayName(Material toolType) {
        if (toolType.name().contains("PICKAXE")) return "Pickaxe";
        if (toolType.name().contains("SHOVEL")) return "Shovel";
        if (toolType.name().contains("AXE")) return "Axe";
        if (toolType.name().contains("SWORD")) return "Sword";
        return "Tool";
    }
    
    private boolean isCustomTool(ItemStack item) {
        if (item == null) return false;
        
        // Check if it's any of our custom tools
        Material type = item.getType();
        if (!type.name().contains("WOODEN_PICKAXE") && 
            !type.name().contains("WOODEN_SHOVEL") && 
            !type.name().contains("WOODEN_AXE") && 
            !type.name().contains("WOODEN_SWORD") && 
            !type.name().contains("STICK")) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        return meta.getPersistentDataContainer().has(customPickaxeKey, PersistentDataType.STRING) || 
               meta.getPersistentDataContainer().has(eggLauncherKey, PersistentDataType.STRING);
    }
    
    private boolean isEggLauncher(ItemStack item) {
        if (item == null) return false;
        
        // Check if it's our custom egg launcher (stick)
        Material type = item.getType();
        if (type != Material.STICK) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        return meta.getPersistentDataContainer().has(eggLauncherKey, PersistentDataType.STRING);
    }
    
    private boolean canMineWithShovel(Material material) {
        switch (material) {
            case DIRT:
            case GRASS_BLOCK:
            case PODZOL:
            case MYCELIUM:
            case SAND:
            case RED_SAND:
            case GRAVEL:
            case CLAY:
            case SOUL_SAND:
            case SOUL_SOIL:
            case SNOW:
            case SNOW_BLOCK:
            case POWDER_SNOW:
                return true;
            default:
                return false;
        }
    }
    
    private boolean canMineWithAxe(Material material) {
        switch (material) {
            case OAK_LOG:
            case SPRUCE_LOG:
            case BIRCH_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
            case MANGROVE_LOG:
            case CHERRY_LOG:
            case CRIMSON_STEM:
            case WARPED_STEM:
            case STRIPPED_OAK_LOG:
            case STRIPPED_SPRUCE_LOG:
            case STRIPPED_BIRCH_LOG:
            case STRIPPED_JUNGLE_LOG:
            case STRIPPED_ACACIA_LOG:
            case STRIPPED_DARK_OAK_LOG:
            case STRIPPED_MANGROVE_LOG:
            case STRIPPED_CHERRY_LOG:
            case STRIPPED_CRIMSON_STEM:
            case STRIPPED_WARPED_STEM:
            case OAK_WOOD:
            case SPRUCE_WOOD:
            case BIRCH_WOOD:
            case JUNGLE_WOOD:
            case ACACIA_WOOD:
            case DARK_OAK_WOOD:
            case MANGROVE_WOOD:
            case CHERRY_WOOD:
            case CRIMSON_HYPHAE:
            case WARPED_HYPHAE:
            case OAK_PLANKS:
            case SPRUCE_PLANKS:
            case BIRCH_PLANKS:
            case JUNGLE_PLANKS:
            case ACACIA_PLANKS:
            case DARK_OAK_PLANKS:
            case MANGROVE_PLANKS:
            case CHERRY_PLANKS:
            case CRIMSON_PLANKS:
            case WARPED_PLANKS:
            case BAMBOO_PLANKS:
                return true;
            default:
                return false;
        }
    }
    
    private boolean canMineWithPickaxe(Material material) {
        // List of materials that can be mined with a pickaxe
        switch (material) {
            case STONE:
            case COBBLESTONE:
            case GRANITE:
            case DIORITE:
            case ANDESITE:
            case DEEPSLATE:
            case COBBLED_DEEPSLATE:
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
            case NETHER_QUARTZ_ORE:
            case ANCIENT_DEBRIS:
            case NETHERRACK:
            case BLACKSTONE:
            case BASALT:
            case SMOOTH_BASALT:
            case SANDSTONE:
            case RED_SANDSTONE:
            case OBSIDIAN:
            case CRYING_OBSIDIAN:
            case ICE:
            case PACKED_ICE:
            case BLUE_ICE:
            case TERRACOTTA:
            case WHITE_TERRACOTTA:
            case ORANGE_TERRACOTTA:
            case MAGENTA_TERRACOTTA:
            case LIGHT_BLUE_TERRACOTTA:
            case YELLOW_TERRACOTTA:
            case LIME_TERRACOTTA:
            case PINK_TERRACOTTA:
            case GRAY_TERRACOTTA:
            case LIGHT_GRAY_TERRACOTTA:
            case CYAN_TERRACOTTA:
            case PURPLE_TERRACOTTA:
            case BLUE_TERRACOTTA:
            case BROWN_TERRACOTTA:
            case GREEN_TERRACOTTA:
            case RED_TERRACOTTA:
            case BLACK_TERRACOTTA:
                return true;
            default:
                return false;
        }
    }
    
    private ItemStack createMorphedTool(Material toolType) {
        ItemStack tool = new ItemStack(toolType);
        ItemMeta meta = tool.getItemMeta();
        
        if (meta != null) {
            meta.displayName(Component.text("Adaptive Magical Tool").color(NamedTextColor.GOLD));
            meta.lore(Arrays.asList(
                Component.text("A magical tool that adapts to your needs!").color(NamedTextColor.GRAY),
                Component.text("Changes form based on what you're doing!").color(NamedTextColor.BLUE).decorate(TextDecoration.ITALIC),
                Component.text(""),
                Component.text("⚡ Lightning Miner III").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD),
                Component.text("Mines in a 3x3 area with lightning!").color(NamedTextColor.DARK_PURPLE),
                Component.text(""),
                Component.text("🔄 Adaptive Tool").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD),
                Component.text("Morphs into the perfect tool!").color(NamedTextColor.DARK_PURPLE),
                Component.text(""),
                Component.text("Unbreakable").color(NamedTextColor.GREEN).decorate(TextDecoration.ITALIC)
            ));
            
            // Add appropriate enchantments based on tool type
            if (toolType.name().contains("PICKAXE")) {
                meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
                meta.addEnchant(Enchantment.FORTUNE, 2, true);
            } else if (toolType.name().contains("SHOVEL")) {
                meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
                meta.addEnchant(Enchantment.FORTUNE, 2, true);
            } else if (toolType.name().contains("AXE")) {
                meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
                meta.addEnchant(Enchantment.FORTUNE, 1, true);
            } else if (toolType.name().contains("SWORD")) {
                meta.addEnchant(Enchantment.SHARPNESS, 3, true);
                meta.addEnchant(Enchantment.LOOTING, 2, true);
            } else if (toolType.name().contains("STICK")) {
                meta.addEnchant(Enchantment.FLAME, 1, true);
                meta.addEnchant(Enchantment.INFINITY, 1, true);
            }
            
            meta.addEnchant(Enchantment.UNBREAKING, 10, true);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
            if (toolType.name().contains("STICK")) {
                meta.getPersistentDataContainer().set(eggLauncherKey, PersistentDataType.STRING, "egg_launcher");
            } else {
                meta.getPersistentDataContainer().set(customPickaxeKey, PersistentDataType.STRING, "starter_pickaxe");
            }
            tool.setItemMeta(meta);
        }
        
        return tool;
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        
        // Check if it's a snowball from our launcher
        if (!(projectile instanceof Snowball)) {
            return;
        }
        
        Snowball snowball = (Snowball) projectile;
        if (!specialEggs.contains(snowball.getUniqueId())) {
            return;
        }
        
        // Remove from tracking set
        specialEggs.remove(snowball.getUniqueId());
        
        // Get the shooter (player)
        if (!(snowball.getShooter() instanceof Player)) {
            return;
        }
        
        Player shooter = (Player) snowball.getShooter();
        
        // Check if it hit an entity
        if (event.getHitEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getHitEntity();
            
            // Apply massive damage
            double damage = getConfig().getDouble("egg_launcher.damage", 20.0);
            target.damage(damage, shooter);
            
            Location hitLocation = target.getLocation();
            
            // Explosion particles
            target.getWorld().spawnParticle(Particle.EXPLOSION, hitLocation, 5, 0.5, 0.5, 0.5, 0.1);
            
            // Explosion sound
            target.getWorld().playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            
            // Lightning effect for extra drama
            target.getWorld().strikeLightningEffect(hitLocation);
            
            // Feedback to shooter
            shooter.sendMessage(Component.text("❄️ Snowball hit for " + (damage/2) + " hearts of damage!")
                    .color(NamedTextColor.GOLD));
        } else {
            // Hit a block - still show effects
            Location hitLocation = event.getHitBlock() != null ? 
                event.getHitBlock().getLocation().add(0.5, 0.5, 0.5) : 
                snowball.getLocation();
            
            // Explosion particles
            snowball.getWorld().spawnParticle(Particle.EXPLOSION, hitLocation, 3, 0.3, 0.3, 0.3, 0.1);
            
            // Explosion sound
            snowball.getWorld().playSound(hitLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
            
            // Feedback to shooter
            shooter.sendMessage(Component.text("❄️ Snowball exploded on impact!")
                    .color(NamedTextColor.YELLOW));
        }
    }
}
