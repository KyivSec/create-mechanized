package kyivsec.createmechanized.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Locale;

/**
 * F-22 / fighter-jet style helmet-mounted display (HMD).
 *
 * <p>All elements are 2D screen-space primitives drawn with {@link GuiGraphics#fill}
 * and {@link GuiGraphics#drawString} — no textures.</p>
 *
 * <p>Widgets, when on a sublevel:</p>
 * <ul>
 *     <li>Boresight cross ({@code -W-}) — fixed reference for the aircraft nose axis.</li>
 *     <li>Flight Path Marker (FPM) — small ringed cross drifting to where the velocity vector points.</li>
 *     <li>Pitch ladder — rungs every 5°, labels every 10°, solid above horizon, dashed below.</li>
 *     <li>Bank scale — fixed arc with tick marks; a triangle pointer rotates with bank.</li>
 *     <li>Heading tape — horizontal compass strip at top, current heading boxed in centre.</li>
 *     <li>Speed box — left side, boxed KM/H readout.</li>
 *     <li>Altitude box — right side, boxed M readout.</li>
 * </ul>
 * <p>When NOT on a sublevel, only the boresight + altitude box render.</p>
 */
public final class HudRenderer {

    /* ------------ Palette ----------------------------------------------- */
    /**
     * Three-tier palette derived from a single {@code 0xRRGGBB} tint set by
     * {@link #setPalette(int)} at the start of each {@link #draw} call. Non-final
     * because the helmet can be dyed at runtime; rendering is single-threaded so
     * mutation is safe.
     */
    private static int COLOR_HUD = 0xFF000000 | kyivsec.createmechanized.PilotHelmetColor.DEFAULT_RGB;
    private static int COLOR_HUD_DIM = 0xCC000000 | kyivsec.createmechanized.PilotHelmetColor.DEFAULT_RGB;
    private static int COLOR_HUD_FAINT = 0x99000000 | kyivsec.createmechanized.PilotHelmetColor.DEFAULT_RGB;

    /** Updates the three palette tiers from a single 0xRRGGBB color. */
    private static void setPalette(int rgb) {
        int rgbOnly = rgb & 0xFFFFFF;
        COLOR_HUD = 0xFF000000 | rgbOnly;
        COLOR_HUD_DIM = 0xCC000000 | rgbOnly;
        COLOR_HUD_FAINT = 0x99000000 | rgbOnly;
    }

    /* ------------ Geometry constants ------------------------------------ */
    /** Pixels per radian of pitch. ~3.5 px/° puts ladder rungs at sensible spacing. */
    private static final float PIXELS_PER_RAD = 200.0f;
    /** Half-width of the visible pitch band before fading. */
    private static final int PITCH_BAND_HALF = 95;
    /** Pixel offset per degree on the heading tape. */
    private static final int HDG_TAPE_PX_PER_DEG = 4;
    /** Half-width of the heading tape window. */
    private static final int HDG_TAPE_HALF_WIDTH = 60;
    /** Radius of the bank arc. */
    private static final int BANK_ARC_RADIUS = 78;
    /** Half-span of the bank arc, in radians (±60°). */
    private static final float BANK_ARC_HALF = (float) Math.toRadians(60.0);
    /** FPM circle radius. */
    private static final int FPM_RADIUS = 6;

    private HudRenderer() {
    }

    /* ==================================================================== */
    /*  Entry point                                                          */
    /* ==================================================================== */

    public static void draw(GuiGraphics graphics, FlightData data, int colorRgb) {
        setPalette(colorRgb);
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        int cx = w / 2;
        int cy = h / 2;
        Font font = Minecraft.getInstance().font;

        // Always-on elements.
        drawBoresight(graphics, cx, cy);
        drawAltitudeBox(graphics, font, w, cy, data.altitude());

        if (!data.minimal()) {
            drawSpeedBox(graphics, font, cy, data.speedMs());
            drawPitchLadder(graphics, font, cx, cy, data.pitchRad(), data.rollRad());
            drawBankScale(graphics, cx, cy, data.rollRad());
            drawHeadingTape(graphics, font, cx, data.headingRad());
            drawFlightPathMarker(graphics, cx, cy, data);
        }
    }

