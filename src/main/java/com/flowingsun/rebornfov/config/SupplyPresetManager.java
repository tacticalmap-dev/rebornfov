package com.flowingsun.rebornfov.config;

import com.flowingsun.rebornfov.RebornFovMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SupplyPresetManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, SupplyPreset> PRESETS = new LinkedHashMap<>();

    public static void ensurePresetDirectory() {
        Path dir = getPresetDirectory();
        try {
            Files.createDirectories(dir);
            Path sample = dir.resolve("default.json");
            if (Files.notExists(sample)) {
                JsonObject root = new JsonObject();
                root.addProperty("displayName", "Default Frontline Preset");
                var entries = new com.google.gson.JsonArray();
                entries.add(createEntry("minecraft:bread", 8, 120));
                entries.add(createEntry("minecraft:arrow", 16, 60));
                root.add("entries", entries);
                Files.writeString(sample, GSON.toJson(root));
            }
        } catch (IOException ignored) {
        }
    }

    private static JsonObject createEntry(String itemId, int amount, int intervalSeconds) {
        JsonObject object = new JsonObject();
        object.addProperty("item", itemId);
        object.addProperty("amount", amount);
        object.addProperty("intervalSeconds", intervalSeconds);
        return object;
    }

    public static void onConfigReload(net.minecraftforge.fml.event.config.ModConfigEvent event) {
        ensurePresetDirectory();
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        reload(event.getServer());
    }

    public static void reload(MinecraftServer server) {
        ensurePresetDirectory();
        PRESETS.clear();
        try (var stream = Files.list(getPresetDirectory())) {
            stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(path -> loadPreset(path).ifPresent(preset -> PRESETS.put(preset.id(), preset)));
        } catch (IOException ignored) {
        }

        if (PRESETS.isEmpty()) {
            PRESETS.put("default", SupplyPreset.empty("default", "Default Preset"));
        }
    }

    private static Optional<SupplyPreset> loadPreset(Path path) {
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            String fileName = path.getFileName().toString();
            String id = fileName.substring(0, fileName.length() - 5);
            String displayName = root.has("displayName") ? root.get("displayName").getAsString() : id;
            List<SupplyEntry> entries = new ArrayList<>();

            if (root.has("entries") && root.get("entries").isJsonArray()) {
                for (JsonElement element : root.getAsJsonArray("entries")) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    parseEntry(element.getAsJsonObject()).ifPresent(entries::add);
                }
            } else {
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    if ("displayName".equals(entry.getKey())) {
                        continue;
                    }
                    if (entry.getValue().isJsonObject()) {
                        JsonObject object = entry.getValue().getAsJsonObject();
                        object.addProperty("item", entry.getKey());
                        parseEntry(object).ifPresent(entries::add);
                    } else {
                        parseEntry(entry.getKey(), entry.getValue()).ifPresent(entries::add);
                    }
                }
            }
            return Optional.of(new SupplyPreset(id, displayName, entries));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static Optional<SupplyEntry> parseEntry(JsonObject object) {
        if (!object.has("item")) {
            return Optional.empty();
        }
        ResourceLocation itemId = ResourceLocation.tryParse(object.get("item").getAsString());
        Item item = itemId == null ? null : ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            return Optional.empty();
        }
        int amount = object.has("amount") ? object.get("amount").getAsInt() : 1;
        int interval = object.has("intervalSeconds")
                ? object.get("intervalSeconds").getAsInt()
                : (object.has("interval") ? object.get("interval").getAsInt() : 60);
        int maxCount = object.has("maxCount")
                ? object.get("maxCount").getAsInt()
                : (object.has("maxStock")
                ? object.get("maxStock").getAsInt()
                : (object.has("cap") ? object.get("cap").getAsInt() : -1));
        return Optional.of(new SupplyEntry(itemId, amount, interval, maxCount));
    }

    private static Optional<SupplyEntry> parseEntry(String itemKey, JsonElement value) {
        ResourceLocation itemId = ResourceLocation.tryParse(itemKey);
        Item item = itemId == null ? null : ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            return Optional.empty();
        }

        if (value.isJsonArray() && value.getAsJsonArray().size() >= 2) {
            int amount = value.getAsJsonArray().get(0).getAsInt();
            int interval = value.getAsJsonArray().get(1).getAsInt();
            int maxCount = value.getAsJsonArray().size() >= 3 ? value.getAsJsonArray().get(2).getAsInt() : -1;
            return Optional.of(new SupplyEntry(itemId, amount, interval, maxCount));
        }

        if (value.isJsonPrimitive()) {
            String raw = value.getAsString();
            String[] parts = raw.split("[-,:\\s]+");
            if (parts.length >= 2) {
                try {
                    int amount = Integer.parseInt(parts[0]);
                    int interval = Integer.parseInt(parts[1]);
                    int maxCount = parts.length >= 3 ? Integer.parseInt(parts[2]) : -1;
                    return Optional.of(new SupplyEntry(itemId, amount, interval, maxCount));
                } catch (NumberFormatException ignored) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    public static Path getPresetDirectory() {
        return FMLPaths.CONFIGDIR.get().resolve(RebornFovMod.MOD_ID).resolve("pre");
    }

    public static List<SupplyPreset> getPresets() {
        return Collections.unmodifiableList(new ArrayList<>(PRESETS.values()));
    }

    public static SupplyPreset getPreset(String presetId) {
        return PRESETS.getOrDefault(
                presetId,
                PRESETS.getOrDefault(RebornFovCommonConfig.defaultPreset, SupplyPreset.empty("default", "Default Preset"))
        );
    }

    public record SupplyPreset(String id, String displayName, List<SupplyEntry> entries) {
        public static SupplyPreset empty(String id, String displayName) {
            return new SupplyPreset(id, displayName, List.of());
        }
    }

    public record SupplyEntry(ResourceLocation itemId, int amount, int intervalSeconds, int maxCount) {
    }
}
