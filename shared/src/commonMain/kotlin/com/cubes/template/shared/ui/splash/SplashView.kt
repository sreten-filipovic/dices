package com.cubes.template.shared.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.app_name
import com.cubes.template.shared.resources.ic_refresh
import com.cubes.template.shared.ui.common.ColorFeltDark
import com.cubes.template.shared.ui.common.ColorFeltLight
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.views.AnimatedDie
import com.cubes.template.shared.ui.common.views.DieAnimation
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

object SplashAnimation {
    const val START_DELAY_MILLIS = 250L

    private const val HOLD_MILLIS = 450L

    val totalMillis: Long get() = START_DELAY_MILLIS + DieAnimation.totalMillis() + HOLD_MILLIS
}

@Composable
fun SplashView(
    onRetry: () -> Unit,
    showRefresh: Boolean
) {
    var rollKey by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(SplashAnimation.START_DELAY_MILLIS)
        rollKey = 1
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(ColorFeltLight, ColorFeltDark))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(5) { index ->
                    AnimatedDie(
                        value = 6,
                        rollKey = rollKey,
                        size = 52.dp,
                        highlighted = true,
                        startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
                        soundEnabled = false,
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(Res.string.app_name),
                color = ColorWhite,
                fontSize = 40.sp,
                fontFamily = ExtraBoldFont(),
            )
        }

        if (showRefresh) {
            Image(
                painter = painterResource(Res.drawable.ic_refresh),
                contentDescription = "Retry",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .size(30.dp)
                    .clickable { onRetry() },
                colorFilter = ColorFilter.tint(ColorWhite)
            )
        }
    }
}

@Preview
@Composable
private fun SplashViewPreview() {
    SplashView(onRetry = {}, showRefresh = false)
}
