package com.cubes.template.shared.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.app_name
import com.cubes.template.shared.resources.ic_refresh
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.views.LoadingIndicator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SplashView(
    onRetry: () -> Unit,
    showRefresh: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPrimary),
        contentAlignment = Alignment.Center
    ) {
        // Replace with the app logo.
        Text(
            text = stringResource(Res.string.app_name),
            color = ColorWhite,
            fontSize = 32.sp,
            fontFamily = ExtraBoldFont(),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        ) {
            if (showRefresh) {
                Image(
                    painter = painterResource(Res.drawable.ic_refresh),
                    contentDescription = "Retry",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { onRetry() },
                    colorFilter = ColorFilter.tint(ColorWhite)
                )
            } else {
                LoadingIndicator(color = ColorWhite)
            }
        }
    }
}

@Preview
@Composable
private fun SplashViewPreview() {
    SplashView(onRetry = {}, showRefresh = false)
}
