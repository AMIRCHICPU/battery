package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BatteryHealth
import com.example.data.model.BatteryInfo
import com.example.data.model.BatteryStatus
import com.example.data.model.DesignStyle
import com.example.data.model.WidgetConfig
import com.example.data.preferences.WidgetPreferences
import com.example.widget.WidgetRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Volt Battery", appName)
    }

    @Test
    fun `test battery info formatting and persian numerals`() {
        val info = BatteryInfo(
            level = 85,
            scale = 100,
            percentage = 85,
            status = BatteryStatus.CHARGING,
            isCharging = true
        )

        assertEquals("85%", info.getFormattedPercentage(usePersianNumerals = false))
        assertEquals("۸۵٪", info.getFormattedPercentage(usePersianNumerals = true))
        assertEquals("85", info.getFormattedPercentage(usePersianNumerals = false, includePercentSign = false))
        assertEquals("۸۵", info.getFormattedPercentage(usePersianNumerals = true, includePercentSign = false))
    }

    @Test
    fun `test widget config json serialization roundtrip`() {
        val original = WidgetConfig(
            profileId = "test_profile_1",
            profileName = "My Custom Neon",
            style = DesignStyle.NEON_HUD,
            percentageTextSizeSp = 48f,
            usePersianNumerals = true,
            batteryHighColor = 0xFF00FF9D,
            cornerRadiusDp = 14f,
            segmentCount = 12
        )

        val json = original.toJson()
        val parsed = WidgetConfig.fromJson(json)

        assertNotNull("Deserialized config should not be null", parsed)
        assertEquals(original.profileId, parsed?.profileId)
        assertEquals(original.profileName, parsed?.profileName)
        assertEquals(original.style, parsed?.style)
        assertEquals(original.percentageTextSizeSp, parsed?.percentageTextSizeSp)
        assertEquals(original.usePersianNumerals, parsed?.usePersianNumerals)
        assertEquals(original.batteryHighColor, parsed?.batteryHighColor)
        assertEquals(original.cornerRadiusDp, parsed?.cornerRadiusDp)
        assertEquals(original.segmentCount, parsed?.segmentCount)
    }

    @Test
    fun `test all 12 design style presets exist and render bitmaps without error`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dummyBattery = BatteryInfo(percentage = 74, status = BatteryStatus.DISCHARGING, isCharging = false)

        for (style in DesignStyle.entries) {
            val preset = WidgetConfig.getPresetForStyle(style)
            assertEquals(style, preset.style)

            val bitmap = WidgetRenderer.renderWidgetBitmap(
                context = context,
                config = preset,
                battery = dummyBattery,
                widthDp = 220,
                heightDp = 90
            )

            assertNotNull("Bitmap for ${style.displayName} must not be null", bitmap)
            assertTrue("Bitmap width should be positive", bitmap.width > 0)
            assertTrue("Bitmap height should be positive", bitmap.height > 0)
        }
    }

    @Test
    fun `test widget preferences profile management`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = WidgetPreferences(context)

        val custom = WidgetConfig(
            profileId = "unit_test_id",
            profileName = "Unit Test Profile",
            style = DesignStyle.ONE_UI
        )

        prefs.saveProfile(custom)
        val saved = prefs.getSavedProfiles()
        val found = saved.find { it.profileId == "unit_test_id" }
        assertNotNull("Saved profile should be present", found)
        assertEquals("Unit Test Profile", found?.profileName)

        prefs.deleteProfile("unit_test_id")
        val updated = prefs.getSavedProfiles()
        assertTrue("Deleted profile should not be present", updated.none { it.profileId == "unit_test_id" })
    }
}
