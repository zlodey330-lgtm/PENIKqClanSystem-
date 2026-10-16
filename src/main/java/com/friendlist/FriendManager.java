package com.friendlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    private static final Map<String, String> FRIENDS = new LinkedHashMap<>();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("friendlist.json");

    private FriendManager() {}

    public static synchronized void load() {
        FRIENDS.clear();
        if (!Files.exists(FILE)) return;
        try {
            List<String> names = GSON.fromJson(Files.readString(FILE), LIST_TYPE);
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
            Files.writeString(FILE, GSON.toJson(new ArrayList<>(FRIENDS.values())));
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
}
