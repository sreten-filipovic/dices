package com.cubes.template.shared.ui.main.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.settings_theme
import com.cubes.template.shared.resources.settings_theme_dark
import com.cubes.template.shared.resources.settings_theme_light
import com.cubes.template.shared.resources.settings_title
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorDivider
import com.cubes.template.shared.ui.common.ColorOnPrimary
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.ShapeLarge
import com.cubes.template.shared.ui.common.ShapeMedium
import com.cubes.template.shared.ui.common.ThemeMode
import com.cubes.template.shared.ui.common.views.Toolbar
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsView(viewModel: SettingsViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    SettingsScreen(viewData = viewData)
}

@Composable
fun SettingsScreen(viewData: Settings.ViewData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Toolbar(title = stringResource(Res.string.settings_title))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ThemeSection(selected = viewData.themeMode, onSelect = viewData.listener::onThemeModeClick)
        }
    }
}

@Composable
private fun ThemeSection(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLarge)
            .background(ColorSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.settings_theme),
            fontFamily = BoldFont(),
            fontSize = 15.sp,
            color = ColorTextSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ThemeMode.entries.forEach { mode ->
                ThemeOption(
                    text = stringResource(
                        when (mode) {
                            ThemeMode.LIGHT -> Res.string.settings_theme_light
                            ThemeMode.DARK -> Res.string.settings_theme_dark
                        }
                    ),
                    isSelected = mode == selected,
                    onClick = { onSelect(mode) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontFamily = if (isSelected) BoldFont() else SemiBoldFont(),
        fontSize = 17.sp,
        color = if (isSelected) ColorOnPrimary else ColorPrimary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(ShapeMedium)
            .background(if (isSelected) ColorPrimary else ColorPrimaryLight)
            .border(1.dp, if (isSelected) ColorPrimary else ColorDivider, ShapeMedium)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 14.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            Settings.ViewData(
                listener = object : SettingsViewListener {
                    override fun onThemeModeClick(mode: ThemeMode) = Unit
                },
            )
        )
    }
}
