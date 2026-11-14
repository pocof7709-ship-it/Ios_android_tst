package com.iosexperience.app

import androidx.compose.ui.graphics.Color
import com.iosexperience.app.ui.batteryFillColor
import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryColorTest {
    @Test fun chargingIsGreen() = assertEquals(Color(0xFF30D158), batteryFillColor(10, true))
    @Test fun lowIsRed() = assertEquals(Color(0xFFFF453A), batteryFillColor(20, false))
    @Test fun normalIsWhite() = assertEquals(Color.White, batteryFillColor(21, false))
}
