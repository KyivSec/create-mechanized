package kyivsec.createmechanized.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Locale;

public final class HudRenderer {

    public record Palette(int primary, int dim, int faint) {
        public static Palette fromRgb(int rgb) {
            int rgbOnly = rgb & 0xFFFFFF;
            return new Palette(0xFF000000 | rgbOnly, 0xCC000000 | rgbOnly, 0x99000000 | rgbOnly);
        }
    }

    private record Rung(float rungRad, int halfLength, boolean labelled, String label, boolean dashed, int tickSign) {
    }

    private static final float PIXELS_PER_RAD = 200.0f;
    private static final int PITCH_BAND_HALF = 95;
    private static final int HDG_TAPE_PX_PER_DEG = 4;
    private static final int HDG_TAPE_HALF_WIDTH = 60;
    private static final int BANK_ARC_RADIUS = 78;
    private static final float BANK_ARC_HALF = (float) Math.toRadians(60.0);
    private static final int FPM_RADIUS = 6;

    private static final int[] BANK_MAJOR_DEG = {-60, -45, -30, -20, -10, 0, 10, 20, 30, 45, 60};

    private static final Rung[] LADDER_RUNGS = buildLadderRungs();

    private static Rung[] buildLadderRungs() {
        Rung[] arr = new Rung[1 + 18 * 2];
        arr[0] = new Rung(0.0f, 90, true, "0", false, 1);
        int idx = 1;
        for (int deg = 5; deg <= 90; deg += 5) {
            float theta = (float) Math.toRadians(deg);
            boolean labelled = (deg % 10 == 0);
            int halfLength = labelled ? 55 : 28;
            String label = labelled ? Integer.toString(deg) : "";
            arr[idx++] = new Rung(theta,  halfLength, labelled, label, false, 1);
            arr[idx++] = new Rung(-theta, halfLength, labelled, label, true,  -1);
        }
        return arr;
    }

    private HudRenderer() {
    }

    public static void draw(GuiGraphics graphics, FlightData data, int colorRgb) {
        Palette p = Palette.fromRgb(colorRgb);
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        int cx = w / 2;
        int cy = h / 2;
        Font font = Minecraft.getInstance().font;

        drawBoresight(graphics, cx, cy, p);
        drawAltitudeBox(graphics, font, w, cy, data.altitude(), p);

        if (!data.minimal()) {
            drawSpeedBox(graphics, font, cy, data.speedMs(), p);
            drawPitchLadder(graphics, font, cx, cy, data.pitchRad(), data.rollRad(), p);
            drawBankScale(graphics, cx, cy, data.rollRad(), p);
            drawHeadingTape(graphics, font, cx, data.headingRad(), p);
            drawFlightPathMarker(graphics, cx, cy, data, p);
        }
    }

    private static void drawBoresight(GuiGraphics g, int cx, int cy, Palette p) {
        g.fill(cx - 14, cy, cx - 6, cy + 1, p.primary());
        g.fill(cx - 6, cy, cx - 5, cy + 4, p.primary());
        g.fill(cx + 6, cy, cx + 14, cy + 1, p.primary());
        g.fill(cx + 5, cy, cx + 6, cy + 4, p.primary());
        g.fill(cx - 1, cy, cx + 1, cy + 1, p.primary());
    }

    private static void drawAltitudeBox(GuiGraphics g, Font font, int screenWidth, int cy, double altitude, Palette p) {
        String text = String.format(Locale.ROOT, "%5.0f", altitude);
        String label = "ALT M";
        int boxW = Math.max(font.width(text), font.width(label)) + 12;
        int boxH = 22;
        int x = screenWidth - boxW - 16;
        int y = cy - boxH / 2;
        drawBracket(g, x, y, boxW, boxH, true, p);
        g.drawString(font, text, x + 6, y + 3, p.primary(), false);
        g.drawString(font, label, x + 6, y + 12, p.dim(), false);
    }