    /* ==================================================================== */
    /*  Boresight (waterline / aircraft datum)                               */
    /* ==================================================================== */

    /**
     * Draws the fixed "-W-" boresight: two short bars flanking a downward notch,
     * marking the aircraft's nose axis. Always at screen centre.
     */
    private static void drawBoresight(GuiGraphics g, int cx, int cy) {
        // Left bar
        g.fill(cx - 14, cy, cx - 6, cy + 1, COLOR_HUD);
        g.fill(cx - 6, cy, cx - 5, cy + 4, COLOR_HUD);
        // Right bar
        g.fill(cx + 6, cy, cx + 14, cy + 1, COLOR_HUD);
        g.fill(cx + 5, cy, cx + 6, cy + 4, COLOR_HUD);
        // Centre dot
        g.fill(cx - 1, cy, cx + 1, cy + 1, COLOR_HUD);
    }

    /* ==================================================================== */
    /*  Boxed numeric readouts (speed / altitude)                            */
    /* ==================================================================== */

    private static void drawAltitudeBox(GuiGraphics g, Font font, int screenWidth, int cy, double altitude) {
        String text = String.format(Locale.ROOT, "%5.0f", altitude);
        String label = "ALT M";
        int boxW = Math.max(font.width(text), font.width(label)) + 12;
        int boxH = 22;
        int x = screenWidth - boxW - 16;
        int y = cy - boxH / 2;
        drawBracket(g, x, y, boxW, boxH, true /* right-facing */);
        g.drawString(font, text, x + 6, y + 3, COLOR_HUD, false);
        g.drawString(font, label, x + 6, y + 12, COLOR_HUD_DIM, false);
    }

    private static void drawSpeedBox(GuiGraphics g, Font font, int cy, double ms) {
        String text = String.format(Locale.ROOT, "%5.1f", ms);
        String label = "M/S";
        int boxW = Math.max(font.width(text), font.width(label)) + 12;
        int boxH = 22;
        int x = 16;
        int y = cy - boxH / 2;
        drawBracket(g, x, y, boxW, boxH, false /* left-facing */);
        g.drawString(font, text, x + 6, y + 3, COLOR_HUD, false);
        g.drawString(font, label, x + 6, y + 12, COLOR_HUD_DIM, false);
    }

    /** Draws an open-sided bracket "[" or "]" outline around a numeric readout. */
    private static void drawBracket(GuiGraphics g, int x, int y, int w, int h, boolean rightFacing) {
        // Top + bottom edges (full width)
        g.fill(x, y, x + w, y + 1, COLOR_HUD);
        g.fill(x, y + h - 1, x + w, y + h, COLOR_HUD);
        // One vertical edge (the side facing centre of screen)
        if (rightFacing) {
            // bracket like "[" — vertical line on the right (toward screen edge), short stubs left
            g.fill(x + w - 1, y, x + w, y + h, COLOR_HUD);
            g.fill(x, y, x + 1, y + 4, COLOR_HUD);
            g.fill(x, y + h - 4, x + 1, y + h, COLOR_HUD);
        } else {
            // bracket like "]" — vertical line on the left (toward screen edge), short stubs right
            g.fill(x, y, x + 1, y + h, COLOR_HUD);
            g.fill(x + w - 1, y, x + w, y + 4, COLOR_HUD);
            g.fill(x + w - 1, y + h - 4, x + w, y + h, COLOR_HUD);
        }
    }

    /* ==================================================================== */
    /*  Pitch ladder                                                         */
    /* ==================================================================== */

