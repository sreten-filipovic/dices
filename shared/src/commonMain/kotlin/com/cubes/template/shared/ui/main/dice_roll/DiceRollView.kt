package com.cubes.template.shared.ui.main.dice_roll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.dice_roll_hint
import com.cubes.template.shared.resources.dice_roll_title
import com.cubes.template.shared.resources.roll
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.views.DieFace
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.stringResource

@Composable
fun DiceRollView(viewModel: DiceRollViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    DiceRollScreen(viewData = viewData)
}

@Composable
fun DiceRollScreen(viewData: DiceRoll.ViewData) {
    val listener = viewData.listener
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Toolbar(title = stringResource(Res.string.dice_roll_title), onBackClick = listener::onBackClick)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                DieFace(value = viewData.value, size = 140.dp)
                Text(
                    text = viewData.value?.toString() ?: stringResource(Res.string.dice_roll_hint),
                    fontFamily = if (viewData.value != null) ExtraBoldFont() else RegularFont(),
                    fontSize = if (viewData.value != null) 48.sp else 16.sp,
                    color = if (viewData.value != null) ColorTextPrimary else ColorTextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Button(
            text = stringResource(Res.string.roll),
            onClick = listener::onRollClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding(),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun DiceRollScreenPreview() {
    AppTheme { DiceRollScreen(viewData = PreviewHelper.diceRoll(value = 5)) }
}