    private static void drawSpeedBox(GuiGraphics g, Font font, int cy, double ms, Palette p) {
        String text = String.format(Locale.ROOT, "%5.1f", ms);
        String label = "M/S";
        int boxW = Math.max(font.width(text), font.width(label)) + 12;
        int boxH = 22;
        int x = 16;
        int y = cy - boxH / 2;
        drawBracket(g, x, y, boxW, boxH, false, p);
        g.drawString(font, text, x + 6, y + 3, p.primary(), false);
        g.drawString(font, label, x + 6, y + 12, p.dim(), false);
    }

    private static void drawBracket(GuiGraphics g, int x, int y, int w, int h, boolean rightFacing, Palette p) {
        g.fill(x, y, x + w, y + 1, p.primary());
        g.fill(x, y + h - 1, x + w, y + h, p.primary());
        if (rightFacing) {
            g.fill(x + w - 1, y, x + w, y + h, p.primary());
            g.fill(x, y, x + 1, y + 4, p.primary());
            g.fill(x, y + h - 4, x + 1, y + h, p.primary());
        } else {
            g.fill(x, y, x + 1, y + h, p.primary());
            g.fill(x + w - 1, y, x + w, y + 4, p.primary());
            g.fill(x + w - 1, y + h - 4, x + w, y + h, p.primary());
        }
    }

    private static void drawPitchLadder(GuiGraphics g, Font font, int cx, int cy, float pitchRad, float rollRad, Palette p) {
        float cos = Mth.cos(-rollRad);
        float sin = Mth.sin(-rollRad);
        for (Rung r : LADDER_RUNGS) {
            drawLadderRung(g, font, cx, cy, pitchRad, cos, sin, r, p);
        }
    }

    private static void drawLadderRung(GuiGraphics g, Font font,
                                       int cx, int cy, float pitchRad,
                                       float cos, float sin, Rung r, Palette p) {
        float vOff = (r.rungRad() - pitchRad) * PIXELS_PER_RAD;

        float fade = 1.0f - Mth.clamp(Math.abs(vOff) / (float) PITCH_BAND_HALF, 0.0f, 1.0f);
        if (fade <= 0.02f) return;
        int color = applyAlpha(p.primary(), fade);

        int gap = 14;
        int halfLength = r.halfLength();
        if (r.dashed()) {
            drawDashedSegmentRotated(g, cx, cy, -halfLength, vOff, -gap, vOff, cos, sin, 4, 4, color);
            drawDashedSegmentRotated(g, cx, cy, gap, vOff, halfLength, vOff, cos, sin, 4, 4, color);
        } else {
            drawSegmentRotated(g, cx, cy, -halfLength, vOff, -gap, vOff, cos, sin, color);
            drawSegmentRotated(g, cx, cy, gap, vOff, halfLength, vOff, cos, sin, color);
        }

        int tickSign = r.tickSign();
        drawSegmentRotated(g, cx, cy, -halfLength, vOff, -halfLength, vOff + 4 * tickSign, cos, sin, color);
        drawSegmentRotated(g, cx, cy, halfLength, vOff, halfLength, vOff + 4 * tickSign, cos, sin, color);

        if (r.labelled()) {
            int lw = font.width(r.label());
            int[] leftAnchor = rotate(cx, cy, -halfLength - lw - 3, vOff - 3, cos, sin);
            int[] rightAnchor = rotate(cx, cy, halfLength + 3, vOff - 3, cos, sin);
            g.drawString(font, r.label(), leftAnchor[0], leftAnchor[1], color, false);
            g.drawString(font, r.label(), rightAnchor[0], rightAnchor[1], color, false);
        }
    }

