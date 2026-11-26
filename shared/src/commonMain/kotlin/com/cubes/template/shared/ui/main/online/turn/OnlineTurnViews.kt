package com.cubes.template.shared.ui.main.online.turn

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.online_next_round_soon
import com.cubes.template.shared.resources.online_playing
import com.cubes.template.shared.resources.online_seconds_left
import com.cubes.template.shared.ui.common.ColorAccent
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.ColorWhite
import org.jetbrains.compose.resources.stringResource

private const val TIMER_WARNING_SECONDS = 10

@Composable
fun OnlineTimer(secondsLeft: Int) {
    Text(
        text = stringResource(Res.string.online_seconds_left, secondsLeft),
        fontFamily = SemiBoldFont(),
        fontSize = 15.sp,
        color = if (secondsLeft <= TIMER_WARNING_SECONDS) ColorAccent else ColorTextSecondary,
    )
}

@Composable
fun NextRoundSoon() {
    Text(
        text = stringResource(Res.string.online_next_round_soon),
        fontFamily = SemiBoldFont(),
        fontSize = 15.sp,
        color = ColorTextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun OnlineWatching(playerName: String) {
    Text(
        text = stringResource(Res.string.online_playing, playerName),
        fontFamily = SemiBoldFont(),
        fontSize = 15.sp,
        color = ColorWhite.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
        modifier = Modifier.height(56.dp),
    )
}
