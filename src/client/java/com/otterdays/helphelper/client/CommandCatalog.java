package com.otterdays.helphelper.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class CommandCatalog {
    private static final Map<String, CommandInfo> VANILLA_COMMANDS = vanillaCommands();

    private CommandCatalog() {
    }

    static CommandRow row(String command) {
        return row(command, topLevelCommand(command));
    }

    static CommandRow row(String command, String root) {
        return row(command, root, command, "Brigadier root /" + (root == null ? topLevelCommand(command) : root));
    }

    static CommandRow row(String command, String root, String syntax, String originHint) {
        String normalizedRoot = root == null || root.isBlank() ? topLevelCommand(command) : root;
        CommandInfo info = VANILLA_COMMANDS.getOrDefault(normalizedRoot, CommandInfo.server(normalizedRoot));
        String normalizedSyntax = syntax == null || syntax.isBlank() ? command : syntax;
        String normalizedOrigin = originHint == null || originHint.isBlank() ? originHint(normalizedRoot, info) : originHint;
        return new CommandRow(command, normalizedRoot, normalizedSyntax, normalizedOrigin, info);
    }

    private static String originHint(String root, CommandInfo info) {
        if (info.vanilla()) {
            return "Minecraft vanilla root /" + root;
        }
        int namespace = root.indexOf(':');
        if (namespace > 0) {
            return "Namespace " + root.substring(0, namespace);
        }
        return "Server/mod root /" + root;
    }

    private static String topLevelCommand(String command) {
        String cleaned = command.startsWith("/") ? command.substring(1) : command;
        int space = cleaned.indexOf(' ');
        return space < 0 ? cleaned : cleaned.substring(0, space);
    }

    private static Map<String, CommandInfo> vanillaCommands() {
        Map<String, CommandInfo> map = new HashMap<>();
        add(map, "advancement", "Player", false, "Advancement", "Grant, revoke, or test player advancements.",
            aliases(), presets("/advancement grant @s only <advancement>", "/advancement revoke @s only <advancement>"));
        add(map, "attribute", "Storage", false, "Attribute", "Inspect or modify entity attributes.", aliases(),
            presets("/attribute @s minecraft:generic.max_health get"));
        add(map, "bossbar", "Admin", false, "Bossbar", "Create and manage custom boss bars.", aliases(),
            presets("/bossbar list", "/bossbar add <id> <name>"));
        add(map, "clear", "Inventory", true, "Clear Inventory", "Remove items from player inventories.", aliases(),
            presets("/clear @s", "/clear @s minecraft:stone"));
        add(map, "clone", "Build", true, "Clone", "Copy blocks from one region to another.", aliases(),
            presets("/clone <begin> <end> <destination>"));
        add(map, "damage", "Entities", true, "Damage", "Apply damage to entities.", aliases(),
            presets("/damage @s 1", "/damage <target> <amount>"));
        add(map, "data", "Storage", true, "Data", "Inspect or modify block, entity, or storage NBT.", aliases(),
            presets("/data get entity @s", "/data get block ~ ~ ~"));
        add(map, "datapack", "Admin", true, "Data Pack", "List, enable, or disable datapacks.", aliases(),
            presets("/datapack list", "/datapack enable <name>"));
        add(map, "defaultgamemode", "World", false, "Default Game Mode",
            "Change the game mode new players receive.", aliases(), presets("/defaultgamemode survival"));
        add(map, "difficulty", "World", false, "Difficulty", "Change world difficulty.", aliases(),
            presets("/difficulty peaceful", "/difficulty easy", "/difficulty normal", "/difficulty hard"));
        add(map, "effect", "Entities", false, "Effect", "Apply or clear potion effects.", aliases(),
            presets("/effect give @s minecraft:night_vision 999999 0 true", "/effect clear @s"));
        add(map, "enchant", "Inventory", false, "Enchant", "Apply an enchantment to a held item.", aliases(),
            presets("/enchant @s minecraft:unbreaking 3"));
        add(map, "execute", "Utility", false, "Execute", "Run commands conditionally from another context.", aliases(),
            presets("/execute as @s at @s run <command>", "/execute if block ~ ~-1 ~ minecraft:stone run <command>"));
        add(map, "experience", "Player", false, "Experience", "Add, set, or query player experience.", aliases("xp"),
            presets("/xp query @s levels", "/xp add @s 1 levels", "/experience query @s points"));
        add(map, "fill", "Build", true, "Fill", "Fill a region with blocks. Large regions can be destructive.",
            aliases(), presets("/fill ~ ~ ~ ~5 ~5 ~5 minecraft:air", "/fill ~ ~ ~ ~5 ~5 ~5 minecraft:stone"));
        add(map, "fillbiome", "Worldgen", true, "Fill Biome", "Change biomes in a region.", aliases(),
            presets("/fillbiome ~ ~ ~ ~5 ~5 ~5 minecraft:plains"));
        add(map, "forceload", "Worldgen", true, "Force Load", "Keep chunks loaded even without players nearby.",
            aliases(), presets("/forceload query", "/forceload add ~ ~"));
        add(map, "function", "Utility", false, "Function", "Run datapack functions.", aliases(),
            presets("/function <namespace:path>"));
        add(map, "gamemode", "Player", false, "Game Mode", "Change your own or another player's game mode.", aliases(),
            presets("/gamemode survival", "/gamemode creative", "/gamemode adventure", "/gamemode spectator"));
        add(map, "gamerule", "World", true, "Game Rules", "Inspect or change world rule settings.", aliases(),
            presets("/gamerule keepInventory true", "/gamerule doDaylightCycle false"));
        add(map, "give", "Inventory", false, "Give", "Give items to a player.", aliases(),
            presets("/give @s minecraft:stone", "/give @s minecraft:torch 64"));
        add(map, "help", "Server", false, "Help", "Show usage for commands. This mod replaces bare /help with the GUI.",
            aliases(), presets("/help <command>"));
        add(map, "item", "Inventory", false, "Item", "Modify items in blocks or entity inventories.", aliases(),
            presets("/item replace entity @s hotbar.0 with minecraft:stone"));
        add(map, "kill", "Entities", true, "Kill", "Remove entities or players. Use with care.", aliases(),
            presets("/kill @s", "/kill @e[type=item]"));
        add(map, "list", "Server", false, "List Players", "Show the players currently connected to the server.",
            aliases(), presets("/list"));
        add(map, "locate", "Worldgen", false, "Locate", "Find structures, biomes, or points of interest.", aliases(),
            presets("/locate structure minecraft:village", "/locate biome minecraft:plains"));
        add(map, "loot", "Inventory", false, "Loot", "Generate loot into inventories, entities, or the world.", aliases(),
            presets("/loot give @s loot <loot_table>"));
        add(map, "msg", "Social", false, "Message", "Send private or team chat messages.", aliases("tell", "w"),
            presets("/msg <player> <message>", "/tell <player> <message>", "/teammsg <message>"));
        add(map, "particle", "Visual", false, "Particle", "Spawn particles for testing or effects.", aliases(),
            presets("/particle minecraft:happy_villager ~ ~1 ~"));
        add(map, "place", "Worldgen", true, "Place", "Place features, structures, or templates.", aliases(),
            presets("/place feature <feature>", "/place structure <structure>"));
        add(map, "playsound", "Visual", false, "Play Sound", "Play a sound to one or more players.", aliases(),
            presets("/playsound minecraft:block.note_block.pling master @s"));
        add(map, "publish", "Transport", false, "Publish", "Open an integrated world to LAN.", aliases(),
            presets("/publish"));
        add(map, "random", "Utility", false, "Random", "Draw or reset command random sequences.", aliases(),
            presets("/random value 1..100"));
        add(map, "recipe", "Inventory", false, "Recipe", "Grant or revoke crafting recipes.", aliases(),
            presets("/recipe give @s *", "/recipe take @s *"));
        add(map, "reload", "Admin", true, "Reload", "Reload datapacks and server resources.", aliases(),
            presets("/reload"));
        add(map, "return", "Utility", false, "Return", "Return a value from a function or macro command.", aliases(),
            presets("/return 1"));
        add(map, "ride", "Movement", false, "Ride", "Make entities mount or dismount other entities.", aliases(),
            presets("/ride <target> mount <vehicle>", "/ride <target> dismount"));
        add(map, "rotate", "Movement", false, "Rotate", "Rotate entities toward angles or targets.", aliases(),
            presets("/rotate @s 0 0", "/rotate @s facing entity <target>"));
        add(map, "say", "Chat", false, "Say", "Broadcast a server message.", aliases(), presets("/say <message>"));
        add(map, "schedule", "Utility", false, "Schedule", "Schedule a function to run later.", aliases(),
            presets("/schedule function <namespace:path> 10s"));
        add(map, "scoreboard", "Admin", false, "Scoreboard", "Manage scoreboard objectives, players, and teams.",
            aliases(), presets("/scoreboard objectives list", "/scoreboard players list @s"));
        add(map, "seed", "Server", false, "Seed", "Show the world seed when allowed.", aliases(), presets("/seed"));
        add(map, "setblock", "Build", true, "Set Block", "Set one block at a position.", aliases(),
            presets("/setblock ~ ~ ~ minecraft:stone", "/setblock ~ ~ ~ minecraft:air"));
        add(map, "setidletimeout", "Server", false, "Idle Timeout", "Set the server idle timeout.", aliases(),
            presets("/setidletimeout 0"));
        add(map, "setworldspawn", "World", false, "World Spawn", "Set the default world spawn.", aliases(),
            presets("/setworldspawn", "/setworldspawn ~ ~ ~"));
        add(map, "spawnpoint", "Player", false, "Spawn Point", "Set a player's personal spawn point.", aliases(),
            presets("/spawnpoint @s", "/spawnpoint @s ~ ~ ~"));
        add(map, "spectate", "Movement", false, "Spectate", "Start spectating an entity.", aliases(),
            presets("/spectate <target> @s"));
        add(map, "spreadplayers", "Movement", true, "Spread Players", "Spread entities across an area.", aliases(),
            presets("/spreadplayers ~ ~ 10 100 false @a"));
        add(map, "stop", "Admin", true, "Stop", "Stop a dedicated server.", aliases(), presets("/stop"));
        add(map, "stopsound", "Visual", false, "Stop Sound", "Stop sounds currently playing for players.", aliases(),
            presets("/stopsound @s", "/stopsound @s master"));
        add(map, "summon", "Entities", false, "Summon", "Create an entity at a position.", aliases(),
            presets("/summon minecraft:pig", "/summon minecraft:item"));
        add(map, "tag", "Storage", false, "Tag", "Add, remove, or list entity tags.", aliases(),
            presets("/tag @s list", "/tag @s add <name>", "/tag @s remove <name>"));
        add(map, "team", "Social", false, "Team", "Manage scoreboard teams.", aliases(),
            presets("/team list", "/team add <team>", "/team join <team> @s"));
        add(map, "teammsg", "Social", false, "Team Message", "Send a message to your team.", aliases("tm"),
            presets("/teammsg <message>", "/tm <message>"));
        add(map, "teleport", "Movement", false, "Teleport", "Move players or entities to another target or position.",
            aliases("tp"), presets("/tp @s ~ ~ ~", "/tp @s <player>", "/teleport <player> <x> <y> <z>"));
        add(map, "tellraw", "Chat", false, "Tell Raw", "Send raw JSON chat to players.", aliases(),
            presets("/tellraw @s {\"text\":\"Hello\"}"));
        add(map, "tick", "Debug", true, "Tick", "Inspect or control server ticking.", aliases(),
            presets("/tick query", "/tick rate 20"));
        add(map, "time", "World", false, "Time", "Inspect or change the world's time.", aliases(),
            presets("/time set day", "/time set night", "/time query daytime"));
        add(map, "title", "Visual", false, "Title", "Show title, subtitle, or actionbar text.", aliases(),
            presets("/title @s title {\"text\":\"Hello\"}", "/title @s actionbar {\"text\":\"Ready\"}"));
        add(map, "transfer", "Transport", false, "Transfer", "Transfer a player to another server.", aliases(),
            presets("/transfer <hostname>"));
        add(map, "trigger", "Utility", false, "Trigger", "Run a scoreboard trigger made available by a map or server.",
            aliases(), presets("/trigger <objective>", "/trigger <objective> set <value>"));
        add(map, "version", "Server", false, "Version", "Show the server version.", aliases(), presets("/version"));
        add(map, "waypoint", "Movement", false, "Waypoint", "Manage server-provided waypoints.", aliases(),
            presets("/waypoint list"));
        add(map, "weather", "World", false, "Weather", "Change or clear the current weather.", aliases(),
            presets("/weather clear", "/weather rain", "/weather thunder"));
        add(map, "worldborder", "Admin", true, "World Border", "Inspect or change the world border.", aliases(),
            presets("/worldborder get", "/worldborder set 1000"));
        add(map, "ban", "Admin", true, "Ban", "Ban players or IP addresses on dedicated servers.", aliases("ban-ip"),
            presets("/ban <player> <reason>", "/ban-ip <address> <reason>"));
        add(map, "op", "Admin", false, "Op", "Grant or revoke operator status.", aliases("deop"),
            presets("/op <player>", "/deop <player>"));
        add(map, "save-all", "Admin", false, "Save All", "Save server worlds to disk.", aliases(),
            presets("/save-all"));
        add(map, "save-off", "Admin", true, "Save Off", "Disable automatic world saving.", aliases(),
            presets("/save-off"));
        add(map, "save-on", "Admin", false, "Save On", "Enable automatic world saving.", aliases(), presets("/save-on"));
        add(map, "whitelist", "Admin", false, "Whitelist", "Manage the server whitelist.", aliases(),
            presets("/whitelist list", "/whitelist add <player>", "/whitelist remove <player>"));
        return Collections.unmodifiableMap(map);
    }

    private static void add(Map<String, CommandInfo> map, String root, String category, boolean risky, String title,
        String description, List<String> aliases, List<String> presets) {
        CommandInfo info = new CommandInfo(title, category, description, aliases, presets, true, risky);
        map.put(root, info);
        for (String alias : aliases) {
            map.put(alias, info);
        }
    }

    private static List<String> aliases(String... aliases) {
        return Arrays.asList(aliases);
    }

    private static List<String> presets(String... presets) {
        return Arrays.asList(presets);
    }

    record CommandRow(String command, String root, String syntax, String originHint, CommandInfo info) {
        String category() {
            return info.category();
        }

        boolean matches(String query) {
            return command.toLowerCase(Locale.ROOT).contains(query)
                || root.toLowerCase(Locale.ROOT).contains(query)
                || info.title().toLowerCase(Locale.ROOT).contains(query)
                || info.category().toLowerCase(Locale.ROOT).contains(query)
                || info.description().toLowerCase(Locale.ROOT).contains(query)
                || syntax.toLowerCase(Locale.ROOT).contains(query)
                || originHint.toLowerCase(Locale.ROOT).contains(query)
                || info.aliases().stream().anyMatch(alias -> alias.toLowerCase(Locale.ROOT).contains(query));
        }
    }

    record CommandInfo(String title, String category, String description, List<String> aliases,
        List<String> presets, boolean vanilla, boolean risky) {
        static CommandInfo server(String root) {
            return new CommandInfo(root, "Server/Modded", "Command provided by this server or another mod.",
                List.of(), List.of(), false, false);
        }
    }
}