    private static void drawBankScale(GuiGraphics g, int cx, int cy, float rollRad, Palette p) {
        for (float a = -BANK_ARC_HALF; a <= BANK_ARC_HALF; a += (float) Math.toRadians(2.0)) {
            int px = cx + Math.round(Mth.sin(a) * BANK_ARC_RADIUS);
            int py = cy - Math.round(Mth.cos(a) * BANK_ARC_RADIUS);
            g.fill(px, py, px + 1, py + 1, p.faint());
        }

        for (int deg : BANK_MAJOR_DEG) {
            float a = (float) Math.toRadians(deg);
            int outer = BANK_ARC_RADIUS + (deg == 0 ? 8 : (Math.abs(deg) >= 30 ? 6 : 4));
            int x1 = cx + Math.round(Mth.sin(a) * BANK_ARC_RADIUS);
            int y1 = cy - Math.round(Mth.cos(a) * BANK_ARC_RADIUS);
            int x2 = cx + Math.round(Mth.sin(a) * outer);
            int y2 = cy - Math.round(Mth.cos(a) * outer);
            drawLineThin(g, x1, y1, x2, y2, p.primary());
        }

        float ptr = Mth.clamp(rollRad, -BANK_ARC_HALF, BANK_ARC_HALF);
        float pSin = Mth.sin(ptr);
        float pCos = Mth.cos(ptr);
        int tipR = BANK_ARC_RADIUS - 3;
        int baseR = BANK_ARC_RADIUS - 10;
        int tipX = cx + Math.round(pSin * tipR);
        int tipY = cy - Math.round(pCos * tipR);
        int base1X = cx + Math.round((pSin * baseR) - (pCos * 4));
        int base1Y = cy - Math.round((pCos * baseR) + (pSin * 4));
        int base2X = cx + Math.round((pSin * baseR) + (pCos * 4));
        int base2Y = cy - Math.round((pCos * baseR) - (pSin * 4));
        fillTriangle(g, tipX, tipY, base1X, base1Y, base2X, base2Y, p.primary());
    }

    private static void drawHeadingTape(GuiGraphics g, Font font, int cx, float headingRad, Palette p) {
        int tapeY = 21;
        int tickBaseY = tapeY + 4;
        float headingDeg = (float) Math.toDegrees(headingRad);

        g.fill(cx - HDG_TAPE_HALF_WIDTH - 1, tapeY, cx + HDG_TAPE_HALF_WIDTH + 1, tapeY + 1, p.faint());

        int minDeg = (int) Math.floor(headingDeg - (HDG_TAPE_HALF_WIDTH / (float) HDG_TAPE_PX_PER_DEG));
        int maxDeg = (int) Math.ceil(headingDeg + (HDG_TAPE_HALF_WIDTH / (float) HDG_TAPE_PX_PER_DEG));
        for (int d = (minDeg / 5) * 5; d <= maxDeg; d += 5) {
            int normalised = ((d % 360) + 360) % 360;
            int x = cx + Math.round((d - headingDeg) * HDG_TAPE_PX_PER_DEG);
            if (x < cx - HDG_TAPE_HALF_WIDTH || x > cx + HDG_TAPE_HALF_WIDTH) continue;
            boolean major = (normalised % 30 == 0);
            int tickH = major ? 6 : 3;
            g.fill(x, tickBaseY, x + 1, tickBaseY + tickH, p.primary());
            if (major) {
                String label = compassLabel(normalised);
                int lw = font.width(label);
                g.drawString(font, label, x - lw / 2, tickBaseY + tickH + 1, p.dim(), false);
            }
        }

        fillTriangle(g, cx, tapeY - 1, cx - 4, tapeY - 7, cx + 4, tapeY - 7, p.primary());

        String hdg = String.format(Locale.ROOT, "%03d", ((int) Math.round(headingDeg) % 360 + 360) % 360);
        int hw = font.width(hdg);
        int boxX = cx - hw / 2 - 4;
        int boxY = tapeY - 18;
        int boxW = hw + 8;
        int boxH = 12;
        g.fill(boxX, boxY, boxX + boxW, boxY + 1, p.primary());
        g.fill(boxX, boxY + boxH - 1, boxX + boxW, boxY + boxH, p.primary());
        g.fill(boxX, boxY, boxX + 1, boxY + boxH, p.primary());
        g.fill(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, p.primary());
        g.drawString(font, hdg, boxX + 4, boxY + 2, p.primary(), false);
    }

