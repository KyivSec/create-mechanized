package kyivsec.createmechanized.client.hud;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.client.hud.layout.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.io.Reader;
import java.util.Optional;

public final class HudLayoutLoader extends SimplePreparableReloadListener<HudLayout> {

    private static final Gson GSON = new Gson();
    private static volatile HudLayout current = HudLayout.DEFAULT;

    @Override
    protected HudLayout prepare(ResourceManager rm, ProfilerFiller profiler) {
        return new HudLayout(
                load(rm, "boresight",           BoresightSettings.CODEC,          BoresightSettings.DEFAULT),
                load(rm, "altitude_box",         AltitudeBoxSettings.CODEC,        AltitudeBoxSettings.DEFAULT),
                load(rm, "speed_box",            SpeedBoxSettings.CODEC,           SpeedBoxSettings.DEFAULT),
                load(rm, "pitch_ladder",         PitchLadderSettings.CODEC,        PitchLadderSettings.DEFAULT),
                load(rm, "bank_scale",           BankScaleSettings.CODEC,          BankScaleSettings.DEFAULT),
                load(rm, "heading_tape",         HeadingTapeSettings.CODEC,        HeadingTapeSettings.DEFAULT),
                load(rm, "flight_path_marker",   FlightPathMarkerSettings.CODEC,   FlightPathMarkerSettings.DEFAULT)
        );
    }

    @Override
    protected void apply(HudLayout prepared, ResourceManager rm, ProfilerFiller profiler) {
        current = prepared;
    }

    public static HudLayout getCurrent() {
        return current;
    }

    public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new HudLayoutLoader());
    }

    private static <T> T load(ResourceManager rm, String name, Codec<T> codec, T fallback) {
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(CreateMechanizedMod.MODID, "hud_widgets/" + name + ".json");
        Optional<net.minecraft.server.packs.resources.Resource> res = rm.getResource(rl);
        if (res.isEmpty()) return fallback;
        try (Reader reader = res.get().openAsReader()) {
            JsonElement json = GsonHelper.fromJson(GSON, reader, JsonElement.class);
            return codec.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(e -> CreateMechanizedMod.LOGGER.warn("HUD widget '{}': {}", name, e))
                    .orElse(fallback);
        } catch (Exception e) {
            CreateMechanizedMod.LOGGER.warn("Failed to load HUD widget '{}': {}", name, e.getMessage());
            return fallback;
        }
    }
}
