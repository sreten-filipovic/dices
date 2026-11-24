package com.cubes.template.shared.ui.common.views

import com.cubes.template.shared.ui.common.views.PawnColor.BLACK
import com.cubes.template.shared.ui.common.views.PawnColor.BLUE
import com.cubes.template.shared.ui.common.views.PawnColor.GREEN
import com.cubes.template.shared.ui.common.views.PawnColor.PURPLE
import com.cubes.template.shared.ui.common.views.PawnColor.RED
import com.cubes.template.shared.ui.common.views.PawnColor.YELLOW
import kotlin.test.Test
import kotlin.test.assertEquals

class PawnColorTest {

    @Test
    fun withoutChoicesPlayersGetTheDefaultColors() {
        assertEquals(listOf(BLUE, RED), PawnColor.resolve(listOf(null, null)))
        assertEquals(listOf(BLUE, YELLOW, RED), PawnColor.resolve(listOf(null, null, null)))
        assertEquals(listOf(BLUE, YELLOW, RED, GREEN), PawnColor.resolve(listOf(null, null, null, null)))
    }

    @Test
    fun chosenColorsAreKept() {
        assertEquals(listOf(PURPLE, BLACK), PawnColor.resolve(listOf(PURPLE, BLACK)))
    }

    @Test
    fun aDefaultTakenByAnotherPlayerIsSkipped() {
        assertEquals(listOf(BLUE, RED), PawnColor.resolve(listOf(null, RED)))
        assertEquals(listOf(YELLOW, BLUE, RED), PawnColor.resolve(listOf(null, BLUE, null)))
        assertEquals(listOf(RED, BLUE), PawnColor.resolve(listOf(RED, null)))
    }
}
