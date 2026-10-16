package com.friendlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class FriendManager {
    public static final int DEFAULT_COLOR = 0xA06540; // default leather color

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final Map<String, String> FRIENDS = new LinkedHashMap<>();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("friendlist.json");

    private static int color = DEFAULT_COLOR;
    private static ItemStack[] stacks; // helmet, chestplate, leggings, boots

    private FriendManager() {}

    public static synchronized void load() {
        FRIENDS.clear();
        color = DEFAULT_COLOR;
        stacks = null;
        if (!Files.exists(FILE)) return;
        try {
            JsonElement root = JsonParser.parseString(Files.readString(FILE));
            List<String> names = null;
            if (root.isJsonArray()) {
                // old format: just a list of names
                names = GSON.fromJson(root, LIST_TYPE);
            } else if (root.isJsonObject()) {
                JsonObject o = root.getAsJsonObject();
                if (o.has("color")) color = o.get("color").getAsInt() & 0xFFFFFF;
                if (o.has("friends")) names = GSON.fromJson(o.get("friends"), LIST_TYPE);
            }
            if (names != null) {
                for (String n : names) {
                    if (n != null && VALID_NAME.matcher(n).matches()) {
                        FRIENDS.put(n.toLowerCase(Locale.ROOT), n);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[FriendList] Failed to read config: " + e);
        }
    }

    private static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("color", color);
            o.add("friends", GSON.toJsonTree(new ArrayList<>(FRIENDS.values())));
            Files.writeString(FILE, GSON.toJson(o));
        } catch (IOException e) {
            System.err.println("[FriendList] Failed to save config: " + e);
        }
    }

    public static synchronized boolean isFriend(String name) {
        return name != null && FRIENDS.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public static synchronized boolean add(String name) {
        if (name == null) return false;
        name = name.trim();
        if (!VALID_NAME.matcher(name).matches()) return false;
        String key = name.toLowerCase(Locale.ROOT);
        if (FRIENDS.containsKey(key)) return false;
        FRIENDS.put(key, name);
        save();
        return true;
    }

    public static synchronized void remove(String name) {
        if (name != null && FRIENDS.remove(name.toLowerCase(Locale.ROOT)) != null) {
            save();
        }
    }

    public static synchronized List<String> list() {
        return new ArrayList<>(FRIENDS.values());
    }

    public static synchronized int getColor() {
        return color;
    }

    public static synchronized void setColor(int newColor) {
        newColor &= 0xFFFFFF;
        if (newColor == color) return;
        color = newColor;
        stacks = null;
        save();
    }

    /** Dyed leather armor piece for the given slot (EMPTY for non-armor slots). */
    public static synchronized ItemStack leatherFor(EquipmentSlot slot) {
        int idx = switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> -1;
        };
        if (idx < 0) return ItemStack.EMPTY;
        if (stacks == null) {
            Item[] items = {Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS};
            ItemStack[] built = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                ItemStack s = new ItemStack(items[i]);
                s.getOrCreateSubNbt("display").putInt("color", color);
                built[i] = s;
            }
            stacks = built;
        }
        return stacks[idx];
    }
}
