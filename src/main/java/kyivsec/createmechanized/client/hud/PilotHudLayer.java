package kyivsec.createmechanized.client.hud;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import kyivsec.createmechanized.CreateMechanizedMod;
import kyivsec.createmechanized.PilotHelmetWearableItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Pilot HUD overlay layer. Registered against the in-game GUI stack via
 * {@link HudEvents}; renders once per frame.
 *
 * <p>Behaviour:</p>
 * <ol>
 *     <li>If the GUI is hidden (F1) or there is no player, do nothing.</li>
 *     <li>If the player is not wearing a {@code pilot_helmet}, do nothing.</li>
 *     <li>If the player is riding/standing on a Sable sublevel, render the full
 *     F-22 style HUD using that sublevel's pose data.</li>
 *     <li>Otherwise render the minimal HUD (boresight + altitude only).</li>
 * </ol>
 */
public final class PilotHudLayer implements LayeredDraw.Layer {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(CreateMechanizedMod.MODID, "pilot_hud");

    @Override
    public void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (!PilotHelmetWearableItem.isWornBy(player)) {
            return;
        }

        float partialTick = delta.getGameTimeDeltaPartialTick(true);
        ClientSubLevelAccess sub = tryResolveSubLevel(player);

        FlightData data = (sub != null)
                ? FlightDataSource.fromSubLevel(sub, partialTick)
                : FlightDataSource.fromPlayer(player);

        ItemStack helmetStack = player.getItemBySlot(EquipmentSlot.HEAD);
        int colorRgb = PilotHelmetWearableItem.getColorRgb(helmetStack);

        HudRenderer.draw(graphics, data, colorRgb);
    }

    /**
     * Resolves the Sable client sublevel the player is currently piloting or standing on.
     *
     * <p>Tries, in order:</p>
     * <ol>
     *     <li>{@code getTrackingOrVehicleSubLevel} — the player is the passenger of an
     *     entity attached to a sublevel (e.g. sitting in a control seat), or is being
     *     tracked by one for relative motion;</li>
     *     <li>{@code getContaining(Entity)} — the player's position is inside an active
     *     sublevel's chunk footprint (standing on a moving structure without seat).</li>
     * </ol>
     *
     * <p>Both helpers return {@code SubLevelAccess}; on the client they always wrap a
     * {@code ClientSubLevelAccess} (which adds {@code renderPose(partialTick)}).</p>
     */
    private static ClientSubLevelAccess tryResolveSubLevel(LocalPlayer player) {
        try {
            SubLevelAccess access = Sable.HELPER.getTrackingOrVehicleSubLevel(player);
            if (access == null) {
                access = Sable.HELPER.getContaining(player);
            }
            if (access instanceof ClientSubLevelAccess client) {
                return client;
            }
        } catch (Throwable t) {
            // Defensive: never let a broken sublevel resolution crash the render loop.
            CreateMechanizedMod.LOGGER.debug("Pilot HUD: sublevel resolution failed", t);
        }
        return null;
    }
}
