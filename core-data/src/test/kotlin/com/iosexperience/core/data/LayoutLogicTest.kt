package com.iosexperience.core.data

import org.junit.Assert.*
import org.junit.Test

class LayoutLogicTest {
    private fun apps(vararg k: String) = k.map { HomeEntry.App(it) }

    @Test fun syncRemovesMissingExcludesDockAndAddsNew() {
        val r = LayoutLogic.sync(apps("a", "gone"), listOf("a", "b", "dock"), setOf("dock"))
        assertEquals(apps("a", "b"), r)
    }
    @Test fun syncDissolvesTinyFolder() {
        val f = HomeEntry.Folder("f", "P", listOf("a", "x"))
        assertEquals(apps("a"), LayoutLogic.sync(listOf(f), listOf("a"), emptySet()))
    }
    @Test fun mergeCreatesFolder() {
        val r = LayoutLogic.mergeInto(apps("a", "b", "c"), from = 0, to = 2, newId = "F")
        assertEquals(2, r.size)
        assertEquals(HomeEntry.Folder("F", "Pasta", listOf("c", "a")), r[1])
    }
    @Test fun mergeIntoExistingFolder() {
        val l = listOf(HomeEntry.Folder("F", "P", listOf("x", "y")), HomeEntry.App("a"))
        val r = LayoutLogic.mergeInto(l, 1, 0, "N")
        assertEquals(listOf("x", "y", "a"), (r.single() as HomeEntry.Folder).keys)
    }
    @Test fun removeFromBigFolderKeepsFolder() {
        val l = listOf(HomeEntry.Folder("F", "P", listOf("x", "y", "z")))
        val r = LayoutLogic.removeFromFolder(l, "F", "z")
        assertEquals(2, r.size); assertEquals(HomeEntry.App("z"), r[1])
    }
    @Test fun removeFromPairDissolves() {
        val l = listOf<HomeEntry>(HomeEntry.Folder("F", "P", listOf("x", "y")))
        assertEquals(apps("x", "y"), LayoutLogic.removeFromFolder(l, "F", "y"))
    }
    @Test fun reorder() = assertEquals(apps("b", "c", "a"), LayoutLogic.reorder(apps("a", "b", "c"), 0, 2))
    @Test fun renameClampsAndDefaults() {
        val l = listOf<HomeEntry>(HomeEntry.Folder("F", "P", listOf("x", "y")))
        assertEquals("Pasta", (LayoutLogic.rename(l, "F", "  ")[0] as HomeEntry.Folder).name)
    }
    @Test fun settingsSanitized() {
        val s = UiSettings(gridRows = 99, iconCorner = 0, accentIndex = -3).sanitized()
        assertEquals(8, s.gridRows); assertEquals(10, s.iconCorner); assertEquals(0, s.accentIndex)
    }
}
