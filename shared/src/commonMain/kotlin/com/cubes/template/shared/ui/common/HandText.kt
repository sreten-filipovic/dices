package com.cubes.template.shared.ui.common

import androidx.compose.runtime.Composable
import com.cubes.template.shared.domain.game.HandRank
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.hand_five_of_a_kind
import com.cubes.template.shared.resources.hand_four_of_a_kind
import com.cubes.template.shared.resources.hand_full_house
import com.cubes.template.shared.resources.hand_large_straight
import com.cubes.template.shared.resources.hand_nothing
import com.cubes.template.shared.resources.hand_pair
import com.cubes.template.shared.resources.hand_small_straight
import com.cubes.template.shared.resources.hand_three_of_a_kind
import com.cubes.template.shared.resources.hand_two_pairs
import org.jetbrains.compose.resources.stringResource

@Composable
fun handName(rank: HandRank): String = stringResource(
    when (rank) {
        HandRank.NOTHING -> Res.string.hand_nothing
        HandRank.PAIR -> Res.string.hand_pair
        HandRank.TWO_PAIRS -> Res.string.hand_two_pairs
        HandRank.THREE_OF_A_KIND -> Res.string.hand_three_of_a_kind
        HandRank.SMALL_STRAIGHT -> Res.string.hand_small_straight
        HandRank.LARGE_STRAIGHT -> Res.string.hand_large_straight
        HandRank.FULL_HOUSE -> Res.string.hand_full_house
        HandRank.FOUR_OF_A_KIND -> Res.string.hand_four_of_a_kind
        HandRank.FIVE_OF_A_KIND -> Res.string.hand_five_of_a_kind
    }
)