    /**
     * Draws the pitch ladder centred on (cx, cy). Rungs rotate with roll so they
     * always appear parallel to the visible horizon.
     */
    private static void drawPitchLadder(GuiGraphics g, Font font, int cx, int cy, float pitchRad, float rollRad) {
        // 0° horizon line (long).
        drawLadderRung(g, font, cx, cy, pitchRad, rollRad, 0.0f, 90, true, false);
        // Rungs every 5° from -90° to +90°.
        for (int deg = 5; deg <= 90; deg += 5) {
            float thetaPos = (float) Math.toRadians(deg);
            float thetaNeg = -thetaPos;
            boolean labelled = (deg % 10 == 0);
            int halfLength = labelled ? 55 : 28;
            drawLadderRung(g, font, cx, cy, pitchRad, rollRad, thetaPos, halfLength, labelled, false);
            drawLadderRung(g, font, cx, cy, pitchRad, rollRad, thetaNeg, halfLength, labelled, true);
        }
    }

    /**
     * Draws a single pitch ladder rung at rung-pitch {@code rungRad}, given current
     * pitch {@code pitchRad} and roll {@code rollRad}. Rung above horizon = solid;
     * below horizon ({@code dashed=true}) = dashed segments.
     */
    private static void drawLadderRung(GuiGraphics g, Font font,
                                       int cx, int cy, float pitchRad, float rollRad,
                                       float rungRad, int halfLength,
                                       boolean labelled, boolean dashed) {
        // On-screen vertical offset before rotation.
        float vOff = (rungRad - pitchRad) * PIXELS_PER_RAD;

        // Alpha fade as the rung leaves the visible band.
        float fade = 1.0f - Mth.clamp(Math.abs(vOff) / (float) PITCH_BAND_HALF, 0.0f, 1.0f);
        if (fade <= 0.02f) return;
        int color = applyAlpha(COLOR_HUD, fade);

        // Pre-compute roll rotation (rungs rotate around (cx, cy) with -rollRad so that
        // a right-wing-down roll tilts the entire ladder clockwise).
        float cos = Mth.cos(-rollRad);
        float sin = Mth.sin(-rollRad);

        // The rung is a horizontal line segment of half-length `halfLength`, with an
        // inner gap so it doesn't overlap the boresight. Drawn as two segments.
        int gap = 14;
        if (dashed) {
            // Dashed: alternating 4-px on / 4-px off segments.
            drawDashedSegmentRotated(g, cx, cy, -halfLength, vOff, -gap, vOff, cos, sin, 4, 4, color);
            drawDashedSegmentRotated(g, cx, cy, gap, vOff, halfLength, vOff, cos, sin, 4, 4, color);
        } else {
            drawSegmentRotated(g, cx, cy, -halfLength, vOff, -gap, vOff, cos, sin, color);
            drawSegmentRotated(g, cx, cy, gap, vOff, halfLength, vOff, cos, sin, color);
        }

        // Small tick at each end pointing toward the horizon (down for positive, up for negative).
        // The tick is 4 pixels long, oriented perpendicular to the rung.
        int tickSign = (rungRad >= 0.0f) ? 1 : -1;
        drawSegmentRotated(g, cx, cy, -halfLength, vOff, -halfLength, vOff + 4 * tickSign, cos, sin, color);
        drawSegmentRotated(g, cx, cy, halfLength, vOff, halfLength, vOff + 4 * tickSign, cos, sin, color);

        if (labelled) {
            int deg = Math.round((float) Math.toDegrees(rungRad));
            String label = String.valueOf(Math.abs(deg));
            int textColor = applyAlpha(COLOR_HUD, fade);
            int lw = font.width(label);
            // Position labels just outside each end of the rung. We use the rotated
            // anchor for placement.
            int[] leftAnchor = rotate(cx, cy, -halfLength - lw - 3, vOff - 3, cos, sin);
            int[] rightAnchor = rotate(cx, cy, halfLength + 3, vOff - 3, cos, sin);
            g.drawString(font, label, leftAnchor[0], leftAnchor[1], textColor, false);
            g.drawString(font, label, rightAnchor[0], rightAnchor[1], textColor, false);
        }
    }

    /* ==================================================================== */
    /*  Bank scale                                                           */
    /* ==================================================================== */

