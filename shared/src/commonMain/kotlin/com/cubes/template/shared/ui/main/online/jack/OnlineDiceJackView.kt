package com.cubes.template.shared.ui.main.online.jack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.views.LoadingIndicator
import com.cubes.template.shared.ui.main.dice_jack.DiceJackScreen

@Composable
fun OnlineDiceJackView(viewModel: OnlineDiceJackViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    when (val data = viewData) {
        null -> LoadingIndicator(modifier = Modifier.fillMaxSize().background(ColorBackground))
        else -> DiceJackScreen(viewData = data)
    }
}
