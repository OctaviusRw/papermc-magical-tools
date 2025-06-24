# Custom Pickaxe Plugin

A PaperMC plugin for Minecraft 1.21.4+ that gives new players a magical custom wooden pickaxe when they join the server for the first time.

## Features

- **First-time Join Detection**: Automatically detects when a player joins the server for the first time
- **Magical Wooden Pickaxe**: Creates a special pickaxe with:
  - Custom name: "Starter's Lucky Pickaxe" (in gold text)
  - Custom lore with welcome messages and enchantment descriptions
  - **Efficiency III** and **Unbreaking X** enchantments
  - **Unbreakable** - Never breaks or loses durability
  - **Undropable** - Cannot be dropped by players (prevents accidental loss)
  - **Lightning Miner III** - Custom enchantment that mines in a 3x3 area
  - **Lightning Protection** - Protects players from lightning damage
  - Persistent data to identify it as a custom item
- **3x3 Area Mining**: When mining with the pickaxe:
  - Mines a 3x3x3 cube around the broken block
  - Only affects blocks that can be mined with a pickaxe
  - All mined items go directly to the player's inventory
  - Creates a lightning effect at the mining location
  - Shows "⚡ Lightning Miner activated!" message
- **Smart Inventory Management**: Adds the pickaxe to the player's inventory or drops it if full
- **Admin Commands**: Commands to manually give pickaxes and reload configuration
- **Persistent Data**: Tracks which players have already received their starter pickaxe

## Adaptive Tool Morphing

### 🔄 **Adaptive Tool Morphing**
- **Smart Detection**: Automatically detects what you're trying to do
- **Instant Morphing**: Changes into the perfect tool for the job
  - **Pickaxe** for mining stone, ores, and hard blocks
  - **Shovel** for digging dirt, sand, gravel, and soft blocks  
  - **Axe** for chopping wood, logs, and wooden blocks
  - **Sword** for combat when attacking entities
- **Seamless Transition**: Keeps all enchantments and properties when morphing
- **Visual Feedback**: Shows morph messages with tool names

### ✨ Features

### 🔄 **Adaptive Tool Morphing**
The tool automatically changes its type based on what you're doing:
- **🏗️ Mining Stone/Ores** → Morphs to **Pickaxe** (Efficiency V, Fortune III)
- **🏖️ Digging Dirt/Sand/Gravel** → Morphs to **Shovel** (Efficiency V, Fortune III)  
- **🌲 Chopping Wood/Logs** → Morphs to **Axe** (Efficiency V, Fortune III)
- **⚔️ Fighting Mobs** → Morphs to **Sword** (Sharpness V, Looting III)

### 🌳 **Intelligent Tree Felling**
When using the axe form on any log block:
- **Automatically chops down the entire tree** including all connected logs
- **Removes all leaves** connected to the tree
- **Collects all drops** directly to your inventory
- **Works on all tree types** including Nether trees (Crimson/Warped)
- **Lightning visual effects** for dramatic tree felling
- **Smart detection** prevents infinite loops and lag

### ⚔️ **Lightning Combat**
When using the sword form in combat:
- **Lightning strikes the target** with every hit
- **Extra lightning damage** (+1 heart) on top of sword damage
- **Dramatic visual effects** make combat epic
- **Works on all mobs** - hostile, neutral, and passive
- **Automatic sword morphing** when attacking any entity

### ⚡ **Lightning Mining**
- **3x3x3 area mining** with any tool form
- **Lightning strike effects** at the mining location
- **All items collected** directly to inventory (no ground drops!)
- **Works with all tool forms** - pickaxe, shovel, axe

### 🛡️ **Magical Properties**
- **Unbreakable** - Never breaks or loses durability
- **Undropable** - Cannot be dropped or lost on death
- **Lightning Immunity** - Protects holder from lightning damage
- **Auto-given** to new players on first join
- **Persistent** - Retains properties through server restarts

## 🎯 **Weapons & Tools**

### 🔄 **Adaptive Magical Tool**
The main tool that automatically morphs based on your actions:
- **⛏️ Pickaxe** for mining stone, ores, and hard materials
- **🪣 Shovel** for digging dirt, sand, gravel, and soft blocks  
- **🪓 Axe** for chopping wood and tree-related materials
- **⚔️ Sword** for combat against mobs and players
- **🔄 Auto-morphing** - Changes form based on what you're doing

### 🥶 **Snowball Launcher Supreme**
A dedicated magical weapon that stays in its original form:
- **Always remains a stick** - No morphing or changing
- **Left-click to fire** explosive snowball projectiles
- **10 hearts damage** per hit (20.0 damage points)
- **Lightning strike effects** on impact
- **Explosion particles** and dramatic sound effects
- **Combat-only weapon** - Cannot mine blocks
- **Unbreakable and enchanted** with magical properties
- **Cannot be dropped** - stays with you always
- **No chicken spawning** - Uses snowballs instead of eggs

## ⚡ **Special Features**

## 🎮 How It Works

## Installation

1. **Build the Plugin**:
   ```bash
   ./gradlew shadowJar
   ```

2. **Install on Server**:
   - Copy the generated JAR file from `build/libs/custom-pickaxe-plugin-1.0.0.jar`
   - Place it in your PaperMC server's `plugins/` directory
   - Restart your server

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/custompickaxe give` | Give yourself the adaptive magical tool | `custompickaxe.give` |
| `/custompickaxe egglauncher` | Give yourself the egg launcher weapon | `custompickaxe.give` |
| `/custompickaxe reload` | Reload plugin configuration | `custompickaxe.reload` |

## Permissions

- `custompickaxe.give` - Allows giving custom pickaxes to players (default: op)
- `custompickaxe.reload` - Allows reloading the plugin configuration (default: op)

## Configuration

The plugin creates a `config.yml` file where you can customize:
- Pickaxe name and lore
- Enchantments and their levels
- Welcome messages
- Whether to give pickaxes on first join

## Requirements

- **Server**: PaperMC 1.21.4+ (or compatible)
- **Java**: Java 21+
- **API**: Paper API 1.21.6-R0.1-SNAPSHOT

## Development

This plugin is built using:
- Gradle with Kotlin DSL
- PaperMC API
- Adventure Text Components for modern text formatting
- Persistent Data Containers for item identification

## Files Created

The plugin creates the following files in the `plugins/CustomPickaxePlugin/` directory:
- `config.yml` - Plugin configuration
- `playerdata.yml` - Tracks which players have joined before

## Building from Source

1. Clone or download this project
2. Ensure you have Java 21+ installed
3. Run: `./gradlew shadowJar`
4. The compiled plugin will be in `build/libs/`

## License

This plugin is provided as-is for educational and server use.

## About

A comprehensive PaperMC plugin featuring magical tools and weapons for Minecraft 1.21.4+. Originally a simple custom pickaxe, it has evolved into a full magical toolkit with adaptive morphing capabilities and devastating weapons.
