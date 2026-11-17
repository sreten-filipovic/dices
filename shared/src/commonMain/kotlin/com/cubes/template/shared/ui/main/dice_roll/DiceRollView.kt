package com.cubes.template.shared.ui.main.dice_roll

import androidx.compose.foundation.background
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
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.views.AnimatedDie
import com.cubes.template.shared.ui.common.views.DiceTable
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
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            DiceTable(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(140.dp))
                AnimatedDie(value = viewData.value, rollKey = viewData.rollKey, size = 140.dp)
                Spacer(Modifier.height(12.dp))
                val showResult = viewData.value != null && !viewData.rolling
                Text(
                    text = if (showResult) viewData.value.toString() else stringResource(Res.string.dice_roll_hint),
                    fontFamily = if (showResult) ExtraBoldFont() else RegularFont(),
                    fontSize = if (showResult) 48.sp else 16.sp,
                    color = ColorWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.height(64.dp),
                )
            }
        }

        Button(
            text = stringResource(Res.string.roll),
            onClick = listener::onRollClick,
            enabled = !viewData.rolling,
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
