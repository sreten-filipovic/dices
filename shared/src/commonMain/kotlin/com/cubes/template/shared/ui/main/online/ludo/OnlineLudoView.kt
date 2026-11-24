package com.cubes.template.shared.ui.main.online.ludo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.views.LoadingIndicator
import com.cubes.template.shared.ui.main.ludo.LudoScreen

@Composable
fun OnlineLudoView(viewModel: OnlineLudoViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    when (val data = viewData) {
        null -> LoadingIndicator(modifier = Modifier.fillMaxSize().background(ColorBackground))
        else -> LudoScreen(viewData = data)
    }
}