    /**
     * Draws an arc at the top of the boresight with tick marks at fixed bank angles
     * and a downward-pointing triangle pointer at the current roll.
     */
    private static void drawBankScale(GuiGraphics g, int cx, int cy, float rollRad) {
        // Dotted arc (every 2°).
        for (float a = -BANK_ARC_HALF; a <= BANK_ARC_HALF; a += (float) Math.toRadians(2.0)) {
            int px = cx + Math.round(Mth.sin(a) * BANK_ARC_RADIUS);
            int py = cy - Math.round(Mth.cos(a) * BANK_ARC_RADIUS);
            g.fill(px, py, px + 1, py + 1, COLOR_HUD_FAINT);
        }

        // Major ticks: 0°, ±10°, ±20°, ±30°, ±45°, ±60°.
        int[] majorDeg = {-60, -45, -30, -20, -10, 0, 10, 20, 30, 45, 60};
        for (int deg : majorDeg) {
            float a = (float) Math.toRadians(deg);
            int outer = BANK_ARC_RADIUS + (deg == 0 ? 8 : (Math.abs(deg) >= 30 ? 6 : 4));
            int x1 = cx + Math.round(Mth.sin(a) * BANK_ARC_RADIUS);
            int y1 = cy - Math.round(Mth.cos(a) * BANK_ARC_RADIUS);
            int x2 = cx + Math.round(Mth.sin(a) * outer);
            int y2 = cy - Math.round(Mth.cos(a) * outer);
            drawLineThin(g, x1, y1, x2, y2, COLOR_HUD);
        }

        // Pointer: solid triangle just below the arc, rotating with bank.
        float ptr = Mth.clamp(rollRad, -BANK_ARC_HALF, BANK_ARC_HALF);
        float pSin = Mth.sin(ptr);
        float pCos = Mth.cos(ptr);
        // Pointer is anchored at radius (BANK_ARC_RADIUS - 2), tip pointing inward.
        int tipR = BANK_ARC_RADIUS - 3;
        int baseR = BANK_ARC_RADIUS - 10;
        int tipX = cx + Math.round(pSin * tipR);
        int tipY = cy - Math.round(pCos * tipR);
        int base1X = cx + Math.round((pSin * baseR) - (pCos * 4));
        int base1Y = cy - Math.round((pCos * baseR) + (pSin * 4));
        int base2X = cx + Math.round((pSin * baseR) + (pCos * 4));
        int base2Y = cy - Math.round((pCos * baseR) - (pSin * 4));
        fillTriangle(g, tipX, tipY, base1X, base1Y, base2X, base2Y, COLOR_HUD);
    }

    /* ==================================================================== */
    /*  Heading tape                                                         */
    /* ==================================================================== */

