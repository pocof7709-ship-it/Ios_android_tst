package com.iosexperience.feature.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

class GridLayoutTest {
    @Test fun paginates() {
        val pages = (1..50).toList().paginate(GridConfig(6, 4))
        assertEquals(listOf(24, 24, 2), pages.map { it.size })
    }
    @Test fun moves() = assertEquals(listOf(2, 3, 1), listOf(1, 2, 3).moved(0, 2))
    @Test(expected = IllegalArgumentException::class) fun rejectsBadGrid() { GridConfig(1, 1) }
}
