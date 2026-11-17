package com.cubes.template.shared.domain.game

import kotlinx.serialization.Serializable

/** Declared from weakest to strongest; `ordinal` is the strength. */
@Serializable
enum class HandRank {
    NOTHING,
    PAIR,
    TWO_PAIRS,
    THREE_OF_A_KIND,
    SMALL_STRAIGHT,
    LARGE_STRAIGHT,
    FULL_HOUSE,
    FOUR_OF_A_KIND,
    FIVE_OF_A_KIND,
}

/**
 * Evaluated hand. Two hands are compared by [rank] first, then [tiebreakers] element by element.
 * Equal hands are a draw.
 */
@Serializable
data class Hand(
    val rank: HandRank,
    val tiebreakers: List<Int>,
) : Comparable<Hand> {

    override fun compareTo(other: Hand): Int {
        val byRank = rank.compareTo(other.rank)
        if (byRank != 0) return byRank
        for (i in 0 until minOf(tiebreakers.size, other.tiebreakers.size)) {
            val cmp = tiebreakers[i].compareTo(other.tiebreakers[i])
            if (cmp != 0) return cmp
        }
        return tiebreakers.size.compareTo(other.tiebreakers.size)
    }
}