    /**
     * Horizontal compass strip at the very top of the screen. Tick marks every 5°
     * and labels every 30° slide left/right under a fixed downward caret in the
     * centre that shows current heading numerically.
     */
    private static void drawHeadingTape(GuiGraphics g, Font font, int cx, float headingRad) {
        int tapeY = 21;
        int tickBaseY = tapeY + 4;
        float headingDeg = (float) Math.toDegrees(headingRad);

        // Bar enclosing the tape window.
        g.fill(cx - HDG_TAPE_HALF_WIDTH - 1, tapeY, cx + HDG_TAPE_HALF_WIDTH + 1, tapeY + 1, COLOR_HUD_FAINT);

        // Iterate 5° ticks across the visible window.
        int minDeg = (int) Math.floor(headingDeg - (HDG_TAPE_HALF_WIDTH / (float) HDG_TAPE_PX_PER_DEG));
        int maxDeg = (int) Math.ceil(headingDeg + (HDG_TAPE_HALF_WIDTH / (float) HDG_TAPE_PX_PER_DEG));
        for (int d = (minDeg / 5) * 5; d <= maxDeg; d += 5) {
            int normalised = ((d % 360) + 360) % 360;
            int x = cx + Math.round((d - headingDeg) * HDG_TAPE_PX_PER_DEG);
            if (x < cx - HDG_TAPE_HALF_WIDTH || x > cx + HDG_TAPE_HALF_WIDTH) continue;
            boolean major = (normalised % 30 == 0);
            int tickH = major ? 6 : 3;
            g.fill(x, tickBaseY, x + 1, tickBaseY + tickH, COLOR_HUD);
            if (major) {
                String label = compassLabel(normalised);
                int lw = font.width(label);
                g.drawString(font, label, x - lw / 2, tickBaseY + tickH + 1, COLOR_HUD_DIM, false);
            }
        }

        // Centre caret (downward triangle just above the tape).
        fillTriangle(g, cx, tapeY - 1, cx - 4, tapeY - 7, cx + 4, tapeY - 7, COLOR_HUD);

        // Boxed current heading numeric just below the caret, centred.
        String hdg = String.format(Locale.ROOT, "%03d", ((int) Math.round(headingDeg) % 360 + 360) % 360);
        int hw = font.width(hdg);
        int boxX = cx - hw / 2 - 4;
        int boxY = tapeY - 18;
        int boxW = hw + 8;
        int boxH = 12;
        // box outline
        g.fill(boxX, boxY, boxX + boxW, boxY + 1, COLOR_HUD);
        g.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, COLOR_HUD);
        g.fill(boxX, boxY, boxX + 1, boxY + boxH, COLOR_HUD);
        g.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, COLOR_HUD);
        g.drawString(font, hdg, boxX + 4, boxY + 2, COLOR_HUD, false);
    }

    /** N/E/S/W lettering at cardinal points, otherwise zero-padded degrees-over-10. */
    private static String compassLabel(int deg) {
        return switch (deg) {
            case 0 -> "N";
            case 90 -> "E";
            case 180 -> "S";
            case 270 -> "W";
            default -> String.format(Locale.ROOT, "%02d", deg / 10);
        };
    }

    /* ==================================================================== */
    /*  Flight Path Marker (FPM)                                             */
    /* ==================================================================== */

    /**
     * The FPM is a small ringed cross with stubby wings that drifts to where the
     * aircraft is actually going (velocity vector projected onto the HUD plane).
     */
    private static void drawFlightPathMarker(GuiGraphics g, int cx, int cy, FlightData d) {
        double fwd = d.bodyVelZ();
        // Forward axis is -Z in body frame, so "going forward" means bodyVelZ < 0.
        double speed = Math.sqrt(d.bodyVelX() * d.bodyVelX() + d.bodyVelY() * d.bodyVelY() + fwd * fwd);
        if (speed < 1.0e-4) return;

        // FPM horizontal angle (drift). Negative bodyVelX means sliding left.
        // We use forward magnitude as the projection denominator so when nearly stationary
        // the FPM doesn't fly off-screen.
        double denom = Math.max(Math.abs(fwd), 0.05);
        double yawDrift = Math.atan2(d.bodyVelX(), denom);
        double pitchDrift = Math.atan2(d.bodyVelY(), denom);

        // Limit angular excursion so the marker stays on-screen.
        double maxRad = Math.toRadians(15.0);
        yawDrift = Mth.clamp(yawDrift, -maxRad, maxRad);
        pitchDrift = Mth.clamp(pitchDrift, -maxRad, maxRad);

        int fx = cx + (int) Math.round(yawDrift * PIXELS_PER_RAD);
        // Positive pitchDrift means climbing → smaller pixel-y.
        int fy = cy - (int) Math.round(pitchDrift * PIXELS_PER_RAD);

        // Centre ring (square approximation for crisp pixel look).
        drawHollowRect(g, fx - FPM_RADIUS, fy - FPM_RADIUS, FPM_RADIUS * 2 + 1, FPM_RADIUS * 2 + 1, COLOR_HUD);
        // Wings.
        g.fill(fx - FPM_RADIUS - 8, fy, fx - FPM_RADIUS - 1, fy + 1, COLOR_HUD);
        g.fill(fx + FPM_RADIUS + 1, fy, fx + FPM_RADIUS + 8, fy + 1, COLOR_HUD);
        // Tail.
        g.fill(fx, fy - FPM_RADIUS - 4, fx + 1, fy - FPM_RADIUS - 1, COLOR_HUD);
        // Centre dot.
        g.fill(fx, fy, fx + 1, fy + 1, COLOR_HUD);
    }

    /* ==================================================================== */
    /*  Drawing primitives                                                   */
    /* ==================================================================== */

    /** Hollow rectangle outline. */
    private static void drawHollowRect(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    /** Bresenham-style 1px line. */
    private static void drawLineThin(GuiGraphics g, int x1, int y1, int x2, int y2, int argb) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1, y = y1;
        while (true) {
            g.fill(x, y, x + 1, y + 1, argb);
            if (x == x2 && y == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx)  { err += dx; y += sy; }
        }
    }

    /**
     * Filled triangle via flat-bottom / flat-top decomposition. Order of points
     * doesn't matter — the routine sorts internally.
     */
    private static void fillTriangle(GuiGraphics g, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        // Sort by Y.
        if (y2 < y1) { int tx = x1, ty = y1; x1 = x2; y1 = y2; x2 = tx; y2 = ty; }
        if (y3 < y1) { int tx = x1, ty = y1; x1 = x3; y1 = y3; x3 = tx; y3 = ty; }
        if (y3 < y2) { int tx = x2, ty = y2; x2 = x3; y2 = y3; x3 = tx; y3 = ty; }
        // Edge interpolators.
        for (int y = y1; y <= y3; y++) {
            float t13 = (y3 == y1) ? 0.0f : (y - y1) / (float) (y3 - y1);
            int xA = Math.round(x1 + (x3 - x1) * t13);
            int xB;
            if (y < y2) {
                float t12 = (y2 == y1) ? 0.0f : (y - y1) / (float) (y2 - y1);
                xB = Math.round(x1 + (x2 - x1) * t12);
            } else {
                float t23 = (y3 == y2) ? 0.0f : (y - y2) / (float) (y3 - y2);
                xB = Math.round(x2 + (x3 - x2) * t23);
            }
            int xLo = Math.min(xA, xB);
            int xHi = Math.max(xA, xB);
            g.fill(xLo, y, xHi + 1, y + 1, color);
        }
    }

    /**
     * Draws a rotated line segment from local point (lx1, ly1) to (lx2, ly2) where
     * "local" means relative to the rotation pivot (cx, cy). Used by the pitch
     * ladder so rungs rotate with roll.
     */
    private static void drawSegmentRotated(GuiGraphics g, int cx, int cy,
                                           float lx1, float ly1, float lx2, float ly2,
                                           float cos, float sin, int color) {
        int[] a = rotate(cx, cy, lx1, ly1, cos, sin);
        int[] b = rotate(cx, cy, lx2, ly2, cos, sin);
        drawLineThin(g, a[0], a[1], b[0], b[1], color);
    }

    /**
     * Like {@link #drawSegmentRotated} but rendered as a dashed line with given
     * on/off pixel lengths. Used for the below-horizon pitch ladder rungs.
     */
    private static void drawDashedSegmentRotated(GuiGraphics g, int cx, int cy,
                                                 float lx1, float ly1, float lx2, float ly2,
                                                 float cos, float sin, int onPx, int offPx, int color) {
        float dx = lx2 - lx1;
        float dy = ly2 - ly1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1.0f) return;
        float nx = dx / len;
        float ny = dy / len;
        int period = onPx + offPx;
        int steps = (int) Math.ceil(len / period);
        for (int i = 0; i < steps; i++) {
            float t0 = i * period;
            float t1 = Math.min(t0 + onPx, len);
            drawSegmentRotated(g, cx, cy,
                    lx1 + nx * t0, ly1 + ny * t0,
                    lx1 + nx * t1, ly1 + ny * t1,
                    cos, sin, color);
        }
    }

    /** Rotates a local point by (cos, sin) around pivot (cx, cy) and returns pixel coords. */
    private static int[] rotate(int cx, int cy, float lx, float ly, float cos, float sin) {
        int rx = cx + Math.round(lx * cos - ly * sin);
        int ry = cy + Math.round(lx * sin + ly * cos);
        return new int[]{rx, ry};
    }

    /** Multiplies the alpha channel of {@code argb} by {@code factor} (0..1). */
    private static int applyAlpha(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int scaled = Mth.clamp(Math.round(a * factor), 0, 255);
        return (scaled << 24) | (argb & 0x00FFFFFF);
    }
}
