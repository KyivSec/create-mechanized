package kyivsec.createmechanized;

import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Palette of 16 HUD colors selectable by combining a {@link DyeItem} with the
 * pilot helmet in a crafting grid. Each entry maps a vanilla dye name
 * (matching {@link net.minecraft.world.item.DyeColor#getName()}) to a
 * human-readable label and a custom RGB triplet, distinct from the vanilla dye
 * tint table so the HUD stays bright and saturated against the sky.
 */
public enum PilotHelmetColor {
    WHITE("white", "White", 0xFFFFFF),
    ORANGE("orange", "Orange", 0xFF8000),
    MAGENTA("magenta", "Magenta", 0xFF00FF),
    LIGHT_BLUE("light_blue", "Light Blue", 0x00BFFF),
    YELLOW("yellow", "Yellow", 0xFFFF00),
    LIME("lime", "Lime", 0x7FFF00),
    PINK("pink", "Pink", 0xFF69B4),
    GRAY("gray", "Gray", 0x808080),
    LIGHT_GRAY("light_gray", "Light Gray", 0xC0C0C0),
    CYAN("cyan", "Cyan", 0x00FFFF),
    PURPLE("purple", "Purple", 0xBF00FF),
    BLUE("blue", "Blue", 0x5555FF),
    BROWN("brown", "Brown", 0xC86400),
    GREEN("green", "Green", 0x00C800),
    RED("red", "Red", 0xFF0000),
    BLACK("black", "Black", 0x2A2A2A);

    /** RGB used by the HUD when no color component is set (matches the original COLOR_HUD). */
    public static final int DEFAULT_RGB = 0x1AFF6E;

    private static final Map<String, PilotHelmetColor> BY_ID = new HashMap<>();
    private static final Map<Integer, PilotHelmetColor> BY_RGB = new HashMap<>();

    static {
        for (PilotHelmetColor c : values()) {
            BY_ID.put(c.id, c);
            BY_RGB.put(c.rgb, c);
        }
    }

    public final String id;
    public final String displayName;
    public final int rgb; // 0xRRGGBB (no alpha)

    PilotHelmetColor(String id, String displayName, int rgb) {
        this.id = id;
        this.displayName = displayName;
        this.rgb = rgb;
    }

    /** Returns the RGB with full alpha as 0xFFRRGGBB. */
    public int argb() {
        return 0xFF000000 | rgb;
    }

    /** Maps a held {@link DyeItem} to its palette entry, or {@code null} if not a recognised dye. */
    public static PilotHelmetColor fromDyeItem(Item item) {
        if (!(item instanceof DyeItem dye)) {
            return null;
        }
        return BY_ID.get(dye.getDyeColor().getName());
    }

    /** Reverse lookup from a stored RGB to a palette entry. {@code null} if the RGB is not in the palette. */
    public static PilotHelmetColor fromRgb(int rgb) {
        return BY_RGB.get(rgb & 0xFFFFFF);
    }
}
