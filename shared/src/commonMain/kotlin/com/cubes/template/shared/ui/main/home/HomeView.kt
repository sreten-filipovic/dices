package com.cubes.template.shared.ui.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.home_new_game
import com.cubes.template.shared.resources.home_title
import com.cubes.template.shared.resources.home_try_die
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.views.DieFace
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeView(viewModel: HomeViewModel) {
    HomeScreen(viewData = viewModel.viewData)
}

@Composable
fun HomeScreen(viewData: Home.ViewData) {
    val listener = viewData.listener
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(6, 6, 6, 6, 6).forEach { DieFace(value = it, size = 48.dp, highlighted = true) }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.home_title),
            fontFamily = ExtraBoldFont(),
            fontSize = 36.sp,
            color = ColorTextPrimary,
        )
        Spacer(Modifier.height(48.dp))
        Button(
            text = stringResource(Res.string.home_new_game),
            onClick = listener::onNewGameClick,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Button(
            text = stringResource(Res.string.home_try_die),
            onClick = listener::onTryDieClick,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = ColorPrimaryLight,
            textColor = ColorPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme { HomeScreen(viewData = PreviewHelper.home()) }
}
