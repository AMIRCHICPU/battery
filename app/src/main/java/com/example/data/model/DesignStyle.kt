package com.example.data.model

enum class DesignStyle(
    val id: String,
    val displayName: String,
    val category: String,
    val description: String
) {
    ULTRA_MINIMAL(
        id = "ultra_minimal",
        displayName = "Ultra Minimal",
        category = "Minimalist",
        description = "Extra-large percentage focal point with clean transparent or dark backdrop and optional subtle ring."
    ),
    HORIZONTAL_BATTERY(
        id = "horizontal_battery",
        displayName = "Horizontal Battery",
        category = "Classic",
        description = "Landscape battery cell with real-time level fill, terminal anode, and customizable label positioning."
    ),
    VERTICAL_BATTERY(
        id = "vertical_battery",
        displayName = "Vertical Battery",
        category = "Classic",
        description = "Classic vertical cell filling from bottom to top, optimized for tall or single-column home screen spaces."
    ),
    CIRCULAR_GAUGE(
        id = "circular_gauge",
        displayName = "Circular Gauge",
        category = "Modern",
        description = "Precision radial arc gauge with customizable ring thickness, center percentage, and optional tick marks."
    ),
    SEGMENTED_BATTERY(
        id = "segmented_battery",
        displayName = "Segmented Battery",
        category = "Digital",
        description = "Digital bar partitioned into distinct LED-style blocks with illuminated active state indicators."
    ),
    ONE_UI(
        id = "one_ui",
        displayName = "Samsung One UI",
        category = "System",
        description = "Clean aesthetic inspired by One UI with soft rounded squircle surfaces and modern pill status bars."
    ),
    NEON_HUD(
        id = "neon_hud",
        displayName = "Neon HUD",
        category = "Futuristic",
        description = "Cyberpunk interface with angular telemetry brackets, neon glow effects, and digital readouts."
    ),
    GLASS_BATTERY(
        id = "glass_battery",
        displayName = "Glass Battery",
        category = "Aesthetic",
        description = "Frosted glass card with subtle edge borders, specular highlights, and high-readability typography."
    ),
    PIXEL_RETRO(
        id = "pixel_retro",
        displayName = "Pixel Retro",
        category = "Retro",
        description = "Nostalgic 8-bit aesthetic featuring blocky pixel font numerals and chunky segment matrix graphics."
    ),
    DASHBOARD(
        id = "dashboard",
        displayName = "Battery Dashboard",
        category = "Informative",
        description = "Comprehensive multi-metric widget displaying drain rate, temperature, voltage, health, and trend sparkline."
    ),
    OUTLINE_BATTERY(
        id = "outline_battery",
        displayName = "Outline Battery",
        category = "Minimalist",
        description = "Delicate wireframe silhouette with an ultra-light inner charge line for distraction-free setups."
    ),
    ADAPTIVE_SMART(
        id = "adaptive_smart",
        displayName = "Adaptive Smart Layout",
        category = "Dynamic",
        description = "Intelligently transforms its layout from ultra-compact percentage to full multi-metric dashboard as resized."
    );

    companion object {
        fun fromId(id: String): DesignStyle = entries.find { it.id == id } ?: ULTRA_MINIMAL
    }
}
