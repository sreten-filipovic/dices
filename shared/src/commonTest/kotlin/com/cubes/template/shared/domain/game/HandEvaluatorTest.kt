package com.cubes.template.shared.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandEvaluatorTest {

    private fun hand(vararg dice: Int) = HandEvaluator.evaluate(dice.toList())
    private fun rank(vararg dice: Int) = hand(*dice).rank

    @Test
    fun recognizesEveryRank() {
        assertEquals(HandRank.FIVE_OF_A_KIND, rank(3, 3, 3, 3, 3))
        assertEquals(HandRank.FOUR_OF_A_KIND, rank(2, 2, 6, 2, 2))
        assertEquals(HandRank.FULL_HOUSE, rank(4, 1, 4, 1, 4))
        assertEquals(HandRank.LARGE_STRAIGHT, rank(6, 2, 5, 3, 4))
        assertEquals(HandRank.SMALL_STRAIGHT, rank(5, 4, 3, 2, 1))
        assertEquals(HandRank.THREE_OF_A_KIND, rank(5, 5, 5, 1, 2))
        assertEquals(HandRank.TWO_PAIRS, rank(1, 1, 6, 6, 3))
        assertEquals(HandRank.PAIR, rank(2, 2, 4, 5, 6))
        assertEquals(HandRank.NOTHING, rank(1, 2, 3, 4, 6))
    }

    @Test
    fun rankOrder() {
        val ordered = listOf(
            hand(1, 2, 3, 4, 6),
            hand(6, 6, 1, 2, 3),
            hand(1, 1, 2, 2, 3),
            hand(1, 1, 1, 2, 3),
            hand(1, 2, 3, 4, 5),
            hand(2, 3, 4, 5, 6),
            hand(1, 1, 1, 2, 2),
            hand(1, 1, 1, 1, 2),
            hand(1, 1, 1, 1, 1),
        )
        ordered.zipWithNext().forEach { (weaker, stronger) -> assertTrue(weaker < stronger, "$weaker < $stronger") }
    }

    @Test
    fun pairComparesPairThenRest() {
        assertTrue(hand(5, 5, 1, 2, 3) > hand(4, 4, 6, 3, 1))
        assertTrue(hand(5, 5, 6, 3, 2) > hand(5, 5, 6, 3, 1))
        // same pair, same sum of the rest (10) -> draw
        assertEquals(0, hand(5, 5, 6, 3, 1).compareTo(hand(1, 5, 3, 6, 5)))
        // rest is summed, not compared die by die: 5+4+3 = 12 > 6+3+1 = 10
        assertTrue(hand(2, 2, 5, 4, 3) > hand(2, 2, 6, 3, 1))
    }

    @Test
    fun twoPairsComparesSumThenKicker() {
        // 6+1 = 7 < 5+3 = 8
        assertTrue(hand(6, 6, 1, 1, 2) < hand(5, 5, 3, 3, 1))
        // same sum (6+2 = 5+3): kicker decides
        assertTrue(hand(6, 6, 2, 2, 4) > hand(5, 5, 3, 3, 1))
        assertEquals(0, hand(6, 6, 2, 2, 4).compareTo(hand(5, 5, 3, 3, 4)))
    }

    @Test
    fun threeOfAKindComparesTripleThenRest() {
        assertTrue(hand(4, 4, 4, 1, 2) > hand(3, 3, 3, 6, 5))
        assertTrue(hand(4, 4, 4, 6, 2) > hand(4, 4, 4, 1, 3))
    }

    @Test
    fun fullHouseComparesTripleThenPair() {
        assertTrue(hand(6, 6, 6, 1, 1) > hand(5, 5, 5, 6, 6))
        assertTrue(hand(5, 5, 5, 3, 3) > hand(5, 5, 5, 2, 2))
    }

    @Test
    fun pokersAndStraights() {
        assertTrue(hand(3, 3, 3, 3, 1) > hand(2, 2, 2, 2, 6))
        assertTrue(hand(3, 3, 3, 3, 4) > hand(3, 3, 3, 3, 1))
        assertTrue(hand(6, 6, 6, 6, 6) > hand(5, 5, 5, 5, 5))
        assertEquals(0, hand(1, 2, 3, 4, 5).compareTo(hand(5, 3, 1, 2, 4)))
    }

    @Test
    fun nothingComparesSum() {
        assertTrue(hand(1, 3, 4, 5, 6) > hand(1, 2, 3, 4, 6))
        assertEquals(0, hand(1, 2, 4, 5, 6).compareTo(hand(6, 5, 4, 2, 1)))
    }

    @Test
    fun scoringIndices() {
        assertEquals(setOf(0, 2), HandEvaluator.scoringIndices(listOf(4, 1, 4, 2, 6)))
        assertEquals(setOf(0, 1, 2, 3, 4), HandEvaluator.scoringIndices(listOf(1, 2, 3, 4, 5)))
        assertEquals(emptySet(), HandEvaluator.scoringIndices(listOf(1, 2, 3, 4, 6)))
    }
}
