package com.cubes.template.shared.domain.game

object HandEvaluator {

    const val DICE_COUNT = 5

    fun evaluate(dice: List<Int>): Hand {
        require(dice.size == DICE_COUNT) { "Expected $DICE_COUNT dice, got ${dice.size}" }
        require(dice.all { it in 1..6 }) { "Die values must be 1..6: $dice" }

        val groups = dice.groupingBy { it }.eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenByDescending { it.key })
        val counts = groups.map { it.value }
        val values = groups.map { it.key }
        fun restSum(vararg exclude: Int) = dice.filter { it !in exclude }.sum()

        return when {
            counts == listOf(5) ->
                Hand(HandRank.FIVE_OF_A_KIND, listOf(values[0]))

            counts == listOf(4, 1) ->
                Hand(HandRank.FOUR_OF_A_KIND, listOf(values[0], values[1]))

            counts == listOf(3, 2) ->
                Hand(HandRank.FULL_HOUSE, listOf(values[0], values[1]))

            dice.sorted() == listOf(2, 3, 4, 5, 6) ->
                Hand(HandRank.LARGE_STRAIGHT, emptyList())

            dice.sorted() == listOf(1, 2, 3, 4, 5) ->
                Hand(HandRank.SMALL_STRAIGHT, emptyList())

            counts == listOf(3, 1, 1) ->
                Hand(HandRank.THREE_OF_A_KIND, listOf(values[0], restSum(values[0])))

            counts == listOf(2, 2, 1) ->
                Hand(HandRank.TWO_PAIRS, listOf(values[0] + values[1], values[2]))

            counts == listOf(2, 1, 1, 1) ->
                Hand(HandRank.PAIR, listOf(values[0], restSum(values[0])))

            else ->
                Hand(HandRank.NOTHING, listOf(dice.sum()))
        }
    }

    fun scoringIndices(dice: List<Int>): Set<Int> {
        val hand = evaluate(dice)
        return when (hand.rank) {
            HandRank.NOTHING -> emptySet()
            HandRank.SMALL_STRAIGHT, HandRank.LARGE_STRAIGHT,
            HandRank.FULL_HOUSE, HandRank.FIVE_OF_A_KIND -> dice.indices.toSet()
            else -> {
                val counts = dice.groupingBy { it }.eachCount()
                dice.indices.filter { counts.getValue(dice[it]) >= 2 }.toSet()
            }
        }
    }
}
