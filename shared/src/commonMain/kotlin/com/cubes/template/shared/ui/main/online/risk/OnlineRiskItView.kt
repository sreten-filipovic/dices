package com.cubes.template.shared.ui.main.online.risk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.views.LoadingIndicator
import com.cubes.template.shared.ui.main.risk_it.RiskItScreen

@Composable
fun OnlineRiskItView(viewModel: OnlineRiskItViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    when (val data = viewData) {
        null -> LoadingIndicator(modifier = Modifier.fillMaxSize().background(ColorBackground))
        else -> RiskItScreen(viewData = data)
    }
}
