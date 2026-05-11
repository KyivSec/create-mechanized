# HudRenderer

`kyivsec.createmechanized.client.hud.HudRenderer`

Final class, package-private state, no instances.

## Static state

```java
private static int COLOR_HUD       = 0xFF000000 | PilotHelmetColor.DEFAULT_RGB;
private static int COLOR_HUD_DIM   = 0xCC000000 | PilotHelmetColor.DEFAULT_RGB;
private static int COLOR_HUD_FAINT = 0x99000000 | PilotHelmetColor.DEFAULT_RGB;
```

Three ARGB tiers derived from one `0xRRGGBB` tint. Mutated per-frame by `setPalette`. Safe because all calls into this class come from the render thread.

```java
private static void setPalette(int rgb)
```
Strips alpha from `rgb`, OR-s in `0xFF/0xCC/0x99` for the three tiers, assigns to the static fields.

## Geometry constants

| Constant | Value | Used by |
|---|---|---|
| `PIXELS_PER_RAD` | `200.0f` | `drawLadderRung` (`vOff = (rungRad - pitchRad) * PIXELS_PER_RAD`), `drawFlightPathMarker` |
| `PITCH_BAND_HALF` | `95` | `drawLadderRung` fade range |
| `HDG_TAPE_PX_PER_DEG` | `4` | `drawHeadingTape` tick spacing |
| `HDG_TAPE_HALF_WIDTH` | `60` | `drawHeadingTape` window half-width |
| `BANK_ARC_RADIUS` | `78` | `drawBankScale` |
| `BANK_ARC_HALF` | `toRadians(60)` | `drawBankScale` clamp + arc span |
| `FPM_RADIUS` | `6` | `drawFlightPathMarker` ring half-size |

## Entry point

```java
public static void draw(GuiGraphics graphics, FlightData data, int colorRgb)
```

1. `setPalette(colorRgb)`
2. Reads `graphics.guiWidth()`, `graphics.guiHeight()`, computes `cx = w / 2`, `cy = h / 2`
3. Fetches `Minecraft.getInstance().font`
4. Always calls `drawBoresight(g, cx, cy)` and `drawAltitudeBox(g, font, w, cy, data.altitude())`
5. If `!data.minimal()`, additionally calls:
   - `drawSpeedBox(g, font, cy, data.speedMs())`
   - `drawPitchLadder(g, font, cx, cy, data.pitchRad(), data.rollRad())`
   - `drawBankScale(g, cx, cy, data.rollRad())`
   - `drawHeadingTape(g, font, cx, data.headingRad())`
   - `drawFlightPathMarker(g, cx, cy, data)`

## Widget methods

### `drawBoresight(GuiGraphics, int cx, int cy)`

Five `g.fill` calls forming `-W-` centred on `(cx, cy)`:

| Rect (x1, y1) → (x2, y2) | Role |
|---|---|
| `(cx-14, cy)` → `(cx-6, cy+1)` | left horizontal bar |
| `(cx-6,  cy)` → `(cx-5, cy+4)` | left vertical stub (notch) |
| `(cx+6,  cy)` → `(cx+14, cy+1)` | right horizontal bar |
| `(cx+5,  cy)` → `(cx+6, cy+4)` | right vertical stub |
| `(cx-1,  cy)` → `(cx+1, cy+1)` | centre dot |

All in `COLOR_HUD`.

### `drawAltitudeBox(GuiGraphics, Font, int screenWidth, int cy, double altitude)`

- `text = String.format(Locale.ROOT, "%5.0f", altitude)`
- `label = "ALT M"`
- `boxW = max(font.width(text), font.width(label)) + 12`
- `boxH = 22`
- Position: `x = screenWidth - boxW - 16`, `y = cy - boxH/2`
- `drawBracket(g, x, y, boxW, boxH, true)` (right-facing `[` form, vertical on right)
- `drawString(font, text, x+6, y+3, COLOR_HUD, false)`
- `drawString(font, label, x+6, y+12, COLOR_HUD_DIM, false)`

### `drawSpeedBox(GuiGraphics, Font, int cy, double ms)`

- `text = String.format(Locale.ROOT, "%5.1f", ms)` — m/s with one decimal
- `label = "M/S"`
- Same box sizing as altitude (`boxW = max(textW, labelW) + 12`, `boxH = 22`)
- Position: `x = 16`, `y = cy - boxH/2`
- `drawBracket(g, x, y, boxW, boxH, false)` (left-facing `]` form)
- Same `drawString` placement as altitude

### `drawBracket(GuiGraphics g, int x, int y, int w, int h, boolean rightFacing)`

Outline of `[` or `]`:
- Top edge: `g.fill(x, y, x+w, y+1, COLOR_HUD)`
- Bottom edge: `g.fill(x, y+h-1, x+w, y+h, COLOR_HUD)`
- If `rightFacing`: full right vertical + 4-px stubs on left top/bottom
- Else: full left vertical + 4-px stubs on right top/bottom

### `drawPitchLadder(GuiGraphics, Font, int cx, int cy, float pitchRad, float rollRad)`

