package kyivsec.createmechanized;

import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

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

    public static final int DEFAULT_RGB = 0x00C800;
    public static final PilotHelmetColor DEFAULT = GREEN;

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
    public final int rgb;

    PilotHelmetColor(String id, String displayName, int rgb) {
        this.id = id;
        this.displayName = displayName;
        this.rgb = rgb;
    }

    public int argb() {
        return 0xFF000000 | rgb;
    }

    public static PilotHelmetColor fromDyeItem(Item item) {
        if (!(item instanceof DyeItem dye)) {
            return null;
        }
        return BY_ID.get(dye.getDyeColor().getName());
    }

    public static PilotHelmetColor fromRgb(int rgb) {
        return BY_RGB.get(rgb & 0xFFFFFF);
    }
}
