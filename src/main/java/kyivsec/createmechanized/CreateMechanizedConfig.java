package kyivsec.createmechanized;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CreateMechanizedConfig {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    public static final int DEFAULT_RGB = 0x00C800;
    public static final String DEFAULT_DISPLAY_NAME = "Green";

    public static final Map<String, ModConfigSpec.ConfigValue<String>> HELMET_DYE_COLORS = new LinkedHashMap<>();

    private static final Map<String, String> DISPLAY_NAMES = new LinkedHashMap<>();

    private static volatile Map<Integer, String> rgbToDyeId;

    static {
        String[][] entries = {
                { "white",       "White",       "FFFFFF" },
                { "orange",      "Orange",      "FF8000" },
                { "magenta",     "Magenta",     "FF00FF" },
                { "light_blue",  "Light Blue",  "00BFFF" },
                { "yellow",      "Yellow",      "FFFF00" },
                { "lime",        "Lime",        "7FFF00" },
                { "pink",        "Pink",        "FF69B4" },
                { "gray",        "Gray",        "808080" },
                { "light_gray",  "Light Gray",  "C0C0C0" },
                { "cyan",        "Cyan",        "00FFFF" },
                { "purple",      "Purple",      "BF00FF" },
                { "blue",        "Blue",        "5555FF" },
                { "brown",       "Brown",       "C86400" },
                { "green",       "Green",       "00C800" },
                { "red",         "Red",         "FF0000" },
                { "black",       "Black",       "2A2A2A" },
        };

        COMMON_BUILDER.push("pilotHelmetColors");
        COMMON_BUILDER.comment("HUD tint color applied when crafting the pilot helmet with the given dye. Values are RRGGBB hex strings.");
        for (String[] e : entries) {
            String dyeName = e[0];
            String displayName = e[1];
            String defaultHex = e[2];
            String dyeItemId = "minecraft:" + dyeName + "_dye";
            HELMET_DYE_COLORS.put(dyeItemId, COMMON_BUILDER.define(dyeName, defaultHex));
            DISPLAY_NAMES.put(dyeItemId, displayName);
        }
        COMMON_BUILDER.pop();

        COMMON_SPEC = COMMON_BUILDER.build();
    }

    private CreateMechanizedConfig() {
    }

    public static Integer getColorRgb(String dyeItemId) {
        ModConfigSpec.ConfigValue<String> cv = HELMET_DYE_COLORS.get(dyeItemId);
        if (cv == null) return null;
        return parseHexSafe(cv);
    }

    public static String getDisplayName(String dyeItemId) {
        return DISPLAY_NAMES.get(dyeItemId);
    }

    public static String findDyeIdByRgb(int rgb) {
        Map<Integer, String> map = rgbToDyeId;
        if (map == null) {
            map = buildRgbToDyeIdMap();
            if (map != null) rgbToDyeId = map;
        }
        if (map == null) return null;
        return map.get(rgb & 0xFFFFFF);
    }

    public static boolean isHelmetDye(String dyeItemId) {
        return HELMET_DYE_COLORS.containsKey(dyeItemId);
    }

    /** Invalidate the reverse-lookup cache; call when config reloads at runtime. */
    public static void invalidateReverseLookup() {
        rgbToDyeId = null;
    }

    private static Map<Integer, String> buildRgbToDyeIdMap() {
        Map<Integer, String> map = new HashMap<>(HELMET_DYE_COLORS.size() * 2);
        boolean anyParsed = false;
        for (Map.Entry<String, ModConfigSpec.ConfigValue<String>> e : HELMET_DYE_COLORS.entrySet()) {
            Integer color = parseHexSafe(e.getValue());
            if (color == null) return null;
            map.putIfAbsent(color, e.getKey());
            anyParsed = true;
        }
        return anyParsed ? map : null;
    }

    private static Integer parseHexSafe(ModConfigSpec.ConfigValue<String> cv) {
        try {
            return Integer.parseUnsignedInt(cv.get().trim(), 16) & 0xFFFFFF;
        } catch (NumberFormatException | IllegalStateException ignored) {
            return null;
        }
    }
}
