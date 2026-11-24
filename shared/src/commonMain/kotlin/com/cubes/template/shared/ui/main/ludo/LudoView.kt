package com.cubes.template.shared.ui.main.ludo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.game_ludo
import com.cubes.template.shared.resources.ludo_coming_soon
import com.cubes.template.shared.resources.ludo_coming_soon_message
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.main.home.LudoArtwork
import org.jetbrains.compose.resources.stringResource

@Composable
fun LudoView(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(ColorBackground)) {
        Toolbar(onBackClick = onBack, title = stringResource(Res.string.game_ludo))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LudoArtwork(pawnHeight = 64.dp, dieSize = 48.dp)
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(Res.string.ludo_coming_soon),
                fontFamily = ExtraBoldFont(),
                fontSize = 28.sp,
                color = ColorTextPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.ludo_coming_soon_message),
                fontFamily = RegularFont(),
                fontSize = 16.sp,
                color = ColorTextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview
@Composable
private fun LudoViewPreview() {
    AppTheme { LudoView(onBack = {}) }
}
