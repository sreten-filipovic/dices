package com.cubes.template.shared.ui.common.views.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cubes.template.shared.ui.common.ColorPaletteSand
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.views.LoadingIndicator

@Composable
fun LoadingButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    backgroundColor: Color = ColorPrimary,
    textColor: Color = ColorWhite,
    borderColor: Color = ColorPaletteSand,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Button(
            text = text,
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !isLoading,
            backgroundColor = backgroundColor,
            textColor = textColor,
            borderColor = borderColor,
        )

        if (isLoading) {
            LoadingIndicator(color = textColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingButtonPreview() {
    LoadingButton(
        text = "Button",
        onClick = {},
        isLoading = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
    )
}