    private static String compassLabel(int deg) {
        return switch (deg) {
            case 0 -> "N";
            case 90 -> "E";
            case 180 -> "S";
            case 270 -> "W";
            default -> String.format(Locale.ROOT, "%02d", deg / 10);
        };
    }

    private static void drawFlightPathMarker(GuiGraphics g, int cx, int cy, FlightData d, Palette p) {
        double fwd = d.bodyVelZ();
        double speed = Math.sqrt(d.bodyVelX() * d.bodyVelX() + d.bodyVelY() * d.bodyVelY() + fwd * fwd);
        if (speed < 1.0e-4) return;

        double denom = Math.max(Math.abs(fwd), 0.05);
        double yawDrift = Math.atan2(d.bodyVelX(), denom);
        double pitchDrift = Math.atan2(d.bodyVelY(), denom);

        double maxRad = Math.toRadians(15.0);
        yawDrift = Mth.clamp(yawDrift, -maxRad, maxRad);
        pitchDrift = Mth.clamp(pitchDrift, -maxRad, maxRad);

        int fx = cx + (int) Math.round(yawDrift * PIXELS_PER_RAD);
        int fy = cy - (int) Math.round(pitchDrift * PIXELS_PER_RAD);

        drawHollowRect(g, fx - FPM_RADIUS, fy - FPM_RADIUS, FPM_RADIUS * 2 + 1, FPM_RADIUS * 2 + 1, p.primary());
        g.fill(fx - FPM_RADIUS - 8, fy, fx - FPM_RADIUS - 1, fy + 1, p.primary());
        g.fill(fx + FPM_RADIUS + 1, fy, fx + FPM_RADIUS + 8, fy + 1, p.primary());
        g.fill(fx, fy - FPM_RADIUS - 4, fx + 1, fy - FPM_RADIUS - 1, p.primary());
        g.fill(fx, fy, fx + 1, fy + 1, p.primary());
    }

    private static void drawHollowRect(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

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

    private static void fillTriangle(GuiGraphics g, int x1, int y1, int x2, int y2, int x3, int y3, int color) {
        if (y2 < y1) { int tx = x1, ty = y1; x1 = x2; y1 = y2; x2 = tx; y2 = ty; }
        if (y3 < y1) { int tx = x1, ty = y1; x1 = x3; y1 = y3; x3 = tx; y3 = ty; }
        if (y3 < y2) { int tx = x2, ty = y2; x2 = x3; y2 = y3; x3 = tx; y3 = ty; }
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

    private static void drawSegmentRotated(GuiGraphics g, int cx, int cy,
                                           float lx1, float ly1, float lx2, float ly2,
                                           float cos, float sin, int color) {
        int[] a = rotate(cx, cy, lx1, ly1, cos, sin);
        int[] b = rotate(cx, cy, lx2, ly2, cos, sin);
        drawLineThin(g, a[0], a[1], b[0], b[1], color);
    }

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

    private static int[] rotate(int cx, int cy, float lx, float ly, float cos, float sin) {
        int rx = cx + Math.round(lx * cos - ly * sin);
        int ry = cy + Math.round(lx * sin + ly * cos);
        return new int[]{rx, ry};
    }

    private static int applyAlpha(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int scaled = Mth.clamp(Math.round(a * factor), 0, 255);
        return (scaled << 24) | (argb & 0x00FFFFFF);
    }

    public static void drawProgressBar(GuiGraphics g, int x, int y, int w, int h, float fraction, Palette p) {
        fraction = Mth.clamp(fraction, 0.0f, 1.0f);
        drawHollowRect(g, x, y, w, h, p.dim());
        int innerW = Math.max(0, w - 2);
        int filledW = Math.round(fraction * innerW);
        if (filledW > 0 && h > 2) {
            g.fill(x + 1, y + 1, x + 1 + filledW, y + h - 1, p.primary());
        }
    }
}
