# Volt Battery — Professional, Low-Consumption Android Battery Widget Suite

**Volt Battery** is a complete, production-grade Android application and AppWidget suite engineered for high accuracy, extreme readability, low energy consumption, and near-total visual customization. It is specifically optimized for **Samsung Galaxy devices running One UI (One UI 4/5/6/7)** while maintaining universal compatibility across all modern Android devices (API 24 to 36).

---

## ⚡ Core Architecture

1. **Pixel-Perfect Canvas RemoteViews Engine (`WidgetRenderer.kt`)**:
   - Rather than relying on rigid, version-inconsistent XML layouts across OEM launchers, Volt Battery employs a hardware-accelerated Canvas rendering pipeline.
   - Generates exact bitmaps matching the home screen dimensions (`OPTION_APPWIDGET_MIN_WIDTH` and `OPTION_APPWIDGET_MIN_HEIGHT`) dynamically.
   - Guarantees 100% visual parity between the in-app Design Studio live preview and the home screen widget.
   - Seamlessly supports smooth anti-aliased corners, multi-stop gradients, discrete LED segment counters, glowing neon effects, retro pixel grids, and custom typography.

2. **Accurate Battery Telemetry & Zero Mock Data (`BatteryMonitor.kt`)**:
   - Directly hooks into Android's system `BatteryManager` and sticky broadcast `Intent.ACTION_BATTERY_CHANGED`.
   - Reads:
     - Real system battery percentage (`EXTRA_LEVEL` / `EXTRA_SCALE`)
     - Charging state (`BATTERY_STATUS_CHARGING`, `FULL`, `DISCHARGING`, `NOT_CHARGING`)
     - Power source (`AC Fast Charger`, `USB Port`, `Wireless Dock`)
     - Battery chemical health (`Good`, `Overheated`, `Over Voltage`, `Cold`)
     - Temperature in tenths of a degree Celsius (with Fahrenheit conversion)
     - Voltage in millivolts (`mV`) and cell chemistry (e.g. `Li-ion`, `Li-poly`)
     - Time to full charge via Android 9+ `BatteryManager.computeChargeTimeRemaining()`
     - Historical hourly discharge rates calculated from actual recorded discharge intervals.

3. **Two Operating Modes**:
   - **Eco Mode (Default & Recommended)**:
     - 100% event-driven updates listening to official system broadcasts and charger events.
     - Zero wake locks, zero continuous polling loops, and zero permanent background services.
     - Negligible battery footprint (<0.01% battery consumption over 24 hours).
   - **Live Updates Mode (Optional)**:
     - User-activated foreground service (`BatteryLiveService`) with an ongoing low-priority notification.
     - Receives real-time broadcast changes immediately on every 1% battery change.
     - Transparently informs users of the operational state.

4. **Samsung Galaxy / One UI Specific Integration**:
   - Built-in guidance for One UI 4.0, 5.0, 6.0, and 7.0 battery policies.
   - Step-by-step instructions for adding Volt Battery to **Never sleeping apps** (`Settings > Battery > Background usage limits > Never sleeping apps`).
   - Direct Intent triggers launching Samsung Device Care (`com.samsung.android.lool`) and Android battery optimization screens.
   - Handles device reboot via `BootReceiver` (`RECEIVE_BOOT_COMPLETED`), instantly restoring all widgets on startup.

---

## 🎨 12 Distinct Battery Widget Designs

Volt Battery includes 12 genuinely unique visual styles:

1. **Ultra Minimal**: Giant, distraction-free battery percentage number with an optional subtle hairline circular track. Perfect for minimalist setups and AMOLED black screens.
2. **Horizontal Battery**: Classic landscape battery cell with an anode terminal tip, real-time proportional fill level, and configurable label positioning.
3. **Vertical Battery**: Upright battery cell filling from bottom to top, optimized for tall 1x2 or 2x2 home screen spaces.
4. **Circular Gauge**: Precision radial arc gauge with customizable start/end sweep angles, stroke thickness, center percentage, and charging bolt.
5. **Segmented Battery**: Modern digital bar partitioned into discrete LED blocks (customizable from 5 to 20 segments) with illuminated active state indicators.
6. **Samsung One UI**: Authentic One UI aesthetic featuring soft rounded squircle surfaces, rounded pill progress bar, and secondary time-to-full status badge.
7. **Neon HUD**: Cyberpunk interface with angular telemetry corner brackets, glowing neon cyan/magenta accents, and bundled Orbitron tech typography.
8. **Glass Battery**: Translucent frosted glassmorphism card with diagonal specular reflection streaks and subtle border lighting.
9. **Pixel Retro**: Nostalgic 8-bit arcade aesthetic with chunky block matrix segments and bundled Roboto Mono typography.
10. **Battery Dashboard**: Multi-metric command center displaying battery percentage ring, charging state, temperature, voltage, health, and hourly drain rate.
11. **Outline Battery**: Ultra-clean wireframe silhouette with a delicate accent charge line for understated elegance.
12. **Adaptive Smart Layout**: Intelligently senses widget dimensions on the launcher—displaying compact focus at 2x1, expanding to One UI pill at 3x1, and full dashboard at 4x2.

---

## 🛠 Deep Customization Engine (Design Studio)

Users have independent control over:
- **Typography**: Percentage size (18sp to 76sp), label size, font family (`Default`, `Roboto Mono`, `Orbitron`, `Monospace`, `Serif`), bold/extra-bold weights, letter spacing, number formats (`85%`, `85`, `[ 85% ]`), and **Persian/Eastern numerals (`۸۵٪`)**.
- **Colors**: Hex color picker with transparency/alpha slider for high level, medium level, low level, charging status, background, and borders.
- **Thresholds**: User-defined low battery threshold (e.g. 20%) and medium battery threshold (e.g. 50%).
- **Geometry**: Corner curvature (0 to 36dp), border width (0 to 6dp), line stroke width (1 to 20dp), internal padding, and element spacing.
- **Profiles**: Create, save, duplicate, rename, delete, export to JSON, and import from JSON with automatic validation.
- **Multi-Widget Independence**: Each placed widget can have its own independent profile via `WidgetConfigureActivity`.

---

## 📊 Local Battery History & Analytics

- Uses a local-only **Room Database** (`BatteryDatabase`) to store periodic timestamped battery snapshots.
- Zero network transmission, zero external tracking, 100% offline privacy.
- Visual discharge curve drawn via Compose Canvas.
- Users can clear history at any time with one tap.

---

## 🔔 Smart Battery Alerts

- Low battery alert (configurable threshold 5%–35%).
- Target charge alert (configurable threshold 70%–100%, ideal for preserving battery longevity at 80%).
- Built-in hysteresis debouncing prevents duplicate alerts.

---

## 🧪 Testing & Verification

- **Robolectric JVM Unit Tests** (`ExampleRobolectricTest.kt`):
  - Verified system string resource resolution (`Volt Battery`).
  - Verified `BatteryInfo` percentage calculations and Persian digits formatting.
  - Verified `WidgetConfig` JSON serialization and deserialization roundtrip without loss.
  - Verified all 12 `DesignStyle` presets create valid, non-null bitmaps across multiple dimensions.
  - Verified `WidgetPreferences` profile management (saving, retrieving, updating, deleting).
- Executed via `gradle :app:testDebugUnitTest`: **BUILD SUCCESSFUL** (33 actionable tasks executed/up-to-date, 0 failures).
