package com.cubes.template.shared.ui.main.demo_details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.demo_details_title
import com.cubes.template.shared.resources.demo_submit
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.LoadingButton
import com.cubes.template.shared.ui.common.views.states.ErrorState
import com.cubes.template.shared.ui.common.views.states.LoadingState
import org.jetbrains.compose.resources.stringResource

@Composable
fun DemoDetailsView(viewModel: DemoDetailsViewModel) {
    val viewState by viewModel.viewState.collectAsState()
    DemoDetailsScreen(viewState = viewState)
}

@Composable
fun DemoDetailsScreen(viewState: DemoDetails.ViewState) {
    val data = viewState.data
    val listener = data.listener

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Toolbar(
            title = stringResource(Res.string.demo_details_title),
            onBackClick = listener::onBackClick,
        )

        when (viewState) {
            is DemoDetails.ViewState.Loading -> LoadingState()
            is DemoDetails.ViewState.Error -> ErrorState(onRefresh = listener::onRefresh)
            is DemoDetails.ViewState.Success -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(
                        text = data.item.title,
                        fontFamily = ExtraBoldFont(),
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        color = ColorTextPrimary,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = data.item.description,
                        fontFamily = RegularFont(),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = ColorTextSecondary,
                    )
                }

                LoadingButton(
                    text = stringResource(Res.string.demo_submit),
                    onClick = listener::onSubmitClick,
                    isLoading = data.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .navigationBarsPadding(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoDetailsScreenPreview() {
    AppTheme {
        DemoDetailsScreen(viewState = PreviewHelper.demoDetailsSuccess())
    }
}