- One call for the horizon at `rungRad = 0`, `halfLength = 90`, `labelled = true`, `dashed = false`
- Loop `deg` from 5 to 90 step 5:
  - `thetaPos = toRadians(deg)`, `thetaNeg = -thetaPos`
  - `labelled = (deg % 10 == 0)`
  - `halfLength = labelled ? 55 : 28`
  - Positive rung: `dashed = false`
  - Negative rung: `dashed = true`

### `drawLadderRung(GuiGraphics g, Font font, int cx, int cy, float pitchRad, float rollRad, float rungRad, int halfLength, boolean labelled, boolean dashed)`

```
vOff = (rungRad - pitchRad) * PIXELS_PER_RAD
fade = 1 - clamp(|vOff| / PITCH_BAND_HALF, 0, 1)
if fade <= 0.02: return
color = applyAlpha(COLOR_HUD, fade)
cos, sin = cos(-rollRad), sin(-rollRad)
gap = 14
```

Two segments per rung with a `gap`-wide centre gap (so the rung doesn't overlap the boresight):
- `dashed`: two `drawDashedSegmentRotated(..., onPx=4, offPx=4)` calls
- `!dashed`: two `drawSegmentRotated(...)` calls

End ticks (always solid):
- `tickSign = (rungRad >= 0) ? 1 : -1`
- Vertical 4-px stub at both `-halfLength` and `+halfLength`, pointing toward horizon

If `labelled`:
- `deg = round(toDegrees(rungRad))`
- `label = String.valueOf(abs(deg))`
- `textColor = applyAlpha(COLOR_HUD, fade)`
- `leftAnchor = rotate(cx, cy, -halfLength - lw - 3, vOff - 3, cos, sin)`
- `rightAnchor = rotate(cx, cy, halfLength + 3, vOff - 3, cos, sin)`
- `drawString` at both anchors with `dropShadow = false`

### `drawBankScale(GuiGraphics g, int cx, int cy, float rollRad)`

Dot arc:
```
for a in [-BANK_ARC_HALF, +BANK_ARC_HALF] step toRadians(2):
    px = cx + round(sin(a) * BANK_ARC_RADIUS)
    py = cy - round(cos(a) * BANK_ARC_RADIUS)
    g.fill(px, py, px+1, py+1, COLOR_HUD_FAINT)
```

Major ticks at `{-60, -45, -30, -20, -10, 0, 10, 20, 30, 45, 60}` degrees:
- `outer = BANK_ARC_RADIUS + (deg == 0 ? 8 : (|deg| >= 30 ? 6 : 4))`
- `drawLineThin` from `BANK_ARC_RADIUS` to `outer` along angle `a`, in `COLOR_HUD`

Pointer (filled triangle, tip pointing inward at `BANK_ARC_RADIUS - 3`):
```
ptr   = clamp(rollRad, -BANK_ARC_HALF, BANK_ARC_HALF)
pSin  = sin(ptr), pCos = cos(ptr)
tipR  = BANK_ARC_RADIUS - 3
baseR = BANK_ARC_RADIUS - 10
tip   = (cx + round(pSin * tipR), cy - round(pCos * tipR))
base1 = (cx + round(pSin*baseR - pCos*4), cy - round(pCos*baseR + pSin*4))
base2 = (cx + round(pSin*baseR + pCos*4), cy - round(pCos*baseR - pSin*4))
fillTriangle(g, tip, base1, base2, COLOR_HUD)
```

### `drawHeadingTape(GuiGraphics g, Font font, int cx, float headingRad)`

```
tapeY      = 21
tickBaseY  = tapeY + 4
headingDeg = toDegrees(headingRad)
```

Horizontal underline of the tape window:
```java
g.fill(cx - HDG_TAPE_HALF_WIDTH - 1, tapeY,
       cx + HDG_TAPE_HALF_WIDTH + 1, tapeY + 1, COLOR_HUD_FAINT);
```

Tick loop:
```
minDeg = floor(headingDeg - HDG_TAPE_HALF_WIDTH / HDG_TAPE_PX_PER_DEG)
maxDeg = ceil (headingDeg + HDG_TAPE_HALF_WIDTH / HDG_TAPE_PX_PER_DEG)
for d in [(minDeg/5)*5 .. maxDeg] step 5:
    normalised = ((d % 360) + 360) % 360
    x = cx + round((d - headingDeg) * HDG_TAPE_PX_PER_DEG)
    if x < cx - HDG_TAPE_HALF_WIDTH or x > cx + HDG_TAPE_HALF_WIDTH: continue
    major = (normalised % 30 == 0)
    tickH = major ? 6 : 3
    g.fill(x, tickBaseY, x+1, tickBaseY+tickH, COLOR_HUD)
    if major:
        label = compassLabel(normalised)
        drawString centred horizontally on x, at y = tickBaseY + tickH + 1, COLOR_HUD_DIM
```

Centre caret (downward triangle just above the tape bar):
```java
fillTriangle(g, cx, tapeY - 1, cx - 4, tapeY - 7, cx + 4, tapeY - 7, COLOR_HUD);
```

Boxed numeric heading above the caret:
```
hdg = String.format("%03d", ((round(headingDeg) % 360) + 360) % 360)
hw  = font.width(hdg)
boxX = cx - hw/2 - 4
boxY = tapeY - 18
boxW = hw + 8
boxH = 12
```
Outline drawn as four `g.fill` rects (top, bottom, left, right). Text at `(boxX + 4, boxY + 2)` in `COLOR_HUD`.

### `compassLabel(int deg) → String`

Switch expression:
- `0  → "N"`
- `90 → "E"`
- `180 → "S"`
- `270 → "W"`
- default → `String.format(Locale.ROOT, "%02d", deg / 10)`

### `drawFlightPathMarker(GuiGraphics g, int cx, int cy, FlightData d)`

```
fwd   = d.bodyVelZ()
speed = sqrt(bodyVelX² + bodyVelY² + bodyVelZ²)
if speed < 1e-4: return
denom      = max(|fwd|, 0.05)
yawDrift   = atan2(bodyVelX, denom)
pitchDrift = atan2(bodyVelY, denom)
maxRad     = toRadians(15)
yawDrift   = clamp(yawDrift,   -maxRad, +maxRad)
pitchDrift = clamp(pitchDrift, -maxRad, +maxRad)
fx = cx + round(yawDrift   * PIXELS_PER_RAD)
fy = cy - round(pitchDrift * PIXELS_PER_RAD)
```

Composition at `(fx, fy)` in `COLOR_HUD`:
- `drawHollowRect(fx - FPM_RADIUS, fy - FPM_RADIUS, FPM_RADIUS*2+1, FPM_RADIUS*2+1)` — centre ring
- Left wing: `g.fill(fx - FPM_RADIUS - 8, fy, fx - FPM_RADIUS - 1, fy + 1, …)`
- Right wing: `g.fill(fx + FPM_RADIUS + 1, fy, fx + FPM_RADIUS + 8, fy + 1, …)`
- Tail: `g.fill(fx, fy - FPM_RADIUS - 4, fx + 1, fy - FPM_RADIUS - 1, …)`
- Centre dot: `g.fill(fx, fy, fx + 1, fy + 1, …)`

## Drawing primitives

### `drawHollowRect(g, x, y, w, h, color)`
Four `g.fill` calls — top edge, bottom edge, left edge, right edge.

### `drawLineThin(g, x1, y1, x2, y2, argb)`
Bresenham line drawing — one 1×1 `g.fill` per cell.
```
dx = |x2 - x1|;  dy = |y2 - y1|
sx = (x1 < x2) ? 1 : -1
sy = (y1 < y2) ? 1 : -1
err = dx - dy
loop until (x,y) == (x2,y2):
    g.fill(x, y, x+1, y+1, argb)
    e2 = err * 2
    if e2 > -dy: err -= dy; x += sx
    if e2 <  dx: err += dx; y += sy
```

### `fillTriangle(g, x1, y1, x2, y2, x3, y3, color)`

Sorts the three points by `y` ascending in-place. Then for each scanline `y` from `y1` to `y3`:
- `t13 = (y3 == y1) ? 0 : (y - y1) / (y3 - y1)` → `xA = lerp(x1, x3, t13)`
- If `y < y2`: `t12 = (y2 == y1) ? 0 : (y - y1) / (y2 - y1)` → `xB = lerp(x1, x2, t12)`
- Else: `t23 = (y3 == y2) ? 0 : (y - y2) / (y3 - y2)` → `xB = lerp(x2, x3, t23)`
- `g.fill(min(xA, xB), y, max(xA, xB) + 1, y + 1, color)`

All vertices are integer; lerp uses `Math.round(float)`.

### `drawSegmentRotated(g, cx, cy, lx1, ly1, lx2, ly2, cos, sin, color)`

Rotates two local-frame points around `(cx, cy)` via `rotate(...)`, then `drawLineThin` between the results.

### `drawDashedSegmentRotated(g, cx, cy, lx1, ly1, lx2, ly2, cos, sin, onPx, offPx, color)`

```
dx, dy = lx2-lx1, ly2-ly1
len = sqrt(dx² + dy²)
if len < 1: return
nx, ny = dx/len, dy/len
period = onPx + offPx
steps  = ceil(len / period)
for i in 0..steps-1:
    t0 = i * period
    t1 = min(t0 + onPx, len)
    drawSegmentRotated(g, cx, cy, lx1+nx*t0, ly1+ny*t0, lx1+nx*t1, ly1+ny*t1, cos, sin, color)
```

### `rotate(cx, cy, lx, ly, cos, sin) → int[2]`

Single-point rotation around `(cx, cy)`:
```
rx = cx + round(lx * cos - ly * sin)
ry = cy + round(lx * sin + ly * cos)
return [rx, ry]
```

### `applyAlpha(argb, factor) → int`

```
a      = (argb >>> 24) & 0xFF
scaled = clamp(round(a * factor), 0, 255)
return (scaled << 24) | (argb & 0x00FFFFFF)
```
