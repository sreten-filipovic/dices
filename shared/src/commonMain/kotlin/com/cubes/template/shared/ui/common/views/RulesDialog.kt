package com.cubes.template.shared.ui.common.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.game_dice_duel
import com.cubes.template.shared.resources.game_dice_jack
import com.cubes.template.shared.resources.game_dice_poker
import com.cubes.template.shared.resources.game_sevens
import com.cubes.template.shared.resources.ok
import com.cubes.template.shared.resources.rules_dice_duel
import com.cubes.template.shared.resources.rules_dice_jack
import com.cubes.template.shared.resources.rules_dice_poker
import com.cubes.template.shared.resources.rules_sevens
import com.cubes.template.shared.resources.rules_title
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.popup.popupDialogProperties
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class GameRules(val game: StringResource, val text: StringResource) {
    DICE_POKER(game = Res.string.game_dice_poker, text = Res.string.rules_dice_poker),
    DICE_DUEL(game = Res.string.game_dice_duel, text = Res.string.rules_dice_duel),
    SEVENS(game = Res.string.game_sevens, text = Res.string.rules_sevens),
    DICE_JACK(game = Res.string.game_dice_jack, text = Res.string.rules_dice_jack),
}

@Composable
fun RulesDialog(rules: GameRules, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = popupDialogProperties()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBlack.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            RulesCard(rules = rules, onDismiss = onDismiss)
        }
    }
}

@Composable
private fun RulesCard(rules: GameRules, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ColorSurface)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.rules_title),
                fontFamily = SemiBoldFont(),
                fontSize = 14.sp,
                color = ColorTextSecondary,
            )
            Text(
                text = stringResource(rules.game).uppercase(),
                fontFamily = ExtraBoldFont(),
                fontSize = 24.sp,
                letterSpacing = 1.sp,
                color = ColorTextPrimary,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = stringResource(rules.text),
            fontFamily = RegularFont(),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = ColorTextPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
        )
        Button(
            text = stringResource(Res.string.ok),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun RulesCardPreview() {
    RulesCard(rules = GameRules.SEVENS, onDismiss = {})
}
