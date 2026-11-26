package com.cubes.template.shared.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.tab_games
import com.cubes.template.shared.resources.tab_profile
import com.cubes.template.shared.resources.tab_settings
import com.cubes.template.shared.resources.tab_shop
import com.cubes.template.shared.resources.ic_settings
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorDivider
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.views.CartIcon
import com.cubes.template.shared.ui.common.views.DiceIcon
import com.cubes.template.shared.ui.common.views.PersonIcon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

enum class MainTab(val enabled: Boolean) {
    GAMES(enabled = true),
    SHOP(enabled = false),
    PROFILE(enabled = true),
    SETTINGS(enabled = true),
}

@Composable
fun MainTabBar(selected: MainTab, onSelect: (MainTab) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().background(ColorSurface)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(ColorDivider))
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            MainTab.entries.forEach { tab ->
                TabItem(
                    tab = tab,
                    isSelected = tab == selected,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TabItem(tab: MainTab, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = if (isSelected) ColorPrimary else ColorTextSecondary
    Column(
        modifier = modifier
            .clickable(enabled = tab.enabled && !isSelected, role = Role.Tab, onClick = onClick)
            .alpha(if (tab.enabled) 1f else 0.35f)
            .padding(top = 8.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TabIcon(tab, color)
        Text(
            text = stringResource(
                when (tab) {
                    MainTab.GAMES -> Res.string.tab_games
                    MainTab.SHOP -> Res.string.tab_shop
                    MainTab.PROFILE -> Res.string.tab_profile
                    MainTab.SETTINGS -> Res.string.tab_settings
                }
            ),
            fontFamily = if (isSelected) BoldFont() else SemiBoldFont(),
            fontSize = 12.sp,
            color = color,
        )
    }
}

@Composable
private fun TabIcon(tab: MainTab, color: Color) = when (tab) {
    MainTab.GAMES -> DiceIcon(color)
    MainTab.SHOP -> CartIcon(color)
    MainTab.PROFILE -> PersonIcon(color)
    MainTab.SETTINGS -> Icon(
        painter = painterResource(Res.drawable.ic_settings),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp),
    )
}

@Preview
@Composable
private fun MainTabBarPreview() {
    MainTabBar(selected = MainTab.GAMES, onSelect = {})
}
